package com.example.identity.presentation.record;

import java.util.Date;
import java.util.UUID;

import com.example.shared.enums.ESystemUserGender;

public final class ProfileRequests {
    private ProfileRequests() {
    }

    public record Create(String identityCode, UUID accountId) {
    }

    public record Update(String identityCode, UUID accountId, String firstName, String lastName,
            Date dateOfBirth, String phoneNumber, String address, String avatarUrl,
            ESystemUserGender gender) {
    }
}
