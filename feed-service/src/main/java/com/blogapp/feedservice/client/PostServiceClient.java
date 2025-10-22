package com.blogapp.feedservice.client;

import com.blogapp.feedservice.model.Post;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "post-service", path = "/api/posts")
public interface PostServiceClient {

    @GetMapping("/{id}")
    Post getPostById(@PathVariable Long id);

    @PostMapping("/batch")
    List<Post> getPostsBatch(@RequestBody List<Long> postIds);
}