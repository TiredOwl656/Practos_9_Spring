package com.blogapp.webservice.service;

import com.blogapp.webservice.dto.AuthResponse;
import com.blogapp.webservice.dto.User;
import jakarta.servlet.http.HttpSession;

public interface SessionService {
    void setCurrentUser(HttpSession session, AuthResponse authResponse);
    User getCurrentUser(HttpSession session);
    boolean isAuthenticated(HttpSession session);
    void clearSession(HttpSession session);
}