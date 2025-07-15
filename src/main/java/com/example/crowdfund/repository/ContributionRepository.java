package com.example.crowdfund.repository;

import com.example.crowdfund.entity.Contribution;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ContributionRepository extends JpaRepository<Contribution, Long> {
    Optional<Contribution> findByPaymentIntentId(String paymentIntentId);
}
