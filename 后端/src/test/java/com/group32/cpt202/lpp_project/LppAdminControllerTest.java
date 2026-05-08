package com.group32.cpt202.lpp_project;

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
class LppAdminControllerTest {

    @Mock
    private AdminService adminService;

    private LppAdminController controller;

    @BeforeEach
    void setUp() {
        controller = new LppAdminController(adminService);
    }

    @Test
    void getAllResourcesReturnsServiceResult() {
        List<Resource> resources = List.of(new Resource());
        when(adminService.getAllResources()).thenReturn(resources);

        assertThat(controller.getAllResources()).isSameAs(resources);
    }

    @Test
    void getPendingResourcesReturnsServiceResult() {
        List<Resource> resources = List.of(new Resource());
        when(adminService.getPendingResources()).thenReturn(resources);

        assertThat(controller.getPendingResources()).isSameAs(resources);
    }

    @Test
    void publishDelegatesToService() {
        ResourceReviewRequest request = new ResourceReviewRequest();
        request.reviewerId = 2L;
        request.feedback = "ok";
        Result result = Result.success("resource published");
        when(adminService.publish(10L, 2L, "ok")).thenReturn(result);

        assertThat(controller.publish(10L, request)).isSameAs(result);
    }

    @Test
    void rejectDelegatesToService() {
        ResourceReviewRequest request = new ResourceReviewRequest();
        request.reviewerId = 2L;
        request.feedback = "bad";
        Result result = Result.success("resource rejected");
        when(adminService.reject(10L, 2L, "bad")).thenReturn(result);

        assertThat(controller.reject(10L, request)).isSameAs(result);
    }

    @Test
    void archiveOverridesResourceIdFromPath() {
        ResourceArchive archive = new ResourceArchive();
        Result result = Result.success("resource archived");
        when(adminService.archive(archive)).thenReturn(result);

        Result response = controller.archive(10L, archive);

        assertThat(archive.resourceId).isEqualTo(10L);
        assertThat(response).isSameAs(result);
    }

    @Test
    void deleteDelegatesToService() {
        Result result = Result.success("resource deleted");
        when(adminService.deleteResource(10L, 2L)).thenReturn(result);

        assertThat(controller.delete(10L, 2L)).isSameAs(result);
    }

    @Test
    void getArchivesReturnsServiceResult() {
        List<ResourceArchive> archives = List.of(new ResourceArchive());
        when(adminService.getArchives()).thenReturn(archives);

        assertThat(controller.getArchives()).isSameAs(archives);
    }
}
