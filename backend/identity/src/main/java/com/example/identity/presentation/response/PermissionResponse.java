package com.example.identity.presentation.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record PermissionResponse(
        UUID permissionId,
        String code,
        String name,
        String description,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
