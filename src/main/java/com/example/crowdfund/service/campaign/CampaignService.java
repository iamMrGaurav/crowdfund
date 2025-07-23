package com.example.crowdfund.service.campaign;

import com.example.crowdfund.dto.request.CampaignRequest;
import com.example.crowdfund.GloablExceptionHandler.BadRequestException;
import com.example.crowdfund.GloablExceptionHandler.ResourceAlreadyExistsException;
import com.example.crowdfund.entity.Campaign;
import com.example.crowdfund.entity.Category;
import com.example.crowdfund.entity.User;
import com.example.crowdfund.enums.CampaignStatus;
import com.example.crowdfund.repository.CampaignRepository;
import com.example.crowdfund.service.category.CategoryService;
import com.example.crowdfund.service.document.ImageService;
import com.example.crowdfund.service.aws.S3BucketService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CampaignService {

    private final CampaignRepository campaignRepository;
    private final CategoryService categoryService;

    @Autowired
    private ImageService imageService;

    @Autowired
    private S3BucketService s3BucketService;

    public Campaign saveDraft(CampaignRequest campaignRequest, User creator, MultipartFile[] images) throws IOException {
        Campaign campaign = new Campaign();

        campaign.setTitle(campaignRequest.getTitle());
        campaign.setShortDescription(campaignRequest.getShortDescription());
        campaign.setFullDescription(campaignRequest.getFullDescription());
        campaign.setFundingGoal(campaignRequest.getFundingGoal());
        campaign.setCurrency(campaignRequest.getCurrency());
        campaign.setDeadline(campaignRequest.getDeadline());

        if (images != null && images.length > 0) {
            if (images.length > 5) {
                throw new BadRequestException("Maximum 5 images allowed");
            }
            String campaignUuid = UUID.randomUUID().toString();
            List<String> uploadedUrls = imageService.uploadImage(images, campaignUuid);
            campaign.setImageUrls(uploadedUrls);
        }

        if (campaignRequest.getCategoryId() != null) {
            Category category = categoryService.findById(campaignRequest.getCategoryId());
            campaign.setCategory(category);
        }
        
        campaign.setCreator(creator);
        campaign.setStatus(CampaignStatus.DRAFT);

        return campaignRepository.save(campaign);
    }

    public Campaign updateDraft(Long campaignId, CampaignRequest campaignRequest, User user) throws IOException {
        Campaign campaign = findById(campaignId);

        if (!campaign.getCreator().getId().equals(user.getId())) {
            throw new BadRequestException("You can only update your own campaigns");
        }

        if (campaign.getStatus() != CampaignStatus.DRAFT) {
            throw new BadRequestException("Only draft campaigns can be updated");
        }

        updateCampaignFromRequest(campaign, campaignRequest);
        updateCampaignImages(campaign, campaignRequest.getImages(), campaignRequest.getImagesToRemove());

        return campaignRepository.save(campaign);
    }

    private void updateCampaignImages(Campaign campaign, MultipartFile[] newImages, List<String> imagesToRemove) throws IOException {
        List<String> currentImages = campaign.getImageUrls() != null ? 
            new ArrayList<>(campaign.getImageUrls()) : new ArrayList<>();

        if (imagesToRemove != null && !imagesToRemove.isEmpty()) {
            for (String imageUrl : imagesToRemove) {
                s3BucketService.deleteFile(imageUrl);
            }
            currentImages.removeAll(imagesToRemove);
        }

        if (newImages != null && newImages.length > 0) {
            String campaignUuid = UUID.randomUUID().toString();
            List<String> uploadedUrls = imageService.uploadImage(newImages, campaignUuid);
            currentImages.addAll(uploadedUrls);
        }

        if (currentImages.size() > 5) {
            throw new BadRequestException("Maximum 5 images allowed. Current total would be: " + currentImages.size());
        }

        campaign.setImageUrls(currentImages);
    }

    public Campaign findById(Long id){
        Optional<Campaign> campaign = campaignRepository.findById(id);
        return campaign.orElseThrow(() ->
                new ResourceAlreadyExistsException("Campaign not found with campaign ID: " + id)
        );
    }

    public Campaign submitReview(Long campaignId, CampaignRequest request, User user) {
        Campaign campaign = findById(campaignId);

        if (!campaign.getCreator().getId().equals(user.getId())) {
            throw new BadRequestException("You can only submit your own campaigns");
        }

        if (campaign.getStatus() != CampaignStatus.DRAFT) {
            throw new BadRequestException("Only draft campaigns can be submitted for review");
        }

        updateCampaignFromRequest(campaign, request);
        validateForSubmission(campaign);

        campaign.setStatus(CampaignStatus.ACTIVE);
        
        return campaignRepository.save(campaign);
    }

    public Campaign createCampaign(CampaignRequest campaignRequest, User creator) throws IOException {
        Campaign campaign = new Campaign();

        campaign.setCreator(creator);

        updateCampaignFromRequest(campaign, campaignRequest);

        if (campaignRequest.getImages() != null && campaignRequest.getImages().length > 0) {
            if (campaignRequest.getImages().length > 5) {
                throw new BadRequestException("Maximum 5 images allowed");
            }
            String campaignUuid = UUID.randomUUID().toString();
            List<String> uploadedUrls = imageService.uploadImage(campaignRequest.getImages(), campaignUuid);
            campaign.setImageUrls(uploadedUrls);
        }

        validateForSubmission(campaign);
        campaign.setStatus(CampaignStatus.ACTIVE);

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


    public Page<Campaign> getCampaigns(int pageNumber, int pageSize, String sortBy, Long categoryId){
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(sortBy).descending());
        return categoryId != null ? campaignRepository.findByStatusAndCategoryId(CampaignStatus.ACTIVE, categoryId, pageable) : campaignRepository.findByStatus(CampaignStatus.ACTIVE, pageable);
    }

    public Page<Campaign> getCampaignsByUserId(int pageNumber, int pageSize, String sortBy, Long userId){
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(sortBy).descending());
        return campaignRepository.findByCreatorId(userId, pageable);
    }

    private boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }
}
