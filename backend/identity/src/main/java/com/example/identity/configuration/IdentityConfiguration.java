package com.example.identity.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "identity.account")
public record IdentityConfiguration(
        int minPasswordLength,
        int maxPasswordLength,
        boolean requireUppercase,
        boolean requireLowercase,
        boolean requireDigit,
        boolean requireSpecialCharacter) {
}
