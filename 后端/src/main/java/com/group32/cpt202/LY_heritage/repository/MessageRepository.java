package com.group32.cpt202.LY_heritage.repository;

import com.group32.cpt202.LY_heritage.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface MessageRepository extends JpaRepository<Message, Long> {

    List<Message> findByHeritageIdOrderBySentAtDesc(Long heritageId);

    List<Message> findByHeritageIdOrderBySentAtAsc(Long heritageId);

    long countByHeritageId(Long heritageId);

    long countByHeritageIdIsNotNull();

    void deleteByHeritageId(Long heritageId);

    List<Message> findByForumPostIdOrderBySentAtAsc(Long forumPostId);

    long countByForumPostId(Long forumPostId);

    void deleteByForumPostId(Long forumPostId);

    List<Message> findByForumPostIdIsNullAndHeritageIdIsNullAndTitleIsNotNullOrderBySentAtDesc();

    List<Message> findByForumPostIdIn(Collection<Long> forumPostIds);

    Optional<Message> findByLegacyHeritageItemId(Long legacyHeritageItemId);

    Optional<Message> findFirstByUserIdAndTitleAndForumPostIdIsNullAndHeritageIdIsNull(Long userId, String title);
}
