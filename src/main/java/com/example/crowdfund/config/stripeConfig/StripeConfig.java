package com.example.crowdfund.config.stripeConfig;

import com.stripe.Stripe;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Data
public class StripeConfig {
    
    @Value("${STRIPE_SECRET_KEY}")
    private String secretKey;
    
    @Value("${STRIPE_PUBLIC_KEY}")
    private String publicKey;
    
    @Value("${STRIPE_WEBHOOK_SECRET}")
    private String webhookSecret;
    
    @PostConstruct
    public void init() {
        Stripe.apiKey = secretKey;
    }
}
