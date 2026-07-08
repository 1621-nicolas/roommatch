package com.roommatch.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "publicacion_roomie")
public class PublicacionRoomie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_publicacion")
    private Integer idPublicacion;


    /*
     * =========================================================
     * USUARIO
     * =========================================================
     */

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "id_usuario",
            nullable = false
    )
    private Usuario usuario;


    /*
     * =========================================================
     * DATOS DE LA PUBLICACIÓN
     * =========================================================
     */

    @Column(
            name = "tipo_publicacion",
            nullable = false,
            length = 30
    )
    private String tipoPublicacion;


    @Column(
            name = "titulo",
            nullable = false,
            length = 150
    )
    private String titulo;


    @Column(
            name = "descripcion",
            nullable = false,
            length = 1000
    )
    private String descripcion;


    @Column(
            name = "distrito",
            nullable = false,
            length = 100
    )
    private String distrito;


    @Column(
            name = "presupuesto_min",
            precision = 10,
            scale = 2
    )
    private BigDecimal presupuestoMin;


    @Column(
            name = "presupuesto_max",
            precision = 10,
            scale = 2
    )
    private BigDecimal presupuestoMax;


    @Column(
            name = "estado",
            nullable = false,
            length = 20
    )
    private String estado;


    /*
     * =========================================================
     * VIVIENDA VINCULADA
     * =========================================================
     */

    @Column(
            name = "tipo_vinculacion_vivienda",
            length = 20
    )
    private String tipoVinculacionVivienda;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_habitacion")
    private Habitacion habitacion;


    /*
     * =========================================================
     * VIVIENDA EXTERNA
     * =========================================================
     */

    @Column(
            name = "vivienda_externa_titulo",
            length = 150
    )
    private String viviendaExternaTitulo;


    @Column(
            name = "vivienda_externa_direccion",
            length = 255
    )
    private String viviendaExternaDireccion;


    @Column(
            name = "vivienda_externa_precio",
            precision = 10,
            scale = 2
    )
    private BigDecimal viviendaExternaPrecio;


    /*
     * =========================================================
     * FECHAS
     * =========================================================
     */

    @Column(name = "fecha_publicacion")
    private LocalDateTime fechaPublicacion;


    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;


    /*
     * =========================================================
     * EVENTOS JPA
     * =========================================================
     */

    @PrePersist
    public void prePersist() {

        LocalDateTime ahora =
                LocalDateTime.now();

        if (estado == null) {
            estado = "activa";
        }

        fechaPublicacion = ahora;
        fechaActualizacion = ahora;
    }


    @PreUpdate
    public void preUpdate() {

        fechaActualizacion =
                LocalDateTime.now();
    }


    /*
     * =========================================================
     * GETTERS Y SETTERS
     * =========================================================
     */

    public Integer getIdPublicacion() {
        return idPublicacion;
    }

    public void setIdPublicacion(
            Integer idPublicacion
    ) {
        this.idPublicacion = idPublicacion;
    }


    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(
            Usuario usuario
    ) {
        this.usuario = usuario;
    }


    public String getTipoPublicacion() {
        return tipoPublicacion;
    }

    public void setTipoPublicacion(
            String tipoPublicacion
    ) {
        this.tipoPublicacion = tipoPublicacion;
    }


    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(
            String titulo
    ) {
        this.titulo = titulo;
    }


    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(
            String descripcion
    ) {
        this.descripcion = descripcion;
    }


    public String getDistrito() {
        return distrito;
    }

    public void setDistrito(
            String distrito
    ) {
        this.distrito = distrito;
    }


    public BigDecimal getPresupuestoMin() {
        return presupuestoMin;
    }

    public void setPresupuestoMin(
            BigDecimal presupuestoMin
    ) {
        this.presupuestoMin = presupuestoMin;
    }


    public BigDecimal getPresupuestoMax() {
        return presupuestoMax;
    }

    public void setPresupuestoMax(
            BigDecimal presupuestoMax
    ) {
        this.presupuestoMax = presupuestoMax;
    }


    public String getEstado() {
        return estado;
    }

    public void setEstado(
            String estado
    ) {
        this.estado = estado;
    }


    public String getTipoVinculacionVivienda() {
        return tipoVinculacionVivienda;
    }

    public void setTipoVinculacionVivienda(
            String tipoVinculacionVivienda
    ) {
        this.tipoVinculacionVivienda =
                tipoVinculacionVivienda;
    }


    public Habitacion getHabitacion() {
        return habitacion;
    }

    public void setHabitacion(
            Habitacion habitacion
    ) {
        this.habitacion = habitacion;
    }


    public String getViviendaExternaTitulo() {
        return viviendaExternaTitulo;
    }

    public void setViviendaExternaTitulo(
            String viviendaExternaTitulo
    ) {
        this.viviendaExternaTitulo =
                viviendaExternaTitulo;
    }


    public String getViviendaExternaDireccion() {
        return viviendaExternaDireccion;
    }

    public void setViviendaExternaDireccion(
            String viviendaExternaDireccion
    ) {
        this.viviendaExternaDireccion =
                viviendaExternaDireccion;
    }


    public BigDecimal getViviendaExternaPrecio() {
        return viviendaExternaPrecio;
    }

    public void setViviendaExternaPrecio(
            BigDecimal viviendaExternaPrecio
    ) {
        this.viviendaExternaPrecio =
                viviendaExternaPrecio;
    }


    public LocalDateTime getFechaPublicacion() {
        return fechaPublicacion;
    }

    public void setFechaPublicacion(
            LocalDateTime fechaPublicacion
    ) {
        this.fechaPublicacion =
                fechaPublicacion;
    }


    public LocalDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }

    public void setFechaActualizacion(
            LocalDateTime fechaActualizacion
    ) {
        this.fechaActualizacion =
                fechaActualizacion;
    }
}