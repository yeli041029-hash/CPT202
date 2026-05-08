package com.group32.cpt202.LY_contributor.controller;

import com.group32.cpt202.LY_contributor.dto.ReviewRequest;
import com.group32.cpt202.LY_contributor.dto.UserSummaryResponse;
import com.group32.cpt202.LY_contributor.entity.ContributorApplication;
import com.group32.cpt202.LY_contributor.entity.User;
import com.group32.cpt202.LY_contributor.service.ContributorApplicationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminContributorControllerTest {

    @Mock
    private ContributorApplicationService contributorApplicationService;

    private AdminContributorController controller;

    @BeforeEach
    void setUp() {
        controller = new AdminContributorController(contributorApplicationService);
    }

    @Test
    void getPendingApplicationsReturnsServiceResult() {
        List<ContributorApplication> pending = List.of(new ContributorApplication());
        when(contributorApplicationService.getPendingApplications()).thenReturn(pending);

        assertThat(controller.getPendingApplications()).isSameAs(pending);
    }

    @Test
    void approveApplicationDelegatesToService() {
        ReviewRequest request = new ReviewRequest();
        request.setReviewedBy(2L);
        request.setFeedback("ok");
        ContributorApplication application = new ContributorApplication();
        when(contributorApplicationService.approveApplication(10L, 2L, "ok")).thenReturn(application);

        assertThat(controller.approveApplication(10L, request)).isSameAs(application);
    }

    @Test
    void rejectApplicationDelegatesToService() {
        ReviewRequest request = new ReviewRequest();
        request.setReviewedBy(2L);
        request.setFeedback("bad");
        ContributorApplication application = new ContributorApplication();
        when(contributorApplicationService.rejectApplication(10L, 2L, "bad")).thenReturn(application);

        assertThat(controller.rejectApplication(10L, request)).isSameAs(application);
    }

    @Test
    void revokeContributorReturnsMappedSummary() {
        User user = new User();
        user.setId(5L);
        user.setUsername("alice");
        user.setRole(User.Role.USER);
        when(contributorApplicationService.revokeContributor(5L)).thenReturn(user);

        UserSummaryResponse result = controller.revokeContributor(5L);

        assertThat(result.getId()).isEqualTo(5L);
        assertThat(result.getUsername()).isEqualTo("alice");
        assertThat(result.getRole()).isEqualTo(User.Role.USER);
    }
}
