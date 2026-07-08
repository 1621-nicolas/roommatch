package com.roommatch.repository;

import com.roommatch.model.PublicacionRoomie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Optional;

public interface PublicacionRoomieRepository extends JpaRepository<PublicacionRoomie, Integer> {

    Optional<PublicacionRoomie> findByIdPublicacionAndUsuarioIdUsuario(
            Integer idPublicacion,
            Integer idUsuario
    );

    Page<PublicacionRoomie> findByUsuarioIdUsuarioOrderByFechaPublicacionDesc(
            Integer idUsuario,
            Pageable pageable
    );

    @Query("""
            SELECT p
            FROM PublicacionRoomie p
            WHERE p.estado = 'activa'
            AND (:tipo IS NULL OR p.tipoPublicacion = :tipo)
            AND (:distrito IS NULL OR LOWER(p.distrito) LIKE LOWER(CONCAT('%', :distrito, '%')))
            AND (:presupuestoMin IS NULL OR p.presupuestoMax >= :presupuestoMin)
            AND (:presupuestoMax IS NULL OR p.presupuestoMin <= :presupuestoMax)
            ORDER BY p.fechaPublicacion DESC
            """)
    Page<PublicacionRoomie> buscarPublicaciones(
            @Param("tipo") String tipo,
            @Param("distrito") String distrito,
            @Param("presupuestoMin") BigDecimal presupuestoMin,
            @Param("presupuestoMax") BigDecimal presupuestoMax,
            Pageable pageable
    );
}