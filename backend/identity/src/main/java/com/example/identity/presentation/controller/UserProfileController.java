package com.example.identity.presentation.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.identity.interfaces.iapplication.IUserProfileApplication;
import com.example.identity.models.profile.UserProfileModel;
import com.example.identity.presentation.response.ProfileResponse;
import com.example.identity.presentation.record.ProfileRequests;

@RestController
@RequestMapping("/api/profile/profile")
public class UserProfileController {
    private final IUserProfileApplication profiles;

    public UserProfileController(IUserProfileApplication profiles) {
        this.profiles = profiles;
    }

    @GetMapping
    public ResponseEntity<ProfileResponse> getProfile(
            @RequestParam(required = false) String identityCode,
            @RequestParam(required = false) UUID accountId) {
        if (accountId != null) {
            return ResponseEntity.ok(profiles.getByAccountId(accountId));
        }
        if (identityCode != null) {
            return ResponseEntity.ok(profiles.getByIdentityCode(identityCode));
        }
        return ResponseEntity.badRequest().build();
    }

    @PostMapping("/create")
    public ProfileResponse create(@RequestBody ProfileRequests.Create request) {
        if (request.identityCode() == null || request.identityCode().length() != 12) {
            throw new IllegalArgumentException("Identity code must be 12 characters.");
        }
        return profiles.create(request.identityCode(), request.accountId());
    }

    @PostMapping("/update")
    public ProfileResponse update(@RequestBody ProfileRequests.Update request) {
        return profiles.update(UserProfileModel.builder()
                .userProfileId(request.identityCode()).userProfileAccountId(request.accountId())
                .userProfileFirstName(request.firstName()).userProfileLastName(request.lastName())
                .userProfileDateOfBirth(request.dateOfBirth()).userProfilePhoneNumber(request.phoneNumber())
                .userProfileAddress(request.address()).userProfileAvatarUrl(request.avatarUrl())
                .userProfileGender(request.gender()).build());
    }
}
