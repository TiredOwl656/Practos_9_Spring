package com.blogapp.webservice.service;

import com.blogapp.webservice.model.LoginForm;
import com.blogapp.webservice.model.RegisterForm;
import com.blogapp.webservice.model.CreatePostForm;
import com.blogapp.webservice.dto.*;

import java.util.List;

public interface ApiClientService {

    // Аутентификация
    AuthResponse login(String username, String password);
    void register(RegisterForm registerForm);

    // Пользователи
    List<User> getAllUsers();
    User getUserById(Long id);
    User getUserByUsername(String username);

    // Посты
    Post createPost(CreatePostForm form, User currentUser);
    List<Post> getAllPosts();
    Post getPostById(Long id);
    List<Post> getUserPosts(Long userId);
    void likePost(Long postId);

    // Лента и подписки
    List<FeedItem> getUserFeed(Long userId);
    void subscribe(Long followerId, String followerUsername, Long followingId, String followingUsername);
    void unsubscribe(Long followerId, Long followingId);
    boolean isSubscribed(Long followerId, Long followingId);
    void notifyNewPost(Long postId, Long authorId, String authorUsername);


}