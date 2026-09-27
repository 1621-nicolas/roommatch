-- Existing duplicate open cases require explicit review, never automatic deletion.
IF EXISTS(SELECT id_usuario_reportante,id_usuario_reportado FROM reporte_usuario WHERE estado IN ('pendiente','revisado') GROUP BY id_usuario_reportante,id_usuario_reportado HAVING COUNT(*)>1)
    THROW 51000, 'Revisar reportes de usuario abiertos duplicados antes de migrar', 1;
IF EXISTS(SELECT id_usuario_reportante,id_habitacion FROM reporte_habitacion WHERE estado IN ('pendiente','revisado') GROUP BY id_usuario_reportante,id_habitacion HAVING COUNT(*)>1)
    THROW 51000, 'Revisar reportes de habitación abiertos duplicados antes de migrar', 1;
GO
ALTER TABLE usuario ADD version BIGINT NOT NULL CONSTRAINT DF_usuario_version DEFAULT 0;
ALTER TABLE reporte_usuario ADD version BIGINT NOT NULL CONSTRAINT DF_reporte_usuario_version DEFAULT 0;
ALTER TABLE reporte_habitacion ADD version BIGINT NOT NULL CONSTRAINT DF_reporte_habitacion_version DEFAULT 0;
CREATE UNIQUE INDEX UX_reporte_usuario_abierto ON reporte_usuario(id_usuario_reportante,id_usuario_reportado) WHERE estado IN ('pendiente','revisado');
CREATE UNIQUE INDEX UX_reporte_habitacion_abierto ON reporte_habitacion(id_usuario_reportante,id_habitacion) WHERE estado IN ('pendiente','revisado');
-- Exact state filter + descending queue order used by listarReportes.
CREATE INDEX IX_reporte_usuario_cola ON reporte_usuario(estado,fecha_reporte DESC,id_reporte DESC);
CREATE INDEX IX_reporte_habitacion_cola ON reporte_habitacion(estado,fecha_reporte DESC,id_reporte_habitacion DESC);
CREATE TABLE moderacion_evento (
    id_evento INT IDENTITY(1,1) PRIMARY KEY,
    id_admin INT NOT NULL,
    id_reporte_usuario INT NULL,
    id_reporte_habitacion INT NULL,
    accion VARCHAR(30) NOT NULL,
    motivo VARCHAR(500) NOT NULL,
    fecha DATETIME2 NOT NULL,
    CONSTRAINT FK_evento_admin FOREIGN KEY(id_admin) REFERENCES usuario(id_usuario),
    CONSTRAINT FK_evento_reporte_usuario FOREIGN KEY(id_reporte_usuario) REFERENCES reporte_usuario(id_reporte),
    CONSTRAINT FK_evento_reporte_habitacion FOREIGN KEY(id_reporte_habitacion) REFERENCES reporte_habitacion(id_reporte_habitacion),
    CONSTRAINT CK_evento_un_reporte CHECK((id_reporte_usuario IS NOT NULL AND id_reporte_habitacion IS NULL) OR (id_reporte_usuario IS NULL AND id_reporte_habitacion IS NOT NULL)),
    CONSTRAINT CK_evento_accion CHECK(accion IN ('revisado','rechazado','sancionado','restaurado')),
    CONSTRAINT CK_evento_motivo CHECK(LEN(LTRIM(RTRIM(motivo)))>0)
);
-- Audit detail endpoints filter one report and order newest first.
CREATE INDEX IX_evento_usuario_fecha ON moderacion_evento(id_reporte_usuario,fecha DESC,id_evento DESC) WHERE id_reporte_usuario IS NOT NULL;
CREATE INDEX IX_evento_habitacion_fecha ON moderacion_evento(id_reporte_habitacion,fecha DESC,id_evento DESC) WHERE id_reporte_habitacion IS NOT NULL;
