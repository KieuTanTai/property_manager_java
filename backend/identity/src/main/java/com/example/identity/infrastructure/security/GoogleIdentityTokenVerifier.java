package com.example.identity.infrastructure.security;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.JwtValidationException;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.stream.Collectors;

@Component
public class GoogleIdentityTokenVerifier {
    private static final Logger logger = LoggerFactory.getLogger(GoogleIdentityTokenVerifier.class);
    private final JwtDecoder googleJwtDecoder;

    public GoogleIdentityTokenVerifier(JwtDecoder googleJwtDecoder) {
        this.googleJwtDecoder = googleJwtDecoder;
    }

    public VerifiedGoogleIdentity verify(String credential, String requestedEmail) {
        final Jwt token;
        try {
            token = googleJwtDecoder.decode(credential);
        } catch (JwtValidationException exception) {
            String details = exception.getErrors().stream()
                    .map(error -> error.getDescription() == null ? error.getErrorCode() : error.getDescription())
                    .collect(Collectors.joining("; "));
            logger.warn("Google ID token validation failed: {}", details);
            throw new IllegalArgumentException(details.isBlank()
                    ? "Google ID token validation failed. Check backend Google Client ID configuration."
                    : "Google ID token rejected: " + details);
        } catch (JwtException exception) {
            logger.warn("Google ID token validation failed: {}", exception.getMessage());
            throw new IllegalArgumentException("Google ID token is invalid, expired, or not issued for this app.");
        }

        String tokenEmail = token.getClaimAsString("email");
        Boolean emailVerified = token.getClaim("email_verified");
        if (token.getSubject() == null || token.getSubject().isBlank()
                || tokenEmail == null || tokenEmail.isBlank()
                || !Boolean.TRUE.equals(emailVerified)) {
            throw new IllegalArgumentException("Google account must provide a verified email address.");
        }
        if (!tokenEmail.trim().equalsIgnoreCase(requestedEmail.trim())) {
            throw new IllegalArgumentException("Email does not match the Google ID token.");
        }
        return new VerifiedGoogleIdentity(
                tokenEmail.trim().toLowerCase(java.util.Locale.ROOT), token.getSubject());
    }

    public record VerifiedGoogleIdentity(String email, String subject) {
    }
}
