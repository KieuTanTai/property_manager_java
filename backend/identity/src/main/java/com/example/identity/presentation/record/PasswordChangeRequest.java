package com.example.identity.presentation.record;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record PasswordChangeRequest(
        @Email @NotBlank String email,
        @NotBlank String oldPassword,
        @NotBlank String newPassword) {
}
