package com.group32.cpt202.LY_contributor.controller;

import com.group32.cpt202.LY_contributor.dto.ApplicationRequest;
import com.group32.cpt202.LY_contributor.entity.ContributorApplication;
import com.group32.cpt202.LY_contributor.service.ContributorApplicationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContributorApplicationControllerTest {

    @Mock
    private ContributorApplicationService contributorApplicationService;

    private ContributorApplicationController controller;

    @BeforeEach
    void setUp() {
        controller = new ContributorApplicationController(contributorApplicationService);
    }

    @Test
    void submitApplicationDelegatesToService() {
        ApplicationRequest request = new ApplicationRequest();
        request.setUserId(1L);
        request.setApplicationReason("I want to help");
        request.setApplicantName("Alice");
        request.setDomain("crafts");
        request.setPortfolioUrl("https://portfolio.test");

        String result = controller.submitApplication(request);

        verify(contributorApplicationService).submitApplication(1L, "I want to help", "Alice", "crafts", "https://portfolio.test");
        assertThat(result).isEqualTo("application submitted");
    }

    @Test
    void getMyApplicationReturnsServiceResult() {
        ContributorApplication application = new ContributorApplication();
        application.setId(10L);
        when(contributorApplicationService.getMyApplication(1L)).thenReturn(application);

        ContributorApplication result = controller.getMyApplication(1L);

        assertThat(result).isSameAs(application);
    }
}
