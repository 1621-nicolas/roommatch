package com.roommatch.repository;

/** Shared read predicate; alias h and parameter ahora are part of its query contract. */
public final class PublicRoomQuery {
    private PublicRoomQuery() { }
    public static final String VISIBLE = """
        h.estado = 'activa' AND h.bloqueada = false AND h.propietario.estado = 'activo'
        AND h.propietario.usuario.estado = 'activo'
        AND EXISTS (select s.idSuscripcion from SuscripcionPropietario s where s.propietario = h.propietario
            and s.estado = 'activo' and s.plan.estado = 'activo' and s.fechaInicio <= :ahora
            and (s.fechaFin is null or s.fechaFin > :ahora))
        """;
}
