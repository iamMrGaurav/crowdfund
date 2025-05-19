//package com.example.crowdfund.entity;
//
//
//import com.example.crowdfund.enums.CampaignStatus;
//import jakarta.persistence.*;
//import lombok.AllArgsConstructor;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//import org.hibernate.annotations.CreationTimestamp;
//import org.hibernate.annotations.UpdateTimestamp;
//
//import java.math.BigDecimal;
//import java.time.LocalDateTime;
//import java.util.ArrayList;
//import java.util.List;
//
//@Entity
//@Data
//@NoArgsConstructor
//@AllArgsConstructor
//@Table(name = "campaign")
//public class Campaign {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @Column(nullable = false)
//    private String title;
//
//    @Column(length = 1000)
//    private String shortDescription;
//
//    @Column(columnDefinition = "TEXT")
//    private String fullDescription;
//
//    @Column(nullable = false)
//    private BigDecimal fundingGoal;
//
//    private BigDecimal currentAmount = BigDecimal.ZERO;
//
//    @Column(nullable = false)
//    private LocalDateTime deadline;
//
//    @Enumerated(EnumType.STRING)
//    private CampaignStatus status = CampaignStatus.DRAFT;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "category_id")
//    private Category category;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "creator_id", nullable = false)
//    private User creator;
//
//
//    @OneToMany(mappedBy = "campaign", cascade = CascadeType.ALL)
//    private List<Contribution> contributions = new ArrayList<>();
//
//    @OneToMany(mappedBy = "campaign", cascade = CascadeType.ALL)
//    private List<CampaignUpdate> updates = new ArrayList<>();
//
//    @ElementCollection
//    @CollectionTable(name = "campaign_images", joinColumns = @JoinColumn(name = "campaign_id"))
//    @Column(name = "image_url")
//    private List<String> imageUrls = new ArrayList<>();
//
//    @CreationTimestamp
//    private LocalDateTime createdAt;
//
//    @UpdateTimestamp
//    private LocalDateTime updatedAt;
//
//    // Helper methods to manage relationships
//    public void addReward(Reward reward) {
//        rewards.add(reward);
//        reward.setCampaign(this);
//    }
//
//    public void removeReward(Reward reward) {
//        rewards.remove(reward);
//        reward.setCampaign(null);
//    }
//
//    public void addUpdate(CampaignUpdate update) {
//        updates.add(update);
//        update.setCampaign(this);
//    }
//
//    // Calculate campaign progress percentage
//    public int getProgressPercentage() {
//        if (fundingGoal.compareTo(BigDecimal.ZERO) <= 0) {
//            return 0;
//        }
//        return currentAmount.multiply(BigDecimal.valueOf(100))
//                .divide(fundingGoal, 0, BigDecimal.ROUND_DOWN)
//                .intValue();
//    }
//
//    // Check if campaign is active
//    public boolean isActive() {
//        return status == CampaignStatus.ACTIVE &&
//                LocalDateTime.now().isBefore(deadline);
//    }
//
//    // Check if campaign is successful
//    public boolean isSuccessful() {
//        return currentAmount.compareTo(fundingGoal) >= 0;
//    }
//
//}
