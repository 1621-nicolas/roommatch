package com.roommatch.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "contacto_roomie")
public class ContactoRoomie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_contacto_roomie")
    private Integer idContactoRoomie;

    @OneToOne
    @JoinColumn(name = "id_solicitud", nullable = false, unique = true)
    private SolicitudContacto solicitud;

    @ManyToOne
    @JoinColumn(name = "id_usuario_a", nullable = false)
    private Usuario usuarioA;

    @ManyToOne
    @JoinColumn(name = "id_usuario_b", nullable = false)
    private Usuario usuarioB;

    @Column(name = "fecha_desbloqueo", nullable = false)
    private LocalDateTime fechaDesbloqueo;

    @PrePersist
    public void prePersist() {
        this.fechaDesbloqueo = LocalDateTime.now();
    }

    public Integer getIdContactoRoomie() {
        return idContactoRoomie;
    }

    public void setIdContactoRoomie(Integer idContactoRoomie) {
        this.idContactoRoomie = idContactoRoomie;
    }

    public SolicitudContacto getSolicitud() {
        return solicitud;
    }

    public void setSolicitud(SolicitudContacto solicitud) {
        this.solicitud = solicitud;
    }

    public Usuario getUsuarioA() {
        return usuarioA;
    }

    public void setUsuarioA(Usuario usuarioA) {
        this.usuarioA = usuarioA;
    }

    public Usuario getUsuarioB() {
        return usuarioB;
    }

    public void setUsuarioB(Usuario usuarioB) {
        this.usuarioB = usuarioB;
    }

    public LocalDateTime getFechaDesbloqueo() {
        return fechaDesbloqueo;
    }

    public void setFechaDesbloqueo(LocalDateTime fechaDesbloqueo) {
        this.fechaDesbloqueo = fechaDesbloqueo;
    }
}