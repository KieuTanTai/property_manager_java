package com.example.identity.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.identity.models.role.RoleModel;

public interface RoleJpaRepository extends JpaRepository<RoleModel, UUID> {
    Optional<RoleModel> findByRoleCode(String roleCode);
    Optional<RoleModel> findByRoleName(String roleName);
}
