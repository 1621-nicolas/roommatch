package com.roommatch.dto;

import com.roommatch.model.SolicitudContacto;

import java.time.LocalDateTime;

public class SolicitudContactoResponse {

    private Integer idSolicitud;

    private Integer idUsuarioEmisor;
    private String nombreEmisor;

    private Integer idUsuarioReceptor;
    private String nombreReceptor;

    private String mensaje;
    private String estado;
    private LocalDateTime fechaSolicitud;
    private LocalDateTime fechaRespuesta;

    public static SolicitudContactoResponse fromEntity(SolicitudContacto solicitud) {
        SolicitudContactoResponse response = new SolicitudContactoResponse();

        response.setIdSolicitud(solicitud.getIdSolicitud());

        if (solicitud.getUsuarioEmisor() != null) {
            response.setIdUsuarioEmisor(solicitud.getUsuarioEmisor().getIdUsuario());
            response.setNombreEmisor(
                    solicitud.getUsuarioEmisor().getNombres() + " " +
                    solicitud.getUsuarioEmisor().getApellidos()
            );
        }

        if (solicitud.getUsuarioReceptor() != null) {
            response.setIdUsuarioReceptor(solicitud.getUsuarioReceptor().getIdUsuario());
            response.setNombreReceptor(
                    solicitud.getUsuarioReceptor().getNombres() + " " +
                    solicitud.getUsuarioReceptor().getApellidos()
            );
        }

        response.setMensaje(solicitud.getMensaje());
        response.setEstado(solicitud.getEstado());
        response.setFechaSolicitud(solicitud.getFechaSolicitud());
        response.setFechaRespuesta(solicitud.getFechaRespuesta());

        return response;
    }

    public Integer getIdSolicitud() {
        return idSolicitud;
    }

    public void setIdSolicitud(Integer idSolicitud) {
        this.idSolicitud = idSolicitud;
    }

    public Integer getIdUsuarioEmisor() {
        return idUsuarioEmisor;
    }

    public void setIdUsuarioEmisor(Integer idUsuarioEmisor) {
        this.idUsuarioEmisor = idUsuarioEmisor;
    }

    public String getNombreEmisor() {
        return nombreEmisor;
    }

    public void setNombreEmisor(String nombreEmisor) {
        this.nombreEmisor = nombreEmisor;
    }

    public Integer getIdUsuarioReceptor() {
        return idUsuarioReceptor;
    }

    public void setIdUsuarioReceptor(Integer idUsuarioReceptor) {
        this.idUsuarioReceptor = idUsuarioReceptor;
    }

    public String getNombreReceptor() {
        return nombreReceptor;
    }

    public void setNombreReceptor(String nombreReceptor) {
        this.nombreReceptor = nombreReceptor;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public LocalDateTime getFechaSolicitud() {
        return fechaSolicitud;
    }

    public void setFechaSolicitud(LocalDateTime fechaSolicitud) {
        this.fechaSolicitud = fechaSolicitud;
    }

    public LocalDateTime getFechaRespuesta() {
        return fechaRespuesta;
    }

    public void setFechaRespuesta(LocalDateTime fechaRespuesta) {
        this.fechaRespuesta = fechaRespuesta;
    }
}