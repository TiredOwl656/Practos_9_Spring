package com.blogapp.webservice.service;

import com.blogapp.webservice.client.UserServiceClient;
import com.blogapp.webservice.client.PostServiceClient;
import com.blogapp.webservice.client.FeedServiceClient;
import com.blogapp.webservice.model.RegisterForm;
import com.blogapp.webservice.model.CreatePostForm;
import com.blogapp.webservice.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApiClientServiceImpl implements ApiClientService {

    private final UserServiceClient userServiceClient;
    private final PostServiceClient postServiceClient;
    private final FeedServiceClient feedServiceClient;

    @Override
    public AuthResponse login(String username, String password) {
        return userServiceClient.login(new AuthRequest(username, password));
    }

    @Override
    public void register(RegisterForm registerForm) {
        User user = new User();
        user.setUsername(registerForm.getUsername());
        user.setEmail(registerForm.getEmail());
        user.setPassword(registerForm.getPassword());
        user.setFirstName(registerForm.getFirstName());
        user.setLastName(registerForm.getLastName());

        userServiceClient.register(user);
    }

    @Override
    public List<User> getAllUsers() {
        return userServiceClient.getAllUsers();
    }

    @Override
    public User getUserById(Long id) {
        return userServiceClient.getUserById(id);
    }

    @Override
    public User getUserByUsername(String username) {
        return userServiceClient.getUserByUsername(username);
    }

    @Override
    public Post createPost(CreatePostForm form, User currentUser) {
        CreatePostRequest request = new CreatePostRequest();
        request.setTitle(form.getTitle());
        request.setContent(form.getContent());
        request.setAuthorId(currentUser.getId());
        request.setAuthorUsername(currentUser.getUsername());

        return postServiceClient.createPost(request);
    }

    @Override
    public List<Post> getAllPosts() {
        return postServiceClient.getAllPosts();
    }

    @Override
    public List<Post> getUserPosts(Long userId) {
        try {
            return postServiceClient.getPostsByAuthor(userId);
        } catch (Exception e) {
            log.error("Error getting user posts for userId {}: {}", userId, e.getMessage());
            return new ArrayList<>(); // Возвращаем пустой список вместо ошибки
        }
    }

    @Override
    public Post getPostById(Long id) {
        return postServiceClient.getPostById(id);
    }

    @Override
    public void likePost(Long postId) {
        postServiceClient.likePost(postId);
    }

    @Override
    public List<FeedItem> getUserFeed(Long userId) {
        try {
            return feedServiceClient.getFullUserFeed(userId);
        } catch (Exception e) {
            log.error("Error getting user feed for userId {}: {}", userId, e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public void subscribe(Long followerId, String followerUsername, Long followingId, String followingUsername) {
        feedServiceClient.subscribe(followerId, followerUsername, followingId, followingUsername);
    }

    @Override
    public void unsubscribe(Long followerId, Long followingId) {
        feedServiceClient.unsubscribe(followerId, followingId);
    }

    @Override
    public boolean isSubscribed(Long followerId, Long followingId) {
        return feedServiceClient.isSubscribed(followerId, followingId);
    }

    @Override
    public void notifyNewPost(Long postId, Long authorId, String authorUsername) {
        feedServiceClient.notifyNewPost(postId, authorId, authorUsername);
    }
}