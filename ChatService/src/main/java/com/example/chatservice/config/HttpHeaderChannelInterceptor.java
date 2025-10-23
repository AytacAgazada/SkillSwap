package com.example.chatservice.config;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;

import java.util.LinkedList;
import java.util.Map;

public class HttpHeaderChannelInterceptor implements ChannelInterceptor {

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            Object raw = message.getHeaders().get(StompHeaderAccessor.NATIVE_HEADERS);

            if (raw instanceof Map) {
                Object res = ((Map) raw).get("X-Auth-User-Id");

                if (res instanceof LinkedList) {
                    accessor.getSessionAttributes().put("X-Auth-User-Id", ((LinkedList) res).get(0));
                }
            }
        }
        return message;
    }
}
