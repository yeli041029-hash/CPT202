package com.group32.cpt202.CY_project.controller;

import com.group32.cpt202.CY_project.Result;
import com.group32.cpt202.CY_project.dto.AuditDto;
import com.group32.cpt202.CY_project.dto.PermissionAuditDto;
import com.group32.cpt202.CY_project.entity.HeritageItem;
import com.group32.cpt202.CY_project.service.WorkflowService;
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
class WorkflowControllerTest {

    @Mock
    private WorkflowService workflowService;

    private WorkflowController controller;

    @BeforeEach
    void setUp() {
        controller = new WorkflowController(workflowService);
    }

    @Test
    void applyReturnsSuccessResult() {
        Result result = controller.apply(1L, "I want to help");

        verify(workflowService).applyContributor(1L, "I want to help");
        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getMsg()).isEqualTo("application submitted");
    }

    @Test
    void pendingApplyReturnsWrappedData() {
        List<String> pending = List.of("item");
        when(workflowService.getPendingApply()).thenReturn((List) pending);

        assertThat(controller.pendingApply().getData()).isEqualTo(pending);
    }

    @Test
    void auditApplyDelegatesToService() {
        AuditDto dto = new AuditDto();
        dto.setId(10L);
        dto.setAdminId(2L);
        dto.setPass(true);
        dto.setFeedback("ok");

        Result result = controller.auditApply(dto);

        verify(workflowService).auditApply(10L, 2L, true, "ok");
        assertThat(result.getMsg()).isEqualTo("review completed");
    }

    @Test
    void myApplyReturnsWrappedData() {
        List<String> applications = List.of("app");
        when(workflowService.getMyApply(1L)).thenReturn((List) applications);

        assertThat(controller.myApply(1L).getData()).isEqualTo(applications);
    }

    @Test
    void revokeReturnsSuccessResult() {
        Result result = controller.revoke(1L);

        verify(workflowService).revokeContributor(1L);
        assertThat(result.getMsg()).isEqualTo("contributor privileges revoked");
    }

    @Test
    void pendingItemReturnsWrappedData() {
        List<HeritageItem> items = List.of(new HeritageItem());
        when(workflowService.getPendingItem()).thenReturn(items);

        assertThat(controller.pendingItem().getData()).isEqualTo(items);
    }

    @Test
    void approveItemReturnsSuccessResult() {
        Result result = controller.approveItem(10L);

        verify(workflowService).approveItem(10L);
        assertThat(result.getMsg()).isEqualTo("approved and published");
    }

    @Test
    void rejectItemDelegatesToService() {
        PermissionAuditDto dto = new PermissionAuditDto();
        dto.setId(10L);
        dto.setReason("bad");

        Result result = controller.rejectItem(dto);

        verify(workflowService).rejectItem(10L, "bad");
        assertThat(result.getMsg()).isEqualTo("rejected");
    }

    @Test
    void myRejectedReturnsWrappedData() {
        List<HeritageItem> items = List.of(new HeritageItem());
        when(workflowService.getMyRejectedItem(1L)).thenReturn(items);

        assertThat(controller.myRejected(1L).getData()).isEqualTo(items);
    }

    @Test
    void reSubmitDelegatesToService() {
        HeritageItem item = new HeritageItem();

        Result result = controller.reSubmit(10L, item);

        verify(workflowService).reSubmitItem(10L, item);
        assertThat(result.getMsg()).isEqualTo("resubmitted for review");
    }
}
