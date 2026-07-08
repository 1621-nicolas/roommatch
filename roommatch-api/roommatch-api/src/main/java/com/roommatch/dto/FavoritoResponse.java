package com.roommatch.dto;

import com.roommatch.model.FavoritoUsuario;

import java.time.LocalDateTime;

public class FavoritoResponse {

    private Integer idFavorito;
    private Integer idUsuarioFavorito;
    private String nombres;
    private String apellidos;
    private String email;
    private Integer edad;
    private String ocupacion;
    private String universidad;
    private String foto;
    private LocalDateTime fechaFavorito;

    public static FavoritoResponse fromEntity(FavoritoUsuario favorito) {
        FavoritoResponse response = new FavoritoResponse();

        response.setIdFavorito(favorito.getIdFavorito());
        response.setFechaFavorito(favorito.getFechaFavorito());

        if (favorito.getUsuarioFavorito() != null) {
            response.setIdUsuarioFavorito(favorito.getUsuarioFavorito().getIdUsuario());
            response.setNombres(favorito.getUsuarioFavorito().getNombres());
            response.setApellidos(favorito.getUsuarioFavorito().getApellidos());
            response.setEmail(favorito.getUsuarioFavorito().getEmail());
            response.setEdad(favorito.getUsuarioFavorito().getEdad());
            response.setOcupacion(favorito.getUsuarioFavorito().getOcupacion());
            response.setUniversidad(favorito.getUsuarioFavorito().getUniversidad());
            response.setFoto(favorito.getUsuarioFavorito().getFoto());
        }

        return response;
    }

    public Integer getIdFavorito() {
        return idFavorito;
    }

    public void setIdFavorito(Integer idFavorito) {
        this.idFavorito = idFavorito;
    }

    public Integer getIdUsuarioFavorito() {
        return idUsuarioFavorito;
    }

    public void setIdUsuarioFavorito(Integer idUsuarioFavorito) {
        this.idUsuarioFavorito = idUsuarioFavorito;
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

    public LocalDateTime getFechaFavorito() {
        return fechaFavorito;
    }

    public void setFechaFavorito(LocalDateTime fechaFavorito) {
        this.fechaFavorito = fechaFavorito;
    }
}