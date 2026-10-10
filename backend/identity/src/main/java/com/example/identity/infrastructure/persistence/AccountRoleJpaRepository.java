package com.example.identity.infrastructure.persistence;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.identity.models.account.AccountRoleModel;

public interface AccountRoleJpaRepository extends JpaRepository<AccountRoleModel, AccountRoleModel.AccountRoleModelId> {
    void deleteByIdAccountId(UUID accountId);
}
