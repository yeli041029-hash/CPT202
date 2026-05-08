package com.group32.cpt202.zyl_project.zyl_display;

import com.group32.cpt202.LY_heritage.dto.HeritageCommentDTO;
import com.group32.cpt202.LY_heritage.dto.MessageCreateRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HeritageDisplayControllerTest {

    @Mock
    private HeritageDisplayService service;

    private HeritageDisplayController controller;

    @BeforeEach
    void setUp() {
        controller = new HeritageDisplayController(service);
    }

    @Test
    void getHomeSummaryReturnsOkResponse() {
        HomeSummary summary = new HomeSummary();
        when(service.getHomeSummary()).thenReturn(summary);

        ResponseEntity<HomeSummary> response = controller.getHomeSummary();

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isSameAs(summary);
    }

    @Test
    void getAllReturnsServiceResult() {
        List<HeritageDisplay> displays = List.of(new HeritageDisplay());
        when(service.getAll(1L)).thenReturn(displays);

        assertThat(controller.getAll(1L).getBody()).isSameAs(displays);
    }

    @Test
    void getPlatformApprovedReturnsServiceResult() {
        List<HeritageDisplay> displays = List.of(new HeritageDisplay());
        when(service.getPlatformApproved()).thenReturn(displays);

        assertThat(controller.getPlatformApproved().getBody()).isSameAs(displays);
    }

    @Test
    void getPlatformByIdReturnsServiceResult() {
        HeritageDisplay display = new HeritageDisplay();
        when(service.getPlatformById(10L)).thenReturn(display);

        assertThat(controller.getPlatformById(10L).getBody()).isSameAs(display);
    }

    @Test
    void createReturnsCreatedDisplay() {
        CommunityPostCreateRequest request = new CommunityPostCreateRequest();
        HeritageDisplay display = new HeritageDisplay();
        when(service.createCommunityPost(request)).thenReturn(display);

        assertThat(controller.create(request).getBody()).isSameAs(display);
    }

    @Test
    void getByIdReturnsDisplay() {
        HeritageDisplay display = new HeritageDisplay();
        when(service.getById(10L, 2L)).thenReturn(display);

        assertThat(controller.getById(10L, 2L).getBody()).isSameAs(display);
    }

    @Test
    void getCommentsReturnsCommentList() {
        List<HeritageCommentDTO> comments = List.of(new HeritageCommentDTO());
        when(service.getCommunityComments(10L)).thenReturn(comments);

        assertThat(controller.getComments(10L).getBody()).isSameAs(comments);
    }

    @Test
    void addCommentReturnsUpdatedCommentList() {
        MessageCreateRequest request = new MessageCreateRequest();
        request.setUserId(2L);
        request.setContent("Nice");
        request.setParentMessageId(100L);
        List<HeritageCommentDTO> comments = List.of(new HeritageCommentDTO());
        when(service.addCommunityComment(10L, 2L, "Nice", 100L)).thenReturn(comments);

        assertThat(controller.addComment(10L, request).getBody()).isSameAs(comments);
    }

    @Test
    void deleteReturnsNoContent() {
        ResponseEntity<Void> response = controller.delete(10L, 2L);

        verify(service).deleteCommunityPost(10L, 2L);
        assertThat(response.getStatusCode().value()).isEqualTo(204);
        assertThat(response.getBody()).isNull();
    }

    @Test
    void toggleLikeReturnsUpdatedDisplay() {
        HeritageDisplay display = new HeritageDisplay();
        when(service.toggleCommunityPostLike(10L, 2L)).thenReturn(display);

        assertThat(controller.toggleLike(10L, 2L).getBody()).isSameAs(display);
    }

    @Test
    void incrementShareReturnsUpdatedDisplay() {
        HeritageDisplay display = new HeritageDisplay();
        when(service.incrementCommunityPostShare(10L, 2L)).thenReturn(display);

        assertThat(controller.incrementShare(10L, 2L).getBody()).isSameAs(display);
    }

    @Test
    void getByCategoryReturnsFilteredDisplays() {
        List<HeritageDisplay> displays = List.of(new HeritageDisplay());
        when(service.getByCategory("relics", 2L)).thenReturn(displays);

        assertThat(controller.getByCategory("relics", 2L).getBody()).isSameAs(displays);
    }

    @Test
    void getPlatformByCategoryReturnsFilteredDisplays() {
        List<HeritageDisplay> displays = List.of(new HeritageDisplay());
        when(service.getPlatformApprovedByCategory("relics")).thenReturn(displays);

        assertThat(controller.getPlatformByCategory("relics").getBody()).isSameAs(displays);
    }
}
