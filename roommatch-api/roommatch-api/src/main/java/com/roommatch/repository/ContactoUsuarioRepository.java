package com.roommatch.repository;

import com.roommatch.model.ContactoUsuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ContactoUsuarioRepository extends JpaRepository<ContactoUsuario, Integer> {

    Optional<ContactoUsuario> findByUsuarioIdUsuario(Integer idUsuario);

    boolean existsByUsuarioIdUsuario(Integer idUsuario);
}