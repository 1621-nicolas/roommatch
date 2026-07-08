package com.roommatch.service;

import com.roommatch.dto.MiPlanResponse;
import com.roommatch.dto.PlanPropietarioResponse;

import com.roommatch.model.Notificacion;
import com.roommatch.model.PlanPropietario;
import com.roommatch.model.Propietario;
import com.roommatch.model.SuscripcionPropietario;

import com.roommatch.repository.NotificacionRepository;
import com.roommatch.repository.PlanPropietarioRepository;
import com.roommatch.repository.PropietarioRepository;
import com.roommatch.repository.SuscripcionPropietarioRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;


@Service
public class PlanPropietarioService {

    private final PlanPropietarioRepository planRepository;

    private final PropietarioRepository propietarioRepository;

    private final SuscripcionPropietarioRepository suscripcionRepository;

    private final NotificacionRepository notificacionRepository;


    public PlanPropietarioService(
            PlanPropietarioRepository planRepository,
            PropietarioRepository propietarioRepository,
            SuscripcionPropietarioRepository suscripcionRepository,
            NotificacionRepository notificacionRepository
    ) {

        this.planRepository =
                planRepository;

        this.propietarioRepository =
                propietarioRepository;

        this.suscripcionRepository =
                suscripcionRepository;

        this.notificacionRepository =
                notificacionRepository;
    }


    /*
     * =========================================================
     * LISTAR PLANES ACTIVOS
     * =========================================================
     */

    @Transactional(readOnly = true)
    public List<PlanPropietarioResponse> listarPlanesActivos() {

        return planRepository
                .findByEstadoOrderByPrecioMensualAsc(
                        "activo"
                )
                .stream()
                .map(
                        PlanPropietarioResponse::fromEntity
                )
                .toList();
    }


    /*
     * =========================================================
     * OBTENER MI PLAN
     * =========================================================
     */

    @Transactional(readOnly = true)
    public MiPlanResponse obtenerMiPlan(
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
                                                "No tienes perfil de propietario"
                                        )
                        );


        SuscripcionPropietario suscripcion =

                suscripcionRepository
                        .findFirstByPropietarioIdPropietarioAndEstadoOrderByFechaInicioDesc(

                                propietario.getIdPropietario(),

                                "activo"

                        )
                        .orElseThrow(
                                () ->

                                        new IllegalArgumentException(
                                                "No tienes una suscripción activa"
                                        )
                        );


        return MiPlanResponse.fromEntity(
                suscripcion
        );
    }


    /*
     * =========================================================
     * CAMBIAR PLAN
     * =========================================================
     */

    @Transactional
    public MiPlanResponse cambiarPlan(
            Integer idUsuario,
            Integer idPlanNuevo
    ) {

        /*
         * =====================================================
         * OBTENER PROPIETARIO
         * =====================================================
         */

        Propietario propietario =

                propietarioRepository
                        .findByUsuarioIdUsuario(
                                idUsuario
                        )
                        .orElseThrow(
                                () ->

                                        new IllegalArgumentException(
                                                "No tienes perfil de propietario"
                                        )
                        );


        /*
         * =====================================================
         * BUSCAR NUEVO PLAN
         * =====================================================
         */

        PlanPropietario nuevoPlan =

                planRepository
                        .findById(
                                idPlanNuevo
                        )
                        .orElseThrow(
                                () ->

                                        new IllegalArgumentException(
                                                "Plan no encontrado"
                                        )
                        );


        if (
                !"activo".equalsIgnoreCase(
                        nuevoPlan.getEstado()
                )
        ) {

            throw new IllegalArgumentException(
                    "El plan seleccionado no está activo"
            );
        }


        /*
         * =====================================================
         * OBTENER SUSCRIPCIÓN ACTUAL
         * =====================================================
         */

        SuscripcionPropietario suscripcionActual =

                suscripcionRepository
                        .findFirstByPropietarioIdPropietarioAndEstadoOrderByFechaInicioDesc(

                                propietario.getIdPropietario(),

                                "activo"

                        )
                        .orElseThrow(
                                () ->

                                        new IllegalArgumentException(
                                                "No tienes una suscripción activa"
                                        )
                        );


        /*
         * =====================================================
         * VALIDAR MISMO PLAN
         * =====================================================
         */

        if (
                suscripcionActual.getPlan() != null &&
                suscripcionActual
                        .getPlan()
                        .getIdPlan()
                        .equals(
                                nuevoPlan.getIdPlan()
                        )
        ) {

            throw new IllegalArgumentException(
                    "Ya tienes activo el plan " +
                            nuevoPlan.getNombrePlan()
            );
        }


        /*
         * =====================================================
         * CANCELAR SUSCRIPCIÓN ACTUAL
         * =====================================================
         */

        suscripcionActual.setEstado(
                "cancelado"
        );

        suscripcionActual.setFechaFin(
                LocalDateTime.now()
        );


        suscripcionRepository.save(
                suscripcionActual
        );


        /*
         * =====================================================
         * CREAR NUEVA SUSCRIPCIÓN
         * =====================================================
         */

        SuscripcionPropietario nuevaSuscripcion =
                new SuscripcionPropietario();


        nuevaSuscripcion.setPropietario(
                propietario
        );

        nuevaSuscripcion.setPlan(
                nuevoPlan
        );

        nuevaSuscripcion.setEstado(
                "activo"
        );


        SuscripcionPropietario suscripcionGuardada =

                suscripcionRepository.save(
                        nuevaSuscripcion
                );


        /*
         * =====================================================
         * NOTIFICACIÓN
         * =====================================================
         */

        Notificacion notificacion =
                new Notificacion();


        notificacion.setUsuario(
                propietario.getUsuario()
        );

        notificacion.setTitulo(
                "Plan actualizado"
        );

        notificacion.setMensaje(

                "Tu plan cambió correctamente a " +

                nuevoPlan.getNombrePlan() +

                "."

        );

        notificacion.setTipo(
                "sistema"
        );

        notificacion.setUrlDestino(
                "/propietario/planes"
        );


        notificacionRepository.save(
                notificacion
        );


        /*
         * =====================================================
         * RESPUESTA
         * =====================================================
         */

        return MiPlanResponse.fromEntity(
                suscripcionGuardada
        );
    }
}