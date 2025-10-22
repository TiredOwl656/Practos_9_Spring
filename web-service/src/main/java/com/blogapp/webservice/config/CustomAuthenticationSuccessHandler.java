package com.blogapp.webservice.config;

import com.blogapp.webservice.service.SessionService;
import com.blogapp.webservice.dto.AuthResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final SessionService sessionService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        HttpSession session = request.getSession();

        // Получаем authResponse из details authentication
        Object details = authentication.getDetails();
        if (details instanceof AuthResponse authResponse) {
            // Сохраняем пользователя в сессии
            sessionService.setCurrentUser(session, authResponse);
            log.info("User {} successfully authenticated and saved in session", authResponse.getUsername());
        } else {
            log.warn("AuthResponse not found in authentication details");
        }

        // Редирект на ленту
        response.sendRedirect("/feed");
    }
}