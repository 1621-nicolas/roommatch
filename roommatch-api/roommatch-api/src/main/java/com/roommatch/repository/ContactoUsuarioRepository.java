package com.roommatch.repository;

import com.roommatch.model.ContactoUsuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ContactoUsuarioRepository extends JpaRepository<ContactoUsuario, Integer> {

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"usuario", "usuario.rol"})
    java.util.List<ContactoUsuario> findByUsuarioIdUsuarioIn(java.util.Collection<Integer> ids);

    Optional<ContactoUsuario> findByUsuarioIdUsuario(Integer idUsuario);

    boolean existsByUsuarioIdUsuario(Integer idUsuario);
}
