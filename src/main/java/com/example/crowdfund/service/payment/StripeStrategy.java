package com.example.crowdfund.service.payment;


import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.stripe.Stripe;


@Service
public class StripeStrategy implements PaymentStrategy{

    @Value("{STRIPE_SECRET_KEY:}")
    private String STRIPE_SECRET_KEY;

    @Override
    public void processPayment(double amount) {
    }

    @PostConstruct
    public void init() {
        Stripe.apiKey = STRIPE_SECRET_KEY;
    }
}
