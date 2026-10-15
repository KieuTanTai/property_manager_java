package com.example.identity.infrastructure.repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.example.identity.infrastructure.persistence.AccountJpaRepository;
import com.example.identity.infrastructure.persistence.RoleJpaRepository;
import com.example.identity.interfaces.irepository.AccountRepository;
import com.example.identity.models.account.AccountModel;
import com.example.identity.models.role.RoleModel;
import com.example.shared.persistence.record.RecordBaseCursorPage;
import com.example.shared.persistence.SharedGetApplyPagingRepository;

@Repository
public class AccountRepositoryImpl implements AccountRepository {
    private final AccountJpaRepository delegate;
    private final RoleJpaRepository roles;

    public AccountRepositoryImpl(AccountJpaRepository delegate, RoleJpaRepository roles) {
        this.delegate = delegate;
        this.roles = roles;
    }

    @Override
    public AccountModel save(AccountModel account) {
        return delegate.save(account);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccountModel> findAll() {
        return delegate.findAll(Sort.by(Sort.Direction.DESC, "accountId"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccountModel> findByIds(List<UUID> accountIds) {
        return accountIds.isEmpty() ? List.of() : delegate.findByAccountIdIn(accountIds);
    }

    @Override
    public Optional<AccountModel> findTrackedById(UUID id) {
        return delegate.findById(id);
    }

    @Override
    public Optional<AccountModel> findTrackedByEmail(String email) {
        return delegate.findTrackedByAccountEmailIgnoreCase(email);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(UUID id) {
        return delegate.existsById(id);
    }

    @Override
    public Optional<AccountModel> findById(UUID id) {
        return delegate.findById(id);
    }

    @Override
    public Optional<AccountModel> findByEmail(String email) {
        return delegate.findByAccountEmailIgnoreCase(email);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AccountModel> findByEmailWithNavigations(String email,
            boolean withRoles, boolean withAdditionalPermissions, boolean withProfile) {
        Optional<AccountModel> account = delegate.findByAccountEmailIgnoreCase(email);
        return account.flatMap(accountModel -> hydrateAccounts(List.of(accountModel), withRoles, withAdditionalPermissions, withProfile)
                .stream().findFirst());
    }

    @Override
    public Page<AccountModel> findByActive(boolean active, Pageable pageable) {
        return delegate.findByAccountIsActive(active, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public RecordBaseCursorPage<AccountModel> findPage(UUID cursor, int pageSize) {
        return findCursorPage(cursor, pageSize, null, false, false, false, false);
    }

    /**
     * Matches the C# overload that loads roles, permissions assigned to those roles,
     * and account-specific permissions, with an optional profile.
     */
    @Override
    @Transactional(readOnly = true)
    public RecordBaseCursorPage<AccountModel> findPage(UUID cursor, int pageSize, boolean withProfile) {
        return findCursorPage(cursor, pageSize, null, true, true, withProfile, true);
    }

    @Override
    @Transactional(readOnly = true)
    public RecordBaseCursorPage<AccountModel> findPageByStatus(UUID cursor, int pageSize, boolean active) {
        return findCursorPage(cursor, pageSize, active, true, true, true, false);
    }

    private RecordBaseCursorPage<AccountModel> findCursorPage(UUID cursor, int pageSize,
            Boolean active, boolean withRoles, boolean withAdditionalPermissions,
            boolean withProfile, boolean withRolePermissions) {
        SharedGetApplyPagingRepository.validatePageSize(pageSize);
        List<UUID> cursorWindow = delegate.findAccountIdsForCursorPage(cursor, active,
                PageRequest.of(0, pageSize + 1));
        boolean hasMore = cursorWindow.size() > pageSize;
        List<UUID> pageIds = cursorWindow.subList(0, Math.min(pageSize, cursorWindow.size()));
        if (pageIds.isEmpty()) {
            return new RecordBaseCursorPage<>(List.of(), null, false);
        }

        List<AccountModel> pageItems = hydrateByIds(pageIds, withRoles, withAdditionalPermissions,
                withProfile, withRolePermissions);
        UUID nextCursor = hasMore ? pageItems.getLast().getAccountId() : null;
        return new RecordBaseCursorPage<>(pageItems, nextCursor, hasMore);
    }

    private List<AccountModel> hydrateAccounts(List<AccountModel> source, boolean withRoles,
            boolean withAdditionalPermissions, boolean withProfile) {
        List<UUID> ids = source.stream().map(AccountModel::getAccountId).toList();
        return hydrateByIds(ids, withRoles, withAdditionalPermissions, withProfile, false);
    }

    private List<AccountModel> hydrateByIds(List<UUID> ids, boolean withRoles,
            boolean withAdditionalPermissions, boolean withProfile, boolean withRolePermissions) {
        List<AccountModel> accounts = delegate.findByAccountIdIn(ids);
        if (withRoles) {
            delegate.fetchAccountsWithRoles(ids);
        }
        if (withAdditionalPermissions) {
            delegate.fetchAccountsWithAdditionalPermissions(ids);
        }
        if (withProfile) {
            delegate.fetchAccountsWithProfiles(ids);
        }
        if (withRolePermissions) {
            List<UUID> roleIds = accounts.stream()
                    .flatMap(account -> account.getRoles().stream())
                    .map(RoleModel::getRoleId)
                    .distinct()
                    .toList();
            if (!roleIds.isEmpty()) {
                roles.fetchRolesWithPermissions(roleIds);
            }
        }

        Map<UUID, AccountModel> byId = accounts.stream().collect(Collectors.toMap(
                AccountModel::getAccountId, Function.identity(), (first, ignored) -> first,
                HashMap::new));
        List<AccountModel> ordered = new ArrayList<>(ids.size());
        for (UUID id : ids) {
            AccountModel account = byId.get(id);
            if (account != null) {
                ordered.add(account);
            }
        }
        return ordered;
    }

    @Override
    public void updateAll(Iterable<AccountModel> accounts) {
        delegate.saveAll(accounts);
    }
}
