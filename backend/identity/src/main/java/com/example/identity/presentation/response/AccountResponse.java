package com.example.identity.presentation.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record AccountResponse(
        UUID accountId,
        String email,
        boolean active,
        List<String> roleCodes,
        List<String> permissionCodes,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
