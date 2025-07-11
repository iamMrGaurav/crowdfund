package com.example.crowdfund.service;

import com.example.crowdfund.DTO.CampaignRequest;
import com.example.crowdfund.GloablExceptionHandler.BadRequestException;
import com.example.crowdfund.GloablExceptionHandler.ResourceAlreadyExistsException;
import com.example.crowdfund.entity.Campaign;
import com.example.crowdfund.entity.Category;
import com.example.crowdfund.entity.User;
import com.example.crowdfund.enums.CampaignStatus;
import com.example.crowdfund.repository.CampaignRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CampaignService {

    private final CampaignRepository campaignRepository;
    private final CategoryService categoryService;

    public Campaign saveDraft(CampaignRequest campaignRequest, User creator) {
        Campaign campaign = new Campaign();

        campaign.setTitle(campaignRequest.getTitle());
        campaign.setShortDescription(campaignRequest.getShortDescription());
        campaign.setFullDescription(campaignRequest.getFullDescription());
        campaign.setFundingGoal(campaignRequest.getFundingGoal());
        campaign.setCurrency(campaignRequest.getCurrency());
        campaign.setDeadline(campaignRequest.getDeadline());
        campaign.setImageUrls(campaignRequest.getImageUrls());

        if (campaignRequest.getCategoryId() != null) {
            Category category = categoryService.findById(campaignRequest.getCategoryId());
            campaign.setCategory(category);
        }
        
        campaign.setCreator(creator);
        campaign.setStatus(CampaignStatus.DRAFT);

        return campaignRepository.save(campaign);
    }

    public Campaign findById(Long id){
        Optional<Campaign> campaign = campaignRepository.findById(id);
        return campaign.orElseThrow(() ->
                new ResourceAlreadyExistsException("Campaign not found with campaign ID: " + id)
        );
    }

    public Campaign submitReview(Long campaignId, CampaignRequest request, User user) {
        Campaign campaign = findById(campaignId);
        
        // Security check: only creator can submit
        if (!campaign.getCreator().getId().equals(user.getId())) {
            throw new BadRequestException("You can only submit your own campaigns");
        }
        
        // Business rule check: only DRAFT can be submitted
        if (campaign.getStatus() != CampaignStatus.DRAFT) {
            throw new BadRequestException("Only draft campaigns can be submitted for review");
        }
        
        // Update with fresh data from request
        updateCampaignFromRequest(campaign, request);
        
        // Validate complete campaign before submission
        validateForSubmission(campaign);
        
        // Change status to PENDING
        campaign.setStatus(CampaignStatus.PENDING);
        
        return campaignRepository.save(campaign);
    }

    private void updateCampaignFromRequest(Campaign campaign, CampaignRequest request) {
        if (request.getTitle() != null) {
            campaign.setTitle(request.getTitle());
        }
        if (request.getShortDescription() != null) {
            campaign.setShortDescription(request.getShortDescription());
        }
        if (request.getFullDescription() != null) {
            campaign.setFullDescription(request.getFullDescription());
        }
        if (request.getFundingGoal() != null) {
            campaign.setFundingGoal(request.getFundingGoal());
        }
        if (request.getCurrency() != null) {
            campaign.setCurrency(request.getCurrency());
        }
        if (request.getDeadline() != null) {
            campaign.setDeadline(request.getDeadline());
        }
        if (request.getCategoryId() != null) {
            Category category = categoryService.findById(request.getCategoryId());
            campaign.setCategory(category);
        }
        if (request.getImageUrls() != null) {
            campaign.setImageUrls(request.getImageUrls());
        }
    }

    private void validateForSubmission(Campaign campaign) {
        List<String> errors = new ArrayList<>();
        
        // Required field checks
        if (isBlank(campaign.getTitle())) {
            errors.add("Title is required");
        }
        if (isBlank(campaign.getShortDescription())) {
            errors.add("Short description is required");
        }
        if (isBlank(campaign.getFullDescription())) {
            errors.add("Full description is required");
        }
        if (campaign.getFundingGoal() == null || campaign.getFundingGoal().compareTo(BigDecimal.ZERO) <= 0) {
            errors.add("Funding goal must be greater than 0");
        }
        if (campaign.getCurrency() == null) {
            errors.add("Currency is required");
        }
        if (campaign.getDeadline() == null || campaign.getDeadline().isBefore(LocalDateTime.now())) {
            errors.add("Deadline must be in the future");
        }
        if (campaign.getCategory() == null) {
            errors.add("Category is required");
        }
        
        if (!errors.isEmpty()) {
            throw new BadRequestException("Validation failed: " + String.join(", ", errors));
        }
    }

    private boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }
}
