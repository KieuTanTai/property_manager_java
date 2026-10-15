package com.example.identity.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.identity.models.role.RoleModel;

public interface RoleJpaRepository extends JpaRepository<RoleModel, UUID> {
    Optional<RoleModel> findByRoleCode(String roleCode);
    Optional<RoleModel> findByRoleName(String roleName);

    @Query("select distinct role from RoleModel role left join fetch role.permissions")
    List<RoleModel> findAllWithPermissions();

    @Query("select distinct role from RoleModel role left join fetch role.permissions " +
            "where role.roleCode = :roleCode")
    Optional<RoleModel> findByRoleCodeWithPermissions(@Param("roleCode") String roleCode);

    @Query("select distinct role from RoleModel role left join fetch role.permissions " +
            "where role.roleId in :roleIds")
    List<RoleModel> fetchRolesWithPermissions(@Param("roleIds") List<UUID> roleIds);
}
