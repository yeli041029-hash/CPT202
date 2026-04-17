// 贡献者申请服务
// 负责处理用户提交的贡献者申请，以及管理员查看待审批的申请列表
// 主要功能包括：
// 1. 用户提交申请：检查用户身份和已有申请状态，创建新的申请
// 2. 管理员查看待审批申请：返回所有状态为 PENDING 的申请列表
// 该服务依赖于 ContributorApplicationRepository 和 UserRepository 来访问数据库中的申请和用户数据
// 该服务的实现确保了用户只能提交一次待审批的申请，并且只有普通用户才能申请成为贡献者
// 该服务的实现还包括了基本的错误处理，例如用户不存在、用户角色不符合要求、以及已有待审批申请的情况


package com.group32.cpt202.LY_contributor.service;

import com.group32.cpt202.LY_contributor.entity.ContributorApplication;
import com.group32.cpt202.LY_contributor.entity.User;
import com.group32.cpt202.LY_contributor.repository.ContributorApplicationRepository;
import com.group32.cpt202.LY_contributor.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ContributorApplicationService {

    private ContributorApplicationRepository applicationRepository;
    private UserRepository userRepository;

    public ContributorApplicationService(ContributorApplicationRepository applicationRepository, UserRepository userRepository) {
        this.applicationRepository = applicationRepository;
        this.userRepository = userRepository;
    }

    // 用户提交申请
    public void submitApplication(Long userId, String reason) {

        // 1. 查用户
        User user = userRepository.findById(userId).orElse(null);

        if (user == null) {
            throw new RuntimeException("user not found");
        }

        // 2. 检查角色
        if (user.getRole() != User.Role.USER) {
            throw new RuntimeException("cannot apply");
        }

        // 3. 检查是否已有 pending
        List<ContributorApplication> list =
                applicationRepository.findByUserId(userId);

        for (ContributorApplication app : list) {
            if (app.getStatus() == ContributorApplication.Status.PENDING) {
                throw new RuntimeException("already applied");
            }
        }

        // 4. 创建申请
        ContributorApplication newApp = new ContributorApplication();
        newApp.setUserId(userId);
        newApp.setApplicationReason(reason);
        newApp.setStatus(ContributorApplication.Status.PENDING);

        // 5. 保存
        applicationRepository.save(newApp);
    }

    // 管理员审批申请
    @Transactional
    public ContributorApplication approveApplication(Long applicationId, Long reviewedBy, String feedback) {
        ContributorApplication application = applicationRepository.findById(applicationId).orElse(null);

        if (application == null) {
            throw new RuntimeException("application not found");
        }

        if (application.getStatus() != ContributorApplication.Status.PENDING) {
            throw new RuntimeException("application is not pending");
        }

        User user = userRepository.findById(application.getUserId()).orElse(null);

        if (user == null) {
            throw new RuntimeException("user not found");
        }

        User reviewer = userRepository.findById(reviewedBy).orElse(null);

        if (reviewer == null) {
            throw new RuntimeException("reviewer not found");
        }

        if (reviewer.getRole() != User.Role.ADMIN) {
            throw new RuntimeException("reviewer is not admin");
        }

        application.setStatus(ContributorApplication.Status.APPROVED);
        application.setReviewedBy(reviewedBy);
        application.setFeedback(feedback);
        application.setReviewedAt(java.time.LocalDateTime.now());

        user.setRole(User.Role.CONTRIBUTOR);

        userRepository.save(user);
        return applicationRepository.save(application);
    }

    
    @Transactional
    public ContributorApplication rejectApplication(Long applicationId, Long reviewedBy, String feedback) {
        ContributorApplication application = applicationRepository.findById(applicationId).orElse(null);

        if (application == null) {
            throw new RuntimeException("application not found");
        }

        if (application.getStatus() != ContributorApplication.Status.PENDING) {
            throw new RuntimeException("application is not pending");
        }

        User reviewer = userRepository.findById(reviewedBy).orElse(null);

        if (reviewer == null) {
            throw new RuntimeException("reviewer not found");
        }

        if (reviewer.getRole() != User.Role.ADMIN) {
            throw new RuntimeException("reviewer is not admin");
        }

        application.setStatus(ContributorApplication.Status.REJECTED);
        application.setReviewedBy(reviewedBy);
        application.setFeedback(feedback);
        application.setReviewedAt(java.time.LocalDateTime.now());

        return applicationRepository.save(application);
    }

    //用户查看自己的申请
    public ContributorApplication getMyApplication(Long userId) { 
        List<ContributorApplication> list = applicationRepository.findByUserIdOrderByCreatedAtDescIdDesc(userId);

        if (list == null || list.isEmpty()) {
            throw new RuntimeException("no application found");
        }

        return list.get(0);
    }

    

    // 管理员查看待审批
    public List<ContributorApplication> getPendingApplications() {
        return applicationRepository.findByStatus(
                ContributorApplication.Status.PENDING
        );
    }


    @Transactional
    public User revokeContributor(Long userId) {
        User user = userRepository.findById(userId).orElse(null);

        if (user == null) {
            throw new RuntimeException("user not found");
        }

        if (user.getRole() != User.Role.CONTRIBUTOR) {
            throw new RuntimeException("user is not contributor");
        }

        user.setRole(User.Role.USER);
        return userRepository.save(user);
    }
}
