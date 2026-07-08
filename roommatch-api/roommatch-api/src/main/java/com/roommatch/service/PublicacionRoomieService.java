package com.roommatch.service;

import com.roommatch.dto.CompatibilidadCalculada;
import com.roommatch.dto.PublicacionRoomieRequest;
import com.roommatch.dto.PublicacionRoomieResponse;
import com.roommatch.repository.ImagenPublicacionRepository;
import com.roommatch.model.Habitacion;
import com.roommatch.model.PublicacionRoomie;
import com.roommatch.model.Usuario;

import com.roommatch.repository.HabitacionRepository;
import com.roommatch.repository.ImagenPublicacionRepository;
import com.roommatch.repository.PublicacionRoomieRepository;
import com.roommatch.repository.UsuarioRepository;

import com.roommatch.util.ApiConstants;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import java.util.Optional;
import java.util.Set;


@Service
public class PublicacionRoomieService {


    /*
     * =========================================================
     * DEPENDENCIAS
     * =========================================================
     */

    private final PublicacionRoomieRepository
            publicacionRepository;


    private final UsuarioRepository
            usuarioRepository;


    private final HabitacionRepository
            habitacionRepository;


    private final MatchService
            matchService;

        private final ImagenPublicacionRepository imagenPublicacionRepository;


    /*
     * =========================================================
     * CONSTRUCTOR
     * =========================================================
     */

    public PublicacionRoomieService(
        PublicacionRoomieRepository publicacionRepository,
        UsuarioRepository usuarioRepository,
        MatchService matchService,
        HabitacionRepository habitacionRepository,
        ImagenPublicacionRepository imagenPublicacionRepository
) {

    this.publicacionRepository =
            publicacionRepository;

    this.usuarioRepository =
            usuarioRepository;

    this.matchService =
            matchService;

    this.habitacionRepository =
            habitacionRepository;

    this.imagenPublicacionRepository =
            imagenPublicacionRepository;
}


    /*
     * =========================================================
     * CREAR PUBLICACIÓN
     * =========================================================
     */

    @Transactional
    public PublicacionRoomieResponse crearPublicacion(

            Integer idUsuario,

            PublicacionRoomieRequest request

    ) {

        /*
         * =====================================================
         * BUSCAR USUARIO
         * =====================================================
         */

        Usuario usuario =

                usuarioRepository
                        .findById(idUsuario)
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "Usuario no encontrado"
                                        )
                        );


        /*
         * =====================================================
         * VALIDACIONES
         * =====================================================
         */

        validarTipoPublicacion(

                request.getTipoPublicacion()

        );


        validarPresupuesto(

                request

        );


        validarVinculacionVivienda(

                request

        );


        /*
         * =====================================================
         * CREAR PUBLICACIÓN
         * =====================================================
         */

        PublicacionRoomie publicacion =

                new PublicacionRoomie();


        publicacion.setUsuario(

                usuario

        );


        copiarDatos(

                request,

                publicacion

        );


        aplicarVinculacionVivienda(

                request,

                publicacion

        );


        publicacion.setEstado(

                ApiConstants.ESTADO_ACTIVA

        );


        /*
         * =====================================================
         * GUARDAR
         * =====================================================
         */

        PublicacionRoomie guardada =

                publicacionRepository
                        .save(publicacion);


        /*
         * =====================================================
         * RESPONSE
         * =====================================================
         */

        return crearResponse(

                guardada,

                idUsuario

        );

    }


    /*
     * =========================================================
     * LISTAR PUBLICACIONES
     * =========================================================
     */

    @Transactional(readOnly = true)
    public Page<PublicacionRoomieResponse>
    listarPublicaciones(

            Integer idUsuarioActual,

            String tipo,

            String distrito,

            BigDecimal presupuestoMin,

            BigDecimal presupuestoMax,

            Pageable pageable

    ) {

        /*
         * =====================================================
         * NORMALIZAR TIPO
         * =====================================================
         */

        if (

                tipo != null

                &&

                !tipo.trim().isEmpty()

        ) {

            validarTipoPublicacion(

                    tipo

            );


            tipo =

                    tipo
                            .trim()
                            .toLowerCase();

        } else {

            tipo = null;

        }


        /*
         * =====================================================
         * NORMALIZAR DISTRITO
         * =====================================================
         */

        if (

                distrito != null

                &&

                distrito.trim().isEmpty()

        ) {

            distrito = null;

        }


        /*
         * =====================================================
         * VALIDAR RANGO DE PRESUPUESTO
         * =====================================================
         */

        if (

                presupuestoMin != null

                &&

                presupuestoMax != null

                &&

                presupuestoMax
                        .compareTo(
                                presupuestoMin
                        ) < 0

        ) {

            throw new IllegalArgumentException(

                    "El presupuesto máximo no puede ser menor que el presupuesto mínimo"

            );

        }


        /*
         * =====================================================
         * CONSULTAR PUBLICACIONES
         * =====================================================
         */

        return publicacionRepository

                .buscarPublicaciones(

                        tipo,

                        distrito,

                        presupuestoMin,

                        presupuestoMax,

                        pageable

                )

                .map(

                        publicacion ->

                                crearResponse(

                                        publicacion,

                                        idUsuarioActual

                                )

                );

    }


    /*
     * =========================================================
     * OBTENER PUBLICACIÓN
     * =========================================================
     */

    @Transactional(readOnly = true)
    public PublicacionRoomieResponse obtenerPublicacion(

            Integer idUsuarioActual,

            Integer idPublicacion

    ) {

        /*
         * =====================================================
         * BUSCAR PUBLICACIÓN
         * =====================================================
         */

        PublicacionRoomie publicacion =

                publicacionRepository
                        .findById(idPublicacion)
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "Publicación no encontrada"
                                        )
                        );


        /*
         * =====================================================
         * VALIDAR ESTADO
         * =====================================================
         */

        if (

                publicacion.getEstado() == null

                ||

                !publicacion
                        .getEstado()
                        .equalsIgnoreCase(
                                ApiConstants.ESTADO_ACTIVA
                        )

        ) {

            throw new IllegalArgumentException(

                    "La publicación no está disponible"

            );

        }


        /*
         * =====================================================
         * RESPONSE
         * =====================================================
         */

        return crearResponse(

                publicacion,

                idUsuarioActual

        );

    }


    /*
     * =========================================================
     * LISTAR MIS PUBLICACIONES
     * =========================================================
     */

    @Transactional(readOnly = true)
    public Page<PublicacionRoomieResponse>
    listarMisPublicaciones(

            Integer idUsuario,

            Pageable pageable

    ) {

        return publicacionRepository

                .findByUsuarioIdUsuarioOrderByFechaPublicacionDesc(

                        idUsuario,

                        pageable

                )

                .map(

                        publicacion ->

                                crearResponse(

                                        publicacion,

                                        idUsuario

                                )

                );

    }


    /*
     * =========================================================
     * ACTUALIZAR PUBLICACIÓN
     * =========================================================
     */

    @Transactional
    public PublicacionRoomieResponse actualizarPublicacion(

            Integer idUsuario,

            Integer idPublicacion,

            PublicacionRoomieRequest request

    ) {

        /*
         * =====================================================
         * VALIDACIONES
         * =====================================================
         */

        validarTipoPublicacion(

                request.getTipoPublicacion()

        );


        validarPresupuesto(

                request

        );


        validarVinculacionVivienda(

                request

        );


        /*
         * =====================================================
         * BUSCAR PUBLICACIÓN DEL USUARIO
         * =====================================================
         */

        PublicacionRoomie publicacion =

                publicacionRepository

                        .findByIdPublicacionAndUsuarioIdUsuario(

                                idPublicacion,

                                idUsuario

                        )

                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(

                                                "Publicación no encontrada o no te pertenece"

                                        )
                        );


        /*
         * =====================================================
         * ACTUALIZAR DATOS
         * =====================================================
         */

        copiarDatos(

                request,

                publicacion

        );


        aplicarVinculacionVivienda(

                request,

                publicacion

        );


        /*
         * =====================================================
         * GUARDAR
         * =====================================================
         */

        PublicacionRoomie actualizada =

                publicacionRepository
                        .save(publicacion);


        return crearResponse(

                actualizada,

                idUsuario

        );

    }


    /*
     * =========================================================
     * PAUSAR PUBLICACIÓN
     * =========================================================
     */

    @Transactional
    public PublicacionRoomieResponse pausarPublicacion(

            Integer idUsuario,

            Integer idPublicacion

    ) {

        PublicacionRoomie publicacion =

                obtenerPublicacionPropia(

                        idUsuario,

                        idPublicacion

                );


        publicacion.setEstado(

                ApiConstants.ESTADO_PAUSADA

        );


        PublicacionRoomie actualizada =

                publicacionRepository
                        .save(publicacion);


        return crearResponse(

                actualizada,

                idUsuario

        );

    }


    /*
     * =========================================================
     * ACTIVAR PUBLICACIÓN
     * =========================================================
     */

    @Transactional
    public PublicacionRoomieResponse activarPublicacion(

            Integer idUsuario,

            Integer idPublicacion

    ) {

        PublicacionRoomie publicacion =

                obtenerPublicacionPropia(

                        idUsuario,

                        idPublicacion

                );


        publicacion.setEstado(

                ApiConstants.ESTADO_ACTIVA

        );


        PublicacionRoomie actualizada =

                publicacionRepository
                        .save(publicacion);


        return crearResponse(

                actualizada,

                idUsuario

        );

    }


    /*
     * =========================================================
     * CERRAR PUBLICACIÓN
     * =========================================================
     */

    @Transactional
    public PublicacionRoomieResponse cerrarPublicacion(

            Integer idUsuario,

            Integer idPublicacion

    ) {

        PublicacionRoomie publicacion =

                obtenerPublicacionPropia(

                        idUsuario,

                        idPublicacion

                );


        publicacion.setEstado(

                ApiConstants.ESTADO_CERRADA

        );


        PublicacionRoomie actualizada =

                publicacionRepository
                        .save(publicacion);


        return crearResponse(

                actualizada,

                idUsuario

        );

    }


    /*
     * =========================================================
     * ELIMINAR PUBLICACIÓN LÓGICAMENTE
     * =========================================================
     */

    @Transactional
public void eliminarPublicacion(
        Integer idUsuario,
        Integer idPublicacion
) {

    PublicacionRoomie publicacion =
            publicacionRepository
                    .findByIdPublicacionAndUsuarioIdUsuario(
                            idPublicacion,
                            idUsuario
                    )
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "La publicación no existe o no te pertenece"
                            )
                    );


    /*
     * =========================================================
     * ELIMINAR IMÁGENES RELACIONADAS
     * =========================================================
     */

    imagenPublicacionRepository
            .deleteByPublicacionIdPublicacion(
                    publicacion.getIdPublicacion()
            );


    /*
     * =========================================================
     * ELIMINAR PUBLICACIÓN
     * =========================================================
     */

    publicacionRepository.delete(
            publicacion
    );
}

    /*
     * =========================================================
     * CREAR RESPONSE CON COMPATIBILIDAD
     * =========================================================
     */

    private PublicacionRoomieResponse crearResponse(

            PublicacionRoomie publicacion,

            Integer idUsuarioActual

    ) {

        /*
         * =====================================================
         * OBTENER AUTOR
         * =====================================================
         */

        Usuario autor =

                publicacion.getUsuario();


        if (

                autor == null

        ) {

            throw new IllegalStateException(

                    "La publicación no tiene un usuario asociado"

            );

        }


        Integer idUsuarioAutor =

                autor.getIdUsuario();


        /*
         * =====================================================
         * PUBLICACIÓN PROPIA O SIN USUARIO AUTENTICADO
         * =====================================================
         */

        if (

                idUsuarioActual == null

                ||

                idUsuarioActual.equals(
                        idUsuarioAutor
                )

        ) {

            return PublicacionRoomieResponse

                    .fromEntity(

                            publicacion,

                            (CompatibilidadCalculada) null,

                            idUsuarioActual

                    );

        }


        /*
         * =====================================================
         * OBTENER COMPATIBILIDAD
         * =====================================================
         */

        Optional<CompatibilidadCalculada>
                compatibilidad =

                matchService

                        .obtenerCompatibilidadEntreUsuarios(

                                idUsuarioActual,

                                idUsuarioAutor

                        );


        /*
         * =====================================================
         * CONSTRUIR RESPONSE
         * =====================================================
         */

        return PublicacionRoomieResponse

                .fromEntity(

                        publicacion,

                        compatibilidad
                                .orElse(null),

                        idUsuarioActual

                );

    }


    /*
     * =========================================================
     * OBTENER PUBLICACIÓN PROPIA
     * =========================================================
     */

    private PublicacionRoomie obtenerPublicacionPropia(

            Integer idUsuario,

            Integer idPublicacion

    ) {

        return publicacionRepository

                .findByIdPublicacionAndUsuarioIdUsuario(

                        idPublicacion,

                        idUsuario

                )

                .orElseThrow(
                        () ->
                                new IllegalArgumentException(

                                        "Publicación no encontrada o no te pertenece"

                                )
                );

    }


    /*
     * =========================================================
     * COPIAR DATOS GENERALES
     * =========================================================
     */

    private void copiarDatos(

            PublicacionRoomieRequest request,

            PublicacionRoomie publicacion

    ) {

        publicacion.setTipoPublicacion(

                request
                        .getTipoPublicacion()
                        .trim()
                        .toLowerCase()

        );


        publicacion.setTitulo(

                request
                        .getTitulo()
                        .trim()

        );


        publicacion.setDescripcion(

                request
                        .getDescripcion()
                        .trim()

        );


        publicacion.setDistrito(

                request
                        .getDistrito()
                        .trim()

        );


        publicacion.setPresupuestoMin(

                request.getPresupuestoMin()

        );


        publicacion.setPresupuestoMax(

                request.getPresupuestoMax()

        );

    }


    /*
     * =========================================================
     * APLICAR VINCULACIÓN DE VIVIENDA
     * =========================================================
     */

    private void aplicarVinculacionVivienda(

            PublicacionRoomieRequest request,

            PublicacionRoomie publicacion

    ) {

        String tipoVinculacion =

                normalizarTexto(

                        request
                                .getTipoVinculacionVivienda()

                );


        /*
         * =====================================================
         * SIN VIVIENDA VINCULADA
         * =====================================================
         */

        if (

                tipoVinculacion == null

        ) {

            limpiarVinculacionVivienda(

                    publicacion

            );

            return;

        }


        /*
         * =====================================================
         * HABITACIÓN ROOMMATCH
         * =====================================================
         */

        if (

                tipoVinculacion
                        .equals("roommatch")

        ) {

            Habitacion habitacion =

                    habitacionRepository

                            .findById(

                                    request.getIdHabitacion()

                            )

                            .orElseThrow(
                                    () ->
                                            new IllegalArgumentException(

                                                    "La habitación seleccionada no existe"

                                            )
                            );


            if (

                    habitacion.getEstado() == null

                    ||

                    !habitacion
                            .getEstado()
                            .equalsIgnoreCase(
                                    ApiConstants.ESTADO_ACTIVA
                            )

            ) {

                throw new IllegalArgumentException(

                        "La habitación seleccionada no está disponible"

                );

            }


            publicacion.setTipoVinculacionVivienda(

                    "roommatch"

            );


            publicacion.setHabitacion(

                    habitacion

            );


            /*
             * LIMPIAR DATOS EXTERNOS
             */

            publicacion.setViviendaExternaTitulo(

                    null

            );


            publicacion.setViviendaExternaDireccion(

                    null

            );


            publicacion.setViviendaExternaPrecio(

                    null

            );


            return;

        }


        /*
         * =====================================================
         * VIVIENDA EXTERNA
         * =====================================================
         */

        if (

                tipoVinculacion
                        .equals("externa")

        ) {

            publicacion.setTipoVinculacionVivienda(

                    "externa"

            );


            /*
             * NO UTILIZAR HABITACIÓN ROOMMATCH
             */

            publicacion.setHabitacion(

                    null

            );


            publicacion.setViviendaExternaTitulo(

                    request
                            .getViviendaExternaTitulo()
                            .trim()

            );


            publicacion.setViviendaExternaDireccion(

                    normalizarTexto(

                            request
                                    .getViviendaExternaDireccion()

                    )

            );


            publicacion.setViviendaExternaPrecio(

                    request
                            .getViviendaExternaPrecio()

            );

        }

    }


    /*
     * =========================================================
     * LIMPIAR VINCULACIÓN DE VIVIENDA
     * =========================================================
     */

    private void limpiarVinculacionVivienda(

            PublicacionRoomie publicacion

    ) {

        publicacion.setTipoVinculacionVivienda(

                null

        );


        publicacion.setHabitacion(

                null

        );


        publicacion.setViviendaExternaTitulo(

                null

        );


        publicacion.setViviendaExternaDireccion(

                null

        );


        publicacion.setViviendaExternaPrecio(

                null

        );

    }


    /*
     * =========================================================
     * VALIDAR VINCULACIÓN DE VIVIENDA
     * =========================================================
     */

    private void validarVinculacionVivienda(

            PublicacionRoomieRequest request

    ) {

        String tipoPublicacion =

                request
                        .getTipoPublicacion()
                        .trim()
                        .toLowerCase();


        String tipoVinculacion =

                normalizarTexto(

                        request
                                .getTipoVinculacionVivienda()

                );


        /*
         * =====================================================
         * SIN VINCULACIÓN
         * =====================================================
         */

        if (

                tipoVinculacion == null

        ) {

            return;

        }


        /*
         * =====================================================
         * SOLO BUSCO COMPARTIR PUEDE VINCULAR VIVIENDA
         * =====================================================
         */

        if (

                !tipoPublicacion.equals(

                        ApiConstants.TIPO_BUSCO_COMPARTIR

                )

        ) {

            throw new IllegalArgumentException(

                    "Solo una publicación de tipo busco_compartir puede vincular una vivienda"

            );

        }


        /*
         * =====================================================
         * VALIDAR TIPO DE VINCULACIÓN
         * =====================================================
         */

        if (

                !tipoVinculacion.equals("roommatch")

                &&

                !tipoVinculacion.equals("externa")

        ) {

            throw new IllegalArgumentException(

                    "Tipo de vinculación no válido. Usa: roommatch o externa"

            );

        }


        /*
         * =====================================================
         * VALIDAR HABITACIÓN ROOMMATCH
         * =====================================================
         */

        if (

                tipoVinculacion.equals("roommatch")

                &&

                request.getIdHabitacion() == null

        ) {

            throw new IllegalArgumentException(

                    "Debes seleccionar una habitación de RoomMatch"

            );

        }


        /*
         * =====================================================
         * VALIDAR VIVIENDA EXTERNA
         * =====================================================
         */

        if (

                tipoVinculacion.equals("externa")

                &&

                (

                        request.getViviendaExternaTitulo() == null

                        ||

                        request
                                .getViviendaExternaTitulo()
                                .trim()
                                .isEmpty()

                )

        ) {

            throw new IllegalArgumentException(

                    "Debes ingresar una referencia para la vivienda externa"

            );

        }

    }


    /*
     * =========================================================
     * NORMALIZAR TEXTO
     * =========================================================
     */

    private String normalizarTexto(

            String valor

    ) {

        if (

                valor == null

                ||

                valor.trim().isEmpty()

        ) {

            return null;

        }


        return valor
                .trim()
                .toLowerCase();

    }


    /*
     * =========================================================
     * VALIDAR TIPO DE PUBLICACIÓN
     * =========================================================
     */

    private void validarTipoPublicacion(

            String tipo

    ) {

        Set<String> tiposPermitidos =

                Set.of(

                        ApiConstants.TIPO_BUSCO_ROOMIE,

                        ApiConstants.TIPO_BUSCO_CUARTO,

                        ApiConstants.TIPO_BUSCO_COMPARTIR

                );


        if (

                tipo == null

                ||

                !tiposPermitidos.contains(

                        tipo
                                .trim()
                                .toLowerCase()

                )

        ) {

            throw new IllegalArgumentException(

                    "Tipo de publicación no válido. "
                            +
                            "Usa: busco_roomie, "
                            +
                            "busco_cuarto o "
                            +
                            "busco_compartir"

            );

        }

    }


    /*
     * =========================================================
     * VALIDAR PRESUPUESTO
     * =========================================================
     */

    private void validarPresupuesto(

            PublicacionRoomieRequest request

    ) {

        if (

                request.getPresupuestoMin() != null

                &&

                request.getPresupuestoMax() != null

                &&

                request
                        .getPresupuestoMax()
                        .compareTo(

                                request.getPresupuestoMin()

                        ) < 0

        ) {

            throw new IllegalArgumentException(

                    "El presupuesto máximo no puede ser menor que el presupuesto mínimo"

            );

        }

    }

}