package com.blogapp.userservice.service;

import com.blogapp.userservice.model.AuthRequest;
import com.blogapp.userservice.model.AuthResponse;
import com.blogapp.userservice.model.User;

public interface AuthService {
    AuthResponse login(AuthRequest authRequest);
    String register(User user);
}