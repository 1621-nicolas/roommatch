package com.roommatch.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ImagenHabitacionRequest {

    @NotBlank(message = "La URL de la imagen es obligatoria")
    @Size(max = 255, message = "La URL no puede superar 255 caracteres")
    private String urlImagen;

    @Min(value = 1, message = "El orden debe ser mínimo 1")
    private Integer orden;

    private Boolean principal;

    public String getUrlImagen() {
        return urlImagen;
    }

    public void setUrlImagen(String urlImagen) {
        this.urlImagen = urlImagen;
    }

    public Integer getOrden() {
        return orden;
    }

    public void setOrden(Integer orden) {
        this.orden = orden;
    }

    public Boolean getPrincipal() {
        return principal;
    }

    public void setPrincipal(Boolean principal) {
        this.principal = principal;
    }
}