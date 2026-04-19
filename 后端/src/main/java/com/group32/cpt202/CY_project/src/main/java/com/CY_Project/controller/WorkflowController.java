package com.CY_Project.controller;

import com.CY_Project.Result;
import com.CY_Project.HeritageItem;
import com.CY_Project.service.WorkflowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class WorkflowController {

    @Autowired
    private WorkflowService workflowService;

    // 改成 GET 就能浏览器访问了
    @GetMapping("/apply")
    public Result apply(@RequestParam Long userId, @RequestParam String reason) {
        workflowService.applyContributor(userId, reason);
        return Result.success("The application has been submitted.");
    }

    @GetMapping("/pending-apply")
    public Result pendingApply() {
        return Result.success(workflowService.getPendingApply());
    }

    @GetMapping("/audit-apply")
    public Result auditApply(@RequestParam Long id,
                             @RequestParam boolean pass,
                             @RequestParam String feedback) {
        workflowService.auditApply(id, 1L, pass, feedback);
        return Result.success("Review completed");
    }

    @GetMapping("/my-apply")
    public Result myApply(@RequestParam Long userId) {
        return Result.success(workflowService.getMyApply(userId));
    }

    @GetMapping("/revoke-contributor")
    public Result revoke(@RequestParam Long userId) {
        workflowService.revokeContributor(userId);
        return Result.success("Contributor privileges have been revoked");
    }

    @GetMapping("/pending-item")
    public Result pendingItem() {
        return Result.success(workflowService.getPendingItem());
    }

    @GetMapping("/approve-item")
    public Result approveItem(@RequestParam Long id) {
        workflowService.approveItem(id);
        return Result.success("Approved and published");
    }

    @GetMapping("/reject-item")
    public Result rejectItem(@RequestParam Long id, @RequestParam String reason) {
        workflowService.rejectItem(id, reason);
        return Result.success("It has been refused and the reasons have been recorded");
    }

    @GetMapping("/my-rejected-item")
    public Result myRejected(@RequestParam Long userId) {
        return Result.success(workflowService.getMyRejectedItem(userId));
    }

    @GetMapping("/re-submit-item")
    public Result reSubmit(@RequestParam Long id, HeritageItem item) {
        workflowService.reSubmitItem(id, item);
        return Result.success("The modification is successful. Resubmit for review");
    }
}