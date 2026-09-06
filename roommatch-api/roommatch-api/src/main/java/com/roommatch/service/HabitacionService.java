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
import com.roommatch.util.ApiConstants;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

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
        Integer usuarioId = requerirId(idUsuario, "idUsuario");
        Objects.requireNonNull(request, "request");

        Propietario propietario = propietarioRepository.findByUsuarioIdUsuario(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Primero debes convertirte en propietario"
                ));

        Integer propietarioId = requerirId(
                propietario.getIdPropietario(),
                "idPropietario"
        );

        SuscripcionPropietario suscripcion = suscripcionRepository
                .findFirstByPropietarioIdPropietarioAndEstadoOrderByFechaInicioDesc(
                        propietarioId,
                        ApiConstants.ESTADO_ACTIVO
                )
                .orElseThrow(() -> new IllegalArgumentException(
                        "No tienes una suscripción activa"
                ));

        long habitacionesActivas = habitacionRepository
                .countByPropietarioIdPropietarioAndEstadoIn(
                        propietarioId,
                        List.of(ApiConstants.ESTADO_ACTIVA, ApiConstants.ESTADO_PAUSADA)
                );

        Integer limitePlan = Objects.requireNonNull(
                suscripcion.getPlan().getLimiteHabitaciones(),
                "El plan no tiene límite de habitaciones configurado"
        );

        if (habitacionesActivas >= limitePlan) {
            throw new IllegalArgumentException(
                    "Tu plan actual solo permite publicar " + limitePlan + " habitación(es)"
            );
        }

        boolean deseaDestacar = Boolean.TRUE.equals(request.getDestacada());

        if (deseaDestacar && !Boolean.TRUE.equals(suscripcion.getPlan().getPermiteDestacar())) {
            throw new IllegalArgumentException(
                    "Tu plan actual no permite destacar habitaciones"
            );
        }

        Habitacion habitacion = new Habitacion();
        habitacion.setPropietario(propietario);
        copiarDatos(request, habitacion);
        habitacion.setEstado(ApiConstants.ESTADO_ACTIVA);

        Habitacion guardada = habitacionRepository.save(
                Objects.requireNonNull(habitacion)
        );

        return HabitacionResponse.fromEntity(guardada);
    }

    @Transactional(readOnly = true)
    public Page<HabitacionResponse> listarHabitacionesPublicas(
            String distrito,
            BigDecimal precioMin,
            BigDecimal precioMax,
            Boolean amoblado,
            Boolean banoPrivado,
            Boolean permiteMascotas,
            Pageable pageable
    ) {
        Objects.requireNonNull(pageable, "pageable");

        if (precioMin != null && precioMax != null && precioMax.compareTo(precioMin) < 0) {
            throw new IllegalArgumentException(
                    "El precio máximo no puede ser menor que el precio mínimo"
            );
        }

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

    @Transactional(readOnly = true)
    public HabitacionResponse obtenerHabitacionPorId(Integer idHabitacion) {
        Integer habitacionId = requerirId(idHabitacion, "idHabitacion");

        Habitacion habitacion = habitacionRepository.findById(habitacionId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Habitación no encontrada"
                ));

        if (!ApiConstants.ESTADO_ACTIVA.equalsIgnoreCase(habitacion.getEstado())) {
            throw new IllegalArgumentException("La habitación no está disponible");
        }

        return HabitacionResponse.fromEntity(habitacion);
    }

    @Transactional(readOnly = true)
    public Page<HabitacionResponse> listarMisHabitaciones(
            Integer idUsuario,
            Pageable pageable
    ) {
        Integer usuarioId = requerirId(idUsuario, "idUsuario");
        Objects.requireNonNull(pageable, "pageable");

        Propietario propietario = propietarioRepository.findByUsuarioIdUsuario(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No tienes perfil de propietario"
                ));

        Integer propietarioId = requerirId(
                propietario.getIdPropietario(),
                "idPropietario"
        );

        return habitacionRepository
                .findByPropietarioIdPropietarioOrderByFechaPublicacionDesc(
                        propietarioId,
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
        Integer usuarioId = requerirId(idUsuario, "idUsuario");
        Integer habitacionId = requerirId(idHabitacion, "idHabitacion");
        Objects.requireNonNull(request, "request");

        Propietario propietario = propietarioRepository.findByUsuarioIdUsuario(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No tienes perfil de propietario"
                ));

        Integer propietarioId = requerirId(
                propietario.getIdPropietario(),
                "idPropietario"
        );

        Habitacion habitacion = habitacionRepository
                .findByIdHabitacionAndPropietarioIdPropietario(
                        habitacionId,
                        propietarioId
                )
                .orElseThrow(() -> new IllegalArgumentException(
                        "Habitación no encontrada o no te pertenece"
                ));

        SuscripcionPropietario suscripcion = suscripcionRepository
                .findFirstByPropietarioIdPropietarioAndEstadoOrderByFechaInicioDesc(
                        propietarioId,
                        ApiConstants.ESTADO_ACTIVO
                )
                .orElseThrow(() -> new IllegalArgumentException(
                        "No tienes una suscripción activa"
                ));

        boolean deseaDestacar = Boolean.TRUE.equals(request.getDestacada());

        if (deseaDestacar && !Boolean.TRUE.equals(suscripcion.getPlan().getPermiteDestacar())) {
            throw new IllegalArgumentException(
                    "Tu plan actual no permite destacar habitaciones"
            );
        }

        copiarDatos(request, habitacion);

        Habitacion actualizada = habitacionRepository.save(
                Objects.requireNonNull(habitacion)
        );

        return HabitacionResponse.fromEntity(actualizada);
    }

    @Transactional
    public HabitacionResponse pausarHabitacion(
            Integer idUsuario,
            Integer idHabitacion
    ) {
        return cambiarEstado(
                idUsuario,
                idHabitacion,
                ApiConstants.ESTADO_PAUSADA,
                "Habitación no encontrada o no te pertenece"
        );
    }

    @Transactional
    public HabitacionResponse activarHabitacion(
            Integer idUsuario,
            Integer idHabitacion
    ) {
        return cambiarEstado(
                idUsuario,
                idHabitacion,
                ApiConstants.ESTADO_ACTIVA,
                "Habitación no encontrada o no te pertenece"
        );
    }

    private HabitacionResponse cambiarEstado(
            Integer idUsuario,
            Integer idHabitacion,
            String estado,
            String mensajeNoEncontrada
    ) {
        Integer usuarioId = requerirId(idUsuario, "idUsuario");
        Integer habitacionId = requerirId(idHabitacion, "idHabitacion");

        Propietario propietario = propietarioRepository.findByUsuarioIdUsuario(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No tienes perfil de propietario"
                ));

        Integer propietarioId = requerirId(
                propietario.getIdPropietario(),
                "idPropietario"
        );

        Habitacion habitacion = habitacionRepository
                .findByIdHabitacionAndPropietarioIdPropietario(
                        habitacionId,
                        propietarioId
                )
                .orElseThrow(() -> new IllegalArgumentException(mensajeNoEncontrada));

        habitacion.setEstado(estado);

        Habitacion actualizada = habitacionRepository.save(
                Objects.requireNonNull(habitacion)
        );

        return HabitacionResponse.fromEntity(actualizada);
    }

    private void copiarDatos(HabitacionRequest request, Habitacion habitacion) {
        habitacion.setTitulo(request.getTitulo().trim());
        habitacion.setDescripcion(request.getDescripcion().trim());
        habitacion.setDistrito(request.getDistrito().trim());
        habitacion.setDireccionReferencial(normalizarTexto(request.getDireccionReferencial()));
        habitacion.setPrecio(request.getPrecio());
        habitacion.setAreaM2(request.getAreaM2());
        habitacion.setAmoblado(Boolean.TRUE.equals(request.getAmoblado()));
        habitacion.setBanoPrivado(Boolean.TRUE.equals(request.getBanoPrivado()));
        habitacion.setInternetIncluido(Boolean.TRUE.equals(request.getInternetIncluido()));
        habitacion.setAguaIncluida(Boolean.TRUE.equals(request.getAguaIncluida()));
        habitacion.setLuzIncluida(Boolean.TRUE.equals(request.getLuzIncluida()));
        habitacion.setPermiteMascotas(Boolean.TRUE.equals(request.getPermiteMascotas()));
        habitacion.setDisponibleDesde(request.getDisponibleDesde());
        habitacion.setDestacada(Boolean.TRUE.equals(request.getDestacada()));
    }

    private String normalizarTexto(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        return valor.trim();
    }

    private Integer requerirId(Integer id, String nombre) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException(nombre + " debe ser un identificador válido");
        }
        return id;
    }
}
