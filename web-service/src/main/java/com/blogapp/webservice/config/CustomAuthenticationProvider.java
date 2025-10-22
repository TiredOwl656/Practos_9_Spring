package com.blogapp.webservice.config;

import com.blogapp.webservice.service.ApiClientService;
import com.blogapp.webservice.dto.AuthResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomAuthenticationProvider implements AuthenticationProvider {

    private final ApiClientService apiClientService;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        String password = authentication.getCredentials().toString();

        log.info("Attempting authentication for user: {}", username);

        try {
            // Аутентифицируемся через user-service (без JWT)
            AuthResponse authResponse = apiClientService.login(username, password);

            // Сохраняем authResponse в details
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    username,
                    password,
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + authResponse.getRole()))
            );

            authToken.setDetails(authResponse);

            log.info("Authentication successful for user: {}", username);
            return authToken;

        } catch (Exception e) {
            log.error("Authentication failed for user: {}. Error: {}", username, e.getMessage());
            throw new AuthenticationException("Invalid credentials") {};
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}