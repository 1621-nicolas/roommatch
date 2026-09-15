-- Preserve history; stop if old concurrency anomalies need reconciliation.
IF EXISTS (
    SELECT 1 FROM solicitud_contacto WHERE estado = 'pendiente'
    GROUP BY CASE WHEN id_usuario_emisor < id_usuario_receptor THEN id_usuario_emisor ELSE id_usuario_receptor END,
             CASE WHEN id_usuario_emisor < id_usuario_receptor THEN id_usuario_receptor ELSE id_usuario_emisor END
    HAVING COUNT(*) > 1
) THROW 51000, 'Hay solicitudes pendientes duplicadas entre dos usuarios. Revisar el historial antes de migrar.', 1;

IF EXISTS (
    SELECT 1 FROM contacto_roomie
    GROUP BY CASE WHEN id_usuario_a < id_usuario_b THEN id_usuario_a ELSE id_usuario_b END,
             CASE WHEN id_usuario_a < id_usuario_b THEN id_usuario_b ELSE id_usuario_a END
    HAVING COUNT(*) > 1
) THROW 51001, 'Hay contactos duplicados entre dos usuarios. Reconciliar sin perder historial antes de migrar.', 1;
GO

ALTER TABLE solicitud_contacto ADD
    usuario_menor AS (CASE WHEN id_usuario_emisor < id_usuario_receptor THEN id_usuario_emisor ELSE id_usuario_receptor END) PERSISTED,
    usuario_mayor AS (CASE WHEN id_usuario_emisor < id_usuario_receptor THEN id_usuario_receptor ELSE id_usuario_emisor END) PERSISTED;
GO
CREATE UNIQUE INDEX UX_solicitud_pareja_pendiente ON solicitud_contacto(usuario_menor, usuario_mayor) WHERE estado = 'pendiente';
ALTER TABLE solicitud_contacto DROP CONSTRAINT UQ_solicitud_unica;
-- Directed retry/cooldown query, newest attempt first.
CREATE INDEX IX_solicitud_historial ON solicitud_contacto(id_usuario_emisor, id_usuario_receptor, fecha_solicitud DESC, id_solicitud DESC) INCLUDE (estado, fecha_respuesta);
GO

ALTER TABLE contacto_roomie ADD
    usuario_menor AS (CASE WHEN id_usuario_a < id_usuario_b THEN id_usuario_a ELSE id_usuario_b END) PERSISTED,
    usuario_mayor AS (CASE WHEN id_usuario_a < id_usuario_b THEN id_usuario_b ELSE id_usuario_a END) PERSISTED;
GO
CREATE UNIQUE INDEX UX_contacto_pareja ON contacto_roomie(usuario_menor, usuario_mayor);
GO
