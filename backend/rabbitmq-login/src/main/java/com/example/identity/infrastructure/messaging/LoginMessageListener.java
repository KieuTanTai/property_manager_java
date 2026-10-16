package com.example.identity.infrastructure.messaging;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import com.example.identity.configuration.RabbitMQConfiguration;
import com.example.identity.infrastructure.cache.LoginResultCache;
import com.example.identity.interfaces.iapplication.IAccountApplication;
import com.example.identity.models.account.AccountModel;
import com.example.identity.presentation.assembler.IdentityResponseAssembler;
import com.example.identity.presentation.record.LoginRequestMessage;
import com.example.identity.presentation.response.AccountResponse;
import com.example.identity.presentation.response.ClaimResponse;
import com.example.identity.presentation.response.LoginResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Listens for login request messages from RabbitMQ and processes them.
 * This component handles high concurrent login traffic by processing messages
 * asynchronously from the queue.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class LoginMessageListener {

    private final IAccountApplication accountService;
    private final IdentityResponseAssembler assembler;
    private final LoginMessageSender messageSender;
    private final LoginResultCache resultCache;
    
    // Thread pool for processing login requests
    private final ExecutorService executorService = Executors.newCachedThreadPool();
    
    // Maximum retry attempts before sending to dead letter queue
    private static final int MAX_RETRY_ATTEMPTS = 3;

    /**
     * Listens for login request messages on the login queue.
     * Processes each message asynchronously to handle high concurrent traffic.
     * 
     * @param message The login request message
     * @param correlationId The correlation ID for tracking
     * @param retryCount The number of retry attempts (from header)
     */
    @RabbitListener(
            queues = RabbitMQConfiguration.LOGIN_QUEUE,
            containerFactory = "rabbitListenerContainerFactory",
            concurrency = "${rabbitmq.login.concurrent.consumers:5}"
    )
    public CompletableFuture<Void> handleLoginRequest(
            @Payload LoginRequestMessage message,
            @Header(AmqpHeaders.CORRELATION_ID) String correlationId,
            @Header(value = "retry_count", defaultValue = "0") int retryCount) {
        
        return CompletableFuture.runAsync(() -> {
            try {
                log.info("Processing login request for email: {}, correlationId: {}", 
                        message.email(), correlationId);
                
                // Process the login
                AccountModel account = accountService.login(message.email(), message.password());
                
                log.info("Login successful for email: {}", message.email());
                
                // Store successful result in cache
                // Note: In a production environment, you might want to store the actual token
                // For now, we just mark it as successful
                resultCache.storeResult(correlationId, new LoginResponse(
                        assembler.account(account),
                        assembler.profile(account),
                        buildClaims(assembler.account(account))
                ));
                
            } catch (IllegalArgumentException e) {
                // Invalid credentials
                log.warn("Login failed for email: {} - Invalid credentials: {}", 
                        message.email(), e.getMessage());
                resultCache.storeFailure(correlationId, e.getMessage());
                handleLoginFailure(message, correlationId, retryCount, e.getMessage());
            } catch (IllegalStateException e) {
                // Account not active
                log.warn("Login failed for email: {} - Account not active: {}", 
                        message.email(), e.getMessage());
                resultCache.storeFailure(correlationId, e.getMessage());
                handleLoginFailure(message, correlationId, retryCount, e.getMessage());
            } catch (Exception e) {
                // Other errors - retry or send to dead letter
                log.error("Error processing login for email: {}", message.email(), e);
                resultCache.storeFailure(correlationId, "Internal server error: " + e.getMessage());
                handleLoginError(message, correlationId, retryCount, e);
            }
        }, executorService);
    }

    private List<ClaimResponse> buildClaims(AccountResponse account) {
        List<ClaimResponse> claims = new ArrayList<>();
        claims.add(new ClaimResponse(
                "http://schemas.xmlsoap.org/ws/2005/05/identity/claims/nameidentifier",
                account.accountId().toString()));
        claims.add(new ClaimResponse(
                "http://schemas.xmlsoap.org/ws/2005/05/identity/claims/emailaddress",
                account.email()));
        account.roleCodes().forEach(role -> claims.add(new ClaimResponse(
                "http://schemas.microsoft.com/ws/2008/06/identity/claims/role",
                role)));
        account.permissionCodes().forEach(permission ->
                claims.add(new ClaimResponse("AdditionalPermission", permission)));
        return claims;
    }

    /**
     * Handles login failure by retrying or sending to dead letter queue.
     */
    private void handleLoginFailure(LoginRequestMessage message, String correlationId, 
                                     int retryCount, String errorMessage) {
        if (retryCount < MAX_RETRY_ATTEMPTS) {
            log.info("Retrying login for email: {}, attempt: {}", 
                    message.email(), retryCount + 1);
            // Delay increases with each retry
            long delay = (retryCount + 1) * 1000L; // 1s, 2s, 3s
            messageSender.sendToRetryQueue(message, delay);
        } else {
            log.warn("Max retries exceeded for login email: {}, sending to dead letter", 
                    message.email());
            // Send to dead letter queue
            // In a real implementation, you might want to log this to a database
            // or send an alert
        }
    }

    /**
     * Handles unexpected errors during login processing.
     */
    private void handleLoginError(LoginRequestMessage message, String correlationId, 
                                   int retryCount, Exception exception) {
        if (retryCount < MAX_RETRY_ATTEMPTS) {
            log.info("Retrying login for email: {}, attempt: {}", 
                    message.email(), retryCount + 1);
            long delay = (retryCount + 1) * 2000L; // 2s, 4s, 6s
            messageSender.sendToRetryQueue(message, delay);
        } else {
            log.error("Max retries exceeded for login email: {}, error: {}", 
                    message.email(), exception.getMessage(), exception);
            // Send to dead letter queue
        }
    }

    /**
     * Listens for messages on the dead letter queue for error handling.
     */
    @RabbitListener(queues = RabbitMQConfiguration.LOGIN_DEAD_LETTER_QUEUE)
    public void handleDeadLetterMessage(@Payload LoginRequestMessage message, 
                                         @Header(AmqpHeaders.CORRELATION_ID) String correlationId) {
        log.error("Dead letter: Login request failed permanently - email: {}, correlationId: {}", 
                message.email(), correlationId);
        // Here you could:
        // 1. Log to database
        // 2. Send alert
        // 3. Notify monitoring system
    }
}
