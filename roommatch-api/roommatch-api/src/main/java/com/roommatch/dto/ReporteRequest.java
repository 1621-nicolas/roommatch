package com.roommatch.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ReporteRequest {

    @NotBlank(message = "El motivo es obligatorio")
    @Size(max = 150, message = "El motivo no puede superar 150 caracteres")
    private String motivo;

    @Size(max = 600, message = "La descripción no puede superar 600 caracteres")
    private String descripcion;

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}