package com.example.identity.presentation.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import com.example.identity.interfaces.iapplication.IAuthorizationApplication;
import com.example.identity.models.permission.PermissionModel;
import com.example.identity.models.role.RoleModel;

@RestController
@RequestMapping("/api/authorization")
public class AuthorizationController {
    private final IAuthorizationApplication application;

    public AuthorizationController(IAuthorizationApplication application) {
        this.application = application;
    }

    @GetMapping("/roles")
    public List<RoleModel> getRoles() {
        return application.getRoles();
    }

    @GetMapping("/permissions")
    public List<PermissionModel> getPermissions() {
        return application.getPermissions();
    }

    @PostMapping("/roles")
    public RoleModel addRole(@RequestBody RoleModel role) {
        return application.saveRole(role);
    }

    @PostMapping("/permissions")
    public PermissionModel addPermission(@RequestBody PermissionModel permission) {
        return application.savePermission(permission);
    }
}
