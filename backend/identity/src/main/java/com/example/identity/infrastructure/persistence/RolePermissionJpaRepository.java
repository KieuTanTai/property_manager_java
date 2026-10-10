package com.example.identity.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.identity.models.role.RolePermissionModel;

public interface RolePermissionJpaRepository extends
        JpaRepository<RolePermissionModel, RolePermissionModel.RolePermissionModelId> {
}
