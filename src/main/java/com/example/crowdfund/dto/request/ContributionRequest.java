package com.example.crowdfund.dto.request;

import com.example.crowdfund.enums.Currency;
import com.example.crowdfund.enums.PaymentProvider;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class ContributionRequest {

    @NotNull(message = "Campaign ID cannot be null")
    private Long campaignId;

    private Long userId;

    @NotBlank(message = "Message cannot be blank")
    @Size(min = 1, max = 50, message = "Message must be between 3 and 50 characters")
    private String message;

    @NotBlank(message = "Display Name cannot be null")
    @Size(min = 1, max = 10, message = "Display Name must be between 3 and 50 characters")
    private String displayName;

    @NotNull(message = "Amount cannot be null")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;

    @NotNull(message = "Is Anonymous cannot be null")
    private Boolean isAnonymous;

    private PaymentProvider paymentProvider;

    @NotNull(message = "Currency cannot be null")
    private Currency currency = Currency.USD;
}
