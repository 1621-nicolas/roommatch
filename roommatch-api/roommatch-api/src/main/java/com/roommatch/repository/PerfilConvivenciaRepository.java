package com.roommatch.repository;

import com.roommatch.model.PerfilConvivencia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PerfilConvivenciaRepository extends JpaRepository<PerfilConvivencia, Integer> {

    Optional<PerfilConvivencia> findByUsuarioIdUsuario(Integer idUsuario);

    boolean existsByUsuarioIdUsuario(Integer idUsuario);
}