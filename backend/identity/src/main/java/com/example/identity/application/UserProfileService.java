package com.example.identity.application;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.example.identity.interfaces.iapplication.IUserProfileApplication;
import com.example.identity.interfaces.irepository.UserProfileRepository;
import com.example.identity.models.profile.UserProfileModel;

@Service
public class UserProfileService implements IUserProfileApplication {
    private final UserProfileRepository profiles;

    public UserProfileService(UserProfileRepository profiles) {
        this.profiles = profiles;
    }

    public UserProfileModel getByIdentityCode(String identityCode) {
        return profiles.findById(identityCode).orElseThrow(() -> new IllegalArgumentException("Profile not found."));
    }

    public UserProfileModel getByAccountId(UUID accountId) {
        return profiles.findByAccountId(accountId).orElseThrow(() -> new IllegalArgumentException("Profile not found."));
    }

    public UserProfileModel save(UserProfileModel profile) { return profiles.save(profile); }
}
