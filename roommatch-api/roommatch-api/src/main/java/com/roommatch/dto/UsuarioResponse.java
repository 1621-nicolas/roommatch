package com.roommatch.dto;

import com.roommatch.model.Usuario;

import java.time.LocalDateTime;

public class UsuarioResponse {

    private Integer idUsuario;
    private String nombres;
    private String apellidos;
    private String email;
    private Integer edad;
    private String ocupacion;
    private String universidad;
    private String foto;
    private String estado;
    private String rol;
    private LocalDateTime fechaRegistro;

    public UsuarioResponse() {
    }

    public static UsuarioResponse fromEntity(Usuario usuario) {
        UsuarioResponse response = new UsuarioResponse();

        response.setIdUsuario(usuario.getIdUsuario());
        response.setNombres(usuario.getNombres());
        response.setApellidos(usuario.getApellidos());
        response.setEmail(usuario.getEmail());
        response.setEdad(usuario.getEdad());
        response.setOcupacion(usuario.getOcupacion());
        response.setUniversidad(usuario.getUniversidad());
        response.setFoto(usuario.getFoto());
        response.setEstado(usuario.getEstado());

        if (usuario.getRol() != null) {
            response.setRol(usuario.getRol().getNombreRol());
        }

        response.setFechaRegistro(usuario.getFechaRegistro());

        return response;
    }

    public Integer getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Integer idUsuario) {
        this.idUsuario = idUsuario;
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

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }
}