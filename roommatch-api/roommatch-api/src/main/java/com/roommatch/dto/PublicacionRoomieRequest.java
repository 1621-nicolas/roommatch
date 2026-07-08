package com.roommatch.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class PublicacionRoomieRequest {

    @NotBlank(
            message = "El tipo de publicación es obligatorio"
    )
    private String tipoPublicacion;


    @NotBlank(
            message = "El título es obligatorio"
    )
    @Size(
            max = 150,
            message = "El título no puede superar los 150 caracteres"
    )
    private String titulo;


    @NotBlank(
            message = "La descripción es obligatoria"
    )
    @Size(
            max = 1000,
            message = "La descripción no puede superar los 1000 caracteres"
    )
    private String descripcion;


    @NotBlank(
            message = "El distrito es obligatorio"
    )
    private String distrito;


    @DecimalMin(
            value = "0.0",
            message = "El presupuesto mínimo no puede ser negativo"
    )
    private BigDecimal presupuestoMin;


    @DecimalMin(
            value = "0.0",
            message = "El presupuesto máximo no puede ser negativo"
    )
    private BigDecimal presupuestoMax;


    /*
     * =========================================================
     * VINCULACIÓN DE VIVIENDA
     * =========================================================
     */

    private String tipoVinculacionVivienda;


    private Integer idHabitacion;


    @Size(
            max = 150,
            message = "El nombre de la vivienda externa no puede superar los 150 caracteres"
    )
    private String viviendaExternaTitulo;


    @Size(
            max = 255,
            message = "La dirección referencial no puede superar los 255 caracteres"
    )
    private String viviendaExternaDireccion;


    @DecimalMin(
            value = "0.0",
            message = "El precio de la vivienda externa no puede ser negativo"
    )
    private BigDecimal viviendaExternaPrecio;


    /*
     * =========================================================
     * GETTERS Y SETTERS
     * =========================================================
     */

    public String getTipoPublicacion() {
        return tipoPublicacion;
    }

    public void setTipoPublicacion(
            String tipoPublicacion
    ) {
        this.tipoPublicacion = tipoPublicacion;
    }


    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(
            String titulo
    ) {
        this.titulo = titulo;
    }


    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(
            String descripcion
    ) {
        this.descripcion = descripcion;
    }


    public String getDistrito() {
        return distrito;
    }

    public void setDistrito(
            String distrito
    ) {
        this.distrito = distrito;
    }


    public BigDecimal getPresupuestoMin() {
        return presupuestoMin;
    }

    public void setPresupuestoMin(
            BigDecimal presupuestoMin
    ) {
        this.presupuestoMin = presupuestoMin;
    }


    public BigDecimal getPresupuestoMax() {
        return presupuestoMax;
    }

    public void setPresupuestoMax(
            BigDecimal presupuestoMax
    ) {
        this.presupuestoMax = presupuestoMax;
    }


    public String getTipoVinculacionVivienda() {
        return tipoVinculacionVivienda;
    }

    public void setTipoVinculacionVivienda(
            String tipoVinculacionVivienda
    ) {
        this.tipoVinculacionVivienda =
                tipoVinculacionVivienda;
    }


    public Integer getIdHabitacion() {
        return idHabitacion;
    }

    public void setIdHabitacion(
            Integer idHabitacion
    ) {
        this.idHabitacion = idHabitacion;
    }


    public String getViviendaExternaTitulo() {
        return viviendaExternaTitulo;
    }

    public void setViviendaExternaTitulo(
            String viviendaExternaTitulo
    ) {
        this.viviendaExternaTitulo =
                viviendaExternaTitulo;
    }


    public String getViviendaExternaDireccion() {
        return viviendaExternaDireccion;
    }

    public void setViviendaExternaDireccion(
            String viviendaExternaDireccion
    ) {
        this.viviendaExternaDireccion =
                viviendaExternaDireccion;
    }


    public BigDecimal getViviendaExternaPrecio() {
        return viviendaExternaPrecio;
    }

    public void setViviendaExternaPrecio(
            BigDecimal viviendaExternaPrecio
    ) {
        this.viviendaExternaPrecio =
                viviendaExternaPrecio;
    }
}