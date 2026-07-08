package com.roommatch.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "reporte_habitacion")
public class ReporteHabitacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_reporte_habitacion")
    private Integer idReporteHabitacion;

    @ManyToOne
    @JoinColumn(name = "id_usuario_reportante", nullable = false)
    private Usuario usuarioReportante;

    @ManyToOne
    @JoinColumn(name = "id_habitacion", nullable = false)
    private Habitacion habitacion;

    @Column(name = "motivo", nullable = false, length = 150)
    private String motivo;

    @Column(name = "descripcion", length = 600)
    private String descripcion;

    @Column(name = "estado", nullable = false, length = 30)
    private String estado = "pendiente";

    @Column(name = "fecha_reporte", nullable = false)
    private LocalDateTime fechaReporte;

    @Column(name = "fecha_revision")
    private LocalDateTime fechaRevision;

    @PrePersist
    public void prePersist() {
        this.fechaReporte = LocalDateTime.now();
        this.estado = "pendiente";
    }

    public Integer getIdReporteHabitacion() {
        return idReporteHabitacion;
    }

    public void setIdReporteHabitacion(Integer idReporteHabitacion) {
        this.idReporteHabitacion = idReporteHabitacion;
    }

    public Usuario getUsuarioReportante() {
        return usuarioReportante;
    }

    public void setUsuarioReportante(Usuario usuarioReportante) {
        this.usuarioReportante = usuarioReportante;
    }

    public Habitacion getHabitacion() {
        return habitacion;
    }

    public void setHabitacion(Habitacion habitacion) {
        this.habitacion = habitacion;
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