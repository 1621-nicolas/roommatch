-- Do not backfill account emails: historical inquiries contain no explicit contact selection.
ALTER TABLE lead_habitacion ADD email_contacto VARCHAR(150) NULL;
ALTER TABLE lead_habitacion ADD version BIGINT NOT NULL CONSTRAINT DF_lead_version DEFAULT 0;
-- The sender history orders by date; the previous room/state index cannot serve this query.
CREATE INDEX IX_lead_usuario_fecha ON lead_habitacion(id_usuario_interesado, fecha_lead DESC, id_lead DESC);
