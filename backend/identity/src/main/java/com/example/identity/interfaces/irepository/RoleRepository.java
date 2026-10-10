package com.example.identity.interfaces.irepository;

import com.example.identity.models.role.RoleModel;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


public interface RoleRepository {
    RoleModel save(RoleModel role);
    List<RoleModel> findAll();
    Optional<RoleModel> findById(UUID id);
    Optional<RoleModel> findByCode(String code);
}
