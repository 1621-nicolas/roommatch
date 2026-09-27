package com.roommatch.repository;

import com.roommatch.model.Propietario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PropietarioRepository extends JpaRepository<Propietario, Integer> {

    Optional<Propietario> findByUsuarioIdUsuario(Integer idUsuario);

    boolean existsByUsuarioIdUsuario(Integer idUsuario);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Propietario p where p.usuario.idUsuario = :id")
    Optional<Propietario> lockByUsuarioId(@Param("id") Integer id);
}
