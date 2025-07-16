package com.example.crowdfund.service.payment;

import com.example.crowdfund.entity.Contribution;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import org.springframework.stereotype.Service;

@Service
public interface PaymentStrategy {
    Session createCheckoutSession(Contribution contribution, String successUrl, String cancelUrl) throws StripeException;
}
