package com.example.identity.interfaces.irepository;

import com.example.identity.models.role.RolePermissionModel;
import com.example.shared.interfaces.BaseAssociativeRepository;
import java.util.UUID;

public interface RolePermissionRepository extends BaseAssociativeRepository<RolePermissionModel, UUID> {}
