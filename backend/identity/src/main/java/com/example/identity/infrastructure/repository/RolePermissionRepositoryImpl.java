package com.example.identity.infrastructure.repository;

import java.util.*;
import org.springframework.stereotype.Repository;
import com.example.identity.infrastructure.persistence.RolePermissionJpaRepository;
import com.example.identity.interfaces.irepository.RolePermissionRepository;
import com.example.identity.models.role.RolePermissionModel;

@Repository
public class RolePermissionRepositoryImpl implements RolePermissionRepository {
    private final RolePermissionJpaRepository db;
    public RolePermissionRepositoryImpl(RolePermissionJpaRepository db) { this.db = db; }
    public List<RolePermissionModel> findAll() { return db.findAll(); }
    public List<RolePermissionModel> findByFirstId(UUID id) { return db.findByIdRoleId(id); }
    public List<RolePermissionModel> findBySecondId(UUID id) { return db.findByIdPermissionId(id); }
    public Optional<RolePermissionModel> findByIds(UUID a, UUID p) { return db.findById(new RolePermissionModel.RolePermissionModelId(a, p)); }
    public boolean existsByIds(UUID a, UUID p) { return db.existsById(new RolePermissionModel.RolePermissionModelId(a, p)); }
    public RolePermissionModel add(RolePermissionModel e) { return db.save(e); }
    public List<RolePermissionModel> addAll(Iterable<RolePermissionModel> e) { return db.saveAll(e); }
    public void deleteByFirstId(UUID id) {
        db.deleteAll(findByFirstId(id));
    }
    public void deleteBySecondId(UUID id) { db.deleteAll(findBySecondId(id)); }
    public void deleteByIds(UUID a, UUID p) { db.deleteByIdRoleIdAndIdPermissionId(a, p); }
}
