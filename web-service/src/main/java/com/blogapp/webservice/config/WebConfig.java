package com.blogapp.webservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // Настройка простых маршрутов без логики в контроллерах
        registry.addViewController("/").setViewName("home");
        registry.addViewController("/login").setViewName("auth/login");
    }
}