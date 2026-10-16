package com.example.identity.presentation.record;

import java.io.Serializable;

import com.example.identity.models.account.AccountModel;
import com.example.identity.presentation.response.AccountResponse;

/**
 * DTO for login responses from RabbitMQ processing.
 */
public record LoginResponseMessage(
        String requestId,
        String correlationId,
        AccountModel account,
        String token,
        long timestamp,
        boolean success,
        String errorMessage
) implements Serializable {
    
    public static LoginResponseMessage success(String requestId, String correlationId, AccountModel account, String token) {
        return new LoginResponseMessage(
                requestId,
                correlationId,
                account,
                token,
                System.currentTimeMillis(),
                true,
                null
        );
    }
    
    public static LoginResponseMessage failure(String requestId, String correlationId, String errorMessage) {
        return new LoginResponseMessage(
                requestId,
                correlationId,
                null,
                null,
                System.currentTimeMillis(),
                false,
                errorMessage
        );
    }
}
