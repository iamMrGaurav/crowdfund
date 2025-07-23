package com.example.crowdfund.entity;

import com.example.crowdfund.enums.CampaignStatus;
import com.example.crowdfund.enums.Currency;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "campaigns")
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Campaign {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = true)
    private String title;

    @Column(name = "short_description")
    private String shortDescription;

    @Column(name = "full_description")
    private String fullDescription;

    @Column(name = "funding_goal", nullable = true, precision = 38, scale = 2)
    private BigDecimal fundingGoal;

    @Column(name = "current_amount", precision = 38, scale = 2)
    private BigDecimal currentAmount = BigDecimal.ZERO;

    @Column(nullable = true)
    private LocalDateTime deadline;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CampaignStatus status = CampaignStatus.DRAFT;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Currency currency = Currency.USD;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    @JsonIgnore
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false)
    @JsonIgnore
    private User creator;

    @ElementCollection
    @CollectionTable(name = "campaign_images", joinColumns = @JoinColumn(name = "campaign_id"))
    @Column(name = "image_url")
    private List<String> imageUrls = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
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
