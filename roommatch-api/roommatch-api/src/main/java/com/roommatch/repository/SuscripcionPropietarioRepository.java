package com.roommatch.repository;

import com.roommatch.model.SuscripcionPropietario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SuscripcionPropietarioRepository extends JpaRepository<SuscripcionPropietario, Integer> {

    Optional<SuscripcionPropietario> findFirstByPropietarioIdPropietarioAndEstadoOrderByFechaInicioDesc(
            Integer idPropietario,
            String estado
    );
}