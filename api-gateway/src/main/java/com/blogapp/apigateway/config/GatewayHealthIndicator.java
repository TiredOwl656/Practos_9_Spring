package com.blogapp.apigateway.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class GatewayHealthIndicator implements HealthIndicator {

    private final RouteLocator routeLocator;

    @Override
    public Health health() {
        try {
            // Проверяем доступность маршрутов
            List<String> routes = routeLocator.getRoutes()
                    .map(route -> route.getId())
                    .collectList()
                    .block();

            return Health.up()
                    .withDetail("gateway", "Spring Cloud Gateway")
                    .withDetail("status", "Running")
                    .withDetail("routes", routes != null ? routes.size() : 0)
                    .build();

        } catch (Exception e) {
            return Health.down()
                    .withDetail("gateway", "Spring Cloud Gateway")
                    .withDetail("status", "Not healthy")
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}