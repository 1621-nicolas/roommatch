package com.roommatch.dto;

import com.roommatch.model.Habitacion;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class HabitacionResponse {

    private Integer idHabitacion;
    private Integer idPropietario;
    private String nombrePropietario;
    private Boolean propietarioVerificado;

    private String titulo;
    private String descripcion;
    private String distrito;
    private String direccionReferencial;
    private BigDecimal precio;
    private BigDecimal areaM2;

    private Boolean amoblado;
    private Boolean banoPrivado;
    private Boolean internetIncluido;
    private Boolean aguaIncluida;
    private Boolean luzIncluida;
    private Boolean permiteMascotas;

    private LocalDate disponibleDesde;
    private Boolean destacada;
    private String estado;
    private LocalDateTime fechaPublicacion;
    private LocalDateTime fechaActualizacion;

    public static HabitacionResponse fromEntity(Habitacion habitacion) {
        HabitacionResponse response = new HabitacionResponse();

        response.setIdHabitacion(habitacion.getIdHabitacion());

        if (habitacion.getPropietario() != null) {
            response.setIdPropietario(habitacion.getPropietario().getIdPropietario());
            response.setNombrePropietario(habitacion.getPropietario().getNombreComercial());
            response.setPropietarioVerificado(habitacion.getPropietario().getVerificado());
        }

        response.setTitulo(habitacion.getTitulo());
        response.setDescripcion(habitacion.getDescripcion());
        response.setDistrito(habitacion.getDistrito());
        response.setDireccionReferencial(habitacion.getDireccionReferencial());
        response.setPrecio(habitacion.getPrecio());
        response.setAreaM2(habitacion.getAreaM2());
        response.setAmoblado(habitacion.getAmoblado());
        response.setBanoPrivado(habitacion.getBanoPrivado());
        response.setInternetIncluido(habitacion.getInternetIncluido());
        response.setAguaIncluida(habitacion.getAguaIncluida());
        response.setLuzIncluida(habitacion.getLuzIncluida());
        response.setPermiteMascotas(habitacion.getPermiteMascotas());
        response.setDisponibleDesde(habitacion.getDisponibleDesde());
        response.setDestacada(habitacion.getDestacada());
        response.setEstado(habitacion.getEstado());
        response.setFechaPublicacion(habitacion.getFechaPublicacion());
        response.setFechaActualizacion(habitacion.getFechaActualizacion());

        return response;
    }

    public Integer getIdHabitacion() {
        return idHabitacion;
    }

    public void setIdHabitacion(Integer idHabitacion) {
        this.idHabitacion = idHabitacion;
    }

    public Integer getIdPropietario() {
        return idPropietario;
    }

    public void setIdPropietario(Integer idPropietario) {
        this.idPropietario = idPropietario;
    }

    public String getNombrePropietario() {
        return nombrePropietario;
    }

    public void setNombrePropietario(String nombrePropietario) {
        this.nombrePropietario = nombrePropietario;
    }

    public Boolean getPropietarioVerificado() {
        return propietarioVerificado;
    }

    public void setPropietarioVerificado(Boolean propietarioVerificado) {
        this.propietarioVerificado = propietarioVerificado;
    }

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

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public LocalDateTime getFechaPublicacion() {
        return fechaPublicacion;
    }

    public void setFechaPublicacion(LocalDateTime fechaPublicacion) {
        this.fechaPublicacion = fechaPublicacion;
    }

    public LocalDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }

    public void setFechaActualizacion(LocalDateTime fechaActualizacion) {
        this.fechaActualizacion = fechaActualizacion;
    }
}