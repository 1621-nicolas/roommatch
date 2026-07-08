package com.roommatch.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "contacto_usuario")
public class ContactoUsuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_contacto")
    private Integer idContacto;

    @OneToOne
    @JoinColumn(name = "id_usuario", nullable = false, unique = true)
    private Usuario usuario;

    @Column(name = "telefono", length = 20)
    private String telefono;

    @Column(name = "whatsapp", length = 20)
    private String whatsapp;

    @Column(name = "instagram", length = 100)
    private String instagram;

    @Column(name = "facebook", length = 150)
    private String facebook;

    @Column(name = "email_contacto", length = 150)
    private String emailContacto;

    @Column(name = "mostrar_telefono", nullable = false)
    private Boolean mostrarTelefono = false;

    @Column(name = "mostrar_whatsapp", nullable = false)
    private Boolean mostrarWhatsapp = false;

    @Column(name = "mostrar_instagram", nullable = false)
    private Boolean mostrarInstagram = false;

    @Column(name = "mostrar_facebook", nullable = false)
    private Boolean mostrarFacebook = false;

    @Column(name = "mostrar_email", nullable = false)
    private Boolean mostrarEmail = true;

    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion;

    @PrePersist
    public void prePersist() {
        this.fechaActualizacion = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.fechaActualizacion = LocalDateTime.now();
    }

    public Integer getIdContacto() {
        return idContacto;
    }

    public void setIdContacto(Integer idContacto) {
        this.idContacto = idContacto;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
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