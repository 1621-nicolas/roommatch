package com.roommatch.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public class ContactoUsuarioRequest {

    @Size(max = 20, message = "El teléfono no puede superar 20 caracteres")
    private String telefono;

    @Size(max = 20, message = "El WhatsApp no puede superar 20 caracteres")
    private String whatsapp;

    @Size(max = 100, message = "El Instagram no puede superar 100 caracteres")
    private String instagram;

    @Size(max = 150, message = "El Facebook no puede superar 150 caracteres")
    private String facebook;

    @Email(message = "El email de contacto no tiene un formato válido")
    private String emailContacto;

    private Boolean mostrarTelefono;
    private Boolean mostrarWhatsapp;
    private Boolean mostrarInstagram;
    private Boolean mostrarFacebook;
    private Boolean mostrarEmail;

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getWhatsapp() {
        return whatsapp;
    }

    public void setWhatsapp(String whatsapp) {
        this.whatsapp = whatsapp;
    }

    public String getInstagram() {
        return instagram;
    }

    public void setInstagram(String instagram) {
        this.instagram = instagram;
    }

    public String getFacebook() {
        return facebook;
    }

    public void setFacebook(String facebook) {
        this.facebook = facebook;
    }

    public String getEmailContacto() {
        return emailContacto;
    }

    public void setEmailContacto(String emailContacto) {
        this.emailContacto = emailContacto;
    }

    public Boolean getMostrarTelefono() {
        return mostrarTelefono;
    }

    public void setMostrarTelefono(Boolean mostrarTelefono) {
        this.mostrarTelefono = mostrarTelefono;
    }

    public Boolean getMostrarWhatsapp() {
        return mostrarWhatsapp;
    }

    public void setMostrarWhatsapp(Boolean mostrarWhatsapp) {
        this.mostrarWhatsapp = mostrarWhatsapp;
    }

    public Boolean getMostrarInstagram() {
        return mostrarInstagram;
    }

    public void setMostrarInstagram(Boolean mostrarInstagram) {
        this.mostrarInstagram = mostrarInstagram;
    }

    public Boolean getMostrarFacebook() {
        return mostrarFacebook;
    }

    public void setMostrarFacebook(Boolean mostrarFacebook) {
        this.mostrarFacebook = mostrarFacebook;
    }

    public Boolean getMostrarEmail() {
        return mostrarEmail;
    }

    public void setMostrarEmail(Boolean mostrarEmail) {
        this.mostrarEmail = mostrarEmail;
    }
}