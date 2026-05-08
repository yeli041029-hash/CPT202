package com.group32.cpt202.lpp_project;

import com.group32.cpt202.LY_contributor.entity.User;
import com.group32.cpt202.LY_contributor.repository.UserRepository;
import com.group32.cpt202.LY_heritage.entity.HeritageItem;
import com.group32.cpt202.LY_heritage.repository.HeritageItemRepository;
import com.group32.cpt202.LY_heritage.repository.MessageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminService {
    private static final ZoneId HERITAGE_ZONE = ZoneId.of("Asia/Shanghai");
    private static final String STATUS_DRAFT = "DRAFT";
    private static final String STATUS_PENDING = "PENDING_APPROVAL";
    private static final String STATUS_APPROVED = "APPROVED";
    private static final String STATUS_REJECTED = "REJECTED";
    private static final String STATUS_ARCHIVED = "ARCHIVED";

    private final HeritageItemRepository heritageItemRepository;
    private final ResourceArchiveRecordRepository archiveRecordRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    public AdminService(HeritageItemRepository heritageItemRepository,
                        ResourceArchiveRecordRepository archiveRecordRepository,
                        MessageRepository messageRepository,
                        UserRepository userRepository) {
        this.heritageItemRepository = heritageItemRepository;
        this.archiveRecordRepository = archiveRecordRepository;
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Result publish(Long resourceId, Long reviewerId, String feedback) {
        HeritageItem item = heritageItemRepository.findById(resourceId).orElse(null);
        if (item == null) {
            return Result.fail("resource not found");
        }
        item.setReviewedBy(reviewerId);
        item.setReviewedAt(LocalDateTime.now(HERITAGE_ZONE));
        item.setFeedback(feedback);
        item.setStatus(STATUS_APPROVED);
        heritageItemRepository.save(item);
        return Result.success("resource published");
    }

    @Transactional
    public Result reject(Long resourceId, Long reviewerId, String feedback) {
        HeritageItem item = heritageItemRepository.findById(resourceId).orElse(null);
        if (item == null) {
            return Result.fail("resource not found");
        }
        item.setReviewedBy(reviewerId);
        item.setReviewedAt(LocalDateTime.now(HERITAGE_ZONE));
        item.setFeedback(feedback);
        item.setStatus(STATUS_REJECTED);
        item.setPlatformPublished(false);
        heritageItemRepository.save(item);
        return Result.success("resource rejected");
    }

    @Transactional
    public Result archive(ResourceArchive archive) {
        if (archive == null || archive.resourceId == null) {
            return Result.fail("resourceId is required");
        }

        HeritageItem item = heritageItemRepository.findById(archive.resourceId).orElse(null);
        if (item == null) {
            return Result.fail("resource not found");
        }

        item.setStatus(STATUS_ARCHIVED);
        heritageItemRepository.save(item);

        ResourceArchiveRecord record = new ResourceArchiveRecord();
        record.setResourceId(archive.resourceId);
        record.setReason(archive.reason);
        record.setOperator(archive.operator);
        record.setArchiveTime(LocalDateTime.now());
        ResourceArchiveRecord saved = archiveRecordRepository.save(record);

        archive.id = saved.getId();
        archive.archiveTime = Date.from(saved.getArchiveTime().atZone(ZoneId.systemDefault()).toInstant());
        return Result.success("resource archived");
    }

    @Transactional
    public Result deleteResource(Long resourceId, Long adminId) {
        if (adminId == null) {
            return Result.fail("adminId is required");
        }

        User admin = userRepository.findById(adminId).orElse(null);
        if (admin == null || admin.getRole() != User.Role.ADMIN) {
            return Result.fail("admin access required");
        }

        HeritageItem item = heritageItemRepository.findById(resourceId).orElse(null);
        if (item == null) {
            return Result.fail("resource not found");
        }

        messageRepository.deleteByHeritageId(resourceId);
        archiveRecordRepository.deleteByResourceId(resourceId);
        heritageItemRepository.delete(item);
        return Result.success("resource deleted");
    }

    public List<Resource> getAllResources() {
        return heritageItemRepository.findAll().stream()
                .filter(item -> !STATUS_DRAFT.equalsIgnoreCase(item.getStatus()))
                .map(this::toResource)
                .collect(Collectors.toList());
    }

    public List<Resource> getPendingResources() {
        return heritageItemRepository.findByStatusIgnoreCaseOrderByUpdatedAtDesc(STATUS_PENDING).stream()
                .map(this::toResource)
                .collect(Collectors.toList());
    }

    public List<ResourceArchive> getArchives() {
        return archiveRecordRepository.findAllByOrderByArchiveTimeDesc().stream()
                .map(this::toArchive)
                .collect(Collectors.toList());
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
        resource.status = toStatusCode(item.getStatus());
        resource.feedback = item.getFeedback();
        resource.reviewedBy = item.getReviewedBy();
        resource.reviewedAt = toDate(item.getReviewedAt());
        resource.platformPublished = Boolean.TRUE.equals(item.getPlatformPublished());
        resource.platformPublishedAt = toDate(item.getPlatformPublishedAt());
        resource.createTime = toDate(item.getCreatedAt());
        resource.updateTime = toDate(item.getUpdatedAt());
        return resource;
    }

    private ResourceArchive toArchive(ResourceArchiveRecord record) {
        ResourceArchive archive = new ResourceArchive();
        archive.id = record.getId();
        archive.resourceId = record.getResourceId();
        archive.reason = record.getReason();
        archive.operator = record.getOperator();
        archive.archiveTime = toDate(record.getArchiveTime());
        return archive;
    }

    private int toStatusCode(String status) {
        return switch (status == null ? STATUS_DRAFT : status.toUpperCase()) {
            case STATUS_PENDING -> 1;
            case STATUS_APPROVED -> 2;
            case STATUS_REJECTED -> 3;
            case STATUS_ARCHIVED -> 4;
            default -> 0;
        };
    }

    private Date toDate(LocalDateTime time) {
        if (time == null) {
            return null;
        }
        return Date.from(time.atZone(HERITAGE_ZONE).toInstant());
    }
}
