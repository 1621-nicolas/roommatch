package com.roommatch.model;

import jakarta.persistence.*;

@Entity
@Table(name = "imagen_publicacion")
public class ImagenPublicacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_imagen")
    private Integer idImagen;

    @ManyToOne
    @JoinColumn(name = "id_publicacion", nullable = false)
    private PublicacionRoomie publicacion;

    @Column(name = "url_imagen", nullable = false, length = 255)
    private String urlImagen;

    @Column(name = "orden", nullable = false)
    private Integer orden = 1;

    @Column(name = "principal", nullable = false)
    private Boolean principal = false;

    public Integer getIdImagen() {
        return idImagen;
    }

    public void setIdImagen(Integer idImagen) {
        this.idImagen = idImagen;
    }

    public PublicacionRoomie getPublicacion() {
        return publicacion;
    }

    public void setPublicacion(PublicacionRoomie publicacion) {
        this.publicacion = publicacion;
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