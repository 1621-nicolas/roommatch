-- Existing privacy choices are preserved. Only the default for future rows changes.
DECLARE @default_name sysname;
SELECT @default_name = dc.name
FROM sys.default_constraints dc
JOIN sys.columns c ON c.object_id = dc.parent_object_id AND c.column_id = dc.parent_column_id
WHERE dc.parent_object_id = OBJECT_ID(N'dbo.contacto_usuario') AND c.name = N'mostrar_email';
IF @default_name IS NOT NULL
    EXEC(N'ALTER TABLE dbo.contacto_usuario DROP CONSTRAINT ' + QUOTENAME(@default_name));
ALTER TABLE dbo.contacto_usuario ADD CONSTRAINT DF_contacto_email_private DEFAULT 0 FOR mostrar_email;
ALTER TABLE dbo.contacto_usuario ADD version BIGINT NOT NULL CONSTRAINT DF_contacto_version DEFAULT 0;
GO

-- paginaConexiones filters either member and orders by unlock time plus stable ID.
CREATE INDEX IX_contacto_roomie_a_fecha ON contacto_roomie(id_usuario_a, fecha_desbloqueo DESC, id_contacto_roomie DESC)
    INCLUDE(id_usuario_b);
CREATE INDEX IX_contacto_roomie_b_fecha ON contacto_roomie(id_usuario_b, fecha_desbloqueo DESC, id_contacto_roomie DESC)
    INCLUDE(id_usuario_a);
GO
