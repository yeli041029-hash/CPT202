package com.group32.cpt202.CY_project.service;

import com.group32.cpt202.CY_project.entity.ContributorApplication;
import com.group32.cpt202.CY_project.entity.HeritageItem;

import java.util.List;

public interface WorkflowService {
    void applyContributor(Long userId, String reason);

    List<ContributorApplication> getPendingApply();

    void auditApply(Long id, Long adminId, boolean pass, String feedback);

    List<ContributorApplication> getMyApply(Long userId);

    void revokeContributor(Long userId);

    List<HeritageItem> getPendingItem();

    void approveItem(Long id);

    void rejectItem(Long id, String reason);

    List<HeritageItem> getMyRejectedItem(Long userId);

    void reSubmitItem(Long id, HeritageItem item);
}
