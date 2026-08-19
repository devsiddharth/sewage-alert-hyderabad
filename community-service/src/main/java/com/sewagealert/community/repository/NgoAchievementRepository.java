package com.sewagealert.community.repository;

import com.sewagealert.community.model.NgoAchievement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NgoAchievementRepository extends JpaRepository<NgoAchievement, Long> {
    List<NgoAchievement> findByNgoOrganizationIdOrderByDateDesc(Long ngoOrganizationId);
}
