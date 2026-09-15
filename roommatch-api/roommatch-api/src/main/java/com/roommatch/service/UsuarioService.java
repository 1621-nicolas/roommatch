package com.roommatch.service;

import com.roommatch.exception.ResourceNotFoundException;

import com.roommatch.dto.ActualizarUsuarioRequest;
import com.roommatch.dto.UsuarioResponse;
import com.roommatch.model.Usuario;
import com.roommatch.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional(readOnly = true)
    public UsuarioResponse obtenerMiUsuario(Integer idUsuario) {
        Integer usuarioId = requerirId(idUsuario);

        Usuario usuario = usuarioRepository
                .findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        return UsuarioResponse.fromEntity(usuario);
    }

    @Transactional
    public UsuarioResponse actualizarMiUsuario(
            Integer idUsuario,
            ActualizarUsuarioRequest request
    ) {
        Integer usuarioId = requerirId(idUsuario);
        Objects.requireNonNull(request, "request");

        Usuario usuario = usuarioRepository
                .findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        usuario.setNombres(request.getNombres().trim());
        usuario.setApellidos(request.getApellidos().trim());
        usuario.setEdad(request.getEdad());
        usuario.setOcupacion(normalizarTexto(request.getOcupacion()));
        usuario.setUniversidad(normalizarTexto(request.getUniversidad()));
        usuario.setFoto(normalizarTexto(request.getFoto()));

        Usuario actualizado = usuarioRepository.save(Objects.requireNonNull(usuario));
        return UsuarioResponse.fromEntity(actualizado);
    }

    private String normalizarTexto(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }

    private Integer requerirId(Integer idUsuario) {
        if (idUsuario == null || idUsuario <= 0) {
            throw new IllegalArgumentException("idUsuario debe ser un identificador válido");
        }
        return idUsuario;
    }
}
