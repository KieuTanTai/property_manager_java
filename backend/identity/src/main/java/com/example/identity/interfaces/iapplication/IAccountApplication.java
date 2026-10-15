package com.example.identity.interfaces.iapplication;

import java.util.UUID;

import org.springframework.data.domain.Page;

import com.example.identity.models.account.AccountModel;
import com.example.identity.models.account.AccountRoleModel;
import com.example.identity.models.account.AccountAdditionalPermissionModel;
import com.example.identity.models.profile.UserProfileModel;
import com.example.shared.persistence.record.RecordBaseCursorPage;

public interface IAccountApplication {
    AccountModel register(String email, String password);
    AccountModel login(String email, String password);
    boolean logout(String email);
    AccountModel findByEmail(String email);
    AccountModel findById(UUID accountId);
    Page<AccountModel> findByStatus(boolean active, int page, int size);
    int changePassword(String email, String oldPassword, String newPassword);
    int deactivate(String email, String password);
    int activateByAdmin(String email);
    int deactivateByAdmin(UUID accountId);
    RecordBaseCursorPage<AccountModel> findPage(UUID cursor, int pageSize);
    RecordBaseCursorPage<AccountModel> findPage(UUID cursor, int pageSize, boolean withProfile);
    RecordBaseCursorPage<AccountModel> findPageByStatus(UUID cursor, int pageSize, boolean active);
    int importAccounts(Iterable<AccountModel> accounts, Iterable<AccountRoleModel> roles,
                       Iterable<AccountAdditionalPermissionModel> permissions, Iterable<UserProfileModel> profiles);
    int updateAll(Iterable<AccountModel> accounts);
}
