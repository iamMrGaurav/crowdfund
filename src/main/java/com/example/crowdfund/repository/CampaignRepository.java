package com.example.crowdfund.repository;

import com.example.crowdfund.entity.Campaign;
import com.example.crowdfund.entity.Category;
import com.example.crowdfund.entity.User;
import com.example.crowdfund.enums.CampaignStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CampaignRepository extends JpaRepository<Campaign, Long> {
    Optional<Campaign> findByCategory(Category category);
    Optional<Campaign> findByTitle(String title);
    List<Campaign> findByCreator(User creator);
    List<Campaign> findByStatus(CampaignStatus status);
}
