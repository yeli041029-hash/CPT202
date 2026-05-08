package com.group32.cpt202.CY_project.service;

import com.group32.cpt202.CY_project.constant.Status;
import com.group32.cpt202.CY_project.entity.ContributorApplication;
import com.group32.cpt202.CY_project.entity.HeritageItem;
import com.group32.cpt202.CY_project.mapper.ContributorApplyMapper;
import com.group32.cpt202.CY_project.mapper.HeritageItemMapper;
import com.group32.cpt202.LY_contributor.entity.User;
import com.group32.cpt202.LY_contributor.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkflowServiceImplTest {

    @Mock
    private ContributorApplyMapper contributorApplyMapper;

    @Mock
    private HeritageItemMapper heritageItemMapper;

    @Mock
    private UserRepository userRepository;

    private WorkflowServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new WorkflowServiceImpl(contributorApplyMapper, heritageItemMapper, userRepository);
    }

    @Test
    void applyContributorInsertsPendingApplicationForNormalUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user(1L, User.Role.USER)));
        when(contributorApplyMapper.selectByUserId(1L)).thenReturn(List.of());

        service.applyContributor(1L, "I want to help");

        ArgumentCaptor<ContributorApplication> captor = ArgumentCaptor.forClass(ContributorApplication.class);
        verify(contributorApplyMapper).insert(captor.capture());
        ContributorApplication saved = captor.getValue();
        assertThat(saved.getUserId()).isEqualTo(1L);
        assertThat(saved.getApplicationReason()).isEqualTo("I want to help");
        assertThat(saved.getStatus()).isEqualTo(Status.PENDING);
        assertThat(saved.getCreatedAt()).isNotNull();
    }

    @Test
    void applyContributorThrowsWhenUserDoesNotExist() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.applyContributor(1L, "I want to help"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("user not found");
    }

    @Test
    void applyContributorThrowsWhenUserIsNotNormalUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user(1L, User.Role.ADMIN)));

        assertThatThrownBy(() -> service.applyContributor(1L, "I want to help"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("only normal users can apply");
    }

    @Test
    void applyContributorThrowsWhenPendingApplicationAlreadyExists() {
        ContributorApplication pending = contributorApplication(10L, 1L, Status.PENDING);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user(1L, User.Role.USER)));
        when(contributorApplyMapper.selectByUserId(1L)).thenReturn(List.of(pending));

        assertThatThrownBy(() -> service.applyContributor(1L, "I want to help"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("pending application already exists");
    }

    @Test
    void getPendingApplyReturnsMapperResult() {
        List<ContributorApplication> pending = List.of(contributorApplication(10L, 1L, Status.PENDING));
        when(contributorApplyMapper.selectPending()).thenReturn(pending);

        List<ContributorApplication> result = service.getPendingApply();

        assertThat(result).containsExactlyElementsOf(pending);
    }

    @Test
    void auditApplyThrowsWhenAdminDoesNotExist() {
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.auditApply(10L, 2L, true, "ok"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("admin not found");
    }

    @Test
    void auditApplyThrowsWhenReviewerIsNotAdmin() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(user(2L, User.Role.USER)));

        assertThatThrownBy(() -> service.auditApply(10L, 2L, true, "ok"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("reviewer is not admin");
    }

    @Test
    void auditApplyThrowsWhenApplicationDoesNotExist() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(user(2L, User.Role.ADMIN)));
        when(contributorApplyMapper.selectById(10L)).thenReturn(null);

        assertThatThrownBy(() -> service.auditApply(10L, 2L, true, "ok"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("application not found");
    }

    @Test
    void auditApplyThrowsWhenApplicationIsNotPending() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(user(2L, User.Role.ADMIN)));
        when(contributorApplyMapper.selectById(10L)).thenReturn(contributorApplication(10L, 1L, Status.APPROVED));

        assertThatThrownBy(() -> service.auditApply(10L, 2L, true, "ok"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("application is not pending");
    }

    @Test
    void auditApplyApprovesApplicationAndPromotesUser() {
        ContributorApplication application = contributorApplication(10L, 1L, Status.PENDING);
        User applicant = user(1L, User.Role.USER);

        when(userRepository.findById(2L)).thenReturn(Optional.of(user(2L, User.Role.ADMIN)));
        when(contributorApplyMapper.selectById(10L)).thenReturn(application);
        when(userRepository.findById(1L)).thenReturn(Optional.of(applicant));

        service.auditApply(10L, 2L, true, "approved");

        assertThat(application.getStatus()).isEqualTo(Status.APPROVED);
        assertThat(application.getFeedback()).isEqualTo("approved");
        assertThat(application.getReviewedBy()).isEqualTo(2L);
        assertThat(application.getReviewedAt()).isNotNull();
        assertThat(applicant.getRole()).isEqualTo(User.Role.CONTRIBUTOR);
        verify(contributorApplyMapper).updateById(application);
        verify(userRepository).save(applicant);
    }

    @Test
    void auditApplyRejectsApplicationWithoutPromotingUser() {
        ContributorApplication application = contributorApplication(10L, 1L, Status.PENDING);

        when(userRepository.findById(2L)).thenReturn(Optional.of(user(2L, User.Role.ADMIN)));
        when(contributorApplyMapper.selectById(10L)).thenReturn(application);

        service.auditApply(10L, 2L, false, "rejected");

        assertThat(application.getStatus()).isEqualTo(Status.REJECTED);
        assertThat(application.getFeedback()).isEqualTo("rejected");
        verify(contributorApplyMapper).updateById(application);
    }

    @Test
    void getMyApplyReturnsMapperResult() {
        List<ContributorApplication> applications = List.of(contributorApplication(10L, 1L, Status.PENDING));
        when(contributorApplyMapper.selectByUserId(1L)).thenReturn(applications);

        assertThat(service.getMyApply(1L)).containsExactlyElementsOf(applications);
    }

    @Test
    void revokeContributorThrowsWhenUserDoesNotExist() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.revokeContributor(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("user not found");
    }

    @Test
    void revokeContributorThrowsWhenUserIsNotContributor() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user(1L, User.Role.USER)));

        assertThatThrownBy(() -> service.revokeContributor(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("user is not contributor");
    }

    @Test
    void revokeContributorDemotesUser() {
        User contributor = user(1L, User.Role.CONTRIBUTOR);
        when(userRepository.findById(1L)).thenReturn(Optional.of(contributor));

        service.revokeContributor(1L);

        assertThat(contributor.getRole()).isEqualTo(User.Role.USER);
        verify(userRepository).save(contributor);
    }

    @Test
    void getPendingItemReturnsMapperResult() {
        List<HeritageItem> items = List.of(heritageItem(9L, "PENDING"));
        when(heritageItemMapper.selectPending()).thenReturn(items);

        assertThat(service.getPendingItem()).containsExactlyElementsOf(items);
    }

    @Test
    void approveItemThrowsWhenItemDoesNotExist() {
        when(heritageItemMapper.selectById(9L)).thenReturn(null);

        assertThatThrownBy(() -> service.approveItem(9L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("heritage item not found");
    }

    @Test
    void approveItemUpdatesStatusToApproved() {
        when(heritageItemMapper.selectById(9L)).thenReturn(heritageItem(9L, Status.PENDING));

        service.approveItem(9L);

        verify(heritageItemMapper).updateStatusById(9L, Status.APPROVED);
    }

    @Test
    void rejectItemThrowsWhenItemDoesNotExist() {
        when(heritageItemMapper.selectById(9L)).thenReturn(null);

        assertThatThrownBy(() -> service.rejectItem(9L, "bad"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("heritage item not found");
    }

    @Test
    void rejectItemUpdatesStatusToRejected() {
        when(heritageItemMapper.selectById(9L)).thenReturn(heritageItem(9L, Status.PENDING));

        service.rejectItem(9L, "bad");

        verify(heritageItemMapper).updateStatusById(9L, Status.REJECTED);
    }

    @Test
    void getMyRejectedItemReturnsMapperResult() {
        List<HeritageItem> rejected = List.of(heritageItem(9L, Status.REJECTED));
        when(heritageItemMapper.selectRejectedByUserId(1L)).thenReturn(rejected);

        assertThat(service.getMyRejectedItem(1L)).containsExactlyElementsOf(rejected);
    }

    @Test
    void reSubmitItemThrowsWhenItemDoesNotExist() {
        when(heritageItemMapper.selectById(9L)).thenReturn(null);

        assertThatThrownBy(() -> service.reSubmitItem(9L, heritageItem(9L, Status.REJECTED)))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("heritage item not found");
    }

    @Test
    void reSubmitItemCopiesFieldsAndMarksPending() {
        HeritageItem existing = heritageItem(9L, Status.REJECTED);
        HeritageItem input = heritageItem(9L, Status.REJECTED);
        input.setTitle("New Title");
        input.setDescription("New Description");
        input.setCategory("architecture");
        input.setLocation("Suzhou");
        input.setImageUrl("new-image");

        when(heritageItemMapper.selectById(9L)).thenReturn(existing);

        service.reSubmitItem(9L, input);

        assertThat(existing.getTitle()).isEqualTo("New Title");
        assertThat(existing.getDescription()).isEqualTo("New Description");
        assertThat(existing.getCategory()).isEqualTo("architecture");
        assertThat(existing.getLocation()).isEqualTo("Suzhou");
        assertThat(existing.getImageUrl()).isEqualTo("new-image");
        assertThat(existing.getStatus()).isEqualTo(Status.PENDING);
        verify(heritageItemMapper).updateForResubmit(existing);
    }

    private User user(Long id, User.Role role) {
        User user = new User();
        user.setId(id);
        user.setUsername("user-" + id);
        user.setRole(role);
        return user;
    }

    private ContributorApplication contributorApplication(Long id, Long userId, String status) {
        ContributorApplication application = new ContributorApplication();
        application.setId(id);
        application.setUserId(userId);
        application.setStatus(status);
        return application;
    }

    private HeritageItem heritageItem(Long id, String status) {
        HeritageItem item = new HeritageItem();
        item.setId(id);
        item.setStatus(status);
        return item;
    }
}
