package com.group32.cpt202.LY_heritage.service;

import com.group32.cpt202.LY_heritage.dto.HeritageDetailResponse;
import com.group32.cpt202.LY_heritage.entity.HeritageItem;
import com.group32.cpt202.LY_heritage.repository.HeritageItemRepository;
import org.springframework.stereotype.Service;

@Service
public class HeritageItemService {

    private HeritageItemRepository heritageItemRepository;

    public HeritageItemService(HeritageItemRepository heritageItemRepository) {
        this.heritageItemRepository = heritageItemRepository;
    }

    public HeritageDetailResponse getHeritageDetail(Long id) {
        HeritageItem heritageItem = heritageItemRepository.findById(id).orElse(null);

        if (heritageItem == null) {
            throw new RuntimeException("heritage item not found");
        }

        if (!"APPROVED".equalsIgnoreCase(heritageItem.getStatus())) {
            throw new RuntimeException("heritage item is not available");
        }

        HeritageDetailResponse response = new HeritageDetailResponse();
        response.setId(heritageItem.getId());
        response.setTitle(heritageItem.getTitle());
        response.setDescription(heritageItem.getDescription());
        response.setCategory(heritageItem.getCategory());
        response.setLocation(heritageItem.getLocation());
        response.setImageUrl(heritageItem.getImageUrl());
        response.setContributorId(heritageItem.getContributorId());
        response.setCreatedAt(heritageItem.getCreatedAt());
        response.setUpdatedAt(heritageItem.getUpdatedAt());

        return response;
    }
}
