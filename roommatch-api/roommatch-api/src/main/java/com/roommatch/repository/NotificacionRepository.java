package com.roommatch.repository;

import com.roommatch.model.Notificacion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface NotificacionRepository extends JpaRepository<Notificacion, Integer> {

    @Query("""
            SELECT n
            FROM Notificacion n
            WHERE n.usuario.idUsuario = :idUsuario
            AND (:leido IS NULL OR n.leido = :leido)
            ORDER BY n.fechaCreacion DESC
            """)
    Page<Notificacion> listarPorUsuario(
            @Param("idUsuario") Integer idUsuario,
            @Param("leido") Boolean leido,
            Pageable pageable
    );

    Optional<Notificacion> findByIdNotificacionAndUsuarioIdUsuario(
            Integer idNotificacion,
            Integer idUsuario
    );

    long countByUsuarioIdUsuarioAndLeidoFalse(Integer idUsuario);

    @Modifying
    @Query("""
            UPDATE Notificacion n
            SET n.leido = true
            WHERE n.usuario.idUsuario = :idUsuario
            AND n.leido = false
            """)
    int marcarTodasComoLeidas(@Param("idUsuario") Integer idUsuario);
}