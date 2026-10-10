package com.example.identity.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.identity.models.account.AccountModel;

public interface AccountJpaRepository extends JpaRepository<AccountModel, UUID> {
    Optional<AccountModel> findByAccountEmailIgnoreCase(String email);
    Page<AccountModel> findByAccountIsActive(boolean active, Pageable pageable);
}
