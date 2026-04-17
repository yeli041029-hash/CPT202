package com.group32.cpt202.LY_heritage.controller;

import com.group32.cpt202.LY_heritage.dto.HeritageDetailResponse;
import com.group32.cpt202.LY_heritage.service.HeritageItemService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin
public class HeritageItemController {

    private HeritageItemService heritageItemService;

    public HeritageItemController(HeritageItemService heritageItemService) {
        this.heritageItemService = heritageItemService;
    }

    @GetMapping("/heritages/{id}")
    public HeritageDetailResponse getHeritageDetail(@PathVariable Long id) {
        return heritageItemService.getHeritageDetail(id);
    }
}