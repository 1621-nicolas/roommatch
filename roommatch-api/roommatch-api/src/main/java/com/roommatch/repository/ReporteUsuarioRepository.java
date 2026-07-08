package com.roommatch.repository;

import com.roommatch.model.ReporteUsuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReporteUsuarioRepository extends JpaRepository<ReporteUsuario, Integer> {

    @Query("""
            SELECT r
            FROM ReporteUsuario r
            WHERE (:estado IS NULL OR r.estado = :estado)
            ORDER BY r.fechaReporte DESC
            """)
    Page<ReporteUsuario> listarReportes(
            @Param("estado") String estado,
            Pageable pageable
    );
}