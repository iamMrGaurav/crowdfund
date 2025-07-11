package com.example.crowdfund.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.example.crowdfund.enums.Currency;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CampaignRequest {

    private String title;
    private String shortDescription;
    private String fullDescription;
    private BigDecimal fundingGoal;
    private Currency currency = Currency.USD;
    private LocalDateTime deadline;
    private Long categoryId;
    private List<String> imageUrls;
}
