package com.example.identity.infrastructure.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.example.identity.infrastructure.persistence.AccountJpaRepository;
import com.example.identity.interfaces.irepository.AccountRepository;
import com.example.identity.models.account.AccountModel;

@Repository
public class AccountRepositoryImpl implements AccountRepository {
    private final AccountJpaRepository delegate;

    public AccountRepositoryImpl(AccountJpaRepository delegate) {
        this.delegate = delegate;
    }

    public AccountModel save(AccountModel account) { return delegate.save(account); }
    public Optional<AccountModel> findById(UUID id) { return delegate.findById(id); }
    public Optional<AccountModel> findByEmail(String email) { return delegate.findByAccountEmailIgnoreCase(email); }
    public Page<AccountModel> findByActive(boolean active, Pageable pageable) {
        return delegate.findByAccountIsActive(active, pageable);
    }

}
