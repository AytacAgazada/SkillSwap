package com.example.notificationservice.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Simple broker handles messages to /topic. Used for broadcasting to users.
        config.enableSimpleBroker("/topic");

        // Application destination prefixes for client to send messages (e.g., /app/hello)
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // The endpoint the client will connect to (e.g., ws://localhost:8080/ws)
        // .withSockJS() allows fallback options for older browsers
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*") // CORS configuration for simplicity
                .withSockJS();
    }
}