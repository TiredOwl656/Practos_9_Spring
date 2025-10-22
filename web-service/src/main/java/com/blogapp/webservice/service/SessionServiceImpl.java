package com.blogapp.webservice.service;

import com.blogapp.webservice.dto.AuthResponse;
import com.blogapp.webservice.dto.User;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SessionServiceImpl implements SessionService {

    private static final String CURRENT_USER = "currentUser";

    @Override
    public void setCurrentUser(HttpSession session, AuthResponse authResponse) {
        User user = new User();
        user.setId(authResponse.getUserId());
        user.setUsername(authResponse.getUsername());

        session.setAttribute(CURRENT_USER, user);
        log.info("User session created for: {} (ID: {})", user.getUsername(), user.getId());
    }

    @Override
    public User getCurrentUser(HttpSession session) {
        User user = (User) session.getAttribute(CURRENT_USER);
        if (user == null) {
            throw new RuntimeException("User not authenticated");
        }
        return user;
    }

    @Override
    public boolean isAuthenticated(HttpSession session) {
        return session.getAttribute(CURRENT_USER) != null;
    }

    @Override
    public void clearSession(HttpSession session) {
        User user = (User) session.getAttribute(CURRENT_USER);
        if (user != null) {
            log.info("User session cleared for: {}", user.getUsername());
        }
        session.removeAttribute(CURRENT_USER);
        session.invalidate();
    }
}