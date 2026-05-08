package com.group32.cpt202.LY_heritage.controller;

import com.group32.cpt202.LY_heritage.dto.HeritageDetailResponse;
import com.group32.cpt202.LY_heritage.dto.MessageCreateRequest;
import com.group32.cpt202.LY_heritage.service.HeritageItemService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HeritageItemControllerTest {

    @Mock
    private HeritageItemService heritageItemService;

    private HeritageItemController controller;

    @BeforeEach
    void setUp() {
        controller = new HeritageItemController(heritageItemService);
    }

    @Test
    void getHeritageDetailReturnsServiceResult() {
        HeritageDetailResponse response = new HeritageDetailResponse();
        response.setId(10L);
        when(heritageItemService.getHeritageDetail(10L)).thenReturn(response);

        assertThat(controller.getHeritageDetail(10L)).isSameAs(response);
    }

    @Test
    void addCommentDelegatesAndReturnsUpdatedDetail() {
        MessageCreateRequest request = new MessageCreateRequest();
        request.setUserId(2L);
        request.setContent("Nice");
        request.setParentMessageId(100L);
        HeritageDetailResponse response = new HeritageDetailResponse();
        response.setId(10L);
        when(heritageItemService.getHeritageDetail(10L)).thenReturn(response);

        HeritageDetailResponse result = controller.addComment(10L, request);

        verify(heritageItemService).addComment(10L, 2L, "Nice", 100L);
        assertThat(result).isSameAs(response);
    }
}
