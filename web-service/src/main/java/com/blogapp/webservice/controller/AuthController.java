package com.blogapp.webservice.controller;

import com.blogapp.webservice.model.RegisterForm;
import com.blogapp.webservice.service.ApiClientService;
import com.blogapp.webservice.service.SessionService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j
@Controller
@RequiredArgsConstructor
public class AuthController {

    private final ApiClientService apiClientService;
    private final SessionService sessionService;

    @GetMapping("/login")
    public String loginForm(Model model, HttpSession session) {
        if (sessionService.isAuthenticated(session)) {
            return "redirect:/feed";
        }

        model.addAttribute("pageTitle", "Вход");
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerForm(Model model, HttpSession session) {
        if (sessionService.isAuthenticated(session)) {
            return "redirect:/feed";
        }

        model.addAttribute("registerForm", new RegisterForm());
        model.addAttribute("pageTitle", "Регистрация");
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute RegisterForm registerForm,
                           BindingResult result,
                           HttpSession session,
                           RedirectAttributes redirectAttributes) {
        if (sessionService.isAuthenticated(session)) {
            return "redirect:/feed";
        }

        if (result.hasErrors()) {
            return "auth/register";
        }

        if (!registerForm.getPassword().equals(registerForm.getConfirmPassword())) {
            result.rejectValue("confirmPassword", "error.registerForm", "Пароли не совпадают");
            return "auth/register";
        }

        try {
            apiClientService.register(registerForm);
            redirectAttributes.addFlashAttribute("success", "Регистрация успешна! Теперь вы можете войти.");
            return "redirect:/login";
        } catch (Exception e) {
            log.error("Registration error: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Ошибка регистрации: " + e.getMessage());
            return "redirect:/register";
        }
    }
}