package com.roommatch.dto;

import com.roommatch.model.MatchResultado;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class MatchResponse {

    private Integer idMatch;
    private Integer idUsuarioDestino;
    private String nombres;
    private String apellidos;
    private String email;
    private Integer edad;
    private String ocupacion;
    private String universidad;
    private String foto;
    private BigDecimal porcentaje;
    private String coincidencias;
    private String diferencias;
    private LocalDateTime fechaCalculo;

    public static MatchResponse fromEntity(MatchResultado match) {
        MatchResponse response = new MatchResponse();

        response.setIdMatch(match.getIdMatch());

        if (match.getUsuarioDestino() != null) {
            response.setIdUsuarioDestino(match.getUsuarioDestino().getIdUsuario());
            response.setNombres(match.getUsuarioDestino().getNombres());
            response.setApellidos(match.getUsuarioDestino().getApellidos());
            response.setEmail(match.getUsuarioDestino().getEmail());
            response.setEdad(match.getUsuarioDestino().getEdad());
            response.setOcupacion(match.getUsuarioDestino().getOcupacion());
            response.setUniversidad(match.getUsuarioDestino().getUniversidad());
            response.setFoto(match.getUsuarioDestino().getFoto());
        }

        response.setPorcentaje(match.getPorcentaje());
        response.setCoincidencias(match.getCoincidencias());
        response.setDiferencias(match.getDiferencias());
        response.setFechaCalculo(match.getFechaCalculo());

        return response;
    }

    public Integer getIdMatch() {
        return idMatch;
    }

    public void setIdMatch(Integer idMatch) {
        this.idMatch = idMatch;
    }

    public Integer getIdUsuarioDestino() {
        return idUsuarioDestino;
    }

    public void setIdUsuarioDestino(Integer idUsuarioDestino) {
        this.idUsuarioDestino = idUsuarioDestino;
    }

    public String getNombres() {
        return nombres;
    }

    public void setNombres(String nombres) {
        this.nombres = nombres;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
  
    public Integer getEdad() {
        return edad;
    }

    public void setEdad(Integer edad) {
        this.edad = edad;
    }

    public String getOcupacion() {
        return ocupacion;
    }

    public void setOcupacion(String ocupacion) {
        this.ocupacion = ocupacion;
    }

    public String getUniversidad() {
        return universidad;
    }

    public void setUniversidad(String universidad) {
        this.universidad = universidad;
    }

    public String getFoto() {
        return foto;
    }

    public void setFoto(String foto) {
        this.foto = foto;
    }

    public BigDecimal getPorcentaje() {
        return porcentaje;
    }

    public void setPorcentaje(BigDecimal porcentaje) {
        this.porcentaje = porcentaje;
    }

    public String getCoincidencias() {
        return coincidencias;
    }

    public void setCoincidencias(String coincidencias) {
        this.coincidencias = coincidencias;
    }

    public String getDiferencias() {
        return diferencias;
    }

    public void setDiferencias(String diferencias) {
        this.diferencias = diferencias;
    }

    public LocalDateTime getFechaCalculo() {
        return fechaCalculo;
    }

    public void setFechaCalculo(LocalDateTime fechaCalculo) {
        this.fechaCalculo = fechaCalculo;
    }
}