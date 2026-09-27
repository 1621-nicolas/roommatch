package com.roommatch.repository;

import com.roommatch.model.LeadHabitacion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LeadHabitacionRepository extends JpaRepository<LeadHabitacion, Integer> {

    boolean existsByHabitacionIdHabitacionAndUsuarioInteresadoIdUsuario(
            Integer idHabitacion,
            Integer idUsuarioInteresado
    );

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"usuarioInteresado", "usuarioInteresado.rol", "habitacion", "habitacion.propietario", "habitacion.propietario.usuario", "habitacion.propietario.usuario.rol"})
    List<LeadHabitacion> findByUsuarioInteresadoIdUsuarioOrderByFechaLeadDesc(
            Integer idUsuarioInteresado
    );

    Optional<LeadHabitacion> findByIdLeadAndHabitacionPropietarioIdPropietario(
            Integer idLead,
            Integer idPropietario
    );

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"usuarioInteresado", "usuarioInteresado.rol", "habitacion", "habitacion.propietario", "habitacion.propietario.usuario", "habitacion.propietario.usuario.rol"})
    @Query("""
            SELECT l
            FROM LeadHabitacion l
            WHERE l.habitacion.propietario.idPropietario = :idPropietario
            AND (:estado IS NULL OR l.estado = :estado)
            ORDER BY l.fechaLead DESC, l.idLead DESC
            """)
    Page<LeadHabitacion> listarLeadsDePropietario(
            @Param("idPropietario") Integer idPropietario,
            @Param("estado") String estado,
            Pageable pageable
    );
}