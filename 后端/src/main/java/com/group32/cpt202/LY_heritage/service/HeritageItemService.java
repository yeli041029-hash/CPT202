package com.group32.cpt202.LY_heritage.service;

import com.group32.cpt202.LY_contributor.entity.User;
import com.group32.cpt202.LY_contributor.repository.UserRepository;
import com.group32.cpt202.LY_heritage.dto.HeritageCommentDTO;
import com.group32.cpt202.LY_heritage.dto.HeritageDetailResponse;
import com.group32.cpt202.LY_heritage.dto.SimpleHeritageDTO;
import com.group32.cpt202.LY_heritage.entity.HeritageItem;
import com.group32.cpt202.LY_heritage.entity.Message;
import com.group32.cpt202.LY_heritage.repository.HeritageItemRepository;
import com.group32.cpt202.LY_heritage.repository.MessageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class HeritageItemService {

    private final HeritageItemRepository heritageItemRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    public HeritageItemService(HeritageItemRepository heritageItemRepository,
                               MessageRepository messageRepository,
                               UserRepository userRepository) {
        this.heritageItemRepository = heritageItemRepository;
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public HeritageDetailResponse getHeritageDetail(Long id) {
        HeritageItem heritageItem = heritageItemRepository.findById(id).orElse(null);
        if (heritageItem == null) {
            throw new RuntimeException("heritage item not found");
        }
        if (!"APPROVED".equalsIgnoreCase(heritageItem.getStatus())) {
            throw new RuntimeException("heritage item is not available");
        }

        Integer currentViewCount = heritageItem.getViewCount();
        if (currentViewCount == null) {
            currentViewCount = 0;
        }
        heritageItemRepository.incrementViewCount(id);

        HeritageDetailResponse response = new HeritageDetailResponse();
        response.setId(heritageItem.getId());
        response.setTitle(heritageItem.getTitle());
        response.setDescription(heritageItem.getDescription());
        response.setCategory(heritageItem.getCategory());
        response.setLocation(heritageItem.getLocation());
        response.setTags(heritageItem.getTags());
        response.setExternalLink(heritageItem.getExternalLink());
        response.setImageUrl(heritageItem.getImageUrl());
        response.setContributorId(heritageItem.getContributorId());
        response.setViewCount(currentViewCount + 1);
        response.setCreatedAt(heritageItem.getCreatedAt());
        response.setUpdatedAt(heritageItem.getUpdatedAt());

        if (heritageItem.getContributorId() != null) {
            User contributor = userRepository.findById(heritageItem.getContributorId()).orElse(null);
            if (contributor != null) {
                response.setContributorName(contributor.getUsername());
            }
        }

        response.setComments(buildCommentTree(id));

        List<HeritageItem> recommendationItems =
                heritageItemRepository.findTop5ByCategoryAndStatusIgnoreCaseAndIdNot(
                        heritageItem.getCategory(),
                        "APPROVED",
                        id
                );
        if (recommendationItems == null || recommendationItems.isEmpty()) {
            recommendationItems = heritageItemRepository.findTop5ByStatusIgnoreCaseAndIdNot("APPROVED", id);
        }

        List<SimpleHeritageDTO> recommendations = new ArrayList<>();
        for (HeritageItem item : recommendationItems) {
            recommendations.add(new SimpleHeritageDTO(item.getId(), item.getTitle(), item.getImageUrl()));
        }
        response.setRecommendations(recommendations);

        return response;
    }

    @Transactional
    public void addComment(Long heritageId, Long userId, String content, Long parentMessageId) {
        if (userId == null) {
            throw new RuntimeException("userId is required");
        }
        if (content == null || content.isBlank()) {
            throw new RuntimeException("content is required");
        }

        HeritageItem heritageItem = heritageItemRepository.findById(heritageId).orElse(null);
        if (heritageItem == null) {
            throw new RuntimeException("heritage item not found");
        }
        if (!"APPROVED".equalsIgnoreCase(heritageItem.getStatus())) {
            throw new RuntimeException("heritage item is not available");
        }

        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            throw new RuntimeException("user not found");
        }

        if (parentMessageId != null) {
            Message parentMessage = messageRepository.findById(parentMessageId).orElse(null);
            if (parentMessage == null) {
                throw new RuntimeException("parent comment not found");
            }
            if (!heritageId.equals(parentMessage.getHeritageId())) {
                throw new RuntimeException("reply target does not belong to the same heritage item");
            }
        }

        Message message = new Message();
        message.setHeritageId(heritageId);
        message.setUserId(userId);
        message.setParentMessageId(parentMessageId);
        message.setContent(content.trim());
        message.setSentAt(LocalDateTime.now());
        messageRepository.save(message);
    }

    private List<HeritageCommentDTO> buildCommentTree(Long heritageId) {
        List<Message> messages = messageRepository.findByHeritageIdOrderBySentAtAsc(heritageId);
        Map<Long, User> usersById = new HashMap<>();
        Map<Long, Message> messagesById = new HashMap<>();
        Map<Long, HeritageCommentDTO> commentsById = new LinkedHashMap<>();

        for (Message message : messages) {
            messagesById.put(message.getId(), message);
            if (message.getUserId() != null && !usersById.containsKey(message.getUserId())) {
                usersById.put(message.getUserId(), userRepository.findById(message.getUserId()).orElse(null));
            }
        }

        for (Message message : messages) {
            HeritageCommentDTO comment = new HeritageCommentDTO();
            User commentUser = usersById.get(message.getUserId());
            comment.setId(message.getId());
            comment.setParentMessageId(message.getParentMessageId());
            comment.setUsername(commentUser == null ? "Unknown User" : commentUser.getUsername());
            comment.setContent(message.getContent());
            comment.setSentAt(message.getSentAt());
            comment.setReplies(new ArrayList<>());
            commentsById.put(message.getId(), comment);
        }

        List<HeritageCommentDTO> topLevelComments = new ArrayList<>();
        for (Message message : messages) {
            HeritageCommentDTO comment = commentsById.get(message.getId());
            Long parentMessageId = message.getParentMessageId();
            if (parentMessageId == null) {
                topLevelComments.add(comment);
                continue;
            }

            HeritageCommentDTO parentComment = commentsById.get(parentMessageId);
            Message parentMessage = messagesById.get(parentMessageId);
            if (parentComment == null || parentMessage == null) {
                topLevelComments.add(comment);
                continue;
            }

            User parentUser = usersById.get(parentMessage.getUserId());
            comment.setReplyToUsername(parentUser == null ? "Unknown User" : parentUser.getUsername());
            parentComment.getReplies().add(comment);
        }

        topLevelComments.sort(Comparator.comparing(
                HeritageCommentDTO::getSentAt,
                Comparator.nullsLast(Comparator.reverseOrder())
        ));
        return topLevelComments;
    }
}
