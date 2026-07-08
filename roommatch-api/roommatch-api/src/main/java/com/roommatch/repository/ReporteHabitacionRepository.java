package com.roommatch.repository;

import com.roommatch.model.ReporteHabitacion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReporteHabitacionRepository extends JpaRepository<ReporteHabitacion, Integer> {

    @Query("""
            SELECT r
            FROM ReporteHabitacion r
            WHERE (:estado IS NULL OR r.estado = :estado)
            ORDER BY r.fechaReporte DESC
            """)
    Page<ReporteHabitacion> listarReportes(
            @Param("estado") String estado,
            Pageable pageable
    );
}