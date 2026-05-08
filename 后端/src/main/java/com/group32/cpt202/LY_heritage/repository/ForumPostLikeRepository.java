package com.group32.cpt202.LY_heritage.repository;

import com.group32.cpt202.LY_heritage.entity.ForumPostLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ForumPostLikeRepository extends JpaRepository<ForumPostLike, Long> {

    Optional<ForumPostLike> findByPostIdAndUserId(Long postId, Long userId);

    List<ForumPostLike> findByUserIdAndPostIdIn(Long userId, Collection<Long> postIds);

    boolean existsByPostIdAndUserId(Long postId, Long userId);

    long countByPostId(Long postId);

    void deleteByPostId(Long postId);
}
