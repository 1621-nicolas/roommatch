package com.roommatch.service;

import com.roommatch.dto.PropietarioRequest;
import com.roommatch.dto.PropietarioResponse;

import com.roommatch.model.Notificacion;
import com.roommatch.model.PlanPropietario;
import com.roommatch.model.Propietario;
import com.roommatch.model.Rol;
import com.roommatch.model.SuscripcionPropietario;
import com.roommatch.model.Usuario;

import com.roommatch.repository.NotificacionRepository;
import com.roommatch.repository.PlanPropietarioRepository;
import com.roommatch.repository.PropietarioRepository;
import com.roommatch.repository.RolRepository;
import com.roommatch.repository.SuscripcionPropietarioRepository;
import com.roommatch.repository.UsuarioRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class PropietarioService {

    private final PropietarioRepository propietarioRepository;

    private final UsuarioRepository usuarioRepository;

    private final RolRepository rolRepository;

    private final PlanPropietarioRepository planRepository;

    private final SuscripcionPropietarioRepository suscripcionRepository;

    private final NotificacionRepository notificacionRepository;


    public PropietarioService(

            PropietarioRepository propietarioRepository,

            UsuarioRepository usuarioRepository,

            RolRepository rolRepository,

            PlanPropietarioRepository planRepository,

            SuscripcionPropietarioRepository suscripcionRepository,

            NotificacionRepository notificacionRepository

    ) {

        this.propietarioRepository =
                propietarioRepository;

        this.usuarioRepository =
                usuarioRepository;

        this.rolRepository =
                rolRepository;

        this.planRepository =
                planRepository;

        this.suscripcionRepository =
                suscripcionRepository;

        this.notificacionRepository =
                notificacionRepository;
    }


    /*
     * =========================================================
     * CONVERTIR USUARIO EN PROPIETARIO
     * =========================================================
     */

    @Transactional
    public PropietarioResponse convertirmeEnPropietario(

            Integer idUsuario,

            PropietarioRequest request

    ) {

        /*
         * =====================================================
         * VALIDAR PROPIETARIO EXISTENTE
         * =====================================================
         */

        if (
                propietarioRepository
                        .existsByUsuarioIdUsuario(
                                idUsuario
                        )
        ) {

            throw new IllegalArgumentException(
                    "Este usuario ya tiene perfil de propietario"
            );
        }


        /*
         * =====================================================
         * VALIDAR TIPO DE PROPIETARIO
         * =====================================================
         */

        if (
                request.getTipoPropietario() == null
        ) {

            throw new IllegalArgumentException(
                    "Debes seleccionar un tipo de propietario"
            );
        }


        String tipoPropietario =

                request
                        .getTipoPropietario()
                        .trim()
                        .toLowerCase();


        if (
                !tipoPropietario.equals("persona") &&
                !tipoPropietario.equals("empresa")
        ) {

            throw new IllegalArgumentException(
                    "El tipo de propietario debe ser persona o empresa"
            );
        }


        /*
         * =====================================================
         * BUSCAR USUARIO
         * =====================================================
         */

        Usuario usuario =

                usuarioRepository
                        .findById(
                                idUsuario
                        )
                        .orElseThrow(

                                () ->

                                        new IllegalArgumentException(
                                                "Usuario no encontrado"
                                        )

                        );


        /*
         * =====================================================
         * BUSCAR ROL PROPIETARIO
         * =====================================================
         */

        Rol rolPropietario =

                rolRepository
                        .findByNombreRol(
                                "PROPIETARIO"
                        )
                        .orElseThrow(

                                () ->

                                        new IllegalArgumentException(
                                                "No existe el rol PROPIETARIO en la base de datos"
                                        )

                        );


        /*
         * =====================================================
         * BUSCAR PLAN GRATIS
         * =====================================================
         */

        PlanPropietario planGratis =

                planRepository
                        .findByNombrePlan(
                                "Gratis"
                        )
                        .orElseThrow(

                                () ->

                                        new IllegalArgumentException(
                                                "No existe el plan Gratis en la base de datos"
                                        )

                        );


        /*
         * =====================================================
         * CAMBIAR ROL DEL USUARIO
         * =====================================================
         */

        usuario.setRol(
                rolPropietario
        );


        usuarioRepository.save(
                usuario
        );


        /*
         * =====================================================
         * CREAR PROPIETARIO
         * =====================================================
         */

        Propietario propietario =
                new Propietario();


        propietario.setUsuario(
                usuario
        );

        propietario.setTipoPropietario(
                tipoPropietario
        );

        propietario.setNombreComercial(
                request.getNombreComercial()
        );

        propietario.setRuc(
                request.getRuc()
        );

        propietario.setDescripcion(
                request.getDescripcion()
        );

        propietario.setEstado(
                "activo"
        );

        propietario.setVerificado(
                false
        );


        Propietario propietarioGuardado =

                propietarioRepository.save(
                        propietario
                );


        /*
         * =====================================================
         * CREAR SUSCRIPCIÓN GRATIS
         * =====================================================
         */

        SuscripcionPropietario suscripcion =
                new SuscripcionPropietario();


        suscripcion.setPropietario(
                propietarioGuardado
        );

        suscripcion.setPlan(
                planGratis
        );

        suscripcion.setEstado(
                "activo"
        );


        SuscripcionPropietario suscripcionGuardada =

                suscripcionRepository.save(
                        suscripcion
                );


        /*
         * =====================================================
         * CREAR NOTIFICACIÓN
         * =====================================================
         */

        Notificacion notificacion =
                new Notificacion();


        notificacion.setUsuario(
                usuario
        );

        notificacion.setTitulo(
                "Perfil de propietario creado"
        );

        notificacion.setMensaje(
                "Ahora puedes publicar habitaciones en RoomMatch con el plan Gratis."
        );

        notificacion.setTipo(
                "sistema"
        );

        notificacion.setUrlDestino(
                "/propietario"
        );


        notificacionRepository.save(
                notificacion
        );


        /*
         * =====================================================
         * RESPUESTA
         * =====================================================
         */

        return PropietarioResponse.fromEntity(

                propietarioGuardado,

                suscripcionGuardada

        );
    }


    /*
     * =========================================================
     * OBTENER MI PERFIL DE PROPIETARIO
     * =========================================================
     */

    @Transactional(readOnly = true)
    public PropietarioResponse obtenerMiPerfilPropietario(

            Integer idUsuario

    ) {

        Propietario propietario =

                propietarioRepository
                        .findByUsuarioIdUsuario(
                                idUsuario
                        )
                        .orElseThrow(

                                () ->

                                        new IllegalArgumentException(
                                                "Aún no tienes perfil de propietario"
                                        )

                        );


        SuscripcionPropietario suscripcion =

                suscripcionRepository
                        .findFirstByPropietarioIdPropietarioAndEstadoOrderByFechaInicioDesc(

                                propietario.getIdPropietario(),

                                "activo"

                        )
                        .orElse(null);


        return PropietarioResponse.fromEntity(

                propietario,

                suscripcion

        );
    }


    /*
     * =========================================================
     * OBTENER PERFIL OPCIONAL
     * =========================================================
     */

    @Transactional(readOnly = true)
    public PropietarioResponse obtenerMiPerfilPropietarioOpcional(

            Integer idUsuario

    ) {

        Propietario propietario =

                propietarioRepository
                        .findByUsuarioIdUsuario(
                                idUsuario
                        )
                        .orElse(null);


        /*
         * El usuario común todavía
         * no tiene perfil de propietario.
         */

        if (
                propietario == null
        ) {

            return null;
        }


        /*
         * Buscar suscripción activa.
         */

        SuscripcionPropietario suscripcion =

                suscripcionRepository
                        .findFirstByPropietarioIdPropietarioAndEstadoOrderByFechaInicioDesc(

                                propietario.getIdPropietario(),

                                "activo"

                        )
                        .orElse(null);


        return PropietarioResponse.fromEntity(

                propietario,

                suscripcion

        );
    }
}