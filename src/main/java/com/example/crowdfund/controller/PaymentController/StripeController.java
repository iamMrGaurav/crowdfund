package com.example.crowdfund.controller.PaymentController;


import com.example.crowdfund.entity.User;
import com.example.crowdfund.service.payment.StripeStrategy;
import com.example.crowdfund.service.user.UserService;
import com.stripe.model.Account;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@Slf4j
@RestController
@RequestMapping("v1/api/payment/stripe")
@RequiredArgsConstructor
public class StripeController {

    private final UserService userService;
    private final StripeStrategy stripeStrategy;

    @GetMapping("/get-onboarding-link")
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

    @GetMapping("/onboarding/return")
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

}
