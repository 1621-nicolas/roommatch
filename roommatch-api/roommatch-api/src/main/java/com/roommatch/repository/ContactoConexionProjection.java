package com.roommatch.repository;

import java.time.LocalDateTime;

/** Only connection metadata, never account email or other private fields. */
public interface ContactoConexionProjection {
    Integer getIdUsuario();
    String getNombres();
    String getApellidos();
    LocalDateTime getFechaConexion();
}
