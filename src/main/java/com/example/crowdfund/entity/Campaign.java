package com.example.crowdfund.entity;


import com.example.crowdfund.enums.CampaignStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor

public class Campaign {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String shortDescription;
    private String fullDescription;

    private BigDecimal fundingGoal;

    private BigDecimal currentAmount = BigDecimal.ZERO;

    private LocalDateTime deadline;

    private CampaignStatus status = CampaignStatus.DRAFT;

    private Category category;

    private User creator;

    private List<String> imageUrls = new ArrayList<>();

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // Calculate campaign progress percentage
    public int getProgressPercentage() {
        if (fundingGoal.compareTo(BigDecimal.ZERO) <= 0) {
            return 0;
        }
        return currentAmount.multiply(BigDecimal.valueOf(100))
                .divide(fundingGoal, 0, BigDecimal.ROUND_DOWN)
                .intValue();
    }

    // Check if campaign is active
    public boolean isActive() {
        return status == CampaignStatus.ACTIVE &&
                LocalDateTime.now().isBefore(deadline);
    }

    // Check if campaign is successful
    public boolean isSuccessful() {
        return currentAmount.compareTo(fundingGoal) >= 0;
    }

}
