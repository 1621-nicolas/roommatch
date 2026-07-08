package com.roommatch.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "favorito_usuario")
public class FavoritoUsuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_favorito")
    private Integer idFavorito;

    @ManyToOne
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "id_usuario_favorito", nullable = false)
    private Usuario usuarioFavorito;

    @Column(name = "fecha_favorito", nullable = false)
    private LocalDateTime fechaFavorito;

    @PrePersist
    public void prePersist() {
        this.fechaFavorito = LocalDateTime.now();
    }

    public Integer getIdFavorito() {
        return idFavorito;
    }

    public void setIdFavorito(Integer idFavorito) {
        this.idFavorito = idFavorito;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Usuario getUsuarioFavorito() {
        return usuarioFavorito;
    }

    public void setUsuarioFavorito(Usuario usuarioFavorito) {
        this.usuarioFavorito = usuarioFavorito;
    }

    public LocalDateTime getFechaFavorito() {
        return fechaFavorito;
    }

    public void setFechaFavorito(LocalDateTime fechaFavorito) {
        this.fechaFavorito = fechaFavorito;
    }
}