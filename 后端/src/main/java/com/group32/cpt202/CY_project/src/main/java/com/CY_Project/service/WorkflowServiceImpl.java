package com.CY_Project.service;

import com.CY_Project.ContributorApplications;
import com.CY_Project.HeritageItem;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class WorkflowServiceImpl implements WorkflowService {

    @Override
    public void applyContributor(Long userId, String reason) {}

    @Override
    public List<ContributorApplications> getPendingApply() {
        return null;
    }

    @Override
    public void auditApply(Long id, Long adminId, boolean pass, String feedback) {}

    @Override
    public List<ContributorApplications> getMyApply(Long userId) {
        return null;
    }

    @Override
    public void revokeContributor(Long userId) {}

    @Override
    public List<HeritageItem> getPendingItem() {
        return null;
    }

    @Override
    public void approveItem(Long id) {}

    @Override
    public void rejectItem(Long id, String reason) {}

    @Override
    public List<HeritageItem> getMyRejectedItem(Long userId) {
        return null;
    }

    @Override
    public void reSubmitItem(Long id, HeritageItem item) {}
}