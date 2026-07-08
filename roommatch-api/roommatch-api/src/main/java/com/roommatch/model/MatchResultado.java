package com.roommatch.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "match_resultado")
public class MatchResultado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_match")
    private Integer idMatch;

    @ManyToOne
    @JoinColumn(name = "id_usuario_origen", nullable = false)
    private Usuario usuarioOrigen;

    @ManyToOne
    @JoinColumn(name = "id_usuario_destino", nullable = false)
    private Usuario usuarioDestino;

    @Column(name = "porcentaje", nullable = false)
    private BigDecimal porcentaje;

    @Column(name = "coincidencias", length = 800)
    private String coincidencias;

    @Column(name = "diferencias", length = 800)
    private String diferencias;

    @Column(name = "fecha_calculo", nullable = false)
    private LocalDateTime fechaCalculo;

    @PrePersist
    public void prePersist() {
        this.fechaCalculo = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.fechaCalculo = LocalDateTime.now();
    }

    public Integer getIdMatch() {
        return idMatch;
    }

    public void setIdMatch(Integer idMatch) {
        this.idMatch = idMatch;
    }

    public Usuario getUsuarioOrigen() {
        return usuarioOrigen;
    }

    public void setUsuarioOrigen(Usuario usuarioOrigen) {
        this.usuarioOrigen = usuarioOrigen;
    }

    public Usuario getUsuarioDestino() {
        return usuarioDestino;
    }

    public void setUsuarioDestino(Usuario usuarioDestino) {
        this.usuarioDestino = usuarioDestino;
    }

    public BigDecimal getPorcentaje() {
        return porcentaje;
    }

    public void setPorcentaje(BigDecimal porcentaje) {
        this.porcentaje = porcentaje;
    }

    public String getCoincidencias() {
        return coincidencias;
    }

    public void setCoincidencias(String coincidencias) {
        this.coincidencias = coincidencias;
    }

    public String getDiferencias() {
        return diferencias;
    }

    public void setDiferencias(String diferencias) {
        this.diferencias = diferencias;
    }

    public LocalDateTime getFechaCalculo() {
        return fechaCalculo;
    }

    public void setFechaCalculo(LocalDateTime fechaCalculo) {
        this.fechaCalculo = fechaCalculo;
    }
}