package com.group32.cpt202.zyl_project.zyl_display;

import com.group32.cpt202.LY_contributor.entity.ContributorApplication;
import com.group32.cpt202.LY_contributor.entity.User;
import com.group32.cpt202.LY_contributor.repository.ContributorApplicationRepository;
import com.group32.cpt202.LY_contributor.repository.UserRepository;
import com.group32.cpt202.LY_heritage.dto.HeritageCommentDTO;
import com.group32.cpt202.LY_heritage.entity.ForumPostLike;
import com.group32.cpt202.LY_heritage.entity.Message;
import com.group32.cpt202.LY_heritage.repository.ForumPostLikeRepository;
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
class HeritageDisplayServiceTest {

    @Mock
    private HeritageDisplayRepository repository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ContributorApplicationRepository contributorApplicationRepository;

    @Mock
    private HeritageItemRepository heritageItemRepository;

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private ForumPostLikeRepository forumPostLikeRepository;

    private HeritageDisplayService service;

    @BeforeEach
    void setUp() {
        service = new HeritageDisplayService(
                repository,
                userRepository,
                contributorApplicationRepository,
                heritageItemRepository,
                messageRepository,
                forumPostLikeRepository
        );
    }

    @Test
    void getAllEnrichesCommunityPosts() {
        Message post = communityPost(10L, 1L, "Silk Road Artifact", "A rare artifact", "artifact,history",
                "https://example.com/post.png", 3L, 2L);
        Message comment1 = comment(101L, 10L, null, 2L, "Nice");
        Message comment2 = comment(102L, 10L, 101L, 3L, "Thanks");
        ForumPostLike like = like(10L, 99L);

        when(messageRepository.findByForumPostIdIsNullAndHeritageIdIsNullAndTitleIsNotNullOrderBySentAtDesc())
                .thenReturn(List.of(post));
        when(userRepository.findAllById(List.of(1L))).thenReturn(List.of(namedUser(1L, "alice")));
        when(messageRepository.findByForumPostIdIn(List.of(10L))).thenReturn(List.of(comment1, comment2));
        when(forumPostLikeRepository.findByUserIdAndPostIdIn(99L, List.of(10L))).thenReturn(List.of(like));

        List<HeritageDisplay> result = service.getAll(99L);

        assertThat(result).singleElement().satisfies(item -> {
            assertThat(item.getId()).isEqualTo(10L);
            assertThat(item.getContributorName()).isEqualTo("alice");
            assertThat(item.getCommentCount()).isEqualTo(2L);
            assertThat(item.getLikedByCurrentUser()).isTrue();
            assertThat(item.getCategory()).isEqualTo("relics");
            assertThat(item.getLikeCount()).isEqualTo(3L);
            assertThat(item.getShareCount()).isEqualTo(2L);
        });
    }

    @Test
    void getByIdReturnsEnrichedDisplay() {
        Message post = communityPost(10L, 1L, "Temple Story", "building story", "heritage",
                "https://example.com/post.png", 0L, 0L);
        when(messageRepository.findById(10L)).thenReturn(Optional.of(post));
        when(userRepository.findAllById(List.of(1L))).thenReturn(List.of(namedUser(1L, "alice")));
        when(messageRepository.findByForumPostIdIn(List.of(10L))).thenReturn(List.of());
        when(forumPostLikeRepository.findByUserIdAndPostIdIn(99L, List.of(10L))).thenReturn(List.of());

        HeritageDisplay result = service.getById(10L, 99L);

        assertThat(result.getTitle()).isEqualTo("Temple Story");
        assertThat(result.getContributorName()).isEqualTo("alice");
        assertThat(result.getCategory()).isEqualTo("architecture");
    }

    @Test
    void getByCategoryFiltersPostsByDerivedCategory() {
        Message relicPost = communityPost(10L, 1L, "Artifact Story", "about relic", "artifact",
                "https://example.com/post.png", 0L, 0L);
        Message folklorePost = communityPost(11L, 2L, "Legend", "folktale", "legend",
                "https://example.com/post2.png", 0L, 0L);

        when(messageRepository.findByForumPostIdIsNullAndHeritageIdIsNullAndTitleIsNotNullOrderBySentAtDesc())
                .thenReturn(List.of(relicPost, folklorePost));
        when(userRepository.findAllById(List.of(1L))).thenReturn(List.of(namedUser(1L, "alice")));
        when(messageRepository.findByForumPostIdIn(List.of(10L))).thenReturn(List.of());

        List<HeritageDisplay> result = service.getByCategory("relics", null);

        assertThat(result).singleElement().extracting(HeritageDisplay::getId).isEqualTo(10L);
    }

    @Test
    void getPlatformByIdThrowsWhenPlatformItemDoesNotExist() {
        when(repository.findPlatformApprovedById(10L)).thenReturn(null);

        assertThatThrownBy(() -> service.getPlatformById(10L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("platform heritage not found");
    }

    @Test
    void getHomeSummaryAggregatesCountsAndLatestDisplays() {
        HeritageDisplay latest = display(10L, 1L);
        when(heritageItemRepository.countByStatusIgnoreCase("APPROVED")).thenReturn(8L);
        when(userRepository.countByRole(User.Role.CONTRIBUTOR)).thenReturn(3L);
        when(contributorApplicationRepository.countByStatus(ContributorApplication.Status.PENDING)).thenReturn(2L);
        when(messageRepository.countByHeritageIdIsNotNull()).thenReturn(11L);
        when(repository.findRecentApproved(6)).thenReturn(List.of(latest));
        when(userRepository.findAllById(List.of(1L))).thenReturn(List.of(namedUser(1L, "alice")));
        when(messageRepository.findByForumPostIdIn(List.of(10L))).thenReturn(List.of());

        HomeSummary result = service.getHomeSummary();

        assertThat(result.getApprovedHeritageCount()).isEqualTo(8L);
        assertThat(result.getContributorCount()).isEqualTo(3L);
        assertThat(result.getPendingApplicationCount()).isEqualTo(2L);
        assertThat(result.getCommentCount()).isEqualTo(11L);
        assertThat(result.getLatestHeritages()).singleElement()
                .extracting(HeritageDisplay::getContributorName)
                .isEqualTo("alice");
    }

    @Test
    void createCommunityPostRejectsNullRequest() {
        assertThatThrownBy(() -> service.createCommunityPost(null))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("request is required");
    }

    @Test
    void createCommunityPostRejectsPdfMedia() {
        CommunityPostCreateRequest request = new CommunityPostCreateRequest();
        request.setUserId(1L);
        request.setTitle("Title");
        request.setDescription("Description");
        request.setImageUrl("document.pdf");

        assertThatThrownBy(() -> service.createCommunityPost(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("PDF files are not supported in community posts");
    }

    @Test
    void createCommunityPostSavesTrimmedPost() {
        CommunityPostCreateRequest request = new CommunityPostCreateRequest();
        request.setUserId(1L);
        request.setTitle("  Title  ");
        request.setDescription("  Description  ");
        request.setTags("  artifact  ");
        request.setImageUrl("https://example.com/post.png");

        User user = namedUser(1L, "alice");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(messageRepository.save(org.mockito.ArgumentMatchers.any(Message.class)))
                .thenAnswer(invocation -> {
                    Message post = invocation.getArgument(0);
                    post.setId(10L);
                    return post;
                });
        when(userRepository.findAllById(List.of(1L))).thenReturn(List.of(user));
        when(messageRepository.findByForumPostIdIn(List.of(10L))).thenReturn(List.of());
        when(forumPostLikeRepository.findByUserIdAndPostIdIn(1L, List.of(10L))).thenReturn(List.of());

        HeritageDisplay result = service.createCommunityPost(request);

        ArgumentCaptor<Message> captor = ArgumentCaptor.forClass(Message.class);
        verify(messageRepository).save(captor.capture());
        Message saved = captor.getValue();

        assertThat(saved.getTitle()).isEqualTo("Title");
        assertThat(saved.getContent()).isEqualTo("Description");
        assertThat(saved.getTags()).isEqualTo("artifact");
        assertThat(saved.getLikeCount()).isEqualTo(0L);
        assertThat(saved.getShareCount()).isEqualTo(0L);
        assertThat(result.getContributorName()).isEqualTo("alice");
    }

    @Test
    void deleteCommunityPostRejectsNonAuthor() {
        Message post = communityPost(10L, 1L, "Title", "Description", "artifact", "https://example.com/post.png", 0L, 0L);
        when(messageRepository.findById(10L)).thenReturn(Optional.of(post));

        assertThatThrownBy(() -> service.deleteCommunityPost(10L, 99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("only the post author can delete this post");
    }

    @Test
    void deleteCommunityPostDeletesPostAndAssociations() {
        Message post = communityPost(10L, 1L, "Title", "Description", "artifact", "https://example.com/post.png", 0L, 0L);
        when(messageRepository.findById(10L)).thenReturn(Optional.of(post));

        service.deleteCommunityPost(10L, 1L);

        verify(forumPostLikeRepository).deleteByPostId(10L);
        verify(messageRepository).deleteByForumPostId(10L);
        verify(messageRepository).delete(post);
    }

    @Test
    void toggleCommunityPostLikeAddsNewLike() {
        Message post = communityPost(10L, 1L, "Title", "Description", "artifact", "https://example.com/post.png", 1L, 0L);
        User currentUser = namedUser(99L, "viewer");

        when(messageRepository.findById(10L)).thenReturn(Optional.of(post));
        when(userRepository.findById(99L)).thenReturn(Optional.of(currentUser));
        when(forumPostLikeRepository.findByPostIdAndUserId(10L, 99L)).thenReturn(Optional.empty());
        when(messageRepository.save(post)).thenReturn(post);
        when(userRepository.findAllById(List.of(1L))).thenReturn(List.of(namedUser(1L, "alice")));
        when(messageRepository.findByForumPostIdIn(List.of(10L))).thenReturn(List.of());
        when(forumPostLikeRepository.findByUserIdAndPostIdIn(99L, List.of(10L))).thenReturn(List.of(like(10L, 99L)));

        HeritageDisplay result = service.toggleCommunityPostLike(10L, 99L);

        assertThat(post.getLikeCount()).isEqualTo(2L);
        assertThat(result.getLikedByCurrentUser()).isTrue();
        verify(forumPostLikeRepository).save(org.mockito.ArgumentMatchers.any(ForumPostLike.class));
    }

    @Test
    void toggleCommunityPostLikeRemovesExistingLikeWithoutGoingNegative() {
        Message post = communityPost(10L, 1L, "Title", "Description", "artifact", "https://example.com/post.png", 0L, 0L);
        User currentUser = namedUser(99L, "viewer");
        ForumPostLike existingLike = like(10L, 99L);

        when(messageRepository.findById(10L)).thenReturn(Optional.of(post));
        when(userRepository.findById(99L)).thenReturn(Optional.of(currentUser));
        when(forumPostLikeRepository.findByPostIdAndUserId(10L, 99L)).thenReturn(Optional.of(existingLike));
        when(messageRepository.save(post)).thenReturn(post);
        when(userRepository.findAllById(List.of(1L))).thenReturn(List.of(namedUser(1L, "alice")));
        when(messageRepository.findByForumPostIdIn(List.of(10L))).thenReturn(List.of());
        when(forumPostLikeRepository.findByUserIdAndPostIdIn(99L, List.of(10L))).thenReturn(List.of());

        HeritageDisplay result = service.toggleCommunityPostLike(10L, 99L);

        assertThat(post.getLikeCount()).isEqualTo(0L);
        assertThat(result.getLikedByCurrentUser()).isFalse();
        verify(forumPostLikeRepository).delete(existingLike);
    }

    @Test
    void incrementCommunityPostShareIncrementsCount() {
        Message post = communityPost(10L, 1L, "Title", "Description", "artifact", "https://example.com/post.png", 1L, 1L);
        when(messageRepository.findById(10L)).thenReturn(Optional.of(post));
        when(messageRepository.save(post)).thenReturn(post);
        when(userRepository.findAllById(List.of(1L))).thenReturn(List.of(namedUser(1L, "alice")));
        when(messageRepository.findByForumPostIdIn(List.of(10L))).thenReturn(List.of());
        when(forumPostLikeRepository.findByUserIdAndPostIdIn(99L, List.of(10L))).thenReturn(List.of());

        HeritageDisplay result = service.incrementCommunityPostShare(10L, 99L);

        assertThat(post.getShareCount()).isEqualTo(2L);
        assertThat(result.getShareCount()).isEqualTo(2L);
    }

    @Test
    void getCommunityCommentsBuildsCommentTreeWithUnknownUserFallback() {
        Message post = communityPost(10L, 1L, "Title", "Description", "artifact", "https://example.com/post.png", 0L, 0L);
        Message parent = comment(101L, 10L, null, 2L, "Nice");
        Message reply = comment(102L, 10L, 101L, 3L, "Reply");

        when(messageRepository.findById(10L)).thenReturn(Optional.of(post));
        when(messageRepository.findByForumPostIdOrderBySentAtAsc(10L)).thenReturn(List.of(parent, reply));
        when(userRepository.findAllById(List.of(2L, 3L))).thenReturn(List.of(namedUser(2L, "alice")));

        List<HeritageCommentDTO> result = service.getCommunityComments(10L);

        assertThat(result).singleElement().satisfies(item -> {
            assertThat(item.getUsername()).isEqualTo("alice");
            assertThat(item.getReplies()).singleElement().satisfies(replyItem -> {
                assertThat(replyItem.getUsername()).isEqualTo("Unknown User");
                assertThat(replyItem.getReplyToUsername()).isEqualTo("alice");
            });
        });
    }

    @Test
    void addCommunityCommentRejectsParentCommentFromAnotherPost() {
        Message post = communityPost(10L, 1L, "Title", "Description", "artifact", "https://example.com/post.png", 0L, 0L);
        Message parent = comment(101L, 99L, null, 2L, "Nice");
        parent.setForumPostId(99L);

        when(messageRepository.findById(10L)).thenReturn(Optional.of(post));
        when(userRepository.findById(2L)).thenReturn(Optional.of(namedUser(2L, "alice")));
        when(messageRepository.findById(101L)).thenReturn(Optional.of(parent));

        assertThatThrownBy(() -> service.addCommunityComment(10L, 2L, "Reply", 101L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("parent comment not found");
    }

    @Test
    void addCommunityCommentSavesCommentAndReturnsUpdatedTree() {
        Message post = communityPost(10L, 1L, "Title", "Description", "artifact", "https://example.com/post.png", 0L, 0L);
        User user = namedUser(2L, "alice");
        Message savedComment = comment(101L, 10L, null, 2L, "Reply");

        when(messageRepository.findById(10L)).thenReturn(Optional.of(post), Optional.of(post));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(messageRepository.findByForumPostIdOrderBySentAtAsc(10L)).thenReturn(List.of(savedComment));
        when(userRepository.findAllById(List.of(2L))).thenReturn(List.of(user));

        List<HeritageCommentDTO> result = service.addCommunityComment(10L, 2L, "  Reply  ", null);

        ArgumentCaptor<Message> captor = ArgumentCaptor.forClass(Message.class);
        verify(messageRepository).save(captor.capture());
        assertThat(captor.getValue().getContent()).isEqualTo("Reply");
        assertThat(result).singleElement().extracting(HeritageCommentDTO::getUsername).isEqualTo("alice");
    }

    private HeritageDisplay display(Long id, Long contributorId) {
        HeritageDisplay display = new HeritageDisplay();
        display.setId(id);
        display.setContributorId(contributorId);
        display.setImageUrl("https://example.com/post.png");
        return display;
    }

    private Message communityPost(Long id,
                                  Long userId,
                                  String title,
                                  String content,
                                  String tags,
                                  String imageUrl,
                                  Long likeCount,
                                  Long shareCount) {
        Message message = new Message();
        message.setId(id);
        message.setUserId(userId);
        message.setTitle(title);
        message.setContent(content);
        message.setTags(tags);
        message.setImageUrl(imageUrl);
        message.setLikeCount(likeCount);
        message.setShareCount(shareCount);
        message.setSentAt(LocalDateTime.of(2026, 5, 8, 10, 0));
        return message;
    }

    private Message comment(Long id, Long forumPostId, Long parentMessageId, Long userId, String content) {
        Message message = new Message();
        message.setId(id);
        message.setForumPostId(forumPostId);
        message.setParentMessageId(parentMessageId);
        message.setUserId(userId);
        message.setContent(content);
        message.setSentAt(LocalDateTime.of(2026, 5, 8, 10, 0));
        return message;
    }

    private ForumPostLike like(Long postId, Long userId) {
        ForumPostLike like = new ForumPostLike();
        like.setPostId(postId);
        like.setUserId(userId);
        return like;
    }

    private User namedUser(Long id, String username) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setRole(User.Role.USER);
        return user;
    }
}
