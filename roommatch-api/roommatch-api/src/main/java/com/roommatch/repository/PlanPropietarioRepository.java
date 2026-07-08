package com.roommatch.repository;

import com.roommatch.model.PlanPropietario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PlanPropietarioRepository extends JpaRepository<PlanPropietario, Integer> {

    Optional<PlanPropietario> findByNombrePlan(String nombrePlan);

    List<PlanPropietario> findByEstadoOrderByPrecioMensualAsc(String estado);
}