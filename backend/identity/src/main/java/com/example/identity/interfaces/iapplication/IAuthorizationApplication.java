package com.example.identity.interfaces.iapplication;

import java.util.List;

import com.example.identity.models.permission.PermissionModel;
import com.example.identity.models.role.RoleModel;

public interface IAuthorizationApplication {
    List<RoleModel> getRoles();
    List<PermissionModel> getPermissions();
    RoleModel saveRole(RoleModel role);
    PermissionModel savePermission(PermissionModel permission);
}
