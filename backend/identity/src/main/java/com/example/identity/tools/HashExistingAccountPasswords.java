package com.example.identity.tools;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.regex.Pattern;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * One-off utility for converting existing plain-text account passwords to BCrypt.
 * Run this class directly with the identity module classpath.
 */
public final class HashExistingAccountPasswords {
    private static final String SCHEMA = "ms_identity_test_java";
    private static final Pattern BCRYPT_HASH = Pattern.compile("^\\$2[aby]\\$\\d{2}\\$[./A-Za-z0-9]{53}$");

    private HashExistingAccountPasswords() {
    }

    public static void main(String[] args) throws SQLException {
        String host = env("DB_HOST", "localhost");
        String port = env("DB_PORT", "3306");
        String username = env("DB_USERNAME", "root");
        String password = env("DB_PASSWORD", "");
        String url = "jdbc:mariadb://" + host + ":" + port + "/" + SCHEMA + "?serverTimezone=UTC";

        var encoder = new BCryptPasswordEncoder();
        var accounts = new ArrayList<LegacyAccount>();
        int alreadyHashed = 0;
        int missingPasswords = 0;

        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            connection.setAutoCommit(false);
            try {
                String selectSql = "SELECT account_id, account_password FROM `" + SCHEMA + "`.account";
                try (PreparedStatement select = connection.prepareStatement(selectSql);
                        ResultSet rows = select.executeQuery()) {
                    while (rows.next()) {
                        String storedPassword = rows.getString("account_password");
                        if (storedPassword == null || storedPassword.isBlank()) {
                            missingPasswords++;
                        } else if (BCRYPT_HASH.matcher(storedPassword).matches()) {
                            alreadyHashed++;
                        } else {
                            accounts.add(new LegacyAccount(rows.getObject("account_id"), storedPassword));
                        }
                    }
                }

                String updateSql = "UPDATE `" + SCHEMA
                        + "`.account SET account_password = ? WHERE account_id = ? AND account_password = ?";
                int updated = 0;
                int changedDuringRun = 0;
                try (PreparedStatement update = connection.prepareStatement(updateSql)) {
                    for (LegacyAccount account : accounts) {
                        update.setString(1, encoder.encode(account.password()));
                        update.setObject(2, account.id());
                        update.setString(3, account.password());
                        if (update.executeUpdate() == 1) {
                            updated++;
                        } else {
                            changedDuringRun++;
                        }
                    }
                }
                connection.commit();
                System.out.printf(
                        "Đã hash %d tài khoản trong schema %s; bỏ qua %d mật khẩu BCrypt, %d tài khoản không có mật khẩu, %d dòng đã đổi trong lúc chạy.%n",
                        updated, SCHEMA, alreadyHashed, missingPasswords, changedDuringRun);
            } catch (SQLException | RuntimeException exception) {
                connection.rollback();
                throw exception;
            }
        }
    }

    private static String env(String name, String fallback) {
        String value = System.getenv(name);
        return value == null ? fallback : value;
    }

    private record LegacyAccount(Object id, String password) {
    }
}
