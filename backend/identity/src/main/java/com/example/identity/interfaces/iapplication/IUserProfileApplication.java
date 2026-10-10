package com.example.identity.interfaces.iapplication;

import java.util.UUID;

import com.example.identity.models.profile.UserProfileModel;

public interface IUserProfileApplication {
    UserProfileModel getByIdentityCode(String identityCode);
    UserProfileModel getByAccountId(UUID accountId);
    UserProfileModel save(UserProfileModel profile);
}
