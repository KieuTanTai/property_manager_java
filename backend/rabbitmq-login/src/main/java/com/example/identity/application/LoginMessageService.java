package com.example.identity.application;

import org.springframework.stereotype.Service;

import com.example.identity.interfaces.imessaging.ILoginMessageService;
import com.example.identity.infrastructure.messaging.LoginMessageSender;
import com.example.identity.presentation.record.LoginRequestMessage;

/**
 * Implementation of the login message service.
 * Handles sending login requests to RabbitMQ for asynchronous processing.
 */
@Service
public class LoginMessageService implements ILoginMessageService {

    private final LoginMessageSender messageSender;

    public LoginMessageService(LoginMessageSender messageSender) {
        this.messageSender = messageSender;
    }

    @Override
    public String sendLoginRequest(String email, String password) {
        LoginRequestMessage message = LoginRequestMessage.builder()
                .requestId(java.util.UUID.randomUUID().toString())
                .email(email)
                .password(password)
                .ipAddress(null)
                .userAgent(null)
                .timestamp(System.currentTimeMillis())
                .priority(5)
                .build();
        return messageSender.sendLoginRequest(message);
    }

    @Override
    public String sendLoginRequest(String email, String password, String ipAddress, String userAgent) {
        LoginRequestMessage message = LoginRequestMessage.builder()
                .requestId(java.util.UUID.randomUUID().toString())
                .email(email)
                .password(password)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .timestamp(System.currentTimeMillis())
                .priority(5)
                .build();
        return messageSender.sendLoginRequest(message);
    }

    @Override
    public String sendLoginRequestWithPriority(String email, String password, int priority) {
        LoginRequestMessage message = LoginRequestMessage.builder()
                .requestId(java.util.UUID.randomUUID().toString())
                .email(email)
                .password(password)
                .ipAddress(null)
                .userAgent(null)
                .timestamp(System.currentTimeMillis())
                .priority(Math.clamp(priority, 0, 10))
                .build();
        return messageSender.sendLoginRequest(message);
    }

    @Override
    public String sendLoginRequest(LoginRequestMessage message) {
        return messageSender.sendLoginRequest(message);
    }
}
