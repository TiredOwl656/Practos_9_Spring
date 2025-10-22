package com.blogapp.userservice.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AuthResponse {
    private String username;
    private String role;
    private Long userId;

    // Конструктор с параметрами
    public AuthResponse(String username, String role, Long userId) {
        this.username = username;
        this.role = role;
        this.userId = userId;
    }
}