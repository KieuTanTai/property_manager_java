package com.example.identity.infrastructure.repository;

import java.util.*;
import org.springframework.stereotype.Repository;
import com.example.identity.infrastructure.persistence.AccountRoleJpaRepository;
import com.example.identity.interfaces.irepository.AccountRoleRepository;
import com.example.identity.models.account.AccountRoleModel;

@Repository
public class AccountRoleRepositoryImpl implements AccountRoleRepository {
    private final AccountRoleJpaRepository db;
    public AccountRoleRepositoryImpl(AccountRoleJpaRepository db) { this.db = db; }
    public List<AccountRoleModel> findAll() { return db.findAll(); }
    public List<AccountRoleModel> findByFirstId(UUID id) { return db.findByIdAccountId(id); }
    public List<AccountRoleModel> findBySecondId(UUID id) { return db.findByIdRoleId(id); }
    public Optional<AccountRoleModel> findByIds(UUID a, UUID r) { return db.findById(new AccountRoleModel.AccountRoleModelId(a, r)); }
    public boolean existsByIds(UUID a, UUID r) { return db.existsById(new AccountRoleModel.AccountRoleModelId(a, r)); }
    public AccountRoleModel add(AccountRoleModel e) { return db.save(e); }
    public List<AccountRoleModel> addAll(Iterable<AccountRoleModel> e) { return db.saveAll(e); }
    public void deleteByFirstId(UUID id) { db.deleteByIdAccountId(id); }
    public void deleteBySecondId(UUID id) {
        db.deleteAll(findBySecondId(id));
    }
    public void deleteByIds(UUID a, UUID r) { db.deleteByIdAccountIdAndIdRoleId(a, r); }
}
