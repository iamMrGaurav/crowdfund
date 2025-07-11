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

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("v1/api/campaigns")
@RequiredArgsConstructor
public class CampaignController {
    
    private final CampaignService campaignService;
    private final UserService userService;

    @PostMapping("/draft")
    public ResponseEntity<?> saveDraft(
            @RequestBody CampaignRequest campaignRequest,
            Authentication authentication) {

        User currentUser = userService.findByUsername(authentication.getName());
        Campaign campaign = campaignService.saveDraft(campaignRequest, currentUser);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Draft saved successfully");
        response.put("campaign", campaign);

        return ResponseEntity.ok(response);
    }
}
