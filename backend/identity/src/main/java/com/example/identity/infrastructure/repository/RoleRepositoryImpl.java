package com.example.identity.infrastructure.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.example.identity.infrastructure.persistence.RoleJpaRepository;
import com.example.identity.interfaces.irepository.RoleRepository;
import com.example.identity.models.role.RoleModel;

@Repository
public class RoleRepositoryImpl implements RoleRepository {
    private final RoleJpaRepository delegate;
    public RoleRepositoryImpl(RoleJpaRepository delegate) { this.delegate = delegate; }
    public RoleModel save(RoleModel role) { return delegate.save(role); }
    public List<RoleModel> findAll() { return delegate.findAll(); }
    public Optional<RoleModel> findById(UUID id) { return delegate.findById(id); }
    public Optional<RoleModel> findByCode(String code) { return delegate.findByRoleCode(code); }
}
