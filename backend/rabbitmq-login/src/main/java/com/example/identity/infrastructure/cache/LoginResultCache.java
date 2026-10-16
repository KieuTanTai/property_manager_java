package com.example.identity.infrastructure.cache;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.example.identity.presentation.response.LoginResponse;

/**
 * Cache for storing async login results.
 * Allows clients to poll for their login results using the request ID.
 */
@Component
public class LoginResultCache {

    private final ConcurrentHashMap<String, LoginResult> cache = new ConcurrentHashMap<>();
    
    @Value("${login.result.cache.ttl.seconds:300}") // 5 minutes default
    private long ttlSeconds;

    /**
     * Stores a login result in the cache.
     * 
     * @param requestId The request ID
     * @param loginResponse The login response
     */
    public void storeResult(String requestId, LoginResponse loginResponse) {
        cache.put(requestId, new LoginResult(loginResponse, System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(ttlSeconds)));
    }

    /**
     * Stores a login failure in the cache.
     * 
     * @param requestId The request ID
     * @param errorMessage The error message
     */
    public void storeFailure(String requestId, String errorMessage) {
        cache.put(requestId, new LoginResult(null, errorMessage, System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(ttlSeconds)));
    }

    /**
     * Retrieves a login result from the cache.
     * 
     * @param requestId The request ID
     * @return The login result, or null if not found or expired
     */
    public LoginResult getResult(String requestId) {
        LoginResult result = cache.get(requestId);
        if (result == null) {
            return null;
        }
        
        // Check if expired
        if (System.currentTimeMillis() > result.expirationTime()) {
            cache.remove(requestId);
            return null;
        }
        
        return result;
    }

    /**
     * Removes an expired or completed result from the cache.
     * 
     * @param requestId The request ID
     */
    public void removeResult(String requestId) {
        cache.remove(requestId);
    }

    /**
     * Cleans up expired entries from the cache.
     */
    public void cleanupExpired() {
        long now = System.currentTimeMillis();
        cache.entrySet().removeIf(entry -> now > entry.getValue().expirationTime());
    }

    /**
     * Result container for login operations.
     */
    public record LoginResult(LoginResponse response, String errorMessage, long expirationTime) {
        public LoginResult(LoginResponse response, long expirationTime) {
            this(response, null, expirationTime);
        }
        
        public LoginResult(String errorMessage, long expirationTime) {
            this(null, errorMessage, expirationTime);
        }
        
        public boolean isSuccess() {
            return response != null && errorMessage == null;
        }
        
        public boolean isFailure() {
            return errorMessage != null;
        }
    }
}
