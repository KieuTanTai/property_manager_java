package com.example.identity.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.identity.models.permission.PermissionModel;

public interface PermissionJpaRepository extends JpaRepository<PermissionModel, UUID> {
    Optional<PermissionModel> findByPermissionCode(String permissionCode);
}
