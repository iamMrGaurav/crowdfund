package com.example.crowdfund.controller.PaymentController;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
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
}