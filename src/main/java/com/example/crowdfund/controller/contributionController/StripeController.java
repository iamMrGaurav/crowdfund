package com.example.crowdfund.controller.contributionController;

import com.example.crowdfund.dto.request.ContributionRequest;
import com.example.crowdfund.dto.response.PaymentNotificationResponse;
import com.example.crowdfund.entity.Contribution;
import com.example.crowdfund.entity.Payment;
import com.example.crowdfund.entity.User;
import com.example.crowdfund.enums.Currency;
import com.example.crowdfund.enums.PaymentProvider;
import com.example.crowdfund.enums.PaymentStatus;
import com.example.crowdfund.repository.CampaignRepository;
import com.example.crowdfund.repository.ContributionRepository;
import com.example.crowdfund.repository.PaymentRepository;
import com.example.crowdfund.service.payment.PaymentStrategy;
import com.example.crowdfund.service.payment.StripeStrategy;
import com.example.crowdfund.service.user.UserService;
import com.stripe.exception.StripeException;
import com.stripe.model.Account;
import com.stripe.model.checkout.Session;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("v1/api/payment")
@RequiredArgsConstructor
@Slf4j
public class StripeController {

    private final StripeStrategy stripeStrategy;
    private final ContributionRepository contributionRepository;
    private final PaymentRepository paymentRepository;
    private final CampaignRepository campaignRepository;
    private final UserService userService;
    @Autowired(required = false)
    private KafkaTemplate<Long, PaymentNotificationResponse> kafkaTemplate;
    @PostMapping("/checkout/stripe")
    public ResponseEntity<?> createCheckoutSession(@RequestBody ContributionRequest contributionRequest) {

        Contribution contribution = null;

        try {
            log.info("Processing Stripe Checkout session..." );

            contribution = getContribution(contributionRequest);
            contribution = contributionRepository.save(contribution);

            String successUrl = "http://localhost:8080/v1/api/payment/success?session_id={CHECKOUT_SESSION_ID}";
            String cancelUrl = "http://localhost:8080/v1/api/payment/cancel?session_id={CHECKOUT_SESSION_ID}";

            Session session = stripeStrategy.createStripeCheckoutSessionDestinationCharges(contribution, successUrl, cancelUrl);

            Map<String, Object> response = new HashMap<>();

            response.put("success", true);
            response.put("checkoutUrl", session.getUrl());
            response.put("sessionId", session.getId());
            
            return ResponseEntity.ok(response);

        } catch (StripeException e) {
            log.error("Stripe error creating checkout session", e);

            if (contribution.getId() != null) {
                try {
                    contribution.setPaymentStatus(PaymentStatus.FAILED);
                    contributionRepository.save(contribution);
                    
                    Payment payment = Payment.builder()
                        .contributionId(contribution.getId())
                        .amount(contribution.getAmount())
                        .currency(Currency.valueOf(contribution.getCurrency()))
                        .paymentProvider(PaymentProvider.STRIPE)
                        .paymentStatus(PaymentStatus.FAILED)
                        .failureReason("Stripe error: " + e.getMessage())
                        .build();
                    
                    paymentRepository.save(payment);
                } catch (Exception saveError) {
                    log.error("Error saving failure reason", saveError);
                }
            }

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Payment processing error: " + e.getMessage());

            return ResponseEntity.badRequest().body(errorResponse);

        } catch (Exception e) {
            log.error("Error creating checkout session", e);

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Internal server error");

            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @GetMapping("/success")
    public ResponseEntity<?> handlePaymentSuccess(@RequestParam String session_id) {
        try {
            log.info("Handling payment success for session: {}", session_id);

            Session session = Session.retrieve(session_id);

            var paymentOpt = paymentRepository.findByExternalPaymentId(session_id);
            
            if (paymentOpt.isEmpty()) {
                log.error("No payment found for session: {}", session_id);
                return ResponseEntity.status(302)
                        .header("Location", "http://localhost:8080/payment-error")
                        .build();
            }
            
            Payment payment = paymentOpt.get();
            var contributionOpt = contributionRepository.findById(payment.getContributionId());
            
            if (contributionOpt.isPresent()) {
                Contribution contribution = contributionOpt.get();
                contribution.setPaymentStatus(PaymentStatus.SUCCESSFUL);
                contributionRepository.save(contribution);

                payment.setPaymentStatus(PaymentStatus.SUCCESSFUL);

                Long campaignOwnerId = campaignRepository.findCreatorIdByCampaignId(contribution.getCampaignId());
                PaymentNotificationResponse paymentNotificationResponse = new PaymentNotificationResponse(
                        contribution.getDisplayName(),
                        contribution.getAmount(),
                        contribution.getMessage(),
                        campaignOwnerId
                );


                if (kafkaTemplate != null) {
                    kafkaTemplate.send("payment-notification", campaignOwnerId, paymentNotificationResponse);
                } else {
                    log.info("Kafka not available, skipping notification");
                }

                paymentRepository.save(payment);
                
                log.info("Payment completed successfully for contribution: {}", contribution.getId());
            }

            return ResponseEntity.status(302)
                    .header("Location", "http://localhost:8080/payment-success?session_id=" + session_id)
                    .build();
                    
        } catch (Exception e) {
            log.error("Error handling payment success", e);
            return ResponseEntity.status(302)
                    .header("Location", "http://localhost:8080/payment-error")
                    .build();
        }
    }

    @GetMapping("/cancel")
    public ResponseEntity<?> handlePaymentCancel(@RequestParam String session_id) {
        try {
            log.info("Handling payment cancel for session: {}", session_id);

            Session session = Session.retrieve(session_id);
            var paymentOpt = paymentRepository.findByExternalPaymentId(session_id);
            
            if (paymentOpt.isEmpty()) {
                log.error("No payment found for session: {}", session_id);
                return ResponseEntity.status(302)
                        .header("Location", "http://localhost:8080/payment-error")
                        .build();
            }
            
            Payment payment = paymentOpt.get();
            var contributionOpt = contributionRepository.findById(payment.getContributionId());
            
            if (contributionOpt.isPresent()) {
                Contribution contribution = contributionOpt.get();
                contribution.setPaymentStatus(PaymentStatus.CANCELED);
                contributionRepository.save(contribution);

                payment.setPaymentStatus(PaymentStatus.CANCELED);
                payment.setFailureReason("Payment cancelled by user");
                paymentRepository.save(payment);
                
                log.info("Payment cancelled for contribution: {}", contribution.getId());
            }

            return ResponseEntity.status(302)
                    .header("Location", "http://localhost:8080/payment-cancel?session_id=" + session_id)
                    .build();
                    
        } catch (Exception e) {
            log.error("Error handling payment cancel", e);
            return ResponseEntity.status(302)
                    .header("Location", "http://localhost:8080/payment-error")
                    .build();
        }
    }

    @GetMapping("/stripe/get-onboarding-link")
    public ResponseEntity<?> getOnboardingLink() {
        try {
            User user = userService.getCurrentUser();

            if (user.getStripeAccountId() == null) {
                String accountId = stripeStrategy.createStripeAccount(user.getEmail());
                user.setStripeAccountId(accountId);
                userService.save(user);
            }
            
            String refreshURL = "http://localhost:8080/v1/api/payment/stripe/onboarding/refresh?user_id=" + user.getId();
            String returnURL = "http://localhost:8080/v1/api/payment/stripe/onboarding/return?user_id=" + user.getId();

            String url = stripeStrategy.createOnboardingLink(user.getStripeAccountId(), refreshURL, returnURL);

            return ResponseEntity.status(302).header("location",url).build();
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("{\"success\": false, \"error\": \"" + e.getMessage() + "\"}");
        }
    }

    @GetMapping("/stripe/onboarding/return")
    public ResponseEntity<?> handleOnboardingReturn(@RequestParam(required = false) Long user_id) {
        try {
            log.info("Received user_id from return URL: {}", user_id);
            
            if (user_id == null) {
                log.warn("No user_id provided in return URL");
                return ResponseEntity.status(302)
                        .header("Location", "http://localhost:8080/onboarding-error")
                        .build();
            }

            User user = userService.findById(user_id);
            if (user == null) {
                log.warn("No user found with ID: {}", user_id);
                return ResponseEntity.status(302)
                        .header("Location", "http://localhost:8080/onboarding-error")
                        .build();
            }

            log.info("Found user: {}", user.getUsername());
            log.info("User's Stripe Account ID: {}", user.getStripeAccountId());
            
            if (user.getStripeAccountId() == null) {
                log.warn("User has no Stripe account ID");
                return ResponseEntity.status(302)
                        .header("Location", "http://localhost:8080/onboarding-error")
                        .build();
            }
            
            Account account = Account.retrieve(user.getStripeAccountId());

            log.info("Account ID: {}", account.getId());
            log.info("Charges Enabled: {}", account.getChargesEnabled());
            log.info("Details Submitted: {}", account.getDetailsSubmitted());
            log.info("Payouts Enabled: {}", account.getPayoutsEnabled());

            if (account.getChargesEnabled() && account.getDetailsSubmitted()) {
                user.setStripeVerified(true);
                userService.save(user);
                log.info("Onboarding completed successfully for user: {}", user.getUsername());
                return ResponseEntity.status(302)
                        .header("Location", "http://localhost:8080/onboarding-success")
                        .build();
            } else {
                log.info("Onboarding incomplete for user: {}", user.getUsername());
                return ResponseEntity.status(302)
                        .header("Location", "http://localhost:8080/onboarding-incomplete")
                        .build();
            }
        } catch (Exception e) {
            log.error("Exception in onboarding return: {}", e.getMessage(), e);
            return ResponseEntity.status(302)
                    .header("Location", "http://localhost:8080/onboarding-error")
                    .build();
        }
    }

    private static Contribution getContribution(ContributionRequest contributionRequest) {
        Contribution contribution = new Contribution();
        contribution.setCampaignId(contributionRequest.getCampaignId());
        contribution.setUserId(contributionRequest.getUserId());
        contribution.setAmount(contributionRequest.getAmount());
        contribution.setIsAnonymous(contributionRequest.getIsAnonymous());
        contribution.setDisplayName(contributionRequest.getDisplayName());
        contribution.setMessage(contributionRequest.getMessage());
        contribution.setCurrency(contributionRequest.getCurrency().name());
        contribution.setPaymentProvider(contributionRequest.getPaymentProvider());
        return contribution;
    }
}