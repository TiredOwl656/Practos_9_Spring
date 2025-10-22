package com.blogapp.webservice.controller;

import com.blogapp.webservice.model.CreatePostForm;
import com.blogapp.webservice.service.ApiClientService;
import com.blogapp.webservice.service.SessionService;
import com.blogapp.webservice.dto.User; // Добавляем импорт User из dto
import com.blogapp.webservice.dto.Post; // Добавляем импорт Post из dto
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/posts")
public class PostController {

    private final ApiClientService apiClientService;
    private final SessionService sessionService;

    @GetMapping("/create")
    public String createPostForm(Model model, HttpSession session) {
        if (!sessionService.isAuthenticated(session)) {
            return "redirect:/login";
        }
        model.addAttribute("createPostForm", new CreatePostForm());
        model.addAttribute("pageTitle", "Создать пост");
        return "post/create";
    }

    @PostMapping("/create")
    public String createPost(@Valid @ModelAttribute CreatePostForm createPostForm,
                             BindingResult result,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        if (!sessionService.isAuthenticated(session)) {
            return "redirect:/login";
        }

        if (result.hasErrors()) {
            return "post/create";
        }

        try {
            User currentUser = sessionService.getCurrentUser(session); // Теперь User распознается
            Post post = apiClientService.createPost(createPostForm, currentUser); // Теперь Post распознается

            // Уведомляем feed-service о новом посте
            apiClientService.notifyNewPost(post.getId(), currentUser.getId(), currentUser.getUsername());

            redirectAttributes.addFlashAttribute("success", "Пост успешно создан!");
            return "redirect:/feed";
        } catch (Exception e) {
            log.error("Error creating post: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Ошибка при создании поста");
            return "redirect:/posts/create";
        }
    }

    @GetMapping
    public String getAllPosts(Model model, HttpSession session) {
        try {
            var posts = apiClientService.getAllPosts();
            model.addAttribute("posts", posts);
            // Убедитесь что передается isAuthenticated или используем session.currentUser
            model.addAttribute("pageTitle", "Все посты");
            return "post/list";
        } catch (Exception e) {
            log.error("Error getting posts: {}", e.getMessage());
            model.addAttribute("error", "Ошибка при загрузке постов");
            return "post/list";
        }
    }

    @GetMapping("/{id}")
    public String getPost(@PathVariable Long id, Model model, HttpSession session) {
        try {
            var post = apiClientService.getPostById(id);

            model.addAttribute("post", post);
            model.addAttribute("isAuthenticated", sessionService.isAuthenticated(session));
            model.addAttribute("pageTitle", post.getTitle());

            return "post/detail";
        } catch (Exception e) {
            log.error("Error getting post: {}", e.getMessage());
            return "redirect:/posts";
        }
    }

    @PostMapping("/{id}/like")
    public String likePost(@PathVariable Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        if (!sessionService.isAuthenticated(session)) {
            return "redirect:/login";
        }

        try {
            apiClientService.likePost(id);
            redirectAttributes.addFlashAttribute("success", "Лайк добавлен!");
        } catch (Exception e) {
            log.error("Error liking post: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Ошибка при добавлении лайка");
        }

        return "redirect:/posts/" + id;
    }
}