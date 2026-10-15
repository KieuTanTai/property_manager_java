package com.example.identity.presentation.assembler;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.Persistence;
import com.example.identity.interfaces.irepository.AccountRepository;
import com.example.identity.models.account.AccountModel;
import com.example.identity.presentation.mapper.IdentityResponseMapper;
import com.example.identity.presentation.response.AccountResponse;
import com.example.identity.presentation.response.ProfileResponse;

@Component
public class IdentityResponseAssembler {
    private final AccountRepository accounts;
    private final IdentityResponseMapper mapper;

    public IdentityResponseAssembler(AccountRepository accounts, IdentityResponseMapper mapper) {
        this.accounts = accounts;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public AccountResponse account(AccountModel account) {
        return mapper.account(hasLoadedAccountPermissions(account) ? account : loadAccount(account));
    }

    @Transactional(readOnly = true)
    public ProfileResponse profile(AccountModel account) {
        AccountModel loaded = Persistence.getPersistenceUtil().isLoaded(account, "userProfile")
                ? account
                : accounts.findByEmailWithNavigations(account.getAccountEmail(), false, false, true)
                        .orElse(account);
        return mapper.profile(loaded.getUserProfile());
    }

    private boolean hasLoadedAccountPermissions(AccountModel account) {
        var persistence = Persistence.getPersistenceUtil();
        return persistence.isLoaded(account, "roles")
                && persistence.isLoaded(account, "additionalPermissions");
    }

    private AccountModel loadAccount(AccountModel account) {
        return accounts.findByEmailWithNavigations(account.getAccountEmail(), true, true, false)
                .orElse(account);
    }
}
