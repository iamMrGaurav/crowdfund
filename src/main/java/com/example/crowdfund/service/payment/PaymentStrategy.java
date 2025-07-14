package com.example.crowdfund.service.payment;

public interface PaymentStrategy {
    void processPayment(double amount);
}
