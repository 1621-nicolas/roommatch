package com.roommatch.dto;

import jakarta.validation.constraints.*;

public record PerfilDescripcionRequest(
    @NotNull @Size(max = 500, message = "La descripción no puede superar 500 caracteres") String descripcionPersonal,
    @NotNull(message = "Recarga tu perfil antes de guardar") @Min(0) Long version
) {}
