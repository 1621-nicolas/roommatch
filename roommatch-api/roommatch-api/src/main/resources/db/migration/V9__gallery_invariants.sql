-- Preserve URLs and stable ordering. Stop for manual review if legacy races exceeded the quota.
IF EXISTS (SELECT id_habitacion FROM imagen_habitacion GROUP BY id_habitacion HAVING COUNT(*) > 5)
    THROW 51000, 'Revisar galería con más de cinco imágenes antes de migrar', 1;
GO
WITH ranked AS (
    SELECT id_imagen, ROW_NUMBER() OVER (PARTITION BY id_habitacion ORDER BY orden, id_imagen) AS position,
           ROW_NUMBER() OVER (PARTITION BY id_habitacion ORDER BY principal DESC, orden, id_imagen) AS preferred
    FROM imagen_habitacion
)
UPDATE target SET orden=ranked.position, principal=CASE WHEN ranked.preferred=1 THEN 1 ELSE 0 END
FROM imagen_habitacion target JOIN ranked ON target.id_imagen=ranked.id_imagen;
ALTER TABLE imagen_habitacion ADD CONSTRAINT CK_imagen_habitacion_max_orden CHECK(orden <= 5);
CREATE UNIQUE INDEX UX_imagen_habitacion_orden ON imagen_habitacion(id_habitacion, orden);
CREATE UNIQUE INDEX UX_imagen_habitacion_principal ON imagen_habitacion(id_habitacion) WHERE principal=1;
GO
IF EXISTS (SELECT id_publicacion FROM imagen_publicacion GROUP BY id_publicacion HAVING COUNT(*) > 5)
    THROW 51000, 'Revisar galería con más de cinco imágenes antes de migrar', 1;
GO
WITH ranked AS (
    SELECT id_imagen, ROW_NUMBER() OVER (PARTITION BY id_publicacion ORDER BY orden, id_imagen) AS position,
           ROW_NUMBER() OVER (PARTITION BY id_publicacion ORDER BY principal DESC, orden, id_imagen) AS preferred
    FROM imagen_publicacion
)
UPDATE target SET orden=ranked.position, principal=CASE WHEN ranked.preferred=1 THEN 1 ELSE 0 END
FROM imagen_publicacion target JOIN ranked ON target.id_imagen=ranked.id_imagen;
ALTER TABLE imagen_publicacion ADD CONSTRAINT CK_imagen_publicacion_max_orden CHECK(orden <= 5);
CREATE UNIQUE INDEX UX_imagen_publicacion_orden ON imagen_publicacion(id_publicacion, orden);
CREATE UNIQUE INDEX UX_imagen_publicacion_principal ON imagen_publicacion(id_publicacion) WHERE principal=1;
GO
