package com.example.identity.interfaces.iapplication;

import java.util.UUID;

import com.example.identity.models.profile.UserProfileModel;
import com.example.identity.presentation.response.ProfileResponse;

public interface IUserProfileApplication {
    ProfileResponse getByIdentityCode(String identityCode);
    ProfileResponse getByAccountId(UUID accountId);
    ProfileResponse update(UserProfileModel profile);
    ProfileResponse create(String identityCode, UUID accountId);
    int importProfiles(Iterable<UserProfileModel> profiles);
    int updateAll(Iterable<UserProfileModel> profiles);
}
