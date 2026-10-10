package com.example.identity.application;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.identity.interfaces.iapplication.IAuthorizationApplication;
import com.example.identity.interfaces.irepository.PermissionRepository;
import com.example.identity.interfaces.irepository.RoleRepository;
import com.example.identity.models.permission.PermissionModel;
import com.example.identity.models.role.RoleModel;

@Service
public class AuthorizationService implements IAuthorizationApplication {
    private final RoleRepository roles;
    private final PermissionRepository permissions;

    public AuthorizationService(RoleRepository roles, PermissionRepository permissions) {
        this.roles = roles;
        this.permissions = permissions;
    }

    public List<RoleModel> getRoles() { return roles.findAll(); }
    public List<PermissionModel> getPermissions() { return permissions.findAll(); }
    public RoleModel saveRole(RoleModel role) { return roles.save(role); }
    public PermissionModel savePermission(PermissionModel permission) { return permissions.save(permission); }
}
