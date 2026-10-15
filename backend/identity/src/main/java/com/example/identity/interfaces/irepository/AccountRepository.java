package com.example.identity.interfaces.irepository;

import java.util.Optional;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.identity.models.account.AccountModel;
import com.example.shared.persistence.record.RecordBaseCursorPage;

public interface AccountRepository {
    AccountModel save(AccountModel account);
    List<AccountModel> findAll();
    List<AccountModel> findByIds(List<UUID> accountIds);
    Optional<AccountModel> findTrackedById(UUID id);
    Optional<AccountModel> findTrackedByEmail(String email);
    boolean existsById(UUID id);
    Optional<AccountModel> findById(UUID id);
    Optional<AccountModel> findByEmail(String email);
    Optional<AccountModel> findByEmailWithNavigations(String email,
            boolean withRoles, boolean withAdditionalPermissions, boolean withProfile);
    Page<AccountModel> findByActive(boolean active, Pageable pageable);
    RecordBaseCursorPage<AccountModel> findPage(UUID cursor, int pageSize);
    RecordBaseCursorPage<AccountModel> findPage(UUID cursor, int pageSize, boolean withProfile);
    RecordBaseCursorPage<AccountModel> findPageByStatus(UUID cursor, int pageSize, boolean active);
    void updateAll(Iterable<AccountModel> accounts);
}
