package com.blogapp.apigateway.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class FallbackController {

    @GetMapping("/fallback/user-service")
    public ResponseEntity<Map<String, Object>> userServiceFallback() {
        Map<String, Object> response = createFallbackResponse("User Service is temporarily unavailable");
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }

    @GetMapping("/fallback/post-service")
    public ResponseEntity<Map<String, Object>> postServiceFallback() {
        Map<String, Object> response = createFallbackResponse("Post Service is temporarily unavailable");
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }

    @GetMapping("/fallback/task-service")
    public ResponseEntity<Map<String, Object>> taskServiceFallback() {
        Map<String, Object> response = createFallbackResponse("Task Service is temporarily unavailable");
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }

    @GetMapping("/fallback/feed-service")
    public ResponseEntity<Map<String, Object>> feedServiceFallback() {
        Map<String, Object> response = createFallbackResponse("Feed Service is temporarily unavailable");
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }

    @GetMapping("/fallback/default")
    public ResponseEntity<Map<String, Object>> defaultFallback() {
        Map<String, Object> response = createFallbackResponse("Service is temporarily unavailable");
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }

    private Map<String, Object> createFallbackResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", HttpStatus.SERVICE_UNAVAILABLE.value());
        response.put("message", message);
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }
}