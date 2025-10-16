package com.example.apigateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.io.Decoders;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.GatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import java.security.Key;

@Slf4j
@Component
@RequiredArgsConstructor
public class AddAuthUserIdHeaderFilter implements GatewayFilterFactory<AddAuthUserIdHeaderFilter.Config> {

    @Value("${jwt.secret}")
    private String jwtSecret;

    // JWT secret Base64 → Key
    private Key getSignKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.warn("Missing or invalid Authorization header");
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }

            String token = authHeader.substring(7);

            try {
                Claims claims = Jwts.parserBuilder()
                        .setSigningKey(getSignKey())
                        .build()
                        .parseClaimsJws(token)
                        .getBody();

                Object userIdObj = claims.get("userId");
                if (userIdObj == null) {
                    log.warn("JWT token does not contain 'userId' claim");
                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                    return exchange.getResponse().setComplete();
                }

                Long authUserId = Long.valueOf(String.valueOf(userIdObj));

                // Header əlavə et
                ServerWebExchange mutatedExchange = exchange.mutate()
                        .request(r -> r.headers(h -> h.set("X-Auth-User-Id", String.valueOf(authUserId))))
                        .build();

                log.info("X-Auth-User-Id set to {}", authUserId);
                return chain.filter(mutatedExchange);

            } catch (ExpiredJwtException e) {
                log.warn("JWT token expired: {}", e.getMessage());
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            } catch (Exception e) {
                log.error("Failed to parse JWT token: {}", e.getMessage());
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }
        };
    }

    @Override
    public Config newConfig() {
        return new Config();
    }

    @Override
    public Class<Config> getConfigClass() {
        return Config.class;
    }

    public static class Config {
        // gələcəkdə əlavə konfiq parametrlər üçün boş class
    }
}
