package com.example.identity.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.identity.models.account.AccountModel;

public interface AccountJpaRepository extends JpaRepository<AccountModel, UUID> {
    Optional<AccountModel> findByAccountEmailIgnoreCase(String email);
    Optional<AccountModel> findTrackedByAccountEmailIgnoreCase(String email);
    Page<AccountModel> findByAccountIsActive(boolean active, Pageable pageable);

    List<AccountModel> findByAccountIdIn(Iterable<UUID> accountIds);

    @Query("select account.accountId from AccountModel account " +
            "where (:cursor is null or account.accountId < :cursor) " +
            "and (:active is null or account.accountIsActive = :active) " +
            "order by account.accountId desc")
    List<UUID> findAccountIdsForCursorPage(@Param("cursor") UUID cursor,
            @Param("active") Boolean active, Pageable pageable);

    @Query("select distinct account from AccountModel account " +
            "left join fetch account.roles " +
            "where account.accountId in :accountIds")
    List<AccountModel> fetchAccountsWithRoles(@Param("accountIds") List<UUID> accountIds);

    @Query("select distinct account from AccountModel account " +
            "left join fetch account.additionalPermissions " +
            "where account.accountId in :accountIds")
    List<AccountModel> fetchAccountsWithAdditionalPermissions(@Param("accountIds") List<UUID> accountIds);

    @Query("select account from AccountModel account " +
            "left join fetch account.userProfile " +
            "where account.accountId in :accountIds")
    List<AccountModel> fetchAccountsWithProfiles(@Param("accountIds") List<UUID> accountIds);
}
