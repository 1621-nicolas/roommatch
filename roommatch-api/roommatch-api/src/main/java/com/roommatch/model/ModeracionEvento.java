package com.roommatch.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "moderacion_evento")
public class ModeracionEvento {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) @Column(name = "id_evento")
    private Integer idEvento;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "id_admin", nullable = false)
    private Usuario admin;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "id_reporte_usuario")
    private ReporteUsuario reporteUsuario;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "id_reporte_habitacion")
    private ReporteHabitacion reporteHabitacion;
    @Column(nullable = false, length = 30)
    private String accion;
    @Column(nullable = false, length = 500)
    private String motivo;
    @Column(nullable = false)
    private LocalDateTime fecha;
    protected ModeracionEvento() {}
    public ModeracionEvento(Usuario admin, ReporteUsuario userReport, ReporteHabitacion roomReport, String action, String reason, LocalDateTime date) {
        this.admin = admin; reporteUsuario = userReport; reporteHabitacion = roomReport; accion = action; motivo = reason; fecha = date;
    }
    public Integer getIdEvento() { return idEvento; }
    public Usuario getAdmin() { return admin; }
    public String getAccion() { return accion; }
    public String getMotivo() { return motivo; }
    public LocalDateTime getFecha() { return fecha; }
}
