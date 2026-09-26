IF EXISTS (SELECT id_propietario FROM suscripcion_propietario WHERE estado = 'activo' GROUP BY id_propietario HAVING COUNT(*) > 1)
    THROW 51002, 'Hay más de una suscripción activa por propietario. Revisar antes de migrar.', 1;
GO
CREATE UNIQUE INDEX UX_suscripcion_activa ON suscripcion_propietario(id_propietario) INCLUDE (id_plan, fecha_inicio, fecha_fin) WHERE estado = 'activo';
GO
ALTER TABLE habitacion ADD bloqueada BIT NOT NULL CONSTRAINT DF_habitacion_bloqueada DEFAULT 0 WITH VALUES,
    version BIGINT NOT NULL CONSTRAINT DF_habitacion_version DEFAULT 0 WITH VALUES;
GO
-- Preserve existing moderation decisions even if the owner previously bypassed the pause.
UPDATE habitacion SET bloqueada = 1, destacada = 0
WHERE EXISTS (SELECT 1 FROM reporte_habitacion r WHERE r.id_habitacion = habitacion.id_habitacion AND r.estado = 'sancionado');
-- An old downgrade must not retain a paid capability absent from its active plan.
UPDATE habitacion SET destacada = 0
WHERE destacada = 1 AND NOT EXISTS (
    SELECT 1 FROM suscripcion_propietario s JOIN plan_propietario p ON p.id_plan = s.id_plan
    WHERE s.id_propietario = habitacion.id_propietario AND s.estado = 'activo' AND p.permite_destacar = 1 AND p.estado = 'activo'
);
GO
