package com.roommatch.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public class HabitacionRequest {

    @NotBlank(message = "El título es obligatorio")
    @Size(max = 150, message = "El título no puede superar 150 caracteres")
    private String titulo;

    @NotBlank(message = "La descripción es obligatoria")
    @Size(max = 800, message = "La descripción no puede superar 800 caracteres")
    private String descripcion;

    @NotBlank(message = "El distrito es obligatorio")
    @Size(max = 100, message = "El distrito no puede superar 100 caracteres")
    private String distrito;

    @Size(max = 250, message = "La dirección referencial no puede superar 250 caracteres")
    private String direccionReferencial;

    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.0", inclusive = false, message = "El precio debe ser mayor que 0")
    private BigDecimal precio;

    @DecimalMin(value = "0.1", message = "El área debe ser mayor que 0")
    private BigDecimal areaM2;

    private Boolean amoblado;
    private Boolean banoPrivado;
    private Boolean internetIncluido;
    private Boolean aguaIncluida;
    private Boolean luzIncluida;
    private Boolean permiteMascotas;

    private LocalDate disponibleDesde;

    private Boolean destacada;

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDistrito() {
        return distrito;
    }

    public void setDistrito(String distrito) {
        this.distrito = distrito;
    }

    public String getDireccionReferencial() {
        return direccionReferencial;
    }

    public void setDireccionReferencial(String direccionReferencial) {
        this.direccionReferencial = direccionReferencial;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    public BigDecimal getAreaM2() {
        return areaM2;
    }

    public void setAreaM2(BigDecimal areaM2) {
        this.areaM2 = areaM2;
    }

    public Boolean getAmoblado() {
        return amoblado;
    }

    public void setAmoblado(Boolean amoblado) {
        this.amoblado = amoblado;
    }

    public Boolean getBanoPrivado() {
        return banoPrivado;
    }

    public void setBanoPrivado(Boolean banoPrivado) {
        this.banoPrivado = banoPrivado;
    }

    public Boolean getInternetIncluido() {
        return internetIncluido;
    }

    public void setInternetIncluido(Boolean internetIncluido) {
        this.internetIncluido = internetIncluido;
    }

    public Boolean getAguaIncluida() {
        return aguaIncluida;
    }

    public void setAguaIncluida(Boolean aguaIncluida) {
        this.aguaIncluida = aguaIncluida;
    }

    public Boolean getLuzIncluida() {
        return luzIncluida;
    }

    public void setLuzIncluida(Boolean luzIncluida) {
        this.luzIncluida = luzIncluida;
    }

    public Boolean getPermiteMascotas() {
        return permiteMascotas;
    }

    public void setPermiteMascotas(Boolean permiteMascotas) {
        this.permiteMascotas = permiteMascotas;
    }

    public LocalDate getDisponibleDesde() {
        return disponibleDesde;
    }

    public void setDisponibleDesde(LocalDate disponibleDesde) {
        this.disponibleDesde = disponibleDesde;
    }

    public Boolean getDestacada() {
        return destacada;
    }

    public void setDestacada(Boolean destacada) {
        this.destacada = destacada;
    }
}