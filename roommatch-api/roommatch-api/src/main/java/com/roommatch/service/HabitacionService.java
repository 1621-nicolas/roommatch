package com.roommatch.service;

import com.roommatch.dto.HabitacionRequest;
import com.roommatch.dto.HabitacionResponse;
import com.roommatch.exception.ResourceNotFoundException;
import com.roommatch.exception.ConflictException;
import com.roommatch.model.Habitacion;
import com.roommatch.model.Propietario;
import com.roommatch.repository.HabitacionRepository;
import com.roommatch.repository.PropietarioRepository;
import com.roommatch.repository.HabitacionBusquedaRepository;
import java.math.BigDecimal;
import java.util.Objects;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class HabitacionService {
    private final HabitacionRepository habitacionRepository;
    private final PropietarioRepository propietarioRepository;
    private final PlanPolicy policy;
    private final HabitacionBusquedaRepository busqueda;
    private final ImagePreviewService previews;

    public HabitacionService(HabitacionRepository habitaciones, PropietarioRepository propietarios,
            PlanPolicy policy, HabitacionBusquedaRepository busqueda, ImagePreviewService previews) {
        this.habitacionRepository = habitaciones; this.propietarioRepository = propietarios;
        this.policy = policy; this.busqueda = busqueda; this.previews = previews;
    }

    @Transactional
    public HabitacionResponse crearHabitacion(Integer usuario, HabitacionRequest request) {
        Propietario propietario = propietarioParaEscritura(usuario);
        var plan = policy.vigente(propietario).getPlan();
        policy.comprobarCupo(propietario, plan, 1);
        policy.comprobarDestacada(Boolean.TRUE.equals(request.getDestacada()), plan);
        Habitacion habitacion = new Habitacion();
        habitacion.setPropietario(propietario);
        copiarDatos(request, habitacion);
        return response(habitacionRepository.saveAndFlush(habitacion));
    }

    public Page<HabitacionResponse> listarHabitacionesPublicas(String distrito, BigDecimal minimo, BigDecimal maximo,
            Boolean amoblado, Boolean banoPrivado, Boolean mascotas, Pageable pageable) {
        if (minimo != null && maximo != null && maximo.compareTo(minimo) < 0) throw new IllegalArgumentException("El precio máximo no puede ser menor que el mínimo");
        return responses(busqueda.buscarHabitacionesAvanzado(distrito, minimo, maximo, amoblado, banoPrivado, mascotas, pageable));
    }

    public HabitacionResponse obtenerHabitacionPorId(Integer id) {
        Habitacion habitacion = habitacionRepository.findById(requerirId(id, "idHabitacion"))
                .orElseThrow(() -> new ResourceNotFoundException("Habitación no encontrada"));
        if (!policy.visible(habitacion)) throw new ResourceNotFoundException("La habitación no está disponible");
        return response(habitacion);
    }

    public Page<HabitacionResponse> listarMisHabitaciones(Integer usuario, Pageable pageable) {
        Propietario propietario = propietarioRepository.findByUsuarioIdUsuario(usuario)
                .orElseThrow(() -> new ResourceNotFoundException("No tienes perfil de propietario"));
        return responses(habitacionRepository.findByPropietarioIdPropietarioOrderByFechaPublicacionDesc(propietario.getIdPropietario(), pageable));
    }

    @Transactional
    public HabitacionResponse actualizarHabitacion(Integer usuario, Integer id, HabitacionRequest request) {
        Propietario propietario = propietarioParaEscritura(usuario);
        Habitacion habitacion = propia(propietario, id);
        if ("eliminada".equals(habitacion.getEstado())) throw new ConflictException("La habitación está archivada");
        if (Boolean.TRUE.equals(request.getDestacada())) {
            if (Boolean.TRUE.equals(habitacion.getBloqueada())) throw new AccessDeniedException("La habitación está bloqueada por moderación");
            policy.comprobarDestacada(true, policy.vigente(propietario).getPlan());
        }
        // Content edits and removing a highlight remain possible after expiration.
        copiarDatos(request, habitacion);
        return response(habitacionRepository.saveAndFlush(habitacion));
    }

    @Transactional
    public HabitacionResponse pausarHabitacion(Integer usuario, Integer id) { return cambiarEstado(usuario, id, "pausada"); }
    @Transactional
    public HabitacionResponse activarHabitacion(Integer usuario, Integer id) { return cambiarEstado(usuario, id, "activa"); }
    @Transactional
    public HabitacionResponse alquilarHabitacion(Integer usuario, Integer id) { return cambiarEstado(usuario, id, "alquilada"); }
    @Transactional
    public HabitacionResponse archivarHabitacion(Integer usuario, Integer id) { return cambiarEstado(usuario, id, "eliminada"); }

    private HabitacionResponse cambiarEstado(Integer usuario, Integer id, String estado) {
        Propietario propietario = propietarioParaEscritura(usuario);
        Habitacion habitacion = propia(propietario, id);
        if (estado.equals(habitacion.getEstado())) return response(habitacion);
        if ("eliminada".equals(habitacion.getEstado())) throw new ConflictException("La habitación está archivada");
        if ("activa".equals(estado)) {
            if (Boolean.TRUE.equals(habitacion.getBloqueada())) throw new AccessDeniedException("La habitación está bloqueada por moderación");
            var plan = policy.vigente(propietario).getPlan();
            int adicionales = PlanPolicy.ESTADOS_CON_CUPO.contains(habitacion.getEstado()) ? 0 : 1;
            policy.comprobarCupo(propietario, plan, adicionales);
            policy.comprobarDestacada(Boolean.TRUE.equals(habitacion.getDestacada()), plan);
        } else if ("pausada".equals(estado) && !"activa".equals(habitacion.getEstado())) {
            throw new ConflictException("Solo puedes pausar una habitación activa");
        }
        if ("eliminada".equals(estado) || "alquilada".equals(estado)) habitacion.setDestacada(false);
        habitacion.setEstado(estado);
        return response(habitacionRepository.saveAndFlush(habitacion));
    }

    private Page<HabitacionResponse> responses(Page<Habitacion> page) {
        var images = previews.rooms(page.getContent().stream().map(Habitacion::getIdHabitacion).toList());
        return page.map(room -> {
            var dto = HabitacionResponse.fromEntity(room);
            dto.setImagenPrincipal(images.get(room.getIdHabitacion()));
            return dto;
        });
    }

    private HabitacionResponse response(Habitacion room) {
        return responses(new org.springframework.data.domain.PageImpl<>(java.util.List.of(room))).getContent().get(0);
    }

    private Propietario propietarioParaEscritura(Integer usuario) {
        Propietario propietario = propietarioRepository.lockByUsuarioId(requerirId(usuario, "idUsuario"))
                .orElseThrow(() -> new AccessDeniedException("Primero debes convertirte en propietario"));
        policy.exigirPropietarioActivo(propietario);
        return propietario;
    }

    private Habitacion propia(Propietario propietario, Integer id) {
        return habitacionRepository.findByIdHabitacionAndPropietarioIdPropietario(requerirId(id, "idHabitacion"), propietario.getIdPropietario())
                .orElseThrow(() -> new ResourceNotFoundException("Habitación no encontrada o no te pertenece"));
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
