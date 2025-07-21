package com.example.crowdfund.dto.response;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentNotificationResponse {
    String donorName;
    BigDecimal amount;
    String message;
    Long campaignOwnerId;
    Long contributionId;
    Long campaignId;
}
