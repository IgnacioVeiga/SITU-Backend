package com.backend.situ.util;

public final class ContactMaskingUtils {
    private ContactMaskingUtils() {
    }

    public static String maskEmail(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }

        int atIndex = email.indexOf('@');
        if (atIndex <= 1) {
            return "***";
        }

        String prefix = email.substring(0, 1);
        String domain = email.substring(atIndex);
        return prefix + "***" + domain;
    }

    public static String maskPhone(String phone) {
        if (phone == null || phone.isBlank()) {
            return null;
        }

        String normalized = phone.replaceAll("\\s+", "");
        if (normalized.length() <= 4) {
            return "***" + normalized;
        }

        String lastFour = normalized.substring(normalized.length() - 4);
        return "***" + lastFour;
    }
}
