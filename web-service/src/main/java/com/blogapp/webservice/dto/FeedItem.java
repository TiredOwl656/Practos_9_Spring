package com.blogapp.webservice.dto;

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
}