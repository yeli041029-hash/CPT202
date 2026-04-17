package com.group32.cpt202.LY_contributor.zyl_display;

import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/heritages")
@CrossOrigin
public class HeritageController {

    private final HeritageService heritageService;

    public HeritageController(HeritageService heritageService) {
        this.heritageService = heritageService;
    }

    // 获取所有文化遗产
    @GetMapping
    public List<Heritage> getAll() {
        return heritageService.getAllHeritages();
    }

    // 获取单个详情
    @GetMapping("/{id}")
    public Heritage getDetail(@PathVariable Long id) {
        return heritageService.getById(id);
    }
}