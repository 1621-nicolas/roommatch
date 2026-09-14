package com.roommatch.service;

import com.roommatch.exception.ResourceNotFoundException;

import com.roommatch.exception.ConflictException;

import com.roommatch.dto.PerfilConvivenciaRequest;
import com.roommatch.dto.PerfilConvivenciaResponse;
import com.roommatch.model.PerfilConvivencia;
import com.roommatch.model.Usuario;
import com.roommatch.repository.PerfilConvivenciaRepository;
import com.roommatch.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.roommatch.repository.PerfilConvivenciaNamedRepository;
import java.util.List;

@Service
public class PerfilConvivenciaService {

    private final PerfilConvivenciaRepository perfilRepository;
    private final UsuarioRepository usuarioRepository;
    private final PerfilConvivenciaNamedRepository perfilNamedRepository;

    public PerfilConvivenciaService(
        PerfilConvivenciaRepository perfilRepository,
        UsuarioRepository usuarioRepository,
        PerfilConvivenciaNamedRepository perfilNamedRepository
) {
    this.perfilRepository = perfilRepository;
    this.usuarioRepository = usuarioRepository;
    this.perfilNamedRepository = perfilNamedRepository;
}

    @Transactional
    public PerfilConvivenciaResponse crearPerfil(Integer idUsuario, PerfilConvivenciaRequest request) {

        if (perfilRepository.existsByUsuarioIdUsuario(idUsuario)) {
            throw new ConflictException("El usuario ya tiene un perfil de convivencia registrado");
        }

        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        validarPresupuesto(request);

        PerfilConvivencia perfil = new PerfilConvivencia();

        perfil.setUsuario(usuario);
        copiarDatos(request, perfil);

        PerfilConvivencia perfilGuardado = perfilRepository.saveAndFlush(perfil);

        return PerfilConvivenciaResponse.fromEntity(perfilGuardado);
    }

    @Transactional(readOnly = true)
    public PerfilConvivenciaResponse obtenerMiPerfil(Integer idUsuario) {

        PerfilConvivencia perfil = perfilRepository.findByUsuarioIdUsuario(idUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("No tienes un perfil de convivencia registrado"));

        return PerfilConvivenciaResponse.fromEntity(perfil);
    }

    @Transactional
    public PerfilConvivenciaResponse actualizarMiPerfil(Integer idUsuario, PerfilConvivenciaRequest request) {

        PerfilConvivencia perfil = perfilRepository.findByUsuarioIdUsuario(idUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("No tienes un perfil de convivencia registrado"));

        comprobarVersion(request.getVersion(), perfil);
        validarPresupuesto(request);

        copiarDatos(request, perfil);

        PerfilConvivencia perfilActualizado = perfilRepository.saveAndFlush(perfil);

        return PerfilConvivenciaResponse.fromEntity(perfilActualizado);
    }
    @Transactional(readOnly = true)
    public List<PerfilConvivenciaResponse> buscarPerfilesPorDistrito(String distrito) {

    if (distrito == null || distrito.trim().isEmpty()) {
        throw new IllegalArgumentException("El distrito es obligatorio");
    }

    return perfilNamedRepository.buscarActivosPorDistrito(distrito)
            .stream()
            .map(PerfilConvivenciaResponse::fromEntity)
            .toList();
}

@Transactional(readOnly = true)
public List<PerfilConvivenciaResponse> buscarCompatiblesBasico(Integer idUsuario) {

    PerfilConvivencia miPerfil = perfilRepository.findByUsuarioIdUsuario(idUsuario)
            .orElseThrow(() -> new IllegalArgumentException("Primero debes completar tu perfil de convivencia"));

    return perfilNamedRepository.buscarCompatiblesPorPresupuesto(
                    idUsuario,
                    miPerfil.getPresupuestoMin(),
                    miPerfil.getPresupuestoMax()
            )
            .stream()
            .map(PerfilConvivenciaResponse::fromEntity)
            .toList();
}

    @Transactional
    public PerfilConvivenciaResponse actualizarDescripcion(Integer idUsuario, com.roommatch.dto.PerfilDescripcionRequest request) {
        PerfilConvivencia perfil = perfilRepository.findByUsuarioIdUsuario(idUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("No tienes un perfil de convivencia registrado"));
        comprobarVersion(request.version(), perfil);
        String text = request.descripcionPersonal();
        if (text == null || text.length() > 500) throw new IllegalArgumentException("La descripción debe tener hasta 500 caracteres");
        perfil.setDescripcionPersonal(text.trim().isEmpty() ? null : text.trim());
        return PerfilConvivenciaResponse.fromEntity(perfilRepository.saveAndFlush(perfil));
    }

    private void comprobarVersion(Long expected, PerfilConvivencia perfil) {
        if (expected == null) throw new IllegalArgumentException("Recarga tu perfil antes de guardar");
        if (expected != perfil.getVersion()) throw new ConflictException("El perfil cambió en otra sesión. Recarga y revisa los datos antes de guardar.");
    }

    private String opcion(String value, String field, String... allowed) {
        String normalized = value == null ? "" : value.trim().toLowerCase(java.util.Locale.ROOT);
        if (!java.util.Set.of(allowed).contains(normalized)) throw new IllegalArgumentException("Valor no válido para " + field);
        return normalized;
    }

    private void copiarDatos(PerfilConvivenciaRequest request, PerfilConvivencia perfil) {
        perfil.setPresupuestoMin(request.getPresupuestoMin());
        perfil.setPresupuestoMax(request.getPresupuestoMax());
        perfil.setDistritoPreferido(request.getDistritoPreferido().trim());
        perfil.setFechaMudanza(request.getFechaMudanza());
        perfil.setLimpieza(request.getLimpieza());
        perfil.setRuido(request.getRuido());
        perfil.setSociabilidad(request.getSociabilidad());
        perfil.setHorario(opcion(request.getHorario(), "horario", "mañana", "tarde", "noche", "variable"));
        perfil.setVisitas(opcion(request.getVisitas(), "visitas", "bajas", "moderadas", "frecuentes"));
        perfil.setMascotas(opcion(request.getMascotas(), "mascotas", "si", "no"));
        perfil.setFumar(opcion(request.getFumar(), "fumar", "si", "no"));
        perfil.setAlcohol(opcion(request.getAlcohol(), "alcohol", "si", "no"));
        perfil.setGastos(opcion(request.getGastos(), "gastos", "divididos", "proporcional"));
        perfil.setConvivencia(opcion(request.getConvivencia(), "convivencia", "tranquila", "social", "independiente", "mixta"));
        perfil.setDescripcionPersonal(request.getDescripcionPersonal());
        perfil.setPerfilCompleto(true);
    }

    private void validarPresupuesto(PerfilConvivenciaRequest request) {
        if (request.getPresupuestoMin() == null || request.getPresupuestoMax() == null) throw new IllegalArgumentException("Completa ambos presupuestos");
        for (var amount : java.util.List.of(request.getPresupuestoMin(), request.getPresupuestoMax())) {
            if (amount.signum() < 0 || amount.scale() > 2 || amount.compareTo(new java.math.BigDecimal("99999999.99")) > 0)
                throw new IllegalArgumentException("El presupuesto debe tener hasta ocho enteros y dos decimales");
        }
        if (request.getPresupuestoMax().compareTo(request.getPresupuestoMin()) < 0) {
            throw new IllegalArgumentException("El presupuesto máximo no puede ser menor que el presupuesto mínimo");
        }
    }
}