package com.roommatch.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "lead_habitacion")
public class LeadHabitacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_lead")
    private Integer idLead;

    @ManyToOne
    @JoinColumn(name = "id_habitacion", nullable = false)
    private Habitacion habitacion;

    @ManyToOne
    @JoinColumn(name = "id_usuario_interesado", nullable = false)
    private Usuario usuarioInteresado;

    @Column(name = "mensaje", length = 500)
    private String mensaje;

    @Column(name = "estado", nullable = false, length = 30)
    private String estado = "pendiente";

    @Column(name = "fecha_lead", nullable = false)
    private LocalDateTime fechaLead;

    @PrePersist
    public void prePersist() {
        this.fechaLead = LocalDateTime.now();
        this.estado = "pendiente";
    }

    public Integer getIdLead() {
        return idLead;
    }

    public void setIdLead(Integer idLead) {
        this.idLead = idLead;
    }

    public Habitacion getHabitacion() {
        return habitacion;
    }

    public void setHabitacion(Habitacion habitacion) {
        this.habitacion = habitacion;
    }

    public Usuario getUsuarioInteresado() {
        return usuarioInteresado;
    }

    public void setUsuarioInteresado(Usuario usuarioInteresado) {
        this.usuarioInteresado = usuarioInteresado;
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

    public LocalDateTime getFechaLead() {
        return fechaLead;
    }

    public void setFechaLead(LocalDateTime fechaLead) {
        this.fechaLead = fechaLead;
    }
}