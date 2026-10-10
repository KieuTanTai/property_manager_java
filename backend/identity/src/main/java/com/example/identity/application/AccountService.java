package com.example.identity.application;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.identity.configuration.IdentityConfiguration;
import com.example.identity.interfaces.irepository.AccountRepository;
import com.example.identity.interfaces.irepository.RoleRepository;
import com.example.identity.interfaces.iapplication.IAccountApplication;
import com.example.identity.models.account.AccountModel;
import com.example.identity.models.role.RoleModel;

@Service
public class AccountService implements IAccountApplication {
    private final AccountRepository accounts;
    private final RoleRepository roles;
    private final PasswordEncoder passwordEncoder;
    private final IdentityConfiguration configuration;

    public AccountService(AccountRepository accounts, RoleRepository roles, PasswordEncoder passwordEncoder,
            IdentityConfiguration configuration) {
        this.accounts = accounts;
        this.roles = roles;
        this.passwordEncoder = passwordEncoder;
        this.configuration = configuration;
    }

    @Transactional
    public AccountModel register(String email, String password) {
        validateCredentials(email, password);
        if (accounts.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Account email is already registered.");
        }
        AccountModel account = AccountModel.builder()
                .accountId(UUID.randomUUID())
                .accountEmail(email.trim().toLowerCase())
                .accountPassword(passwordEncoder.encode(password))
                .accountIsActive(true)
                .build();
        return accounts.save(account);
    }

    @Transactional(readOnly = true)
    public AccountModel login(String email, String password) {
        validateCredentials(email, password);
        AccountModel account = accounts.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Invalid account credentials."));
        if (!Boolean.TRUE.equals(account.getAccountIsActive())
                || !passwordEncoder.matches(password, account.getAccountPassword())) {
            throw new IllegalArgumentException("Invalid account credentials.");
        }
        return account;
    }

    @Transactional(readOnly = true)
    public AccountModel findByEmail(String email) {
        return accounts.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Account not found."));
    }

    @Transactional(readOnly = true)
    public Page<AccountModel> findByStatus(boolean active, int page, int size) {
        return accounts.findByActive(active, PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100)));
    }

    @Transactional
    public AccountModel changePassword(String email, String oldPassword, String newPassword) {
        validateCredentials(email, oldPassword);
        validateCredentials(email, newPassword);
        if (oldPassword.equals(newPassword)) {
            throw new IllegalArgumentException("New password must be different from old password.");
        }
        AccountModel account = login(email, oldPassword);
        account.setAccountPassword(passwordEncoder.encode(newPassword));
        return accounts.save(account);
    }

    @Transactional
    public AccountModel setActive(UUID id, boolean active) {
        AccountModel account = accounts.findById(id).orElseThrow(() -> new IllegalArgumentException("Account not found."));
        account.setAccountIsActive(active);
        return accounts.save(account);
    }

    private void validateCredentials(String email, String password) {
        if (email == null || !email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new IllegalArgumentException("A valid email is required.");
        }
        if (password == null || password.length() < configuration.minPasswordLength()
                || password.length() > configuration.maxPasswordLength()
                || (configuration.requireUppercase() && !password.matches(".*[A-Z].*"))
                || (configuration.requireLowercase() && !password.matches(".*[a-z].*"))
                || (configuration.requireDigit() && !password.matches(".*\\d.*"))
                || (configuration.requireSpecialCharacter() && !password.matches(".*[^A-Za-z0-9].*"))) {
            throw new IllegalArgumentException("Password does not satisfy the configured policy.");
        }
    }
}
