package com.example.crowdfund.service;

import com.example.crowdfund.DTO.CampaignRequest;
import com.example.crowdfund.entity.Campaign;
import com.example.crowdfund.entity.Category;
import com.example.crowdfund.entity.User;
import com.example.crowdfund.enums.CampaignStatus;
import com.example.crowdfund.repository.CampaignRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
}
