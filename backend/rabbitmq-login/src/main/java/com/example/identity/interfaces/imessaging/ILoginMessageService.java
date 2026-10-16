package com.example.identity.interfaces.imessaging;

import com.example.identity.presentation.record.LoginRequestMessage;
import com.example.identity.presentation.record.LoginResponseMessage;

/**
 * Interface for login message processing service.
 * Handles sending and receiving login requests via RabbitMQ.
 */
public interface ILoginMessageService {
    
    /**
     * Sends a login request to RabbitMQ for asynchronous processing.
     * 
     * @param email The user's email
     * @param password The user's password
     * @return The correlation ID for tracking the request
     */
    String sendLoginRequest(String email, String password);
    
    /**
     * Sends a login request with additional context.
     * 
     * @param email The user's email
     * @param password The user's password
     * @param ipAddress The client IP address
     * @param userAgent The client user agent
     * @return The correlation ID
     */
    String sendLoginRequest(String email, String password, String ipAddress, String userAgent);
    
    /**
     * Sends a login request with priority.
     * 
     * @param email The user's email
     * @param password The user's password
     * @param priority The priority (0-10, where 10 is highest)
     * @return The correlation ID
     */
    String sendLoginRequestWithPriority(String email, String password, int priority);
    
    /**
     * Sends a login request with full message.
     * 
     * @param message The login request message
     * @return The correlation ID
     */
    String sendLoginRequest(LoginRequestMessage message);
}
