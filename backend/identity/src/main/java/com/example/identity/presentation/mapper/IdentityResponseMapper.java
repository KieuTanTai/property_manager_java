package com.example.identity.presentation.mapper;

import org.springframework.stereotype.Component;

import com.example.identity.models.account.AccountModel;
import com.example.identity.models.permission.PermissionModel;
import com.example.identity.models.profile.UserProfileModel;
import com.example.identity.models.role.RoleModel;
import com.example.identity.presentation.response.AccountResponse;
import com.example.identity.presentation.response.PermissionResponse;
import com.example.identity.presentation.response.ProfileResponse;
import com.example.identity.presentation.response.RoleResponse;
import com.example.identity.presentation.response.RoleWithPermissionsResponse;

import java.util.List;

@Component
public class IdentityResponseMapper {
    public AccountResponse account(AccountModel account, List<String> roleCodes,
            List<String> permissionCodes) {
        return new AccountResponse(
                account.getAccountId(),
                account.getAccountEmail(),
                Boolean.TRUE.equals(account.getAccountIsActive()),
                roleCodes,
                permissionCodes,
                account.getAccountCreatedAt(),
                account.getAccountUpdatedAt());
    }

    public ProfileResponse profile(UserProfileModel profile) {
        if (profile == null) {
            return null;
        }
        return new ProfileResponse(
                profile.getUserProfileId(),
                profile.getUserProfileAccountId(),
                profile.getUserProfileFirstName(),
                profile.getUserProfileLastName(),
                profile.getUserProfilePhoneNumber(),
                profile.getUserProfileAddress(),
                profile.getUserProfileAvatarUrl(),
                profile.getUserProfileDateOfBirth(),
                profile.getUserProfileGender());
    }

    public RoleResponse role(RoleModel role) {
        return new RoleResponse(
                role.getRoleId(),
                role.getRoleCode(),
                role.getRoleName(),
                role.getRoleDescription(),
                Boolean.TRUE.equals(role.getRoleIsActive()),
                role.getRoleCreatedAt(),
                role.getRoleUpdatedAt());
    }

    public RoleWithPermissionsResponse roleWithPermissions(RoleModel role,
            List<PermissionResponse> permissions) {
        return new RoleWithPermissionsResponse(
                role.getRoleId(),
                role.getRoleCode(),
                role.getRoleName(),
                role.getRoleDescription(),
                Boolean.TRUE.equals(role.getRoleIsActive()),
                permissions);
    }

    public PermissionResponse permission(PermissionModel permission) {
        return new PermissionResponse(
                permission.getPermissionId(),
                permission.getPermissionCode(),
                permission.getPermissionName(),
                permission.getPermissionDescription(),
                Boolean.TRUE.equals(permission.getPermissionIsActive()),
                permission.getPermissionCreatedAt(),
                permission.getPermissionUpdatedAt());
    }
}
