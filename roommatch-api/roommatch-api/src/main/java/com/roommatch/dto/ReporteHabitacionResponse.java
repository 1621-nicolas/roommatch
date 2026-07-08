package com.roommatch.dto;

import com.roommatch.model.ReporteHabitacion;

import java.time.LocalDateTime;

public class ReporteHabitacionResponse {

    private Integer idReporteHabitacion;
    private Integer idUsuarioReportante;
    private String nombreReportante;
    private Integer idHabitacion;
    private String tituloHabitacion;
    private String propietario;
    private String motivo;
    private String descripcion;
    private String estado;
    private LocalDateTime fechaReporte;
    private LocalDateTime fechaRevision;

    public static ReporteHabitacionResponse fromEntity(ReporteHabitacion reporte) {
        ReporteHabitacionResponse response = new ReporteHabitacionResponse();

        response.setIdReporteHabitacion(reporte.getIdReporteHabitacion());
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

        if (reporte.getHabitacion() != null) {
            response.setIdHabitacion(reporte.getHabitacion().getIdHabitacion());
            response.setTituloHabitacion(reporte.getHabitacion().getTitulo());

            if (reporte.getHabitacion().getPropietario() != null) {
                response.setPropietario(
                        reporte.getHabitacion().getPropietario().getNombreComercial()
                );
            }
        }

        return response;
    }

    public Integer getIdReporteHabitacion() {
        return idReporteHabitacion;
    }

    public void setIdReporteHabitacion(Integer idReporteHabitacion) {
        this.idReporteHabitacion = idReporteHabitacion;
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

    public Integer getIdHabitacion() {
        return idHabitacion;
    }

    public void setIdHabitacion(Integer idHabitacion) {
        this.idHabitacion = idHabitacion;
    }

    public String getTituloHabitacion() {
        return tituloHabitacion;
    }

    public void setTituloHabitacion(String tituloHabitacion) {
        this.tituloHabitacion = tituloHabitacion;
    }

    public String getPropietario() {
        return propietario;
    }

    public void setPropietario(String propietario) {
        this.propietario = propietario;
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