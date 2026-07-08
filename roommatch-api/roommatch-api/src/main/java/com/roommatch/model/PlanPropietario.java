package com.roommatch.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "plan_propietario")
public class PlanPropietario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_plan")
    private Integer idPlan;

    @Column(name = "nombre_plan", nullable = false, unique = true, length = 50)
    private String nombrePlan;

    @Column(name = "descripcion", length = 300)
    private String descripcion;

    @Column(name = "precio_mensual", nullable = false)
    private BigDecimal precioMensual;

    @Column(name = "limite_habitaciones", nullable = false)
    private Integer limiteHabitaciones;

    @Column(name = "permite_destacar", nullable = false)
    private Boolean permiteDestacar;

    @Column(name = "permite_estadisticas", nullable = false)
    private Boolean permiteEstadisticas;

    @Column(name = "estado", nullable = false, length = 30)
    private String estado;

    public Integer getIdPlan() {
        return idPlan;
    }

    public void setIdPlan(Integer idPlan) {
        this.idPlan = idPlan;
    }

    public String getNombrePlan() {
        return nombrePlan;
    }

    public void setNombrePlan(String nombrePlan) {
        this.nombrePlan = nombrePlan;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public BigDecimal getPrecioMensual() {
        return precioMensual;
    }

    public void setPrecioMensual(BigDecimal precioMensual) {
        this.precioMensual = precioMensual;
    }

    public Integer getLimiteHabitaciones() {
        return limiteHabitaciones;
    }

    public void setLimiteHabitaciones(Integer limiteHabitaciones) {
        this.limiteHabitaciones = limiteHabitaciones;
    }

    public Boolean getPermiteDestacar() {
        return permiteDestacar;
    }

    public void setPermiteDestacar(Boolean permiteDestacar) {
        this.permiteDestacar = permiteDestacar;
    }

    public Boolean getPermiteEstadisticas() {
        return permiteEstadisticas;
    }

    public void setPermiteEstadisticas(Boolean permiteEstadisticas) {
        this.permiteEstadisticas = permiteEstadisticas;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}