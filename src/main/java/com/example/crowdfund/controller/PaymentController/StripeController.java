package com.example.crowdfund.controller.PaymentController;


import com.example.crowdfund.entity.User;
import com.example.crowdfund.service.payment.StripeStrategy;
import com.example.crowdfund.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("v1/api/payment")
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
            
            String refreshURL = "http://localhost:8080/v1/api/payment/onboarding/refresh";
            String returnURL = "http://localhost:8080/v1/api/payment/onboarding/return";

            String url = stripeStrategy.createOnboardingLink(user.getStripeAccountId(), refreshURL, returnURL);
            
            return ResponseEntity.ok("{\"success\": true, \"onboardingUrl\": \"" + url + "\"}");
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("{\"success\": false, \"error\": \"" + e.getMessage() + "\"}");
        }
    }
}
