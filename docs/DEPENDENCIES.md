# Dependencias: actualización compatible

## Frontend (20 de septiembre de 2026)

La auditoría previa informó 23 paquetes vulnerables: 11 de severidad alta,
11 moderada y uno baja. Se actualizaron parches dentro de las versiones
mayores existentes, sin `--force` ni `--legacy-peer-deps`.

| Dependencia | Antes | Después |
| --- | --- | --- |
| Angular runtime y compiler-cli | 21.2.17 | 21.2.23 |
| Angular CLI/build | 21.2.18 | 21.2.24 |
| Vitest | 4.1.9 | 4.1.11 |

El lockfile incluye actualizaciones transitivas compatibles; la comparación
con el anterior no encuentra cambios de versión mayor en paquetes existentes.
La resolución limpia evita conservar un conjunto de peers Angular incompatible.
Un override limitado a `@modelcontextprotocol/sdk` mantiene
`@hono/node-server` en `^1.19.17`, permitido por el rango de su consumidor,
en lugar de introducir su versión mayor 2. Revisar este override al actualizar
el SDK; no es una dependencia de la aplicación Angular en producción.

## Evidencia y alcance

- [Angular: atributos de eventos traducidos](https://github.com/angular/angular/security/advisories/GHSA-jj27-h5hq-8x99).
- [Angular: sanitización de host bindings](https://github.com/angular/angular/security/advisories/GHSA-hh8m-fm6v-7cvg).
- [Vitest: traversal en el mocker](https://github.com/vitest-dev/vitest/security/advisories/GHSA-82fw-gwwq-j7x9).

Las versiones anteriores estaban afectadas por avisos publicados. La revisión
no encontró en el código de producción los patrones Angular mencionados en
esos avisos; esto no demuestra explotación ni permite afirmar que el proyecto
estuviera libre de otros problemas. Vitest pertenece al entorno de pruebas.

`npm ci` y `npm audit --package-lock-only --json` terminaron correctamente
el 20 de septiembre de 2026; el segundo informó **cero vulnerabilidades**.
Ese resultado depende del registro de avisos consultado y no reemplaza la
revisión de autorización, privacidad o reglas de negocio.

CI ejecuta también `npm audit --audit-level=high`, incluyendo dependencias
de desarrollo, sin ocultar errores. Un aviso nuevo o un fallo del registro
detiene ese paso y requiere revisión; no se actualizan versiones automáticamente.

## Backend

Este cambio no modifica Java, Spring Boot ni dependencias Maven. La validación
del backend mantiene `clean verify -Psqlserver` en CI. La ejecución local de
Maven está limitada por la resolución de `repo.maven.apache.org`; no se
presenta esa limitación como una prueba aprobada ni como un análisis de CVE
de las dependencias Java.
