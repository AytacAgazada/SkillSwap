package com.skillswap.apigateway.filter;

import com.skillswap.apigateway.config.JwtUtil;
import io.jsonwebtoken.Claims;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class AuthorizationFilter implements GatewayFilter {

    private final JwtUtil jwtUtil;

    public AuthorizationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        if (isAuthEndpoint(request)) {
            return chain.filter(exchange);
        }

        final String authHeader = request.getHeaders().getFirst("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return onError(exchange, HttpStatus.UNAUTHORIZED);
        }

        final String token = authHeader.substring(7);

        if (jwtUtil.isInvalid(token)) {
            return onError(exchange, HttpStatus.UNAUTHORIZED);
        }

        populateRequestWithHeaders(exchange, token);

        return chain.filter(exchange);
    }

    private boolean isAuthEndpoint(ServerHttpRequest request) {
        return request.getURI().getPath().contains("/api/v1/auth");
    }

    private Mono<Void> onError(ServerWebExchange exchange, HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        return response.setComplete();
    }

    private void populateRequestWithHeaders(ServerWebExchange exchange, String token) {
        Claims claims = jwtUtil.extractAllClaims(token);
        exchange.getRequest().mutate()
                .header("X-User-Email", claims.getSubject())
                .header("X-User-Roles", claims.get("roles").toString())
                .build();
    }
}
