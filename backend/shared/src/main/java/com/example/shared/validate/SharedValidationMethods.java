package com.example.shared.validate;

import java.util.UUID;
import java.util.regex.Pattern;

public final class SharedValidationMethods {
    private static final Pattern EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    private SharedValidationMethods() {
    }

    public static boolean isValidEmail(String email) {
        return email != null && EMAIL.matcher(email).matches();
    }

    public static boolean isValidId(UUID id) {
        return id != null;
    }

    public static boolean isValidIdentityCode(String identityCode) {
        return identityCode != null && identityCode.length() == 12 && identityCode.chars().allMatch(Character::isDigit);
    }
}
