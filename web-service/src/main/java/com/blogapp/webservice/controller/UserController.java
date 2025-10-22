package com.blogapp.webservice.controller;

import com.blogapp.webservice.service.ApiClientService;
import com.blogapp.webservice.service.SessionService;
import com.blogapp.webservice.dto.User;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final ApiClientService apiClientService;
    private final SessionService sessionService;

    @GetMapping
    public String getAllUsers(Model model, HttpSession session) {
        try {
            var users = apiClientService.getAllUsers();
            User currentUser = null;
            Map<Long, Boolean> subscriptionStatus = new HashMap<>();

            if (sessionService.isAuthenticated(session)) {
                currentUser = sessionService.getCurrentUser(session);

                // Проверяем подписку для каждого пользователя с обработкой ошибок
                for (User user : users) {
                    if (!user.getId().equals(currentUser.getId())) {
                        try {
                            boolean isSubscribed = apiClientService.isSubscribed(currentUser.getId(), user.getId());
                            subscriptionStatus.put(user.getId(), isSubscribed);
                        } catch (Exception e) {
                            log.warn("Could not check subscription status for user {}: {}", user.getId(), e.getMessage());
                            subscriptionStatus.put(user.getId(), false); // По умолчанию false
                        }
                    }
                }
            }

            model.addAttribute("users", users);
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("subscriptionStatus", subscriptionStatus);
            model.addAttribute("pageTitle", "Все пользователи");
            return "user/list";
        } catch (Exception e) {
            log.error("Error getting users: {}", e.getMessage());
            model.addAttribute("error", "Ошибка при загрузке пользователей");
            return "user/list";
        }
    }

    @GetMapping("/{id}")
    public String getUserProfile(@PathVariable Long id, Model model, HttpSession session) {
        try {
            User user = apiClientService.getUserById(id);
            var posts = apiClientService.getUserPosts(id);

            boolean isSubscribed = false;
            User currentUser = null;

            if (sessionService.isAuthenticated(session)) {
                currentUser = sessionService.getCurrentUser(session);
                if (!id.equals(currentUser.getId())) {
                    isSubscribed = apiClientService.isSubscribed(currentUser.getId(), id);
                }
            }

            model.addAttribute("user", user);
            model.addAttribute("posts", posts);
            model.addAttribute("isSubscribed", isSubscribed);
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("pageTitle", "Профиль " + user.getUsername());

            return "user/profile";
        } catch (Exception e) {
            log.error("Error getting user profile: {}", e.getMessage());
            return "redirect:/users";
        }
    }

    @GetMapping("/profile")
    public String myProfile(HttpSession session) {
        if (!sessionService.isAuthenticated(session)) {
            return "redirect:/login";
        }

        User currentUser = sessionService.getCurrentUser(session);
        return "redirect:/users/" + currentUser.getId();
    }

    @PostMapping("/follow")
    public String followUser(@RequestParam Long followingId,
                             @RequestParam String followingUsername,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        if (!sessionService.isAuthenticated(session)) {
            return "redirect:/login";
        }

        try {
            User currentUser = sessionService.getCurrentUser(session);
            apiClientService.subscribe(currentUser.getId(), currentUser.getUsername(),
                    followingId, followingUsername);
            redirectAttributes.addFlashAttribute("success", "Вы подписались на " + followingUsername);
        } catch (Exception e) {
            log.error("Error following user: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Ошибка при подписке: " + e.getMessage());
        }

        return "redirect:/users/" + followingId;
    }

    @PostMapping("/unfollow")
    public String unfollowUser(@RequestParam Long followingId,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        if (!sessionService.isAuthenticated(session)) {
            return "redirect:/login";
        }

        try {
            User currentUser = sessionService.getCurrentUser(session);
            apiClientService.unsubscribe(currentUser.getId(), followingId);
            redirectAttributes.addFlashAttribute("success", "Вы отписались");
        } catch (Exception e) {
            log.error("Error unfollowing user: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Ошибка при отписке");
        }

        return "redirect:/users/" + followingId;
    }
}