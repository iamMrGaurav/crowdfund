package com.example.crowdfund.entity;

import com.example.crowdfund.enums.PaymentStrategy;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "contributions")
@Data
@AllArgsConstructor

public class Contribution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "campaign_id")
    private Long campaignId;

    @Column(name = "user_id", nullable = true)
    private Long userId;

    private BigDecimal amount;

    @Column(name = "is_anonymous")
    private Boolean isAnonymous;

    @Column(name = "display_name")
    private String displayName;

    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStrategy paymentStrategy;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
