package com.blogapp.webservice.client;

import com.blogapp.webservice.dto.AuthRequest;
import com.blogapp.webservice.dto.AuthResponse;
import com.blogapp.webservice.dto.User;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "user-service", path = "/api")
public interface UserServiceClient {

    @PostMapping("/auth/login")
    AuthResponse login(@RequestBody AuthRequest request);

    @PostMapping("/auth/register")
    String register(@RequestBody User user);

    @GetMapping("/users")
    List<User> getAllUsers();

    @GetMapping("/users/{id}")
    User getUserById(@PathVariable Long id);

    @GetMapping("/users/profile/{username}")
    User getUserByUsername(@PathVariable String username);
}