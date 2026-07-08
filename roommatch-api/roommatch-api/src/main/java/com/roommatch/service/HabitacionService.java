package com.roommatch.service;

import com.roommatch.dto.HabitacionRequest;
import com.roommatch.dto.HabitacionResponse;
import com.roommatch.model.Habitacion;
import com.roommatch.model.Propietario;
import com.roommatch.model.SuscripcionPropietario;
import com.roommatch.repository.HabitacionBusquedaRepository;
import com.roommatch.repository.HabitacionRepository;
import com.roommatch.repository.PropietarioRepository;
import com.roommatch.repository.SuscripcionPropietarioRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;

@Service
public class HabitacionService {

    private final HabitacionRepository habitacionRepository;
    private final PropietarioRepository propietarioRepository;
    private final SuscripcionPropietarioRepository suscripcionRepository;
    private final HabitacionBusquedaRepository habitacionBusquedaRepository;
    

   public HabitacionService(
        HabitacionRepository habitacionRepository,
        PropietarioRepository propietarioRepository,
        SuscripcionPropietarioRepository suscripcionRepository,
        HabitacionBusquedaRepository habitacionBusquedaRepository
) {
    this.habitacionRepository = habitacionRepository;
    this.propietarioRepository = propietarioRepository;
    this.suscripcionRepository = suscripcionRepository;
    this.habitacionBusquedaRepository = habitacionBusquedaRepository;
}

    @Transactional
    public HabitacionResponse crearHabitacion(
            Integer idUsuario,
            HabitacionRequest request
    ) {
        Propietario propietario = propietarioRepository.findByUsuarioIdUsuario(idUsuario)
                .orElseThrow(() -> new IllegalArgumentException("Primero debes convertirte en propietario"));

        SuscripcionPropietario suscripcion = suscripcionRepository
                .findFirstByPropietarioIdPropietarioAndEstadoOrderByFechaInicioDesc(
                        propietario.getIdPropietario(),
                        "activo"
                )
                .orElseThrow(() -> new IllegalArgumentException("No tienes una suscripción activa"));

        long habitacionesActivas = habitacionRepository.countByPropietarioIdPropietarioAndEstadoIn(
                propietario.getIdPropietario(),
                List.of("activa", "pausada")
        );

        Integer limitePlan = suscripcion.getPlan().getLimiteHabitaciones();

        if (habitacionesActivas >= limitePlan) {
            throw new IllegalArgumentException(
                    "Tu plan actual solo permite publicar " + limitePlan + " habitación(es)"
            );
        }

        Boolean deseaDestacar = Boolean.TRUE.equals(request.getDestacada());

        if (deseaDestacar && !Boolean.TRUE.equals(suscripcion.getPlan().getPermiteDestacar())) {
            throw new IllegalArgumentException("Tu plan actual no permite destacar habitaciones");
        }

        Habitacion habitacion = new Habitacion();
        habitacion.setPropietario(propietario);
        copiarDatos(request, habitacion);
        habitacion.setEstado("activa");

        Habitacion habitacionGuardada = habitacionRepository.save(habitacion);

        return HabitacionResponse.fromEntity(habitacionGuardada);
    }

    public Page<HabitacionResponse> listarHabitacionesPublicas(
        String distrito,
        BigDecimal precioMin,
        BigDecimal precioMax,
        Boolean amoblado,
        Boolean banoPrivado,
        Boolean permiteMascotas,
        Pageable pageable
) {
    return habitacionBusquedaRepository.buscarHabitacionesAvanzado(
            distrito,
            precioMin,
            precioMax,
            amoblado,
            banoPrivado,
            permiteMascotas,
            pageable
    ).map(HabitacionResponse::fromEntity);
}

    public HabitacionResponse obtenerHabitacionPorId(Integer idHabitacion) {
        Habitacion habitacion = habitacionRepository.findById(idHabitacion)
                .orElseThrow(() -> new IllegalArgumentException("Habitación no encontrada"));

        if (!habitacion.getEstado().equalsIgnoreCase("activa")) {
            throw new IllegalArgumentException("La habitación no está disponible");
        }

        return HabitacionResponse.fromEntity(habitacion);
    }

    public Page<HabitacionResponse> listarMisHabitaciones(
            Integer idUsuario,
            Pageable pageable
    ) {
        Propietario propietario = propietarioRepository.findByUsuarioIdUsuario(idUsuario)
                .orElseThrow(() -> new IllegalArgumentException("No tienes perfil de propietario"));

        return habitacionRepository
                .findByPropietarioIdPropietarioOrderByFechaPublicacionDesc(
                        propietario.getIdPropietario(),
                        pageable
                )
                .map(HabitacionResponse::fromEntity);
    }

    @Transactional
    public HabitacionResponse actualizarHabitacion(
            Integer idUsuario,
            Integer idHabitacion,
            HabitacionRequest request
    ) {
        Propietario propietario = propietarioRepository.findByUsuarioIdUsuario(idUsuario)
                .orElseThrow(() -> new IllegalArgumentException("No tienes perfil de propietario"));

        Habitacion habitacion = habitacionRepository
                .findByIdHabitacionAndPropietarioIdPropietario(
                        idHabitacion,
                        propietario.getIdPropietario()
                )
                .orElseThrow(() -> new IllegalArgumentException("Habitación no encontrada o no te pertenece"));

        SuscripcionPropietario suscripcion = suscripcionRepository
                .findFirstByPropietarioIdPropietarioAndEstadoOrderByFechaInicioDesc(
                        propietario.getIdPropietario(),
                        "activo"
                )
                .orElseThrow(() -> new IllegalArgumentException("No tienes una suscripción activa"));

        Boolean deseaDestacar = Boolean.TRUE.equals(request.getDestacada());

        if (deseaDestacar && !Boolean.TRUE.equals(suscripcion.getPlan().getPermiteDestacar())) {
            throw new IllegalArgumentException("Tu plan actual no permite destacar habitaciones");
        }

        copiarDatos(request, habitacion);

        Habitacion habitacionActualizada = habitacionRepository.save(habitacion);

        return HabitacionResponse.fromEntity(habitacionActualizada);
    }

    @Transactional
    public HabitacionResponse pausarHabitacion(
            Integer idUsuario,
            Integer idHabitacion
    ) {
        Propietario propietario = propietarioRepository.findByUsuarioIdUsuario(idUsuario)
                .orElseThrow(() -> new IllegalArgumentException("No tienes perfil de propietario"));

        Habitacion habitacion = habitacionRepository
                .findByIdHabitacionAndPropietarioIdPropietario(
                        idHabitacion,
                        propietario.getIdPropietario()
                )
                .orElseThrow(() -> new IllegalArgumentException("Habitación no encontrada o no te pertenece"));

        habitacion.setEstado("pausada");

        return HabitacionResponse.fromEntity(habitacionRepository.save(habitacion));
    }

    @Transactional
    public HabitacionResponse activarHabitacion(
            Integer idUsuario,
            Integer idHabitacion
    ) {
        Propietario propietario = propietarioRepository.findByUsuarioIdUsuario(idUsuario)
                .orElseThrow(() -> new IllegalArgumentException("No tienes perfil de propietario"));

        Habitacion habitacion = habitacionRepository
                .findByIdHabitacionAndPropietarioIdPropietario(
                        idHabitacion,
                        propietario.getIdPropietario()
                )
                .orElseThrow(() -> new IllegalArgumentException("Habitación no encontrada o no te pertenece"));

        habitacion.setEstado("activa");

        return HabitacionResponse.fromEntity(habitacionRepository.save(habitacion));
    }

    private void copiarDatos(HabitacionRequest request, Habitacion habitacion) {
        habitacion.setTitulo(request.getTitulo());
        habitacion.setDescripcion(request.getDescripcion());
        habitacion.setDistrito(request.getDistrito());
        habitacion.setDireccionReferencial(request.getDireccionReferencial());
        habitacion.setPrecio(request.getPrecio());
        habitacion.setAreaM2(request.getAreaM2());

        habitacion.setAmoblado(valorBoolean(request.getAmoblado(), false));
        habitacion.setBanoPrivado(valorBoolean(request.getBanoPrivado(), false));
        habitacion.setInternetIncluido(valorBoolean(request.getInternetIncluido(), false));
        habitacion.setAguaIncluida(valorBoolean(request.getAguaIncluida(), false));
        habitacion.setLuzIncluida(valorBoolean(request.getLuzIncluida(), false));
        habitacion.setPermiteMascotas(valorBoolean(request.getPermiteMascotas(), false));

        habitacion.setDisponibleDesde(request.getDisponibleDesde());
        habitacion.setDestacada(valorBoolean(request.getDestacada(), false));
    }

    private Boolean valorBoolean(Boolean valor, Boolean defecto) {
        return valor != null ? valor : defecto;
    }
}