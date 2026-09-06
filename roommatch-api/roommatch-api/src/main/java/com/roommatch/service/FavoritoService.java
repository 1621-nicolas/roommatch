package com.roommatch.service;

import com.roommatch.dto.FavoritoResponse;
import com.roommatch.model.FavoritoUsuario;
import com.roommatch.model.Usuario;
import com.roommatch.repository.FavoritoUsuarioRepository;
import com.roommatch.repository.UsuarioRepository;
import com.roommatch.util.ApiConstants;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
public class FavoritoService {

    private final FavoritoUsuarioRepository favoritoRepository;
    private final UsuarioRepository usuarioRepository;

    public FavoritoService(
            FavoritoUsuarioRepository favoritoRepository,
            UsuarioRepository usuarioRepository
    ) {
        this.favoritoRepository = favoritoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public FavoritoResponse agregarFavorito(Integer idUsuario, Integer idUsuarioFavorito) {
        Integer usuarioId = requerirId(idUsuario, "idUsuario");
        Integer favoritoId = requerirId(idUsuarioFavorito, "idUsuarioFavorito");

        if (usuarioId.equals(favoritoId)) {
            throw new IllegalArgumentException("No puedes agregarte a ti mismo como favorito");
        }

        if (favoritoRepository.existsByUsuarioIdUsuarioAndUsuarioFavoritoIdUsuario(
                usuarioId,
                favoritoId
        )) {
            throw new IllegalArgumentException("Este usuario ya está en tus favoritos");
        }

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Usuario autenticado no encontrado"
                ));

        Usuario usuarioFavorito = usuarioRepository.findById(favoritoId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Usuario favorito no encontrado"
                ));

        if (!ApiConstants.ESTADO_ACTIVO.equalsIgnoreCase(usuarioFavorito.getEstado())) {
            throw new IllegalArgumentException(
                    "No puedes agregar un usuario inactivo como favorito"
            );
        }

        FavoritoUsuario favorito = new FavoritoUsuario();
        favorito.setUsuario(usuario);
        favorito.setUsuarioFavorito(usuarioFavorito);

        FavoritoUsuario guardado = favoritoRepository.save(
                Objects.requireNonNull(favorito)
        );

        return FavoritoResponse.fromEntity(guardado);
    }

    @Transactional(readOnly = true)
    public List<FavoritoResponse> listarMisFavoritos(Integer idUsuario) {
        Integer usuarioId = requerirId(idUsuario, "idUsuario");

        return favoritoRepository
                .findByUsuarioIdUsuarioOrderByFechaFavoritoDesc(usuarioId)
                .stream()
                .map(FavoritoResponse::fromEntity)
                .toList();
    }

    @Transactional
    public void eliminarFavorito(Integer idUsuario, Integer idUsuarioFavorito) {
        Integer usuarioId = requerirId(idUsuario, "idUsuario");
        Integer favoritoId = requerirId(idUsuarioFavorito, "idUsuarioFavorito");

        FavoritoUsuario favorito = favoritoRepository
                .findByUsuarioIdUsuarioAndUsuarioFavoritoIdUsuario(
                        usuarioId,
                        favoritoId
                )
                .orElseThrow(() -> new IllegalArgumentException(
                        "El usuario no está en tus favoritos"
                ));

        favoritoRepository.delete(Objects.requireNonNull(favorito));
    }

    private Integer requerirId(Integer id, String nombre) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException(nombre + " debe ser un identificador válido");
        }
        return id;
    }
}
