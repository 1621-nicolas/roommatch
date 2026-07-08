package com.roommatch.dto;

import com.roommatch.model.PlanPropietario;
import com.roommatch.model.SuscripcionPropietario;

import java.math.BigDecimal;
import java.time.LocalDateTime;


public class MiPlanResponse {

    private Integer idSuscripcion;

    private Integer idPropietario;

    private Integer idPlan;

    private String nombrePlan;

    private String descripcionPlan;

    private BigDecimal precioMensual;

    private Integer limiteHabitaciones;

    private Boolean permiteDestacar;

    private Boolean permiteEstadisticas;

    private String estadoSuscripcion;

    private LocalDateTime fechaInicio;

    private LocalDateTime fechaFin;


    public static MiPlanResponse fromEntity(
            SuscripcionPropietario suscripcion
    ) {

        MiPlanResponse response =
                new MiPlanResponse();


        response.setIdSuscripcion(
                suscripcion.getIdSuscripcion()
        );

        response.setEstadoSuscripcion(
                suscripcion.getEstado()
        );

        response.setFechaInicio(
                suscripcion.getFechaInicio()
        );

        response.setFechaFin(
                suscripcion.getFechaFin()
        );


        if (
                suscripcion.getPropietario() != null
        ) {

            response.setIdPropietario(

                    suscripcion
                            .getPropietario()
                            .getIdPropietario()

            );
        }


        PlanPropietario plan =
                suscripcion.getPlan();


        if (plan != null) {

            response.setIdPlan(
                    plan.getIdPlan()
            );

            response.setNombrePlan(
                    plan.getNombrePlan()
            );

            response.setDescripcionPlan(
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
        }


        return response;
    }


    public Integer getIdSuscripcion() {
        return idSuscripcion;
    }

    public void setIdSuscripcion(Integer idSuscripcion) {
        this.idSuscripcion = idSuscripcion;
    }


    public Integer getIdPropietario() {
        return idPropietario;
    }

    public void setIdPropietario(Integer idPropietario) {
        this.idPropietario = idPropietario;
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


    public String getDescripcionPlan() {
        return descripcionPlan;
    }

    public void setDescripcionPlan(String descripcionPlan) {
        this.descripcionPlan = descripcionPlan;
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


    public String getEstadoSuscripcion() {
        return estadoSuscripcion;
    }

    public void setEstadoSuscripcion(String estadoSuscripcion) {
        this.estadoSuscripcion = estadoSuscripcion;
    }


    public LocalDateTime getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDateTime fechaInicio) {
        this.fechaInicio = fechaInicio;
    }


    public LocalDateTime getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDateTime fechaFin) {
        this.fechaFin = fechaFin;
    }
}