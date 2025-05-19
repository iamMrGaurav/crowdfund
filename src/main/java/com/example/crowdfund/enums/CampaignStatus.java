package com.example.crowdfund.enums;

public enum CampaignStatus {
    DRAFT,        // Campaign is being created, not visible to backers
    PENDING,      // Campaign submitted for review (if platform requires approval)
    ACTIVE,       // Campaign is live and accepting contributions
    SUCCESSFUL,   // Campaign reached its goal
    FAILED,       // Campaign ended without reaching its goal
    CANCELED      // Campaign was canceled by creator or admin
}