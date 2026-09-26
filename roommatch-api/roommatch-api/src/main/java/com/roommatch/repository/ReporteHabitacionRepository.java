package com.roommatch.repository;

import com.roommatch.model.ReporteHabitacion;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface ReporteHabitacionRepository extends JpaRepository<ReporteHabitacion, Integer> {
    @EntityGraph(attributePaths = {"usuarioReportante", "usuarioReportante.rol", "habitacion", "habitacion.propietario", "habitacion.propietario.usuario", "habitacion.propietario.usuario.rol"})
    @Query("select r from ReporteHabitacion r where (:estado is null or r.estado=:estado) order by r.fechaReporte desc, r.idReporteHabitacion desc")
    Page<ReporteHabitacion> listarReportes(@Param("estado") String estado, Pageable pageable);

    @Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from ReporteHabitacion r where r.idReporteHabitacion=:id")
    Optional<ReporteHabitacion> lockById(@Param("id") Integer id);

    @Query("select (count(r)>0) from ReporteHabitacion r where r.usuarioReportante.idUsuario=:reporter and r.habitacion.idHabitacion=:target and r.estado in ('pendiente','revisado')")
    boolean hasOpenReport(@Param("reporter") Integer reporter, @Param("target") Integer target);
}
