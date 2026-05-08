package com.group32.cpt202.lpp_project;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/api/lpp/admin/resources")
public class LppAdminController {
    private final AdminService adminService;

    public LppAdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping
    public List<Resource> getAllResources() {
        return adminService.getAllResources();
    }

    @GetMapping("/pending")
    public List<Resource> getPendingResources() {
        return adminService.getPendingResources();
    }

    @PutMapping("/{resourceId}/publish")
    public Result publish(@PathVariable Long resourceId, @RequestBody ResourceReviewRequest request) {
        return adminService.publish(resourceId, request == null ? null : request.reviewerId, request == null ? null : request.feedback);
    }

    @PutMapping("/{resourceId}/reject")
    public Result reject(@PathVariable Long resourceId, @RequestBody ResourceReviewRequest request) {
        return adminService.reject(resourceId, request == null ? null : request.reviewerId, request == null ? null : request.feedback);
    }

    @PostMapping("/{resourceId}/archive")
    public Result archive(@PathVariable Long resourceId, @RequestBody ResourceArchive archive) {
        archive.resourceId = resourceId;
        return adminService.archive(archive);
    }

    @DeleteMapping("/{resourceId}")
    public Result delete(@PathVariable Long resourceId, @RequestParam Long adminId) {
        return adminService.deleteResource(resourceId, adminId);
    }

    @GetMapping("/archives")
    public List<ResourceArchive> getArchives() {
        return adminService.getArchives();
    }
}
