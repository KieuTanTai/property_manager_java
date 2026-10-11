package com.example.identity.presentation.response;

import java.util.Date;
import java.util.UUID;

import com.example.shared.enums.ESystemUserGender;

public record ProfileResponse(
        String identityCode,
        UUID accountId,
        String firstName,
        String lastName,
        String phoneNumber,
        String address,
        String avatarUrl,
        Date dateOfBirth,
        ESystemUserGender gender) {
}
