package com.roommatch.service;

import com.roommatch.dto.*;
import com.roommatch.exception.ConflictException;
import com.roommatch.exception.ResourceNotFoundException;
import com.roommatch.model.*;
import com.roommatch.repository.*;
import com.roommatch.util.ApiConstants;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class PublicacionRoomieService {
    private final PublicacionRoomieRepository publicaciones;
    private final UsuarioRepository usuarios;
    private final HabitacionRepository habitaciones;
    private final MatchService matches;
    private final PlanPolicy planPolicy;
    private final Clock clock;

    public PublicacionRoomieService(PublicacionRoomieRepository publicaciones, UsuarioRepository usuarios,
            MatchService matches, HabitacionRepository habitaciones, PlanPolicy planPolicy, Clock clock) {
        this.publicaciones = publicaciones; this.usuarios = usuarios; this.matches = matches;
        this.habitaciones = habitaciones; this.planPolicy = planPolicy; this.clock = clock;
    }

    @Transactional
    public PublicacionRoomieResponse crearPublicacion(Integer usuarioId, PublicacionRoomieRequest request) {
        Usuario usuario = usuarios.findById(usuarioId).orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        if (!"activo".equals(usuario.getEstado())) throw new AccessDeniedException("Tu cuenta no está activa");
        validar(request);
        PublicacionRoomie publicacion = new PublicacionRoomie();
        publicacion.setUsuario(usuario);
        copiarDatos(request, publicacion);
        aplicarVinculacionVivienda(request, publicacion);
        return response(publicaciones.saveAndFlush(publicacion), usuarioId);
    }

    public Page<PublicacionRoomieResponse> listarPublicaciones(Integer actual, String tipo, String distrito,
            BigDecimal minimo, BigDecimal maximo, Pageable pageable) {
        validarPagina(pageable);
        tipo = normalizarTexto(tipo);
        if (tipo != null) validarTipoPublicacion(tipo);
        if (minimo != null && maximo != null && maximo.compareTo(minimo) < 0) throw new IllegalArgumentException("El presupuesto máximo no puede ser menor que el mínimo");
        return responses(publicaciones.buscarPublicaciones(tipo, texto(distrito), minimo, maximo, pageable), actual);
    }

    public PublicacionRoomieResponse obtenerPublicacion(Integer actual, Integer id) {
        PublicacionRoomie publicacion = publicaciones.findById(id).orElseThrow(() -> new ResourceNotFoundException("Publicación no encontrada"));
        if (!"activa".equals(publicacion.getEstado()) || !"activo".equals(publicacion.getUsuario().getEstado()))
            throw new ResourceNotFoundException("La publicación no está disponible");
        return response(publicacion, actual);
    }

    public Page<PublicacionRoomieResponse> listarMisPublicaciones(Integer usuario, Pageable pageable) {
        validarPagina(pageable);
        return responses(publicaciones.findByUsuarioIdUsuarioAndEstadoNotOrderByFechaPublicacionDesc(usuario, "eliminada", pageable), usuario);
    }

    @Transactional
    public PublicacionRoomieResponse actualizarPublicacion(Integer usuario, Integer id, PublicacionRoomieRequest request) {
        validar(request);
        PublicacionRoomie publicacion = propia(usuario, id);
        copiarDatos(request, publicacion);
        aplicarVinculacionVivienda(request, publicacion);
        return response(publicaciones.saveAndFlush(publicacion), usuario);
    }

    @Transactional
    public PublicacionRoomieResponse pausarPublicacion(Integer usuario, Integer id) { return cambiarEstado(usuario, id, "pausada"); }
    @Transactional
    public PublicacionRoomieResponse activarPublicacion(Integer usuario, Integer id) { return cambiarEstado(usuario, id, "activa"); }
    @Transactional
    public PublicacionRoomieResponse cerrarPublicacion(Integer usuario, Integer id) { return cambiarEstado(usuario, id, "cerrada"); }

    @Transactional
    public void eliminarPublicacion(Integer usuario, Integer id) {
        PublicacionRoomie publicacion = publicaciones.findByIdPublicacionAndUsuarioIdUsuario(id, usuario)
                .orElseThrow(() -> new ResourceNotFoundException("La publicación no existe o no te pertenece"));
        if (!"eliminada".equals(publicacion.getEstado())) {
            publicacion.setEstado("eliminada");
            publicaciones.saveAndFlush(publicacion);
        }
    }

    private PublicacionRoomieResponse cambiarEstado(Integer usuario, Integer id, String estado) {
        PublicacionRoomie publicacion = propia(usuario, id);
        if ("activa".equals(estado) && publicacion.getHabitacion() != null && !planPolicy.visible(publicacion.getHabitacion()))
            throw new ConflictException("La habitación vinculada ya no está disponible. Edita la referencia antes de reactivar.");
        publicacion.setEstado(estado);
        return response(publicaciones.saveAndFlush(publicacion), usuario);
    }

    private PublicacionRoomie propia(Integer usuario, Integer id) {
        PublicacionRoomie publicacion = publicaciones.findByIdPublicacionAndUsuarioIdUsuario(id, usuario)
                .orElseThrow(() -> new ResourceNotFoundException("Publicación no encontrada o no te pertenece"));
        if ("eliminada".equals(publicacion.getEstado())) throw new ResourceNotFoundException("La publicación fue eliminada");
        if (!"activo".equals(publicacion.getUsuario().getEstado())) throw new AccessDeniedException("Tu cuenta no está activa");
        return publicacion;
    }

    private Page<PublicacionRoomieResponse> responses(Page<PublicacionRoomie> page, Integer actual) {
        List<PublicacionRoomie> rows = page.getContent();
        Map<Integer, CompatibilidadCalculada> compatibility = matches.obtenerCompatibilidades(actual,
                rows.stream().map(p -> p.getUsuario().getIdUsuario()).toList());
        Set<Integer> linkedIds = rows.stream().map(PublicacionRoomie::getHabitacion).filter(Objects::nonNull)
                .map(Habitacion::getIdHabitacion).collect(Collectors.toSet());
        Set<Integer> visible = linkedIds.isEmpty() ? Set.of() : new HashSet<>(habitaciones.findPublicIds(linkedIds, LocalDateTime.now(clock)));
        return page.map(publicacion -> {
            PublicacionRoomieResponse dto = PublicacionRoomieResponse.fromEntity(publicacion,
                    compatibility.get(publicacion.getUsuario().getIdUsuario()), actual);
            boolean disponible = publicacion.getHabitacion() != null && visible.contains(publicacion.getHabitacion().getIdHabitacion());
            dto.setViviendaReferenciaDisponible(disponible);
            if (publicacion.getHabitacion() != null && !disponible) {
                dto.setIdHabitacion(null); dto.setHabitacionTitulo(null); dto.setHabitacionDistrito(null);
                dto.setHabitacionPrecio(null); dto.setNombrePropietarioHabitacion(null);
            }
            return dto;
        });
    }

    private PublicacionRoomieResponse response(PublicacionRoomie publicacion, Integer actual) {
        return responses(new PageImpl<>(List.of(publicacion)), actual).getContent().get(0);
    }

    private void aplicarVinculacionVivienda(PublicacionRoomieRequest request, PublicacionRoomie publicacion) {
        String tipo = normalizarTexto(request.getTipoVinculacionVivienda());
        publicacion.setTipoVinculacionVivienda(tipo);
        publicacion.setHabitacion(null);
        publicacion.setViviendaExternaTitulo(null);
        publicacion.setViviendaExternaDireccion(null);
        publicacion.setViviendaExternaPrecio(null);
        if ("roommatch".equals(tipo)) {
            Habitacion habitacion = habitaciones.findById(request.getIdHabitacion()).orElseThrow(() -> new ResourceNotFoundException("La habitación seleccionada no existe"));
            // A public reference helps find someone to share; it grants no owner capability.
            if (!planPolicy.visible(habitacion)) throw new ConflictException("La habitación seleccionada no está disponible");
            publicacion.setHabitacion(habitacion);
        } else if ("externa".equals(tipo)) {
            publicacion.setViviendaExternaTitulo(request.getViviendaExternaTitulo().trim());
            publicacion.setViviendaExternaDireccion(texto(request.getViviendaExternaDireccion()));
            publicacion.setViviendaExternaPrecio(request.getViviendaExternaPrecio());
        }
    }

    private void validar(PublicacionRoomieRequest request) {
        validarTipoPublicacion(request.getTipoPublicacion()); validarPresupuesto(request); validarVinculacionVivienda(request);
    }
    private void validarPagina(Pageable page) {
        if (page.getPageSize() > 100 || page.getPageSize() < 1 || page.getPageNumber() > 10000)
            throw new IllegalArgumentException("Usa un tamaño de página entre 1 y 100");
    }
    private String texto(String valor) { return valor == null || valor.isBlank() ? null : valor.trim(); }
    private String normalizarTexto(String valor) { return texto(valor) == null ? null : texto(valor).toLowerCase(java.util.Locale.ROOT); }

    private void copiarDatos(
            PublicacionRoomieRequest request,
            PublicacionRoomie publicacion
    ) {
        publicacion.setTipoPublicacion(
                request
                        .getTipoPublicacion()
                        .trim()
                        .toLowerCase(java.util.Locale.ROOT)
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

    private void validarVinculacionVivienda(
            PublicacionRoomieRequest request
    ) {
        String tipoPublicacion =
                request
                        .getTipoPublicacion()
                        .trim()
                        .toLowerCase(java.util.Locale.ROOT);
        String tipoVinculacion =
                normalizarTexto(
                        request
                                .getTipoVinculacionVivienda()
                );
        if (
                tipoVinculacion == null
        ) {
            return;
        }
        if (
                !tipoPublicacion.equals(
                        ApiConstants.TIPO_BUSCO_COMPARTIR
                )
        ) {
            throw new IllegalArgumentException(
                    "Solo una publicación de tipo busco_compartir puede vincular una vivienda"
            );
        }
        if (
                !tipoVinculacion.equals("roommatch")
                &&
                !tipoVinculacion.equals("externa")
        ) {
            throw new IllegalArgumentException(
                    "Tipo de vinculación no válido. Usa: roommatch o externa"
            );
        }
        if (
                tipoVinculacion.equals("roommatch")
                &&
                request.getIdHabitacion() == null
        ) {
            throw new IllegalArgumentException(
                    "Debes seleccionar una habitación de RoomMatch"
            );
        }
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
                                .toLowerCase(java.util.Locale.ROOT)
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
