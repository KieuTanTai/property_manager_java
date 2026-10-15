package com.example.identity.application;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.StreamSupport;

import com.example.identity.interfaces.irepository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.identity.interfaces.IAccountHelper;
import com.example.identity.interfaces.iapplication.IAccountApplication;
import com.example.identity.interfaces.iapplication.IAuthorizationApplication;
import com.example.identity.models.account.AccountAdditionalPermissionModel;
import com.example.identity.models.account.AccountModel;
import com.example.identity.models.account.AccountRoleModel;
import com.example.identity.models.profile.UserProfileModel;
import com.example.shared.persistence.record.RecordBaseCursorPage;
import com.example.shared.uuid.UuidV7;

@Service
public class AccountService implements IAccountApplication {
    private final AccountRepository accounts;
    private final IAccountHelper helper;
    private final AccountRoleRepository accountRoles;
    private final AccountAdditionalPermissionRepository accountPermissions;
    private final UserProfileRepository userProfiles;
    private final IAuthorizationApplication authorization;

    public AccountService(AccountRepository accounts, IAccountHelper helper,
            AccountRoleRepository accountRoles,
            AccountAdditionalPermissionRepository accountPermissions,
            UserProfileRepository profiles,
            IAuthorizationApplication authorization) {
        this.accounts = accounts;
        this.helper = helper;
        this.accountRoles = accountRoles;
        this.accountPermissions = accountPermissions;
        this.userProfiles = profiles;
        this.authorization = authorization;
    }

    @Transactional
    public AccountModel register(String email, String password) {
        validateCredentials(email, password);
        String normalizedEmail = email.trim().toLowerCase(java.util.Locale.ROOT);
        if (accounts.findByEmail(normalizedEmail).isPresent()) {
            throw new IllegalArgumentException("Account email is existed.");
        }
        var baseRole = authorization.getBaseRole();
        AccountModel account = accounts.save(AccountModel.builder()
                .accountId(UuidV7.generate())
                .accountEmail(normalizedEmail)
                .accountPassword(helper.hashPassword(password))
                .accountIsActive(true)
                .roles(new ArrayList<>(List.of(baseRole)))
                .build());
        return account;
    }

    @Transactional(readOnly = true)
    public AccountModel login(String email, String password) {
        validateCredentials(email, password);
        AccountModel account = accounts.findByEmailWithNavigations(email.trim(), true, true, true)
                .orElseThrow(() -> new IllegalArgumentException("Account not found."));
        if (!Boolean.TRUE.equals(account.getAccountIsActive())) {
            throw new IllegalStateException("Account is not active.");
        }
        if (!helper.verifyPassword(password, account.getAccountPassword())) {
            throw new IllegalArgumentException("Account password is invalid.");
        }
        return account;
    }

    public boolean logout(String email) {
        if (!helper.isEmailValid(email)) {
            throw new IllegalArgumentException("Account email is invalid.");
        }
        throw new UnsupportedOperationException("Logout is not implemented.");
    }

    @Transactional(readOnly = true)
    public AccountModel findByEmail(String email) {
        if (!helper.isEmailValid(email)) {
            throw new IllegalArgumentException("Account email is invalid.");
        }
        return accounts.findByEmail(email.trim())
                .orElseThrow(() -> new IllegalArgumentException("Account not found."));
    }

    @Transactional(readOnly = true)
    public AccountModel findById(UUID accountId) {
        return accounts.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found."));
    }

    @Transactional(readOnly = true)
    public Page<AccountModel> findByStatus(boolean active, int page, int size) {
        return accounts.findByActive(active, PageRequest.of(Math.max(page, 0), Math.clamp(size, 1, 100)));
    }

    @Transactional
    public int changePassword(String email, String oldPassword, String newPassword) {
        if (Objects.equals(oldPassword, newPassword)) {
            throw new IllegalArgumentException("New password must be different from old password.");
        }
        validateCredentials(email, oldPassword);
        if (!helper.isPasswordValid(newPassword)) {
            throw new IllegalArgumentException("Account password is invalid.");
        }
        AccountModel account = accounts.findTrackedByEmail(email.trim())
                .orElseThrow(() -> new IllegalArgumentException("Account not found."));
        if (!Boolean.TRUE.equals(account.getAccountIsActive())) {
            throw new IllegalStateException("Account is not active.");
        }
        if (!helper.verifyPassword(oldPassword, account.getAccountPassword())) {
            throw new IllegalArgumentException("Account password is invalid.");
        }
        account.setAccountPassword(helper.hashPassword(newPassword));
        accounts.save(account);
        return 1;
    }

    @Transactional
    public int deactivate(String email, String password) {
        AccountModel account = login(email, password);
        if (!account.getAccountIsActive()) {
            throw new IllegalStateException("Account is already inactive.");
        }
        account.setAccountIsActive(false);
        accounts.save(account);
        return 1;
    }

    @Transactional
    public int activateByAdmin(String email) {
        AccountModel account = findByEmail(email);
        if (account.getAccountIsActive()) {
            throw new IllegalStateException("Account is already active.");
        }
        account.setAccountIsActive(true);
        accounts.save(account);
        return 1;
    }

    @Transactional
    public int deactivateByAdmin(UUID accountId) {
        AccountModel account = findById(accountId);
        if (!account.getAccountIsActive()) {
            throw new IllegalStateException("Account is already inactive.");
        }
        account.setAccountIsActive(false);
        accounts.save(account);
        return 1;
    }

    @Transactional(readOnly = true)
    public RecordBaseCursorPage<AccountModel> findPage(UUID cursor, int pageSize) {
        return accounts.findPage(cursor, pageSize);
    }

    @Transactional(readOnly = true)
    public RecordBaseCursorPage<AccountModel> findPage(UUID cursor, int pageSize, boolean withProfile) {
        return accounts.findPage(cursor, pageSize, withProfile);
    }

    @Transactional(readOnly = true)
    public RecordBaseCursorPage<AccountModel> findPageByStatus(UUID cursor, int pageSize, boolean active) {
        return accounts.findPageByStatus(cursor, pageSize, active);
    }

    @Transactional
    public int importAccounts(Iterable<AccountModel> accountModels,
            Iterable<AccountRoleModel> roles,
            Iterable<AccountAdditionalPermissionModel> permissions,
            Iterable<UserProfileModel> profiles) {
        var accountsToImport = toList(accountModels);
        if (accountsToImport.isEmpty()) {
            throw new IllegalArgumentException("AccountModel collection is required.");
        }
        var accountRoleModels = toList(roles);
        if (accountRoleModels.isEmpty()) {
            throw new IllegalArgumentException("Account roles collection is required.");
        }
        var accountPermissionModels = permissions == null ? List.<AccountAdditionalPermissionModel>of()
                : toList(permissions);
        var userProfiles = profiles == null ? List.<UserProfileModel>of() : toList(profiles);
        accountsToImport.forEach(account -> validateAccount(account, false));
        if (profiles != null && userProfiles.size() != accountsToImport.size()) {
            throw new IllegalArgumentException(
                    "UserProfileModel collection must have the same count as AccountModel collection.");
        }
        userProfiles.forEach(this::validateImportedProfile);
        accounts.updateAll(accountsToImport);
        accountRoles.addAll(accountRoleModels);
        if (!accountPermissionModels.isEmpty()) {
            accountPermissions.addAll(accountPermissionModels);
        }
        if (!userProfiles.isEmpty()) {
            this.userProfiles.saveAll(userProfiles);
        }
        return accountsToImport.size() + accountRoleModels.size()
                + accountPermissionModels.size() + userProfiles.size();
    }

    @Transactional
    public int updateAll(Iterable<AccountModel> accountModels) {
        var accountsToUpdate = toList(accountModels);
        if (accountsToUpdate.isEmpty()) {
            throw new IllegalArgumentException("AccountModel collection is required.");
        }
        accountsToUpdate.forEach(account -> validateAccount(account, true));
        accounts.updateAll(accountsToUpdate);
        return accountsToUpdate.size();
    }

    private void validateCredentials(String email, String password) {
        if (!helper.isEmailValid(email)) {
            throw new IllegalArgumentException("Account email is invalid.");
        }
        if (!helper.isPasswordValid(password)) {
            throw new IllegalArgumentException("Account password is invalid.");
        }
    }

    private void validateAccount(AccountModel account, boolean requireId) {
        if (account == null) {
            throw new IllegalArgumentException("Account is required.");
        }
        if (requireId && account.getAccountId() == null) {
            throw new IllegalArgumentException("Account id is required.");
        }
        if (!helper.isEmailValid(account.getAccountEmail()) || account.getAccountEmail().length() > 255) {
            throw new IllegalArgumentException("Account email is invalid.");
        }
        String password = account.getAccountPassword();
        if (password == null || password.isBlank() || password.length() > 255) {
            throw new IllegalArgumentException("Account password is required and must not exceed 255 characters.");
        }
    }

    private void validateImportedProfile(UserProfileModel profile) {
        if (profile == null || profile.getUserProfileId() == null
                || profile.getUserProfileId().isBlank() || profile.getUserProfileId().length() != 12) {
            throw new IllegalArgumentException("User profile id is required and must be 12 characters.");
        }
        if (profile.getUserProfileAccountId() == null) {
            throw new IllegalArgumentException("User profile account id is required.");
        }
    }

    private <T> List<T> toList(Iterable<T> values) {
        if (values == null) {
            throw new IllegalArgumentException("Collection is required.");
        }
        return StreamSupport.stream(values.spliterator(), false).toList();
    }
}
