package com.carocart.delivery.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordUtil {
    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public static String hashPassword(String raw) {
        return encoder.encode(raw);
    }

    public static boolean verifyPassword(String raw, String hash) {
        return encoder.matches(raw, hash);
    }
}
