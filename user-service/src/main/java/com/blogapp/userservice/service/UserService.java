package com.blogapp.userservice.service;

import com.blogapp.userservice.model.User;
import java.util.List;
import java.util.Optional;

public interface UserService {
    User registerUser(User user);
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    List<User> getAllUsers();
    Optional<User> getUserById(Long id);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}