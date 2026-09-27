package com.roommatch.repository;

import com.roommatch.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    Optional<Usuario> findByEmail(String email);

    boolean existsByEmail(String email);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from Usuario u where u.idUsuario = :id")
    Optional<Usuario> lockById(@Param("id") Integer id);
}
