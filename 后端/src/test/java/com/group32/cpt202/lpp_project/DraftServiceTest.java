package com.group32.cpt202.lpp_project;

import com.group32.cpt202.LY_heritage.entity.HeritageItem;
import com.group32.cpt202.LY_heritage.repository.HeritageItemRepository;
import com.group32.cpt202.LY_heritage.repository.MessageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DraftServiceTest {

    @Mock
    private HeritageItemRepository heritageItemRepository;

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private ResourceArchiveRecordRepository archiveRecordRepository;

    private DraftService service;

    @BeforeEach
    void setUp() {
        service = new DraftService(heritageItemRepository, messageRepository, archiveRecordRepository);
    }

    @Test
    void saveDraftFailsWhenUserIdIsMissing() {
        Result result = service.saveDraft(new ResourceDraft());

        assertThat(result.code).isEqualTo(500);
        assertThat(result.msg).isEqualTo("userId is required");
    }

    @Test
    void saveDraftCreatesNewDraft() {
        ResourceDraft draft = new ResourceDraft();
        draft.userId = 1L;
        draft.title = "Bronze Mirror";
        draft.description = "fallback description";
        draft.content = "full content";
        draft.category = "relics";
        draft.location = "Suzhou";
        draft.tags = "artifact,mirror";
        draft.externalLink = "https://example.com";
        draft.fileUrl = "image-1";

        when(heritageItemRepository.save(org.mockito.ArgumentMatchers.any(HeritageItem.class)))
                .thenAnswer(invocation -> {
                    HeritageItem item = invocation.getArgument(0);
                    item.setId(10L);
                    item.setCreatedAt(LocalDateTime.of(2026, 5, 8, 10, 0));
                    item.setUpdatedAt(LocalDateTime.of(2026, 5, 8, 10, 30));
                    return item;
                });

        Result result = service.saveDraft(draft);

        ArgumentCaptor<HeritageItem> captor = ArgumentCaptor.forClass(HeritageItem.class);
        verify(heritageItemRepository).save(captor.capture());

        HeritageItem saved = captor.getValue();
        assertThat(saved.getContributorId()).isEqualTo(1L);
        assertThat(saved.getTitle()).isEqualTo("Bronze Mirror");
        assertThat(saved.getDescription()).isEqualTo("full content");
        assertThat(saved.getStatus()).isEqualTo("DRAFT");
        assertThat(saved.getPlatformPublished()).isFalse();
        assertThat(saved.getCommunityPost()).isFalse();

        assertThat(result.code).isEqualTo(200);
        assertThat(result.data).isInstanceOf(ResourceDraft.class);
        ResourceDraft savedDraft = (ResourceDraft) result.data;
        assertThat(savedDraft.id).isEqualTo(10L);
        assertThat(savedDraft.title).isEqualTo("Bronze Mirror");
    }

    @Test
    void saveDraftUsesDescriptionWhenContentIsBlank() {
        ResourceDraft draft = new ResourceDraft();
        draft.id = 8L;
        draft.userId = 1L;
        draft.description = "fallback description";
        draft.content = "   ";

        HeritageItem existing = heritageItem(8L, 1L, "REJECTED");
        when(heritageItemRepository.findById(8L)).thenReturn(Optional.of(existing));
        when(heritageItemRepository.save(existing)).thenReturn(existing);

        service.saveDraft(draft);

        assertThat(existing.getDescription()).isEqualTo("fallback description");
        assertThat(existing.getStatus()).isEqualTo("DRAFT");
    }

    @Test
    void saveDraftFailsWhenExistingDraftDoesNotExist() {
        ResourceDraft draft = new ResourceDraft();
        draft.id = 8L;
        draft.userId = 1L;

        when(heritageItemRepository.findById(8L)).thenReturn(Optional.empty());

        Result result = service.saveDraft(draft);

        assertThat(result.code).isEqualTo(500);
        assertThat(result.msg).isEqualTo("draft not found");
    }

    @Test
    void saveDraftFailsWhenEditingAnotherUsersDraft() {
        ResourceDraft draft = new ResourceDraft();
        draft.id = 8L;
        draft.userId = 2L;

        when(heritageItemRepository.findById(8L)).thenReturn(Optional.of(heritageItem(8L, 1L, "DRAFT")));

        Result result = service.saveDraft(draft);

        assertThat(result.code).isEqualTo(500);
        assertThat(result.msg).isEqualTo("cannot edit another user's draft");
    }

    @Test
    void saveDraftFailsWhenResourceStatusCannotBeEdited() {
        ResourceDraft draft = new ResourceDraft();
        draft.id = 8L;
        draft.userId = 1L;

        when(heritageItemRepository.findById(8L)).thenReturn(Optional.of(heritageItem(8L, 1L, "APPROVED")));

        Result result = service.saveDraft(draft);

        assertThat(result.code).isEqualTo(500);
        assertThat(result.msg).isEqualTo("resource cannot be edited");
    }

    @Test
    void submitDraftFailsWhenDraftDoesNotExist() {
        when(heritageItemRepository.findById(10L)).thenReturn(Optional.empty());

        Result result = service.submitDraft(10L);

        assertThat(result.code).isEqualTo(500);
        assertThat(result.msg).isEqualTo("draft not found");
    }

    @Test
    void submitDraftFailsWhenStatusIsInvalid() {
        when(heritageItemRepository.findById(10L)).thenReturn(Optional.of(heritageItem(10L, 1L, "APPROVED")));

        Result result = service.submitDraft(10L);

        assertThat(result.code).isEqualTo(500);
        assertThat(result.msg).isEqualTo("draft cannot be submitted");
    }

    @Test
    void submitDraftMarksPendingAndClearsReviewFields() {
        HeritageItem item = heritageItem(10L, 1L, "REJECTED");
        item.setFeedback("old");
        item.setReviewedBy(99L);
        item.setReviewedAt(LocalDateTime.of(2026, 5, 8, 10, 0));

        when(heritageItemRepository.findById(10L)).thenReturn(Optional.of(item));
        when(heritageItemRepository.save(item)).thenReturn(item);

        Result result = service.submitDraft(10L);

        assertThat(item.getStatus()).isEqualTo("PENDING_APPROVAL");
        assertThat(item.getFeedback()).isNull();
        assertThat(item.getReviewedBy()).isNull();
        assertThat(item.getReviewedAt()).isNull();
        assertThat(result.code).isEqualTo(200);
        assertThat(result.data).isInstanceOf(Resource.class);
    }

    @Test
    void getMyDraftsMapsRepositoryResults() {
        HeritageItem draft = heritageItem(10L, 1L, "DRAFT");
        draft.setTitle("Draft Title");
        draft.setDescription("Draft Description");

        when(heritageItemRepository.findByContributorIdAndStatusIgnoreCaseOrderByUpdatedAtDesc(1L, "DRAFT"))
                .thenReturn(List.of(draft));

        Result result = service.getMyDrafts(1L);

        assertThat(result.code).isEqualTo(200);
        @SuppressWarnings("unchecked")
        List<ResourceDraft> drafts = (List<ResourceDraft>) result.data;
        assertThat(drafts).singleElement().satisfies(item -> {
            assertThat(item.id).isEqualTo(10L);
            assertThat(item.title).isEqualTo("Draft Title");
            assertThat(item.status).isEqualTo(0);
        });
    }

    @Test
    void getMyResourcesMapsStatusCodes() {
        HeritageItem approved = heritageItem(10L, 1L, "APPROVED");
        HeritageItem archived = heritageItem(11L, 1L, "ARCHIVED");

        when(heritageItemRepository.findByContributorIdOrderByUpdatedAtDesc(1L))
                .thenReturn(List.of(approved, archived));

        Result result = service.getMyResources(1L);

        assertThat(result.code).isEqualTo(200);
        @SuppressWarnings("unchecked")
        List<Resource> resources = (List<Resource>) result.data;
        assertThat(resources).hasSize(2);
        assertThat(resources.get(0).status).isEqualTo(2);
        assertThat(resources.get(1).status).isEqualTo(4);
    }

    @Test
    void deleteDraftFailsWhenDraftDoesNotExist() {
        when(heritageItemRepository.findById(10L)).thenReturn(Optional.empty());

        Result result = service.deleteDraft(10L, 1L);

        assertThat(result.code).isEqualTo(500);
        assertThat(result.msg).isEqualTo("draft not found");
    }

    @Test
    void deleteDraftFailsWhenOwnedByAnotherUser() {
        when(heritageItemRepository.findById(10L)).thenReturn(Optional.of(heritageItem(10L, 2L, "DRAFT")));

        Result result = service.deleteDraft(10L, 1L);

        assertThat(result.code).isEqualTo(500);
        assertThat(result.msg).isEqualTo("cannot delete another user's draft");
    }

    @Test
    void deleteDraftFailsWhenStatusIsNotDraft() {
        when(heritageItemRepository.findById(10L)).thenReturn(Optional.of(heritageItem(10L, 1L, "APPROVED")));

        Result result = service.deleteDraft(10L, 1L);

        assertThat(result.code).isEqualTo(500);
        assertThat(result.msg).isEqualTo("only drafts can be deleted");
    }

    @Test
    void deleteDraftDeletesDraft() {
        HeritageItem item = heritageItem(10L, 1L, "DRAFT");
        when(heritageItemRepository.findById(10L)).thenReturn(Optional.of(item));

        Result result = service.deleteDraft(10L, 1L);

        verify(heritageItemRepository).delete(item);
        assertThat(result.code).isEqualTo(200);
        assertThat(result.msg).isEqualTo("draft deleted");
    }

    @Test
    void deleteResourceFailsWhenResourceDoesNotExist() {
        when(heritageItemRepository.findById(10L)).thenReturn(Optional.empty());

        Result result = service.deleteResource(10L, 1L);

        assertThat(result.code).isEqualTo(500);
        assertThat(result.msg).isEqualTo("resource not found");
    }

    @Test
    void deleteResourceFailsWhenOwnedByAnotherUser() {
        when(heritageItemRepository.findById(10L)).thenReturn(Optional.of(heritageItem(10L, 2L, "APPROVED")));

        Result result = service.deleteResource(10L, 1L);

        assertThat(result.code).isEqualTo(500);
        assertThat(result.msg).isEqualTo("cannot delete another user's resource");
    }

    @Test
    void deleteResourceDeletesResourceAndAssociations() {
        HeritageItem item = heritageItem(10L, 1L, "APPROVED");
        when(heritageItemRepository.findById(10L)).thenReturn(Optional.of(item));

        Result result = service.deleteResource(10L, 1L);

        verify(messageRepository).deleteByHeritageId(10L);
        verify(archiveRecordRepository).deleteByResourceId(10L);
        verify(heritageItemRepository).delete(item);
        assertThat(result.code).isEqualTo(200);
        assertThat(result.msg).isEqualTo("resource deleted");
    }

    @Test
    void uploadApprovedResourceFailsWhenResourceDoesNotExist() {
        when(heritageItemRepository.findById(10L)).thenReturn(Optional.empty());

        Result result = service.uploadApprovedResource(10L, 1L);

        assertThat(result.code).isEqualTo(500);
        assertThat(result.msg).isEqualTo("resource not found");
    }

    @Test
    void uploadApprovedResourceFailsWhenOwnedByAnotherUser() {
        when(heritageItemRepository.findById(10L)).thenReturn(Optional.of(heritageItem(10L, 2L, "APPROVED")));

        Result result = service.uploadApprovedResource(10L, 1L);

        assertThat(result.code).isEqualTo(500);
        assertThat(result.msg).isEqualTo("cannot upload another user's resource");
    }

    @Test
    void uploadApprovedResourceFailsWhenStatusIsNotApproved() {
        when(heritageItemRepository.findById(10L)).thenReturn(Optional.of(heritageItem(10L, 1L, "REJECTED")));

        Result result = service.uploadApprovedResource(10L, 1L);

        assertThat(result.code).isEqualTo(500);
        assertThat(result.msg).isEqualTo("only approved resources can be uploaded");
    }

    @Test
    void uploadApprovedResourceMarksResourceAsPublished() {
        HeritageItem item = heritageItem(10L, 1L, "APPROVED");
        when(heritageItemRepository.findById(10L)).thenReturn(Optional.of(item));
        when(heritageItemRepository.save(item)).thenReturn(item);

        Result result = service.uploadApprovedResource(10L, 1L);

        assertThat(item.getPlatformPublished()).isTrue();
        assertThat(item.getPlatformPublishedAt()).isNotNull();
        assertThat(result.code).isEqualTo(200);
        assertThat(result.data).isInstanceOf(Resource.class);
    }

    private HeritageItem heritageItem(Long id, Long contributorId, String status) {
        HeritageItem item = new HeritageItem();
        item.setId(id);
        item.setContributorId(contributorId);
        item.setStatus(status);
        return item;
    }
}
