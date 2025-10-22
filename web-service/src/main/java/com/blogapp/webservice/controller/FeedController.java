package com.blogapp.webservice.controller;

import com.blogapp.webservice.service.ApiClientService;
import com.blogapp.webservice.service.SessionService;
import com.blogapp.webservice.dto.User;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Slf4j
@Controller
@RequiredArgsConstructor
public class FeedController {

    private final ApiClientService apiClientService;
    private final SessionService sessionService;

    @GetMapping("/feed")
    public String userFeed(Model model, HttpSession session) {
        if (!sessionService.isAuthenticated(session)) {
            return "redirect:/login";
        }

        try {
            User currentUser = sessionService.getCurrentUser(session);
            var feed = apiClientService.getUserFeed(currentUser.getId());

            model.addAttribute("feed", feed);
            model.addAttribute("currentUser", currentUser); // Добавляем currentUser
            model.addAttribute("pageTitle", "Моя лента");
            return "user/feed";
        } catch (Exception e) {
            log.error("Error getting user feed: {}", e.getMessage());
            model.addAttribute("error", "Ошибка при загрузке ленты");
            return "user/feed";
        }
    }
}