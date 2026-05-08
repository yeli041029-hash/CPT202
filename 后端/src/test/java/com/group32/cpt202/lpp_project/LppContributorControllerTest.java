package com.group32.cpt202.lpp_project;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LppContributorControllerTest {

    @Mock
    private DraftService draftService;

    private LppContributorController controller;

    @BeforeEach
    void setUp() {
        controller = new LppContributorController(draftService);
    }

    @Test
    void saveDraftDelegatesToService() {
        ResourceDraft draft = new ResourceDraft();
        Result result = Result.success("saved");
        when(draftService.saveDraft(draft)).thenReturn(result);

        assertThat(controller.saveDraft(draft)).isSameAs(result);
    }

    @Test
    void submitDraftDelegatesToService() {
        Result result = Result.success("submitted");
        when(draftService.submitDraft(10L)).thenReturn(result);

        assertThat(controller.submitDraft(10L)).isSameAs(result);
    }

    @Test
    void getMyDraftsDelegatesToService() {
        Result result = Result.success("drafts");
        when(draftService.getMyDrafts(1L)).thenReturn(result);

        assertThat(controller.getMyDrafts(1L)).isSameAs(result);
    }

    @Test
    void getMyResourcesDelegatesToService() {
        Result result = Result.success("resources");
        when(draftService.getMyResources(1L)).thenReturn(result);

        assertThat(controller.getMyResources(1L)).isSameAs(result);
    }

    @Test
    void deleteDraftDelegatesToService() {
        Result result = Result.success("deleted");
        when(draftService.deleteDraft(10L, 1L)).thenReturn(result);

        assertThat(controller.deleteDraft(10L, 1L)).isSameAs(result);
    }

    @Test
    void deleteResourceDelegatesToService() {
        Result result = Result.success("deleted");
        when(draftService.deleteResource(10L, 1L)).thenReturn(result);

        assertThat(controller.deleteResource(10L, 1L)).isSameAs(result);
    }

    @Test
    void uploadApprovedResourceDelegatesToService() {
        Result result = Result.success("uploaded");
        when(draftService.uploadApprovedResource(10L, 1L)).thenReturn(result);

        assertThat(controller.uploadApprovedResource(10L, 1L)).isSameAs(result);
    }
}
