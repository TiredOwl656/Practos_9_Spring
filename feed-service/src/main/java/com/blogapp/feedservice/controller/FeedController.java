package com.blogapp.feedservice.controller;

import com.blogapp.feedservice.model.FeedItem;
import com.blogapp.feedservice.model.Subscription;
import com.blogapp.feedservice.service.FeedService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/feed")
@RequiredArgsConstructor
public class FeedController {

    private final FeedService feedService;

    // Подписки
    @PostMapping("/subscribe")
    public ResponseEntity<Subscription> subscribe(
            @RequestParam Long followerId,
            @RequestParam String followerUsername,
            @RequestParam Long followingId,
            @RequestParam String followingUsername) {
        try {
            Subscription subscription = feedService.subscribe(
                    followerId, followerUsername, followingId, followingUsername);
            return ResponseEntity.ok(subscription);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/unsubscribe/{followerId}/{followingId}")
    public ResponseEntity<Void> unsubscribe(
            @PathVariable Long followerId,
            @PathVariable Long followingId) {
        try {
            feedService.unsubscribe(followerId, followingId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{userId}/subscriptions")
    public List<Subscription> getUserSubscriptions(@PathVariable Long userId) {
        return feedService.getUserSubscriptions(userId);
    }

    @GetMapping("/{userId}/followers")
    public List<Subscription> getUserFollowers(@PathVariable Long userId) {
        return feedService.getUserFollowers(userId);
    }

    @GetMapping("/{followerId}/is-subscribed/{followingId}")
    public ResponseEntity<Boolean> isSubscribed(
            @PathVariable Long followerId,
            @PathVariable Long followingId) {
        boolean isSubscribed = feedService.isSubscribed(followerId, followingId);
        return ResponseEntity.ok(isSubscribed);
    }

    @GetMapping("/{userId}/followers-count")
    public ResponseEntity<Long> getFollowersCount(@PathVariable Long userId) {
        Long count = feedService.getFollowersCount(userId);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/{userId}/following-count")
    public ResponseEntity<Long> getFollowingCount(@PathVariable Long userId) {
        Long count = feedService.getFollowingCount(userId);
        return ResponseEntity.ok(count);
    }

    // Лента
    @GetMapping("/{userId}")
    public ResponseEntity<List<FeedItem>> getUserFeed(@PathVariable Long userId) {
        try {
            List<FeedItem> feed = feedService.getFullUserFeed(userId); // Используем новый метод
            return ResponseEntity.ok(feed);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/{userId}/add-post/{postId}")
    public ResponseEntity<Void> addToFeed(
            @PathVariable Long userId,
            @PathVariable Long postId) {
        try {
            feedService.addToFeed(userId, postId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{userId}/remove-post/{postId}")
    public ResponseEntity<Void> removeFromFeed(
            @PathVariable Long userId,
            @PathVariable Long postId) {
        try {
            feedService.removeFromFeed(userId, postId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{userId}/post-ids")
    public List<Long> getUserFeedPostIds(@PathVariable Long userId) {
        return feedService.getUserFeedPostIds(userId);
    }

    @GetMapping("/{userId}/full")
    public ResponseEntity<List<FeedItem>> getFullUserFeed(@PathVariable Long userId) {
        try {
            List<FeedItem> feed = feedService.getFullUserFeed(userId);
            return ResponseEntity.ok(feed);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // Новый endpoint для автоматического добавления постов подписчикам
    @PostMapping("/notify-new-post")
    public ResponseEntity<Void> notifyNewPost(
            @RequestParam Long postId,
            @RequestParam Long authorId,
            @RequestParam String authorUsername) {
        try {
            feedService.addPostToSubscribersFeeds(postId, authorId, authorUsername);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}