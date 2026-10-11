package com.example.identity.infrastructure.persistence;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

import com.example.identity.models.account.AccountAdditionalPermissionModel;

public interface AccountAdditionalPermissionJpaRepository extends
        JpaRepository<AccountAdditionalPermissionModel, AccountAdditionalPermissionModel.AccountAdditionalPermissionModelId> {
    List<AccountAdditionalPermissionModel> findByIdAccountId(UUID accountId);
    List<AccountAdditionalPermissionModel> findByIdPermissionId(UUID permissionId);
    void deleteByIdAccountIdAndIdPermissionId(UUID accountId, UUID permissionId);
    void deleteByIdAccountId(UUID accountId);
}
