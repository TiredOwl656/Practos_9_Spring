package com.blogapp.webservice.dto;

import lombok.Data;

@Data
public class CreatePostRequest {
    private String title;
    private String content;
    private Long authorId;
    private String authorUsername;
}