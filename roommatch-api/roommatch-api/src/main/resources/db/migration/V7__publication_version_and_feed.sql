ALTER TABLE publicacion_roomie ADD version BIGINT NOT NULL CONSTRAINT DF_publicacion_version DEFAULT 0 WITH VALUES;
GO
-- Public feed starts with active rows and returns newest first; optional filters remain residual.
CREATE INDEX IX_publicacion_feed ON publicacion_roomie(estado, fecha_publicacion DESC, id_publicacion DESC)
    INCLUDE (id_usuario, tipo_publicacion, distrito, presupuesto_min, presupuesto_max);
-- Owner feed, including closed/paused entries, excludes only logical deletion.
CREATE INDEX IX_publicacion_usuario_fecha ON publicacion_roomie(id_usuario, fecha_publicacion DESC, id_publicacion DESC) INCLUDE (estado);
GO
