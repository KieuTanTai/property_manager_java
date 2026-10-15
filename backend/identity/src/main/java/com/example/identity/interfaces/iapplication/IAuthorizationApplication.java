package com.example.identity.interfaces.iapplication;

import java.util.List;

import com.example.identity.models.permission.PermissionModel;
import com.example.identity.models.role.RoleModel;
import com.example.identity.utils.Enum.ESystemRoleCode;
import com.example.identity.utils.Enum.ESystemPermissionCode;
import java.util.UUID;
import com.example.identity.presentation.response.PermissionResponse;
import com.example.identity.presentation.response.RoleResponse;
import com.example.identity.presentation.response.RoleWithPermissionsResponse;

public interface IAuthorizationApplication {
    RoleModel getBaseRole();
    RoleWithPermissionsResponse getRoleWithPermissions(ESystemRoleCode code);
    RoleWithPermissionsResponse getRoleWithPermissions(String code);
    RoleResponse getRoleByCode(String code);
    List<RoleWithPermissionsResponse> getRoles();
    PermissionResponse getPermissionByCode(ESystemPermissionCode code);
    PermissionResponse getPermissionByCode(String code);
    List<PermissionResponse> getPermissions();
    int addRole(RoleModel role, List<UUID> permissionIds);
    int addAccountRoles(UUID accountId, List<UUID> roleIds);
    int updateRole(RoleModel role);
    int updateRole(RoleModel role, List<UUID> permissionIds);
    int updateAccountRoles(UUID accountId, List<UUID> roleIds);
    int addPermission(PermissionModel permission);
    int addAccountPermissions(UUID accountId, List<UUID> permissionIds);
    int updatePermission(PermissionModel permission);
    int updateAccountPermissions(UUID accountId, List<UUID> permissionIds);
}
