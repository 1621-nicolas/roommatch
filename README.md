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

Crea una base vacía `roommatch_db` y configura la conexión del backend. Flyway ejecuta las migraciones versionadas de `roommatch-api/roommatch-api/src/main/resources/db/migration`, incluidos los roles y el plan `Gratis`.

Para una base existente sigue el [procedimiento de baseline y actualización](database/README.md). El baseline es explícito: arrancar contra una base antigua sin historial Flyway falla, sin borrar sus datos.

`Base de datos_Roommatch.sql` se conserva como bootstrap histórico **destructivo y exclusivo de desarrollo**. No es un mecanismo de actualización.

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

La API del navegador usa `/api`. En desarrollo, `proxy.conf.json` reenvía estas peticiones a `http://localhost:8081`; reinicia `npm start` si cambias el proxy. En producción, sirve Angular y `/api` bajo el mismo origen mediante un reverse proxy. La configuración compilada se define en `src/environments/`. Consulta [despliegue y sesión](deployment/README.md).

## 5. Pruebas y compilación

Backend:

```powershell
cd roommatch-api\roommatch-api
.\mvnw clean verify
# Con Docker, incluye migraciones e integración sobre SQL Server 2022:
.\mvnw clean verify -Psqlserver
```

Frontend:

```bash
cd roommatch-web
npm ci
npm run build
npm test -- --watch=false
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
