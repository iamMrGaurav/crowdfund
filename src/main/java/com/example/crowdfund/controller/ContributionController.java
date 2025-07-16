package com.example.crowdfund.controller;

import com.example.crowdfund.dto.request.ContributionRequest;
import com.example.crowdfund.entity.Contribution;
import com.example.crowdfund.entity.Payment;
import com.example.crowdfund.enums.Currency;
import com.example.crowdfund.enums.PaymentProvider;
import com.example.crowdfund.enums.PaymentStatus;
import com.example.crowdfund.repository.ContributionRepository;
import com.example.crowdfund.repository.PaymentRepository;
import com.example.crowdfund.service.payment.PaymentStrategy;
import com.example.crowdfund.service.payment.StripeStrategy;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.checkout.Session;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("v1/api/payment")
@RequiredArgsConstructor
@Slf4j
public class ContributionController {

    private final PaymentStrategy stripeStrategy;
    private final ContributionRepository contributionRepository;
    private final PaymentRepository paymentRepository;

    @PostMapping("/checkout/stripe")
    public ResponseEntity<?> createCheckoutSession(@RequestBody ContributionRequest contributionRequest) {
        
        Contribution contribution = null;
        
        try {
            log.info("Processing Stripe Checkout session..." );

            contribution = getContribution(contributionRequest);
            contribution = contributionRepository.save(contribution);

            String successUrl = "http://localhost:8080/v1/api/payment/success?session_id={CHECKOUT_SESSION_ID}";
            String cancelUrl = "http://localhost:8080/v1/api/payment/cancel?session_id={CHECKOUT_SESSION_ID}";

            Session session = stripeStrategy.createCheckoutSession(contribution, successUrl, cancelUrl);

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

                if (payment.getPaymentIntentId() != null && payment.getClientSecret() == null) {
                    try {
                        PaymentIntent paymentIntent = PaymentIntent.retrieve(payment.getPaymentIntentId());
                        if (paymentIntent.getClientSecret() != null) {
                            payment.setClientSecret(paymentIntent.getClientSecret());
                        }
                    } catch (StripeException e) {
                        log.warn("Could not retrieve PaymentIntent details: {}", e.getMessage());
                    }
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
