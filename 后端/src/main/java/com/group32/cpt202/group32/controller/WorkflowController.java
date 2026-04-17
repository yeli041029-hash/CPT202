package com.group32.controller;
import com.group32.Result;
import com.group32.service.WorkflowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class WorkflowController {

    @Autowired
    private WorkflowService workflowService;

    @PostMapping("/apply")
    public Result apply(@RequestParam Long userId, @RequestParam String reason) {
        workflowService.applyContributor(userId, reason);
        return Result.success("申请成功");
    }

    @GetMapping("/pending-apply")
    public Result pendingApply() {
        return Result.success(workflowService.getPendingApplications());
    }

    @PostMapping("/audit-apply")
    public Result auditApply(@RequestParam Long id, @RequestParam boolean pass, @RequestParam String feedback) {
        workflowService.auditApplication(id, 1L, pass, feedback);
        return Result.success("审核完成");
    }

    @GetMapping("/pending-item")
    public Result pendingItem() {
        return Result.success(workflowService.getPendingItems());
    }

    @PostMapping("/approve-item")
    public Result approveItem(@RequestParam Long id) {
        workflowService.approveItem(id);
        return Result.success("已发布");
    }

    @PostMapping("/reject-item")
    public Result rejectItem(@RequestParam Long id, @RequestParam String reason) {
        workflowService.rejectItem(id, reason);
        return Result.success("已拒绝");
    }
}
