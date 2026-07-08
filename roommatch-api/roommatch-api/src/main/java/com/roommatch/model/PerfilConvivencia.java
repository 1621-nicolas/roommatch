package com.roommatch.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "perfil_convivencia")
@NamedQueries({
        @NamedQuery(
                name = "PerfilConvivencia.buscarActivosPorDistrito",
                query = """
                        SELECT p
                        FROM PerfilConvivencia p
                        WHERE p.usuario.estado = 'activo'
                        AND LOWER(p.distritoPreferido) LIKE LOWER(:distrito)
                        ORDER BY p.fechaActualizacion DESC
                        """
        ),
        @NamedQuery(
                name = "PerfilConvivencia.buscarCompatiblesPorPresupuesto",
                query = """
                        SELECT p
                        FROM PerfilConvivencia p
                        WHERE p.usuario.idUsuario <> :idUsuario
                        AND p.usuario.estado = 'activo'
                        AND p.presupuestoMin <= :presupuestoMax
                        AND p.presupuestoMax >= :presupuestoMin
                        ORDER BY p.fechaActualizacion DESC
                        """
        )
})
public class PerfilConvivencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_perfil")
    private Integer idPerfil;

    @OneToOne
    @JoinColumn(name = "id_usuario", nullable = false, unique = true)
    private Usuario usuario;

    @Column(name = "presupuesto_min", nullable = false)
    private BigDecimal presupuestoMin;

    @Column(name = "presupuesto_max", nullable = false)
    private BigDecimal presupuestoMax;

    @Column(name = "distrito_preferido", nullable = false, length = 100)
    private String distritoPreferido;

    @Column(name = "fecha_mudanza")
    private LocalDate fechaMudanza;

    @Column(name = "limpieza", nullable = false)
    private Integer limpieza;

    @Column(name = "ruido", nullable = false)
    private Integer ruido;

    @Column(name = "sociabilidad", nullable = false)
    private Integer sociabilidad;

    @Column(name = "horario", nullable = false, length = 50)
    private String horario;

    @Column(name = "visitas", nullable = false, length = 50)
    private String visitas;

    @Column(name = "mascotas", nullable = false, length = 50)
    private String mascotas;

    @Column(name = "fumar", nullable = false, length = 50)
    private String fumar;

    @Column(name = "alcohol", nullable = false, length = 50)
    private String alcohol;

    @Column(name = "gastos", nullable = false, length = 50)
    private String gastos;

    @Column(name = "convivencia", nullable = false, length = 50)
    private String convivencia;

    @Column(name = "descripcion_personal", length = 500)
    private String descripcionPersonal;

    @Column(name = "perfil_completo", nullable = false)
    private Boolean perfilCompleto = true;

    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion;

    @PrePersist
    public void prePersist() {
        this.fechaActualizacion = LocalDateTime.now();
        this.perfilCompleto = true;
    }

    @PreUpdate
    public void preUpdate() {
        this.fechaActualizacion = LocalDateTime.now();
    }

    public Integer getIdPerfil() {
        return idPerfil;
    }

    public void setIdPerfil(Integer idPerfil) {
        this.idPerfil = idPerfil;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public BigDecimal getPresupuestoMin() {
        return presupuestoMin;
    }

    public void setPresupuestoMin(BigDecimal presupuestoMin) {
        this.presupuestoMin = presupuestoMin;
    }

    public BigDecimal getPresupuestoMax() {
        return presupuestoMax;
    }

    public void setPresupuestoMax(BigDecimal presupuestoMax) {
        this.presupuestoMax = presupuestoMax;
    }

    public String getDistritoPreferido() {
        return distritoPreferido;
    }

    public void setDistritoPreferido(String distritoPreferido) {
        this.distritoPreferido = distritoPreferido;
    }

    public LocalDate getFechaMudanza() {
        return fechaMudanza;
    }

    public void setFechaMudanza(LocalDate fechaMudanza) {
        this.fechaMudanza = fechaMudanza;
    }

    public Integer getLimpieza() {
        return limpieza;
    }

    public void setLimpieza(Integer limpieza) {
        this.limpieza = limpieza;
    }

    public Integer getRuido() {
        return ruido;
    }

    public void setRuido(Integer ruido) {
        this.ruido = ruido;
    }

    public Integer getSociabilidad() {
        return sociabilidad;
    }

    public void setSociabilidad(Integer sociabilidad) {
        this.sociabilidad = sociabilidad;
    }

    public String getHorario() {
        return horario;
    }

    public void setHorario(String horario) {
        this.horario = horario;
    }

    public String getVisitas() {
        return visitas;
    }

    public void setVisitas(String visitas) {
        this.visitas = visitas;
    }

    public String getMascotas() {
        return mascotas;
    }

    public void setMascotas(String mascotas) {
        this.mascotas = mascotas;
    }

    public String getFumar() {
        return fumar;
    }

    public void setFumar(String fumar) {
        this.fumar = fumar;
    }

    public String getAlcohol() {
        return alcohol;
    }

    public void setAlcohol(String alcohol) {
        this.alcohol = alcohol;
    }

    public String getGastos() {
        return gastos;
    }

    public void setGastos(String gastos) {
        this.gastos = gastos;
    }

    public String getConvivencia() {
        return convivencia;
    }

    public void setConvivencia(String convivencia) {
        this.convivencia = convivencia;
    }

    public String getDescripcionPersonal() {
        return descripcionPersonal;
    }

    public void setDescripcionPersonal(String descripcionPersonal) {
        this.descripcionPersonal = descripcionPersonal;
    }

    public Boolean getPerfilCompleto() {
        return perfilCompleto;
    }

    public void setPerfilCompleto(Boolean perfilCompleto) {
        this.perfilCompleto = perfilCompleto;
    }

    public LocalDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }

    public void setFechaActualizacion(LocalDateTime fechaActualizacion) {
        this.fechaActualizacion = fechaActualizacion;
    }
}