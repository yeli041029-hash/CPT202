package com.group32.cpt202.LY_contributor.repository;

import com.group32.cpt202.LY_contributor.entity.ContributorApplication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContributorApplicationRepository extends JpaRepository<ContributorApplication, Long> {
    List<ContributorApplication> findByUserId(Long userId);

    List<ContributorApplication> findByUserIdOrderByCreatedAtDescIdDesc(Long userId);

    List<ContributorApplication> findByStatus(ContributorApplication.Status status);

    List<ContributorApplication> findByStatusOrderByCreatedAtDescIdDesc(ContributorApplication.Status status);

    long countByStatus(ContributorApplication.Status status);
}
