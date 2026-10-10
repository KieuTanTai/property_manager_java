package com.example.identity.infrastructure.persistence;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.identity.models.account.AccountAdditionalPermissionModel;

public interface AccountAdditionalPermissionJpaRepository extends
        JpaRepository<AccountAdditionalPermissionModel, AccountAdditionalPermissionModel.AccountAdditionalPermissionModelId> {
    void deleteByIdAccountId(UUID accountId);
}
