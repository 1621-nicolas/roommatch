package com.roommatch.service;

import com.roommatch.dto.FavoritoResponse;
import com.roommatch.model.FavoritoUsuario;
import com.roommatch.model.Usuario;
import com.roommatch.repository.FavoritoUsuarioRepository;
import com.roommatch.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

        if (idUsuario.equals(idUsuarioFavorito)) {
            throw new IllegalArgumentException("No puedes agregarte a ti mismo como favorito");
        }

        if (favoritoRepository.existsByUsuarioIdUsuarioAndUsuarioFavoritoIdUsuario(idUsuario, idUsuarioFavorito)) {
            throw new IllegalArgumentException("Este usuario ya está en tus favoritos");
        }

        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new IllegalArgumentException("Usuario autenticado no encontrado"));

        Usuario usuarioFavorito = usuarioRepository.findById(idUsuarioFavorito)
                .orElseThrow(() -> new IllegalArgumentException("Usuario favorito no encontrado"));

        if (!usuarioFavorito.getEstado().equalsIgnoreCase("activo")) {
            throw new IllegalArgumentException("No puedes agregar un usuario inactivo como favorito");
        }

        FavoritoUsuario favorito = new FavoritoUsuario();
        favorito.setUsuario(usuario);
        favorito.setUsuarioFavorito(usuarioFavorito);

        FavoritoUsuario favoritoGuardado = favoritoRepository.save(favorito);

        return FavoritoResponse.fromEntity(favoritoGuardado);
    }

    public List<FavoritoResponse> listarMisFavoritos(Integer idUsuario) {
        return favoritoRepository.findByUsuarioIdUsuarioOrderByFechaFavoritoDesc(idUsuario)
                .stream()
                .map(FavoritoResponse::fromEntity)
                .toList();
    }

    @Transactional
    public void eliminarFavorito(Integer idUsuario, Integer idUsuarioFavorito) {

        FavoritoUsuario favorito = favoritoRepository
                .findByUsuarioIdUsuarioAndUsuarioFavoritoIdUsuario(idUsuario, idUsuarioFavorito)
                .orElseThrow(() -> new IllegalArgumentException("El usuario no está en tus favoritos"));

        favoritoRepository.delete(favorito);
    }
}