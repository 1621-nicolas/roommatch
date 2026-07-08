package com.roommatch.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class PropietarioRequest {

    @NotBlank(message = "El tipo de propietario es obligatorio")
    private String tipoPropietario;

    @Size(max = 150, message = "El nombre comercial no puede superar 150 caracteres")
    private String nombreComercial;

    @Size(max = 20, message = "El RUC no puede superar 20 caracteres")
    private String ruc;

    @Size(max = 500, message = "La descripción no puede superar 500 caracteres")
    private String descripcion;

    public String getTipoPropietario() {
        return tipoPropietario;
    }

    public void setTipoPropietario(String tipoPropietario) {
        this.tipoPropietario = tipoPropietario;
    }

    public String getNombreComercial() {
        return nombreComercial;
    }

    public void setNombreComercial(String nombreComercial) {
        this.nombreComercial = nombreComercial;
    }

    public String getRuc() {
        return ruc;
    }

    public void setRuc(String ruc) {
        this.ruc = ruc;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}