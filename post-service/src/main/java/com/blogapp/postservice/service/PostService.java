package com.blogapp.postservice.service;

import com.blogapp.postservice.model.Post;
import java.util.List;
import java.util.Optional;

public interface PostService {
    Post createPost(Post post);
    Optional<Post> getPostById(Long id);
    List<Post> getAllPosts();
    List<Post> getPostsByAuthorId(Long authorId);
    List<Post> searchPosts(String query);
    Post updatePost(Long id, Post postDetails);
    void deletePost(Long id);

    void likePost(Long postId);
    void unlikePost(Long postId);
}