package com.roommatch.repository;

import com.roommatch.model.Habitacion;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class HabitacionBusquedaRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public Page<Habitacion> buscarHabitacionesAvanzado(
            String distrito,
            BigDecimal precioMin,
            BigDecimal precioMax,
            Boolean amoblado,
            Boolean banoPrivado,
            Boolean permiteMascotas,
            Pageable pageable
    ) {
        StringBuilder where = new StringBuilder();
        Map<String, Object> parametros = new HashMap<>();

        where.append(" WHERE h.estado = :estado ");
        parametros.put("estado", "activa");

        if (distrito != null && !distrito.trim().isEmpty()) {
            where.append(" AND LOWER(h.distrito) LIKE LOWER(:distrito) ");
            parametros.put("distrito", "%" + distrito.trim() + "%");
        }

        if (precioMin != null) {
            where.append(" AND h.precio >= :precioMin ");
            parametros.put("precioMin", precioMin);
        }

        if (precioMax != null) {
            where.append(" AND h.precio <= :precioMax ");
            parametros.put("precioMax", precioMax);
        }

        if (amoblado != null) {
            where.append(" AND h.amoblado = :amoblado ");
            parametros.put("amoblado", amoblado);
        }

        if (banoPrivado != null) {
            where.append(" AND h.banoPrivado = :banoPrivado ");
            parametros.put("banoPrivado", banoPrivado);
        }

        if (permiteMascotas != null) {
            where.append(" AND h.permiteMascotas = :permiteMascotas ");
            parametros.put("permiteMascotas", permiteMascotas);
        }

        String jpql = """
                SELECT h
                FROM Habitacion h
                """ + where + """
                ORDER BY h.destacada DESC, h.fechaPublicacion DESC
                """;

        TypedQuery<Habitacion> query = entityManager.createQuery(jpql, Habitacion.class);

        parametros.forEach(query::setParameter);

        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());

        List<Habitacion> habitaciones = query.getResultList();

        String jpqlCount = """
                SELECT COUNT(h)
                FROM Habitacion h
                """ + where;

        TypedQuery<Long> countQuery = entityManager.createQuery(jpqlCount, Long.class);

        parametros.forEach(countQuery::setParameter);

        Long total = countQuery.getSingleResult();

        return new PageImpl<>(habitaciones, pageable, total);
    }
}