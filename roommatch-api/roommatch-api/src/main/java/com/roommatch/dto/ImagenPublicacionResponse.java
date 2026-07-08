package com.roommatch.dto;

import com.roommatch.model.ImagenPublicacion;

public class ImagenPublicacionResponse {

    private Integer idImagen;
    private Integer idPublicacion;
    private String urlImagen;
    private Integer orden;
    private Boolean principal;

    public static ImagenPublicacionResponse fromEntity(ImagenPublicacion imagen) {
        ImagenPublicacionResponse response = new ImagenPublicacionResponse();

        response.setIdImagen(imagen.getIdImagen());
        response.setUrlImagen(imagen.getUrlImagen());
        response.setOrden(imagen.getOrden());
        response.setPrincipal(imagen.getPrincipal());

        if (imagen.getPublicacion() != null) {
            response.setIdPublicacion(imagen.getPublicacion().getIdPublicacion());
        }

        return response;
    }

    public Integer getIdImagen() {
        return idImagen;
    }

    public void setIdImagen(Integer idImagen) {
        this.idImagen = idImagen;
    }

    public Integer getIdPublicacion() {
        return idPublicacion;
    }

    public void setIdPublicacion(Integer idPublicacion) {
        this.idPublicacion = idPublicacion;
    }

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