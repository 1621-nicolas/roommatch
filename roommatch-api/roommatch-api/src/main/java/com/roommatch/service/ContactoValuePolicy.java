package com.roommatch.service;

import java.net.URI;
import java.util.Set;

/** Values are stored as text. No server-side requests are made to external profiles. */
public final class ContactoValuePolicy {
    private ContactoValuePolicy() {}

    public static String phone(String value, String label) {
        String text = text(value);
        if (text == null) return null;
        String digits = text.replaceAll("[^0-9]", "");
        if (text.length() > 20 || !text.matches("\\+?[0-9 ()\\-.]+") || digits.length() < 7 || digits.length() > 15) {
            throw new IllegalArgumentException(label + ": usa entre 7 y 15 dígitos, incluyendo el código de país");
        }
        return text;
    }

    public static String social(String value, String network) {
        String text = text(value);
        if (text == null) return null;
        int max = network.equals("instagram") ? 100 : 150;
        String handle = text.startsWith("@") ? text.substring(1) : text;
        if (!text.contains(":") && !text.contains("/") && handle.matches("[A-Za-z0-9_.]{1,50}")) {
            if (network.equals("instagram") && handle.length() > 30) throw invalid(network);
            return handle;
        }
        String candidate = text.matches("^(www\\.|m\\.)?" + network + "\\.com/.*") ? "https://" + text : text;
        try {
            URI uri = URI.create(candidate);
            Set<String> hosts = network.equals("instagram")
                    ? Set.of("instagram.com", "www.instagram.com")
                    : Set.of("facebook.com", "www.facebook.com", "m.facebook.com");
            String path = uri.getPath();
            if (!"https".equalsIgnoreCase(uri.getScheme()) || uri.getHost() == null
                    || !hosts.contains(uri.getHost().toLowerCase(java.util.Locale.ROOT))
                    || uri.getRawUserInfo() != null || uri.getPort() != -1 || uri.getRawFragment() != null
                    || path == null || path.equals("/") || path.contains("%") || uri.toASCIIString().length() > max) throw invalid(network);
            boolean profileId = network.equals("facebook") && path.equals("/profile.php")
                    && uri.getRawQuery() != null && uri.getRawQuery().matches("id=[0-9]{1,30}");
            boolean namedProfile = path.matches("/[A-Za-z0-9_.]+/?") && uri.getRawQuery() == null
                    && !Set.of("/l.php", "/login", "/login.php", "/sharer.php", "/share.php").contains(path.replaceAll("/$", ""));
            if (!profileId && !namedProfile) throw invalid(network);
            return uri.toASCIIString();
        } catch (IllegalArgumentException ex) {
            throw invalid(network);
        }
    }

    public static String text(String value) { return value == null || value.isBlank() ? null : value.trim(); }
    private static IllegalArgumentException invalid(String network) {
        return new IllegalArgumentException("Usa tu usuario de " + network + " o una URL HTTPS de tu perfil en " + network + ".com");
    }
}
