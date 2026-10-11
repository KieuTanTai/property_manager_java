package com.example.identity.interfaces.irepository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.identity.models.account.AccountModel;
import com.example.shared.persistence.record.RecordBaseCursorPage;

public interface AccountRepository {
    AccountModel save(AccountModel account);
    Optional<AccountModel> findById(UUID id);
    Optional<AccountModel> findByEmail(String email);
    Page<AccountModel> findByActive(boolean active, Pageable pageable);
    RecordBaseCursorPage<AccountModel> findPage(UUID cursor, int pageSize, boolean withProfile);
    RecordBaseCursorPage<AccountModel> findPageByStatus(UUID cursor, int pageSize, boolean active);
    void updateAll(Iterable<AccountModel> accounts);
}
