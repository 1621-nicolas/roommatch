package com.roommatch.repository;

import com.roommatch.model.FavoritoUsuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FavoritoUsuarioRepository extends JpaRepository<FavoritoUsuario, Integer> {

    boolean existsByUsuarioIdUsuarioAndUsuarioFavoritoIdUsuario(
            Integer idUsuario,
            Integer idUsuarioFavorito
    );

    Optional<FavoritoUsuario> findByUsuarioIdUsuarioAndUsuarioFavoritoIdUsuario(
            Integer idUsuario,
            Integer idUsuarioFavorito
    );

    List<FavoritoUsuario> findByUsuarioIdUsuarioOrderByFechaFavoritoDesc(
            Integer idUsuario
    );
}