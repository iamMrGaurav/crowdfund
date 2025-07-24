package com.example.crowdfund.repository;

import com.example.crowdfund.entity.Contribution;
import com.example.crowdfund.enums.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ContributionRepository extends JpaRepository<Contribution, Long> {

    Page<Contribution> findByPaymentStatusAndCampaignId(PaymentStatus status, Long campaignId, Pageable pageable);
}
