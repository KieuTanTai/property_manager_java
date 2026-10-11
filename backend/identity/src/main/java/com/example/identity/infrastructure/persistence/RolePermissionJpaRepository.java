package com.example.identity.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

import com.example.identity.models.role.RolePermissionModel;

public interface RolePermissionJpaRepository extends
        JpaRepository<RolePermissionModel, RolePermissionModel.RolePermissionModelId> {
    List<RolePermissionModel> findByIdRoleId(UUID roleId);
    List<RolePermissionModel> findByIdPermissionId(UUID permissionId);
    void deleteByIdRoleIdAndIdPermissionId(UUID roleId, UUID permissionId);
}
