package com.roommatch.dto;

import jakarta.validation.constraints.Size;

public class LeadHabitacionRequest {

    @jakarta.validation.constraints.Email(message = "Ingresa un correo de contacto válido")
    @Size(max = 150, message = "El correo no puede superar 150 caracteres")
    private String emailContacto;

    public String getEmailContacto() { return emailContacto; }
    public void setEmailContacto(String value) { emailContacto = value == null || value.isBlank() ? null : value.trim(); }


    @Size(max = 500, message = "El mensaje no puede superar 500 caracteres")
    private String mensaje;

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }
}