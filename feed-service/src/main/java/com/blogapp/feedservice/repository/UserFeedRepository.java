package com.blogapp.feedservice.repository;

import com.blogapp.feedservice.model.UserFeed;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface UserFeedRepository extends JpaRepository<UserFeed, Long> {
    List<UserFeed> findByUserIdOrderByAddedAtDesc(Long userId);

    @Query("SELECT uf FROM UserFeed uf WHERE uf.userId = :userId ORDER BY uf.addedAt DESC")
    List<UserFeed> findUserFeed(@Param("userId") Long userId);

    void deleteByUserIdAndPostId(Long userId, Long postId);
    void deleteByPostId(Long postId);
    boolean existsByUserIdAndPostId(Long userId, Long postId);
}