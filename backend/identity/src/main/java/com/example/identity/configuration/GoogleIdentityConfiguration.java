package com.example.identity.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;

import java.util.Set;

@Configuration
public class GoogleIdentityConfiguration {
    @Bean
    JwtDecoder googleJwtDecoder(@Value("${identity.google.client-id:}") String clientId) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder
                .withJwkSetUri("https://www.googleapis.com/oauth2/v3/certs")
                .build();

        OAuth2TokenValidator<Jwt> issuerValidator = jwt -> {
            String issuer = jwt.getClaimAsString("iss");
            if (issuer != null && Set.of("https://accounts.google.com", "accounts.google.com").contains(issuer)) {
                return OAuth2TokenValidatorResult.success();
            }
            return OAuth2TokenValidatorResult.failure(new OAuth2Error(
                    "invalid_token", "Google ID token issuer is invalid.", null));
        };
        OAuth2TokenValidator<Jwt> audienceValidator = jwt -> {
            String authorizedParty = jwt.getClaimAsString("azp");
            boolean audienceMatches = !clientId.isBlank()
                    && jwt.getAudience() != null && jwt.getAudience().contains(clientId);
            boolean authorizedPartyMatches = authorizedParty == null || clientId.equals(authorizedParty);
            if (audienceMatches && authorizedPartyMatches) {
                return OAuth2TokenValidatorResult.success();
            }
            return OAuth2TokenValidatorResult.failure(new OAuth2Error(
                    "invalid_token", clientId.isBlank()
                            ? "VITE_GOOGLE_CLIENT_ID is not configured for the identity backend."
                            : "Google ID token audience does not match VITE_GOOGLE_CLIENT_ID.", null));
        };
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                JwtValidators.createDefault(), issuerValidator, audienceValidator));
        return decoder;
    }
}
