package com.roommatch.dto;

import com.roommatch.model.PlanPropietario;
import com.roommatch.model.Propietario;
import com.roommatch.model.SuscripcionPropietario;

import java.math.BigDecimal;
import java.time.LocalDateTime;


public class PropietarioResponse {

    /*
     * =========================================================
     * DATOS DEL PROPIETARIO
     * =========================================================
     */

    private Integer idPropietario;

    private Integer idUsuario;

    private String nombreUsuario;

    private String email;

    private String tipoPropietario;

    private String nombreComercial;

    private String ruc;

    private String descripcion;

    private Boolean verificado;

    private String estado;

    private LocalDateTime fechaRegistro;


    /*
     * =========================================================
     * DATOS DEL PLAN
     * =========================================================
     */

    private String planActual;

    private Integer idPlan;

    private String nombrePlan;

    private BigDecimal precioMensual;

    private Integer limiteHabitaciones;

    private Boolean permiteDestacar;

    private Boolean permiteEstadisticas;


    /*
     * =========================================================
     * DATOS DE LA SUSCRIPCIÓN
     * =========================================================
     */

    private String estadoSuscripcion;

    private LocalDateTime fechaInicioPlan;

    private LocalDateTime fechaFinPlan;


    /*
     * =========================================================
     * CONVERSIÓN DE ENTIDAD A DTO
     * =========================================================
     */

    public static PropietarioResponse fromEntity(
            Propietario propietario,
            SuscripcionPropietario suscripcion
    ) {

        PropietarioResponse response =
                new PropietarioResponse();


        /*
         * =====================================================
         * PROPIETARIO
         * =====================================================
         */

        response.setIdPropietario(
                propietario.getIdPropietario()
        );

        response.setTipoPropietario(
                propietario.getTipoPropietario()
        );

        response.setNombreComercial(
                propietario.getNombreComercial()
        );

        response.setRuc(
                propietario.getRuc()
        );

        response.setDescripcion(
                propietario.getDescripcion()
        );

        response.setVerificado(
                propietario.getVerificado()
        );

        response.setEstado(
                propietario.getEstado()
        );

        response.setFechaRegistro(
                propietario.getFechaRegistro()
        );


        /*
         * =====================================================
         * USUARIO
         * =====================================================
         */

        if (
                propietario.getUsuario() != null
        ) {

            response.setIdUsuario(
                    propietario
                            .getUsuario()
                            .getIdUsuario()
            );


            String nombres =
                    propietario
                            .getUsuario()
                            .getNombres();


            String apellidos =
                    propietario
                            .getUsuario()
                            .getApellidos();


            response.setNombreUsuario(
                    (
                            (nombres != null ? nombres : "")
                                    + " "
                                    + (apellidos != null ? apellidos : "")
                    ).trim()
            );


            response.setEmail(
                    propietario
                            .getUsuario()
                            .getEmail()
            );
        }


        /*
         * =====================================================
         * SUSCRIPCIÓN Y PLAN
         * =====================================================
         */

        if (
                suscripcion != null
        ) {

            response.setEstadoSuscripcion(
                    suscripcion.getEstado()
            );

            response.setFechaInicioPlan(
                    suscripcion.getFechaInicio()
            );

            response.setFechaFinPlan(
                    suscripcion.getFechaFin()
            );


            PlanPropietario plan =
                    suscripcion.getPlan();


            if (
                    plan != null
            ) {

                response.setPlanActual(
                        plan.getNombrePlan()
                );

                response.setIdPlan(
                        plan.getIdPlan()
                );

                response.setNombrePlan(
                        plan.getNombrePlan()
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
        }


        return response;
    }


    /*
     * =========================================================
     * GETTERS Y SETTERS
     * =========================================================
     */

    public Integer getIdPropietario() {
        return idPropietario;
    }

    public void setIdPropietario(
            Integer idPropietario
    ) {
        this.idPropietario = idPropietario;
    }


    public Integer getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(
            Integer idUsuario
    ) {
        this.idUsuario = idUsuario;
    }


    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(
            String nombreUsuario
    ) {
        this.nombreUsuario = nombreUsuario;
    }


    public String getEmail() {
        return email;
    }

    public void setEmail(
            String email
    ) {
        this.email = email;
    }


    public String getTipoPropietario() {
        return tipoPropietario;
    }

    public void setTipoPropietario(
            String tipoPropietario
    ) {
        this.tipoPropietario = tipoPropietario;
    }


    public String getNombreComercial() {
        return nombreComercial;
    }

    public void setNombreComercial(
            String nombreComercial
    ) {
        this.nombreComercial = nombreComercial;
    }


    public String getRuc() {
        return ruc;
    }

    public void setRuc(
            String ruc
    ) {
        this.ruc = ruc;
    }


    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(
            String descripcion
    ) {
        this.descripcion = descripcion;
    }


    public Boolean getVerificado() {
        return verificado;
    }

    public void setVerificado(
            Boolean verificado
    ) {
        this.verificado = verificado;
    }


    public String getEstado() {
        return estado;
    }

    public void setEstado(
            String estado
    ) {
        this.estado = estado;
    }


    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(
            LocalDateTime fechaRegistro
    ) {
        this.fechaRegistro = fechaRegistro;
    }


    public String getPlanActual() {
        return planActual;
    }

    public void setPlanActual(
            String planActual
    ) {
        this.planActual = planActual;
    }


    public Integer getIdPlan() {
        return idPlan;
    }

    public void setIdPlan(
            Integer idPlan
    ) {
        this.idPlan = idPlan;
    }


    public String getNombrePlan() {
        return nombrePlan;
    }

    public void setNombrePlan(
            String nombrePlan
    ) {
        this.nombrePlan = nombrePlan;
    }


    public BigDecimal getPrecioMensual() {
        return precioMensual;
    }

    public void setPrecioMensual(
            BigDecimal precioMensual
    ) {
        this.precioMensual = precioMensual;
    }


    public Integer getLimiteHabitaciones() {
        return limiteHabitaciones;
    }

    public void setLimiteHabitaciones(
            Integer limiteHabitaciones
    ) {
        this.limiteHabitaciones = limiteHabitaciones;
    }


    public Boolean getPermiteDestacar() {
        return permiteDestacar;
    }

    public void setPermiteDestacar(
            Boolean permiteDestacar
    ) {
        this.permiteDestacar = permiteDestacar;
    }


    public Boolean getPermiteEstadisticas() {
        return permiteEstadisticas;
    }

    public void setPermiteEstadisticas(
            Boolean permiteEstadisticas
    ) {
        this.permiteEstadisticas = permiteEstadisticas;
    }


    public String getEstadoSuscripcion() {
        return estadoSuscripcion;
    }

    public void setEstadoSuscripcion(
            String estadoSuscripcion
    ) {
        this.estadoSuscripcion = estadoSuscripcion;
    }


    public LocalDateTime getFechaInicioPlan() {
        return fechaInicioPlan;
    }

    public void setFechaInicioPlan(
            LocalDateTime fechaInicioPlan
    ) {
        this.fechaInicioPlan = fechaInicioPlan;
    }


    public LocalDateTime getFechaFinPlan() {
        return fechaFinPlan;
    }

    public void setFechaFinPlan(
            LocalDateTime fechaFinPlan
    ) {
        this.fechaFinPlan = fechaFinPlan;
    }
}