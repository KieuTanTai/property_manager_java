package com.example.identity.presentation.record;

import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public final class AuthorizationRequests {
    private AuthorizationRequests() {
    }

    public record IdListRequest(@NotEmpty List<@NotNull UUID> ids) {
    }

    public record RoleChangeRequest(
            @Size(max = 50) String code,
            @NotBlank @Size(max = 150) String name,
            @Size(max = 300)
            String description,
            @JsonAlias("isActive") boolean active,
            @NotEmpty List<@NotNull UUID> permissionIds) {
    }

    public record PermissionChangeRequest(
            @Size(max = 50) String code,
            @NotBlank @Size(max = 150) String name,
            @Size(max = 300)
            String description,
            @JsonAlias("isActive") boolean active) {
    }
}
