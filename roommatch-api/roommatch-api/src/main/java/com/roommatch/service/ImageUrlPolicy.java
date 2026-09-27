package com.roommatch.service;

import java.net.URI;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ImageUrlPolicy {
    private final boolean allowHttp;
    public ImageUrlPolicy(@Value("${app.images.allow-http:false}") boolean allowHttp) { this.allowHttp = allowHttp; }

    public String validate(String value) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException("La URL de imagen es obligatoria");
        String url = value.trim();
        if (url.length() > 255 || url.chars().anyMatch(c -> c <= 32 || c == 127))
            throw new IllegalArgumentException("La URL debe tener hasta 255 caracteres y no contener espacios ni controles");
        try {
            URI uri = new URI(url);
            String scheme = uri.getScheme() == null ? "" : uri.getScheme().toLowerCase(Locale.ROOT);
            if (!(scheme.equals("https") || allowHttp && scheme.equals("http")) || uri.getHost() == null
                    || uri.getRawUserInfo() != null || uri.getRawFragment() != null || uri.getPort() > 65535 || uri.getPort() == 0)
                throw new IllegalArgumentException("Usa una URL HTTPS absoluta, sin credenciales ni fragmentos");
            String ascii = uri.toASCIIString();
            if (ascii.length() > 255) throw new IllegalArgumentException("La URL codificada supera 255 caracteres");
            return ascii;
        } catch (java.net.URISyntaxException ex) {
            throw new IllegalArgumentException("La URL de imagen no es válida");
        }
    }

    public boolean allowed(String value) {
        try { validate(value); return true; } catch (IllegalArgumentException ex) { return false; }
    }

    static int position(Integer requested, int existing) {
        if (existing >= 5) throw new com.roommatch.exception.ConflictException("Solo puedes registrar hasta 5 imágenes");
        int result = requested == null ? existing + 1 : requested;
        if (result < 1 || result > existing + 1) throw new IllegalArgumentException("El orden debe estar entre 1 y " + (existing + 1));
        return result;
    }
}
