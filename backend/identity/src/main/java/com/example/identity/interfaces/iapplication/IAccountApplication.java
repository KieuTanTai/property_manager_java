package com.example.identity.interfaces.iapplication;

import java.util.UUID;

import org.springframework.data.domain.Page;

import com.example.identity.models.account.AccountModel;

public interface IAccountApplication {
    AccountModel register(String email, String password);
    AccountModel login(String email, String password);
    AccountModel findByEmail(String email);
    Page<AccountModel> findByStatus(boolean active, int page, int size);
    AccountModel changePassword(String email, String oldPassword, String newPassword);
    AccountModel setActive(UUID accountId, boolean active);
}
