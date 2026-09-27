package com.roommatch.repository;

import com.roommatch.model.ReporteUsuario;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface ReporteUsuarioRepository extends JpaRepository<ReporteUsuario, Integer> {
    @EntityGraph(attributePaths = {"usuarioReportante", "usuarioReportante.rol", "usuarioReportado", "usuarioReportado.rol"})
    @Query("select r from ReporteUsuario r where (:estado is null or r.estado=:estado) order by r.fechaReporte desc, r.idReporte desc")
    Page<ReporteUsuario> listarReportes(@Param("estado") String estado, Pageable pageable);

    @Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from ReporteUsuario r where r.idReporte=:id")
    Optional<ReporteUsuario> lockById(@Param("id") Integer id);

    @Query("select (count(r)>0) from ReporteUsuario r where r.usuarioReportante.idUsuario=:reporter and r.usuarioReportado.idUsuario=:target and r.estado in ('pendiente','revisado')")
    boolean hasOpenReport(@Param("reporter") Integer reporter, @Param("target") Integer target);
}
