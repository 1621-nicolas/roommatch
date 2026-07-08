package com.roommatch.dto;

import com.roommatch.model.Notificacion;

import java.time.LocalDateTime;

public class NotificacionResponse {

    private Integer idNotificacion;
    private Integer idUsuario;
    private String titulo;
    private String mensaje;
    private String tipo;
    private Boolean leido;
    private String urlDestino;
    private LocalDateTime fechaCreacion;

    public static NotificacionResponse fromEntity(Notificacion notificacion) {
        NotificacionResponse response = new NotificacionResponse();

        response.setIdNotificacion(notificacion.getIdNotificacion());

        if (notificacion.getUsuario() != null) {
            response.setIdUsuario(notificacion.getUsuario().getIdUsuario());
        }

        response.setTitulo(notificacion.getTitulo());
        response.setMensaje(notificacion.getMensaje());
        response.setTipo(notificacion.getTipo());
        response.setLeido(notificacion.getLeido());
        response.setUrlDestino(notificacion.getUrlDestino());
        response.setFechaCreacion(notificacion.getFechaCreacion());

        return response;
    }

    public Integer getIdNotificacion() {
        return idNotificacion;
    }

    public void setIdNotificacion(Integer idNotificacion) {
        this.idNotificacion = idNotificacion;
    }

    public Integer getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Integer idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public Boolean getLeido() {
        return leido;
    }

    public void setLeido(Boolean leido) {
        this.leido = leido;
    }

    public String getUrlDestino() {
        return urlDestino;
    }

    public void setUrlDestino(String urlDestino) {
        this.urlDestino = urlDestino;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }
}