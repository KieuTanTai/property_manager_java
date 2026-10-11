package com.example.identity.interfaces;

import org.yaml.snakeyaml.util.Tuple;

public interface IAccountHelper {
    Tuple<Boolean, String> validateEmailAndPassword(String email, String password);
    Boolean isEmailValid(String email);
    Boolean isPasswordValid(String password);
    String hashPassword(String password);
    Boolean verifyPassword(String password, String hash);
}
