package com.roommatch.dto;

public class LoginResponse {

    private String token;
    private String tipoToken;
    private UsuarioResponse usuario;

    public LoginResponse() {
    }

    public LoginResponse(String token, String tipoToken, UsuarioResponse usuario) {
        this.token = token;
        this.tipoToken = tipoToken;
        this.usuario = usuario;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getTipoToken() {
        return tipoToken;
    }

    public void setTipoToken(String tipoToken) {
        this.tipoToken = tipoToken;
    }

    public UsuarioResponse getUsuario() {
        return usuario;
    }

    public void setUsuario(UsuarioResponse usuario) {
        this.usuario = usuario;
    }
}