package com.example.identity.infrastructure.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import com.example.identity.infrastructure.persistence.AccountJpaRepository;
import com.example.identity.interfaces.irepository.AccountRepository;
import com.example.identity.models.account.AccountModel;
import com.example.shared.persistence.record.RecordBaseCursorPage;
import java.util.ArrayList;

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
    public RecordBaseCursorPage<AccountModel> findPage(UUID cursor, int pageSize, boolean withProfile) {
        var all = delegate.findAll(Sort.by(Sort.Direction.DESC, "accountId"));
        return page(all, cursor, pageSize);
    }
    public RecordBaseCursorPage<AccountModel> findPageByStatus(UUID cursor, int pageSize, boolean active) {
        var all = delegate.findByAccountIsActive(active, PageRequest.of(0, Math.clamp(pageSize + 1, 1, 101),
                        Sort.by(Sort.Direction.DESC, "accountId"))).getContent();
        return page(all, cursor, pageSize);
    }
    private RecordBaseCursorPage<AccountModel> page(List<AccountModel> source, UUID cursor, int size) {
        var filtered = source.stream().filter(a -> cursor == null || a.getAccountId().compareTo(cursor) < 0).limit(Math.max(size, 1) + 1).toList();
        boolean more = filtered.size() > Math.max(size, 1);
        var items = new ArrayList<>(filtered.subList(0, Math.clamp(size, 1, filtered.size())));
        return new RecordBaseCursorPage<>(items, more ? items.getLast().getAccountId() : null, more);
    }
    public void updateAll(Iterable<AccountModel> accounts) { delegate.saveAll(accounts); }

}
