package com.example.apigateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys; // Yeni import
import io.jsonwebtoken.io.Decoders; // Yeni import
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.GatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import java.security.Key; // Yeni import

@Slf4j
@Component
@RequiredArgsConstructor
public class AddAuthUserIdHeaderFilter implements GatewayFilterFactory<AddAuthUserIdHeaderFilter.Config> {

    @Value("${jwt.secret}")
    private String jwtSecret;

    // Secret key-i Base64 stringindən Key obyektinə çevirən metod
    // Bu metod Auth Service-dəki JwtService-dəki getSignKey() metoduna bənzəməlidir.
    private Key getSignKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                try {
                    String token = authHeader.substring(7);

                    // JWT-ni parse etmək üçün YENİ SYNTAX
                    Claims claims = Jwts.parserBuilder()
                            .setSigningKey(getSignKey()) // Yeni metod çağırışı
                            .build() // Builder-i build etmək lazımdır
                            .parseClaimsJws(token)
                            .getBody();

                    // JWT payload-dan "userId" claim-ini Long olaraq oxuyun
                    // Auth Service-də token yaradarkən "userId" claim-ini əlavə etdiyinizdən əmin olun.
                    Long authUserId = claims.get("userId", Long.class);

                    if (authUserId != null) {
                        exchange = exchange.mutate()
                                .request(r -> r.headers(headers -> headers.set("X-Auth-User-Id", String.valueOf(authUserId)))) // Long-u String-ə çevirin
                                .build();
                        log.info("X-Auth-User-Id header set to: {}", authUserId);
                    } else {
                        log.warn("JWT 'userId' claim is null or not found. X-Auth-User-Id header not set.");
                        // Əgər userId claim-i yoxdursa, sorğunu bloklamaq istəyə bilərsiniz.
                        // exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                        // return exchange.getResponse().setComplete();
                    }
                } catch (Exception e) {
                    log.warn("Failed to parse JWT in Gateway or extract userId claim: {}", e.getMessage());
                    // Bu xəta halında sorğunu bloklamaq üçün:
                    // exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                    // return exchange.getResponse().setComplete();
                }
            } else {
                log.debug("No Authorization header or not Bearer token. Skipping X-Auth-User-Id filter.");
            }
            return chain.filter(exchange);
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
    }
}
