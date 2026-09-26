package com.roommatch.repository;

import com.roommatch.model.PublicacionRoomie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.EntityGraph;

public interface PublicacionRoomieRepository extends JpaRepository<PublicacionRoomie, Integer> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<PublicacionRoomie> findByIdPublicacionAndUsuarioIdUsuario(
            Integer idPublicacion,
            Integer idUsuario
    );

    @EntityGraph(attributePaths = {"usuario", "usuario.rol", "habitacion", "habitacion.propietario", "habitacion.propietario.usuario", "habitacion.propietario.usuario.rol"})
    Page<PublicacionRoomie> findByUsuarioIdUsuarioAndEstadoNotOrderByFechaPublicacionDesc(
            Integer idUsuario,
            String estado,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {"usuario", "usuario.rol", "habitacion", "habitacion.propietario", "habitacion.propietario.usuario", "habitacion.propietario.usuario.rol"})
    @Query("""
            SELECT p
            FROM PublicacionRoomie p
            WHERE p.estado = 'activa' AND p.usuario.estado = 'activo'
            AND (:tipo IS NULL OR p.tipoPublicacion = :tipo)
            AND (:distrito IS NULL OR LOWER(p.distrito) LIKE LOWER(CONCAT('%', :distrito, '%')))
            AND (:presupuestoMin IS NULL OR p.presupuestoMax >= :presupuestoMin)
            AND (:presupuestoMax IS NULL OR p.presupuestoMin <= :presupuestoMax)
            ORDER BY p.fechaPublicacion DESC, p.idPublicacion DESC
            """)
    Page<PublicacionRoomie> buscarPublicaciones(
            @Param("tipo") String tipo,
            @Param("distrito") String distrito,
            @Param("presupuestoMin") BigDecimal presupuestoMin,
            @Param("presupuestoMax") BigDecimal presupuestoMax,
            Pageable pageable
    );
    @Override
    @EntityGraph(attributePaths = {"usuario", "usuario.rol", "habitacion", "habitacion.propietario", "habitacion.propietario.usuario", "habitacion.propietario.usuario.rol"})
    Optional<PublicacionRoomie> findById(Integer id);
}