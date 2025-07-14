package com.example.crowdfund.controller;

import com.example.crowdfund.DTO.CampaignRequest;
import com.example.crowdfund.entity.Campaign;
import com.example.crowdfund.entity.User;
import com.example.crowdfund.service.CampaignService;
import com.example.crowdfund.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

 /*
    TODO
    1. Update Campaign (Tomorrow) - DONE
    2. Pagination (At Last)
    3. Image Upload (Tomorrow) - Login, Campaign - DONE
    4. Contribution (Stripe, Razor Pay, Google Pay, UPI) (Tomorrow)
    5. Anonymous Contribution
    6. Real Time Contribution Leader Board in a campaign
    7. AI Powered Optimisation
*/

@RestController
@RequestMapping("/v1/api/campaigns")
@RequiredArgsConstructor
public class CampaignController {
    
    private final CampaignService campaignService;
    private final UserService userService;

    @PostMapping(value = "/draft", consumes = {"multipart/form-data"})
    public ResponseEntity<?> saveDraft(@ModelAttribute CampaignRequest campaignRequest, Authentication authentication) throws IOException {

        User currentUser = userService.findByUsername(authentication.getName());
        Campaign campaign = campaignService.saveDraft(campaignRequest, currentUser, campaignRequest.getImages());

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Draft saved successfully");
        response.put("campaign", campaign);

        return ResponseEntity.ok(response);
    }

    @PutMapping(value = "/draft/{campaignId}", consumes = {"multipart/form-data"})
    public ResponseEntity<?> updateDraft(@PathVariable Long campaignId, @ModelAttribute CampaignRequest campaignRequest, Authentication authentication) throws IOException {

        User currentUser = userService.findByUsername(authentication.getName());
        Campaign campaign = campaignService.updateDraft(campaignId, campaignRequest, currentUser);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Draft updated successfully");
        response.put("campaign", campaign);

        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/submit-draft/{campaignId}", consumes = {"multipart/form-data"})
    public ResponseEntity<?> submitReview(@ModelAttribute CampaignRequest campaignRequest, @PathVariable Long campaignId, Authentication authentication){

        User currentUser = userService.findByUsername(authentication.getName());
        Campaign campaign = campaignService.submitReview(campaignId, campaignRequest, currentUser);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Campaign submitted for review successfully");
        response.put("campaign", campaign);

        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/create-campaign", consumes = {"multipart/form-data"})
    public ResponseEntity<?> createCampaign(@ModelAttribute CampaignRequest campaignRequest, Authentication authentication) throws IOException {

        User currentUser = userService.findByUsername(authentication.getName());
        Campaign campaign = campaignService.createCampaign(campaignRequest, currentUser);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Draft saved successfully");
        response.put("campaign", campaign);

        return ResponseEntity.ok(response);
    }
}
