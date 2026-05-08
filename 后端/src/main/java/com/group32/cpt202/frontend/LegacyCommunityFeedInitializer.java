package com.group32.cpt202.frontend;

import com.group32.cpt202.LY_contributor.entity.User;
import com.group32.cpt202.LY_contributor.repository.UserRepository;
import com.group32.cpt202.LY_heritage.entity.HeritageItem;
import com.group32.cpt202.LY_heritage.entity.Message;
import com.group32.cpt202.LY_heritage.repository.HeritageItemRepository;
import com.group32.cpt202.LY_heritage.repository.MessageRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Component
public class LegacyCommunityFeedInitializer implements ApplicationRunner {

    private static final String DEFAULT_PASSWORD = "123456";
    private static final String ALICE_USERNAME = "Alice";
    private static final String BOB_USERNAME = "Bob";

    private final UserRepository userRepository;
    private final HeritageItemRepository heritageItemRepository;
    private final MessageRepository messageRepository;

    public LegacyCommunityFeedInitializer(UserRepository userRepository,
                                          HeritageItemRepository heritageItemRepository,
                                          MessageRepository messageRepository) {
        this.userRepository = userRepository;
        this.heritageItemRepository = heritageItemRepository;
        this.messageRepository = messageRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        User alice = ensureContributor(
                ALICE_USERNAME,
                "alice@example.com",
                "/Resources/image/IMG_1113.JPG",
                "Ceramics enthusiast sharing classical porcelain stories."
        );
        User bob = ensureContributor(
                BOB_USERNAME,
                "bob@example.com",
                "/Resources/image/IMG_1111.JPG",
                "Artifact collector focusing on Silk Road history."
        );

        archiveLegacyIntegrationPost();
        migrateLegacyCommunityPosts();

        ensureCommunityPost(
                alice,
                "Ancient Porcelain Vase",
                "This is a beautiful porcelain vase from the Ming Dynasty. The blue and white patterns are exquisite.",
                "porcelain,ming,ceramics",
                "/Resources/image/resource1.jpg"
        );
        ensureCommunityPost(
                bob,
                "Silk Road Artifact",
                "A rare artifact discovered along the Silk Road, showcasing ancient craftsmanship.",
                "silk road,artifact,history",
                "/Resources/image/resource2.jpg"
        );
    }

    private User ensureContributor(String username, String email, String avatarUrl, String bio) {
        User user = userRepository.findAll().stream()
                .filter(item -> username.equalsIgnoreCase(item.getUsername()))
                .findFirst()
                .orElseGet(User::new);

        if (user.getId() == null) {
            user.setPassword(DEFAULT_PASSWORD);
        }
        if (user.getPassword() == null || user.getPassword().isBlank()) {
            user.setPassword(DEFAULT_PASSWORD);
        }

        user.setUsername(username);
        user.setEmail(email);
        user.setAvatarUrl(avatarUrl);
        user.setBio(bio);
        user.setRole(User.Role.CONTRIBUTOR);

        return userRepository.save(user);
    }

    private void ensureCommunityPost(User contributor,
                                     String title,
                                     String description,
                                     String tags,
                                     String imageUrl) {
        Message post = messageRepository.findFirstByUserIdAndTitleAndForumPostIdIsNullAndHeritageIdIsNull(
                        contributor.getId(),
                        title
                )
                .orElseGet(Message::new);

        post.setUserId(contributor.getId());
        post.setTitle(title);
        post.setContent(description);
        post.setTags(tags);
        post.setImageUrl(imageUrl);
        if (post.getSentAt() == null) {
            post.setSentAt(LocalDateTime.now());
        }

        messageRepository.save(post);
    }

    private void archiveLegacyIntegrationPost() {
        List<HeritageItem> items = heritageItemRepository.findAll().stream()
                .filter(item -> "Integration Heritage".equalsIgnoreCase(item.getTitle()))
                .toList();
        for (HeritageItem item : items) {
            item.setStatus("ARCHIVED");
            heritageItemRepository.save(item);
        }
    }

    private void migrateLegacyCommunityPosts() {
        List<HeritageItem> items = heritageItemRepository.findAll().stream()
                .filter(item -> Boolean.TRUE.equals(item.getCommunityPost()) || "Community".equalsIgnoreCase(item.getLocation()))
                .toList();

        for (HeritageItem item : items) {
            migrateLegacyCommunityPost(item);
        }
    }

    private void migrateLegacyCommunityPost(HeritageItem item) {
        if (item.getContributorId() == null) {
            heritageItemRepository.delete(item);
            return;
        }

        Message rootPost = messageRepository.findByLegacyHeritageItemId(item.getId())
                .orElseGet(Message::new);

        rootPost.setLegacyHeritageItemId(item.getId());
        rootPost.setUserId(item.getContributorId());
        rootPost.setTitle(item.getTitle());
        rootPost.setContent(item.getDescription());
        rootPost.setTags(item.getTags());
        rootPost.setImageUrl(item.getImageUrl());
        rootPost.setHeritageId(null);
        rootPost.setForumPostId(null);
        rootPost.setParentMessageId(null);
        if (rootPost.getSentAt() == null) {
            rootPost.setSentAt(item.getUpdatedAt() != null ? item.getUpdatedAt() : item.getCreatedAt());
        }

        rootPost = messageRepository.save(rootPost);

        List<Message> heritageComments = messageRepository.findByHeritageIdOrderBySentAtAsc(item.getId());
        for (Message comment : heritageComments) {
            comment.setForumPostId(rootPost.getId());
            comment.setHeritageId(null);
            if (Objects.equals(comment.getId(), rootPost.getId())) {
                comment.setForumPostId(null);
            }
        }
        if (!heritageComments.isEmpty()) {
            messageRepository.saveAll(heritageComments);
        }

        heritageItemRepository.delete(item);
    }
}
