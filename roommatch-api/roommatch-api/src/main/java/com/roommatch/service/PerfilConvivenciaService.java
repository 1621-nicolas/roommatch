package com.roommatch.service;

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
            throw new IllegalArgumentException("El usuario ya tiene un perfil de convivencia registrado");
        }

        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        validarPresupuesto(request);

        PerfilConvivencia perfil = new PerfilConvivencia();

        perfil.setUsuario(usuario);
        copiarDatos(request, perfil);

        PerfilConvivencia perfilGuardado = perfilRepository.save(perfil);

        return PerfilConvivenciaResponse.fromEntity(perfilGuardado);
    }

    public PerfilConvivenciaResponse obtenerMiPerfil(Integer idUsuario) {

        PerfilConvivencia perfil = perfilRepository.findByUsuarioIdUsuario(idUsuario)
                .orElseThrow(() -> new IllegalArgumentException("No tienes un perfil de convivencia registrado"));

        return PerfilConvivenciaResponse.fromEntity(perfil);
    }

    @Transactional
    public PerfilConvivenciaResponse actualizarMiPerfil(Integer idUsuario, PerfilConvivenciaRequest request) {

        PerfilConvivencia perfil = perfilRepository.findByUsuarioIdUsuario(idUsuario)
                .orElseThrow(() -> new IllegalArgumentException("No tienes un perfil de convivencia registrado"));

        validarPresupuesto(request);

        copiarDatos(request, perfil);

        PerfilConvivencia perfilActualizado = perfilRepository.save(perfil);

        return PerfilConvivenciaResponse.fromEntity(perfilActualizado);
    }
    public List<PerfilConvivenciaResponse> buscarPerfilesPorDistrito(String distrito) {

    if (distrito == null || distrito.trim().isEmpty()) {
        throw new IllegalArgumentException("El distrito es obligatorio");
    }

    return perfilNamedRepository.buscarActivosPorDistrito(distrito)
            .stream()
            .map(PerfilConvivenciaResponse::fromEntity)
            .toList();
}

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

    private void copiarDatos(PerfilConvivenciaRequest request, PerfilConvivencia perfil) {
        perfil.setPresupuestoMin(request.getPresupuestoMin());
        perfil.setPresupuestoMax(request.getPresupuestoMax());
        perfil.setDistritoPreferido(request.getDistritoPreferido());
        perfil.setFechaMudanza(request.getFechaMudanza());
        perfil.setLimpieza(request.getLimpieza());
        perfil.setRuido(request.getRuido());
        perfil.setSociabilidad(request.getSociabilidad());
        perfil.setHorario(request.getHorario());
        perfil.setVisitas(request.getVisitas());
        perfil.setMascotas(request.getMascotas());
        perfil.setFumar(request.getFumar());
        perfil.setAlcohol(request.getAlcohol());
        perfil.setGastos(request.getGastos());
        perfil.setConvivencia(request.getConvivencia());
        perfil.setDescripcionPersonal(request.getDescripcionPersonal());
        perfil.setPerfilCompleto(true);
    }

    private void validarPresupuesto(PerfilConvivenciaRequest request) {
        if (request.getPresupuestoMax().compareTo(request.getPresupuestoMin()) < 0) {
            throw new IllegalArgumentException("El presupuesto máximo no puede ser menor que el presupuesto mínimo");
        }
    }
}