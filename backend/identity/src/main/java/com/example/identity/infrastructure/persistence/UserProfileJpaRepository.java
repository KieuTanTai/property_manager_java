package com.example.identity.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.identity.models.profile.UserProfileModel;

public interface UserProfileJpaRepository extends JpaRepository<UserProfileModel, String> {
    Optional<UserProfileModel> findByUserProfileAccountId(UUID accountId);
}
