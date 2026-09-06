USE roommatch_db;
GO

/* =========================================================
   DATOS MÍNIMOS REQUERIDOS POR EL BACKEND
   Este script es idempotente: puede ejecutarse varias veces.
   ========================================================= */

IF NOT EXISTS (SELECT 1 FROM rol WHERE nombre_rol = 'USUARIO')
BEGIN
    INSERT INTO rol (nombre_rol, descripcion)
    VALUES ('USUARIO', 'Usuario que busca roomies y habitaciones');
END;
GO

IF NOT EXISTS (SELECT 1 FROM rol WHERE nombre_rol = 'PROPIETARIO')
BEGIN
    INSERT INTO rol (nombre_rol, descripcion)
    VALUES ('PROPIETARIO', 'Usuario que publica y administra habitaciones');
END;
GO

IF NOT EXISTS (SELECT 1 FROM rol WHERE nombre_rol = 'ADMIN')
BEGIN
    INSERT INTO rol (nombre_rol, descripcion)
    VALUES ('ADMIN', 'Administrador de RoomMatch');
END;
GO

/* PropietarioService requiere exactamente un plan llamado "Gratis". */
IF NOT EXISTS (SELECT 1 FROM plan_propietario WHERE nombre_plan = 'Gratis')
BEGIN
    INSERT INTO plan_propietario (
        nombre_plan,
        descripcion,
        precio_mensual,
        limite_habitaciones,
        permite_destacar,
        permite_estadisticas,
        estado
    )
    VALUES (
        'Gratis',
        'Plan inicial para propietarios de RoomMatch',
        0,
        1,
        0,
        0,
        'activo'
    );
END;
GO
