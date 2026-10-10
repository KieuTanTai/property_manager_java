package com.example.identity.presentation.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.identity.interfaces.iapplication.IUserProfileApplication;
import com.example.identity.models.profile.UserProfileModel;

@RestController
@RequestMapping("/api/profile/profile")
public class UserProfileController {
    private final IUserProfileApplication profiles;

    public UserProfileController(IUserProfileApplication profiles) {
        this.profiles = profiles;
    }

    @GetMapping
    public ResponseEntity<UserProfileModel> getProfile(
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
    public UserProfileModel create(@RequestBody UserProfileModel profile) {
        if (profile.getUserProfileId() == null || profile.getUserProfileId().length() != 12) {
            throw new IllegalArgumentException("Identity code must be 12 characters.");
        }
        return profiles.save(profile);
    }

    @PostMapping("/update")
    public UserProfileModel update(@RequestBody UserProfileModel profile) {
        return profiles.save(profile);
    }
}
