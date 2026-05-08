package com.group32.cpt202.LY_contributor.service;

import com.group32.cpt202.LY_contributor.entity.ContributorApplication;
import com.group32.cpt202.LY_contributor.entity.User;
import com.group32.cpt202.LY_contributor.repository.ContributorApplicationRepository;
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
class ContributorApplicationServiceTest {

    @Mock
    private ContributorApplicationRepository applicationRepository;

    @Mock
    private UserRepository userRepository;

    private ContributorApplicationService service;

    @BeforeEach
    void setUp() {
        service = new ContributorApplicationService(applicationRepository, userRepository);
    }

    @Test
    void submitApplicationSavesPendingApplicationForEligibleUser() {
        User applicant = user(1L, User.Role.USER);
        when(userRepository.findById(1L)).thenReturn(Optional.of(applicant));
        when(applicationRepository.findByUserId(1L)).thenReturn(List.of());

        service.submitApplication(1L, "I can help with artifacts", "Alice", "Crafts", "https://portfolio.test");

        ArgumentCaptor<ContributorApplication> captor = ArgumentCaptor.forClass(ContributorApplication.class);
        verify(applicationRepository).save(captor.capture());

        ContributorApplication saved = captor.getValue();
        assertThat(saved.getUserId()).isEqualTo(1L);
        assertThat(saved.getApplicationReason()).isEqualTo("I can help with artifacts");
        assertThat(saved.getApplicantName()).isEqualTo("Alice");
        assertThat(saved.getDomain()).isEqualTo("Crafts");
        assertThat(saved.getPortfolioUrl()).isEqualTo("https://portfolio.test");
        assertThat(saved.getStatus()).isEqualTo(ContributorApplication.Status.PENDING);
    }

    @Test
    void submitApplicationThrowsWhenUserDoesNotExist() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.submitApplication(99L, "reason", "Alice", "Crafts", "url"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("user not found");
    }

    @Test
    void submitApplicationThrowsWhenUserIsNotNormalUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user(1L, User.Role.ADMIN)));

        assertThatThrownBy(() -> service.submitApplication(1L, "reason", "Alice", "Crafts", "url"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("cannot apply");
    }

    @Test
    void submitApplicationThrowsWhenPendingApplicationAlreadyExists() {
        User applicant = user(1L, User.Role.USER);
        ContributorApplication pending = application(10L, 1L, ContributorApplication.Status.PENDING);

        when(userRepository.findById(1L)).thenReturn(Optional.of(applicant));
        when(applicationRepository.findByUserId(1L)).thenReturn(List.of(pending));

        assertThatThrownBy(() -> service.submitApplication(1L, "reason", "Alice", "Crafts", "url"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("already applied");
    }

    @Test
    void approveApplicationApprovesPendingApplicationAndPromotesApplicant() {
        ContributorApplication pending = application(10L, 1L, ContributorApplication.Status.PENDING);
        User applicant = user(1L, User.Role.USER);
        User reviewer = user(2L, User.Role.ADMIN);

        when(applicationRepository.findById(10L)).thenReturn(Optional.of(pending));
        when(userRepository.findById(1L)).thenReturn(Optional.of(applicant));
        when(userRepository.findById(2L)).thenReturn(Optional.of(reviewer));
        when(applicationRepository.save(pending)).thenReturn(pending);

        ContributorApplication result = service.approveApplication(10L, 2L, "Looks good");

        assertThat(result.getStatus()).isEqualTo(ContributorApplication.Status.APPROVED);
        assertThat(result.getReviewedBy()).isEqualTo(2L);
        assertThat(result.getFeedback()).isEqualTo("Looks good");
        assertThat(result.getReviewedAt()).isNotNull();
        assertThat(applicant.getRole()).isEqualTo(User.Role.CONTRIBUTOR);

        verify(userRepository).save(applicant);
        verify(applicationRepository).save(pending);
    }

    @Test
    void approveApplicationThrowsWhenApplicationDoesNotExist() {
        when(applicationRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.approveApplication(10L, 2L, "Looks good"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("application not found");
    }

    @Test
    void approveApplicationThrowsWhenApplicationIsNotPending() {
        ContributorApplication approved = application(10L, 1L, ContributorApplication.Status.APPROVED);
        when(applicationRepository.findById(10L)).thenReturn(Optional.of(approved));

        assertThatThrownBy(() -> service.approveApplication(10L, 2L, "Looks good"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("application is not pending");
    }

    @Test
    void approveApplicationThrowsWhenApplicantDoesNotExist() {
        ContributorApplication pending = application(10L, 1L, ContributorApplication.Status.PENDING);
        when(applicationRepository.findById(10L)).thenReturn(Optional.of(pending));
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.approveApplication(10L, 2L, "Looks good"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("user not found");
    }

    @Test
    void approveApplicationThrowsWhenReviewerDoesNotExist() {
        ContributorApplication pending = application(10L, 1L, ContributorApplication.Status.PENDING);
        User applicant = user(1L, User.Role.USER);

        when(applicationRepository.findById(10L)).thenReturn(Optional.of(pending));
        when(userRepository.findById(1L)).thenReturn(Optional.of(applicant));
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.approveApplication(10L, 2L, "Looks good"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("reviewer not found");
    }

    @Test
    void approveApplicationThrowsWhenReviewerIsNotAdmin() {
        ContributorApplication pending = application(10L, 1L, ContributorApplication.Status.PENDING);
        User applicant = user(1L, User.Role.USER);
        User reviewer = user(2L, User.Role.USER);

        when(applicationRepository.findById(10L)).thenReturn(Optional.of(pending));
        when(userRepository.findById(1L)).thenReturn(Optional.of(applicant));
        when(userRepository.findById(2L)).thenReturn(Optional.of(reviewer));

        assertThatThrownBy(() -> service.approveApplication(10L, 2L, "Looks good"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("reviewer is not admin");
    }

    @Test
    void rejectApplicationMarksPendingApplicationRejected() {
        ContributorApplication pending = application(10L, 1L, ContributorApplication.Status.PENDING);
        User reviewer = user(2L, User.Role.ADMIN);

        when(applicationRepository.findById(10L)).thenReturn(Optional.of(pending));
        when(userRepository.findById(2L)).thenReturn(Optional.of(reviewer));
        when(applicationRepository.save(pending)).thenReturn(pending);

        ContributorApplication result = service.rejectApplication(10L, 2L, "Need more experience");

        assertThat(result.getStatus()).isEqualTo(ContributorApplication.Status.REJECTED);
        assertThat(result.getReviewedBy()).isEqualTo(2L);
        assertThat(result.getFeedback()).isEqualTo("Need more experience");
        assertThat(result.getReviewedAt()).isNotNull();
    }

    @Test
    void rejectApplicationThrowsWhenApplicationDoesNotExist() {
        when(applicationRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.rejectApplication(10L, 2L, "Need more experience"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("application not found");
    }

    @Test
    void rejectApplicationThrowsWhenApplicationIsNotPending() {
        ContributorApplication approved = application(10L, 1L, ContributorApplication.Status.APPROVED);
        when(applicationRepository.findById(10L)).thenReturn(Optional.of(approved));

        assertThatThrownBy(() -> service.rejectApplication(10L, 2L, "Need more experience"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("application is not pending");
    }

    @Test
    void rejectApplicationThrowsWhenReviewerDoesNotExist() {
        ContributorApplication pending = application(10L, 1L, ContributorApplication.Status.PENDING);

        when(applicationRepository.findById(10L)).thenReturn(Optional.of(pending));
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.rejectApplication(10L, 2L, "Need more experience"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("reviewer not found");
    }

    @Test
    void rejectApplicationThrowsWhenReviewerIsNotAdmin() {
        ContributorApplication pending = application(10L, 1L, ContributorApplication.Status.PENDING);
        User reviewer = user(2L, User.Role.USER);

        when(applicationRepository.findById(10L)).thenReturn(Optional.of(pending));
        when(userRepository.findById(2L)).thenReturn(Optional.of(reviewer));

        assertThatThrownBy(() -> service.rejectApplication(10L, 2L, "Need more experience"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("reviewer is not admin");
    }

    @Test
    void getMyApplicationReturnsMostRecentApplication() {
        ContributorApplication latest = application(20L, 1L, ContributorApplication.Status.APPROVED);
        ContributorApplication older = application(10L, 1L, ContributorApplication.Status.REJECTED);

        when(applicationRepository.findByUserIdOrderByCreatedAtDescIdDesc(1L))
                .thenReturn(List.of(latest, older));

        ContributorApplication result = service.getMyApplication(1L);

        assertThat(result).isSameAs(latest);
    }

    @Test
    void getMyApplicationThrowsWhenNoApplicationExists() {
        when(applicationRepository.findByUserIdOrderByCreatedAtDescIdDesc(1L)).thenReturn(List.of());

        assertThatThrownBy(() -> service.getMyApplication(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("no application found");
    }

    @Test
    void getPendingApplicationsReturnsRepositoryResult() {
        List<ContributorApplication> pendingApplications = List.of(
                application(20L, 1L, ContributorApplication.Status.PENDING),
                application(21L, 2L, ContributorApplication.Status.PENDING)
        );
        when(applicationRepository.findByStatusOrderByCreatedAtDescIdDesc(ContributorApplication.Status.PENDING))
                .thenReturn(pendingApplications);

        List<ContributorApplication> result = service.getPendingApplications();

        assertThat(result).containsExactlyElementsOf(pendingApplications);
    }

    @Test
    void revokeContributorDemotesContributorToNormalUser() {
        User contributor = user(3L, User.Role.CONTRIBUTOR);
        when(userRepository.findById(3L)).thenReturn(Optional.of(contributor));
        when(userRepository.save(contributor)).thenReturn(contributor);

        User result = service.revokeContributor(3L);

        assertThat(result.getRole()).isEqualTo(User.Role.USER);
        verify(userRepository).save(contributor);
    }

    @Test
    void revokeContributorThrowsWhenUserDoesNotExist() {
        when(userRepository.findById(3L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.revokeContributor(3L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("user not found");
    }

    @Test
    void revokeContributorThrowsWhenUserIsNotContributor() {
        when(userRepository.findById(3L)).thenReturn(Optional.of(user(3L, User.Role.USER)));

        assertThatThrownBy(() -> service.revokeContributor(3L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("user is not contributor");
    }

    private User user(Long id, User.Role role) {
        User user = new User();
        user.setId(id);
        user.setUsername("user-" + id);
        user.setRole(role);
        return user;
    }

    private ContributorApplication application(Long id, Long userId, ContributorApplication.Status status) {
        ContributorApplication application = new ContributorApplication();
        application.setId(id);
        application.setUserId(userId);
        application.setStatus(status);
        return application;
    }
}
