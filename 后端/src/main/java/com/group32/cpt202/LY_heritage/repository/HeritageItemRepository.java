package com.group32.cpt202.LY_heritage.repository;

import com.group32.cpt202.LY_heritage.entity.HeritageItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface HeritageItemRepository extends JpaRepository<HeritageItem, Long> {

    List<HeritageItem> findTop5ByCategoryAndStatusIgnoreCaseAndIdNot(String category, String status, Long id);

    List<HeritageItem> findTop5ByStatusIgnoreCaseAndIdNot(String status, Long id);

    @Modifying
    @Query("update HeritageItem h set h.viewCount = coalesce(h.viewCount, 0) + 1 where h.id = :id")
    int incrementViewCount(@Param("id") Long id);
}
