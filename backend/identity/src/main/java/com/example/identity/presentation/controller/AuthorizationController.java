package com.example.identity.presentation.controller;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import com.example.identity.interfaces.iapplication.IAuthorizationApplication;
import com.example.identity.models.permission.PermissionModel;
import com.example.identity.models.role.RoleModel;
import com.example.identity.presentation.response.PermissionResponse;
import com.example.identity.presentation.response.RoleWithPermissionsResponse;
import com.example.identity.presentation.record.AuthorizationRequests.*;

@RestController
@RequestMapping("/api/authorization")
public class AuthorizationController {
    private final IAuthorizationApplication application;

    public AuthorizationController(IAuthorizationApplication application) {
        this.application = application;
    }

    @GetMapping("/roles")
    public List<RoleWithPermissionsResponse> getRoles() {
        return application.getRoles();
    }

    @GetMapping("/roles/{roleCode}")
    public RoleWithPermissionsResponse getRole(@PathVariable String roleCode) {
        return application.getRoleWithPermissions(roleCode);
    }

    @GetMapping("/roles/{roleCode}/permissions")
    public RoleWithPermissionsResponse getRoleWithPermissions(@PathVariable String roleCode) {
        return application.getRoleWithPermissions(roleCode);
    }

    @GetMapping("/permissions")
    public List<PermissionResponse> getPermissions() {
        return application.getPermissions();
    }

    @GetMapping("/permissions/{permissionCode}")
    public PermissionResponse getPermission(@PathVariable String permissionCode) {
        return application.getPermissionByCode(permissionCode);
    }

    @PostMapping("/roles")
    public int addRole(@Valid @RequestBody RoleChangeRequest request) {
        return application.addRole(toRole(null, request), request.permissionIds());
    }

    @PostMapping("/accounts/{accountId}/roles")
    public int addAccountRoles(@PathVariable UUID accountId, @Valid @RequestBody IdListRequest request) {
        return application.addAccountRoles(accountId, request.ids());
    }

    @PutMapping("/roles/{roleId}")
    public int updateRole(@PathVariable UUID roleId, @Valid @RequestBody RoleChangeRequest request) {
        return application.updateRole(toRole(roleId, request), request.permissionIds());
    }

    @PutMapping("/accounts/{accountId}/roles")
    public int updateAccountRoles(@PathVariable UUID accountId, @Valid @RequestBody IdListRequest request) {
        return application.updateAccountRoles(accountId, request.ids());
    }

    @PostMapping("/permissions")
    public int addPermission(@Valid @RequestBody PermissionChangeRequest request) {
        return application.addPermission(toPermission(null, request));
    }

    @PostMapping("/accounts/{accountId}/permissions")
    public int addAccountPermissions(@PathVariable UUID accountId, @Valid @RequestBody IdListRequest request) {
        return application.addAccountPermissions(accountId, request.ids());
    }

    @PutMapping("/permissions/{permissionId}")
    public int updatePermission(@PathVariable UUID permissionId, @Valid @RequestBody PermissionChangeRequest request) {
        return application.updatePermission(toPermission(permissionId, request));
    }

    @PutMapping("/accounts/{accountId}/permissions")
    public int updateAccountPermissions(@PathVariable UUID accountId,
            @Valid @RequestBody IdListRequest request) {
        return application.updateAccountPermissions(accountId, request.ids());
    }

    private RoleModel toRole(UUID id, RoleChangeRequest request) {
        return RoleModel.builder().roleId(id).roleCode(normalizeCode(request.code(), request.name(), "role", id))
                .roleName(request.name()).roleDescription(request.description())
                .roleIsActive(request.active()).build();
    }

    private PermissionModel toPermission(UUID id, PermissionChangeRequest request) {
        return PermissionModel.builder().permissionId(id)
                .permissionCode(normalizeCode(request.code(), request.name(), "permission", id))
                .permissionName(request.name()).permissionDescription(request.description())
                .permissionIsActive(request.active()).build();
    }

    private String normalizeCode(String code, String name, String prefix, UUID id) {
        if (code != null && !code.isBlank()) {
            return code.trim().toLowerCase(Locale.ROOT);
        }
        if (id != null) {
            return null;
        }
        String generated = name.trim().toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "_")
                .replaceAll("^_+|_+$", "");
        if (generated.isBlank()) {
            generated = prefix + "_" + UUID.randomUUID().toString().replace("-", "");
        }
        return generated.substring(0, Math.min(50, generated.length()));
    }
}
