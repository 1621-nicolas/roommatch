package com.roommatch.dto;

import com.roommatch.model.ContactoUsuario;

import java.time.LocalDateTime;

public class ContactoUsuarioResponse {

    private Integer idContacto;
    private Integer idUsuario;
    private String nombreUsuario;

    private String telefono;
    private String whatsapp;
    private String instagram;
    private String facebook;
    private String emailContacto;

    private Boolean mostrarTelefono;
    private Boolean mostrarWhatsapp;
    private Boolean mostrarInstagram;
    private Boolean mostrarFacebook;
    private Boolean mostrarEmail;

    private LocalDateTime fechaActualizacion;

    public static ContactoUsuarioResponse fromEntity(ContactoUsuario contacto, boolean aplicarPrivacidad) {
        ContactoUsuarioResponse response = new ContactoUsuarioResponse();

        response.setIdContacto(contacto.getIdContacto());

        if (contacto.getUsuario() != null) {
            response.setIdUsuario(contacto.getUsuario().getIdUsuario());
            response.setNombreUsuario(
                    contacto.getUsuario().getNombres() + " " +
                    contacto.getUsuario().getApellidos()
            );
        }

        response.setMostrarTelefono(contacto.getMostrarTelefono());
        response.setMostrarWhatsapp(contacto.getMostrarWhatsapp());
        response.setMostrarInstagram(contacto.getMostrarInstagram());
        response.setMostrarFacebook(contacto.getMostrarFacebook());
        response.setMostrarEmail(contacto.getMostrarEmail());
        response.setFechaActualizacion(contacto.getFechaActualizacion());

        if (aplicarPrivacidad) {
            response.setTelefono(Boolean.TRUE.equals(contacto.getMostrarTelefono()) ? contacto.getTelefono() : null);
            response.setWhatsapp(Boolean.TRUE.equals(contacto.getMostrarWhatsapp()) ? contacto.getWhatsapp() : null);
            response.setInstagram(Boolean.TRUE.equals(contacto.getMostrarInstagram()) ? contacto.getInstagram() : null);
            response.setFacebook(Boolean.TRUE.equals(contacto.getMostrarFacebook()) ? contacto.getFacebook() : null);
            response.setEmailContacto(Boolean.TRUE.equals(contacto.getMostrarEmail()) ? contacto.getEmailContacto() : null);
        } else {
            response.setTelefono(contacto.getTelefono());
            response.setWhatsapp(contacto.getWhatsapp());
            response.setInstagram(contacto.getInstagram());
            response.setFacebook(contacto.getFacebook());
            response.setEmailContacto(contacto.getEmailContacto());
        }

        return response;
    }

    public Integer getIdContacto() {
        return idContacto;
    }

    public void setIdContacto(Integer idContacto) {
        this.idContacto = idContacto;
    }

    public Integer getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Integer idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

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

    public LocalDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }

    public void setFechaActualizacion(LocalDateTime fechaActualizacion) {
        this.fechaActualizacion = fechaActualizacion;
    }
}