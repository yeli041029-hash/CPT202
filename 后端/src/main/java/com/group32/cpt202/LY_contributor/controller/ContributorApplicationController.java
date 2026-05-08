package com.group32.cpt202.LY_contributor.controller;

import com.group32.cpt202.LY_contributor.dto.ApplicationRequest;
import com.group32.cpt202.LY_contributor.entity.ContributorApplication;
import com.group32.cpt202.LY_contributor.service.ContributorApplicationService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({
        "/contributor-applications",
        "/ly-contributor/contributor-applications",
        "/api/ly-contributor/contributor-applications"
})
@CrossOrigin
public class ContributorApplicationController {

    private final ContributorApplicationService contributorApplicationService;

    public ContributorApplicationController(ContributorApplicationService contributorApplicationService) {
        this.contributorApplicationService = contributorApplicationService;
    }

    @PostMapping
    public String submitApplication(@RequestBody ApplicationRequest request) {
        contributorApplicationService.submitApplication(
                request.getUserId(),
                request.getApplicationReason(),
                request.getApplicantName(),
                request.getDomain(),
                request.getPortfolioUrl()
        );
        return "application submitted";
    }

    @GetMapping("/my/{userId}")
    public ContributorApplication getMyApplication(@PathVariable Long userId) {
        return contributorApplicationService.getMyApplication(userId);
    }
}
