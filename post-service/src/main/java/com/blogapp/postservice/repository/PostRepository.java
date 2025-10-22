package com.blogapp.postservice.repository;

import com.blogapp.postservice.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findByAuthorIdOrderByCreatedAtDesc(Long authorId);
    List<Post> findAllByOrderByCreatedAtDesc();

    @Query("SELECT p FROM Post p WHERE p.authorUsername LIKE %:username% ORDER BY p.createdAt DESC")
    List<Post> findByAuthorUsernameContaining(@Param("username") String username);

    @Query("SELECT p FROM Post p WHERE p.title LIKE %:title% OR p.content LIKE %:content% ORDER BY p.createdAt DESC")
    List<Post> findByTitleOrContentContaining(@Param("title") String title, @Param("content") String content);
}