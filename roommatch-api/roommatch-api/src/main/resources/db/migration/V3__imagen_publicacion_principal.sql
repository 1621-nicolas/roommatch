IF COL_LENGTH('imagen_publicacion', 'principal') IS NULL
BEGIN
    ALTER TABLE imagen_publicacion ADD principal BIT NOT NULL
        CONSTRAINT DF_imagen_publicacion_principal DEFAULT 0 WITH VALUES;
END;
GO
