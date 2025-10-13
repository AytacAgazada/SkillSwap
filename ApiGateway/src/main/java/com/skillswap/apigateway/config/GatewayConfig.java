package com.skillswap.apigateway.config;

import com.skillswap.apigateway.filter.AuthorizationFilter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    private final AuthorizationFilter filter;

    public GatewayConfig(AuthorizationFilter filter) {
        this.filter = filter;
    }

    @Bean
    public RouteLocator routes(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("auth-service", r -> r.path("/api/v1/auth/**")
                        .filters(f -> f.filter(filter))
                        .uri("lb://auth-service"))
                .route("other-services", r -> r.path("/api/**")
                        .filters(f -> f.filter(filter))
                        .uri("lb://other-services"))
                .build();
    }
}
