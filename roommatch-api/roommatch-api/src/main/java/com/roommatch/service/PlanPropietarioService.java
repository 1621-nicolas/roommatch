package com.roommatch.service;

import com.roommatch.dto.MiPlanResponse;
import com.roommatch.dto.PlanPropietarioResponse;
import com.roommatch.exception.ConflictException;
import com.roommatch.exception.ResourceNotFoundException;
import com.roommatch.model.Notificacion;
import com.roommatch.model.SuscripcionPropietario;
import com.roommatch.repository.*;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class PlanPropietarioService {
    private final PlanPropietarioRepository planes;
    private final PropietarioRepository propietarios;
    private final SuscripcionPropietarioRepository suscripciones;
    private final NotificacionRepository notificaciones;
    private final HabitacionRepository habitaciones;
    private final PlanPolicy policy;
    private final Clock clock;

    public PlanPropietarioService(PlanPropietarioRepository planes, PropietarioRepository propietarios,
            SuscripcionPropietarioRepository suscripciones, NotificacionRepository notificaciones,
            HabitacionRepository habitaciones, PlanPolicy policy, Clock clock) {
        this.planes = planes; this.propietarios = propietarios; this.suscripciones = suscripciones;
        this.notificaciones = notificaciones; this.habitaciones = habitaciones; this.policy = policy; this.clock = clock;
    }

    public List<PlanPropietarioResponse> listarPlanesActivos() {
        return planes.findByEstadoOrderByPrecioMensualAsc("activo").stream().map(PlanPropietarioResponse::fromEntity).toList();
    }

    public MiPlanResponse obtenerMiPlan(Integer usuario) {
        var propietario = propietarios.findByUsuarioIdUsuario(usuario).orElseThrow(() -> new ResourceNotFoundException("No tienes perfil de propietario"));
        var suscripcion = suscripciones.findFirstByPropietarioIdPropietarioOrderByFechaInicioDesc(propietario.getIdPropietario())
                .orElseThrow(() -> new ResourceNotFoundException("No tienes una suscripción"));
        var response = MiPlanResponse.fromEntity(suscripcion);
        if (!suscripcion.vigente(LocalDateTime.now(clock))) {
            response.setEstadoSuscripcion("activo".equals(suscripcion.getEstado()) ? "vencido" : suscripcion.getEstado());
            response.setPermiteDestacar(false);
            response.setPermiteEstadisticas(false);
        }
        return response;
    }

    @Transactional
    public MiPlanResponse cambiarPlan(Integer usuario, Integer planId) {
        var propietario = propietarios.lockByUsuarioId(usuario).orElseThrow(() -> new ResourceNotFoundException("No tienes perfil de propietario"));
        policy.exigirPropietarioActivo(propietario);
        var nuevo = planes.findById(planId).orElseThrow(() -> new ResourceNotFoundException("Plan no encontrado"));
        if (!"activo".equals(nuevo.getEstado())) throw new ConflictException("El plan seleccionado no está activo");
        policy.comprobarCupo(propietario, nuevo, 0);
        if (!Boolean.TRUE.equals(nuevo.getPermiteDestacar())
                && habitaciones.countByPropietarioIdPropietarioAndDestacadaTrueAndEstadoNot(propietario.getIdPropietario(), "eliminada") > 0) {
            throw new ConflictException("Quita el destacado de tus habitaciones antes de cambiar a un plan sin esa opción");
        }
        LocalDateTime ahora = LocalDateTime.now(clock);
        var actual = suscripciones.findFirstByPropietarioIdPropietarioAndEstadoOrderByFechaInicioDesc(propietario.getIdPropietario(), "activo");
        actual.ifPresent(anterior -> {
            if (anterior.vigente(ahora) && anterior.getPlan().getIdPlan().equals(nuevo.getIdPlan())) throw new ConflictException("Ya tienes activo ese plan");
            anterior.setEstado(anterior.vigente(ahora) ? "cancelado" : "vencido");
            anterior.setFechaFin(ahora);
            // Flush releases the filtered unique slot before inserting the new subscription.
            suscripciones.saveAndFlush(anterior);
        });
        SuscripcionPropietario siguiente = new SuscripcionPropietario();
        siguiente.setPropietario(propietario);
        siguiente.setPlan(nuevo);
        siguiente.setFechaInicio(ahora);
        siguiente = suscripciones.saveAndFlush(siguiente);
        Notificacion notificacion = new Notificacion();
        notificacion.setUsuario(propietario.getUsuario());
        notificacion.setTitulo("Plan actualizado");
        notificacion.setMensaje("Tu plan cambió a " + nuevo.getNombrePlan() + ".");
        notificacion.setTipo("sistema");
        notificacion.setUrlDestino("/propietario/planes");
        notificaciones.save(notificacion);
        return MiPlanResponse.fromEntity(siguiente);
    }
}
