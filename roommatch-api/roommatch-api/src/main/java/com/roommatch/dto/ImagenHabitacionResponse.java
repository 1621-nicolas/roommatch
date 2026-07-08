package com.roommatch.dto;

import com.roommatch.model.ImagenHabitacion;


public class ImagenHabitacionResponse {

    private Integer idImagen;

    private Integer idHabitacion;

    private String urlImagen;

    private Integer orden;

    private Boolean principal;


    public static ImagenHabitacionResponse fromEntity(
            ImagenHabitacion imagen
    ) {

        ImagenHabitacionResponse response =
                new ImagenHabitacionResponse();


        response.setIdImagen(
                imagen.getIdImagen()
        );


        response.setUrlImagen(
                imagen.getUrlImagen()
        );


        response.setOrden(
                imagen.getOrden()
        );


        response.setPrincipal(
                imagen.getPrincipal()
        );


        if (
                imagen.getHabitacion() != null
        ) {

            response.setIdHabitacion(

                    imagen
                            .getHabitacion()
                            .getIdHabitacion()

            );
        }


        return response;
    }


    public Integer getIdImagen() {
        return idImagen;
    }

    public void setIdImagen(Integer idImagen) {
        this.idImagen = idImagen;
    }


    public Integer getIdHabitacion() {
        return idHabitacion;
    }

    public void setIdHabitacion(Integer idHabitacion) {
        this.idHabitacion = idHabitacion;
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