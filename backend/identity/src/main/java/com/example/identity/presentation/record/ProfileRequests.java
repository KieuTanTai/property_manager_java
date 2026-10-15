package com.example.identity.presentation.record;

import java.util.Date;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.example.shared.enums.ESystemUserGender;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public final class ProfileRequests {
    private ProfileRequests() {
    }

    public record Create(
            @NotBlank @Pattern(regexp = "^\\d{12}$") String identityCode,
            @NotNull UUID accountId) {
    }

    public record Update(@NotBlank @Size(max = 12) String identityCode,
            @NotNull UUID accountId,
            @Size(max = 30) String firstName,
            @Size(max = 30) String lastName,
            Date dateOfBirth,
            @Size(max = 10) String phoneNumber,
            @Size(max = 255) String address,
            @Size(max = 255) String avatarUrl,
            @JsonAlias("userGender") ESystemUserGender gender) {
        public Update {
            if (gender == null) {
                gender = ESystemUserGender.Unspecified;
            }
        }
    }
}
