package com.group32.cpt202.LY_contributor.controller;

import com.group32.cpt202.LY_contributor.dto.ReviewRequest;
import com.group32.cpt202.LY_contributor.dto.UserSummaryResponse;
import com.group32.cpt202.LY_contributor.entity.ContributorApplication;
import com.group32.cpt202.LY_contributor.entity.User;
import com.group32.cpt202.LY_contributor.service.ContributorApplicationService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping({
        "/admin/contributor-applications",
        "/ly-contributor/admin/contributor-applications",
        "/api/ly-contributor/admin/contributor-applications"
})
@CrossOrigin
public class AdminContributorController {

    private final ContributorApplicationService contributorApplicationService;

    public AdminContributorController(ContributorApplicationService contributorApplicationService) {
        this.contributorApplicationService = contributorApplicationService;
    }

    @GetMapping("/pending")
    public List<ContributorApplication> getPendingApplications() {
        return contributorApplicationService.getPendingApplications();
    }

    @PutMapping("/{id}/approve")
    public ContributorApplication approveApplication(@PathVariable Long id,
                                                     @RequestBody ReviewRequest request) {
        return contributorApplicationService.approveApplication(
                id,
                request.getReviewedBy(),
                request.getFeedback()
        );
    }

    @PutMapping("/{id}/reject")
    public ContributorApplication rejectApplication(@PathVariable Long id,
                                                    @RequestBody ReviewRequest request) {
        return contributorApplicationService.rejectApplication(
                id,
                request.getReviewedBy(),
                request.getFeedback()
        );
    }

    @PutMapping("/users/{userId}/revoke-contributor")
    public UserSummaryResponse revokeContributor(@PathVariable Long userId) {
        User user = contributorApplicationService.revokeContributor(userId);
        return UserSummaryResponse.fromUser(user);
    }
}
