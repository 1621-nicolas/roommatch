# RoomMatch

RoomMatch es una aplicación web orientada a conectar personas que buscan compañeros de vivienda compatibles y propietarios que desean publicar habitaciones.

## Tecnologías

### Backend
- Java 17
- Spring Boot 3.5
- Spring Web
- Spring Data JPA / Hibernate
- Spring Security
- JWT
- SQL Server
- Swagger / OpenAPI

### Frontend
- Angular 21
- TypeScript
- RxJS
- Bootstrap
- Bootstrap Icons

## Estructura

```text
roommatch/
├── roommatch-api/roommatch-api/   # API Spring Boot
├── roommatch-web/                 # Aplicación Angular
├── Base de datos_Roommatch.sql    # Esquema base SQL Server
└── database/
    ├── migrations/                # Ajustes de esquema posteriores
    └── seed_required_data.sql     # Roles y datos mínimos
```

## 1. Base de datos

El archivo `Base de datos_Roommatch.sql` recrea `roommatch_db` desde cero.

> ADVERTENCIA: el script elimina `roommatch_db` si ya existe. No debe ejecutarse sobre una base con información que necesites conservar.

Para una instalación nueva:

1. Ejecuta `Base de datos_Roommatch.sql` en SQL Server.
2. Ejecuta los scripts de `database/migrations/` en orden.
3. Ejecuta `database/seed_required_data.sql`.

El seed agrega los roles `USUARIO`, `PROPIETARIO`, `ADMIN` y el plan inicial `Gratis` que necesita el backend.

## 2. Configuración local del backend

No guardes contraseñas ni secretos reales en Git.

Dentro de:

```text
roommatch-api/roommatch-api/src/main/resources/
```

copia:

```text
application-local.properties.example
```

como:

```text
application-local.properties
```

Luego configura tus credenciales locales de SQL Server. En el perfil `development`, dejar `jwt.secret` vacío genera una clave temporal que cambia al reiniciar. Para conservar sesiones locales puedes definir una clave aleatoria propia de al menos 32 bytes.

`application-local.properties` está ignorado por Git.

## 3. Ejecutar backend

Desde:

```text
roommatch-api/roommatch-api
```

Windows PowerShell:

```powershell
.\mvnw spring-boot:run "-Dspring-boot.run.profiles=development"
```

Linux/macOS:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=development
```

Sin un perfil explícito se utiliza `production`: requiere `JWT_SECRET`, `DB_URL`, `DB_USERNAME` y `DB_PASSWORD`; la URL SQL debe incluir `encrypt=true;trustServerCertificate=false`. Swagger queda deshabilitado. No se importa el archivo local en producción. Los tokens nuevos usan identidad numérica, issuer/audience y vencen por defecto en dos horas; las sesiones anteriores deben iniciar sesión de nuevo.

En desarrollo la API se inicia en:

```text
http://localhost:8081
```

Swagger:

```text
http://localhost:8081/swagger-ui.html
```

## 4. Ejecutar frontend

Desde:

```text
roommatch-web
```

instala dependencias:

```bash
npm ci
```

y ejecuta:

```bash
npm start
```

Angular se inicia normalmente en:

```text
http://localhost:4200
```

## 5. Pruebas y compilación

Backend:

```powershell
cd roommatch-api\roommatch-api
.\mvnw clean test
```

Frontend:

```bash
cd roommatch-web
npm ci
npm run build
```

El repositorio incluye GitHub Actions para validar ambas partes automáticamente.

## Seguridad

- Las contraseñas de usuarios se almacenan con BCrypt.
- La API usa autenticación JWT.
- Las rutas privadas requieren autenticación.
- Las rutas administrativas requieren rol `ADMIN`.
- Los usuarios suspendidos no pueden continuar autenticándose con un token anterior.
- Los errores internos de JDBC/SQL no se exponen directamente al frontend.

## Flujo principal

```text
Angular
   ↓ HTTP / JSON
Spring Controllers
   ↓
Services / reglas de negocio
   ↓
Repositories JPA
   ↓
SQL Server
```

RoomMatch separa la búsqueda social de roomies de la publicación comercial de habitaciones, manteniendo funcionalidades para usuarios, propietarios y administración.
