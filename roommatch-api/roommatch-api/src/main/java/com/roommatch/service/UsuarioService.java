package com.roommatch.service;

import com.roommatch.dto.ActualizarUsuarioRequest;
import com.roommatch.dto.UsuarioResponse;
import com.roommatch.model.Usuario;
import com.roommatch.repository.UsuarioRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioService(
            UsuarioRepository usuarioRepository
    ) {
        this.usuarioRepository = usuarioRepository;
    }

    public UsuarioResponse obtenerMiUsuario(
            Integer idUsuario
    ) {

        Usuario usuario = usuarioRepository
                .findById(idUsuario)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Usuario no encontrado"
                        )
                );

        return UsuarioResponse.fromEntity(
                usuario
        );
    }

    @Transactional
    public UsuarioResponse actualizarMiUsuario(
            Integer idUsuario,
            ActualizarUsuarioRequest request
    ) {

        Usuario usuario = usuarioRepository
                .findById(idUsuario)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Usuario no encontrado"
                        )
                );

        usuario.setNombres(
                request.getNombres().trim()
        );

        usuario.setApellidos(
                request.getApellidos().trim()
        );

        usuario.setEdad(
                request.getEdad()
        );

        usuario.setOcupacion(
                normalizarTexto(
                        request.getOcupacion()
                )
        );

        usuario.setUniversidad(
                normalizarTexto(
                        request.getUniversidad()
                )
        );

        usuario.setFoto(
                normalizarTexto(
                        request.getFoto()
                )
        );

        Usuario usuarioActualizado =
                usuarioRepository.save(usuario);

        return UsuarioResponse.fromEntity(
                usuarioActualizado
        );
    }

    private String normalizarTexto(
            String valor
    ) {

        if (
                valor == null ||
                valor.isBlank()
        ) {
            return null;
        }

        return valor.trim();
    }
}