package com.example.crowdfund.repository;

import com.example.crowdfund.entity.Campaign;
import com.example.crowdfund.entity.Category;
import com.example.crowdfund.entity.User;
import com.example.crowdfund.enums.CampaignStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CampaignRepository extends JpaRepository<Campaign, Long> {
    Optional<Campaign> findByCategory(Category category);
    Optional<Campaign> findByTitle(String title);
    List<Campaign> findByCreator(User creator);
    Page<Campaign> findByStatus(CampaignStatus status, Pageable pageable);

    Page<Campaign> findByStatusAndCategoryId(CampaignStatus status, Long categoryId, Pageable pageable );

    Page<Campaign> findByCreatorId(Long creatorId, Pageable pageable);
    
    @Query("SELECT c.creator.id FROM Campaign c WHERE c.id = :campaignId")
    Long findCreatorIdByCampaignId(@Param("campaignId") Long campaignId);
}
