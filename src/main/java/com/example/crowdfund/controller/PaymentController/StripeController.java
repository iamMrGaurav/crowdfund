package com.example.crowdfund.controller.PaymentController;


import com.stripe.exception.StripeException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("v1/api/payment")
@RequiredArgsConstructor
public class StripeController {

    public ResponseEntity<String> createConnectAccount() throws StripeException {
        return ResponseEntity.ok("Hello World");
    }
}
