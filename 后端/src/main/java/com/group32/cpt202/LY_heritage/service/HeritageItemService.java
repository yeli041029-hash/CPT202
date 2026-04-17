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

import java.util.ArrayList;
import java.util.List;

@Service
public class HeritageItemService {

    private HeritageItemRepository heritageItemRepository;
    private MessageRepository messageRepository;
    private UserRepository userRepository;

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

        // 1. 浏览量 +1
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
        response.setImageUrl(heritageItem.getImageUrl());
        response.setContributorId(heritageItem.getContributorId());
        response.setViewCount(currentViewCount + 1);
        response.setCreatedAt(heritageItem.getCreatedAt());
        response.setUpdatedAt(heritageItem.getUpdatedAt());

        // 2. 查询贡献者用户名
        if (heritageItem.getContributorId() != null) {
            User contributor = userRepository.findById(heritageItem.getContributorId()).orElse(null);
            if (contributor != null) {
                response.setContributorName(contributor.getUsername());
            }
        }

        // 3. 查询评论列表
        List<Message> messages = messageRepository.findByHeritageIdOrderBySentAtDesc(id);
        List<HeritageCommentDTO> commentDTOList = new ArrayList<>();

        for (Message message : messages) {
            HeritageCommentDTO commentDTO = new HeritageCommentDTO();

            User commentUser = userRepository.findById(message.getUserId()).orElse(null);
            if (commentUser != null) {
                commentDTO.setUsername(commentUser.getUsername());
            } else {
                commentDTO.setUsername("Unknown User");
            }

            commentDTO.setContent(message.getContent());
            commentDTO.setSentAt(message.getSentAt());

            commentDTOList.add(commentDTO);
        }

        response.setComments(commentDTOList);

        // 4. 查询相关推荐（优先同 category）
        List<HeritageItem> recommendationItems =
                heritageItemRepository.findTop5ByCategoryAndStatusIgnoreCaseAndIdNot(
                        heritageItem.getCategory(),
                        "APPROVED",
                        id
                );

        if (recommendationItems == null || recommendationItems.isEmpty()) {
            recommendationItems = heritageItemRepository.findTop5ByStatusIgnoreCaseAndIdNot(
                    "APPROVED",
                    id
            );
        }

        List<SimpleHeritageDTO> recommendationDTOList = new ArrayList<>();

        for (HeritageItem item : recommendationItems) {
            SimpleHeritageDTO dto = new SimpleHeritageDTO();
            dto.setId(item.getId());
            dto.setName(item.getTitle());
            dto.setImageUrl(item.getImageUrl());
            recommendationDTOList.add(dto);
        }

        response.setRecommendations(recommendationDTOList);

        return response;
    }
}
