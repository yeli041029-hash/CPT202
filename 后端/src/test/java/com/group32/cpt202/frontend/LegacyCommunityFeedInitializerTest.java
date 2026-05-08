package com.group32.cpt202.frontend;

import com.group32.cpt202.LY_contributor.entity.User;
import com.group32.cpt202.LY_contributor.repository.UserRepository;
import com.group32.cpt202.LY_heritage.entity.HeritageItem;
import com.group32.cpt202.LY_heritage.entity.Message;
import com.group32.cpt202.LY_heritage.repository.HeritageItemRepository;
import com.group32.cpt202.LY_heritage.repository.MessageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.ApplicationArguments;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LegacyCommunityFeedInitializerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private HeritageItemRepository heritageItemRepository;

    @Mock
    private MessageRepository messageRepository;

    private LegacyCommunityFeedInitializer initializer;

    @BeforeEach
    void setUp() {
        initializer = new LegacyCommunityFeedInitializer(userRepository, heritageItemRepository, messageRepository);
    }

    @Test
    void runArchivesLegacyIntegrationItemAndSeedsCommunityPosts() throws Exception {
        HeritageItem integrationItem = new HeritageItem();
        integrationItem.setId(10L);
        integrationItem.setTitle("Integration Heritage");

        mockDefaultContributorSetup();
        when(heritageItemRepository.findAll()).thenReturn(List.of(integrationItem), List.of());
        when(messageRepository.findFirstByUserIdAndTitleAndForumPostIdIsNullAndHeritageIdIsNull(1L, "Ancient Porcelain Vase"))
                .thenReturn(Optional.empty());
        when(messageRepository.findFirstByUserIdAndTitleAndForumPostIdIsNullAndHeritageIdIsNull(2L, "Silk Road Artifact"))
                .thenReturn(Optional.empty());
        when(messageRepository.save(org.mockito.ArgumentMatchers.any(Message.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        initializer.run(mock(ApplicationArguments.class));

        assertThat(integrationItem.getStatus()).isEqualTo("ARCHIVED");
        verify(heritageItemRepository).save(integrationItem);

        ArgumentCaptor<Message> captor = ArgumentCaptor.forClass(Message.class);
        verify(messageRepository, org.mockito.Mockito.times(2)).save(captor.capture());
        assertThat(captor.getAllValues()).extracting(Message::getTitle)
                .containsExactly("Ancient Porcelain Vase", "Silk Road Artifact");
    }

    @Test
    void runMigratesLegacyCommunityPostAndComments() throws Exception {
        HeritageItem legacyItem = new HeritageItem();
        legacyItem.setId(20L);
        legacyItem.setContributorId(1L);
        legacyItem.setTitle("Legacy Post");
        legacyItem.setDescription("Legacy Description");
        legacyItem.setTags("artifact");
        legacyItem.setImageUrl("legacy-image");
        legacyItem.setCommunityPost(true);
        legacyItem.setUpdatedAt(LocalDateTime.of(2026, 5, 8, 10, 0));

        Message comment = new Message();
        comment.setId(30L);
        comment.setHeritageId(20L);
        comment.setUserId(2L);
        comment.setContent("legacy comment");

        mockDefaultContributorSetup();
        when(heritageItemRepository.findAll()).thenReturn(List.of(), List.of(legacyItem));
        when(messageRepository.findByLegacyHeritageItemId(20L)).thenReturn(Optional.empty());
        when(messageRepository.findByHeritageIdOrderBySentAtAsc(20L)).thenReturn(List.of(comment));
        when(messageRepository.findFirstByUserIdAndTitleAndForumPostIdIsNullAndHeritageIdIsNull(1L, "Ancient Porcelain Vase"))
                .thenReturn(Optional.empty());
        when(messageRepository.findFirstByUserIdAndTitleAndForumPostIdIsNullAndHeritageIdIsNull(2L, "Silk Road Artifact"))
                .thenReturn(Optional.empty());
        when(messageRepository.save(org.mockito.ArgumentMatchers.any(Message.class)))
                .thenAnswer(invocation -> {
                    Message message = invocation.getArgument(0);
                    if (message.getLegacyHeritageItemId() != null) {
                        message.setId(500L);
                    }
                    return message;
                });

        initializer.run(mock(ApplicationArguments.class));

        ArgumentCaptor<Message> captor = ArgumentCaptor.forClass(Message.class);
        verify(messageRepository, org.mockito.Mockito.atLeast(1)).save(captor.capture());
        assertThat(captor.getAllValues()).anySatisfy(saved -> {
            if (saved.getLegacyHeritageItemId() != null) {
                assertThat(saved.getLegacyHeritageItemId()).isEqualTo(20L);
                assertThat(saved.getUserId()).isEqualTo(1L);
                assertThat(saved.getTitle()).isEqualTo("Legacy Post");
            }
        });

        assertThat(comment.getForumPostId()).isEqualTo(500L);
        assertThat(comment.getHeritageId()).isNull();
        verify(messageRepository).saveAll(List.of(comment));
        verify(heritageItemRepository).delete(legacyItem);
    }

    @Test
    void runDeletesLegacyCommunityPostWithoutContributor() throws Exception {
        HeritageItem orphanItem = new HeritageItem();
        orphanItem.setId(30L);
        orphanItem.setCommunityPost(true);
        orphanItem.setTitle("Orphan");

        mockDefaultContributorSetup();
        when(heritageItemRepository.findAll()).thenReturn(List.of(), List.of(orphanItem));
        when(messageRepository.findFirstByUserIdAndTitleAndForumPostIdIsNullAndHeritageIdIsNull(1L, "Ancient Porcelain Vase"))
                .thenReturn(Optional.empty());
        when(messageRepository.findFirstByUserIdAndTitleAndForumPostIdIsNullAndHeritageIdIsNull(2L, "Silk Road Artifact"))
                .thenReturn(Optional.empty());
        when(messageRepository.save(org.mockito.ArgumentMatchers.any(Message.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        initializer.run(mock(ApplicationArguments.class));

        verify(heritageItemRepository).delete(orphanItem);
    }

    private void mockDefaultContributorSetup() {
        when(userRepository.findAll()).thenReturn(List.of(), List.of());
        when(userRepository.save(org.mockito.ArgumentMatchers.any(User.class)))
                .thenAnswer(invocation -> {
                    User user = invocation.getArgument(0);
                    if ("Alice".equalsIgnoreCase(user.getUsername())) {
                        user.setId(1L);
                    } else if ("Bob".equalsIgnoreCase(user.getUsername())) {
                        user.setId(2L);
                    }
                    return user;
                });
    }
}
