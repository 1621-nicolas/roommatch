package com.roommatch.dto;

import java.time.LocalDateTime;

public record ContactoDesbloqueadoResponse(Integer idUsuario, String nombreCompleto,
        LocalDateTime fechaConexion, ContactoUsuarioResponse contacto) {}
