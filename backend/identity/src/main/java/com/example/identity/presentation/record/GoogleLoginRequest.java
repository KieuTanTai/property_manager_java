package com.example.identity.presentation.record;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record GoogleLoginRequest(
        @Email @NotBlank String email,
        @NotBlank String credential) {
}
