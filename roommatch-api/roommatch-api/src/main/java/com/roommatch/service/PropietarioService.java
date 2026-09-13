package com.roommatch.service;

import com.roommatch.exception.ResourceNotFoundException;

import com.roommatch.exception.ConflictException;

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
    public PropietarioResponse convertirmeEnPropietario(Integer idUsuario, PropietarioRequest request) {
        Usuario usuario = usuarioRepository.lockById(idUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        if (!"activo".equals(usuario.getEstado())) throw new AccessDeniedException("Tu cuenta no está activa");
        if (propietarioRepository.existsByUsuarioIdUsuario(idUsuario)) throw new ConflictException("Este usuario ya tiene perfil de propietario");
        String tipo = request.getTipoPropietario() == null ? "" : request.getTipoPropietario().trim().toLowerCase(java.util.Locale.ROOT);
        if (!tipo.equals("persona") && !tipo.equals("empresa")) throw new IllegalArgumentException("El tipo de propietario debe ser persona o empresa");
        Rol rol = rolRepository.findByNombreRol("PROPIETARIO").orElseThrow(() -> new IllegalStateException("Falta el catálogo de roles"));
        PlanPropietario gratis = planRepository.findByNombrePlan("Gratis").orElseThrow(() -> new IllegalStateException("Falta el plan Gratis"));
        if (!"activo".equals(gratis.getEstado())) throw new ConflictException("El plan inicial no está disponible");
        // Owner capability does not remove an administrator or future privileged role.
        if ("USUARIO".equals(usuario.getRol().getNombreRol())) usuario.setRol(rol);
        usuarioRepository.save(usuario);
        Propietario propietario = new Propietario();
        propietario.setUsuario(usuario);
        propietario.setTipoPropietario(tipo);
        propietario.setNombreComercial(request.getNombreComercial());
        propietario.setRuc(request.getRuc());
        propietario.setDescripcion(request.getDescripcion());
        propietario = propietarioRepository.saveAndFlush(propietario);
        SuscripcionPropietario suscripcion = new SuscripcionPropietario();
        suscripcion.setPropietario(propietario);
        suscripcion.setPlan(gratis);
        suscripcion = suscripcionRepository.saveAndFlush(suscripcion);
        Notificacion notificacion = new Notificacion();
        notificacion.setUsuario(usuario);
        notificacion.setTitulo("Perfil de propietario creado");
        notificacion.setMensaje("Ahora puedes publicar habitaciones en RoomMatch con el plan Gratis.");
        notificacion.setTipo("sistema");
        notificacion.setUrlDestino("/propietario");
        notificacionRepository.save(notificacion);
        return PropietarioResponse.fromEntity(propietario, suscripcion);
    }

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

                                        new ResourceNotFoundException(
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