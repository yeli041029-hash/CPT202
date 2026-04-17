//管理员层面的Controller

package com.group32.cpt202.LY_contributor.controller;

import com.group32.cpt202.LY_contributor.dto.ReviewRequest;
import com.group32.cpt202.LY_contributor.entity.ContributorApplication;
import com.group32.cpt202.LY_contributor.entity.User;
import com.group32.cpt202.LY_contributor.service.ContributorApplicationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/contributor-applications")
@CrossOrigin
public class AdminContributorController {

    private ContributorApplicationService contributorApplicationService;

    public AdminContributorController(ContributorApplicationService contributorApplicationService) {
        this.contributorApplicationService = contributorApplicationService;
    }

    @GetMapping("/pending") //GET /admin/contributor-applications/pending这个请求会进这个方法
    public List<ContributorApplication> getPendingApplications() { //把所有待审批申请，直接以 JSON 形式返回给前端
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
    public User revokeContributor(@PathVariable Long userId) {
        return contributorApplicationService.revokeContributor(userId);
    }
}
