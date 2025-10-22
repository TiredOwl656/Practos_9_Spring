package com.blogapp.webservice.dto;

import lombok.Data;

@Data
public class AuthResponse {
    private String username;
    private String role;
    private Long userId;
}