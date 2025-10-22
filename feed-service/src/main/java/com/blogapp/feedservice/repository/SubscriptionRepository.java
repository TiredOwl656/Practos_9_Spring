package com.blogapp.feedservice.repository;

import com.blogapp.feedservice.model.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    Optional<Subscription> findByFollowerIdAndFollowingId(Long followerId, Long followingId);
    List<Subscription> findByFollowerId(Long followerId);
    List<Subscription> findByFollowingId(Long followingId);
    boolean existsByFollowerIdAndFollowingId(Long followerId, Long followingId);

    @Query("SELECT COUNT(s) FROM Subscription s WHERE s.followingId = :userId")
    Long countFollowers(@Param("userId") Long userId);

    @Query("SELECT COUNT(s) FROM Subscription s WHERE s.followerId = :userId")
    Long countFollowing(@Param("userId") Long userId);

    void deleteByFollowerIdAndFollowingId(Long followerId, Long followingId);
}