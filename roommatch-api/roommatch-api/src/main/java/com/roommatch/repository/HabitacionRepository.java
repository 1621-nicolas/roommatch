package com.roommatch.repository;

import com.roommatch.model.Habitacion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface HabitacionRepository extends JpaRepository<Habitacion, Integer> {

    Optional<Habitacion> findByIdHabitacionAndPropietarioIdPropietario(
            Integer idHabitacion,
            Integer idPropietario
    );

    Page<Habitacion> findByPropietarioIdPropietarioOrderByFechaPublicacionDesc(
            Integer idPropietario,
            Pageable pageable
    );

    long countByPropietarioIdPropietarioAndEstadoIn(
            Integer idPropietario,
            List<String> estados
    );

    @Query("""
            SELECT h
            FROM Habitacion h
            WHERE h.estado = 'activa'
            AND (:distrito IS NULL OR LOWER(h.distrito) LIKE LOWER(CONCAT('%', :distrito, '%')))
            AND (:precioMin IS NULL OR h.precio >= :precioMin)
            AND (:precioMax IS NULL OR h.precio <= :precioMax)
            AND (:amoblado IS NULL OR h.amoblado = :amoblado)
            AND (:banoPrivado IS NULL OR h.banoPrivado = :banoPrivado)
            AND (:permiteMascotas IS NULL OR h.permiteMascotas = :permiteMascotas)
            ORDER BY h.destacada DESC, h.fechaPublicacion DESC
            """)
    Page<Habitacion> buscarHabitaciones(
            @Param("distrito") String distrito,
            @Param("precioMin") BigDecimal precioMin,
            @Param("precioMax") BigDecimal precioMax,
            @Param("amoblado") Boolean amoblado,
            @Param("banoPrivado") Boolean banoPrivado,
            @Param("permiteMascotas") Boolean permiteMascotas,
            Pageable pageable
    );
}