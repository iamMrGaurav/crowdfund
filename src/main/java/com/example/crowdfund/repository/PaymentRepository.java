package com.example.crowdfund.repository;

import com.example.crowdfund.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByExternalPaymentId(String externalPaymentId);
}
