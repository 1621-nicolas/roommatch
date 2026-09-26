package com.roommatch.repository;
import com.roommatch.model.ModeracionEvento;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.domain.*;
public interface ModeracionEventoRepository extends JpaRepository<ModeracionEvento, Integer> {
    @EntityGraph(attributePaths = {"admin", "admin.rol"})
    Page<ModeracionEvento> findByReporteUsuarioIdReporteOrderByFechaDescIdEventoDesc(Integer report, Pageable page);
    @EntityGraph(attributePaths = {"admin", "admin.rol"})
    Page<ModeracionEvento> findByReporteHabitacionIdReporteHabitacionOrderByFechaDescIdEventoDesc(Integer report, Pageable page);
}
