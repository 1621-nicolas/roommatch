USE roommatch_db;
GO

/* =========================================================
   MIGRACIÓN: VINCULACIÓN DE VIVIENDA EN PUBLICACIONES ROOMIE
   Alinea la base existente con PublicacionRoomie.java.
   Es segura para ejecutar una sola vez o sobre una BD antigua.
   ========================================================= */

IF COL_LENGTH('publicacion_roomie', 'tipo_vinculacion_vivienda') IS NULL
BEGIN
    ALTER TABLE publicacion_roomie
        ADD tipo_vinculacion_vivienda VARCHAR(20) NULL;
END;
GO

IF COL_LENGTH('publicacion_roomie', 'id_habitacion') IS NULL
BEGIN
    ALTER TABLE publicacion_roomie
        ADD id_habitacion INT NULL;
END;
GO

IF COL_LENGTH('publicacion_roomie', 'vivienda_externa_titulo') IS NULL
BEGIN
    ALTER TABLE publicacion_roomie
        ADD vivienda_externa_titulo VARCHAR(150) NULL;
END;
GO

IF COL_LENGTH('publicacion_roomie', 'vivienda_externa_direccion') IS NULL
BEGIN
    ALTER TABLE publicacion_roomie
        ADD vivienda_externa_direccion VARCHAR(255) NULL;
END;
GO

IF COL_LENGTH('publicacion_roomie', 'vivienda_externa_precio') IS NULL
BEGIN
    ALTER TABLE publicacion_roomie
        ADD vivienda_externa_precio DECIMAL(10,2) NULL;
END;
GO

IF NOT EXISTS (
    SELECT 1
    FROM sys.foreign_keys
    WHERE name = 'FK_publicacion_habitacion'
)
BEGIN
    ALTER TABLE publicacion_roomie
        ADD CONSTRAINT FK_publicacion_habitacion
        FOREIGN KEY (id_habitacion) REFERENCES habitacion(id_habitacion);
END;
GO

IF NOT EXISTS (
    SELECT 1
    FROM sys.check_constraints
    WHERE name = 'CK_publicacion_tipo_vinculacion'
)
BEGIN
    ALTER TABLE publicacion_roomie
        ADD CONSTRAINT CK_publicacion_tipo_vinculacion
        CHECK (
            tipo_vinculacion_vivienda IS NULL
            OR tipo_vinculacion_vivienda IN ('roommatch', 'externa')
        );
END;
GO

IF NOT EXISTS (
    SELECT 1
    FROM sys.check_constraints
    WHERE name = 'CK_publicacion_vivienda_precio'
)
BEGIN
    ALTER TABLE publicacion_roomie
        ADD CONSTRAINT CK_publicacion_vivienda_precio
        CHECK (vivienda_externa_precio IS NULL OR vivienda_externa_precio >= 0);
END;
GO

/* La entidad admite hasta 1000 caracteres. */
ALTER TABLE publicacion_roomie
    ALTER COLUMN descripcion VARCHAR(1000) NOT NULL;
GO
