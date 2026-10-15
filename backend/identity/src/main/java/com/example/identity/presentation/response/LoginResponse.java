package com.example.identity.presentation.response;

import java.util.List;

public record LoginResponse(
        AccountResponse account,
        ProfileResponse profile,
        List<ClaimResponse> claims) {
}
