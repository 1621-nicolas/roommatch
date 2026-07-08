package com.roommatch.dto;

import com.roommatch.model.PlanPropietario;

import java.math.BigDecimal;


public class PlanPropietarioResponse {

    private Integer idPlan;

    private String nombrePlan;

    private String descripcion;

    private BigDecimal precioMensual;

    private Integer limiteHabitaciones;

    private Boolean permiteDestacar;

    private Boolean permiteEstadisticas;

    private String estado;


    public static PlanPropietarioResponse fromEntity(
            PlanPropietario plan
    ) {

        PlanPropietarioResponse response =
                new PlanPropietarioResponse();


        response.setIdPlan(
                plan.getIdPlan()
        );

        response.setNombrePlan(
                plan.getNombrePlan()
        );

        response.setDescripcion(
                plan.getDescripcion()
        );

        response.setPrecioMensual(
                plan.getPrecioMensual()
        );

        response.setLimiteHabitaciones(
                plan.getLimiteHabitaciones()
        );

        response.setPermiteDestacar(
                plan.getPermiteDestacar()
        );

        response.setPermiteEstadisticas(
                plan.getPermiteEstadisticas()
        );

        response.setEstado(
                plan.getEstado()
        );


        return response;
    }


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