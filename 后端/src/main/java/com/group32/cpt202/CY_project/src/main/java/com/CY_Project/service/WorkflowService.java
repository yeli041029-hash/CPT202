package com.CY_Project.service;

import com.CY_Project.ContributorApplications;
import com.CY_Project.HeritageItem;
import java.util.List;

public interface WorkflowService {

    void applyContributor(Long userId, String reason);
    List<ContributorApplications> getPendingApply();
    void auditApply(Long id, Long adminId, boolean pass, String feedback);
    List<ContributorApplications> getMyApply(Long userId);
    void revokeContributor(Long userId);

    // 内容审核
    List<HeritageItem> getPendingItem();
    void approveItem(Long id);
    void rejectItem(Long id, String reason);
    List<HeritageItem> getMyRejectedItem(Long userId);
    void reSubmitItem(Long id, HeritageItem item);
}