package com.roommatch.repository;

import com.roommatch.model.PerfilConvivencia;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public class PerfilConvivenciaNamedRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public List<PerfilConvivencia> buscarActivosPorDistrito(String distrito) {
        return entityManager
                .createNamedQuery(
                        "PerfilConvivencia.buscarActivosPorDistrito",
                        PerfilConvivencia.class
                )
                .setParameter("distrito", "%" + distrito.trim() + "%")
                .getResultList();
    }

    public List<PerfilConvivencia> buscarCompatiblesPorPresupuesto(
            Integer idUsuario,
            BigDecimal presupuestoMin,
            BigDecimal presupuestoMax
    ) {
        return entityManager
                .createNamedQuery(
                        "PerfilConvivencia.buscarCompatiblesPorPresupuesto",
                        PerfilConvivencia.class
                )
                .setParameter("idUsuario", idUsuario)
                .setParameter("presupuestoMin", presupuestoMin)
                .setParameter("presupuestoMax", presupuestoMax)
                .getResultList();
    }
}