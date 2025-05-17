package com.example.crowdfund.config;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApiKeyConfig {

    @Value("${api.key:}")
    private String apiKey;

    public String getApiKey() {
        return apiKey;
    }

    public boolean isValidApiKey(String providedApiKey) {
        return apiKey.equals(providedApiKey);
    }
}
