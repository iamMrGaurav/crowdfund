package com.example.crowdfund.dto.response;


import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class PaymentNotificationResponse {
    String donorName;
    BigDecimal amount;
    String message;
    Long campaignOwnerId;
}
