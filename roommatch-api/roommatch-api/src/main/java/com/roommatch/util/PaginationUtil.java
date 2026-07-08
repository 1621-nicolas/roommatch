package com.roommatch.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

public final class PaginationUtil {

    private PaginationUtil() {
    }

    public static Pageable crearPageable(int page, int size) {

        if (page < 0) {
            throw new IllegalArgumentException("El número de página no puede ser negativo");
        }

        if (size <= 0) {
            throw new IllegalArgumentException("El tamaño de página debe ser mayor a cero");
        }

        if (size > 50) {
            throw new IllegalArgumentException("El tamaño máximo permitido por página es 50");
        }

        return PageRequest.of(page, size);
    }
}