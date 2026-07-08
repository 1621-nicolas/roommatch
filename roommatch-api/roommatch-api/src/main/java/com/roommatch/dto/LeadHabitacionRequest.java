package com.roommatch.dto;

import jakarta.validation.constraints.Size;

public class LeadHabitacionRequest {

    @Size(max = 500, message = "El mensaje no puede superar 500 caracteres")
    private String mensaje;

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }
}