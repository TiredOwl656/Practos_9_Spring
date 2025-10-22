package com.blogapp.feedservice.service;

import com.blogapp.feedservice.client.PostServiceClient;
import com.blogapp.feedservice.model.FeedItem;
import com.blogapp.feedservice.model.Post;
import com.blogapp.feedservice.model.Subscription;
import com.blogapp.feedservice.model.UserFeed;
import com.blogapp.feedservice.repository.SubscriptionRepository;
import com.blogapp.feedservice.repository.UserFeedRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FeedServiceImpl implements FeedService {

    private final SubscriptionRepository subscriptionRepository;
    private final UserFeedRepository userFeedRepository;
    private final PostServiceClient postServiceClient;

    // Подписки
    @Override
    public Subscription subscribe(Long followerId, String followerUsername,
                                  Long followingId, String followingUsername) {
        if (isSubscribed(followerId, followingId)) {
            throw new RuntimeException("Already subscribed");
        }

        Subscription subscription = new Subscription();
        subscription.setFollowerId(followerId);
        subscription.setFollowerUsername(followerUsername);
        subscription.setFollowingId(followingId);
        subscription.setFollowingUsername(followingUsername);

        return subscriptionRepository.save(subscription);
    }

    @Override
    public void unsubscribe(Long followerId, Long followingId) {
        try {
            log.info("Attempting to unsubscribe: followerId={}, followingId={}", followerId, followingId);

            // Проверяем существование подписки перед удалением
            boolean exists = subscriptionRepository.existsByFollowerIdAndFollowingId(followerId, followingId);
            log.info("Subscription exists: {}", exists);

            if (!exists) {
                log.warn("Subscription not found: followerId={}, followingId={}", followerId, followingId);
                throw new RuntimeException("Subscription not found");
            }

            subscriptionRepository.deleteByFollowerIdAndFollowingId(followerId, followingId);
            log.info("Successfully unsubscribed: followerId={}, followingId={}", followerId, followingId);

        } catch (Exception e) {
            log.error("Error in unsubscribe for followerId={}, followingId={}: {}",
                    followerId, followingId, e.getMessage(), e);
            throw new RuntimeException("Unsubscribe failed: " + e.getMessage());
        }
    }

    @Override
    public List<Subscription> getUserSubscriptions(Long userId) {
        return subscriptionRepository.findByFollowerId(userId);
    }

    @Override
    public List<Subscription> getUserFollowers(Long userId) {
        return subscriptionRepository.findByFollowingId(userId);
    }

    @Override
    public boolean isSubscribed(Long followerId, Long followingId) {
        return subscriptionRepository.existsByFollowerIdAndFollowingId(followerId, followingId);
    }

    @Override
    public Long getFollowersCount(Long userId) {
        return subscriptionRepository.countFollowers(userId);
    }

    @Override
    public Long getFollowingCount(Long userId) {
        return subscriptionRepository.countFollowing(userId);
    }

    // Лента
    @Override
    public List<Long> getUserFeedPostIds(Long userId) {
        List<UserFeed> userFeeds = userFeedRepository.findByUserIdOrderByAddedAtDesc(userId);
        return userFeeds.stream()
                .map(UserFeed::getPostId)
                .toList();
    }

    @Override
    public List<FeedItem> getFullUserFeed(Long userId) {
        try {
            // 1. Получаем ID постов из ленты пользователя
            List<Long> postIds = getUserFeedPostIds(userId);

            if (postIds.isEmpty()) {
                return new ArrayList<>();
            }

            // 2. Запрашиваем полную информацию о постах из post-service
            List<Post> posts = postServiceClient.getPostsBatch(postIds);

            // 3. Преобразуем в FeedItem
            return posts.stream()
                    .map(this::convertToFeedItem)
                    .toList();

        } catch (Exception e) {
            log.error("Error getting full user feed for userId: {}", userId, e);
            return new ArrayList<>();
        }
    }

    private FeedItem convertToFeedItem(Post post) {
        return new FeedItem(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getAuthorId(),
                post.getAuthorUsername(),
                post.getLikesCount(),
                post.getCommentsCount(),
                post.getCreatedAt()
        );
    }

    @Override
    public void addToFeed(Long userId, Long postId) {
        try {
            // 1. Проверяем, что пост существует
            Post post = postServiceClient.getPostById(postId);

            // 2. Проверяем, что пользователь подписан на автора поста
            boolean isSubscribed = isSubscribed(userId, post.getAuthorId());

            if (!isSubscribed) {
                throw new RuntimeException("User is not subscribed to post author");
            }

            // 3. Добавляем пост в ленту
            if (!userFeedRepository.existsByUserIdAndPostId(userId, postId)) {
                UserFeed userFeed = new UserFeed();
                userFeed.setUserId(userId);
                userFeed.setPostId(postId);
                userFeedRepository.save(userFeed);
                log.info("Post {} added to feed for user {}", postId, userId);
            }

        } catch (Exception e) {
            log.error("Error adding post to feed: {}", e.getMessage());
            throw new RuntimeException("Failed to add post to feed: " + e.getMessage());
        }
    }

    // Автоматическое добавление постов в ленты подписчиков
    public void addPostToSubscribersFeeds(Long postId, Long authorId, String authorUsername) {
        try {
            // 1. Получаем всех подписчиков автора
            List<Subscription> followers = getUserFollowers(authorId);

            // 2. Добавляем пост в ленту каждого подписчика
            for (Subscription follower : followers) {
                try {
                    addToFeed(follower.getFollowerId(), postId);
                } catch (Exception e) {
                    log.warn("Failed to add post {} to feed of user {}", postId, follower.getFollowerId());
                }
            }

            log.info("Post {} added to {} subscribers' feeds", postId, followers.size());

        } catch (Exception e) {
            log.error("Error adding post to subscribers feeds: {}", e.getMessage());
        }
    }

    @Override
    public void removeFromFeed(Long userId, Long postId) {
        userFeedRepository.deleteByUserIdAndPostId(userId, postId);
    }

    @Override
    public void removePostFromAllFeeds(Long postId) {
        userFeedRepository.deleteByPostId(postId);
    }

    // Вспомогательный метод для получения подписчиков пользователя
    public List<Long> getUserFollowersIds(Long userId) {
        List<Subscription> followers = getUserFollowers(userId);
        return followers.stream()
                .map(Subscription::getFollowerId)
                .toList();
    }
}