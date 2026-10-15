package com.example.identity.application;

import java.util.List;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.identity.interfaces.iapplication.IAuthorizationApplication;
import com.example.identity.interfaces.irepository.AccountAdditionalPermissionRepository;
import com.example.identity.interfaces.irepository.AccountRoleRepository;
import com.example.identity.interfaces.irepository.PermissionRepository;
import com.example.identity.interfaces.irepository.RolePermissionRepository;
import com.example.identity.interfaces.irepository.RoleRepository;
import com.example.identity.models.account.AccountAdditionalPermissionModel;
import com.example.identity.models.account.AccountRoleModel;
import com.example.identity.models.permission.PermissionModel;
import com.example.identity.models.role.RoleModel;
import com.example.identity.models.role.RolePermissionModel;
import com.example.identity.utils.Enum.ESystemPermissionCode;
import com.example.identity.utils.Enum.ESystemRoleCode;
import com.example.identity.presentation.mapper.IdentityResponseMapper;
import com.example.identity.presentation.response.PermissionResponse;
import com.example.identity.presentation.response.RoleResponse;
import com.example.identity.presentation.response.RoleWithPermissionsResponse;
import com.example.shared.uuid.UuidV7;

@Service
public class AuthorizationService implements IAuthorizationApplication {
    private final RoleRepository roles;
    private final PermissionRepository permissions;
    private final RolePermissionRepository rolePermissions;
    private final AccountRoleRepository accountRoles;
    private final AccountAdditionalPermissionRepository accountPermissions;
    private final IdentityResponseMapper mapper;

    public AuthorizationService(RoleRepository roles, PermissionRepository permissions,
            RolePermissionRepository rolePermissions, AccountRoleRepository accountRoles,
            AccountAdditionalPermissionRepository accountPermissions,
            IdentityResponseMapper mapper) {
        this.roles = roles;
        this.permissions = permissions;
        this.rolePermissions = rolePermissions;
        this.accountRoles = accountRoles;
        this.accountPermissions = accountPermissions;
        this.mapper = mapper;
    }

    public RoleModel getBaseRole() {
        return roles.findByCode(ESystemRoleCode.Customer.name().toLowerCase())
                .orElseThrow(() -> new IllegalArgumentException("Role not found."));
    }

    @Transactional(readOnly = true)
    public RoleWithPermissionsResponse getRoleWithPermissions(ESystemRoleCode code) {
        requireValue(code, "Role code is required.");
        return getRoleWithPermissions(code.name());
    }

    @Transactional(readOnly = true)
    public RoleWithPermissionsResponse getRoleWithPermissions(String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Role code is required.");
        }
        RoleModel role = roles.findByCode(code.trim().toLowerCase())
                .orElseThrow(() -> new IllegalArgumentException("Role not found."));
        return mapper.roleWithPermissions(role);
    }

    public RoleResponse getRoleByCode(String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Role code is required.");
        }
        return mapper.role(roles.findByCode(code.trim().toLowerCase())
                .orElseThrow(() -> new IllegalArgumentException("Role not found.")));
    }

    @Transactional(readOnly = true)
    public List<RoleWithPermissionsResponse> getRoles() {
        return roles.findAll().stream().map(mapper::roleWithPermissions).toList();
    }

    public PermissionResponse getPermissionByCode(ESystemPermissionCode code) {
        requireValue(code, "Permission code is required.");
        return mapper.permission(permissions.findByCode(code.name().toLowerCase())
                .orElseThrow(() -> new IllegalArgumentException("Permission not found.")));
    }

    public PermissionResponse getPermissionByCode(String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Permission code is required.");
        }
        return mapper.permission(permissions.findByCode(code.trim().toLowerCase())
                .orElseThrow(() -> new IllegalArgumentException("Permission not found.")));
    }

    @Transactional(readOnly = true)
    public List<PermissionResponse> getPermissions() {
        return permissions.findAll().stream().map(mapper::permission).toList();
    }

    @Transactional
    public int addRole(RoleModel role, List<UUID> permissionIds) {
        validateRole(role, false);
        requireIds(permissionIds, "Permission ids are required.");
        ensureId(role);
        roles.save(role);
        return 1 + addRoleAssociations(role.getRoleId(), permissionIds);
    }

    @Transactional
    public int addAccountRoles(UUID accountId, List<UUID> roleIds) {
        requireIds(roleIds, "Role ids are required.");
        requireId(accountId, "Account id is required.");
        accountRoles.addAll(roleIds.stream()
                .map(roleId -> AccountRoleModel.builder()
                        .id(new AccountRoleModel.AccountRoleModelId(accountId, roleId))
                        .build())
                .toList());
        return roleIds.size();
    }

    @Transactional
    public int updateRole(RoleModel role) {
        validateRole(role, true);
        RoleModel existing = roles.findById(role.getRoleId())
                .orElseThrow(() -> new IllegalArgumentException("Role not found."));
        if (role.getRoleCode() == null || role.getRoleCode().isBlank()) {
            role.setRoleCode(existing.getRoleCode());
        }
        roles.save(role);
        return 1;
    }

    @Transactional
    public int updateRole(RoleModel role, List<UUID> permissionIds) {
        requireIds(permissionIds, "Permission ids are required.");
        int affected = updateRole(role);
        List<RolePermissionModel> existing = rolePermissions.findByFirstId(role.getRoleId());
        Set<UUID> existingIds = existing.stream()
                .map(association -> association.getId().getPermissionId())
                .collect(java.util.stream.Collectors.toSet());
        Set<UUID> desiredIds = new HashSet<>(permissionIds);
        int removed = 0;
        for (RolePermissionModel association : existing) {
            UUID permissionId = association.getId().getPermissionId();
            if (!desiredIds.contains(permissionId)) {
                rolePermissions.deleteByIds(role.getRoleId(), permissionId);
                removed++;
            }
        }
        List<RolePermissionModel> additions = desiredIds.stream()
                .filter(permissionId -> !existingIds.contains(permissionId))
                .map(permissionId -> RolePermissionModel.builder()
                        .id(new RolePermissionModel.RolePermissionModelId(role.getRoleId(), permissionId))
                        .build())
                .toList();
        if (!additions.isEmpty()) {
            rolePermissions.addAll(additions);
        }
        return affected + removed + additions.size();
    }

    @Transactional
    public int updateAccountRoles(UUID accountId, List<UUID> roleIds) {
        requireIds(roleIds, "Role ids are required.");
        requireId(accountId, "Account id is required.");
        List<AccountRoleModel> existing = accountRoles.findByFirstId(accountId);
        Set<UUID> existingIds = existing.stream()
                .map(association -> association.getId().getRoleId())
                .collect(java.util.stream.Collectors.toSet());
        Set<UUID> desiredIds = new HashSet<>(roleIds);
        int removed = 0;
        for (AccountRoleModel association : existing) {
            UUID roleId = association.getId().getRoleId();
            if (!desiredIds.contains(roleId)) {
                accountRoles.deleteByIds(accountId, roleId);
                removed++;
            }
        }
        List<AccountRoleModel> additions = desiredIds.stream()
                .filter(roleId -> !existingIds.contains(roleId))
                .map(roleId -> AccountRoleModel.builder()
                        .id(new AccountRoleModel.AccountRoleModelId(accountId, roleId))
                        .build())
                .toList();
        if (!additions.isEmpty()) {
            accountRoles.addAll(additions);
        }
        return removed + additions.size();
    }

    @Transactional
    public int addPermission(PermissionModel permission) {
        validatePermission(permission, false);
        ensureId(permission);
        permissions.save(permission);
        return 1;
    }

    @Transactional
    public int addAccountPermissions(UUID accountId, List<UUID> permissionIds) {
        requireIds(permissionIds, "Permission ids are required.");
        requireId(accountId, "Account id is required.");
        accountPermissions.addAll(permissionIds.stream()
                .map(permissionId -> AccountAdditionalPermissionModel.builder()
                        .id(new AccountAdditionalPermissionModel.AccountAdditionalPermissionModelId(accountId, permissionId))
                        .build())
                .toList());
        return permissionIds.size();
    }

    @Transactional
    public int updatePermission(PermissionModel permission) {
        validatePermission(permission, true);
        PermissionModel existing = permissions.findById(permission.getPermissionId())
                .orElseThrow(() -> new IllegalArgumentException("Permission not found."));
        if (permission.getPermissionCode() == null || permission.getPermissionCode().isBlank()) {
            permission.setPermissionCode(existing.getPermissionCode());
        }
        permissions.save(permission);
        return 1;
    }

    @Transactional
    public int updateAccountPermissions(UUID accountId, List<UUID> permissionIds) {
        requireIds(permissionIds, "Permission ids are required.");
        requireId(accountId, "Account id is required.");
        List<AccountAdditionalPermissionModel> existing = accountPermissions.findByFirstId(accountId);
        Set<UUID> existingIds = existing.stream()
                .map(association -> association.getId().getPermissionId())
                .collect(java.util.stream.Collectors.toSet());
        Set<UUID> desiredIds = new HashSet<>(permissionIds);
        int removed = 0;
        for (AccountAdditionalPermissionModel association : existing) {
            UUID permissionId = association.getId().getPermissionId();
            if (!desiredIds.contains(permissionId)) {
                accountPermissions.deleteByIds(accountId, permissionId);
                removed++;
            }
        }
        List<AccountAdditionalPermissionModel> additions = desiredIds.stream()
                .filter(permissionId -> !existingIds.contains(permissionId))
                .map(permissionId -> AccountAdditionalPermissionModel.builder()
                        .id(new AccountAdditionalPermissionModel.AccountAdditionalPermissionModelId(accountId, permissionId))
                        .build())
                .toList();
        if (!additions.isEmpty()) {
            accountPermissions.addAll(additions);
        }
        return removed + additions.size();
    }

    private int addRoleAssociations(UUID roleId, List<UUID> permissionIds) {
        rolePermissions.addAll(permissionIds.stream()
                .map(permissionId -> RolePermissionModel.builder()
                        .id(new RolePermissionModel.RolePermissionModelId(roleId, permissionId))
                        .build())
                .toList());
        return permissionIds.size();
    }

    private void ensureId(RoleModel role) {
        if (role.getRoleId() == null) {
            role.setRoleId(UuidV7.generate());
        }
    }

    private void ensureId(PermissionModel permission) {
        if (permission.getPermissionId() == null) {
            permission.setPermissionId(UuidV7.generate());
        }
    }

    private void requireId(UUID id, String message) {
        if (id == null || id.equals(new UUID(0L, 0L))) {
            throw new IllegalArgumentException(message);
        }
    }

    private void requireIds(List<UUID> ids, String message) {
        if (ids == null || ids.isEmpty() || ids.stream().anyMatch(id -> id == null || id.equals(new UUID(0L, 0L)))) {
            throw new IllegalArgumentException(message);
        }
        if (ids.stream().distinct().count() != ids.size()) {
            throw new IllegalArgumentException("Ids must be unique.");
        }
    }

    private void validateRole(RoleModel role, boolean requireId) {
        requireValue(role, "Role is required.");
        if (requireId) {
            requireId(role.getRoleId(), "Role id is required.");
        }
        if (role.getRoleName() == null || role.getRoleName().isBlank() || role.getRoleName().length() > 150) {
            throw new IllegalArgumentException("Role name is required and must not exceed 150 characters.");
        }
        if (role.getRoleDescription() != null && role.getRoleDescription().length() > 300) {
            throw new IllegalArgumentException("Role description must not exceed 300 characters.");
        }
        if ((!requireId && (role.getRoleCode() == null || role.getRoleCode().isBlank()))
                || (role.getRoleCode() != null && role.getRoleCode().length() > 50)) {
            throw new IllegalArgumentException("Role code is required and must not exceed 50 characters.");
        }
    }

    private void validatePermission(PermissionModel permission, boolean requireId) {
        requireValue(permission, "Permission is required.");
        if (requireId) {
            requireId(permission.getPermissionId(), "Permission id is required.");
        }
        if (permission.getPermissionName() == null || permission.getPermissionName().isBlank()
                || permission.getPermissionName().length() > 150) {
            throw new IllegalArgumentException("Permission name is required and must not exceed 150 characters.");
        }
        if (permission.getPermissionDescription() != null && permission.getPermissionDescription().length() > 300) {
            throw new IllegalArgumentException("Permission description must not exceed 300 characters.");
        }
        if ((!requireId && (permission.getPermissionCode() == null || permission.getPermissionCode().isBlank()))
                || (permission.getPermissionCode() != null && permission.getPermissionCode().length() > 50)) {
            throw new IllegalArgumentException("Permission code is required and must not exceed 50 characters.");
        }
    }

    private <T> void requireValue(T value, String message) {
        if (value == null) {
            throw new IllegalArgumentException(message);
        }
    }
}
