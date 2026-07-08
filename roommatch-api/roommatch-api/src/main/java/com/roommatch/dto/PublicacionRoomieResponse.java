package com.roommatch.dto;

import com.roommatch.model.Habitacion;
import com.roommatch.model.MatchResultado;
import com.roommatch.model.PublicacionRoomie;
import com.roommatch.model.Usuario;

import java.math.BigDecimal;
import java.time.LocalDateTime;


public class PublicacionRoomieResponse {

    private Integer idPublicacion;


    /*
     * =========================================================
     * AUTOR
     * =========================================================
     */

    private Integer idUsuario;

    private String nombreUsuario;

    private Integer edad;

    private String ocupacion;

    private String foto;


    /*
     * =========================================================
     * PUBLICACIÓN
     * =========================================================
     */

    private String tipoPublicacion;

    private String titulo;

    private String descripcion;

    private String distrito;

    private BigDecimal presupuestoMin;

    private BigDecimal presupuestoMax;

    private String estado;


    /*
     * =========================================================
     * VINCULACIÓN DE VIVIENDA
     * =========================================================
     */

    private String tipoVinculacionVivienda;


    /*
     * =========================================================
     * HABITACIÓN ROOMMATCH
     * =========================================================
     */

    private Integer idHabitacion;

    private String habitacionTitulo;

    private String habitacionDistrito;

    private BigDecimal habitacionPrecio;

    private String nombrePropietarioHabitacion;


    /*
     * =========================================================
     * VIVIENDA EXTERNA
     * =========================================================
     */

    private String viviendaExternaTitulo;

    private String viviendaExternaDireccion;

    private BigDecimal viviendaExternaPrecio;


    /*
     * =========================================================
     * COMPATIBILIDAD
     * =========================================================
     */

    private BigDecimal porcentajeCompatibilidad;

    private String coincidencias;

    private String diferencias;

    private Boolean esMiPublicacion;


    /*
     * =========================================================
     * FECHAS
     * =========================================================
     */

    private LocalDateTime fechaPublicacion;

    private LocalDateTime fechaActualizacion;


    /*
     * =========================================================
     * CONVERSIÓN BÁSICA
     * =========================================================
     */

    public static PublicacionRoomieResponse fromEntity(
            PublicacionRoomie publicacion
    ) {

        return crearResponseBase(
                publicacion,
                null
        );
    }


    /*
     * =========================================================
     * CONVERSIÓN CON MATCH GUARDADO
     * =========================================================
     */

    public static PublicacionRoomieResponse fromEntity(
            PublicacionRoomie publicacion,
            MatchResultado match,
            Integer idUsuarioActual
    ) {

        PublicacionRoomieResponse response =
                crearResponseBase(
                        publicacion,
                        idUsuarioActual
                );


        if (match != null) {

            response.setPorcentajeCompatibilidad(
                    match.getPorcentaje()
            );

            response.setCoincidencias(
                    match.getCoincidencias()
            );

            response.setDiferencias(
                    match.getDiferencias()
            );
        }


        return response;
    }


    /*
     * =========================================================
     * CONVERSIÓN CON COMPATIBILIDAD CALCULADA
     * =========================================================
     */

    public static PublicacionRoomieResponse fromEntity(
            PublicacionRoomie publicacion,
            CompatibilidadCalculada compatibilidad,
            Integer idUsuarioActual
    ) {

        PublicacionRoomieResponse response =
                crearResponseBase(
                        publicacion,
                        idUsuarioActual
                );


        if (compatibilidad != null) {

            response.setPorcentajeCompatibilidad(
                    compatibilidad.getPorcentaje()
            );

            response.setCoincidencias(
                    compatibilidad.getCoincidencias()
            );

            response.setDiferencias(
                    compatibilidad.getDiferencias()
            );
        }


        return response;
    }


    /*
     * =========================================================
     * CREAR RESPONSE BASE
     * =========================================================
     */

    private static PublicacionRoomieResponse crearResponseBase(
            PublicacionRoomie publicacion,
            Integer idUsuarioActual
    ) {

        PublicacionRoomieResponse response =
                new PublicacionRoomieResponse();


        /*
         * =====================================================
         * PUBLICACIÓN
         * =====================================================
         */

        response.setIdPublicacion(
                publicacion.getIdPublicacion()
        );

        response.setTipoPublicacion(
                publicacion.getTipoPublicacion()
        );

        response.setTitulo(
                publicacion.getTitulo()
        );

        response.setDescripcion(
                publicacion.getDescripcion()
        );

        response.setDistrito(
                publicacion.getDistrito()
        );

        response.setPresupuestoMin(
                publicacion.getPresupuestoMin()
        );

        response.setPresupuestoMax(
                publicacion.getPresupuestoMax()
        );

        response.setEstado(
                publicacion.getEstado()
        );


        /*
         * =====================================================
         * VINCULACIÓN DE VIVIENDA
         * =====================================================
         */

        response.setTipoVinculacionVivienda(
                publicacion.getTipoVinculacionVivienda()
        );


        /*
         * =====================================================
         * HABITACIÓN ROOMMATCH
         * =====================================================
         */

        Habitacion habitacion =
                publicacion.getHabitacion();


        if (habitacion != null) {

            response.setIdHabitacion(
                    habitacion.getIdHabitacion()
            );

            response.setHabitacionTitulo(
                    habitacion.getTitulo()
            );

            response.setHabitacionDistrito(
                    habitacion.getDistrito()
            );

            response.setHabitacionPrecio(
                    habitacion.getPrecio()
            );


            if (
                    habitacion.getPropietario() != null
            ) {

                response.setNombrePropietarioHabitacion(
                        habitacion
                                .getPropietario()
                                .getNombreComercial()
                );
            }

        } else {

            response.setIdHabitacion(null);

            response.setHabitacionTitulo(null);

            response.setHabitacionDistrito(null);

            response.setHabitacionPrecio(null);

            response.setNombrePropietarioHabitacion(null);
        }


        /*
         * =====================================================
         * VIVIENDA EXTERNA
         * =====================================================
         */

        response.setViviendaExternaTitulo(
                publicacion.getViviendaExternaTitulo()
        );

        response.setViviendaExternaDireccion(
                publicacion.getViviendaExternaDireccion()
        );

        response.setViviendaExternaPrecio(
                publicacion.getViviendaExternaPrecio()
        );


        /*
         * =====================================================
         * FECHAS
         * =====================================================
         */

        response.setFechaPublicacion(
                publicacion.getFechaPublicacion()
        );

        response.setFechaActualizacion(
                publicacion.getFechaActualizacion()
        );


        /*
         * =====================================================
         * USUARIO AUTOR
         * =====================================================
         */

        Usuario usuario =
                publicacion.getUsuario();


        if (usuario != null) {

            response.setIdUsuario(
                    usuario.getIdUsuario()
            );


            String nombres =

                    usuario.getNombres() != null

                            ? usuario
                                    .getNombres()
                                    .trim()

                            : "";


            String apellidos =

                    usuario.getApellidos() != null

                            ? usuario
                                    .getApellidos()
                                    .trim()

                            : "";


            response.setNombreUsuario(
                    (
                            nombres
                                    +
                                    " "
                                    +
                                    apellidos
                    ).trim()
            );


            response.setEdad(
                    usuario.getEdad()
            );

            response.setOcupacion(
                    usuario.getOcupacion()
            );

            response.setFoto(
                    usuario.getFoto()
            );


            boolean esPropia =

                    idUsuarioActual != null

                            &&

                            idUsuarioActual.equals(
                                    usuario.getIdUsuario()
                            );


            response.setEsMiPublicacion(
                    esPropia
            );

        } else {

            response.setEsMiPublicacion(
                    false
            );
        }


        /*
         * =====================================================
         * COMPATIBILIDAD POR DEFECTO
         * =====================================================
         */

        response.setPorcentajeCompatibilidad(
                null
        );

        response.setCoincidencias(
                null
        );

        response.setDiferencias(
                null
        );


        return response;
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


    public Integer getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(
            Integer idUsuario
    ) {
        this.idUsuario = idUsuario;
    }


    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(
            String nombreUsuario
    ) {
        this.nombreUsuario = nombreUsuario;
    }


    public Integer getEdad() {
        return edad;
    }

    public void setEdad(
            Integer edad
    ) {
        this.edad = edad;
    }


    public String getOcupacion() {
        return ocupacion;
    }

    public void setOcupacion(
            String ocupacion
    ) {
        this.ocupacion = ocupacion;
    }


    public String getFoto() {
        return foto;
    }

    public void setFoto(
            String foto
    ) {
        this.foto = foto;
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


    /*
     * =========================================================
     * VINCULACIÓN DE VIVIENDA
     * =========================================================
     */


    public String getTipoVinculacionVivienda() {
        return tipoVinculacionVivienda;
    }

    public void setTipoVinculacionVivienda(
            String tipoVinculacionVivienda
    ) {
        this.tipoVinculacionVivienda =
                tipoVinculacionVivienda;
    }


    public Integer getIdHabitacion() {
        return idHabitacion;
    }

    public void setIdHabitacion(
            Integer idHabitacion
    ) {
        this.idHabitacion = idHabitacion;
    }


    public String getHabitacionTitulo() {
        return habitacionTitulo;
    }

    public void setHabitacionTitulo(
            String habitacionTitulo
    ) {
        this.habitacionTitulo =
                habitacionTitulo;
    }


    public String getHabitacionDistrito() {
        return habitacionDistrito;
    }

    public void setHabitacionDistrito(
            String habitacionDistrito
    ) {
        this.habitacionDistrito =
                habitacionDistrito;
    }


    public BigDecimal getHabitacionPrecio() {
        return habitacionPrecio;
    }

    public void setHabitacionPrecio(
            BigDecimal habitacionPrecio
    ) {
        this.habitacionPrecio =
                habitacionPrecio;
    }


    public String getNombrePropietarioHabitacion() {
        return nombrePropietarioHabitacion;
    }

    public void setNombrePropietarioHabitacion(
            String nombrePropietarioHabitacion
    ) {
        this.nombrePropietarioHabitacion =
                nombrePropietarioHabitacion;
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


    /*
     * =========================================================
     * COMPATIBILIDAD
     * =========================================================
     */


    public BigDecimal getPorcentajeCompatibilidad() {
        return porcentajeCompatibilidad;
    }

    public void setPorcentajeCompatibilidad(
            BigDecimal porcentajeCompatibilidad
    ) {
        this.porcentajeCompatibilidad =
                porcentajeCompatibilidad;
    }


    public String getCoincidencias() {
        return coincidencias;
    }

    public void setCoincidencias(
            String coincidencias
    ) {
        this.coincidencias = coincidencias;
    }


    public String getDiferencias() {
        return diferencias;
    }

    public void setDiferencias(
            String diferencias
    ) {
        this.diferencias = diferencias;
    }


    public Boolean getEsMiPublicacion() {
        return esMiPublicacion;
    }

    public void setEsMiPublicacion(
            Boolean esMiPublicacion
    ) {
        this.esMiPublicacion =
                esMiPublicacion;
    }


    /*
     * =========================================================
     * FECHAS
     * =========================================================
     */


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