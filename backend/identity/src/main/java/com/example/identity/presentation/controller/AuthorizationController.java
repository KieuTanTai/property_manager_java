package com.example.identity.presentation.controller;

import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import com.example.identity.interfaces.iapplication.IAuthorizationApplication;
import com.example.identity.models.permission.PermissionModel;
import com.example.identity.models.role.RoleModel;
import com.example.identity.presentation.response.PermissionResponse;
import com.example.identity.presentation.response.RoleResponse;
import com.example.identity.presentation.record.AuthorizationRequests.*;

@RestController
@RequestMapping("/api/authorization")
public class AuthorizationController {
    private final IAuthorizationApplication application;

    public AuthorizationController(IAuthorizationApplication application) {
        this.application = application;
    }

    @GetMapping("/roles")
    public List<RoleResponse> getRoles() {
        return application.getRoles();
    }

    @GetMapping("/roles/{roleCode}")
    public RoleResponse getRole(@PathVariable String roleCode) {
        return application.getRoleByCode(roleCode);
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
    public RoleResponse addRole(@Valid @RequestBody RoleChangeRequest request) {
        return application.saveRole(toRole(request));
    }

    @PostMapping("/accounts/{accountId}/roles")
    public int addAccountRoles(@PathVariable UUID accountId, @RequestBody IdListRequest request) {
        return application.addAccountRoles(accountId, request.ids());
    }

    @PutMapping("/roles/{roleId}")
    public int updateRole(@PathVariable UUID roleId, @Valid @RequestBody RoleChangeRequest request) {
        return application.updateRole(toRole(roleId, request), request.permissionIds());
    }

    @PutMapping("/accounts/{accountId}/roles")
    public int updateAccountRoles(@PathVariable UUID accountId, @RequestBody IdListRequest request) {
        return application.updateAccountRoles(accountId, request.ids());
    }

    @PostMapping("/permissions")
    public PermissionResponse addPermission(@Valid @RequestBody PermissionChangeRequest request) {
        return application.savePermission(toPermission(request));
    }

    @PostMapping("/accounts/{accountId}/permissions")
    public int addAccountPermissions(@PathVariable UUID accountId, @RequestBody IdListRequest request) {
        return application.addAccountPermissions(accountId, request.ids());
    }

    @PutMapping("/permissions/{permissionId}")
    public int updatePermission(@PathVariable UUID permissionId, @Valid @RequestBody PermissionChangeRequest request) {
        return application.updatePermission(toPermission(permissionId, request));
    }

    @PutMapping("/accounts/{accountId}/permissions")
    public int updateAccountPermissions(@PathVariable UUID accountId, @RequestBody IdListRequest request) {
        return application.updateAccountPermissions(accountId, request.ids());
    }

    private RoleModel toRole(RoleChangeRequest request) {
        return toRole(null, request);
    }

    private RoleModel toRole(UUID id, RoleChangeRequest request) {
        return RoleModel.builder().roleId(id).roleCode(request.code().trim().toLowerCase())
                .roleName(request.name()).roleDescription(request.description())
                .roleIsActive(request.active()).build();
    }

    private PermissionModel toPermission(PermissionChangeRequest request) {
        return toPermission(null, request);
    }

    private PermissionModel toPermission(UUID id, PermissionChangeRequest request) {
        return PermissionModel.builder().permissionId(id).permissionCode(request.code().trim().toLowerCase())
                .permissionName(request.name()).permissionDescription(request.description())
                .permissionIsActive(request.active()).build();
    }
}
