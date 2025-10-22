package com.blogapp.webservice.client;

import com.blogapp.webservice.dto.Post;
import com.blogapp.webservice.dto.CreatePostRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "post-service", path = "/api/posts")
public interface PostServiceClient {

    // Существующие методы...
    @PostMapping
    Post createPost(@RequestBody CreatePostRequest request);

    @GetMapping
    List<Post> getAllPosts();

    @GetMapping("/{id}")
    Post getPostById(@PathVariable Long id);

    @GetMapping("/author/{authorId}")
    List<Post> getPostsByAuthor(@PathVariable Long authorId);

    @PostMapping("/{postId}/like")
    void likePost(@PathVariable Long postId);
}