package com.example.crowdfund.controller.pageController;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@Slf4j
public class PaymentPageController {

    @GetMapping("/payment-success")
    public String paymentSuccess(@RequestParam(required = false) String session_id, Model model) {
        if (session_id != null) {
            model.addAttribute("sessionId", session_id);
        }
        return "payment-success";
    }

    @GetMapping("/payment-cancel")
    public String paymentCancel(@RequestParam(required = false) String session_id, Model model) {
        if (session_id != null) {
            model.addAttribute("sessionId", session_id);
        }
        return "payment-cancel";
    }

    @GetMapping("/payment-error")
    public String paymentError(Model model) {
        return "payment-error";
    }

    @GetMapping("/onboarding-success")
    public String onboardingSuccess() {
        log.info("Serving onboarding success page");
        return "onboarding-success";
    }

    @GetMapping("/onboarding-error")
    public String onboardingError() {
        log.info("Serving onboarding error page");
        return "onboarding-error";
    }

    @GetMapping("/onboarding-incomplete")
    public String onboardingIncomplete() {
        log.info("Serving onboarding incomplete page");
        return "onboarding-incomplete";
    }
}