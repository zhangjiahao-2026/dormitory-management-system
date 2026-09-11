package com.example.springboot.common;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/** Password hashing with transparent support for legacy salted-MD5 records. */
public final class PasswordUtil {

    private static final BCryptPasswordEncoder BCRYPT = new BCryptPasswordEncoder();

    private PasswordUtil() {
    }

    public static String encode(String rawPassword) {
        return BCRYPT.encode(rawPassword);
    }

    public static boolean matches(String rawPassword, String storedPassword) {
        if (rawPassword == null || storedPassword == null) {
            return false;
        }
        if (isBcrypt(storedPassword)) {
            return BCRYPT.matches(rawPassword, storedPassword);
        }
        return MD5Util.md5(rawPassword).equals(storedPassword);
    }

    public static boolean needsUpgrade(String storedPassword) {
        return storedPassword != null && !isBcrypt(storedPassword);
    }

    public static boolean isBcrypt(String value) {
        return value != null && (value.startsWith("$2a$") || value.startsWith("$2b$") || value.startsWith("$2y$"));
    }
}
