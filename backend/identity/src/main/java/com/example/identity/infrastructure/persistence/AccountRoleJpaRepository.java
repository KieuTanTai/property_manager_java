package com.example.identity.infrastructure.persistence;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

import com.example.identity.models.account.AccountRoleModel;

public interface AccountRoleJpaRepository extends JpaRepository<AccountRoleModel, AccountRoleModel.AccountRoleModelId> {
    List<AccountRoleModel> findByIdAccountId(UUID accountId);
    List<AccountRoleModel> findByIdRoleId(UUID roleId);
    void deleteByIdAccountIdAndIdRoleId(UUID accountId, UUID roleId);
    void deleteByIdAccountId(UUID accountId);
}
