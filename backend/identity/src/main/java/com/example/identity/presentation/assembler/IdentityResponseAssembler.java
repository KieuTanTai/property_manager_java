package com.example.identity.presentation.assembler;

import java.util.List;
import java.util.Optional;

import com.example.identity.models.permission.PermissionModel;
import com.example.identity.models.role.RoleModel;
import org.springframework.stereotype.Component;

import com.example.identity.interfaces.irepository.AccountAdditionalPermissionRepository;
import com.example.identity.interfaces.irepository.AccountRoleRepository;
import com.example.identity.interfaces.irepository.PermissionRepository;
import com.example.identity.interfaces.irepository.RoleRepository;
import com.example.identity.models.account.AccountModel;
import com.example.identity.presentation.mapper.IdentityResponseMapper;
import com.example.identity.presentation.response.AccountResponse;

@Component
public class IdentityResponseAssembler {
    private final AccountRoleRepository accountRoles;
    private final AccountAdditionalPermissionRepository accountPermissions;
    private final RoleRepository roles;
    private final PermissionRepository permissions;
    private final IdentityResponseMapper mapper;

    public IdentityResponseAssembler(AccountRoleRepository accountRoles,
            AccountAdditionalPermissionRepository accountPermissions,
            RoleRepository roles, PermissionRepository permissions,
            IdentityResponseMapper mapper) {
        this.accountRoles = accountRoles;
        this.accountPermissions = accountPermissions;
        this.roles = roles;
        this.permissions = permissions;
        this.mapper = mapper;
    }

    public AccountResponse account(AccountModel account) {
        List<String> roleCodes = accountRoles.findByFirstId(account.getAccountId()).stream()
                .map(association -> roles.findById(association.getId().getRoleId()))
                .flatMap(Optional::stream)
                .map(RoleModel::getRoleCode)
                .toList();
        List<String> permissionCodes = accountPermissions.findByFirstId(account.getAccountId()).stream()
                .map(association -> permissions.findById(association.getId().getPermissionId()))
                .flatMap(Optional::stream)
                .map(PermissionModel::getPermissionCode)
                .toList();
        return mapper.account(account, roleCodes, permissionCodes);
    }
}
