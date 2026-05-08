package com.group32.cpt202.lpp_project;

import com.group32.cpt202.LY_contributor.entity.User;
import com.group32.cpt202.LY_contributor.repository.UserRepository;
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
class AdminServiceTest {

    @Mock
    private HeritageItemRepository heritageItemRepository;

    @Mock
    private ResourceArchiveRecordRepository archiveRecordRepository;

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private UserRepository userRepository;

    private AdminService service;

    @BeforeEach
    void setUp() {
        service = new AdminService(heritageItemRepository, archiveRecordRepository, messageRepository, userRepository);
    }

    @Test
    void publishFailsWhenResourceDoesNotExist() {
        when(heritageItemRepository.findById(10L)).thenReturn(Optional.empty());

        Result result = service.publish(10L, 2L, "ok");

        assertThat(result.code).isEqualTo(500);
        assertThat(result.msg).isEqualTo("resource not found");
    }

    @Test
    void publishMarksResourceApproved() {
        HeritageItem item = heritageItem(10L, "PENDING_APPROVAL");
        when(heritageItemRepository.findById(10L)).thenReturn(Optional.of(item));

        Result result = service.publish(10L, 2L, "approved");

        assertThat(item.getStatus()).isEqualTo("APPROVED");
        assertThat(item.getReviewedBy()).isEqualTo(2L);
        assertThat(item.getFeedback()).isEqualTo("approved");
        assertThat(item.getReviewedAt()).isNotNull();
        verify(heritageItemRepository).save(item);
        assertThat(result.code).isEqualTo(200);
        assertThat(result.msg).isEqualTo("resource published");
    }

    @Test
    void rejectFailsWhenResourceDoesNotExist() {
        when(heritageItemRepository.findById(10L)).thenReturn(Optional.empty());

        Result result = service.reject(10L, 2L, "bad");

        assertThat(result.code).isEqualTo(500);
        assertThat(result.msg).isEqualTo("resource not found");
    }

    @Test
    void rejectMarksResourceRejectedAndUnpublished() {
        HeritageItem item = heritageItem(10L, "PENDING_APPROVAL");
        item.setPlatformPublished(true);
        when(heritageItemRepository.findById(10L)).thenReturn(Optional.of(item));

        Result result = service.reject(10L, 2L, "bad");

        assertThat(item.getStatus()).isEqualTo("REJECTED");
        assertThat(item.getPlatformPublished()).isFalse();
        assertThat(item.getReviewedBy()).isEqualTo(2L);
        assertThat(item.getFeedback()).isEqualTo("bad");
        verify(heritageItemRepository).save(item);
        assertThat(result.msg).isEqualTo("resource rejected");
    }

    @Test
    void archiveFailsWhenResourceIdIsMissing() {
        Result result = service.archive(new ResourceArchive());

        assertThat(result.code).isEqualTo(500);
        assertThat(result.msg).isEqualTo("resourceId is required");
    }

    @Test
    void archiveFailsWhenResourceDoesNotExist() {
        ResourceArchive archive = new ResourceArchive();
        archive.resourceId = 10L;
        when(heritageItemRepository.findById(10L)).thenReturn(Optional.empty());

        Result result = service.archive(archive);

        assertThat(result.code).isEqualTo(500);
        assertThat(result.msg).isEqualTo("resource not found");
    }

    @Test
    void archiveMarksResourceArchivedAndSavesRecord() {
        HeritageItem item = heritageItem(10L, "APPROVED");
        ResourceArchive archive = new ResourceArchive();
        archive.resourceId = 10L;
        archive.reason = "legacy";
        archive.operator = "admin";

        when(heritageItemRepository.findById(10L)).thenReturn(Optional.of(item));
        when(archiveRecordRepository.save(org.mockito.ArgumentMatchers.any(ResourceArchiveRecord.class)))
                .thenAnswer(invocation -> {
                    ResourceArchiveRecord record = invocation.getArgument(0);
                    record.setId(99L);
                    return record;
                });

        Result result = service.archive(archive);

        assertThat(item.getStatus()).isEqualTo("ARCHIVED");
        verify(heritageItemRepository).save(item);

        ArgumentCaptor<ResourceArchiveRecord> captor = ArgumentCaptor.forClass(ResourceArchiveRecord.class);
        verify(archiveRecordRepository).save(captor.capture());
        assertThat(captor.getValue().getResourceId()).isEqualTo(10L);
        assertThat(captor.getValue().getReason()).isEqualTo("legacy");
        assertThat(captor.getValue().getOperator()).isEqualTo("admin");

        assertThat(archive.id).isEqualTo(99L);
        assertThat(archive.archiveTime).isNotNull();
        assertThat(result.msg).isEqualTo("resource archived");
    }

    @Test
    void deleteResourceFailsWhenAdminIdIsMissing() {
        Result result = service.deleteResource(10L, null);

        assertThat(result.code).isEqualTo(500);
        assertThat(result.msg).isEqualTo("adminId is required");
    }

    @Test
    void deleteResourceFailsWhenUserIsNotAdmin() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(user(2L, User.Role.USER)));

        Result result = service.deleteResource(10L, 2L);

        assertThat(result.code).isEqualTo(500);
        assertThat(result.msg).isEqualTo("admin access required");
    }

    @Test
    void deleteResourceFailsWhenResourceDoesNotExist() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(user(2L, User.Role.ADMIN)));
        when(heritageItemRepository.findById(10L)).thenReturn(Optional.empty());

        Result result = service.deleteResource(10L, 2L);

        assertThat(result.code).isEqualTo(500);
        assertThat(result.msg).isEqualTo("resource not found");
    }

    @Test
    void deleteResourceDeletesAssociationsAndItem() {
        HeritageItem item = heritageItem(10L, "APPROVED");
        when(userRepository.findById(2L)).thenReturn(Optional.of(user(2L, User.Role.ADMIN)));
        when(heritageItemRepository.findById(10L)).thenReturn(Optional.of(item));

        Result result = service.deleteResource(10L, 2L);

        verify(messageRepository).deleteByHeritageId(10L);
        verify(archiveRecordRepository).deleteByResourceId(10L);
        verify(heritageItemRepository).delete(item);
        assertThat(result.msg).isEqualTo("resource deleted");
    }

    @Test
    void getAllResourcesFiltersOutDraftsAndMapsStatuses() {
        HeritageItem draft = heritageItem(1L, "DRAFT");
        HeritageItem approved = heritageItem(2L, "APPROVED");
        approved.setTitle("Approved");

        when(heritageItemRepository.findAll()).thenReturn(List.of(draft, approved));

        List<Resource> result = service.getAllResources();

        assertThat(result).singleElement().satisfies(item -> {
            assertThat(item.id).isEqualTo(2L);
            assertThat(item.status).isEqualTo(2);
            assertThat(item.title).isEqualTo("Approved");
        });
    }

    @Test
    void getPendingResourcesMapsRepositoryResults() {
        HeritageItem pending = heritageItem(2L, "PENDING_APPROVAL");
        pending.setTitle("Pending");
        when(heritageItemRepository.findByStatusIgnoreCaseOrderByUpdatedAtDesc("PENDING_APPROVAL"))
                .thenReturn(List.of(pending));

        List<Resource> result = service.getPendingResources();

        assertThat(result).singleElement().satisfies(item -> {
            assertThat(item.id).isEqualTo(2L);
            assertThat(item.status).isEqualTo(1);
            assertThat(item.title).isEqualTo("Pending");
        });
    }

    @Test
    void getArchivesMapsArchiveRecords() {
        ResourceArchiveRecord record = new ResourceArchiveRecord();
        record.setId(10L);
        record.setResourceId(2L);
        record.setReason("legacy");
        record.setOperator("admin");
        record.setArchiveTime(LocalDateTime.of(2026, 5, 8, 10, 0));

        when(archiveRecordRepository.findAllByOrderByArchiveTimeDesc()).thenReturn(List.of(record));

        List<ResourceArchive> result = service.getArchives();

        assertThat(result).singleElement().satisfies(item -> {
            assertThat(item.id).isEqualTo(10L);
            assertThat(item.resourceId).isEqualTo(2L);
            assertThat(item.reason).isEqualTo("legacy");
            assertThat(item.operator).isEqualTo("admin");
            assertThat(item.archiveTime).isNotNull();
        });
    }

    private HeritageItem heritageItem(Long id, String status) {
        HeritageItem item = new HeritageItem();
        item.setId(id);
        item.setStatus(status);
        return item;
    }

    private User user(Long id, User.Role role) {
        User user = new User();
        user.setId(id);
        user.setUsername("user-" + id);
        user.setRole(role);
        return user;
    }
}
