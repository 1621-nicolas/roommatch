package com.roommatch.repository;

import com.roommatch.model.SolicitudContacto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SolicitudContactoRepository extends JpaRepository<SolicitudContacto, Integer> {

    boolean existsByUsuarioEmisorIdUsuarioAndUsuarioReceptorIdUsuario(
            Integer idUsuarioEmisor,
            Integer idUsuarioReceptor
    );

    boolean existsByUsuarioEmisorIdUsuarioAndUsuarioReceptorIdUsuarioAndEstado(
            Integer idUsuarioEmisor,
            Integer idUsuarioReceptor,
            String estado
    );

    Optional<SolicitudContacto> findByUsuarioEmisorIdUsuarioAndUsuarioReceptorIdUsuario(
            Integer idUsuarioEmisor,
            Integer idUsuarioReceptor
    );

    List<SolicitudContacto> findByUsuarioReceptorIdUsuarioOrderByFechaSolicitudDesc(
            Integer idUsuarioReceptor
    );

    List<SolicitudContacto> findByUsuarioEmisorIdUsuarioOrderByFechaSolicitudDesc(
            Integer idUsuarioEmisor
    );
}