package com.example.identity.infrastructure.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.example.identity.infrastructure.persistence.UserProfileJpaRepository;
import com.example.identity.interfaces.irepository.UserProfileRepository;
import com.example.identity.models.profile.UserProfileModel;

@Repository
public class UserProfileRepositoryImpl implements UserProfileRepository {
    private final UserProfileJpaRepository delegate;
    public UserProfileRepositoryImpl(UserProfileJpaRepository delegate) { this.delegate = delegate; }
    public UserProfileModel save(UserProfileModel profile) { return delegate.save(profile); }
    public Optional<UserProfileModel> findById(String identityCode) { return delegate.findById(identityCode); }
    public Optional<UserProfileModel> findByAccountId(UUID accountId) {
        return delegate.findByUserProfileAccountId(accountId);
    }
}
