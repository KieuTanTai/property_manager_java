package com.example.identity.presentation.response;

import java.util.List;
import java.util.UUID;

public record RoleWithPermissionsResponse(
        UUID roleId,
        String code,
        String name,
        String description,
        boolean active,
        List<PermissionResponse> permissions) {
}
