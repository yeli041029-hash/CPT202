package com.group32.cpt202.CY_project.service;

import com.group32.cpt202.CY_project.constant.Status;
import com.group32.cpt202.CY_project.entity.ContributorApplication;
import com.group32.cpt202.CY_project.entity.HeritageItem;
import com.group32.cpt202.CY_project.mapper.ContributorApplyMapper;
import com.group32.cpt202.CY_project.mapper.HeritageItemMapper;
import com.group32.cpt202.LY_contributor.entity.User;
import com.group32.cpt202.LY_contributor.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class WorkflowServiceImpl implements WorkflowService {
    private final ContributorApplyMapper contributorApplyMapper;
    private final HeritageItemMapper heritageItemMapper;
    private final UserRepository userRepository;

    public WorkflowServiceImpl(ContributorApplyMapper contributorApplyMapper,
                               HeritageItemMapper heritageItemMapper,
                               UserRepository userRepository) {
        this.contributorApplyMapper = contributorApplyMapper;
        this.heritageItemMapper = heritageItemMapper;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public void applyContributor(Long userId, String reason) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("user not found"));

        if (user.getRole() != User.Role.USER) {
            throw new RuntimeException("only normal users can apply");
        }

        List<ContributorApplication> applications = contributorApplyMapper.selectByUserId(userId);
        for (ContributorApplication application : applications) {
            if (Status.PENDING.equals(application.getStatus())) {
                throw new RuntimeException("pending application already exists");
            }
        }

        ContributorApplication application = new ContributorApplication();
        application.setUserId(userId);
        application.setApplicationReason(reason);
        application.setStatus(Status.PENDING);
        application.setCreatedAt(LocalDateTime.now());
        contributorApplyMapper.insert(application);
    }

    @Override
    public List<ContributorApplication> getPendingApply() {
        return contributorApplyMapper.selectPending();
    }

    @Override
    @Transactional
    public void auditApply(Long id, Long adminId, boolean pass, String feedback) {
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("admin not found"));
        if (admin.getRole() != User.Role.ADMIN) {
            throw new RuntimeException("reviewer is not admin");
        }

        ContributorApplication application = contributorApplyMapper.selectById(id);
        if (application == null) {
            throw new RuntimeException("application not found");
        }
        if (!Status.PENDING.equals(application.getStatus())) {
            throw new RuntimeException("application is not pending");
        }

        application.setStatus(pass ? Status.APPROVED : Status.REJECTED);
        application.setFeedback(feedback);
        application.setReviewedBy(adminId);
        application.setReviewedAt(LocalDateTime.now());
        contributorApplyMapper.updateById(application);

        if (pass) {
            User applicant = userRepository.findById(application.getUserId())
                    .orElseThrow(() -> new RuntimeException("user not found"));
            applicant.setRole(User.Role.CONTRIBUTOR);
            userRepository.save(applicant);
        }
    }

    @Override
    public List<ContributorApplication> getMyApply(Long userId) {
        return contributorApplyMapper.selectByUserId(userId);
    }

    @Override
    @Transactional
    public void revokeContributor(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("user not found"));

        if (user.getRole() != User.Role.CONTRIBUTOR) {
            throw new RuntimeException("user is not contributor");
        }

        user.setRole(User.Role.USER);
        userRepository.save(user);
    }

    @Override
    public List<HeritageItem> getPendingItem() {
        return heritageItemMapper.selectPending();
    }

    @Override
    public void approveItem(Long id) {
        HeritageItem item = heritageItemMapper.selectById(id);
        if (item == null) {
            throw new RuntimeException("heritage item not found");
        }
        heritageItemMapper.updateStatusById(id, Status.APPROVED);
    }

    @Override
    public void rejectItem(Long id, String reason) {
        HeritageItem item = heritageItemMapper.selectById(id);
        if (item == null) {
            throw new RuntimeException("heritage item not found");
        }
        heritageItemMapper.updateStatusById(id, Status.REJECTED);
    }

    @Override
    public List<HeritageItem> getMyRejectedItem(Long userId) {
        return heritageItemMapper.selectRejectedByUserId(userId);
    }

    @Override
    public void reSubmitItem(Long id, HeritageItem item) {
        HeritageItem existing = heritageItemMapper.selectById(id);
        if (existing == null) {
            throw new RuntimeException("heritage item not found");
        }

        existing.setTitle(item.getTitle());
        existing.setDescription(item.getDescription());
        existing.setCategory(item.getCategory());
        existing.setLocation(item.getLocation());
        existing.setImageUrl(item.getImageUrl());
        existing.setStatus(Status.PENDING);
        heritageItemMapper.updateForResubmit(existing);
    }
}
