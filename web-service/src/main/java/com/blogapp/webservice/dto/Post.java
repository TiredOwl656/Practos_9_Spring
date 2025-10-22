package com.blogapp.webservice.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Post {
    private Long id;
    private String title;
    private String content;
    private Long authorId;
    private String authorUsername;
    private Integer likesCount = 0;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Post() {}
}