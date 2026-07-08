package com.roommatch.dto;

import com.roommatch.model.LeadHabitacion;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class LeadHabitacionResponse {

    private Integer idLead;

    private Integer idHabitacion;
    private String tituloHabitacion;
    private String distrito;
    private BigDecimal precio;

    private Integer idUsuarioInteresado;
    private String nombreInteresado;
    private String emailInteresado;

    private Integer idPropietario;
    private String nombrePropietario;

    private String mensaje;
    private String estado;
    private LocalDateTime fechaLead;

    public static LeadHabitacionResponse fromEntity(LeadHabitacion lead) {
        LeadHabitacionResponse response = new LeadHabitacionResponse();

        response.setIdLead(lead.getIdLead());
        response.setMensaje(lead.getMensaje());
        response.setEstado(lead.getEstado());
        response.setFechaLead(lead.getFechaLead());

        if (lead.getHabitacion() != null) {
            response.setIdHabitacion(lead.getHabitacion().getIdHabitacion());
            response.setTituloHabitacion(lead.getHabitacion().getTitulo());
            response.setDistrito(lead.getHabitacion().getDistrito());
            response.setPrecio(lead.getHabitacion().getPrecio());

            if (lead.getHabitacion().getPropietario() != null) {
                response.setIdPropietario(lead.getHabitacion().getPropietario().getIdPropietario());
                response.setNombrePropietario(lead.getHabitacion().getPropietario().getNombreComercial());
            }
        }

        if (lead.getUsuarioInteresado() != null) {
            response.setIdUsuarioInteresado(lead.getUsuarioInteresado().getIdUsuario());
            response.setNombreInteresado(
                    lead.getUsuarioInteresado().getNombres() + " " +
                    lead.getUsuarioInteresado().getApellidos()
            );
            response.setEmailInteresado(lead.getUsuarioInteresado().getEmail());
        }

        return response;
    }

    public Integer getIdLead() {
        return idLead;
    }

    public void setIdLead(Integer idLead) {
        this.idLead = idLead;
    }

    public Integer getIdHabitacion() {
        return idHabitacion;
    }

    public void setIdHabitacion(Integer idHabitacion) {
        this.idHabitacion = idHabitacion;
    }

    public String getTituloHabitacion() {
        return tituloHabitacion;
    }

    public void setTituloHabitacion(String tituloHabitacion) {
        this.tituloHabitacion = tituloHabitacion;
    }

    public String getDistrito() {
        return distrito;
    }

    public void setDistrito(String distrito) {
        this.distrito = distrito;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    public Integer getIdUsuarioInteresado() {
        return idUsuarioInteresado;
    }

    public void setIdUsuarioInteresado(Integer idUsuarioInteresado) {
        this.idUsuarioInteresado = idUsuarioInteresado;
    }

    public String getNombreInteresado() {
        return nombreInteresado;
    }

    public void setNombreInteresado(String nombreInteresado) {
        this.nombreInteresado = nombreInteresado;
    }

    public String getEmailInteresado() {
        return emailInteresado;
    }

    public void setEmailInteresado(String emailInteresado) {
        this.emailInteresado = emailInteresado;
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

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public LocalDateTime getFechaLead() {
        return fechaLead;
    }

    public void setFechaLead(LocalDateTime fechaLead) {
        this.fechaLead = fechaLead;
    }
}