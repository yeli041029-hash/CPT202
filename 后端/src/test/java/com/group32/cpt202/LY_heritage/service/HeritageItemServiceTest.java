package com.group32.cpt202.LY_heritage.service;

import com.group32.cpt202.LY_contributor.entity.User;
import com.group32.cpt202.LY_contributor.repository.UserRepository;
import com.group32.cpt202.LY_heritage.dto.HeritageCommentDTO;
import com.group32.cpt202.LY_heritage.dto.HeritageDetailResponse;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HeritageItemServiceTest {

    @Mock
    private HeritageItemRepository heritageItemRepository;

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private UserRepository userRepository;

    private HeritageItemService service;

    @BeforeEach
    void setUp() {
        service = new HeritageItemService(heritageItemRepository, messageRepository, userRepository);
    }

    @Test
    void getHeritageDetailReturnsEnrichedDetailAndBuildsCommentTree() {
        HeritageItem heritageItem = approvedHeritage(1L, "architecture", 2, 7L);
        heritageItem.setTitle("Old Temple");
        heritageItem.setDescription("Historic wooden temple");

        Message olderTopLevel = message(101L, 1L, null, LocalDateTime.of(2026, 5, 1, 10, 0), "First comment");
        Message reply = message(102L, 2L, 101L, LocalDateTime.of(2026, 5, 1, 10, 30), "Reply comment");
        Message newerTopLevel = message(103L, 3L, null, LocalDateTime.of(2026, 5, 2, 9, 0), "Latest comment");

        when(heritageItemRepository.findById(1L)).thenReturn(Optional.of(heritageItem));
        when(messageRepository.findByHeritageIdOrderBySentAtAsc(1L))
                .thenReturn(List.of(olderTopLevel, reply, newerTopLevel));
        when(userRepository.findById(7L)).thenReturn(Optional.of(namedUser(7L, "contributor")));
        when(userRepository.findById(1L)).thenReturn(Optional.of(namedUser(1L, "alice")));
        when(userRepository.findById(2L)).thenReturn(Optional.of(namedUser(2L, "bob")));
        when(userRepository.findById(3L)).thenReturn(Optional.of(namedUser(3L, "carol")));
        when(heritageItemRepository.findTop5ByCategoryAndStatusIgnoreCaseAndIdNot("architecture", "APPROVED", 1L))
                .thenReturn(List.of(recommendation(9L, "City Gate")));

        HeritageDetailResponse response = service.getHeritageDetail(1L);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getTitle()).isEqualTo("Old Temple");
        assertThat(response.getContributorName()).isEqualTo("contributor");
        assertThat(response.getViewCount()).isEqualTo(3);
        assertThat(response.getRecommendations())
                .singleElement()
                .satisfies(item -> {
                    assertThat(item.getId()).isEqualTo(9L);
                    assertThat(item.getName()).isEqualTo("City Gate");
                });

        assertThat(response.getComments()).hasSize(2);
        assertThat(response.getComments().get(0).getId()).isEqualTo(103L);
        HeritageCommentDTO threadedComment = response.getComments().get(1);
        assertThat(threadedComment.getId()).isEqualTo(101L);
        assertThat(threadedComment.getUsername()).isEqualTo("alice");
        assertThat(threadedComment.getReplies()).hasSize(1);
        assertThat(threadedComment.getReplies().get(0).getReplyToUsername()).isEqualTo("alice");
        assertThat(threadedComment.getReplies().get(0).getUsername()).isEqualTo("bob");

        verify(heritageItemRepository).incrementViewCount(1L);
    }

    @Test
    void getHeritageDetailFallsBackToGenericRecommendations() {
        HeritageItem heritageItem = approvedHeritage(1L, "architecture", 0, null);

        when(heritageItemRepository.findById(1L)).thenReturn(Optional.of(heritageItem));
        when(messageRepository.findByHeritageIdOrderBySentAtAsc(1L)).thenReturn(List.of());
        when(heritageItemRepository.findTop5ByCategoryAndStatusIgnoreCaseAndIdNot("architecture", "APPROVED", 1L))
                .thenReturn(List.of());
        when(heritageItemRepository.findTop5ByStatusIgnoreCaseAndIdNot("APPROVED", 1L))
                .thenReturn(List.of(recommendation(5L, "Bronze Drum")));

        HeritageDetailResponse response = service.getHeritageDetail(1L);

        assertThat(response.getRecommendations())
                .singleElement()
                .satisfies(item -> assertThat(item.getName()).isEqualTo("Bronze Drum"));
    }

    @Test
    void getHeritageDetailThrowsWhenItemDoesNotExist() {
        when(heritageItemRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getHeritageDetail(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("heritage item not found");
    }

    @Test
    void getHeritageDetailThrowsWhenItemIsNotApproved() {
        HeritageItem heritageItem = approvedHeritage(1L, "architecture", 0, null);
        heritageItem.setStatus("PENDING");
        when(heritageItemRepository.findById(1L)).thenReturn(Optional.of(heritageItem));

        assertThatThrownBy(() -> service.getHeritageDetail(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("heritage item is not available");
    }

    @Test
    void getHeritageDetailHandlesNullViewCountAndMissingUsers() {
        HeritageItem heritageItem = approvedHeritage(1L, "architecture", null, 7L);
        Message comment = message(101L, 55L, null, LocalDateTime.of(2026, 5, 1, 10, 0), "First comment");

        when(heritageItemRepository.findById(1L)).thenReturn(Optional.of(heritageItem));
        when(messageRepository.findByHeritageIdOrderBySentAtAsc(1L)).thenReturn(List.of(comment));
        when(userRepository.findById(7L)).thenReturn(Optional.empty());
        when(userRepository.findById(55L)).thenReturn(Optional.empty());
        when(heritageItemRepository.findTop5ByCategoryAndStatusIgnoreCaseAndIdNot("architecture", "APPROVED", 1L))
                .thenReturn(List.of());
        when(heritageItemRepository.findTop5ByStatusIgnoreCaseAndIdNot("APPROVED", 1L))
                .thenReturn(List.of());

        HeritageDetailResponse response = service.getHeritageDetail(1L);

        assertThat(response.getViewCount()).isEqualTo(1);
        assertThat(response.getContributorName()).isNull();
        assertThat(response.getComments())
                .singleElement()
                .satisfies(item -> assertThat(item.getUsername()).isEqualTo("Unknown User"));
    }

    @Test
    void addCommentSavesTrimmedComment() {
        HeritageItem heritageItem = approvedHeritage(1L, "architecture", 0, null);
        User user = namedUser(9L, "commenter");

        when(heritageItemRepository.findById(1L)).thenReturn(Optional.of(heritageItem));
        when(userRepository.findById(9L)).thenReturn(Optional.of(user));

        service.addComment(1L, 9L, "  Great post  ", null);

        ArgumentCaptor<Message> captor = ArgumentCaptor.forClass(Message.class);
        verify(messageRepository).save(captor.capture());

        Message saved = captor.getValue();
        assertThat(saved.getHeritageId()).isEqualTo(1L);
        assertThat(saved.getUserId()).isEqualTo(9L);
        assertThat(saved.getParentMessageId()).isNull();
        assertThat(saved.getContent()).isEqualTo("Great post");
        assertThat(saved.getSentAt()).isNotNull();
    }

    @Test
    void addCommentThrowsWhenContentIsBlank() {
        assertThatThrownBy(() -> service.addComment(1L, 9L, "   ", null))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("content is required");
    }

    @Test
    void addCommentThrowsWhenHeritageItemDoesNotExist() {
        when(heritageItemRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.addComment(1L, 9L, "Reply", null))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("heritage item not found");
    }

    @Test
    void addCommentThrowsWhenHeritageItemIsNotApproved() {
        HeritageItem heritageItem = approvedHeritage(1L, "architecture", 0, null);
        heritageItem.setStatus("PENDING");
        when(heritageItemRepository.findById(1L)).thenReturn(Optional.of(heritageItem));

        assertThatThrownBy(() -> service.addComment(1L, 9L, "Reply", null))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("heritage item is not available");
    }

    @Test
    void addCommentThrowsWhenUserDoesNotExist() {
        HeritageItem heritageItem = approvedHeritage(1L, "architecture", 0, null);
        when(heritageItemRepository.findById(1L)).thenReturn(Optional.of(heritageItem));
        when(userRepository.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.addComment(1L, 9L, "Reply", null))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("user not found");
    }

    @Test
    void addCommentThrowsWhenParentCommentDoesNotExist() {
        HeritageItem heritageItem = approvedHeritage(1L, "architecture", 0, null);
        User user = namedUser(9L, "commenter");

        when(heritageItemRepository.findById(1L)).thenReturn(Optional.of(heritageItem));
        when(userRepository.findById(9L)).thenReturn(Optional.of(user));
        when(messageRepository.findById(88L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.addComment(1L, 9L, "Reply", 88L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("parent comment not found");
    }

    @Test
    void addCommentThrowsWhenReplyTargetBelongsToDifferentHeritage() {
        HeritageItem heritageItem = approvedHeritage(1L, "architecture", 0, null);
        User user = namedUser(9L, "commenter");
        Message parent = new Message();
        parent.setId(88L);
        parent.setHeritageId(2L);

        when(heritageItemRepository.findById(1L)).thenReturn(Optional.of(heritageItem));
        when(userRepository.findById(9L)).thenReturn(Optional.of(user));
        when(messageRepository.findById(88L)).thenReturn(Optional.of(parent));

        assertThatThrownBy(() -> service.addComment(1L, 9L, "Reply", 88L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("reply target does not belong to the same heritage item");
    }

    @Test
    void addCommentThrowsWhenUserIdIsMissing() {
        assertThatThrownBy(() -> service.addComment(1L, null, "Reply", null))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("userId is required");
    }

    private HeritageItem approvedHeritage(Long id, String category, Integer viewCount, Long contributorId) {
        HeritageItem item = new HeritageItem();
        item.setId(id);
        item.setTitle("Heritage-" + id);
        item.setDescription("Description-" + id);
        item.setCategory(category);
        item.setLocation("Suzhou");
        item.setStatus("APPROVED");
        item.setViewCount(viewCount);
        item.setContributorId(contributorId);
        return item;
    }

    private HeritageItem recommendation(Long id, String title) {
        HeritageItem item = new HeritageItem();
        item.setId(id);
        item.setTitle(title);
        item.setImageUrl("image-" + id);
        return item;
    }

    private Message message(Long id, Long userId, Long parentMessageId, LocalDateTime sentAt, String content) {
        Message message = new Message();
        message.setId(id);
        message.setHeritageId(1L);
        message.setUserId(userId);
        message.setParentMessageId(parentMessageId);
        message.setSentAt(sentAt);
        message.setContent(content);
        return message;
    }

    private User namedUser(Long id, String username) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setRole(User.Role.USER);
        return user;
    }
}
