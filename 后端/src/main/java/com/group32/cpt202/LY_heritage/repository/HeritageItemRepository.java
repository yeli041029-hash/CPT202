package com.group32.cpt202.LY_heritage.repository;

import com.group32.cpt202.LY_heritage.entity.HeritageItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface HeritageItemRepository extends JpaRepository<HeritageItem, Long> {

    List<HeritageItem> findTop10ByStatusIgnoreCaseOrderByUpdatedAtDesc(String status);

    List<HeritageItem> findTop5ByCategoryAndStatusIgnoreCaseAndIdNot(String category, String status, Long id);

    List<HeritageItem> findTop5ByStatusIgnoreCaseAndIdNot(String status, Long id);

    List<HeritageItem> findByContributorIdOrderByUpdatedAtDesc(Long contributorId);

    List<HeritageItem> findByContributorIdAndStatusIgnoreCaseOrderByUpdatedAtDesc(Long contributorId, String status);

    List<HeritageItem> findByStatusIgnoreCaseOrderByUpdatedAtDesc(String status);

    List<HeritageItem> findByCategoryIgnoreCaseAndStatusIgnoreCaseOrderByUpdatedAtDesc(String category, String status);

    List<HeritageItem> findByStatusIgnoreCaseAndPlatformPublishedTrueOrderByUpdatedAtDesc(String status);

    List<HeritageItem> findByCategoryIgnoreCaseAndStatusIgnoreCaseAndPlatformPublishedTrueOrderByUpdatedAtDesc(String category, String status);

    List<HeritageItem> findByStatusIgnoreCaseAndCommunityPostTrueOrderByUpdatedAtDesc(String status);

    List<HeritageItem> findByCategoryIgnoreCaseAndStatusIgnoreCaseAndCommunityPostTrueOrderByUpdatedAtDesc(String category, String status);

    long countByStatusIgnoreCase(String status);

    @Modifying
    @Query("update HeritageItem h set h.viewCount = coalesce(h.viewCount, 0) + 1 where h.id = :id")
    int incrementViewCount(@Param("id") Long id);
}
