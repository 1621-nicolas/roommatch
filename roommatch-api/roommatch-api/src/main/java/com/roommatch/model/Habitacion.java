package com.roommatch.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "habitacion")
public class Habitacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_habitacion")
    private Integer idHabitacion;

    @ManyToOne
    @JoinColumn(name = "id_propietario", nullable = false)
    private Propietario propietario;

    @Column(name = "titulo", nullable = false, length = 150)
    private String titulo;

    @Column(name = "descripcion", nullable = false, length = 800)
    private String descripcion;

    @Column(name = "distrito", nullable = false, length = 100)
    private String distrito;

    @Column(name = "direccion_referencial", length = 250)
    private String direccionReferencial;

    @Column(name = "precio", nullable = false)
    private BigDecimal precio;

    @Column(name = "area_m2")
    private BigDecimal areaM2;

    @Column(name = "amoblado", nullable = false)
    private Boolean amoblado = false;

    @Column(name = "bano_privado", nullable = false)
    private Boolean banoPrivado = false;

    @Column(name = "internet_incluido", nullable = false)
    private Boolean internetIncluido = false;

    @Column(name = "agua_incluida", nullable = false)
    private Boolean aguaIncluida = false;

    @Column(name = "luz_incluida", nullable = false)
    private Boolean luzIncluida = false;

    @Column(name = "permite_mascotas", nullable = false)
    private Boolean permiteMascotas = false;

    @Column(name = "disponible_desde")
    private LocalDate disponibleDesde;

    @Column(name = "destacada", nullable = false)
    private Boolean destacada = false;

    @Column(name = "estado", nullable = false, length = 30)
    private String estado = "activa";

    @Column(name = "fecha_publicacion", nullable = false)
    private LocalDateTime fechaPublicacion;

    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion;

    @PrePersist
    public void prePersist() {
        this.fechaPublicacion = LocalDateTime.now();
        this.fechaActualizacion = LocalDateTime.now();
        this.estado = "activa";
    }

    @PreUpdate
    public void preUpdate() {
        this.fechaActualizacion = LocalDateTime.now();
    }

    public Integer getIdHabitacion() {
        return idHabitacion;
    }

    public void setIdHabitacion(Integer idHabitacion) {
        this.idHabitacion = idHabitacion;
    }

    public Propietario getPropietario() {
        return propietario;
    }

    public void setPropietario(Propietario propietario) {
        this.propietario = propietario;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDistrito() {
        return distrito;
    }

    public void setDistrito(String distrito) {
        this.distrito = distrito;
    }

    public String getDireccionReferencial() {
        return direccionReferencial;
    }

    public void setDireccionReferencial(String direccionReferencial) {
        this.direccionReferencial = direccionReferencial;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    public BigDecimal getAreaM2() {
        return areaM2;
    }

    public void setAreaM2(BigDecimal areaM2) {
        this.areaM2 = areaM2;
    }

    public Boolean getAmoblado() {
        return amoblado;
    }

    public void setAmoblado(Boolean amoblado) {
        this.amoblado = amoblado;
    }

    public Boolean getBanoPrivado() {
        return banoPrivado;
    }

    public void setBanoPrivado(Boolean banoPrivado) {
        this.banoPrivado = banoPrivado;
    }

    public Boolean getInternetIncluido() {
        return internetIncluido;
    }

    public void setInternetIncluido(Boolean internetIncluido) {
        this.internetIncluido = internetIncluido;
    }

    public Boolean getAguaIncluida() {
        return aguaIncluida;
    }

    public void setAguaIncluida(Boolean aguaIncluida) {
        this.aguaIncluida = aguaIncluida;
    }

    public Boolean getLuzIncluida() {
        return luzIncluida;
    }

    public void setLuzIncluida(Boolean luzIncluida) {
        this.luzIncluida = luzIncluida;
    }

    public Boolean getPermiteMascotas() {
        return permiteMascotas;
    }

    public void setPermiteMascotas(Boolean permiteMascotas) {
        this.permiteMascotas = permiteMascotas;
    }

    public LocalDate getDisponibleDesde() {
        return disponibleDesde;
    }

    public void setDisponibleDesde(LocalDate disponibleDesde) {
        this.disponibleDesde = disponibleDesde;
    }

    public Boolean getDestacada() {
        return destacada;
    }

    public void setDestacada(Boolean destacada) {
        this.destacada = destacada;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public LocalDateTime getFechaPublicacion() {
        return fechaPublicacion;
    }

    public void setFechaPublicacion(LocalDateTime fechaPublicacion) {
        this.fechaPublicacion = fechaPublicacion;
    }

    public LocalDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }

    public void setFechaActualizacion(LocalDateTime fechaActualizacion) {
        this.fechaActualizacion = fechaActualizacion;
    }
}