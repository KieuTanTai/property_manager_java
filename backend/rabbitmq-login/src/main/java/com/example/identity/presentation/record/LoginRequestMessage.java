package com.example.identity.presentation.record;

import java.io.Serializable;
import java.util.UUID;

import lombok.Builder;

/**
 * DTO for login requests sent via RabbitMQ.
 * This message is used to handle high concurrent login traffic asynchronously.
 */
@Builder
public record LoginRequestMessage(
        String requestId,
        String email,
        String password,
        String ipAddress,
        String userAgent,
        long timestamp,
        int priority
) implements Serializable {
    
    public static LoginRequestMessage fromAuthRequest(AuthRequest request) {
        return LoginRequestMessage.builder()
                .requestId(UUID.randomUUID().toString())
                .email(request.email())
                .password(request.password())
                .ipAddress(null)
                .userAgent(null)
                .timestamp(System.currentTimeMillis())
                .priority(5) // Default priority
                .build();
    }
    
    public static LoginRequestMessage fromAuthRequest(AuthRequest request, String ipAddress, String userAgent) {
        return LoginRequestMessage.builder()
                .requestId(UUID.randomUUID().toString())
                .email(request.email())
                .password(request.password())
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .timestamp(System.currentTimeMillis())
                .priority(5) // Default priority
                .build();
    }
}
