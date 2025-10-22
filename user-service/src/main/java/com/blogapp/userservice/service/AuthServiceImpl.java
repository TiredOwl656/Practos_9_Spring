package com.blogapp.userservice.service;

import com.blogapp.userservice.model.AuthRequest;
import com.blogapp.userservice.model.AuthResponse;
import com.blogapp.userservice.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;
    private final UserService userService;

    @Override
    public AuthResponse login(AuthRequest authRequest) {
        try {
            System.out.println("Login attempt for user: " + authRequest.getUsername());

            // Проверяем существование пользователя ДО аутентификации
            Optional<User> userOpt = userService.findByUsername(authRequest.getUsername());
            if (userOpt.isEmpty()) {
                System.out.println("User not found: " + authRequest.getUsername());
                throw new RuntimeException("User not found");
            }

            // Аутентифицируем пользователя
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword())
            );

            // Получаем UserDetails
            final UserDetails userDetails = userDetailsService.loadUserByUsername(authRequest.getUsername());

            // Находим пользователя в БД чтобы получить ID
            userOpt = userService.findByUsername(authRequest.getUsername());
            if (userOpt.isEmpty()) {
                throw new RuntimeException("User not found");
            }

            User user = userOpt.get();

            // Создаем AuthResponse
            return new AuthResponse(
                    user.getUsername(),
                    user.getRole().name(),
                    user.getId()
            );

        } catch (Exception e) {
            System.out.println("Authentication failed: " + e.getMessage());
            throw new RuntimeException("Invalid credentials: " + e.getMessage());
        }
    }

    @Override
    public String register(User user) {
        try {
            User savedUser = userService.registerUser(user);
            return "User registered successfully: " + savedUser.getUsername();
        } catch (Exception e) {
            throw new RuntimeException("Registration failed: " + e.getMessage());
        }
    }
}