package com.roommatch.dto;

import com.roommatch.model.PerfilConvivencia;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class PerfilConvivenciaResponse {

    private Integer idPerfil;
    private Integer idUsuario;
    private String usuario;
    private BigDecimal presupuestoMin;
    private BigDecimal presupuestoMax;
    private String distritoPreferido;
    private LocalDate fechaMudanza;
    private Integer limpieza;
    private Integer ruido;
    private Integer sociabilidad;
    private String horario;
    private String visitas;
    private String mascotas;
    private String fumar;
    private String alcohol;
    private String gastos;
    private String convivencia;
    private String descripcionPersonal;
    private Boolean perfilCompleto;
    private LocalDateTime fechaActualizacion;

    public static PerfilConvivenciaResponse fromEntity(PerfilConvivencia perfil) {
        PerfilConvivenciaResponse response = new PerfilConvivenciaResponse();

        response.setIdPerfil(perfil.getIdPerfil());

        if (perfil.getUsuario() != null) {
            response.setIdUsuario(perfil.getUsuario().getIdUsuario());
            response.setUsuario(perfil.getUsuario().getNombres() + " " + perfil.getUsuario().getApellidos());
        }

        response.setPresupuestoMin(perfil.getPresupuestoMin());
        response.setPresupuestoMax(perfil.getPresupuestoMax());
        response.setDistritoPreferido(perfil.getDistritoPreferido());
        response.setFechaMudanza(perfil.getFechaMudanza());
        response.setLimpieza(perfil.getLimpieza());
        response.setRuido(perfil.getRuido());
        response.setSociabilidad(perfil.getSociabilidad());
        response.setHorario(perfil.getHorario());
        response.setVisitas(perfil.getVisitas());
        response.setMascotas(perfil.getMascotas());
        response.setFumar(perfil.getFumar());
        response.setAlcohol(perfil.getAlcohol());
        response.setGastos(perfil.getGastos());
        response.setConvivencia(perfil.getConvivencia());
        response.setDescripcionPersonal(perfil.getDescripcionPersonal());
        response.setPerfilCompleto(perfil.getPerfilCompleto());
        response.setFechaActualizacion(perfil.getFechaActualizacion());

        return response;
    }

    public Integer getIdPerfil() {
        return idPerfil;
    }

    public void setIdPerfil(Integer idPerfil) {
        this.idPerfil = idPerfil;
    }

    public Integer getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Integer idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public BigDecimal getPresupuestoMin() {
        return presupuestoMin;
    }

    public void setPresupuestoMin(BigDecimal presupuestoMin) {
        this.presupuestoMin = presupuestoMin;
    }

    public BigDecimal getPresupuestoMax() {
        return presupuestoMax;
    }

    public void setPresupuestoMax(BigDecimal presupuestoMax) {
        this.presupuestoMax = presupuestoMax;
    }

    public String getDistritoPreferido() {
        return distritoPreferido;
    }

    public void setDistritoPreferido(String distritoPreferido) {
        this.distritoPreferido = distritoPreferido;
    }

    public LocalDate getFechaMudanza() {
        return fechaMudanza;
    }

    public void setFechaMudanza(LocalDate fechaMudanza) {
        this.fechaMudanza = fechaMudanza;
    }

    public Integer getLimpieza() {
        return limpieza;
    }

    public void setLimpieza(Integer limpieza) {
        this.limpieza = limpieza;
    }

    public Integer getRuido() {
        return ruido;
    }

    public void setRuido(Integer ruido) {
        this.ruido = ruido;
    }

    public Integer getSociabilidad() {
        return sociabilidad;
    }

    public void setSociabilidad(Integer sociabilidad) {
        this.sociabilidad = sociabilidad;
    }

    public String getHorario() {
        return horario;
    }

    public void setHorario(String horario) {
        this.horario = horario;
    }

    public String getVisitas() {
        return visitas;
    }

    public void setVisitas(String visitas) {
        this.visitas = visitas;
    }

    public String getMascotas() {
        return mascotas;
    }

    public void setMascotas(String mascotas) {
        this.mascotas = mascotas;
    }

    public String getFumar() {
        return fumar;
    }

    public void setFumar(String fumar) {
        this.fumar = fumar;
    }

    public String getAlcohol() {
        return alcohol;
    }

    public void setAlcohol(String alcohol) {
        this.alcohol = alcohol;
    }

    public String getGastos() {
        return gastos;
    }

    public void setGastos(String gastos) {
        this.gastos = gastos;
    }

    public String getConvivencia() {
        return convivencia;
    }

    public void setConvivencia(String convivencia) {
        this.convivencia = convivencia;
    }

    public String getDescripcionPersonal() {
        return descripcionPersonal;
    }

    public void setDescripcionPersonal(String descripcionPersonal) {
        this.descripcionPersonal = descripcionPersonal;
    }

    public Boolean getPerfilCompleto() {
        return perfilCompleto;
    }

    public void setPerfilCompleto(Boolean perfilCompleto) {
        this.perfilCompleto = perfilCompleto;
    }

    public LocalDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }

    public void setFechaActualizacion(LocalDateTime fechaActualizacion) {
        this.fechaActualizacion = fechaActualizacion;
    }
}