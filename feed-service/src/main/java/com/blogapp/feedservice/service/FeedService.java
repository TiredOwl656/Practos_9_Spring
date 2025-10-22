package com.blogapp.feedservice.service;

import com.blogapp.feedservice.model.FeedItem;
import com.blogapp.feedservice.model.Subscription;
import java.util.List;

public interface FeedService {
    // Подписки (оставляем без изменений)
    Subscription subscribe(Long followerId, String followerUsername, Long followingId, String followingUsername);
    void unsubscribe(Long followerId, Long followingId);
    List<Subscription> getUserSubscriptions(Long userId);
    List<Subscription> getUserFollowers(Long userId);
    boolean isSubscribed(Long followerId, Long followingId);
    Long getFollowersCount(Long userId);
    Long getFollowingCount(Long userId);

    // Лента (УПРОЩАЕМ)
    List<Long> getUserFeedPostIds(Long userId); // Только ID постов
    void addToFeed(Long userId, Long postId);   // Только ID
    void removeFromFeed(Long userId, Long postId);
    void removePostFromAllFeeds(Long postId);
    List<FeedItem> getFullUserFeed(Long userId);
    void addPostToSubscribersFeeds(Long postId, Long authorId, String authorUsername);
}