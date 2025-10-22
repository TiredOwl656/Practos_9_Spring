package com.blogapp.webservice.client;

import com.blogapp.webservice.dto.FeedItem;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "feed-service", path = "/api/feed")
public interface FeedServiceClient {

    @PostMapping("/subscribe")
    Object subscribe(@RequestParam Long followerId,
                     @RequestParam String followerUsername,
                     @RequestParam Long followingId,
                     @RequestParam String followingUsername);

    // ИЗМЕНИТЕ ЭТОТ МЕТОД:
    @DeleteMapping("/unsubscribe/{followerId}/{followingId}")
    void unsubscribe(@PathVariable Long followerId,
                     @PathVariable Long followingId);

    @GetMapping("/{followerId}/is-subscribed/{followingId}")
    boolean isSubscribed(@PathVariable Long followerId,
                         @PathVariable Long followingId);

    @GetMapping("/{userId}/full")
    List<FeedItem> getFullUserFeed(@PathVariable Long userId);

    @PostMapping("/notify-new-post")
    void notifyNewPost(@RequestParam Long postId,
                       @RequestParam Long authorId,
                       @RequestParam String authorUsername);
}