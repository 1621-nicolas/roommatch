package com.roommatch.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

@Repository
public class DashboardAdminRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public Long totalUsuarios() {
        return contar("SELECT COUNT(u) FROM Usuario u");
    }

    public Long usuariosPorEstado(String estado) {
        return contarPorEstado(
                "SELECT COUNT(u) FROM Usuario u WHERE u.estado = :estado",
                estado
        );
    }

    public Long totalPropietarios() {
        return contar("SELECT COUNT(p) FROM Propietario p");
    }

    public Long totalPerfilesConvivencia() {
        return contar("SELECT COUNT(p) FROM PerfilConvivencia p");
    }

    public Long totalHabitaciones() {
        return contar("SELECT COUNT(h) FROM Habitacion h");
    }

    public Long habitacionesPorEstado(String estado) {
        return contarPorEstado(
                "SELECT COUNT(h) FROM Habitacion h WHERE h.estado = :estado",
                estado
        );
    }

    public Long totalPublicacionesRoomie() {
        return contar("SELECT COUNT(p) FROM PublicacionRoomie p");
    }

    public Long publicacionesRoomiePorEstado(String estado) {
        return contarPorEstado(
                "SELECT COUNT(p) FROM PublicacionRoomie p WHERE p.estado = :estado",
                estado
        );
    }

    public Long totalMatches() {
        return contar("SELECT COUNT(m) FROM MatchResultado m");
    }

    public Long solicitudesPorEstado(String estado) {
        return contarPorEstado(
                "SELECT COUNT(s) FROM SolicitudContacto s WHERE s.estado = :estado",
                estado
        );
    }

    public Long leadsPorEstado(String estado) {
        return contarPorEstado(
                "SELECT COUNT(l) FROM LeadHabitacion l WHERE l.estado = :estado",
                estado
        );
    }

    public Long reportesUsuariosPorEstado(String estado) {
        return contarPorEstado(
                "SELECT COUNT(r) FROM ReporteUsuario r WHERE r.estado = :estado",
                estado
        );
    }

    public Long reportesHabitacionesPorEstado(String estado) {
        return contarPorEstado(
                "SELECT COUNT(r) FROM ReporteHabitacion r WHERE r.estado = :estado",
                estado
        );
    }

    public Long notificacionesNoLeidas() {
        return entityManager
                .createQuery(
                        "SELECT COUNT(n) FROM Notificacion n WHERE n.leido = false",
                        Long.class
                )
                .getSingleResult();
    }

    private Long contar(String jpql) {
        return entityManager
                .createQuery(jpql, Long.class)
                .getSingleResult();
    }

    private Long contarPorEstado(String jpql, String estado) {
        return entityManager
                .createQuery(jpql, Long.class)
                .setParameter("estado", estado)
                .getSingleResult();
    }
}