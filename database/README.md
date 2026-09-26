# Migraciones SQL Server

La fuente de upgrades es `roommatch-api/roommatch-api/src/main/resources/db/migration`.
Flyway usa las versiones administradas por Spring Boot 3.5, comprueba checksums y
ejecuta antes de la validación del esquema JPA. No modifica migraciones ya aplicadas.
Los archivos antiguos de esta carpeta quedan como referencia histórica.

## Instalación vacía

1. Crea una base vacía con SQL Server 2022 o la versión de tu despliegue validada en CI.
2. Configura `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` y el perfil de ejecución.
3. Arranca la API. Se ejecutan V1 (esquema histórico), V2 (vivienda vinculada),
   V3 (imagen principal) y V4 (roles y plan inicial). Las siguientes versiones
   se aplican en orden. No se crean cuentas ni contraseñas de demostración.

## Base existente, sin historial Flyway

No actives `baseline-on-migrate`: podría aceptar como válido un esquema distinto.

1. Detén las escrituras, realiza un backup y prueba su restauración en una base de ensayo.
2. Compara el esquema restaurado con V1, campo por campo (tipos, nulabilidad, FK,
   CHECK, UNIQUE e índices). V2 puede estar aplicada manualmente: es idempotente.
   Investiga cualquier diferencia antes de continuar; no marques una base ajena
   como baseline. Conserva recuentos por tabla para comprobar la actualización.
3. Sobre esa copia verificada, con Flyway CLI compatible con la versión administrada
   por el `pom.xml`, define `FLYWAY_URL`, `FLYWAY_USER` y `FLYWAY_PASSWORD` desde el
   entorno seguro. Ejecuta `flyway -baselineVersion=1 -baselineDescription=legacy-roommatch baseline`.
   Este comando registra V1 como baseline; no ejecuta su DDL ni borra filas.
4. Arranca esta versión de la API y comprueba migraciones, recuentos, inicio de sesión,
   publicaciones e imágenes. Repite el procedimiento sobre producción durante una
   ventana de mantenimiento solamente después de validar la restauración y el ensayo.

No uses `Base de datos_Roommatch.sql` para actualizar: elimina la base completa.
No cambies checksums ni ejecutes `repair` para ocultar un error de migración.

## Fallos y reversión

El despliegue se detiene si una migración o la validación JPA falla. Examina la
transacción y `flyway_schema_history` antes de reintentar. La estrategia habitual es
una nueva migración correctiva hacia delante, probada sobre una copia. Si el cambio
impide recuperar el servicio, restaura el backup y la versión de aplicación anterior
durante la ventana de mantenimiento. Coordina la recuperación de escrituras nuevas;
no hay un rollback automático que descarte datos de usuarios.

En producción conviene usar un usuario de migración con DDL durante el despliegue y
un usuario de ejecución con DML. En ese caso ejecuta las migraciones antes de arrancar
la API con `SPRING_FLYWAY_ENABLED=false`; la validación JPA debe seguir activada.

## Pruebas

`./mvnw clean verify -Psqlserver` requiere Docker y ejecuta SQL Server 2022 en un
contenedor temporal con licencia de desarrollo aceptada solo para las pruebas.
CI exige estas pruebas; no las omite cuando Docker no está disponible. Las pruebas
unitarias siguen usando mocks y H2 para mantener tiempos razonables. El contenedor
prueba instalación limpia, upgrade de datos históricos, checks y unicidad reales.

Referencias: [baseline explícito de Flyway](https://documentation.red-gate.com/flyway/reference/configuration/flyway-namespace/flyway-baseline-on-migrate-setting),
[dependencias administradas de Spring Boot](https://docs.spring.io/spring-boot/3.5/appendix/dependency-versions/coordinates.html),
[SQL Server con Testcontainers](https://java.testcontainers.org/modules/databases/mssqlserver/).
