package com.example.identity.presentation.record;

import java.util.List;
import java.util.UUID;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import com.example.identity.models.account.AccountAdditionalPermissionModel;
import com.example.identity.models.account.AccountModel;
import com.example.identity.models.account.AccountRoleModel;
import com.example.identity.models.profile.UserProfileModel;

public final class AccountRequests {
    private AccountRequests() {
    }

    public record Inactive(
            @Email @NotBlank String email,
            @NotBlank String password,
            @NotBlank String confirmPassword) {
    }

    public record Logout(@Email @NotBlank String email) {
    }

    public record Import(List<AccountModel> accounts, List<AccountRoleModel> accountRoles,
            List<UserProfileModel> profiles, List<AccountAdditionalPermissionModel> additionalPermissions) {
    }

    public record UpdateRange(List<AccountModel> accounts) {
    }
}
