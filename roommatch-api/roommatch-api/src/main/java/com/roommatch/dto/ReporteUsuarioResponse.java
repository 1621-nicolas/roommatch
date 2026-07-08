package com.roommatch.dto;

import com.roommatch.model.ReporteUsuario;

import java.time.LocalDateTime;

public class ReporteUsuarioResponse {

    private Integer idReporte;
    private Integer idUsuarioReportante;
    private String nombreReportante;
    private Integer idUsuarioReportado;
    private String nombreReportado;
    private String motivo;
    private String descripcion;
    private String estado;
    private LocalDateTime fechaReporte;
    private LocalDateTime fechaRevision;

    public static ReporteUsuarioResponse fromEntity(ReporteUsuario reporte) {
        ReporteUsuarioResponse response = new ReporteUsuarioResponse();

        response.setIdReporte(reporte.getIdReporte());
        response.setMotivo(reporte.getMotivo());
        response.setDescripcion(reporte.getDescripcion());
        response.setEstado(reporte.getEstado());
        response.setFechaReporte(reporte.getFechaReporte());
        response.setFechaRevision(reporte.getFechaRevision());

        if (reporte.getUsuarioReportante() != null) {
            response.setIdUsuarioReportante(reporte.getUsuarioReportante().getIdUsuario());
            response.setNombreReportante(
                    reporte.getUsuarioReportante().getNombres() + " " +
                    reporte.getUsuarioReportante().getApellidos()
            );
        }

        if (reporte.getUsuarioReportado() != null) {
            response.setIdUsuarioReportado(reporte.getUsuarioReportado().getIdUsuario());
            response.setNombreReportado(
                    reporte.getUsuarioReportado().getNombres() + " " +
                    reporte.getUsuarioReportado().getApellidos()
            );
        }

        return response;
    }

    public Integer getIdReporte() {
        return idReporte;
    }

    public void setIdReporte(Integer idReporte) {
        this.idReporte = idReporte;
    }

    public Integer getIdUsuarioReportante() {
        return idUsuarioReportante;
    }

    public void setIdUsuarioReportante(Integer idUsuarioReportante) {
        this.idUsuarioReportante = idUsuarioReportante;
    }

    public String getNombreReportante() {
        return nombreReportante;
    }

    public void setNombreReportante(String nombreReportante) {
        this.nombreReportante = nombreReportante;
    }

    public Integer getIdUsuarioReportado() {
        return idUsuarioReportado;
    }

    public void setIdUsuarioReportado(Integer idUsuarioReportado) {
        this.idUsuarioReportado = idUsuarioReportado;
    }

    public String getNombreReportado() {
        return nombreReportado;
    }

    public void setNombreReportado(String nombreReportado) {
        this.nombreReportado = nombreReportado;
    }

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


    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public LocalDateTime getFechaReporte() {
        return fechaReporte;
    }

    public void setFechaReporte(LocalDateTime fechaReporte) {
        this.fechaReporte = fechaReporte;
    }

    public LocalDateTime getFechaRevision() {
        return fechaRevision;
    }

    public void setFechaRevision(LocalDateTime fechaRevision) {
        this.fechaRevision = fechaRevision;
    }
}