package com.example.identity.interfaces.irepository;

import com.example.identity.models.profile.UserProfileModel;

import java.util.Optional;
import java.util.UUID;


public interface UserProfileRepository {
    UserProfileModel save(UserProfileModel profile);
    Optional<UserProfileModel> findById(String identityCode);
    Optional<UserProfileModel> findByAccountId(UUID accountId);
}
