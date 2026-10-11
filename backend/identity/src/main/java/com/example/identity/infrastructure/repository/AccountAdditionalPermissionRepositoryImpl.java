package com.example.identity.infrastructure.repository;

import java.util.*;
import org.springframework.stereotype.Repository;
import com.example.identity.infrastructure.persistence.AccountAdditionalPermissionJpaRepository;
import com.example.identity.interfaces.irepository.AccountAdditionalPermissionRepository;
import com.example.identity.models.account.AccountAdditionalPermissionModel;

@Repository
public class AccountAdditionalPermissionRepositoryImpl implements AccountAdditionalPermissionRepository {
    private final AccountAdditionalPermissionJpaRepository db;
    public AccountAdditionalPermissionRepositoryImpl(AccountAdditionalPermissionJpaRepository db) { this.db = db; }

    public List<AccountAdditionalPermissionModel> findAll() { return db.findAll(); }

    public List<AccountAdditionalPermissionModel> findByFirstId(UUID id) { return db.findByIdAccountId(id); }

    public List<AccountAdditionalPermissionModel> findBySecondId(UUID id) { return db.findByIdPermissionId(id); }

    public Optional<AccountAdditionalPermissionModel> findByIds(UUID a, UUID p) { return db.findById(new AccountAdditionalPermissionModel.AccountAdditionalPermissionModelId(a, p)); }
    public boolean existsByIds(UUID a, UUID p) { return db.existsById(new AccountAdditionalPermissionModel.AccountAdditionalPermissionModelId(a, p)); }

    public AccountAdditionalPermissionModel add(AccountAdditionalPermissionModel e) { return db.save(e); }

    public List<AccountAdditionalPermissionModel> addAll(Iterable<AccountAdditionalPermissionModel> e) { return db.saveAll(e); }
    public void deleteByFirstId(UUID id) { db.deleteByIdAccountId(id); }
    public void deleteBySecondId(UUID id) {
        db.deleteAll(findBySecondId(id));
    }
    public void deleteByIds(UUID a, UUID p) { db.deleteByIdAccountIdAndIdPermissionId(a, p); }
}
