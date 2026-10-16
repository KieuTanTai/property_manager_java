package com.example.identity.presentation.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.identity.infrastructure.cache.LoginResultCache;
import com.example.identity.infrastructure.cache.LoginResultCache.LoginResult;
import com.example.identity.interfaces.imessaging.ILoginMessageService;
import com.example.identity.presentation.record.AuthRequest;

/**
 * Controller for handling asynchronous login requests via RabbitMQ.
 * This controller is designed to handle high concurrent login traffic
 * by queuing login requests and processing them asynchronously.
 */
@RestController
@RequestMapping("/api/async-account")
public class AsyncLoginController {

    private static final String NAME_IDENTIFIER_CLAIM =
            "http://schemas.xmlsoap.org/ws/2005/05/identity/claims/nameidentifier";
    private static final String EMAIL_CLAIM =
            "http://schemas.xmlsoap.org/ws/2005/05/identity/claims/emailaddress";
    private static final String ROLE_CLAIM =
            "http://schemas.microsoft.com/ws/2008/06/identity/claims/role";

    private final ILoginMessageService loginMessageService;
    private final LoginResultCache resultCache;
    private final HttpServletRequest request;

    public AsyncLoginController(ILoginMessageService loginMessageService,
                               LoginResultCache resultCache,
                               HttpServletRequest request) {
        this.loginMessageService = loginMessageService;
        this.resultCache = resultCache;
        this.request = request;
    }

    /**
     * Async login endpoint that queues the login request via RabbitMQ.
     * Returns immediately with a request ID for tracking.
     * 
     * @param authRequest The login credentials
     * @return Response with request ID for tracking
     */
    @PostMapping("/login")
    public ResponseEntity<AsyncLoginResponse> asyncLogin(
            @Valid @RequestBody AuthRequest authRequest) {
        
        String ipAddress = getClientIpAddress();
        String userAgent = request.getHeader("User-Agent");
        
        String requestId = loginMessageService.sendLoginRequest(
                authRequest.email(), 
                authRequest.password(), 
                ipAddress, 
                userAgent
        );
        
        return ResponseEntity.accepted()
                .body(new AsyncLoginResponse(requestId, "Login request queued successfully"));
    }

    /**
     * Async login with priority.
     * Higher priority requests will be processed first.
     * 
     * @param authRequest The login credentials
     * @param priority The priority (0-10, where 10 is highest)
     * @return Response with request ID for tracking
     */
    @PostMapping("/login-priority")
    public ResponseEntity<AsyncLoginResponse> asyncLoginWithPriority(
            @Valid @RequestBody AuthRequest authRequest,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "5") int priority) {
        
        priority = Math.clamp(priority, 0, 10); // Clamp to 0-10
        
        String ipAddress = getClientIpAddress();
        String userAgent = request.getHeader("User-Agent");
        
        String requestId = loginMessageService.sendLoginRequestWithPriority(
                authRequest.email(), 
                authRequest.password(), 
                priority
        );
        
        return ResponseEntity.accepted()
                .body(new AsyncLoginResponse(requestId, "Priority login request queued"));
    }

    /**
     * Check the status of an async login request.
     * 
     * @param requestId The request ID to check
     * @return Status of the login request
     */
    @GetMapping("/login-status")
    public ResponseEntity<AsyncLoginStatus> checkLoginStatus(
            @org.springframework.web.bind.annotation.RequestParam String requestId) {
        
        LoginResult result = resultCache.getResult(requestId);
        
        if (result == null) {
            return ResponseEntity.ok(
                    new AsyncLoginStatus(
                            requestId,
                            "PROCESSING",
                            null,
                            null
                    )
            );
        }
        
        if (result.isSuccess()) {
            return ResponseEntity.ok(
                    new AsyncLoginStatus(
                            requestId,
                            "COMPLETED",
                            "Login successful",
                            null
                    )
            );
        } else {
            return ResponseEntity.ok(
                    new AsyncLoginStatus(
                            requestId,
                            "FAILED",
                            null,
                            result.errorMessage()
                    )
            );
        }
    }

    private String getClientIpAddress() {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    public record AsyncLoginResponse(String requestId, String message) {}

    public record AsyncLoginStatus(
            String requestId,
            String status, // PROCESSING, COMPLETED, FAILED
            String token,
            String errorMessage
    ) {}
}
