package com.example.crowdfund.enums;

public enum Currency {
    USD("United States Dollar", "$"),
    EUR("Euro", "€"),
    GBP("British Pound", "£"),
    CAD("Canadian Dollar", "C$"),
    AUD("Australian Dollar", "A$"),
    JPY("Japanese Yen", "¥"),
    INR("Indian Rupee", "₹");
    
    private final String displayName;
    private final String symbol;
    
    Currency(String displayName, String symbol) {
        this.displayName = displayName;
        this.symbol = symbol;
    }
    
    public String getDisplayName() { 
        return displayName; 
    }
    
    public String getSymbol() { 
        return symbol; 
    }
}