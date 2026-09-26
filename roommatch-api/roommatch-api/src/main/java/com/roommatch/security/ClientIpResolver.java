package com.roommatch.security;

import jakarta.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** Only explicitly configured socket peers may supply one literal X-Real-IP value. */
@Component
public class ClientIpResolver {
    private final Set<String> trusted = new HashSet<>();

    public ClientIpResolver(@Value("${app.security.trusted-proxy-ips:}") String configured) {
        for (String item : configured.split(",")) {
            if (item.isBlank()) continue;
            String ip = literal(item.trim());
            if (ip == null) throw new IllegalStateException("TRUSTED_PROXY_IPS solo admite direcciones IP literales, no hostnames ni rangos");
            trusted.add(ip);
        }
    }

    public String resolve(HttpServletRequest request) {
        String peer = literal(request.getRemoteAddr());
        if (peer == null) return "unknown-peer";
        if (!trusted.contains(peer)) return peer;
        var headers = request.getHeaders("X-Real-IP");
        if (headers == null || !headers.hasMoreElements()) return peer;
        String candidate = headers.nextElement();
        if (headers.hasMoreElements()) return peer;
        String client = literal(candidate);
        return client == null ? peer : client;
    }

    private static String literal(String value) {
        if (value == null || value.length() > 45 || value.isBlank() || !value.matches("[0-9a-fA-F:.]+")) return null;
        if (!value.contains(":") || value.contains(".")) {
            String ipv4 = value.substring(value.lastIndexOf(':') + 1);
            String[] parts = ipv4.split("\\.", -1);
            if (parts.length != 4) return null;
            for (String part : parts) {
                if (!part.matches("0|[1-9][0-9]{0,2}") || Integer.parseInt(part) > 255) return null;
            }
        } else if (value.chars().filter(c -> c == ':').count() < 2) return null;
        try {
            // Grammar above excludes hostnames, so this never performs a DNS lookup.
            return InetAddress.getByName(value).getHostAddress();
        } catch (UnknownHostException ex) { return null; }
    }
}
