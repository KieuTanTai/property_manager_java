package com.example.identity.presentation.record;

import java.util.List;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;

public final class AuthorizationRequests {
    private AuthorizationRequests() {
    }

    public record IdListRequest(List<UUID> ids) {
    }

    public record RoleChangeRequest(
            @NotBlank String code,
            @NotBlank String name,
            String description,
            boolean active,
            List<UUID> permissionIds) {
    }

    public record PermissionChangeRequest(
            @NotBlank String code,
            @NotBlank String name,
            String description,
            boolean active) {
    }
}
