package com.group32.cpt202.CY_project.controller;

import com.group32.cpt202.CY_project.Result;
import com.group32.cpt202.CY_project.dto.AuditDto;
import com.group32.cpt202.CY_project.dto.PermissionAuditDto;
import com.group32.cpt202.CY_project.entity.HeritageItem;
import com.group32.cpt202.CY_project.service.WorkflowService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/cy-workflow", "/api/cy/workflow"})
@CrossOrigin
public class WorkflowController {
    private final WorkflowService workflowService;

    public WorkflowController(WorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    @PostMapping("/apply")
    public Result apply(@RequestParam Long userId, @RequestParam String reason) {
        workflowService.applyContributor(userId, reason);
        return Result.success("application submitted");
    }

    @GetMapping("/pending-apply")
    public Result pendingApply() {
        return Result.success(workflowService.getPendingApply());
    }

    @PostMapping("/audit-apply")
    public Result auditApply(@RequestBody AuditDto auditDto) {
        workflowService.auditApply(auditDto.getId(), auditDto.getAdminId(), auditDto.isPass(), auditDto.getFeedback());
        return Result.success("review completed");
    }

    @GetMapping("/my-apply/{userId}")
    public Result myApply(@PathVariable Long userId) {
        return Result.success(workflowService.getMyApply(userId));
    }

    @PostMapping("/revoke-contributor/{userId}")
    public Result revoke(@PathVariable Long userId) {
        workflowService.revokeContributor(userId);
        return Result.success("contributor privileges revoked");
    }

    @GetMapping("/pending-item")
    public Result pendingItem() {
        return Result.success(workflowService.getPendingItem());
    }

    @PostMapping("/approve-item/{id}")
    public Result approveItem(@PathVariable Long id) {
        workflowService.approveItem(id);
        return Result.success("approved and published");
    }

    @PostMapping("/reject-item")
    public Result rejectItem(@RequestBody PermissionAuditDto permissionAuditDto) {
        workflowService.rejectItem(permissionAuditDto.getId(), permissionAuditDto.getReason());
        return Result.success("rejected");
    }

    @GetMapping("/my-rejected-item/{userId}")
    public Result myRejected(@PathVariable Long userId) {
        return Result.success(workflowService.getMyRejectedItem(userId));
    }

    @PutMapping("/re-submit-item/{id}")
    public Result reSubmit(@PathVariable Long id, @RequestBody HeritageItem item) {
        workflowService.reSubmitItem(id, item);
        return Result.success("resubmitted for review");
    }
}
