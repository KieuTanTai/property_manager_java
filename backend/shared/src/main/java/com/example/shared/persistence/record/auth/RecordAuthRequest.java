package com.example.shared.persistence.record.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RecordAuthRequest(@Email @NotBlank String email, @NotBlank String password) {
}
