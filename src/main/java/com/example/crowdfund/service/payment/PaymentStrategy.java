package com.example.crowdfund.service.payment;

import com.example.crowdfund.entity.Contribution;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;

public interface PaymentStrategy {
    Session createCheckoutSession(Contribution contribution, String successUrl, String cancelUrl) throws StripeException;
}
