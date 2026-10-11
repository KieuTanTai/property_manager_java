package com.example.identity.utils;

import java.util.regex.Pattern;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.example.identity.configuration.IdentityConfiguration;
import com.example.identity.interfaces.IAccountHelper;
import org.yaml.snakeyaml.util.Tuple;

@Component
public class AccountHelper implements IAccountHelper {
    private static final Pattern EMAIL = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private final PasswordEncoder encoder;
    private final IdentityConfiguration rules;

    public AccountHelper(PasswordEncoder encoder, IdentityConfiguration rules) {
        this.encoder = encoder;
        this.rules = rules;
    }

    @Override
    public Boolean isEmailValid(String email) {
        return email != null && !email.isBlank() && EMAIL.matcher(email).matches();
    }

    @Override
    public Boolean isPasswordValid(String password) {
        if (password == null || password.isBlank()
                || password.length() < rules.minPasswordLength()
                || password.length() > rules.maxPasswordLength()) return false;
        return (!rules.requireDigit() || password.chars().anyMatch(Character::isDigit))
                && (!rules.requireUppercase() || password.chars().anyMatch(Character::isUpperCase))
                && (!rules.requireLowercase() || password.chars().anyMatch(Character::isLowerCase))
                && (!rules.requireSpecialCharacter() || password.chars().anyMatch(c -> !Character.isLetterOrDigit(c)));
    }

    @Override public String hashPassword(String password) { return encoder.encode(password); }
    @Override public Boolean verifyPassword(String password, String hash) { return encoder.matches(password, hash); }

    @Override
    public Tuple<Boolean, String> validateEmailAndPassword(String email, String password) {
        if (!isEmailValid(email)) return new Tuple<>(false, "Invalid email format.");
        if (!isPasswordValid(password)) return new Tuple<>(false, "Password does not meet the required criteria.");
        return new Tuple<>(true, "Valid email and password.");
    }
}
