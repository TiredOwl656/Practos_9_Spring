package com.blogapp.feedservice.model;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class FeedItem {
    private Long postId;
    private String title;
    private String content;
    private Long authorId;
    private String authorUsername;
    private Integer likesCount;
    private Integer commentsCount;
    private LocalDateTime postCreatedAt;
    private LocalDateTime addedToFeedAt;

    public FeedItem(Long postId, String title, String content, Long authorId,
                    String authorUsername, Integer likesCount, Integer commentsCount,
                    LocalDateTime postCreatedAt) {
        this.postId = postId;
        this.title = title;
        this.content = content;
        this.authorId = authorId;
        this.authorUsername = authorUsername;
        this.likesCount = likesCount;
        this.commentsCount = commentsCount;
        this.postCreatedAt = postCreatedAt;
        this.addedToFeedAt = LocalDateTime.now();
    }
}