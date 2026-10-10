package com.example.identity.interfaces.irepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.example.identity.models.permission.PermissionModel;

public interface PermissionRepository {
    PermissionModel save(PermissionModel permission);
    List<PermissionModel> findAll();
    Optional<PermissionModel> findById(UUID id);
    Optional<PermissionModel> findByCode(String code);
}
