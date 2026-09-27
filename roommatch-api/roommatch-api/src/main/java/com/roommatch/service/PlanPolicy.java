package com.roommatch.service;

import com.roommatch.exception.ConflictException;
import com.roommatch.model.Habitacion;
import com.roommatch.model.PlanPropietario;
import com.roommatch.model.Propietario;
import com.roommatch.model.SuscripcionPropietario;
import com.roommatch.repository.HabitacionRepository;
import com.roommatch.repository.SuscripcionPropietarioRepository;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

@Component
public class PlanPolicy {
    public static final List<String> ESTADOS_CON_CUPO = List.of("activa", "pausada");
    private final SuscripcionPropietarioRepository suscripciones;
    private final HabitacionRepository habitaciones;
    private final Clock clock;

    public PlanPolicy(SuscripcionPropietarioRepository suscripciones, HabitacionRepository habitaciones, Clock clock) {
        this.suscripciones = suscripciones; this.habitaciones = habitaciones; this.clock = clock;
    }

    public SuscripcionPropietario vigente(Propietario propietario) {
        exigirPropietarioActivo(propietario);
        return suscripciones.findFirstByPropietarioIdPropietarioAndEstadoOrderByFechaInicioDesc(propietario.getIdPropietario(), "activo")
                .filter(s -> s.vigente(LocalDateTime.now(clock)))
                .orElseThrow(() -> new ConflictException("Tu suscripción no está vigente. Revisa tu plan para publicar o reactivar."));
    }

    public void exigirPropietarioActivo(Propietario propietario) {
        if (!"activo".equals(propietario.getEstado()) || !"activo".equals(propietario.getUsuario().getEstado())) {
            throw new AccessDeniedException("El perfil de propietario no está activo");
        }
    }

    public void comprobarCupo(Propietario propietario, PlanPropietario plan, int adicionales) {
        long ocupadas = habitaciones.countByPropietarioIdPropietarioAndEstadoIn(propietario.getIdPropietario(), ESTADOS_CON_CUPO);
        if (ocupadas + adicionales > plan.getLimiteHabitaciones()) {
            throw new ConflictException("Tu plan permite " + plan.getLimiteHabitaciones() + " habitación(es) activa(s) o pausada(s). "
                    + "Tienes " + ocupadas + ". Marca como alquiladas o archiva las que ya no ofreces antes de continuar.");
        }
    }

    public void comprobarDestacada(boolean destacar, PlanPropietario plan) {
        if (destacar && !Boolean.TRUE.equals(plan.getPermiteDestacar())) throw new AccessDeniedException("Tu plan no permite destacar habitaciones");
    }

    public boolean visible(Habitacion habitacion) {
        Propietario propietario = habitacion.getPropietario();
        return "activa".equals(habitacion.getEstado()) && !Boolean.TRUE.equals(habitacion.getBloqueada())
                && "activo".equals(propietario.getEstado()) && "activo".equals(propietario.getUsuario().getEstado())
                && suscripciones.findFirstByPropietarioIdPropietarioAndEstadoOrderByFechaInicioDesc(propietario.getIdPropietario(), "activo")
                    .filter(s -> s.vigente(LocalDateTime.now(clock))).isPresent();
    }
}
