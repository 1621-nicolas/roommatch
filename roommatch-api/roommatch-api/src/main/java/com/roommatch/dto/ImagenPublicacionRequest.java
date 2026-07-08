package com.roommatch.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;


public class ImagenPublicacionRequest {

    @NotBlank(
            message = "La URL de la imagen es obligatoria"
    )
    @Size(
            max = 255,
            message = "La URL no puede superar los 255 caracteres"
    )
    private String urlImagen;


    private Integer orden;


    private Boolean principal;


    public String getUrlImagen() {
        return urlImagen;
    }


    public void setUrlImagen(
            String urlImagen
    ) {

        this.urlImagen =
                urlImagen;
    }


    public Integer getOrden() {
        return orden;
    }


    public void setOrden(
            Integer orden
    ) {

        this.orden =
                orden;
    }


    public Boolean getPrincipal() {
        return principal;
    }


    public void setPrincipal(
            Boolean principal
    ) {

        this.principal =
                principal;
    }
}