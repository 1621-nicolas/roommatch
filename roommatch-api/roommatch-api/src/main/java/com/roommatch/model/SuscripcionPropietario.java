package com.roommatch.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;


@Entity
@Table(name = "suscripcion_propietario")
public class SuscripcionPropietario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_suscripcion")
    private Integer idSuscripcion;


    @ManyToOne
    @JoinColumn(
            name = "id_propietario",
            nullable = false
    )
    private Propietario propietario;


    @ManyToOne
    @JoinColumn(
            name = "id_plan",
            nullable = false
    )
    private PlanPropietario plan;


    @Column(
            name = "fecha_inicio",
            nullable = false
    )
    private LocalDateTime fechaInicio;


    @Column(name = "fecha_fin")
    private LocalDateTime fechaFin;


    @Column(
            name = "estado",
            nullable = false,
            length = 30
    )
    private String estado = "activo";


    /*
     * =========================================================
     * PRE PERSIST
     * =========================================================
     */

    @PrePersist
    public void prePersist() {

        if (this.fechaInicio == null) {

            this.fechaInicio =
                    LocalDateTime.now();
        }


        if (
                this.estado == null ||
                this.estado.isBlank()
        ) {

            this.estado = "activo";
        }
    }


    /*
     * =========================================================
     * GETTERS Y SETTERS
     * =========================================================
     */

    public Integer getIdSuscripcion() {
        return idSuscripcion;
    }

    public void setIdSuscripcion(
            Integer idSuscripcion
    ) {
        this.idSuscripcion = idSuscripcion;
    }


    public Propietario getPropietario() {
        return propietario;
    }

    public void setPropietario(
            Propietario propietario
    ) {
        this.propietario = propietario;
    }


    public PlanPropietario getPlan() {
        return plan;
    }

    public void setPlan(
            PlanPropietario plan
    ) {
        this.plan = plan;
    }


    public LocalDateTime getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(
            LocalDateTime fechaInicio
    ) {
        this.fechaInicio = fechaInicio;
    }


    public LocalDateTime getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(
            LocalDateTime fechaFin
    ) {
        this.fechaFin = fechaFin;
    }


    public String getEstado() {
        return estado;
    }

    public void setEstado(
            String estado
    ) {
        this.estado = estado;
    }
}