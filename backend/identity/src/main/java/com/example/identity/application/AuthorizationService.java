package com.example.identity.application;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.identity.infrastructure.persistence.AccountAdditionalPermissionJpaRepository;
import com.example.identity.infrastructure.persistence.AccountRoleJpaRepository;
import com.example.identity.infrastructure.persistence.RolePermissionJpaRepository;
import com.example.identity.interfaces.iapplication.IAuthorizationApplication;
import com.example.identity.interfaces.irepository.PermissionRepository;
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
    private final RolePermissionJpaRepository rolePermissions;
    private final AccountRoleJpaRepository accountRoles;
    private final AccountAdditionalPermissionJpaRepository accountPermissions;
    private final IdentityResponseMapper mapper;

    public AuthorizationService(RoleRepository roles, PermissionRepository permissions,
            RolePermissionJpaRepository rolePermissions, AccountRoleJpaRepository accountRoles,
            AccountAdditionalPermissionJpaRepository accountPermissions,
            IdentityResponseMapper mapper) {
        this.roles = roles;
        this.permissions = permissions;
        this.rolePermissions = rolePermissions;
        this.accountRoles = accountRoles;
        this.accountPermissions = accountPermissions;
        this.mapper = mapper;
    }

    public RoleResponse getBaseRole() {
        RoleModel role = roles.findByCode(ESystemRoleCode.Customer.name().toLowerCase())
                .orElseThrow(() -> new IllegalArgumentException("Role not found."));
        return mapper.role(role);
    }

    public RoleWithPermissionsResponse getRoleWithPermissions(ESystemRoleCode code) {
        requireValue(code, "Role code is required.");
        RoleModel role = roles.findByCode(code.name().toLowerCase())
                .orElseThrow(() -> new IllegalArgumentException("Role not found."));
        List<PermissionResponse> permissionResponses = rolePermissions.findByIdRoleId(role.getRoleId()).stream()
                .map(association -> permissions.findById(association.getId().getPermissionId()))
                .flatMap(Optional::stream)
                .map(mapper::permission)
                .toList();
        return mapper.roleWithPermissions(role, permissionResponses);
    }

    public RoleResponse getRoleByCode(String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Role code is required.");
        }
        return mapper.role(roles.findByCode(code.trim().toLowerCase())
                .orElseThrow(() -> new IllegalArgumentException("Role not found.")));
    }

    @Transactional(readOnly = true)
    public List<RoleResponse> getRoles() {
        return roles.findAll().stream().map(mapper::role).toList();
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
        requireValue(role, "Role is required.");
        requireIds(permissionIds, "Permission ids are required.");
        ensureId(role);
        roles.save(role);
        return 1 + addRoleAssociations(role.getRoleId(), permissionIds);
    }

    @Transactional
    public int addAccountRoles(UUID accountId, List<UUID> roleIds) {
        requireIds(roleIds, "Role ids are required.");
        requireId(accountId, "Account id is required.");
        accountRoles.saveAll(roleIds.stream()
                .map(roleId -> AccountRoleModel.builder()
                        .id(new AccountRoleModel.AccountRoleModelId(accountId, roleId))
                        .build())
                .toList());
        return roleIds.size();
    }

    @Transactional
    public int updateRole(RoleModel role) {
        requireValue(role, "Role is required.");
        requireId(role.getRoleId(), "Role id is required.");
        roles.save(role);
        return 1;
    }

    @Transactional
    public int updateRole(RoleModel role, List<UUID> permissionIds) {
        requireIds(permissionIds, "Permission ids are required.");
        updateRole(role);
        rolePermissions.deleteAll(rolePermissions.findByIdRoleId(role.getRoleId()));
        return addRoleAssociations(role.getRoleId(), permissionIds);
    }

    @Transactional
    public int updateAccountRoles(UUID accountId, List<UUID> roleIds) {
        requireIds(roleIds, "Role ids are required.");
        requireId(accountId, "Account id is required.");
        accountRoles.deleteByIdAccountId(accountId);
        return addAccountRoles(accountId, roleIds);
    }

    @Transactional
    public int addPermission(PermissionModel permission) {
        requireValue(permission, "Permission is required.");
        ensureId(permission);
        permissions.save(permission);
        return 1;
    }

    @Transactional
    public int addAccountPermissions(UUID accountId, List<UUID> permissionIds) {
        requireIds(permissionIds, "Permission ids are required.");
        requireId(accountId, "Account id is required.");
        accountPermissions.saveAll(permissionIds.stream()
                .map(permissionId -> AccountAdditionalPermissionModel.builder()
                        .id(new AccountAdditionalPermissionModel.AccountAdditionalPermissionModelId(accountId, permissionId))
                        .build())
                .toList());
        return permissionIds.size();
    }

    @Transactional
    public int updatePermission(PermissionModel permission) {
        requireValue(permission, "Permission is required.");
        requireId(permission.getPermissionId(), "Permission id is required.");
        permissions.save(permission);
        return 1;
    }

    @Transactional
    public int updateAccountPermissions(UUID accountId, List<UUID> permissionIds) {
        requireIds(permissionIds, "Permission ids are required.");
        requireId(accountId, "Account id is required.");
        accountPermissions.deleteByIdAccountId(accountId);
        return addAccountPermissions(accountId, permissionIds);
    }

    public RoleResponse saveRole(RoleModel role) {
        requireValue(role, "Role is required.");
        ensureId(role);
        return mapper.role(roles.save(role));
    }

    public PermissionResponse savePermission(PermissionModel permission) {
        requireValue(permission, "Permission is required.");
        ensureId(permission);
        return mapper.permission(permissions.save(permission));
    }

    private int addRoleAssociations(UUID roleId, List<UUID> permissionIds) {
        rolePermissions.saveAll(permissionIds.stream()
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
        if (id == null) {
            throw new IllegalArgumentException(message);
        }
    }

    private void requireIds(List<UUID> ids, String message) {
        if (ids == null || ids.isEmpty() || ids.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException(message);
        }
    }

    private <T> void requireValue(T value, String message) {
        if (value == null) {
            throw new IllegalArgumentException(message);
        }
    }
}
