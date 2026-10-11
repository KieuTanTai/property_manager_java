package com.example.identity.presentation.response;

public record LoginResponse(
        AccountResponse account,
        ProfileResponse profile) {
}
