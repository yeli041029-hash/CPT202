package com.group32.cpt202.lpp_project;

import com.group32.cpt202.LY_heritage.entity.HeritageItem;
import com.group32.cpt202.LY_heritage.repository.HeritageItemRepository;
import com.group32.cpt202.LY_heritage.repository.MessageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class DraftService {
    private static final ZoneId HERITAGE_ZONE = ZoneId.of("Asia/Shanghai");
    private static final String STATUS_DRAFT = "DRAFT";
    private static final String STATUS_PENDING = "PENDING_APPROVAL";
    private static final String STATUS_APPROVED = "APPROVED";
    private static final String STATUS_REJECTED = "REJECTED";

    private final HeritageItemRepository heritageItemRepository;
    private final MessageRepository messageRepository;
    private final ResourceArchiveRecordRepository archiveRecordRepository;

    public DraftService(HeritageItemRepository heritageItemRepository,
                        MessageRepository messageRepository,
                        ResourceArchiveRecordRepository archiveRecordRepository) {
        this.heritageItemRepository = heritageItemRepository;
        this.messageRepository = messageRepository;
        this.archiveRecordRepository = archiveRecordRepository;
    }

    @Transactional
    public Result saveDraft(ResourceDraft draft) {
        if (draft == null || draft.userId == null) {
            return Result.fail("userId is required");
        }

        HeritageItem item;
        if (draft.id != null) {
            item = heritageItemRepository.findById(draft.id).orElse(null);
            if (item == null) {
                return Result.fail("draft not found");
            }
            if (!Objects.equals(item.getContributorId(), draft.userId)) {
                return Result.fail("cannot edit another user's draft");
            }
            String currentStatus = item.getStatus() == null ? STATUS_DRAFT : item.getStatus().toUpperCase();
            if (!Objects.equals(currentStatus, STATUS_DRAFT) && !Objects.equals(currentStatus, STATUS_REJECTED)) {
                return Result.fail("resource cannot be edited");
            }
        } else {
            item = new HeritageItem();
            item.setContributorId(draft.userId);
        }

        item.setTitle(draft.title);
        item.setDescription(
                draft.content != null && !draft.content.isBlank() ? draft.content : draft.description
        );
        item.setCategory(draft.category);
        item.setLocation(draft.location);
        item.setTags(draft.tags);
        item.setExternalLink(draft.externalLink);
        item.setImageUrl(draft.fileUrl);
        item.setStatus(STATUS_DRAFT);
        item.setPlatformPublished(false);
        item.setCommunityPost(false);
        HeritageItem saved = heritageItemRepository.save(item);

        return Result.success(toDraft(saved));
    }

    @Transactional
    public Result submitDraft(Long draftId) {
        HeritageItem item = heritageItemRepository.findById(draftId).orElse(null);
        if (item == null) {
            return Result.fail("draft not found");
        }
        if (!Objects.equals(STATUS_DRAFT, item.getStatus()) && !Objects.equals(STATUS_REJECTED, item.getStatus())) {
            return Result.fail("draft cannot be submitted");
        }

        item.setStatus(STATUS_PENDING);
        item.setFeedback(null);
        item.setReviewedBy(null);
        item.setReviewedAt(null);
        HeritageItem saved = heritageItemRepository.save(item);
        return Result.success(toResource(saved));
    }

    public Result getMyDrafts(Long userId) {
        List<ResourceDraft> drafts = heritageItemRepository
                .findByContributorIdAndStatusIgnoreCaseOrderByUpdatedAtDesc(userId, STATUS_DRAFT)
                .stream()
                .map(this::toDraft)
                .collect(Collectors.toList());
        return Result.success(drafts);
    }

    public Result getMyResources(Long userId) {
        List<Resource> resources = heritageItemRepository.findByContributorIdOrderByUpdatedAtDesc(userId)
                .stream()
                .map(this::toResource)
                .collect(Collectors.toList());
        return Result.success(resources);
    }

    @Transactional
    public Result deleteDraft(Long draftId, Long userId) {
        HeritageItem item = heritageItemRepository.findById(draftId).orElse(null);
        if (item == null) {
            return Result.fail("draft not found");
        }
        if (!Objects.equals(item.getContributorId(), userId)) {
            return Result.fail("cannot delete another user's draft");
        }
        if (!STATUS_DRAFT.equalsIgnoreCase(item.getStatus())) {
            return Result.fail("only drafts can be deleted");
        }
        heritageItemRepository.delete(item);
        return Result.success("draft deleted");
    }

    @Transactional
    public Result deleteResource(Long resourceId, Long userId) {
        HeritageItem item = heritageItemRepository.findById(resourceId).orElse(null);
        if (item == null) {
            return Result.fail("resource not found");
        }
        if (!Objects.equals(item.getContributorId(), userId)) {
            return Result.fail("cannot delete another user's resource");
        }

        messageRepository.deleteByHeritageId(resourceId);
        archiveRecordRepository.deleteByResourceId(resourceId);
        heritageItemRepository.delete(item);
        return Result.success("resource deleted");
    }

    @Transactional
    public Result uploadApprovedResource(Long resourceId, Long userId) {
        HeritageItem item = heritageItemRepository.findById(resourceId).orElse(null);
        if (item == null) {
            return Result.fail("resource not found");
        }
        if (!Objects.equals(item.getContributorId(), userId)) {
            return Result.fail("cannot upload another user's resource");
        }
        if (!STATUS_APPROVED.equalsIgnoreCase(item.getStatus())) {
            return Result.fail("only approved resources can be uploaded");
        }
        item.setPlatformPublished(true);
        item.setPlatformPublishedAt(java.time.LocalDateTime.now(HERITAGE_ZONE));
        HeritageItem saved = heritageItemRepository.save(item);
        return Result.success(toResource(saved));
    }

    private ResourceDraft toDraft(HeritageItem item) {
        ResourceDraft draft = new ResourceDraft();
        draft.id = item.getId();
        draft.resourceId = item.getId();
        draft.userId = item.getContributorId();
        draft.title = item.getTitle();
        draft.description = item.getDescription();
        draft.content = item.getDescription();
        draft.category = item.getCategory();
        draft.location = item.getLocation();
        draft.tags = item.getTags();
        draft.externalLink = item.getExternalLink();
        draft.fileUrl = item.getImageUrl();
        draft.status = 0;
        draft.feedback = item.getFeedback();
        draft.reviewedBy = item.getReviewedBy();
        draft.reviewedAt = toDate(item.getReviewedAt());
        draft.platformPublished = Boolean.TRUE.equals(item.getPlatformPublished());
        draft.platformPublishedAt = toDate(item.getPlatformPublishedAt());
        draft.createTime = toDate(item.getCreatedAt());
        draft.updateTime = toDate(item.getUpdatedAt());
        return draft;
    }

    private Resource toResource(HeritageItem item) {
        Resource resource = new Resource();
        resource.id = item.getId();
        resource.draftId = item.getId();
        resource.title = item.getTitle();
        resource.description = item.getDescription();
        resource.content = item.getDescription();
        resource.category = item.getCategory();
        resource.location = item.getLocation();
        resource.tags = item.getTags();
        resource.externalLink = item.getExternalLink();
        resource.fileUrl = item.getImageUrl();
        resource.userId = item.getContributorId();
        resource.status = switch (item.getStatus() == null ? STATUS_DRAFT : item.getStatus().toUpperCase()) {
            case STATUS_PENDING -> 1;
            case STATUS_APPROVED -> 2;
            case STATUS_REJECTED -> 3;
            case "ARCHIVED" -> 4;
            default -> 0;
        };
        resource.feedback = item.getFeedback();
        resource.reviewedBy = item.getReviewedBy();
        resource.reviewedAt = toDate(item.getReviewedAt());
        resource.platformPublished = Boolean.TRUE.equals(item.getPlatformPublished());
        resource.platformPublishedAt = toDate(item.getPlatformPublishedAt());
        resource.createTime = toDate(item.getCreatedAt());
        resource.updateTime = toDate(item.getUpdatedAt());
        return resource;
    }

    private Date toDate(java.time.LocalDateTime time) {
        if (time == null) {
            return null;
        }
        return Date.from(time.atZone(HERITAGE_ZONE).toInstant());
    }
}
