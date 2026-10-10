package com.example.shared.model.helper;

import java.util.Objects;

public final class ModelFieldGuard {
    private ModelFieldGuard() {
    }

    public static <T> T requireNonNull(T value, String fieldName) {
        return Objects.requireNonNull(value, fieldName + " must not be null");
    }

    public static String requireNotBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value;
    }

    public static String required(String value, int maxLength, String fieldName) {
        String normalized = requireNotBlank(value, fieldName).trim();
        if (normalized.length() > maxLength) {
            throw new IllegalArgumentException(fieldName + " cannot exceed " + maxLength + " characters");
        }
        return normalized;
    }

    public static boolean validateIds(java.util.UUID id, java.util.List<java.util.UUID> ids) {
        return id != null && ids != null && !ids.isEmpty()
                && ids.stream().allMatch(Objects::nonNull);
    }
}
