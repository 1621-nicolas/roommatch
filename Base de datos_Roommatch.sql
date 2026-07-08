USE master;
GO

IF EXISTS (SELECT name FROM sys.databases WHERE name = N'roommatch_db')
BEGIN
    ALTER DATABASE roommatch_db SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
    DROP DATABASE roommatch_db;
END
GO

CREATE DATABASE roommatch_db;
GO

USE roommatch_db;
GO

/* =========================================================
   1. TABLA: ROL
   ========================================================= */
CREATE TABLE rol (
    id_rol INT IDENTITY(1,1) PRIMARY KEY,
    nombre_rol VARCHAR(50) NOT NULL UNIQUE,
    descripcion VARCHAR(200) NULL
);
GO

/* =========================================================
   2. TABLA: USUARIO
   ========================================================= */
CREATE TABLE usuario (
    id_usuario INT IDENTITY(1,1) PRIMARY KEY,
    id_rol INT NOT NULL,

    nombres VARCHAR(100) NOT NULL,
    apellidos VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,

    edad INT NOT NULL,
    ocupacion VARCHAR(100) NULL,
    universidad VARCHAR(150) NULL,
    foto VARCHAR(255) NULL,

    estado VARCHAR(30) NOT NULL DEFAULT 'activo',
    fecha_registro DATETIME2 NOT NULL DEFAULT SYSDATETIME(),

    CONSTRAINT FK_usuario_rol 
        FOREIGN KEY (id_rol) REFERENCES rol(id_rol),

    CONSTRAINT CK_usuario_edad 
        CHECK (edad >= 18),

    CONSTRAINT CK_usuario_estado 
        CHECK (estado IN ('activo', 'suspendido', 'eliminado'))
);
GO

/* =========================================================
   3. TABLA: CONTACTO_USUARIO
   ========================================================= */
CREATE TABLE contacto_usuario (
    id_contacto INT IDENTITY(1,1) PRIMARY KEY,
    id_usuario INT NOT NULL UNIQUE,

    telefono VARCHAR(20) NULL,
    whatsapp VARCHAR(20) NULL,
    instagram VARCHAR(100) NULL,
    facebook VARCHAR(150) NULL,
    email_contacto VARCHAR(150) NULL,

    mostrar_telefono BIT NOT NULL DEFAULT 0,
    mostrar_whatsapp BIT NOT NULL DEFAULT 0,
    mostrar_instagram BIT NOT NULL DEFAULT 0,
    mostrar_facebook BIT NOT NULL DEFAULT 0,
    mostrar_email BIT NOT NULL DEFAULT 1,

    fecha_actualizacion DATETIME2 NOT NULL DEFAULT SYSDATETIME(),

    CONSTRAINT FK_contacto_usuario
        FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario)
);
GO

/* =========================================================
   4. TABLA: PERFIL_CONVIVENCIA
   ========================================================= */
CREATE TABLE perfil_convivencia (
    id_perfil INT IDENTITY(1,1) PRIMARY KEY,
    id_usuario INT NOT NULL UNIQUE,

    presupuesto_min DECIMAL(10,2) NOT NULL,
    presupuesto_max DECIMAL(10,2) NOT NULL,
    distrito_preferido VARCHAR(100) NOT NULL,
    fecha_mudanza DATE NULL,

    limpieza INT NOT NULL,
    ruido INT NOT NULL,
    sociabilidad INT NOT NULL,

    horario VARCHAR(50) NOT NULL,
    visitas VARCHAR(50) NOT NULL,
    mascotas VARCHAR(50) NOT NULL,
    fumar VARCHAR(50) NOT NULL,
    alcohol VARCHAR(50) NOT NULL,
    gastos VARCHAR(50) NOT NULL,
    convivencia VARCHAR(50) NOT NULL,

    descripcion_personal VARCHAR(500) NULL,
    perfil_completo BIT NOT NULL DEFAULT 1,
    fecha_actualizacion DATETIME2 NOT NULL DEFAULT SYSDATETIME(),

    CONSTRAINT FK_perfil_usuario 
        FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario),

    CONSTRAINT CK_perfil_presupuesto 
        CHECK (presupuesto_min >= 0 AND presupuesto_max >= presupuesto_min),

    CONSTRAINT CK_perfil_limpieza 
        CHECK (limpieza BETWEEN 1 AND 5),

    CONSTRAINT CK_perfil_ruido 
        CHECK (ruido BETWEEN 1 AND 5),

    CONSTRAINT CK_perfil_sociabilidad 
        CHECK (sociabilidad BETWEEN 1 AND 5)
);
GO

/* =========================================================
   5. TABLA: PROPIETARIO
   ========================================================= */
CREATE TABLE propietario (
    id_propietario INT IDENTITY(1,1) PRIMARY KEY,
    id_usuario INT NOT NULL UNIQUE,

    tipo_propietario VARCHAR(50) NOT NULL,
    nombre_comercial VARCHAR(150) NULL,
    ruc VARCHAR(20) NULL,
    descripcion VARCHAR(500) NULL,

    verificado BIT NOT NULL DEFAULT 0,
    estado VARCHAR(30) NOT NULL DEFAULT 'activo',
    fecha_registro DATETIME2 NOT NULL DEFAULT SYSDATETIME(),

    CONSTRAINT FK_propietario_usuario
        FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario),

    CONSTRAINT CK_propietario_tipo
        CHECK (tipo_propietario IN ('persona', 'empresa')),

    CONSTRAINT CK_propietario_estado
        CHECK (estado IN ('activo', 'suspendido', 'eliminado'))
);
GO

/* =========================================================
   6. TABLA: PLAN
   Catálogo de planes para propietarios.
   ========================================================= */
CREATE TABLE plan_propietario (
    id_plan INT IDENTITY(1,1) PRIMARY KEY,

    nombre_plan VARCHAR(50) NOT NULL UNIQUE,
    descripcion VARCHAR(300) NULL,

    precio_mensual DECIMAL(10,2) NOT NULL DEFAULT 0,
    limite_habitaciones INT NOT NULL,
    permite_destacar BIT NOT NULL DEFAULT 0,
    permite_estadisticas BIT NOT NULL DEFAULT 0,

    estado VARCHAR(30) NOT NULL DEFAULT 'activo',

    CONSTRAINT CK_plan_precio
        CHECK (precio_mensual >= 0),

    CONSTRAINT CK_plan_limite
        CHECK (limite_habitaciones >= 1),

    CONSTRAINT CK_plan_estado
        CHECK (estado IN ('activo', 'inactivo'))
);
GO

/* =========================================================
   7. TABLA: SUSCRIPCION_PROPIETARIO
   Relaciona propietarios con planes.
   ========================================================= */
CREATE TABLE suscripcion_propietario (
    id_suscripcion INT IDENTITY(1,1) PRIMARY KEY,

    id_propietario INT NOT NULL,
    id_plan INT NOT NULL,

    fecha_inicio DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    fecha_fin DATETIME2 NULL,
    estado VARCHAR(30) NOT NULL DEFAULT 'activo',

    CONSTRAINT FK_suscripcion_propietario
        FOREIGN KEY (id_propietario) REFERENCES propietario(id_propietario),

    CONSTRAINT FK_suscripcion_plan
        FOREIGN KEY (id_plan) REFERENCES plan_propietario(id_plan),

    CONSTRAINT CK_suscripcion_estado
        CHECK (estado IN ('activo', 'vencido', 'cancelado'))
);
GO

/* =========================================================
   8. TABLA: HABITACION
   Publicaciones reales de cuartos por propietarios.
   ========================================================= */
CREATE TABLE habitacion (
    id_habitacion INT IDENTITY(1,1) PRIMARY KEY,
    id_propietario INT NOT NULL,

    titulo VARCHAR(150) NOT NULL,
    descripcion VARCHAR(800) NOT NULL,

    distrito VARCHAR(100) NOT NULL,
    direccion_referencial VARCHAR(250) NULL,

    precio DECIMAL(10,2) NOT NULL,
    area_m2 DECIMAL(6,2) NULL,

    amoblado BIT NOT NULL DEFAULT 0,
    bano_privado BIT NOT NULL DEFAULT 0,
    internet_incluido BIT NOT NULL DEFAULT 0,
    agua_incluida BIT NOT NULL DEFAULT 0,
    luz_incluida BIT NOT NULL DEFAULT 0,
    permite_mascotas BIT NOT NULL DEFAULT 0,

    disponible_desde DATE NULL,
    destacada BIT NOT NULL DEFAULT 0,

    estado VARCHAR(30) NOT NULL DEFAULT 'activa',
    fecha_publicacion DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    fecha_actualizacion DATETIME2 NOT NULL DEFAULT SYSDATETIME(),

    CONSTRAINT FK_habitacion_propietario
        FOREIGN KEY (id_propietario) REFERENCES propietario(id_propietario),

    CONSTRAINT CK_habitacion_precio
        CHECK (precio >= 0),

    CONSTRAINT CK_habitacion_area
        CHECK (area_m2 IS NULL OR area_m2 > 0),

    CONSTRAINT CK_habitacion_estado
        CHECK (estado IN ('activa', 'pausada', 'alquilada', 'eliminada'))
);
GO

/* =========================================================
   9. TABLA: IMAGEN_HABITACION
   ========================================================= */
CREATE TABLE imagen_habitacion (
    id_imagen INT IDENTITY(1,1) PRIMARY KEY,
    id_habitacion INT NOT NULL,

    url_imagen VARCHAR(255) NOT NULL,
    orden INT NOT NULL DEFAULT 1,
    principal BIT NOT NULL DEFAULT 0,

    CONSTRAINT FK_imagen_habitacion
        FOREIGN KEY (id_habitacion) REFERENCES habitacion(id_habitacion),

    CONSTRAINT CK_imagen_habitacion_orden
        CHECK (orden >= 1)
);
GO

/* =========================================================
   10. TABLA: LEAD_HABITACION
   Usuarios interesados en habitaciones.
   ========================================================= */
CREATE TABLE lead_habitacion (
    id_lead INT IDENTITY(1,1) PRIMARY KEY,

    id_habitacion INT NOT NULL,
    id_usuario_interesado INT NOT NULL,

    mensaje VARCHAR(500) NULL,
    estado VARCHAR(30) NOT NULL DEFAULT 'pendiente',
    fecha_lead DATETIME2 NOT NULL DEFAULT SYSDATETIME(),

    CONSTRAINT FK_lead_habitacion
        FOREIGN KEY (id_habitacion) REFERENCES habitacion(id_habitacion),

    CONSTRAINT FK_lead_usuario
        FOREIGN KEY (id_usuario_interesado) REFERENCES usuario(id_usuario),

    CONSTRAINT CK_lead_estado
        CHECK (estado IN ('pendiente', 'contactado', 'cerrado', 'rechazado')),

    CONSTRAINT UQ_lead_unico
        UNIQUE (id_habitacion, id_usuario_interesado)
);
GO

/* =========================================================
   11. TABLA: PUBLICACION_ROOMIE
   Publicaciones sociales de usuarios.
   Ejemplo: busco roomie, busco cuarto, busco compartir.
   ========================================================= */
CREATE TABLE publicacion_roomie (
    id_publicacion INT IDENTITY(1,1) PRIMARY KEY,
    id_usuario INT NOT NULL,

    tipo_publicacion VARCHAR(50) NOT NULL,
    titulo VARCHAR(150) NOT NULL,
    descripcion VARCHAR(800) NOT NULL,

    distrito VARCHAR(100) NOT NULL,
    presupuesto_min DECIMAL(10,2) NULL,
    presupuesto_max DECIMAL(10,2) NULL,

    estado VARCHAR(30) NOT NULL DEFAULT 'activa',
    fecha_publicacion DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    fecha_actualizacion DATETIME2 NOT NULL DEFAULT SYSDATETIME(),

    CONSTRAINT FK_publicacion_usuario
        FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario),

    CONSTRAINT CK_publicacion_tipo
        CHECK (tipo_publicacion IN ('busco_roomie', 'busco_cuarto', 'busco_compartir')),

    CONSTRAINT CK_publicacion_presupuesto
        CHECK (
            presupuesto_min IS NULL 
            OR presupuesto_max IS NULL 
            OR presupuesto_max >= presupuesto_min
        ),

    CONSTRAINT CK_publicacion_estado
        CHECK (estado IN ('activa', 'pausada', 'cerrada', 'eliminada'))
);
GO

/* =========================================================
   12. TABLA: IMAGEN_PUBLICACION
   ========================================================= */
CREATE TABLE imagen_publicacion (
    id_imagen INT IDENTITY(1,1) PRIMARY KEY,
    id_publicacion INT NOT NULL,

    url_imagen VARCHAR(255) NOT NULL,
    orden INT NOT NULL DEFAULT 1,

    CONSTRAINT FK_imagen_publicacion
        FOREIGN KEY (id_publicacion) REFERENCES publicacion_roomie(id_publicacion),

    CONSTRAINT CK_imagen_publicacion_orden
        CHECK (orden >= 1)
);
GO

/* =========================================================
   13. TABLA: MATCH_RESULTADO
   Compatibilidad entre usuarios.
   ========================================================= */
CREATE TABLE match_resultado (
    id_match INT IDENTITY(1,1) PRIMARY KEY,

    id_usuario_origen INT NOT NULL,
    id_usuario_destino INT NOT NULL,

    porcentaje DECIMAL(5,2) NOT NULL,
    coincidencias VARCHAR(800) NULL,
    diferencias VARCHAR(800) NULL,

    fecha_calculo DATETIME2 NOT NULL DEFAULT SYSDATETIME(),

    CONSTRAINT FK_match_usuario_origen
        FOREIGN KEY (id_usuario_origen) REFERENCES usuario(id_usuario),

    CONSTRAINT FK_match_usuario_destino
        FOREIGN KEY (id_usuario_destino) REFERENCES usuario(id_usuario),

    CONSTRAINT CK_match_porcentaje
        CHECK (porcentaje BETWEEN 0 AND 100),

    CONSTRAINT CK_match_usuarios_distintos
        CHECK (id_usuario_origen <> id_usuario_destino),

    CONSTRAINT UQ_match_unico
        UNIQUE (id_usuario_origen, id_usuario_destino)
);
GO

/* =========================================================
   14. TABLA: FAVORITO_USUARIO
   Usuarios guardados como favoritos.
   ========================================================= */
CREATE TABLE favorito_usuario (
    id_favorito INT IDENTITY(1,1) PRIMARY KEY,

    id_usuario INT NOT NULL,
    id_usuario_favorito INT NOT NULL,

    fecha_favorito DATETIME2 NOT NULL DEFAULT SYSDATETIME(),

    CONSTRAINT FK_favorito_usuario
        FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario),

    CONSTRAINT FK_favorito_usuario_favorito
        FOREIGN KEY (id_usuario_favorito) REFERENCES usuario(id_usuario),

    CONSTRAINT CK_favorito_usuarios_distintos
        CHECK (id_usuario <> id_usuario_favorito),

    CONSTRAINT UQ_favorito_unico
        UNIQUE (id_usuario, id_usuario_favorito)
);
GO

/* =========================================================
   15. TABLA: SOLICITUD_CONTACTO
   Solicitudes para desbloquear datos de contacto.
   ========================================================= */
CREATE TABLE solicitud_contacto (
    id_solicitud INT IDENTITY(1,1) PRIMARY KEY,

    id_usuario_emisor INT NOT NULL,
    id_usuario_receptor INT NOT NULL,

    mensaje VARCHAR(500) NULL,
    estado VARCHAR(30) NOT NULL DEFAULT 'pendiente',

    fecha_solicitud DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    fecha_respuesta DATETIME2 NULL,

    CONSTRAINT FK_solicitud_emisor
        FOREIGN KEY (id_usuario_emisor) REFERENCES usuario(id_usuario),

    CONSTRAINT FK_solicitud_receptor
        FOREIGN KEY (id_usuario_receptor) REFERENCES usuario(id_usuario),

    CONSTRAINT CK_solicitud_estado
        CHECK (estado IN ('pendiente', 'aceptada', 'rechazada', 'cancelada')),

    CONSTRAINT CK_solicitud_usuarios_distintos
        CHECK (id_usuario_emisor <> id_usuario_receptor),

    CONSTRAINT UQ_solicitud_unica
        UNIQUE (id_usuario_emisor, id_usuario_receptor)
);
GO

/* =========================================================
   16. TABLA: CONTACTO_ROOMIE
   Contacto desbloqueado cuando una solicitud es aceptada.
   ========================================================= */
CREATE TABLE contacto_roomie (
    id_contacto_roomie INT IDENTITY(1,1) PRIMARY KEY,

    id_solicitud INT NOT NULL UNIQUE,
    id_usuario_a INT NOT NULL,
    id_usuario_b INT NOT NULL,

    fecha_desbloqueo DATETIME2 NOT NULL DEFAULT SYSDATETIME(),

    CONSTRAINT FK_contacto_roomie_solicitud
        FOREIGN KEY (id_solicitud) REFERENCES solicitud_contacto(id_solicitud),

    CONSTRAINT FK_contacto_roomie_usuario_a
        FOREIGN KEY (id_usuario_a) REFERENCES usuario(id_usuario),

    CONSTRAINT FK_contacto_roomie_usuario_b
        FOREIGN KEY (id_usuario_b) REFERENCES usuario(id_usuario),

    CONSTRAINT CK_contacto_roomie_usuarios_distintos
        CHECK (id_usuario_a <> id_usuario_b)
);
GO

/* =========================================================
   17. TABLA: REPORTE_USUARIO
   Reportes para moderación.
   ========================================================= */
CREATE TABLE reporte_usuario (
    id_reporte INT IDENTITY(1,1) PRIMARY KEY,

    id_usuario_reportante INT NOT NULL,
    id_usuario_reportado INT NOT NULL,

    motivo VARCHAR(150) NOT NULL,
    descripcion VARCHAR(600) NULL,

    estado VARCHAR(30) NOT NULL DEFAULT 'pendiente',
    fecha_reporte DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    fecha_revision DATETIME2 NULL,

    CONSTRAINT FK_reporte_reportante
        FOREIGN KEY (id_usuario_reportante) REFERENCES usuario(id_usuario),

    CONSTRAINT FK_reporte_reportado
        FOREIGN KEY (id_usuario_reportado) REFERENCES usuario(id_usuario),

    CONSTRAINT CK_reporte_estado
        CHECK (estado IN ('pendiente', 'revisado', 'rechazado', 'sancionado')),

    CONSTRAINT CK_reporte_usuarios_distintos
        CHECK (id_usuario_reportante <> id_usuario_reportado)
);
GO

/* =========================================================
   18. TABLA: REPORTE_HABITACION
   Reportes sobre habitaciones sospechosas.
   ========================================================= */
CREATE TABLE reporte_habitacion (
    id_reporte_habitacion INT IDENTITY(1,1) PRIMARY KEY,

    id_usuario_reportante INT NOT NULL,
    id_habitacion INT NOT NULL,

    motivo VARCHAR(150) NOT NULL,
    descripcion VARCHAR(600) NULL,

    estado VARCHAR(30) NOT NULL DEFAULT 'pendiente',
    fecha_reporte DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    fecha_revision DATETIME2 NULL,

    CONSTRAINT FK_reporte_habitacion_usuario
        FOREIGN KEY (id_usuario_reportante) REFERENCES usuario(id_usuario),

    CONSTRAINT FK_reporte_habitacion_habitacion
        FOREIGN KEY (id_habitacion) REFERENCES habitacion(id_habitacion),

    CONSTRAINT CK_reporte_habitacion_estado
        CHECK (estado IN ('pendiente', 'revisado', 'rechazado', 'sancionado'))
);
GO

/* =========================================================
   19. TABLA: NOTIFICACION
   Notificaciones internas del sistema.
   ========================================================= */
CREATE TABLE notificacion (
    id_notificacion INT IDENTITY(1,1) PRIMARY KEY,

    id_usuario INT NOT NULL,

    titulo VARCHAR(150) NOT NULL,
    mensaje VARCHAR(500) NOT NULL,
    tipo VARCHAR(50) NOT NULL,

    leido BIT NOT NULL DEFAULT 0,
    url_destino VARCHAR(255) NULL,

    fecha_creacion DATETIME2 NOT NULL DEFAULT SYSDATETIME(),

    CONSTRAINT FK_notificacion_usuario
        FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario),

    CONSTRAINT CK_notificacion_tipo
        CHECK (tipo IN ('match', 'solicitud', 'contacto', 'habitacion', 'lead', 'reporte', 'sistema'))
);
GO

/* =========================================================
   ÍNDICES
   ========================================================= */

CREATE INDEX IX_usuario_email 
ON usuario(email);
GO

CREATE INDEX IX_usuario_estado 
ON usuario(estado);
GO

CREATE INDEX IX_perfil_distrito_presupuesto 
ON perfil_convivencia(distrito_preferido, presupuesto_min, presupuesto_max);
GO

CREATE INDEX IX_habitacion_busqueda 
ON habitacion(distrito, precio, estado);
GO

CREATE INDEX IX_habitacion_propietario 
ON habitacion(id_propietario);
GO

CREATE INDEX IX_match_origen_porcentaje 
ON match_resultado(id_usuario_origen, porcentaje DESC);
GO

CREATE INDEX IX_favorito_usuario 
ON favorito_usuario(id_usuario);
GO

CREATE INDEX IX_solicitud_receptor_estado 
ON solicitud_contacto(id_usuario_receptor, estado);
GO

CREATE INDEX IX_solicitud_emisor_estado 
ON solicitud_contacto(id_usuario_emisor, estado);
GO

CREATE INDEX IX_lead_habitacion_estado 
ON lead_habitacion(id_habitacion, estado);
GO

CREATE INDEX IX_notificacion_usuario_leido 
ON notificacion(id_usuario, leido);
GO

CREATE INDEX IX_reporte_usuario_estado 
ON reporte_usuario(estado);
GO