# ROOMMATCH — Auditoría técnica antes de implementar

## Estado de implementación — 20 de septiembre de 2026

El baseline de este informe se conserva íntegro debajo para mantener la evidencia
contra `main` (`8308ed4`). **Sus hallazgos describen ese commit, no el estado actual
de la rama de correcciones.** La implementación continúa en
[`audit/roommatch-hardening`, PR #4](https://github.com/1621-nicolas/roommatch/pull/4).
Main no se ha modificado ni se ha realizado merge.

Consultar [estado actual y pendientes](docs/IMPLEMENTATION_STATUS.md) antes de
interpretar como vigente un fallo del baseline. No se declara el proyecto
terminado ni listo para producción.

---

## Documento de auditoría original (baseline)

Baseline: 12 de septiembre de 2026. Informe emitido: 13 de septiembre de 2026. Código fijado al commit indicado y revalidado antes de entregar. Repositorio: [1621-nicolas/roommatch](https://github.com/1621-nicolas/roommatch). Rama auditada: `main`. Commit fijado: [8308ed4cde23](https://github.com/1621-nicolas/roommatch/commit/8308ed4cde239bbc03752df985269134d6de52bf).

Fase ejecutada: auditoría, sin correcciones del producto, cambios de dependencias, commits, merges ni ajustes de configuración del repositorio. Las propuestas y casos de prueba de estos documentos **no están implementados**. Evidencia estática y resultados de comandos se distinguen de pruebas dinámicas pendientes. No se certifica preparación para producción.


## A. Resumen ejecutivo

**RoomMatch tiene una base funcional amplia, pero no cumple todavía las condiciones de terminación ni está listo para producción.** El monolito Angular/Spring Boot/SQL Server es apropiado para el alcance; no hay evidencia que justifique cambiar el stack, Java 17 ni versiones mayores. Los problemas principales son de seguridad efectiva, integridad de reglas, contratos y validación, no falta de tecnologías nuevas.

Los bloqueos más relevantes son la clave JWT pública usada como fallback, emails ajenos en matches/favoritos, reactivación de habitaciones sancionadas o fuera de cupo, compatibilidad persistida obsoleta, solicitudes sin reenvío/cancelación y con carreras, SQL de imágenes de publicación incompleto, API fija a localhost, admin ficticio y ausencia de pruebas que cubran esas reglas. Un frontend que compila y un contextLoads exitoso no resuelven esos problemas.

Hay controles que deben conservarse: firma JWT verificada; comprobación de expiración; estado y rol del usuario tomados de BD en cada request; restricciones ADMIN de servidor; identidad extraída del principal para operaciones propias; queries de ownership por ID+dueño; DTO separados de entidades; BCrypt; validaciones Bean Validation; consultas parametrizadas; FK/UNIQUE/CHECK en SQL y varias lecturas readOnly. Las rutas `/mis` están protegidas antes de los comodines públicos, y ContactoUsuarioResponse sí oculta los campos privados del contacto desbloqueado.

La auditoría recorre 278 archivos versionados, 69 endpoints, 17 controllers, 17 servicios, 22 repositories, 34 DTO, 19 entidades, 19 tablas y 23 entradas de rutas Angular. El manifiesto y matrices completas están en `ROOMMATCH_ARCHITECTURE.md`. Seguridad se detalla en `ROOMMATCH_SECURITY.md`; casos de verificación en `ROOMMATCH_TEST_PLAN.md`; decisiones/sprints en `ROOMMATCH_ROADMAP.md`.

### Alcance y límites de evidencia

| Revisión | Resultado y límite |
| ---| ---|
| Repositorio completo en main | Inventario, lectura y búsquedas cruzadas; SHA fijado y revalidado; sin cambios versionados |
| Baseline frontend | Instalación y build ejecutados; 2 tests ejecutados y fallidos |
| Baseline backend local | Wrapper literal sin permiso; alternativa sh bloqueada por resolución Maven; no se declara compilación fallida por código |
| CI de ese mismo SHA | Jobs backend/frontend exitosos; backend 1 test; frontend sin paso tests |
| SQL Server real y performance | No había BD ni Docker/sqlcmd; cotejo DDL y conteo estructural, sin planes/latencias medidas |
| Navegador/accesibilidad/responsive | Inspección de templates/CSS; Playwright sin Chromium y descarga de shell agotó timeouts; sin screenshots ni certificación WCAG |
| Producción/infraestructura | No se entregó despliegue ni configuración real; defaults inseguros no prueban incidente ni uso en producción |
| Dependencias | npm audit ejecutado; Maven tree/escaneo transitivo no completado por el mismo bloqueo de resolución |
| PR/settings | 2 PR abiertos revisados, ambos con conflictos; main protected=false; no cambios remotos |

Las recetas de reproducción usan fixtures A/B/C/ADMIN y una BD desechable. Ningún SQL destructivo ni explotación contra cuentas reales fue ejecutado. Se preservaron código, README, stack, main y PR. No se pidió eludir errores ni se instalaron versiones nuevas del proyecto para obtener verde.

## B. Baseline reproducible

Directorio backend: `roommatch-api/roommatch-api`; frontend: `roommatch-web`. Runtime local: Java 17.0.20, Node 24.19.0, npm 11.9.0. CI configura Node 22; package.json declara npm 11.13.0. Se registra esa diferencia para repetir el baseline, sin atribuir el error ActivatedRoute a la versión de Node.

| Comando | Resultado observado | Interpretación |
| ---| ---| ---|
| `./mvnw clean test` | Exit 126, Permission denied | mvnw versionado con modo 100644; no se hizo chmod en el checkout |
| `./mvnw clean verify` | Exit 126, Permission denied | Mismo problema de permiso |
| `sh mvnw clean test` | Exit 1, parent POM no resoluble | `org.springframework.boot:spring-boot-starter-parent:3.5.16`; DNS de repo.maven.apache.org falla |
| `sh mvnw clean verify` | Exit 1, parent POM no resoluble | No llegó a compilar, testear ni empaquetar |
| `sh mvnw dependency:tree` | Exit 1, mismo parent | Inventario Maven transitivo/scan pendiente |
| Reintento Maven con proxy del entorno en settings temporal | Conexión rechazada | Sin alterar pom, wrapper o settings del repositorio; no resolvió el bloqueo |
| `npm ci` | Exit 0; 474 paquetes, 18 s | Lock mantenido; sin actualización automática |
| `npm run build` | Exit 0; generación 6,733 s según Angular | Main 773,26 kB + CSS 81,18 kB; 854,44 kB raw/167,75 kB transfer estimado |
| `npm test -- --watch=false` | Exit 1; 1 archivo, 2 fallidas, 0 omitidas | Vitest 4.1.9; ambas NG0201 ActivatedRoute; duración total 2,09 s |
| `npm audit --json` | Exit 1 por advisories | 23 paquetes afectados: 11 high, 11 moderate, 1 low, 0 critical; no equivale a 23 exploits |

Extractos significativos de logs, conservando los errores:

```text
./mvnw: Permission denied
Non-resolvable parent POM ... spring-boot-starter-parent:pom:3.5.16
repo.maven.apache.org: Temporary failure in name resolution

NG0201: No provider found for ActivatedRoute. Source: DynamicTestModule
Test Files  1 failed (1)
Tests       2 failed (2)

mis-publicaciones.css exceeded maximum budget.
Budget 12.00 kB ... total of 14.06 kB (2.06 kB over).
```

El segundo test espera `Hello, roommatch-web`, además del error común de providers. No se modificó el setup para esconderlo. No se observó warning de TypeScript ni otro diagnóstico Angular en build; sí presupuesto CSS. npm avisa `Unknown env config http-proxy` (del entorno) y ofrece nueva versión mayor de npm; no se aplicó. Tests backend omitidos localmente **no son 0 skipped de una suite ejecutada**: no llegaron a iniciar por resolución de dependencias.

### Evidencia CI del commit auditado

[Run 34050396262](https://github.com/1621-nicolas/roommatch/actions/runs/34050396262), 6 de septiembre de 2026, mismo SHA. [Backend Java 17](https://github.com/1621-nicolas/roommatch/actions/runs/34050396262/job/101532855452): `clean test`, 1 test, 0 failures, 0 errors, 0 skipped, 7,462 s para contextLoads y BUILD SUCCESS. [Frontend Angular](https://github.com/1621-nicolas/roommatch/actions/runs/34050396262/job/101532855271): npm ci/build correctos; **ningún test frontend**. Es evidencia remota histórica, no reemplazo del verify local pendiente.

Warnings backend de ese CI: H2Dialect explícito innecesario; contraseña de seguridad generada por autoconfiguración (valor omitido); advertencias springdoc por docs/UI habilitados; advertencia JVM de class-data-sharing. No se demuestra acceso de ese usuario autogenerado mediante HTTP porque la aplicación define su propia cadena y filtro. Warnings Actions: setup-node@v4 apunta a runtime Node 20 obsoleto y fue forzado a 24; deprecaciones punycode y url.parse; esto es distinto del Node 22 configurado para compilar Angular. Frontend repite warning CSS. npm ci en ese CI informó 20 advisories (15 high/4 moderate/1 low); la consulta del 12 septiembre informa 23. Son snapshots de fechas distintas, no evidencia de que se modificaran dependencias durante esta auditoría.

## C. Prioridades y alcance del registro

P0 CRÍTICO: bloqueo inmediato de producción bajo condición indicada. P1 ALTO: seguridad/integridad o funcionalidad central. P2 MEDIO: contrato, rendimiento, mantenibilidad o UX relevantes. P3 BAJO: limpieza y mejora controlada. CONFIRMADO significa evidencia directa de código/configuración o ejecución consignada; RIESGO identifica impacto condicionado/interleaving/infraestructura no comprobados dinámicamente. No se informa una vulnerabilidad explotada solo porque falte una anotación.


| Severidad | Hallazgos registrados |
| --- | --- |
| P0 | 1 |
| P1 | 19 |
| P2 | 21 |
| P3 | 4 |

P0 incluye el fallback de firma conocido bajo la condición de que el despliegue lo use. Los riesgos se distinguen de fallos ejecutados; la severidad prioriza trabajo y no demuestra explotación en producción. Un hallazgo puede agrupar defectos relacionados, por lo que 45 hallazgos no equivale a 45 vulnerabilidades.

## Funcionalidades

| Funcionalidad | Frontend | Backend | BD | Tests actuales | Estado |
| --- | --- | --- | --- | --- | --- |
| Registro/login/logout | auth/login, auth/register; AuthService | Auth / Auth | Usuario→usuario; Rol→rol | Sin tests específicos; Sin casos de auth; SEC | PARCIAL |
| Cuenta propia | mi-cuenta; UsuarioService | Usuario / Usuario | Usuario→usuario | Sin tests específicos; Implementación estática; sin pruebas; HTTP/PERFIL | COMPLETA |
| Perfil de convivencia | perfil, mi-cuenta; PerfilService | PerfilConvivencia / PerfilConvivencia | PerfilConvivencia→perfil_convivencia | Sin tests específicos; Sin pruebas; MAT/PERFIL | PARCIAL |
| Búsqueda básica de perfiles | Sin consumidor TS | PerfilConvivencia / PerfilConvivencia | PerfilConvivencia, Usuario→perfil_convivencia, usuario | Sin tests específicos; Sin pruebas; DB/PERF | NO UTILIZADA |
| Matches | matches, home, publicaciones | Match / Match | MatchResultado→match_resultado; perfiles/usuario | Sin tests específicos; Sin pruebas; MAT/SEC/PRIV | PARCIAL |
| Favoritos | favoritos, matches; FavoritoService | Favorito / Favorito | FavoritoUsuario→favorito_usuario | Sin tests específicos; Email expuesto; sin pruebas; PRIV/CON | PARCIAL |
| Enviar/aceptar/rechazar solicitud | solicitudes, matches, favoritos, publicación detalle | SolicitudContacto / SolicitudContacto | solicitud_contacto, contacto_roomie, notificacion | Sin tests específicos; Sin pruebas; SOL | PARCIAL |
| Cancelar/reenviar solicitud | Ausente | — / SolicitudContacto parcial | Estado cancelada definido en SQL | Sin tests específicos; Sin pruebas; SOL | INCOMPLETA |
| Contacto propio y desbloqueado | mi-cuenta, contactos; ContactoService | ContactoUsuario / ContactoUsuario | contacto_usuario, contacto_roomie | Sin tests específicos; Filtro privado presente; fugas laterales; PRIV | PARCIAL |
| Notificaciones | notificaciones; NotificacionService | Notificacion / Notificacion | Notificacion→notificacion | Sin tests específicos; Sin pruebas; WEB/OWN | PARCIAL |
| Catálogo/detalle habitaciones | habitaciones, habitacion-detalle, home | Habitacion / Habitacion | Habitacion, Propietario→habitacion, propietario | Sin tests específicos; Sin galería; sin pruebas; PUB/WEB | PARCIAL |
| Alta/edición/pausa/activación habitaciones | propietario/habitaciones | Habitacion / Habitacion | habitacion, propietario, suscripcion_propietario, plan_propietario | Sin tests específicos; Sin pruebas; PLAN/OWN/MOD | PARCIAL |
| Estado alquilada/eliminada de habitación | Sin acción pública implementada | — / Sin transición dedicada | CHECK SQL permite alquilada/eliminada | Sin tests específicos; No asumir que DELETE habitación exista | INCOMPLETA |
| Imágenes de habitación | gestor real solo propietario | ImagenHabitacion / ImagenHabitacion | imagen_habitacion, habitacion | Sin tests específicos; Sin pruebas; IMG/OWN | PARCIAL |
| Leads/intereses | habitacion-detalle; solicitudes; propietario/leads | LeadHabitacion / LeadHabitacion | lead_habitacion, habitacion, notificacion | Sin tests específicos; Sin pruebas; PRIV/OWN/CON | PARCIAL |
| Alta/perfil de propietario | propietario/registro, contexto | Propietario / Propietario | propietario, usuario, rol, suscripcion_propietario | Sin tests específicos; Sin pruebas; ROLE | PARCIAL |
| Planes/suscripción | propietario/planes, panel | PlanPropietario / PlanPropietario | plan_propietario, suscripcion_propietario | Sin tests específicos; Sin facturación; sin pruebas; PLAN | PARCIAL |
| Publicaciones roomie catálogo | publicaciones-roomie, home | PublicacionRoomie / PublicacionRoomie, Match | publicacion_roomie, match_resultado, perfil_convivencia | Sin tests específicos; Sin pruebas; PUB/PERF | PARCIAL |
| Detalle público publicación | publicaciones-roomie/:id | PublicacionRoomie / PublicacionRoomie | publicacion_roomie | Sin tests específicos; Anónimo rechazado; PUB-01 | ROTA |
| Gestión/vínculo/borrado publicación | mis-publicaciones | PublicacionRoomie / PublicacionRoomie | publicacion_roomie, imagen_publicacion, habitacion | Sin tests específicos; Sin pruebas; PUB/OWN/D-01/D-03 | PARCIAL |
| Imágenes de publicación | Sin servicio/template integrado | ImagenPublicacion / ImagenPublicacion | imagen_publicacion (falta principal) | Sin tests específicos; SQL entregado incompatible; IMG/DB | ROTA |
| Reportar usuario/habitación | report-modal vacío sin uso | Reporte / Reporte | reporte_usuario, reporte_habitacion | Sin tests específicos; Backend disponible; UI ausente; MOD/ADMIN | INCOMPLETA |
| Dashboard admin | admin/dashboard placeholder | DashboardAdmin / DashboardAdmin | agregados de tablas principales | Sin tests específicos; No consumo frontend; ADMIN | INCOMPLETA |
| Revisar/rechazar/sancionar reportes | admin/reportes ficticio | Reporte / Reporte | reporte_usuario, reporte_habitacion, usuario, habitacion | Sin tests específicos; API parcial; ADMIN/MOD | INCOMPLETA |
| Recuperación de contraseña | Enlace vuelve a login | — / — | Sin token de recuperación | Sin tests específicos; Alcance de producto por decidir | INCOMPLETA |
| Componentes compartidos de tarjetas/estado | 7 placeholders; Navbar y gestor sí usados | — / — | — | Sin tests específicos; Sin pruebas; WEB/A11Y | NO UTILIZADA |

## Registro de problemas con evidencia y tratamiento

Las referencias fijan el commit auditado y una línea de entrada; método y causa se explican en cada ficha. “Cómo reproducir” es receta para fixtures locales, no afirmación de que una prueba HTTP/SQL se haya ejecutado. Los bloqueos dinámicos constan en baseline.

### RM-01 — P0 — Clave de firma JWT pública como fallback de cualquier entorno

**Clasificación:** CONFIRMADO. **Archivo/método/línea:** [roommatch-api/roommatch-api/src/main/resources/application.properties: 25](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-api/roommatch-api/src/main/resources/application.properties#L25), [roommatch-api/roommatch-api/src/main/java/com/roommatch/security/JwtService.java: 23](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-api/roommatch-api/src/main/java/com/roommatch/security/JwtService.java#L23), [roommatch-api/roommatch-api/src/main/java/com/roommatch/security/JwtAuthenticationFilter.java: 70](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-api/roommatch-api/src/main/java/com/roommatch/security/JwtAuthenticationFilter.java#L70).

**Descripción y evidencia:** jwt.secret admite un literal conocido si falta JWT_SECRET. No hay perfil production ni validación de la clave al arrancar. El filtro identifica al usuario por el subject firmado. La clave de desarrollo tiene 76 bytes UTF-8: longitud suficiente no equivale a secreto ni entropía.

**Impacto:** Un despliegue con ese fallback permite fabricar tokens para un email conocido, incluido un administrador activo. Configuración vulnerable confirmada; no se ha comprobado un despliegue de producción afectado. P0 es la prioridad de bloqueo de publicación bajo ese supuesto.

**Cómo reproducir:** En una instancia de prueba con BD operativa, quitar JWT_SECRET y verificar que la configuración sigue resolviendo una clave. Prueba automatizada controlada: un JWT firmado con el fallback y subject de una cuenta de fixture resulta verificable. No probar contra usuarios reales.

**Solución propuesta:** Separar development/test/production; producción exige secreto aleatorio externo y validación de clave, algoritmo y TTL en startup. Rechazar claves de ejemplo y ausencia. Planear rotación si algún entorno utilizó el valor público.

**Riesgo de la solución:** Invalidará sesiones firmadas con la clave anterior. Mantener desarrollo local con configuración explícita; nunca generar un secreto distinto por réplica.

**Tests necesarios:** SEC-01 a SEC-05: arranque sin clave, corta, de ejemplo; firma válida, alterada y expiración.


### RM-02 — P1 — Emails de acceso ajenos expuestos por matches y favoritos

**Clasificación:** CONFIRMADO. **Archivo/método/línea:** [roommatch-api/roommatch-api/src/main/java/com/roommatch/dto/MatchResponse.java: 33](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-api/roommatch-api/src/main/java/com/roommatch/dto/MatchResponse.java#L33), [roommatch-api/roommatch-api/src/main/java/com/roommatch/dto/FavoritoResponse.java: 30](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-api/roommatch-api/src/main/java/com/roommatch/dto/FavoritoResponse.java#L30), [roommatch-api/roommatch-api/src/main/java/com/roommatch/service/FavoritoService.java: 30](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-api/roommatch-api/src/main/java/com/roommatch/service/FavoritoService.java#L30), [roommatch-api/roommatch-api/src/main/java/com/roommatch/dto/LeadHabitacionResponse.java: 54](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-api/roommatch-api/src/main/java/com/roommatch/dto/LeadHabitacionResponse.java#L54).

**Descripción y evidencia:** MatchResponse y FavoritoResponse copian usuario.getEmail() sin consultar ContactoRoomie ni mostrarEmail. Agregar un favorito solo exige que el destinatario exista y esté activo. LeadHabitacionResponse también copia el email de acceso a la respuesta del interesado.

**Impacto:** Una cuenta autenticada puede obtener emails sin contacto aceptado; la privacidad del email de contacto no protege el de autenticación. En leads existe interés explícito, pero el consentimiento concreto para compartir ese email no está definido.

**Cómo reproducir:** Crear A/B sin contacto entre sí y B con mostrarEmail=false. Desde A agregar B a favoritos y leer response.data.email; repetir con matches. En leads, enviar interés y revisar la respuesta disponible para el propietario.

**Solución propuesta:** Eliminar email de login de DTO de descubrimiento y de sus interfaces TS; canalizar comunicación a contactos autorizados. Definir con producto si el lead comparte un email de contacto elegido y mostrar consentimiento antes de enviarlo.

**Riesgo de la solución:** Revisar consumidores que usen email como texto o enlace. La decisión de leads requiere aprobación de negocio; el email de login no debe convertirse implícitamente en dato público.

**Tests necesarios:** PRIV-01/02: serialización sin email en favoritos/matches, matriz de visibilidad y consentimiento en leads.


### RM-03 — P1 — SQL Server y configuración local sin separación de producción

**Clasificación:** RIESGO. **Archivo/método/línea:** [roommatch-api/roommatch-api/src/main/resources/application.properties: 6](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-api/roommatch-api/src/main/resources/application.properties#L6), [roommatch-api/roommatch-api/src/main/resources/application.properties: 8](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-api/roommatch-api/src/main/resources/application.properties#L8).

**Descripción y evidencia:** El import opcional de application-local.properties aplica globalmente. URL por defecto con encrypt=false y trustServerCertificate=true; usuario sa por defecto. No hay configuración production ni validación de entorno.

**Impacto:** Un entorno que reutilice los defaults no obliga a cifrar y validar el certificado de SQL Server ni a usar mínimo privilegio. No se ha inspeccionado la conexión de un servidor desplegado.

**Cómo reproducir:** Resolver propiedades sin DB_URL/DB_USERNAME en un entorno de prueba y observar los defaults; verificar por separado negociación TLS y permisos efectivos contra SQL Server.

**Solución propuesta:** Mantener defaults solo en development; producción encrypt=true, trustServerCertificate=false, nombre/cadena de confianza correctos, credenciales de aplicación limitadas y secretos externos. Separar cuenta de migración de cuenta de ejecución.

**Riesgo de la solución:** Certificados locales autofirmados requieren excepción solo local. Validar certificados antes del cambio para evitar interrupción de conexión.

**Tests necesarios:** CFG-01/02: matriz de perfiles y conexión SQL Server con certificado válido/no confiable.


### RM-04 — P2 — Swagger público y ausencia de política CSP declarada

**Clasificación:** CONFIRMADO. **Archivo/método/línea:** [roommatch-api/roommatch-api/src/main/java/com/roommatch/config/SecurityConfig.java: 52](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-api/roommatch-api/src/main/java/com/roommatch/config/SecurityConfig.java#L52), [roommatch-api/roommatch-api/src/main/resources/application.properties: 30](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-api/roommatch-api/src/main/resources/application.properties#L30), [roommatch-web/src/index.html: 1](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-web/src/index.html#L1).

**Descripción y evidencia:** Swagger y /v3/api-docs son permitAll sin distinción de perfil. No se declara CSP/Trusted Types en frontend ni configuración del servidor que aloje la SPA. Esto no demuestra que un proxy externo carezca de headers.

**Impacto:** Superficie de reconocimiento innecesaria en producción y menor contención de un XSS si aparece. Exponer OpenAPI por sí solo no constituye acceso a datos privados.

**Cómo reproducir:** Consultar documentación sin token en instancia de prueba; inspeccionar headers reales de la SPA desplegada antes de dar por ausente una política externa.

**Solución propuesta:** Swagger habilitado en development; deshabilitado o autenticado como ADMIN en production. Diseñar CSP con nonces/hash compatibles con Angular, probar en report-only y luego aplicar; revisar frame-ancestors, referrer y HTTPS en hosting.

**Riesgo de la solución:** Una CSP aplicada sin inventario puede bloquear estilos Angular y URLs de imágenes. No exigir unsafe-inline global como atajo.

**Tests necesarios:** SEC-06 y WEB-09: permisos OpenAPI por perfil y prueba de render con CSP.


### RM-05 — P2 — Sesión JWT de 24 horas sin revocación individual ni contrato completo

**Clasificación:** CONFIRMADO. **Archivo/método/línea:** [roommatch-api/roommatch-api/src/main/java/com/roommatch/security/JwtService.java: 28](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-api/roommatch-api/src/main/java/com/roommatch/security/JwtService.java#L28), [roommatch-api/roommatch-api/src/main/java/com/roommatch/security/JwtService.java: 50](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-api/roommatch-api/src/main/java/com/roommatch/security/JwtService.java#L50), [roommatch-web/src/app/core/services/auth.service.ts: 96](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-web/src/app/core/services/auth.service.ts#L96).

**Descripción y evidencia:** JWT contiene email en sub, idUsuario, nombres, apellidos y rol; exp/iat y firma sí se usan. No hay issuer/audience, jti, versionado, refresh, logout de servidor ni rotación con kid. validarToken y obtenerEmailDelToken vuelven a parsear. Logout elimina almacenamiento local.

**Impacto:** Un token robado sigue siendo utilizable hasta exp, suspensión de la cuenta o rotación global. No se demostró robo ni XSS ejecutable. Nombres/apellidos no aportan al filtro y son legibles por el portador.

**Cómo reproducir:** En una fixture, guardar el token, cerrar sesión en frontend y utilizarlo de nuevo contra /api/usuarios/me antes de exp. Cambiar rol/estado en BD para verificar que el filtro sí consulta esos valores actuales.

**Solución propuesta:** Reducir claims, usar un identificador estable como sub tras plan de transición, issuer/audience y una única validación. Proponer TTL más corto y reingreso antes que refresh por defecto; tokenVersion por usuario si se exige cierre global de sesiones. Documentar rotación y exclusividad de claves por entorno.

**Riesgo de la solución:** TTL, cierre de sesiones y transporte afectan UX; acordarlos. No migrar a cookies ni agregar refresh sin necesidad demostrada.

**Tests necesarios:** SEC-02 a SEC-10: claims mínimos, roles de BD, suspensión, logout y expiración con Clock controlado.


### RM-06 — P1 — Autenticación y endpoints sensibles sin controles de abuso

**Clasificación:** CONFIRMADO. **Archivo/método/línea:** [roommatch-api/roommatch-api/src/main/java/com/roommatch/controller/AuthController.java: 26](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-api/roommatch-api/src/main/java/com/roommatch/controller/AuthController.java#L26), [roommatch-api/roommatch-api/src/main/java/com/roommatch/service/AuthService.java: 31](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-api/roommatch-api/src/main/java/com/roommatch/service/AuthService.java#L31), [roommatch-api/roommatch-api/src/main/java/com/roommatch/dto/RegistroRequest.java: 30](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-api/roommatch-api/src/main/java/com/roommatch/dto/RegistroRequest.java#L30), [roommatch-api/roommatch-api/src/main/java/com/roommatch/service/ReporteService.java: 38](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-api/roommatch-api/src/main/java/com/roommatch/service/ReporteService.java#L38).

**Descripción y evidencia:** No existe rate limiting, backoff, cuotas o CAPTCHA adaptativo en el repositorio. Registro admite mínimo 6 y máximo 100 caracteres; BCrypt recibe el password. Las restricciones únicas no frenan intentos de login, reportes nuevos ni cálculo masivo de matches.

**Impacto:** Exposición a brute force, credential stuffing, spam y consumo de recursos; no se afirma que el sistema esté bajo ataque. Diferencias de errores de registro/login facilitan enumeración.

**Cómo reproducir:** En pruebas locales, repetir login incorrecto, registros, reportes y calcular matches: no hay lógica que devuelva 429 o imponga enfriamiento. Medir antes de pruebas de carga fuera de fixtures.

**Solución propuesta:** Límite por cuenta normalizada e IP confiable con ventana/backoff y 429 Retry-After; cuotas por actor y par/recurso para solicitudes/reportes/calcular. Sin MFA, proponer mínimo 15 caracteres, permitir frases/gestores, sin reglas de composición ni rotación periódica. Conciliar el límite de 72 bytes de BCrypt con la política de longitud y comprobar contraseñas comunes.

**Riesgo de la solución:** No bloquear permanentemente una cuenta por ataques ajenos. Límite en memoria sirve a una instancia académica; al escalar, compartir estado o usar gateway. No aceptar X-Forwarded-For arbitrario.

**Tests necesarios:** SEC-11/12: límites, NAT, reintentos, Unicode/bytes BCrypt, errores genéricos y ausencia de password en logs.


### RM-07 — P2 — Semántica HTTP inconsistente y errores de autenticación ambiguos

**Clasificación:** CONFIRMADO. **Archivo/método/línea:** [roommatch-api/roommatch-api/src/main/java/com/roommatch/exception/GlobalExceptionHandler.java: 26](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-api/roommatch-api/src/main/java/com/roommatch/exception/GlobalExceptionHandler.java#L26), [roommatch-api/roommatch-api/src/main/java/com/roommatch/controller/FavoritoController.java: 40](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-api/roommatch-api/src/main/java/com/roommatch/controller/FavoritoController.java#L40), [roommatch-api/roommatch-api/src/main/java/com/roommatch/security/JwtAuthenticationFilter.java: 98](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-api/roommatch-api/src/main/java/com/roommatch/security/JwtAuthenticationFilter.java#L98), [roommatch-api/roommatch-api/src/main/java/com/roommatch/config/SecurityConfig.java: 44](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-api/roommatch-api/src/main/java/com/roommatch/config/SecurityConfig.java#L44).

**Descripción y evidencia:** IllegalArgumentException se usa para inexistencia, ownership, duplicados y credenciales incorrectas y termina en 400. Ya existe manejo de DataIntegrityViolationException=409 y AccessDeniedException=403 en MVC. No se configura entrada 401/cuerpo JSON en el filtro de seguridad. El filtro captura también fallos de BD como JWT no procesable.

**Impacto:** Clientes no pueden distinguir validación de autorización o conflicto; incidencias de infraestructura se confunden con sesión inválida. El estado exacto del filtro sin credenciales requiere integración; no fue medido localmente.

**Cómo reproducir:** Pedir ID inexistente, editar recurso ajeno, repetir favorito y fallar login; revisar 400 en ramas de controller/advice. Simular BD caída durante autenticación. Cubrir JSON malformado y tipos incorrectos: no tienen handlers específicos.

**Solución propuesta:** Excepciones de dominio 404/403/409/400 y contrato ApiResponse consistente con errorCode, detalles de campo y correlationId opcionales. Configurar AuthenticationEntryPoint 401 y AccessDeniedHandler 403; simplificar catches después de migrar consumidores.

**Riesgo de la solución:** Frontend actualmente interpreta mensajes/400 como ausencia de perfil. Migrar cliente y tests junto con los nuevos estados; no devolver detalles internos.

**Tests necesarios:** HTTP-01 a HTTP-04: matriz de status/body, invalid JSON, validación, permisos y BD caída.


### RM-08 — P1 — Vinculación a habitación de terceros sin regla de autorización definida

**Clasificación:** RIESGO. **Archivo/método/línea:** [roommatch-api/roommatch-api/src/main/java/com/roommatch/service/PublicacionRoomieService.java: 1042](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-api/roommatch-api/src/main/java/com/roommatch/service/PublicacionRoomieService.java#L1042), [roommatch-api/roommatch-api/src/main/java/com/roommatch/service/PublicacionRoomieService.java: 1096](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-api/roommatch-api/src/main/java/com/roommatch/service/PublicacionRoomieService.java#L1096), [roommatch-web/src/app/pages/publicaciones-roomie/mis-publicaciones/mis-publicaciones.ts: 1](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-web/src/app/pages/publicaciones-roomie/mis-publicaciones/mis-publicaciones.ts#L1).

**Descripción y evidencia:** aplicarVinculacionVivienda busca por idHabitacion y exige activa; no verifica dueño ni consentimiento. El frontend ofrece habitaciones del catálogo público para busco_compartir: hay evidencia de una intención más amplia que propiedad exclusiva.

**Impacto:** Se puede asociar una publicación propia a habitación ajena. Es riesgo de suplantación comercial si la UI implica autorización, pero no prueba de modificación de la habitación ajena ni BOLA de escritura sobre ella.

**Cómo reproducir:** A publica con idHabitacion activo perteneciente a B. La validación no contiene condición de ownership/consentimiento. Comprobar cómo se describe ese vínculo al visitante.

**Solución propuesta:** Decisión D-01: referencia pública de interés claramente rotulada; autorización del propietario; o solo habitaciones propias. Recomendar conservar la referencia pública si es la intención original, sin presentar al autor como representante y respetando visibilidad posterior; autorización explícita si se ofrece la habitación.

**Riesgo de la solución:** Elegir solo propiedad eliminaría un caso actual para usuarios sin perfil de propietario. Esperar decisión antes de cambiar esta regla.

**Tests necesarios:** OWN-05: A/B, inactiva, ajena, autorizada y retirada posterior, según D-01.


### RM-09 — P2 — Matching incompleto y poco gradual

**Clasificación:** CONFIRMADO. **Archivo/método/línea:** [roommatch-api/roommatch-api/src/main/java/com/roommatch/service/MatchService.java: 616](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-api/roommatch-api/src/main/java/com/roommatch/service/MatchService.java#L616), [roommatch-api/roommatch-api/src/main/java/com/roommatch/service/PerfilConvivenciaService.java: 106](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-api/roommatch-api/src/main/java/com/roommatch/service/PerfilConvivenciaService.java#L106), [roommatch-web/src/app/pages/perfil/perfil.html: 600](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-web/src/app/pages/perfil/perfil.html#L600).

**Descripción y evidencia:** Se puntúan 10 criterios con 1 punto cada uno. Alcohol, gastos y fechaMudanza se guardan y no se usan. Escalas admiten distancia <=1 como coincidencia completa: 4/5 ya difiere de 1/5; no es igualdad estricta. Presupuesto solo comprueba intersección y tocar un extremo cuenta igual que intervalos idénticos.

**Impacto:** Porcentaje de coincidencias binarias no calibrado como probabilidad de buena convivencia; pérdida de información y expectativas de formulario incumplidas. Datos faltantes penalizados como incompatibilidad.

**Cómo reproducir:** Mantener 10 criterios iguales y cambiar solo alcohol/gastos/fecha: score idéntico. Comparar presupuestos [500,800]/[800,1200] e idénticos: ambos criterio=1.

**Solución propuesta:** Propuesta v2 detallada en este informe: 13 criterios, fórmulas parciales, pesos explícitos provisionales y cobertura separada. Separar algoritmo puro del acceso a datos, explicar contribuciones y versionar configuración. Validar pesos con negocio y resultados reales; no venderlos como científicamente calibrados.

**Riesgo de la solución:** Cambiar pesos/categorías cambia rankings. Requiere D-05 y migración/invalidez de resultados previos.

**Tests necesarios:** MAT-01 a MAT-16: escalas exhaustivas, presupuesto, fechas, categorías, faltantes, simetría y explicaciones.


### RM-10 — P1 — Compatibilidades obsoletas tras actualizar un perfil

**Clasificación:** CONFIRMADO. **Archivo/método/línea:** [roommatch-api/roommatch-api/src/main/java/com/roommatch/service/PerfilConvivenciaService.java: 62](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-api/roommatch-api/src/main/java/com/roommatch/service/PerfilConvivenciaService.java#L62), [roommatch-api/roommatch-api/src/main/java/com/roommatch/service/MatchService.java: 396](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-api/roommatch-api/src/main/java/com/roommatch/service/MatchService.java#L396), [roommatch-api/roommatch-api/src/main/java/com/roommatch/service/MatchService.java: 442](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-api/roommatch-api/src/main/java/com/roommatch/service/MatchService.java#L442), [roommatch-api/roommatch-api/src/main/java/com/roommatch/repository/MatchResultadoRepository.java: 74](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-api/roommatch-api/src/main/java/com/roommatch/repository/MatchResultadoRepository.java#L74).

**Descripción y evidencia:** Actualizar perfil guarda datos sin invalidar matches. obtenerCompatibilidadEntreUsuarios devuelve primero resultado directo o inverso guardado, sin comparar fecha/versiones de ambos perfiles. listarMisMatches lee filas persistidas. El recalculo de A no renueva por obligación B→A.

**Impacto:** Publicaciones y matches pueden mostrar porcentajes y razones que ya no corresponden a los perfiles actuales; usuarios suspendidos pueden seguir en listas persistidas.

**Cómo reproducir:** Calcular A/B, cambiar presupuesto o hábitos de B y consultar publicaciones o matches de A sin ejecutar calcular de A. La rama de cache retorna el resultado anterior.

**Solución propuesta:** Seleccionar versionado de perfil y algoritmo, con recalculo bajo demanda y carga batch. Cache válido solo si coinciden ambas versiones; invalidación simétrica en la transición inicial. Para ranking, reconstruir el conjunto de candidatos antes de filtrar/ordenar/paginar: actualizar solo la página no garantiza ranking correcto.

**Riesgo de la solución:** Una invalidación por fecha sola sufre precisión/reloj y carreras. Capturar versiones en el cálculo y no publicar un resultado si cambian durante él. Cambiar estado de usuario debe excluirlo de lectura y ranking.

**Tests necesarios:** MAT-17 a MAT-21: A y B modificados, cache inverso, recalculo concurrente, suspensión y orden global.


### RM-11 — P1 — Cálculo de matches escala con todos los perfiles y escrituras individuales

**Clasificación:** RIESGO. **Archivo/método/línea:** [roommatch-api/roommatch-api/src/main/java/com/roommatch/service/MatchService.java: 101](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-api/roommatch-api/src/main/java/com/roommatch/service/MatchService.java#L101), [roommatch-api/roommatch-api/src/main/java/com/roommatch/service/MatchService.java: 203](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-api/roommatch-api/src/main/java/com/roommatch/service/MatchService.java#L203), [roommatch-api/roommatch-api/src/main/java/com/roommatch/service/MatchService.java: 255](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-api/roommatch-api/src/main/java/com/roommatch/service/MatchService.java#L255), [roommatch-api/roommatch-api/src/main/java/com/roommatch/service/MatchService.java: 279](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-api/roommatch-api/src/main/java/com/roommatch/service/MatchService.java#L279).

**Descripción y evidencia:** findAll carga todos los perfiles; cada candidato consulta su match y llama save. Ordena la lista completa y responde todos los matches calculados. Hibernate puede adelantar flush al ejecutar la siguiente consulta. No hay paginación de candidatos ni control de frecuencia.

**Impacto:** O(N) memoria y consultas/escrituras, O(N log N) ordenación por cálculo; si todos calculan, O(N²) pares persistidos. Escala de 100.000 no es viable con este diseño sin acotación. No se midieron tiempos SQL reales.

**Cómo reproducir:** Sembrar conjuntos controlados de 100/1.000/10.000, instrumentar Hibernate Statistics y repetir un cálculo con cache vacío/lleno. En auditoría solo se hizo conteo estructural de llamadas.

**Solución propuesta:** Proyección de perfiles activos, cargar resultados existentes en lote y calcular sin I/O dentro del bucle. Para más volumen, candidatos indexables y top-K con semántica visible. saveAll por sí solo no garantiza batching JDBC, especialmente con IDENTITY; decidir si conviene persistir solo top-K o calcular dinámicamente.

**Riesgo de la solución:** Prefiltrar puede ocultar candidatos; no descartar por distrito/fecha sin acordar requisitos excluyentes. Medir antes de añadir colas/servicios externos.

**Tests necesarios:** PERF-01/02: número de consultas por lote, límites de memoria, mismo ranking y límites de frecuencia.


### RM-12 — P2 — N+1 de compatibilidad en publicaciones

**Clasificación:** CONFIRMADO. **Archivo/método/línea:** [roommatch-api/roommatch-api/src/main/java/com/roommatch/service/PublicacionRoomieService.java: 361](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-api/roommatch-api/src/main/java/com/roommatch/service/PublicacionRoomieService.java#L361), [roommatch-api/roommatch-api/src/main/java/com/roommatch/service/PublicacionRoomieService.java: 819](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-api/roommatch-api/src/main/java/com/roommatch/service/PublicacionRoomieService.java#L819), [roommatch-api/roommatch-api/src/main/java/com/roommatch/service/MatchService.java: 348](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-api/roommatch-api/src/main/java/com/roommatch/service/MatchService.java#L348).

**Descripción y evidencia:** crearResponse por publicación llama obtenerCompatibilidadEntreUsuarios para otro autor autenticado. Un acierto directo consulta 1 repositorio; inverso 2; miss completo 4 (2 matches y 2 perfiles), además de hidratación JPA. Misma persona en varias publicaciones repite búsquedas.

**Impacto:** Latencia crece por fila incluso con paginación. Autor y habitación son LAZY y pueden añadir selects; el número SQL exacto depende del contexto de persistencia y datos.

**Cómo reproducir:** Listar páginas 10/20/50 autenticado con autores distintos en fixtures con hit directo, inverso y miss. Contar consultas, no deducirlas solo de duración.

**Solución propuesta:** Recoger IDs de autores distintos, cargar perfiles/cache actuales en lote y construir un mapa de compatibilidad. Proyección/fetch controlado de autor y vínculo. Reutilizar perfil del solicitante una vez.

**Riesgo de la solución:** Evitar fetch joins de colecciones con paginación. Mantener respuesta anónima sin cálculo ni PII adicional.

**Tests necesarios:** PERF-03: presupuesto de consultas independiente del tamaño de página, equivalencia de DTO, sin datos ajenos.


### RM-13 — P1 — Solicitudes rechazadas bloquean nuevos intentos para siempre; cancelación ausente

**Clasificación:** CONFIRMADO. **Archivo/método/línea:** [roommatch-api/roommatch-api/src/main/java/com/roommatch/service/SolicitudContactoService.java: 49](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-api/roommatch-api/src/main/java/com/roommatch/service/SolicitudContactoService.java#L49), [roommatch-api/roommatch-api/src/main/java/com/roommatch/repository/SolicitudContactoRepository.java: 11](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-api/roommatch-api/src/main/java/com/roommatch/repository/SolicitudContactoRepository.java#L11), [Base de datos_Roommatch.sql: 440](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/Base%20de%20datos_Roommatch.sql#L440), [Base de datos_Roommatch.sql: 446](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/Base%20de%20datos_Roommatch.sql#L446).

**Descripción y evidencia:** enviarSolicitud rechaza cualquier fila existente en ambos sentidos, sin filtrar estado. SQL tiene UNIQUE(emisor, receptor). Hay aceptar/rechazar, pero ningún endpoint para cancelar pese a estado cancelada en SQL.

**Impacto:** A→B rechazada impide nuevo intento meses después. A no puede cancelar por API; una cancelada cargada en BD también bloquearía B→A por exists inverso.

**Cómo reproducir:** A envía, B rechaza y A vuelve a enviar: error por existencia. Buscar ruta cancelar: no existe entre los 69 endpoints.

**Solución propuesta:** Elegir D-02: historial de intentos con un solo pendiente por par no ordenado, cooldown configurable tras rechazo/cancelación, aceptada crea un único vínculo. Nuevo intento crea nueva fila; no borrar historial para sortear UNIQUE.

**Riesgo de la solución:** Reenvío sin cooldown aumenta acoso. Acordar quién cancela, plazo y posibilidad futura de bloquear antes de cambiar el contrato.

**Tests necesarios:** SOL-01 a SOL-08: secuencias requeridas, tiempos, self, repetición, direcciones y ownership.


### RM-14 — P1 — Carreras en aceptar/rechazar y solicitudes cruzadas

**Clasificación:** RIESGO. **Archivo/método/línea:** [roommatch-api/roommatch-api/src/main/java/com/roommatch/service/SolicitudContactoService.java: 109](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-api/roommatch-api/src/main/java/com/roommatch/service/SolicitudContactoService.java#L109), [roommatch-api/roommatch-api/src/main/java/com/roommatch/service/SolicitudContactoService.java: 156](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-api/roommatch-api/src/main/java/com/roommatch/service/SolicitudContactoService.java#L156), [Base de datos_Roommatch.sql: 446](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/Base%20de%20datos_Roommatch.sql#L446), [Base de datos_Roommatch.sql: 457](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/Base%20de%20datos_Roommatch.sql#L457).

**Descripción y evidencia:** No hay @Version, locks ni actualización condicional por estado. La aceptación lee pendiente y crea contacto; rechazo lee el mismo estado y actualiza. UNIQUE por dirección permite A→B y B→A concurrentes; contacto solo es único por solicitud.

**Impacto:** Riesgo de solicitud finalmente rechazada con contacto creado, o contactos duplicados por par. Interleavings permitidos por código/constraints; no se ejecutó reproducción multihilo contra SQL Server.

**Cómo reproducir:** Barrera de concurrencia entre lectura pendiente y escrituras para aceptar/rechazar; dos transacciones cruzadas de envío. Comprobar estado, contacto y notificaciones después del commit.

**Solución propuesta:** CAS pendiente→destino o bloqueo/versionado más transacción. Par canónico min/max con unicidad de pendiente y contacto; migrar duplicados existentes antes del constraint. Hacer aceptación idempotente y notificación consistente.

**Riesgo de la solución:** Un UNIQUE por par en todo el historial impediría reenvío: debe restringir el estado pendiente o una tabla de relación activa. Reintentar solo conflictos esperables.

**Tests necesarios:** SOL-09 a SOL-12: SQL Server concurrente, una aceptación, ningún contacto tras rechazo ganador.


### RM-15 — P2 — Borrado de publicaciones es físico y no conserva historial

**Clasificación:** CONFIRMADO. **Archivo/método/línea:** [roommatch-api/roommatch-api/src/main/java/com/roommatch/service/PublicacionRoomieService.java: 772](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-api/roommatch-api/src/main/java/com/roommatch/service/PublicacionRoomieService.java#L772), [roommatch-api/roommatch-api/src/main/java/com/roommatch/repository/ImagenPublicacionRepository.java: 37](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-api/roommatch-api/src/main/java/com/roommatch/repository/ImagenPublicacionRepository.java#L37), [Base de datos_Roommatch.sql: 336](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/Base%20de%20datos_Roommatch.sql#L336).

**Descripción y evidencia:** El método rotulado eliminar lógicamente elimina imágenes y llama repository.delete sobre la publicación. La FK directa dependiente es imagen_publicacion, sin ON DELETE CASCADE. No existe tabla de reportes de publicaciones; reportes de habitación/usuario no apuntan a id_publicacion.

**Impacto:** Pérdida real de publicación e imágenes; no se afirma una violación de FK de reportes inexistente. Estado eliminada está definido pero esa ruta no lo usa.

**Cómo reproducir:** Crear publicación con imágenes en BD de prueba, DELETE como dueño y verificar ausencia física de ambas filas. El esquema debe corregirse primero para poder probar imágenes.

**Solución propuesta:** D-03: soft delete con estado eliminada, retención y exclusión de catálogos/imagenes/reacciones; purga física posterior separada. Alternativa hard delete documentada si la intención de negocio lo exige.

**Riesgo de la solución:** Conservar datos cambia política de retención; soft delete no sustituye solicitudes de supresión ni permite mostrar recursos eliminados. Esperar decisión.

**Tests necesarios:** PUB-05/06: FK, conservación, lectura pública, reactivación prohibida de eliminadas y purga autorizada.


### RM-16 — P1 — Downgrade, expiración y reactivación eluden límites del plan

**Clasificación:** CONFIRMADO. **Archivo/método/línea:** [roommatch-api/roommatch-api/src/main/java/com/roommatch/service/HabitacionService.java: 69](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-api/roommatch-api/src/main/java/com/roommatch/service/HabitacionService.java#L69), [roommatch-api/roommatch-api/src/main/java/com/roommatch/service/HabitacionService.java: 246](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-api/roommatch-api/src/main/java/com/roommatch/service/HabitacionService.java#L246), [roommatch-api/roommatch-api/src/main/java/com/roommatch/service/HabitacionService.java: 284](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-api/roommatch-api/src/main/java/com/roommatch/service/HabitacionService.java#L284), [roommatch-api/roommatch-api/src/main/java/com/roommatch/service/PlanPropietarioService.java: 135](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-api/roommatch-api/src/main/java/com/roommatch/service/PlanPropietarioService.java#L135), [roommatch-api/roommatch-api/src/main/java/com/roommatch/repository/SuscripcionPropietarioRepository.java: 10](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-api/roommatch-api/src/main/java/com/roommatch/repository/SuscripcionPropietarioRepository.java#L10).

**Descripción y evidencia:** Crear cuenta activas+pausadas. Activar solo cambia estado tras ownership, sin revisar plan. Cambiar plan no comprueba exceso ni destacadas. Suscripción se elige por estado activo, sin fechaFin; nuevas no tienen vencimiento. Frontend cuenta activas y puede diferir del backend.

**Impacto:** 8 habitaciones en plan 10 siguen disponibles tras pasar a límite 1; pausadas pueden activarse por API. Permiso de destacar y suscripciones vencidas no se reconcilian. Planes de pago no tienen cobro: es simulación o alcance sin definir, no prueba de fraude de pago.

**Cómo reproducir:** Fixture de plan límite 10 y límite 1 (seed actual solo Gratis); crear 8, cambiar a 1 y activar cada pausada por HTTP. Establecer fechaFin pasada manteniendo activo y crear/activar.

**Solución propuesta:** D-04: definir si cupo incluye activas+pausadas (preserva regla actual de creación) o solo activas. Recomendar bloquear downgrade hasta elegir qué conservar, sin borrar habitaciones. Centralizar policy para crear/reactivar/destacar/cambiar plan con reloj y control concurrente.

**Riesgo de la solución:** No pausar ni borrar 7 habitaciones automáticamente. Acordar transición, expiración, permisos de estadísticas y eventual pago antes de implementar.

**Tests necesarios:** PLAN-01 a PLAN-10: 10→1 con 8, pausadas→downgrade→activar, expiración, destaque y carreras.


### RM-17 — P1 — El propietario puede revertir una sanción de habitación

**Clasificación:** CONFIRMADO. **Archivo/método/línea:** [roommatch-api/roommatch-api/src/main/java/com/roommatch/service/ReporteService.java: 193](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-api/roommatch-api/src/main/java/com/roommatch/service/ReporteService.java#L193), [roommatch-api/roommatch-api/src/main/java/com/roommatch/service/ReporteService.java: 208](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-api/roommatch-api/src/main/java/com/roommatch/service/ReporteService.java#L208), [roommatch-api/roommatch-api/src/main/java/com/roommatch/service/HabitacionService.java: 258](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-api/roommatch-api/src/main/java/com/roommatch/service/HabitacionService.java#L258).

**Descripción y evidencia:** Sancionar habitación la pone pausada; activarHabitacion del dueño pone activa sin comprobar reporte sancionado o un bloqueo administrativo separado.

**Impacto:** La medida de moderación no es duradera. Es un bypass confirmado por las transiciones de servicio, aunque no una modificación de habitación ajena.

**Cómo reproducir:** Admin sanciona reporte de habitación de B; B llama PUT /api/habitaciones/{id}/activar; la lógica permite activa. Validar también nuevas publicaciones vinculadas a la habitación.

**Solución propuesta:** Separar estado comercial de bloqueo de moderación; policy efectiva impide visibilidad/reactivación mientras haya sanción vigente. Levantamiento solo admin autorizado con registro de motivo.

**Riesgo de la solución:** Acordar duración/levantamiento y tratamiento de datos existentes. No equiparar pausa voluntaria y sanción.

**Tests necesarios:** MOD-01/02: sanción, reactivación denegada, levantamiento autorizado y no exposición pública.


### RM-18 — P1 — Alta de propietario sobrescribe incluso el rol ADMIN

**Clasificación:** CONFIRMADO. **Archivo/método/línea:** [roommatch-api/roommatch-api/src/main/java/com/roommatch/service/PropietarioService.java: 82](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-api/roommatch-api/src/main/java/com/roommatch/service/PropietarioService.java#L82), [roommatch-api/roommatch-api/src/main/java/com/roommatch/service/PropietarioService.java: 220](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-api/roommatch-api/src/main/java/com/roommatch/service/PropietarioService.java#L220), [roommatch-api/roommatch-api/src/main/java/com/roommatch/controller/PropietarioController.java: 42](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-api/roommatch-api/src/main/java/com/roommatch/controller/PropietarioController.java#L42).

**Descripción y evidencia:** convertirmeEnPropietario puede ejecutarlo cualquier autenticado y asigna PROPIETARIO sin conservar ADMIN. Usuario tiene una sola FK rol. Las funciones normales requieren authenticated, así que un propietario sí puede seguir usándolas.

**Impacto:** Un administrador sin perfil propietario puede perder sus permisos al registrarse como tal. No hay elevación a ADMIN por este endpoint. Modelo de rol único limita futuras combinaciones.

**Cómo reproducir:** Usar fixture ADMIN sin propietario, llamar POST /api/propietarios/me y consultar rol en BD/token siguiente solicitud.

**Solución propuesta:** D-06: mínimo preservar ADMIN y derivar capacidad propietaria del perfil; o rechazar alta a roles no admitidos. Múltiples roles solo si hay necesidad explícita futura; no migración general por anticipación.

**Riesgo de la solución:** Un admin con perfil no debe poder acceder a recursos de otro propietario salvo operación admin explícita. Actualizar guard/contexto y respuesta de sesión.

**Tests necesarios:** ROLE-01/02: ADMIN, USUARIO, PROPIETARIO y accesos normales tras alta.


### RM-19 — P1 — El esquema entregado carece de imagen_publicacion.principal

**Clasificación:** CONFIRMADO. **Archivo/método/línea:** [roommatch-api/roommatch-api/src/main/java/com/roommatch/model/ImagenPublicacion.java: 24](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-api/roommatch-api/src/main/java/com/roommatch/model/ImagenPublicacion.java#L24), [Base de datos_Roommatch.sql: 343](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/Base%20de%20datos_Roommatch.sql#L343), [database/migrations/002_publicacion_roomie_vivienda.sql: 1](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/database/migrations/002_publicacion_roomie_vivienda.sql#L1).

**Descripción y evidencia:** JPA mapea principal como columna no nullable. La tabla del bootstrap solo tiene id_imagen, id_publicacion, url_imagen y orden; la única migración modifica publicacion_roomie, no imagen_publicacion. ddl-auto=none no crea la columna.

**Impacto:** La funcionalidad de imágenes de publicaciones falla al leer/escribir contra una instalación formada por los SQL entregados. contextLoads/H2 crea el esquema JPA y oculta esta diferencia. No se inspeccionó una BD ya modificada manualmente.

**Cómo reproducir:** Construir SQL Server desechable con base+seed+002; ejecutar listado/alta de imágenes de publicación y validar el error de columna. El cotejo estático demuestra ausencia en scripts.

**Solución propuesta:** Migración aditiva con backfill determinista de principal y constraint posterior; alinear baseline y prueba de esquema. Validar datos antes de imponer principal único.

**Riesgo de la solución:** No ejecutar bootstrap destructivo en BD existente; decidir cuál imagen será principal si hay varias.

**Tests necesarios:** DB-01/02 y IMG-01: esquema real, startup validate y CRUD de imágenes.


### RM-20 — P2 — Imágenes: límites y principal no protegidos concurrentemente

**Clasificación:** RIESGO. **Archivo/método/línea:** [roommatch-api/roommatch-api/src/main/java/com/roommatch/service/ImagenHabitacionService.java: 54](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-api/roommatch-api/src/main/java/com/roommatch/service/ImagenHabitacionService.java#L54), [roommatch-api/roommatch-api/src/main/java/com/roommatch/service/ImagenHabitacionService.java: 244](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-api/roommatch-api/src/main/java/com/roommatch/service/ImagenHabitacionService.java#L244), [roommatch-api/roommatch-api/src/main/java/com/roommatch/service/ImagenPublicacionService.java: 117](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-api/roommatch-api/src/main/java/com/roommatch/service/ImagenPublicacionService.java#L117), [roommatch-api/roommatch-api/src/main/java/com/roommatch/repository/ImagenHabitacionRepository.java: 73](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-api/roommatch-api/src/main/java/com/roommatch/repository/ImagenHabitacionRepository.java#L73).

**Descripción y evidencia:** Máximo 5 es count→save sin bloqueo del padre. No hay UNIQUE filtrado principal por habitación/publicación. Borrar principal de publicación no reasigna otra (habitaciones sí lo intenta). Bulk UPDATE desmarca sin limpiar persistence context: marcar una imagen que ya era principal requiere probar estado administrado no refrescado.

**Impacto:** Carreras pueden exceder 5 o dar múltiples principales; publicación puede quedarse sin principal después de borrar la anterior. El caso de bulk update es riesgo pendiente de integración, no fallo ejecutado.

**Cómo reproducir:** Con 4 imágenes enviar 2 altas concurrentes; marcar 2 principales concurrentemente; borrar principal con otra existente. Repetir petición de marcar la misma principal vía API.

**Solución propuesta:** Bloquear/versionar agregado padre para altas/cambios; UNIQUE filtrado sobre padre WHERE principal=1 tras limpieza; operación transaccional que seleccione sucesora al borrar. Definir orden estable sin imponer unicidad si se permiten empates.

**Riesgo de la solución:** El índice garantiza como máximo una, no que siempre exista una. Resolver principal en servicio. Evitar perder cambios pendientes al limpiar EntityManager.

**Tests necesarios:** IMG-02 a IMG-06: límite concurrente, idempotencia, reordenar, borrar y ownership.


### RM-21 — P2 — Validación de URLs solo parcial y orden de imagen desalineado

**Clasificación:** CONFIRMADO. **Archivo/método/línea:** [roommatch-api/roommatch-api/src/main/java/com/roommatch/dto/ImagenHabitacionRequest.java: 9](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-api/roommatch-api/src/main/java/com/roommatch/dto/ImagenHabitacionRequest.java#L9), [roommatch-api/roommatch-api/src/main/java/com/roommatch/dto/ImagenPublicacionRequest.java: 19](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-api/roommatch-api/src/main/java/com/roommatch/dto/ImagenPublicacionRequest.java#L19), [roommatch-web/src/app/shared/components/image-manager-modal/image-manager-modal.ts: 651](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-web/src/app/shared/components/image-manager-modal/image-manager-modal.ts#L651), [roommatch-api/roommatch-api/src/main/java/com/roommatch/dto/ActualizarUsuarioRequest.java: 30](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-api/roommatch-api/src/main/java/com/roommatch/dto/ActualizarUsuarioRequest.java#L30).

**Descripción y evidencia:** Backend limita URL a 255 pero no exige http/https, host o formato; imagen de publicación no valida orden>=1 aunque SQL sí. Frontend de imágenes admite http/https con URL(), control eludible llamando API. Foto de usuario también es solo string acotado.

**Impacto:** URLs inválidas, mixtas HTTP/HTTPS, rastreo externo o enlaces gigantes rechazados inconsistentemente. No hay descarga de servidor: SSRF no aplica al flujo actual. javascript/data no se validan en API; no se demostró ejecución XSS en Angular.

**Cómo reproducir:** Enviar URL no HTTP o texto arbitrario directamente a API y orden 0 en publicación; comparar validación con SQL Server. Probar recurso 404 en UI y revisar placeholder/fallback.

**Solución propuesta:** Validar URL en backend, HTTPS en producción, longitud explícita acordada, sin credenciales embebidas, y orden>=1. Mantener URLs externas inicialmente con aviso y fallback; no agregar S3/Cloudinary sin requerimientos de carga/retención. Cualquier futuro proxy de descarga necesita defensa SSRF propia.

**Riesgo de la solución:** Un límite 255 puede excluir URLs firmadas legítimas; ampliar exige migración y análisis. No hacer fetch síncrono a destinos arbitrarios para validar existencia.

**Tests necesarios:** IMG-07 y SEC-13: protocolos, Unicode/host, max length, 404, data URI y orden.


### RM-22 — P2 — Lecturas públicas no aplican toda la visibilidad del recurso y su autor

**Clasificación:** CONFIRMADO. **Archivo/método/línea:** [roommatch-api/roommatch-api/src/main/java/com/roommatch/service/ImagenHabitacionService.java: 198](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-api/roommatch-api/src/main/java/com/roommatch/service/ImagenHabitacionService.java#L198), [roommatch-api/roommatch-api/src/main/java/com/roommatch/service/ImagenPublicacionService.java: 77](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-api/roommatch-api/src/main/java/com/roommatch/service/ImagenPublicacionService.java#L77), [roommatch-api/roommatch-api/src/main/java/com/roommatch/repository/PublicacionRoomieRepository.java: 28](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-api/roommatch-api/src/main/java/com/roommatch/repository/PublicacionRoomieRepository.java#L28), [roommatch-api/roommatch-api/src/main/java/com/roommatch/repository/HabitacionBusquedaRepository.java: 39](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-api/roommatch-api/src/main/java/com/roommatch/repository/HabitacionBusquedaRepository.java#L39), [roommatch-api/roommatch-api/src/main/java/com/roommatch/dto/PublicacionRoomieResponse.java: 286](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-api/roommatch-api/src/main/java/com/roommatch/dto/PublicacionRoomieResponse.java#L286).

**Descripción y evidencia:** Imágenes de habitación solo exigen existencia; imágenes de publicación ni comprueban estado del padre. Catálogos filtran estado de habitación/publicación, sin estado del usuario/propietario. Vínculo en DTO publica datos de habitación aun si se pausó después.

**Impacto:** Contenido retirado o de autores suspendidos puede seguir accesible. Los DTO públicos de habitaciones/publicaciones no incluyen teléfonos ni email de acceso: esa parte de la sospecha se refuta.

**Cómo reproducir:** Crear recursos, pausarlos/suspender autor y consultar listas de imágenes, catálogo y publicación vinculada sin autenticación.

**Solución propuesta:** Policy única de visibilidad pública que combine estado comercial, moderación, autor y vínculo. Endpoint privado para propietario permite gestionar sus imágenes sin abrir la lectura pública. Evitar romper edición de pausadas.

**Riesgo de la solución:** Distinguir datos legítimamente públicos (dirección referencial) de información privada y acordar semántica de suspensión del propietario.

**Tests necesarios:** PUB-02/03, MOD-03 y OWN-06: matriz activo/pausado/eliminado/sancionado y autor suspendido.


### RM-23 — P1 — API URL fija a localhost en tres lugares

**Clasificación:** CONFIRMADO. **Archivo/método/línea:** [roommatch-web/src/app/core/config/api.config.ts: 1](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-web/src/app/core/config/api.config.ts#L1), [roommatch-web/src/app/core/services/habitacion.service.ts: 69](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-web/src/app/core/services/habitacion.service.ts#L69), [roommatch-web/src/app/core/services/publicacion-roomie.service.ts: 38](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-web/src/app/core/services/publicacion-roomie.service.ts#L38).

**Descripción y evidencia:** No hay entornos/fileReplacements/runtime config para API. Dos servicios replican http://localhost:8081/api aparte de API_BASE_URL; el interceptor solo reconoce prefijo del valor central.

**Impacto:** Build de producción apunta al computador del visitante. Cambiar solo api.config dejaría habitaciones/publicaciones desalineados y podría impedir adjuntar su token.

**Cómo reproducir:** Inspeccionar dist y las tres declaraciones después de npm run build; alojar la SPA en otro host y observar las URLs de red.

**Solución propuesta:** Una fuente de configuración con development localhost y production origen/API HTTPS explícito o /api bajo proxy; inyectar URL en todos los servicios e interceptor. Documentar CORS y hosting.

**Riesgo de la solución:** Deben concordar URL base, barra final y coincidencia exacta del interceptor. No introducir secretos en configuración de Angular.

**Tests necesarios:** WEB-01/02: entorno dev/prod, endpoints de todos los servicios y token solo al host permitido.


### RM-24 — P1 — Administración visible es placeholder y datos ficticios

**Clasificación:** CONFIRMADO. **Archivo/método/línea:** [roommatch-web/src/app/pages/admin/dashboard/dashboard.html: 1](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-web/src/app/pages/admin/dashboard/dashboard.html#L1), [roommatch-web/src/app/pages/admin/reportes/reportes.html: 51](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-web/src/app/pages/admin/reportes/reportes.html#L51), [roommatch-web/src/app/pages/admin/reportes/reportes.ts: 1](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-web/src/app/pages/admin/reportes/reportes.ts#L1), [roommatch-api/roommatch-api/src/main/java/com/roommatch/controller/ReporteController.java: 79](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-api/roommatch-api/src/main/java/com/roommatch/controller/ReporteController.java#L79).

**Descripción y evidencia:** Dashboard muestra dashboard works!. Reportes contiene nombres/fechas fijos y botones sin handlers. No hay servicios TS ni DTO administrativos. Backend ofrece dashboard, listas de reportes y revisar/sancionar. Report-modal compartido también está vacío; no hay UI integrada para crear reportes.

**Impacto:** Administración no funciona de extremo a extremo y presenta información inventada. La capacidad real del backend no está disponible al administrador desde la web.

**Cómo reproducir:** Inspeccionar templates/TS; entrar con fixture ADMIN cuando se pueda ejecutar y comprobar que no se emiten peticiones de administración ni acciones.

**Solución propuesta:** Implementación posterior en Sprint 3: servicios tipados, métricas reales, pestañas usuario/habitación, filtros de servidor, acciones y confirmaciones; loading/error/empty/success. Integrar creación de reporte donde corresponda, conservar estilo.

**Riesgo de la solución:** Primero fijar moderación, permisos y estados; una UI sobre un backend con sanciones reversibles seguiría siendo defectuosa.

**Tests necesarios:** ADMIN-01 a ADMIN-05: autorización, datos reales, filtros, errores, acciones, confirmaciones y ausencia de mocks.


### RM-25 — P2 — Detalle público de publicación requiere usuario autenticado en controller

**Clasificación:** CONFIRMADO. **Archivo/método/línea:** [roommatch-api/roommatch-api/src/main/java/com/roommatch/controller/PublicacionRoomieController.java: 227](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-api/roommatch-api/src/main/java/com/roommatch/controller/PublicacionRoomieController.java#L227), [roommatch-api/roommatch-api/src/main/java/com/roommatch/controller/PublicacionRoomieController.java: 240](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-api/roommatch-api/src/main/java/com/roommatch/controller/PublicacionRoomieController.java#L240), [roommatch-api/roommatch-api/src/main/java/com/roommatch/controller/PublicacionRoomieController.java: 616](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-api/roommatch-api/src/main/java/com/roommatch/controller/PublicacionRoomieController.java#L616), [roommatch-web/src/app/app.routes.ts: 34](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-web/src/app/app.routes.ts#L34).

**Descripción y evidencia:** SecurityConfig permite GET /api/publicaciones-roomie/** y Angular expone detalle público. obtenerPublicacion llama obtenerIdUsuarioAutenticado, que lanza IllegalArgumentException para visitante; la lista sí permite usuario opcional.

**Impacto:** Visitantes pueden listar pero el detalle falla con 400. No es problema de JWT del visitante sino contradicción en el contrato público.

**Cómo reproducir:** Sin Authorization consultar detalle de una publicación activa; la primera rama de controller rechaza ausencia de usuario antes de llamar servicio.

**Solución propuesta:** Pasar usuario opcional al detalle público y calcular compatibilidad solo autenticado; mantener escrituras autenticadas. Acción enviar solicitud debe conducir a login con retorno al detalle.

**Riesgo de la solución:** No hacer públicas otras operaciones por reutilizar helper; pruebas específicas para cada verbo.

**Tests necesarios:** PUB-01 y WEB-03: detalle anónimo, propio, ajeno, inexistente e inactivo.


### RM-26 — P2 — La UI no consume las imágenes públicas registradas

**Clasificación:** CONFIRMADO. **Archivo/método/línea:** [roommatch-web/src/app/pages/habitaciones/habitaciones.html: 303](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-web/src/app/pages/habitaciones/habitaciones.html#L303), [roommatch-web/src/app/pages/habitacion-detalle/habitacion-detalle.html: 152](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-web/src/app/pages/habitacion-detalle/habitacion-detalle.html#L152), [roommatch-web/src/app/shared/components/image-manager-modal/image-manager-modal.ts: 1](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-web/src/app/shared/components/image-manager-modal/image-manager-modal.ts#L1).

**Descripción y evidencia:** Catálogo y detalle de habitaciones contienen placeholders gráficos; el template incluso comenta que imagen_habitacion se conectará después. ImagenHabitacionService se usa en gestión del propietario, no en esos catálogos. Imágenes de publicaciones tampoco tienen servicio/UI de gestión.

**Impacto:** El propietario registra imágenes que el buscador no puede ver. Flujo de imagen de publicaciones está incompleto además de la divergencia SQL.

**Cómo reproducir:** Registrar imagen de habitación como propietario y revisar código de catálogo/detalle: no hay binding ni consulta que la obtenga.

**Solución propuesta:** Incluir principal en proyección de catálogo y galería en detalle, con alt, carga diferida y fallback; integrar imágenes de publicaciones después de corregir esquema. Reutilizar gestor sin acoplarlo a dos endpoints incoherentes.

**Riesgo de la solución:** Evitar convertir cada tarjeta en una llamada adicional; controlar rastreo/HTTPS y tamaño visual.

**Tests necesarios:** WEB-04 e IMG-08: principal visible, galería, 404 y ausencia de N+1 HTTP en catálogo.


### RM-27 — P2 — Filtros y métricas de frontend se aplican a subconjuntos incompletos

**Clasificación:** CONFIRMADO. **Archivo/método/línea:** [roommatch-web/src/app/pages/publicaciones-roomie/publicaciones-list/publicaciones-list.ts: 1](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-web/src/app/pages/publicaciones-roomie/publicaciones-list/publicaciones-list.ts#L1), [roommatch-web/src/app/pages/notificaciones/notificaciones.ts: 1](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-web/src/app/pages/notificaciones/notificaciones.ts#L1), [roommatch-web/src/app/pages/propietario/propietario-panel/propietario-panel.ts: 1](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-web/src/app/pages/propietario/propietario-panel/propietario-panel.ts#L1), [roommatch-web/src/app/pages/propietario/leads/interesados.ts: 138](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-web/src/app/pages/propietario/leads/interesados.ts#L138).

**Descripción y evidencia:** Compatibilidad/orden se filtran localmente tras paginar 6 publicaciones. No-leídas se filtra en página 10 aunque API acepta leido y dispone de count. MisPublicaciones toma 50, MisHabitaciones 100, panel calcula métricas con primeras 100 y leads pide 1000.

**Impacto:** Puede mostrarse vacío aunque existan resultados en otra página; métricas y ranking no representan todo el conjunto. Botón leer-todas puede quedar deshabilitado si esta página no tiene no-leídas.

**Cómo reproducir:** Fixture con resultados coincidentes solo en página 2; 101 habitaciones/leads o 51 publicaciones propias; notificaciones no leídas fuera de página 1.

**Solución propuesta:** Llevar filtros/orden que afectan universo al servidor; paginación real, count/agregados de servidor y orden estable por fecha+id. No resolverlo subiendo size a un número grande.

**Riesgo de la solución:** Orden por compatibilidad necesita estrategia coherente de ranking/versiones del matching. No prometer conteos globales usando arreglos parciales.

**Tests necesarios:** WEB-05/06: páginas 2+, filtrosglobales, totalElements y métricas con más del límite.


### RM-28 — P2 — Errores silenciados y enlaces funcionalmente muertos

**Clasificación:** CONFIRMADO. **Archivo/método/línea:** [roommatch-web/src/app/pages/home/home.ts: 1](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-web/src/app/pages/home/home.ts#L1), [roommatch-web/src/app/pages/contactos/contactos.ts: 234](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-web/src/app/pages/contactos/contactos.ts#L234), [roommatch-web/src/app/pages/auth/login/login.html: 56](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-web/src/app/pages/auth/login/login.html#L56), [roommatch-api/roommatch-api/src/main/java/com/roommatch/service/LeadHabitacionService.java: 156](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-api/roommatch-api/src/main/java/com/roommatch/service/LeadHabitacionService.java#L156), [roommatch-api/roommatch-api/src/main/java/com/roommatch/service/ReporteService.java: 183](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-api/roommatch-api/src/main/java/com/roommatch/service/ReporteService.java#L183).

**Descripción y evidencia:** Home convierte varias fallas en listas vacías/perfil faltante. Contactos convierte errores individuales en contacto no registrado. Recuperar contraseña enlaza a /login. Notificaciones generan /mis-intereses y /soporte, rutas inexistentes. Intereses reales están en /solicitudes.

**Impacto:** Errores de red/autorización parecen ausencia de datos y enlaces devuelven al inicio o misma pantalla. No existe recuperación de contraseña implementada.

**Cómo reproducir:** Simular 503 en carga Home/contactos; revisar enlaces y wildcard de rutas. No confundir solicitudes con loading roto: su contador sí espera ambas cargas.

**Solución propuesta:** Estado explícito por recurso con retry y mensajes recuperables; distinguir 404/null de 503/403. Corregir destinos a pantallas existentes. D-09 define recuperación; mientras tanto no presentar un enlace que simula completarla.

**Riesgo de la solución:** Mantener datos parciales exitosos y anunciar fallos sin vaciar toda pantalla. No añadir soporte/email automático sin alcance.

**Tests necesarios:** WEB-07/08: fallos parciales, retry, navegación notificaciones y recuperación según decisión.


### RM-29 — P2 — Accesibilidad incompleta en idioma, formularios y modales

**Clasificación:** CONFIRMADO. **Archivo/método/línea:** [roommatch-web/src/index.html: 2](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-web/src/index.html#L2), [roommatch-web/src/app/pages/propietario/mis-habitaciones/mis-habitaciones.html: 383](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-web/src/app/pages/propietario/mis-habitaciones/mis-habitaciones.html#L383), [roommatch-web/src/app/pages/publicaciones-roomie/publicaciones-list/publicaciones-list.html: 105](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-web/src/app/pages/publicaciones-roomie/publicaciones-list/publicaciones-list.html#L105), [roommatch-web/src/app/shared/components/image-manager-modal/image-manager-modal.html: 7](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-web/src/app/shared/components/image-manager-modal/image-manager-modal.html#L7).

**Descripción y evidencia:** Documento declara lang=en con contenido español. Varios labels son hermanos de inputs sin for/id (título de habitación, filtros). Modales propios no declaran dialog/aria-modal, foco inicial, trap, Escape ni restauración de foco. Solo se encontró aria-label de cerrar en gestor de imágenes; faltan anuncios de estado.

**Impacto:** Barreras comprobables en código respecto de WCAG 2.1: idioma, relación label/campo y semántica. Contraste, orden real de foco y responsive no quedaron medidos: navegador no disponible y descarga de Chromium agotó timeout.

**Cómo reproducir:** Revisión de esos templates; después teclado y lector de pantalla en viewport 390/1440, zoom 200/400%, apertura/cierre de cada modal y anuncios de errores.

**Solución propuesta:** lang=es, labels asociados, mensajes aria-describedby/live según tipo, modal accesible reutilizable, foco visible y navegación semántica. Medir contraste de pares reales renderizados antes de corregir colores.

**Riesgo de la solución:** No contar checkboxes envueltos en label como unlabeled. No quitar outline sin reemplazo visible ni cambiar paleta sin comprobar contraste.

**Tests necesarios:** A11Y-01 a A11Y-06: axe más inspección manual/teclado; no equivaler axe verde a certificación AA.


### RM-30 — P2 — Actualización de descripción reenvía perfil completo y puede perder cambios

**Clasificación:** CONFIRMADO. **Archivo/método/línea:** [roommatch-web/src/app/pages/mi-cuenta/mi-cuenta.ts: 653](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-web/src/app/pages/mi-cuenta/mi-cuenta.ts#L653), [roommatch-api/roommatch-api/src/main/java/com/roommatch/service/PerfilConvivenciaService.java: 102](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-api/roommatch-api/src/main/java/com/roommatch/service/PerfilConvivenciaService.java#L102).

**Descripción y evidencia:** guardarDescripcion hace GET perfil y dentro de esa respuesta PUT de todo el perfil con descripción modificada. El servidor copia todos los campos sin versionado. Otras páginas contienen validación/formateo/errores repetidos y grandes responsabilidades.

**Impacto:** Cambio concurrente entre GET y PUT puede sobrescribirse; dos peticiones para un campo. Complejidad dificulta probar reglas. La mayoría de HttpClient subscribe completa: no se afirma fuga de memoria por cada subscribe.

**Cómo reproducir:** Editar presupuesto desde otra sesión entre GET y PUT de descripción; comprobar que vuelve al valor leído. Contar peticiones de guardarDescripcion.

**Solución propuesta:** PATCH de descripción o comando específico con DTO pequeño; @Version/If-Match para edición completa. Extraer formularios/componentes por responsabilidad y helpers de presentación; RxJS para coordinar peticiones y cancelar búsquedas obsoletas.

**Riesgo de la solución:** Mantener semántica de null/no enviado en PATCH; no cambiar todos los endpoints ni convertir formularios sin necesidad.

**Tests necesarios:** WEB-10 y PERFIL-02: PATCH no toca hábitos, concurrencia 409 y guardado independiente.


### RM-31 — P3 — CSS global y componentes duplicados de presentación

**Clasificación:** CONFIRMADO. **Archivo/método/línea:** [roommatch-web/src/styles.css: 1](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-web/src/styles.css#L1), [roommatch-web/src/app/app.css: 1](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-web/src/app/app.css#L1), [roommatch-web/src/app/pages/publicaciones-roomie/mis-publicaciones/mis-publicaciones.css: 1](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-web/src/app/pages/publicaciones-roomie/mis-publicaciones/mis-publicaciones.css#L1).

**Descripción y evidencia:** styles.css: 8.485 líneas físicas, 5.775 no vacías y 123.878 bytes, mezcla navbar/home/auth/perfil/matches/contactos. app.css tiene 235 líneas con navbar similar. Hay selectores repetidos; parte corresponde a media queries, no toda repetición es redundancia. CSS de mis-publicaciones supera presupuesto de warning.

**Impacto:** Coste de mantenimiento y riesgo de cascada global; no se ha demostrado regresión visual ni que cada selector repetido sea eliminable.

**Cómo reproducir:** Inspeccionar responsables de reglas y warning de build: 14,06 kB frente a 12 kB de componente. Comparar visualmente antes/después cuando haya navegador.

**Solución propuesta:** Global para reset/tokens/tipografía/utilidades; componentes compartidos para navbar/modales; feature/page para reglas exclusivas. Consolidar reglas solo con inspección de cascada y capturas comparables, preservando apariencia.

**Riesgo de la solución:** Mover reglas a encapsulación cambia alcance/especificidad. No subir presupuesto para ocultar crecimiento.

**Tests necesarios:** WEB-11: snapshots visuales representativos móvil/escritorio antes/después; build sin aumentar bundle.


### RM-32 — P3 — Siete componentes placeholder y código no utilizado

**Clasificación:** CONFIRMADO. **Archivo/método/línea:** [roommatch-web/src/app/shared/components/room-card/room-card.ts: 1](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-web/src/app/shared/components/room-card/room-card.ts#L1), [roommatch-web/src/app/shared/components/report-modal/report-modal.html: 1](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-web/src/app/shared/components/report-modal/report-modal.html#L1), [roommatch-web/src/app/core/models/ead-habitacion-response.ts: 1](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-web/src/app/core/models/ead-habitacion-response.ts#L1), [roommatch-api/roommatch-api/src/main/java/com/roommatch/util/PaginationUtil.java: 6](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-api/roommatch-api/src/main/java/com/roommatch/util/PaginationUtil.java#L6).

**Descripción y evidencia:** room-card, profile-card, pagination, notification-card, compatibility-badge, empty-state y report-modal tienen clase vacía/template works y no tienen usos de selector. ead-habitacion-response es una copia desactualizada sin imports: mensaje no admite null, mientras el archivo lead correcto sí. PaginationUtil no tiene llamadas. Queries alternativas buscarHabitaciones, buscarEntreUsuarios y listarContactosDesbloqueados no son consumidas; starter-mail no tiene servicio de envío.

**Impacto:** Código muerto y falsas señales de funcionalidad reutilizable. No implica aumento de bundle de todo lo no importado: tree shaking puede excluirlo.

**Cómo reproducir:** Buscar imports, selectores y llamadas en todo el árbol versionado; comparar ambos modelos de lead byte a byte.

**Solución propuesta:** Implementar y reutilizar empty-state/pagination/compatibility-badge/report-modal donde hay necesidad; cards solo tras definir contrato compartido. notification-card puede eliminarse si una sola vista no gana reutilización. Quitar typo confirmado y revisar dependencias sin uso en commit separado.

**Riesgo de la solución:** No eliminar un flujo por estar su componente compartido vacío; las páginas actuales implementan su propia UI. Ausencia de llamada hoy no obliga a eliminar query útil para próximo batch.

**Tests necesarios:** Build y búsquedas de referencias; pruebas de comportamiento del componente nuevo, no tests de archivos vacíos.


### RM-33 — P2 — Desalineación de contratos, precisión y nulabilidad

**Clasificación:** CONFIRMADO. **Archivo/método/línea:** [roommatch-api/roommatch-api/src/main/java/com/roommatch/model/Habitacion.java: 1](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-api/roommatch-api/src/main/java/com/roommatch/model/Habitacion.java#L1), [roommatch-api/roommatch-api/src/main/java/com/roommatch/model/MatchResultado.java: 1](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-api/roommatch-api/src/main/java/com/roommatch/model/MatchResultado.java#L1), [roommatch-api/roommatch-api/src/main/java/com/roommatch/model/PublicacionRoomie.java: 38](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-api/roommatch-api/src/main/java/com/roommatch/model/PublicacionRoomie.java#L38), [roommatch-api/roommatch-api/src/main/java/com/roommatch/dto/ContactoUsuarioRequest.java: 20](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-api/roommatch-api/src/main/java/com/roommatch/dto/ContactoUsuarioRequest.java#L20), [roommatch-web/src/app/core/models/api-response.ts: 1](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-web/src/app/core/models/api-response.ts#L1).

**Descripción y evidencia:** Se cotejaron 168 campos JPA contra 167 columnas SQL efectivas. Además de principal ausente: tamaños de tipo/estado de publicación difieren; 3 fechas tienen nullable por defecto JPA frente a NOT NULL SQL. Seis BigDecimal carecen de precision/scale; requests carecen de @Digits. emailContacto no limita 150. TS ApiResponse.data supone no null; mensaje de solicitud tipado string aunque SQL permite null (lead canónico sí admite null); edad y fecha/descripcion de perfil tienen reglas cliente/servidor distintas.

**Impacto:** H2 puede validar un esquema distinto; errores de truncamiento/overflow llegan tarde o como conflicto. Las fechas se rellenan por callbacks, por lo que no se afirma que actualmente se inserten nulas. VARCHAR no garantiza repertorio Unicode sin conocer collation.

**Cómo reproducir:** Matriz campo por campo en arquitectura; payloads de longitud/precisión extrema, contacto email>150, nulls y emojis contra SQL Server con collation real.

**Solución propuesta:** Alinear anotaciones, DTOs y migraciones; validación de precisión de dinero; nulabilidad TS real y envelope discriminado. Definir catálogos cerrados de hábitos y estados (actualmente strings libres hasta 50); acordar Unicode/collation.

**Riesgo de la solución:** Endurecer constraints puede rechazar datos existentes: perfilar/normalizar antes de aplicar. No cambiar null a 0 o cadena vacía para pasar validación.

**Tests necesarios:** DB-03 a DB-06 y DTO-01/02: límites, decimales, nulls, estados y contrato JSON.


### RM-34 — P1 — Bootstrap destructivo y ausencia de historia de migraciones aplicada

**Clasificación:** RIESGO. **Archivo/método/línea:** [Base de datos_Roommatch.sql: 1](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/Base%20de%20datos_Roommatch.sql#L1), [database/migrations/002_publicacion_roomie_vivienda.sql: 1](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/database/migrations/002_publicacion_roomie_vivienda.sql#L1), [database/seed_required_data.sql: 1](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/database/seed_required_data.sql#L1), [roommatch-api/roommatch-api/pom.xml: 25](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-api/roommatch-api/pom.xml#L25).

**Descripción y evidencia:** El script base DROP/CREATE roommatch_db, migración manual 002 y seed independiente. No hay Flyway ni registro de checksums/versiones. README sí advierte que el base recrea la BD: no se afirma que se ejecute automáticamente en producción.

**Impacto:** Riesgo alto de upgrade manual incoherente o destrucción si se usa el bootstrap para actualizar. La diferencia de imagen_publicacion muestra drift comprobable.

**Cómo reproducir:** Revisar los tres scripts, no ejecutar el base sobre datos existentes; comparar una instalación desechable con JPA.

**Solución propuesta:** Evaluar Flyway con SQL Server: V1 de esquema no destructivo, V2 vivienda normalizada, V3 corrección de imagen tras reconciliar. Baseline explícito de BD existente solo a versión cuyo esquema se haya verificado. Migraciones append-only, backups y forward fixes; bootstrap local separado.

**Riesgo de la solución:** No activar baselineOnMigrate indiscriminadamente ni marcar una BD como V3 si faltan columnas. Rollback de datos requiere estrategia real de restore/compatibilidad, no eliminar tabla history.

**Tests necesarios:** DB-01/07: fresh install y upgrade con datos preexistentes, checksum, reejecución y restore.


### RM-35 — P1 — Invariantes de escritura protegidas de forma incompleta ante concurrencia

**Clasificación:** RIESGO. **Archivo/método/línea:** [roommatch-api/roommatch-api/src/main/java/com/roommatch/service/PlanPropietarioService.java: 248](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-api/roommatch-api/src/main/java/com/roommatch/service/PlanPropietarioService.java#L248), [roommatch-api/roommatch-api/src/main/java/com/roommatch/service/HabitacionService.java: 69](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-api/roommatch-api/src/main/java/com/roommatch/service/HabitacionService.java#L69), [roommatch-api/roommatch-api/src/main/java/com/roommatch/service/ContactoUsuarioService.java: 1](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-api/roommatch-api/src/main/java/com/roommatch/service/ContactoUsuarioService.java#L1), [Base de datos_Roommatch.sql: 188](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/Base%20de%20datos_Roommatch.sql#L188).

**Descripción y evidencia:** Existe exists/count→save en email, favoritos, perfil, propietario, contacto, leads, planes y cuota. SQL sí protege email, pares de favorito/lead y usuario único de perfil/contacto/propietario; no limita una suscripción activa ni el cupo bajo dos altas concurrentes. No hay @Version en entidades.

**Impacto:** En los casos con UNIQUE, duplicados se impiden pero las respuestas pueden variar 400/409; en suscripciones/cupo se permite estado inconsistente. No confundir ausencia de UNIQUE JPA/H2 con ausencia en SQL real.

**Cómo reproducir:** Dos peticiones simultáneas de alta con una plaza libre; dos cambios de plan; dos primeros guardados del mismo contacto. Verificar una fila ganadora y status de la otra.

**Solución propuesta:** Bloqueo/versionado de agregado propietario para cuota/plan; índice único filtrado de suscripción activa después de sanear. Upsert/CAS/recuperación de conflicto donde convenga, manteniendo UNIQUE existentes como última defensa.

**Riesgo de la solución:** Order de locks consistente para evitar deadlocks; no serializar todas las escrituras del sistema. Bulk save no resuelve estas carreras.

**Tests necesarios:** CON-01 a CON-06: barreras multihilo en SQL Server para cada invariante crítica.


### RM-36 — P2 — Paginación sin límite uniforme y consultas no indexables

**Clasificación:** RIESGO. **Archivo/método/línea:** [roommatch-api/roommatch-api/src/main/java/com/roommatch/util/PaginationUtil.java: 6](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-api/roommatch-api/src/main/java/com/roommatch/util/PaginationUtil.java#L6), [roommatch-api/roommatch-api/src/main/java/com/roommatch/controller/PublicacionRoomieController.java: 129](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-api/roommatch-api/src/main/java/com/roommatch/controller/PublicacionRoomieController.java#L129), [roommatch-api/roommatch-api/src/main/java/com/roommatch/controller/LeadHabitacionController.java: 73](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-api/roommatch-api/src/main/java/com/roommatch/controller/LeadHabitacionController.java#L73), [roommatch-api/roommatch-api/src/main/java/com/roommatch/repository/HabitacionBusquedaRepository.java: 43](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-api/roommatch-api/src/main/java/com/roommatch/repository/HabitacionBusquedaRepository.java#L43), [Base de datos_Roommatch.sql: 574](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/Base%20de%20datos_Roommatch.sql#L574).

**Descripción y evidencia:** Habitaciones valida page>=0 y size 1..100. Publicaciones/matches/notificaciones/leads/reportes no aplican máximo uniforme; listas de favoritos/solicitudes/perfiles son completas. LIKE %texto% y LOWER impiden seeks eficientes sobre primer campo de índices actuales. Varias ordenaciones no tienen desempate por id.

**Impacto:** Riesgo de alto consumo y páginas inestables; no hay evidencia de SQL injection: parámetros están enlazados y sort es fijo. No se midieron planes de ejecución.

**Cómo reproducir:** Enviar size grande/negativo en fixtures y comparar endpoints; ejecutar consultas reales con STATISTICS IO/TIME y actual plans antes de proponer índices definitivos.

**Solución propuesta:** Límites consistentes, listas paginadas y orden estable; índices ligados a queries en sección performance. Si negocio admite distrito canónico exacto, indexarlo; mantener búsqueda contiene como filtro consciente o evaluar búsqueda textual solo con evidencia.

**Riesgo de la solución:** Cambiar contiene por igualdad altera búsqueda; esperar D-08. Índices extras cuestan escritura/espacio. No añadir todos los candidatos sin medir.

**Tests necesarios:** PERF-04/05 y HTTP-04: límites, determinismo, planes/lecturas lógicas con datos representativos.


### RM-37 — P2 — Moderación sin máquina de estados ni historial de decisiones

**Clasificación:** CONFIRMADO. **Archivo/método/línea:** [roommatch-api/roommatch-api/src/main/java/com/roommatch/service/ReporteService.java: 122](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-api/roommatch-api/src/main/java/com/roommatch/service/ReporteService.java#L122), [roommatch-api/roommatch-api/src/main/java/com/roommatch/service/ReporteService.java: 162](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-api/roommatch-api/src/main/java/com/roommatch/service/ReporteService.java#L162), [Base de datos_Roommatch.sql: 481](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/Base%20de%20datos_Roommatch.sql#L481), [Base de datos_Roommatch.sql: 512](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/Base%20de%20datos_Roommatch.sql#L512).

**Descripción y evidencia:** Revisar/rechazar puede cambiar estado de reporte previamente sancionado sin revertir la suspensión; no se registra idAdmin ni motivo/acción histórica, solo fechaRevision. Sancionar no comprueba transición anterior. Reportes nuevos carecen de control de duplicados/abuso.

**Impacto:** Estado del reporte y sanción real pueden divergir; difícil saber quién actuó o reconstruir decisiones. Sin UI admin real esta deuda aún no fue cubierta por flujo de usuario.

**Cómo reproducir:** Sancionar y luego revisar como rechazado el mismo reporte; comprobar usuario/habitación y columnas de auditoría disponibles.

**Solución propuesta:** D-07: definir transiciones, efectos y levantamiento; CAS, registro de acción con actor/motivo/fecha y política de duplicados. Evitar sancionar repetidamente por reintentos.

**Riesgo de la solución:** Auditoría de moderación requiere retención y acceso restringido. No inventar sanciones automáticas ni eliminar reportes históricos.

**Tests necesarios:** MOD-04 a MOD-06: estados previos, idempotencia, dos revisores y audit trail.


### RM-38 — P1 — Tests frontend fallan y CI no los ejecuta

**Clasificación:** CONFIRMADO. **Archivo/método/línea:** [roommatch-web/src/app/app.spec.ts: 1](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-web/src/app/app.spec.ts#L1), [.github/workflows/ci.yml: 51](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/.github/workflows/ci.yml#L51), [roommatch-web/package.json: 9](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-web/package.json#L9).

**Descripción y evidencia:** npm test -- --watch=false ejecutó 2 pruebas y ambas fallaron NG0201 No provider found for ActivatedRoute. TestBed importa App sin router; el segundo test además espera Hello, roommatch-web, texto ajeno al template real. CI solo npm ci y build.

**Impacto:** CI verde oculta una suite rota. No hay pruebas de servicios, guards, interceptor ni páginas críticas.

**Cómo reproducir:** Ejecutar exactamente npm test -- --watch=false desde roommatch-web; logs muestran 2 failed, 0 skipped. Revisar pasos frontend del workflow.

**Solución propuesta:** Reparar setup/assertions con expectativas reales; añadir pruebas priorizadas de auth/contratos/estados y paso obligatorio de tests en CI. No eliminar tests ni usar passWithNoTests/continue-on-error para verde.

**Riesgo de la solución:** La corrección del setup no demuestra cobertura de negocio. Mantener tests de regresión ligados a hallazgos.

**Tests necesarios:** WEB-01 a WEB-11 y pipeline con fallo inducido controlado en rama para demostrar gate.


### RM-39 — P1 — Única prueba backend no protege seguridad, reglas ni SQL real

**Clasificación:** CONFIRMADO. **Archivo/método/línea:** [roommatch-api/roommatch-api/src/test/java/com/roommatch/RoommatchApiApplicationTests.java: 1](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-api/roommatch-api/src/test/java/com/roommatch/RoommatchApiApplicationTests.java#L1), [roommatch-api/roommatch-api/src/test/resources/application-test.properties: 1](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-api/roommatch-api/src/test/resources/application-test.properties#L1), [.github/workflows/ci.yml: 32](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/.github/workflows/ci.yml#L32).

**Descripción y evidencia:** Solo contextLoads; último CI del SHA auditado pasa 1 test, 0 fail, 0 skip. H2 MODE=MSSQL Server usa H2Dialect y create-drop; no ejecuta los SQL reales. No hay JaCoCo, Failsafe, integración SQL Server ni suite de seguridad.

**Impacto:** Compilar/crear contexto no detecta fuga de email, cuotas, carreras, schema drift ni admin inexistente. Cobertura porcentual desconocida, no 0% inventado.

**Cómo reproducir:** Inventariar src/test y examinar log del job backend. Localmente Maven quedó bloqueado por resolución de dependencias: no prueba de fallo de compilación del proyecto.

**Solución propuesta:** JUnit puro para matching/policies, Mockito para orquestación, MockMvc para contratos/seguridad; SQL Server Testcontainers solo para esquema/constraints/transacciones/queries críticas. Coverage de ramas críticas como señal y mapa de casos, no 100% artificial.

**Riesgo de la solución:** Containers requieren Docker y licencia SQL Server; fijar imagen/versiones y memoria del runner. No sustituir todos los unitarios por contenedores.

**Tests necesarios:** Plan completo ROOMMATCH_TEST_PLAN.md, gates unitarios en cada PR y SQL Server en cambios críticos.


### RM-40 — P1 — npm audit identifica paquetes afectados; Maven no pudo completar análisis

**Clasificación:** RIESGO. **Archivo/método/línea:** [roommatch-web/package-lock.json: 1](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-web/package-lock.json#L1), [roommatch-api/roommatch-api/pom.xml: 10](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-api/roommatch-api/pom.xml#L10).

**Descripción y evidencia:** npm audit reportó 23 paquetes: 11 high, 11 moderate, 1 low, 0 critical. Incluye Angular 21.2.17 y dependencias de tooling. Es inventario de advisories por versión, no 23 explotaciones demostradas. dependency: tree Maven falló antes de resolver parent 3.5.16 por DNS.

**Impacto:** Riesgo en runtime y supply chain pendiente de triaje de alcanzabilidad; no se puede declarar backend libre de vulnerabilidades. Un paquete solo de build no tiene el mismo escenario que uno enviado al navegador.

**Cómo reproducir:** npm audit --json con lock actual; reproducir advisories bajo sus precondiciones en pruebas aisladas. Completar dependency tree/SBOM y escaneo Maven cuando haya repositorio accesible.

**Solución propuesta:** Priorizar patches compatibles de Angular y tooling afectados, validar changelogs y retestar; no audit fix --force ni cambios mayores automáticos. Separar runtime/dev y excepciones temporales justificadas por advisory.

**Riesgo de la solución:** Actualizaciones incluso patch requieren revisar API/build y lock completo. No mezclar Java 25 como supuesto arreglo de seguridad sin necesidad.

**Tests necesarios:** DEP-01/02: inventario reproducible, scan antes/después y regresión build/test/security.


### RM-41 — P2 — CI incompleto y rama principal sin protección

**Clasificación:** CONFIRMADO. **Archivo/método/línea:** [.github/workflows/ci.yml: 1](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/.github/workflows/ci.yml#L1).

**Descripción y evidencia:** GitHub informa main protected=false. Workflow backend clean test, no verify; frontend omite tests. No gates de cobertura/lint/audit ni reporte de pruebas. Push solo main/fix/**; PR a main sí dispara. Los 2 PR abiertos están dirty y GitHub no los considera mergeables.

**Impacto:** Pueden entrar regresiones con pipeline verde o por push directo. La protección remota y cambios de configuración requieren autorización explícita y no se tocaron.

**Cómo reproducir:** Consultar metadata de rama, workflow y jobs del run 34050396262. Ver sección PR para comparación exacta; no inferir vigencia por título.

**Solución propuesta:** PR obligatorio y checks backend verify/frontend build+test requeridos; mínimo privilegio contents: read, concurrency cancel-in-progress, timeouts y artefactos de tests. Auditoría/lint progresivos con excepciones explícitas y vigencia; proponer protección, no aplicarla sin permiso.

**Riesgo de la solución:** Checks requeridos deben coincidir con nombres reales y ejecutarse en todo PR para no bloquear indefinidamente; no exigir herramientas todavía no configuradas.

**Tests necesarios:** CI-01/02: PR de prueba con check fallido, status requerido, artifacts y merge bloqueado con autorización de settings.


### RM-42 — P3 — Wrapper y documentación de desarrollo requieren ajustes verificados

**Clasificación:** CONFIRMADO. **Archivo/método/línea:** [roommatch-api/roommatch-api/mvnw: 1](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-api/roommatch-api/mvnw#L1), [roommatch-web/README.md: 1](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-web/README.md#L1), [README.md: 1](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/README.md#L1).

**Descripción y evidencia:** mvnw versionado con modo 100644: ./mvnw falla Permission denied en Unix; CI hace chmod. README de frontend conserva plantilla, incluida referencia a e2e sin builder. Root README no documenta limitaciones reales de admin/tests ni ausencia de principal en SQL.

**Impacto:** Instalación y expectativas de contributors no son reproducibles siguiendo todos los comandos literales.

**Cómo reproducir:** Ejecutar wrapper literal; revisar git ls-files --stage y targets de angular.json. La auditoría usó sh mvnw como alternativa sin cambiar permisos del repositorio.

**Solución propuesta:** En fase de implementación, versionar bit executable y documentar comandos comprobados, configuración, migraciones y limitaciones pendientes. Mantener README intacto durante esta fase de auditoría.

**Riesgo de la solución:** No documentar tests verdes, producción lista ni E2E que aún no existen.

**Tests necesarios:** Fresh clone en runner limpio y comandos README una vez resueltos bloqueos.


### RM-43 — P2 — Datos personales en consola y tratamiento de errores de infraestructura

**Clasificación:** CONFIRMADO. **Archivo/método/línea:** [roommatch-web/src/app/pages/contactos/contactos.ts: 297](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-web/src/app/pages/contactos/contactos.ts#L297), [roommatch-web/src/app/pages/propietario/leads/interesados.ts: 178](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-web/src/app/pages/propietario/leads/interesados.ts#L178), [roommatch-api/roommatch-api/src/main/java/com/roommatch/exception/GlobalExceptionHandler.java: 83](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-api/roommatch-api/src/main/java/com/roommatch/exception/GlobalExceptionHandler.java#L83).

**Descripción y evidencia:** console.log imprime contactos desbloqueados completos e interesados con email. Hay console.error/warn de objetos de error. Advice registra excepción de acceso a datos con stack; mensajes del driver pueden incluir valores problemáticos. No se encontró logging explícito del token/password en código de aplicación.

**Impacto:** PII innecesaria en consola y posible captura por diagnóstico/telemetría. Log SQL con valores depende del error/configuración, por eso ese subcaso es riesgo.

**Cómo reproducir:** Con fixtures observar consola al cargar contactos/leads; generar constraint violation y revisar log sanitizado en entorno controlado.

**Solución propuesta:** Quitar dumps de datos personales, logger con redacción y correlationId. Mantener diagnóstico útil sin payload de contacto, password o JWT; acceso/retención controlados en operación.

**Riesgo de la solución:** No silenciar todos los errores para evitar PII; conservar metadatos mínimos y causa técnica segura.

**Tests necesarios:** PRIV-03: aserciones de logs/consola con valores centinela sensibles que no deben aparecer.


### RM-44 — P2 — Guards creen cualquier token almacenado y la sesión no se recupera globalmente

**Clasificación:** CONFIRMADO. **Archivo/método/línea:** [roommatch-web/src/app/core/services/auth.service.ts: 54](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-web/src/app/core/services/auth.service.ts#L54), [roommatch-web/src/app/core/guards/auth.guard.ts: 1](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-web/src/app/core/guards/auth.guard.ts#L1), [roommatch-web/src/app/core/interceptors/auth.interceptor.ts: 1](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-web/src/app/core/interceptors/auth.interceptor.ts#L1).

**Descripción y evidencia:** estaAutenticado solo comprueba presencia del token; datos y rol proceden de localStorage. Interceptor adjunta Bearer al API conocido, pero no centraliza expiración/401. El backend vuelve a leer rol/estado de BD: alterar localStorage no concede autoridad real.

**Impacto:** Sesión expirada puede dejar navegación protegida aparente y múltiples errores en páginas. Se refuta bypass ADMIN de servidor mediante manipulación del guard.

**Cómo reproducir:** Poner token expirado o cadena arbitraria, navegar a ruta protegida y observar que guard permite entrada; API rechaza. Modificar rol local no supera SecurityConfig.

**Solución propuesta:** Estado de sesión coherente, expiración como ayuda de UX, manejo global 401 y retorno seguro a ruta previa; backend sigue siendo autoridad. Sincronizar logout entre pestañas si forma parte de UX acordada.

**Riesgo de la solución:** No interpretar payload decodificado como autenticación ni interceptar 403/503 como logout.

**Tests necesarios:** WEB-01/09: exp, 401,403,503, rol manipulado y preservación de ruta segura.


### RM-45 — P3 — Carga inicial eager y métricas administrativas con múltiples consultas

**Clasificación:** CONFIRMADO. **Archivo/método/línea:** [roommatch-web/src/app/app.routes.ts: 1](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-web/src/app/app.routes.ts#L1), [roommatch-api/roommatch-api/src/main/java/com/roommatch/service/DashboardAdminService.java: 31](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-api/roommatch-api/src/main/java/com/roommatch/service/DashboardAdminService.java#L31), [roommatch-api/roommatch-api/src/main/java/com/roommatch/repository/DashboardAdminRepository.java: 13](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-api/roommatch-api/src/main/java/com/roommatch/repository/DashboardAdminRepository.java#L13).

**Descripción y evidencia:** Las22 rutas de componente usan import eager. Build inicial 854,44 kB raw/167,75 kB transfer; no se midió tiempo de interacción. Dashboard ejecuta 16 consultas agregadas más validación de admin; no es N+1 por fila, sino coste fijo.

**Impacto:** Páginas privadas/admin participan del grafo inicial y el dashboard repite recorridos de algunas tablas. Escala exacta depende del dataset; no sobreoptimizar cifras pequeñas.

**Cómo reproducir:** Revisar estadísticas de build e instrumentar dashboard con mismo snapshot de datos; comprobar si contadores deben representar filas dirigidas o pares únicos de matches.

**Solución propuesta:** Lazy load por feature con loadComponent; consolidar agregaciones por tabla cuando métricas lo justifiquen. Dashboard en readOnly ya existe; definir significado y hora de actualización de métricas.

**Riesgo de la solución:** No incluir pagos/ingresos ficticios porque no hay facturación. Evitar caching sin semántica de frescura.

**Tests necesarios:** PERF-06 y WEB-12: carga de rutas, bundle y equivalencia de contadores.

## Verificación individual de los 39 puntos preliminares

| Nº | Punto | Veredicto contra main | Hallazgos / tratamiento |
| ---: | ---| ---| ---|
| 1 | JWT secret | Confirmado fallback público 76 bytes; no fail-fast de production | RM-01; separar entornos y validar en startup |
| 2 | SQL encrypt/trust | Confirmados defaults locales inseguros para producción; despliegue desconocido | RM-03; conservar excepción local explícita |
| 3 | Swagger | Confirmado permitAll global | RM-04; por perfil, no exposición automática productiva |
| 4 | JWT completo | Firma/exp/estado/rol de BD sí; issuer/audience/revocación no; PII extra sí | RM-01/05/07; no refresh por moda |
| 5 | localStorage | Confirmado; no XSS propio explotable demostrado | RM-04/05/44 y comparación A/B de seguridad |
| 6 | Fuerza bruta/rate limit | No implementado en repo; proxy externo desconocido | RM-06/11/36 |
| 7 | Contraseña 6 | Confirmado; máximo 100 chars no coordina 72 bytes BCrypt | RM-06; política proporcional y pruebas Unicode |
| 8 | Ownership/IDOR | Muchos controles de dueño correctos; vínculo habitación sin autorización explícita y negocio ambiguo | RM-08/17/22; matriz de todos los IDs en seguridad |
| 9 | Matching 10/13 | Ignora 3, confirmado; escalas ya distinguen 4 vs 5 de 1 vs 5 usando umbral≤1 | RM-09; v2 con 13 criterios, fórmulas y cobertura |
| 10 | Matches desactualizados | Confirmado cache directa/inversa sin comparar cambios | RM-10; versionado y ranking completo válido |
| 11 | Performance matching | Confirmado findAll/bucle/find/save/sort; impacto por escala modelado | RM-11; no tiempos inventados |
| 12 | N+1 publicaciones | Confirmado I/O por respuesta; SQL exacto pendiente | RM-12; tabla 10/20/50 y batch |
| 13 | Solicitudes | Confirmado bloqueo de reenvío; cancelar ausente; carreras posibles | RM-13/14; D-02 |
| 14 | Contactos/privacidad | Servicio directo filtra correctamente; fuga lateral de email en matches/favoritos | RM-02/43; matriz propietario/aceptado/tercero |
| 15 | Borrado publicación | Hard delete real; FK imagen estudiada; SQL eliminada desaprovechado | RM-15; D-03 antes de soft delete |
| 16 | Planes/habitaciones | Confirmados downgrade/reactivación/expiry/destaque incompletos | RM-16/35; D-04, casos 10→1 |
| 17 | Roles | Rol único; propietario mantiene funciones authenticated; ADMIN puede perder rol | RM-18; D-06 sin migración masiva automática |
| 18 | HTTP | IllegalArgumentException → 400 general; ya existe 409 por integridad y 403MVC | RM-07; cambiar consumidores junto a contrato |
| 19 | Method security | No EnableMethodSecurity/PreAuthorize; ADMIN ya protegido por URL+servicio | RM-07/18; añadir defensa donde exista policy clara |
| 20 | API URL | Confirmado en 3 lugares, no solo constante | RM-23 |
| 21 | Admin | Confirmado placeholder/dashboard y reportes hardcodeados | RM-24/37 |
| 22 | Compartidos | 7 vacíos sin uso; Navbar/gestor sí funcionales | RM-32; reutilización selectiva |
| 23 | Typo/duplicados | ead no usado y desactualizado, mensaje no nullable | RM-32; eliminar solo copia confirmada después |
| 24 | Componentes grandes | Confirmada duplicación y GET→PUT descripción; no leak universal de subscribe | RM-30/44 |
| 25 | CSS | Global 8.485 líneas; duplicación y alcance mezclado; no toda media query sobra | RM-31; conservar apariencia |
| 26 | Accesibilidad | Defectos de idioma/labels/modal confirmados; contraste/foco real pendientes | RM-29; WCAG 2.1 AA como objetivo |
| 27 | UX async | Hay estados en muchas pantallas, fallos parciales silenciados y admin sin estados | RM-27/28; matriz por pantalla |
| 28 | JPA/DTO/SQL | 168 campos vs 167 columnas; principal ausente y otros desajustes | RM-19/33; matriz completa arquitectura |
| 29 | Migraciones | Base destructivo, 1 migración manual 002, sin Flyway | RM-34; baseline explícito, no ejecutado |
| 30 | Índices | Existen 12 explícitos más UNIQUE/PK; faltan candidatos vinculados a queries | RM-36; planes reales pendientes |
| 31 | Concurrencia | UNIQUE protege varios exists→save; suscripciones/cupo/par/principal no suficientes | RM-14/20/35 |
| 32 | Imágenes | URLs externas; backend solo tamaño/no vacío; max/principal racy | RM-19/20/21/26 |
| 33 | Datos públicos | DTO catálogo no expone email/teléfono; imágenes/autor/vínculos fallan visibilidad | RM-22; email ajeno sí en rutas autenticadas de descubrimiento RM-02 |
| 34 | OWASP | SQL parametrizado; no SSRF actual; riesgos y controles separados | ROOMMATCH_SECURITY.md |
| 35 | CI/CD | Confirmado frontend sin tests, backend sin verify | RM-38/41; propuesta, sin editar workflow en fase 1 |
| 36 | Tests | 1 backend/2 frontend; ninguno de negocio; frontend falla | RM-38/39; plan priorizado |
| 37 | H2 vs SQL Server | H2 usa dialect propio/create-drop; no valida scripts | RM-19/39; Testcontainers en integración crítica |
| 38 | Git/main | GitHub protected=false | RM-41; settings solo con autorización explícita |
| 39 | PR abiertos | 2 PR, ambos conflictivos; título Java 25 no describe head actual | Tabla PR, ninguno mezclado |

Las búsquedas adicionales incluyeron Optional, nulls, EAGER/LAZY, transacciones, queries nativas/dinámicas, estados, rutas, hardcodes, URLs, logging y referencias sin uso. No se halló SQL dinámico con entrada concatenada ejecutable, exposición de passwordHash en DTO ni una LazyInitializationException reproducida. Se detectaron como adicionales: emails por favoritos/matches, lang=en, recuperación simulada, enlaces /mis-intereses y /soporte inexistentes, galería pública desconectada y copy de lead con distinta nulabilidad.


## Matching: diagnóstico y propuesta v2 pendiente de decisión

El porcentaje actual es `100 × coincidencias / 10`, redondeado a dos decimales. No representa una probabilidad observada de convivencia exitosa. La igualdad de peso tampoco está justificada con datos. **No se cambió el algoritmo ni se agregaron pruebas a la aplicación durante esta auditoría.** El plan de pruebas define la implementación posterior.

### Contraste de los 13 campos

| Criterio | Comportamiento actual | Peso v2 propuesto | Justificación de diseño | Fórmula v2 |
| ---| ---| ---: | ---| ---|
| Distrito | Texto igual, sin distinguir catálogo/alias | 14 | Viabilidad geográfica importante; falta distancia real para inferir proximidad | 1 si mismo distrito canónico; 0 distinto. No inventar cercanía por nombres |
| Presupuesto | Cualquier solapamiento = 1, incluyendo extremos | 18 | Viabilidad económica; un intervalo parecido aporta más información que tocar un extremo | Jaccard de conjuntos de importes en céntimos, definido abajo |
| Fecha de mudanza | Ignorada | 8 | Disponibilidad temporal puede impedir concretar una convivencia | `max(0, 1 − abs(díasEntre)/60)`; 60 días es parámetro provisional |
| Limpieza | Diferencia absoluta ≤1 = 1 | 12 | Diferencias de hábitos diarios afectan convivencia repetidamente | `1 − abs(a−b)/4`, escala 1..5 |
| Ruido | Diferencia absoluta ≤1 = 1 | 10 | Descanso y concentración; escala gradual | `1 − abs(a−b)/4` |
| Sociabilidad | Diferencia absoluta ≤1 = 1 | 6 | Preferencia relevante, pero menos excluyente que viabilidad económica | `1 − abs(a−b)/4` |
| Horario | Igualdad de texto | 7 | Rutinas; no hay horas exactas en el modelo | Misma categoría 1; variable/fija 0,5; fija distinta 0 |
| Visitas | Igualdad de texto | 6 | Bajas/moderadas/frecuentes sí expresan orden | Codificar 0/1/2; `1 − abs(a−b)/2` |
| Mascotas | Igualdad de texto | 5 | Puede ser requisito excluyente, pero falta separar tenencia y tolerancia | Igual 1, diferente 0 provisional; no excluir candidatos sin decisión |
| Fumar | Igualdad de texto | 6 | Preferencia potencialmente excluyente; misma ambigüedad del dato | Igual 1, diferente 0 provisional |
| Alcohol | Ignorado | 2 | Incluir dato pedido; importancia no medida y posible duplicación con sociabilidad | Igual 1, diferente 0 provisional |
| Gastos | Ignorado | 4 | Expectativa de reparto económico distinta al presupuesto total | divididos/divididos o proporcional/proporcional 1; distinto 0 |
| Convivencia | Igualdad de texto | 2 | Categoría general solapa con sociabilidad/ruido; evitar doble conteo excesivo | Matriz nominal parcial explícita debajo |
| **Total** | **10 criterios efectivos** | **100** | **Pesos de política propuestos, no aprendidos ni calibrados** | |

No existe evidencia suficiente para afirmar que estos pesos sean óptimos. Son una hipótesis razonada y revisable para D-05. Si el usuario exige probabilidad real, se necesita definir resultado de convivencia, recoger datos consentidos, evaluar calibración, sesgos y suficiente muestra; no sustituir eso por otro porcentaje inventado. La etiqueta recomendada es **“índice de compatibilidad según preferencias”**, con contribuciones y versión. No puntuar edad, nombres, universidad ni atributos personales no solicitados.

### Presupuesto con límites completos y precisión monetaria

Validar números no negativos, mínimo≤máximo y precisión de dos decimales. Convertir exactamente PEN a enteros de céntimos; no usar float. Para intervalos inclusivos `[a,b]` y `[c,d]`:

`I = max(0, min(b,d) − max(a,c) + 1)`

`U = (b−a+1) + (d−c+1) − I`

`s_presupuesto = I/U`

El “+1” corresponde a la unidad mínima monetaria, un céntimo; evita denominador cero en presupuestos fijos. No es una tolerancia oculta. Dos intervalos idénticos puntúan 1, disjuntos 0, y un único importe común tiene compatibilidad muy pequeña salvo que ambos presupuestos sean exactamente ese importe. Un intervalo amplio que contiene otro estrecho resulta viable pero menos parecido: **viabilidad y semejanza son distintos atributos de la respuesta**. Mostrar `hayImporteComun` por separado; no eliminar candidatos solo por score de este criterio.

| A (S/) | B (S/) | Score del criterio | Lectura |
| ---| ---| ---: | ---|
| 500–800 | 500–800 | 1 | Mismo rango |
| 500–900 | 700–1.000 | 20001/50001 ≈0,400012 | Coincidencia parcial |
| 500–800 | 800–1.200 | 1/70001 ≈0,0000143 | Un céntimo común; viable en el extremo |
| 500–800 | 801–1.200 | 0 | Sin importe común |
| 800–800 | 800–800 | 1 | Dos presupuestos fijos iguales |
| 800–800 | 700–900 | 1/20001 ≈0,0000500 | Un fijo dentro de un rango amplio; explicar viabilidad |

Alternativa D-05: coeficiente de solapamiento `I/min(anchoA,anchoB)` premia contención completa con 1; beneficia rangos flexibles pero pierde diferenciación. Jaccard es la propuesta para medir semejanza, no capacidad de pago ni reparto del alquiler. Es necesario confirmar que presupuestoMin/Max corresponde al mismo concepto en ambos usuarios (aporte mensual personal, no precio total de una vivienda).

### Categorías parciales, límites y datos faltantes

Los valores reales del formulario son horario mañana/tarde/noche/variable, visitas bajas/moderadas/frecuentes, mascotas/fumar/alcohol si/no, gastos divididos/proporcional y convivencia tranquila/social/independiente/mixta. API permite strings no vacíos de hasta 50; debe validarse catálogo y normalizarse antes de calcular. No aceptar dos valores desconocidos iguales como coincidencia.

| Convivencia | Tranquila | Social | Independiente | Mixta |
| ---| ---: | ---: | ---: | ---: |
| Tranquila | 1 | 0 | 0,5 | 0,5 |
| Social | 0 | 1 | 0 | 0,5 |
| Independiente | 0,5 | 0 | 1 | 0,5 |
| Mixta | 0,5 | 0,5 | 0,5 | 1 |

Esta matriz es una hipótesis nominal: “independiente” no prueba ausencia de visitas ni “mixta” compatibilidad universal. Debe aprobarse, igual que horario variable/fijo. Si mascotas/fumar describen conducta propia y no tolerancia, la comparación binaria no puede representar aceptación mutua. La alternativa es preguntar por conducta y tolerancia por separado, con costo de formulario/migración. No introducir esa ampliación silenciosamente.

Para cada criterio conocido y válido en ambos perfiles, calcular `sᵢ∈[0,1]`; de lo contrario marcarlo desconocido, sin confundir desconocido con incompatibilidad. Sea `V` ese conjunto:

`cobertura = sum(wᵢ, i∈V) / 100`

`score = roundHalfUp(100 × sum(wᵢ × sᵢ, i∈V) / sum(wᵢ, i∈V), 2)`

Sin criterios válidos: `score=null`, cobertura 0 y razones de faltantes. Propuesta de presentación: por debajo de 70% de cobertura, mostrar “información insuficiente” y no intercalar ese 100% parcial por encima de un perfil completo. El umbral 70% también es decisión de producto, no validez estadística. Conservar score interno para diagnóstico, pero diferenciar grupos de cobertura en ranking.

Ejemplos comprobables del diseño: limpieza 4/5→0,75; limpieza 1/5→0. Si todo coincide salvo limpieza 4/5, el score completo es 97; si limpieza 1/5,88. Si únicamente coincide distrito, el score calculable es 100 pero cobertura 14% y no debe mostrarse como match 100% fiable. Estas cifras son ejemplos aritméticos de la propuesta, no resultados de la aplicación ejecutada.

Fecha: comparar `LocalDate` con días naturales, sin zona horaria ni milisegundos. Diferencia 0→1; 15→0,75; 30→0,5; 60 o más→0. Fechas pasadas no se reescriben a “hoy”: se señalan para confirmación. Si negocio decide que dejan de ser válidas con el tiempo, la cache debe incluir vencimiento temporal además de versiones de perfil. Cero, null, rangos inválidos, 29 febrero y campos desconocidos forman parte de las pruebas.

### Vigencia y ranking

| Alternativa | Ventaja | Limitación |
| ---| ---| ---|
| Borrar todos los matches en ambas direcciones al editar | Reparación simple inicial | Recalculo concurrente puede reinsertar datos viejos; no versiona algoritmo |
| Comparar fechaCalculo con fechaActualizacion de ambos | Menos campos nuevos | Precisión de reloj, misma marca temporal, algoritmo nuevo sin fecha de perfil |
| **Versiones de perfil y algoritmo + cálculo bajo demanda** | Validez explícita; soporta carreras y cambios de pesos | Requiere columnas/cache y política coherente del ranking |
| Cálculo dinámico sin guardar pares | Elimina cache obsoleta | CPU repetida y orden global costoso sin candidatos acotados |
| Evento/cola de recalculo | Desacopla latencia | Consistencia eventual, operación extra; prematuro para alcance actual |

Selección técnica propuesta: versiones enteras de ambos perfiles y `algorithmVersion`; invalidación inicial bidireccional, batch bajo demanda y exclusión inmediata de usuarios inactivos. Leer perfiles y versiones en un snapshot coherente, persistir con versiones capturadas y comprobar que no cambiaron. Si cambian, descartar/reintentar con límite. La lectura siempre vuelve a comparar versiones: ni un proceso tardío ni cache inverso debe imponerse a un perfil nuevo.

`listarMisMatches` necesita validez del conjunto de candidatos, no solo de filas mostradas. Para cientos/miles, recalcular candidatos activos en memoria en lote, ordenar y paginar después; para mayor escala, definir pool de candidatos y top-K con su propia vigencia. Paginar primero y recalcular esa página puede omitir a alguien que ahora debería encabezar el ranking. Deduplicar el par no ordenado si la fórmula es simétrica y mantener una vista por usuario solo cuando sea útil; esto es una migración evaluable, no requisito inmediato.

Cada respuesta propuesta debe poder explicar: versión, versiones de perfiles, fecha de cálculo, score, cobertura, criterios presentes/faltantes y contribución por criterio. No almacenar más PII para explicar el score. Las pruebas MAT del plan incluyen exhaustividad sobre los dominios pequeños y propiedades matemáticas, además de ejemplos.

## Performance: costes demostrables y mediciones pendientes

No hubo SQL Server operativo, Docker disponible ni dataset de carga en este entorno. Por eso **los conteos siguientes son modelos estructurales de llamadas de repositorio, no mediciones SQL ni benchmarks**. H2 o mocks tampoco demostrarían latencia real de SQL Server. Los resultados medidos son npm ci/build/tests y los logs de CI identificados en el baseline.

### Calcular matches según usuarios con perfil

Suponiendo N perfiles, todos activos y uno del solicitante, el bucle compara N−1 candidatos. Cada candidato hace una búsqueda de resultado y un save; no todas las llamadas a save significan un UPDATE si nada quedó dirty. Nuevas filas con IDENTITY pueden forzar INSERT individual. El coste de hidratación de Usuario/Rol y flush antes de consultas es adicional.

| N perfiles | Comparaciones por cálculo | Búsquedas individuales de match | Llamadas save | Pares dirigidos si todos calculan una vez | Acción proporcionada |
| ---: | ---: | ---: | ---: | ---: | ---|
| 100 | 99 | 99 | 99 | 9.900 | Unitarios, quitar I/O del bucle; no cola ni motor externo |
| 1.000 | 999 | 999 | 999 | 999.000 | Cargar proyección/cache en lotes, reloj/versiones, medir plan y heap |
| 10.000 | 9.999 | 9.999 | 9.999 | 99.990.000 | Evitar persistir todos los pares; candidatos/top-K acordados y paginación real |
| 100.000 | 99.999 | 99.999 | 99.999 | 9.999.900.000 | Pool de candidatos acotado/indexable, procesamiento incremental solo si se mide necesidad |

Tiempo algorítmico por cálculo: O(N) comparación de un número fijo de criterios más O(N log N) de ordenación completa; memoria O(N). Persistencia completa de todos los usuarios y recomputaciones: O(N²). No se extrapolan milisegundos desde cien usuarios a cien mil. El tamaño de parámetros de SQL Server impone dividir listas IN grandes: usar lotes conservadores y contar también parámetros de otros filtros; no enviar 100.000 IDs en una sola query.

Una primera optimización puede consistir en leer el perfil actual una vez, consultar proyección de candidatos activos y resultados existentes en una o varias consultas batch, calcular puramente en memoria, ordenar y paginar. Si se conserva historia, persistir solo lo necesario; saveAll no garantiza JDBC batching. Las sugerencias de candidatos por distrito/presupuesto solo son admisibles si negocio acepta que no se explorará todo el universo. Mantener módulos dentro del monolito; no hay evidencia para microservicios, Kafka o un motor vectorial.

### Publicaciones: modelo N+1

Por cada fila de otro autor y sesión autenticada, `crearResponse` llama compatibilidad. En página anónima o publicación propia no hay ese trabajo. Múltiples publicaciones del mismo autor repiten llamadas de repositorio, aunque el primer nivel de Hibernate pueda reutilizar entidades.

| Tamaño página | Hit directo: llamadas extra | Hit inverso: llamadas extra | Sin resultado: llamadas extra | Base típica de paginación |
| ---: | ---: | ---: | ---: | ---|
| 10 | 10 | 20 | 40 | Datos + count, hasta 2 consultas |
| 20 | 20 | 40 | 80 | Datos + count, hasta 2 consultas |
| 50 | 50 | 100 | 200 | Datos + count, hasta 2 consultas |

El miss son 2 búsquedas de match y 2 búsquedas de perfil. Este total **no incluye** carga LAZY de autores, habitación vinculada/propietario, relaciones EAGER de los perfiles/matches, ni autenticación y rol de BD. Spring Data puede omitir count en ciertos casos de página; por tanto tampoco se declara “exactamente 202 SQL” para 50. Con autores distintos, el mapeo LAZY puede añadir aproximadamente una carga de autor por fila; otras relaciones dependen del estado del persistence context. Se necesita Hibernate Statistics/datasource proxy más plan SQL para medirlo.

Propuesta: IDs distintos de autores, perfil solicitante y perfiles/cache en lote, mapa `autorId→compatibilidad`, y proyección de datos públicos con estado efectivo de vínculo. Objetivo del test: número de consultas acotado por lotes, no proporcional a filas. Por ejemplo un presupuesto ≤6 consultas de negocio para páginas≤50, excluyendo autenticación, es una meta inicial a validar, no un resultado obtenido.

### Otros costes

| Punto | Evidencia | Tratamiento propuesto |
| ---| ---| ---|
| Contactos frontend | 2 listas de solicitudes + N peticiones de contacto | Endpoint paginado de contactos autorizados con batch; ya existe query de listado no utilizada |
| Imágenes en gestión habitaciones | Una carga por habitación con forkJoin | Endpoint batch o proyección; público debe incluir principal sin N llamadas nuevas |
| Favoritos/solicitudes/leads/perfiles | Listas sin Page y relaciones EAGER por defecto | Proyecciones, paginar y medir queries; EAGER no garantiza un único join |
| Auth en cada request | Parse JWT repetido, consulta de usuario/rol | Un parse; conservar consulta de estado por seguridad o cache con invalidación definida |
| Dashboard admin | 16 COUNT de métricas + validarAdmin | Coste fijo; agregaciones condicionales por tabla si mediciones lo justifican |
| Buscar habitaciones | SQL dinámico parametrizado; LOWER y contains | No injection confirmada; revisar cardinalidad/lecturas y distrito canónico |
| Lista publicaciones | Sin índice dedicado por autor/fecha o estado/fecha | Candidatos de índice siguientes; validar plan y filtros realmente usados |
| Angular inicial | 22 rutas eager, 854,44 kB raw, 167,75 kB transfer | loadComponent por feature; no umbral de UX inferido desde bytes |
| Componentes grandes | MiCuenta 1.115, MisHabitaciones 1.539, MisPublicaciones 1.531 líneas físicas | Muchas líneas en blanco; dividir por responsabilidad y peticiones, no por un umbral arbitrario de líneas |

### Índices candidatos ligados a consulta

Son hipótesis de optimización. Antes de crear: obtener DDL/índices reales, volúmenes, distribución de estados/distritos, plan real, `SET STATISTICS IO/TIME`, p50/p95 y coste de escrituras. No añadir todos indiscriminadamente. Los índices para unicidad son primero integridad y se justifican por invariantes, aunque no aceleren un listado.

| Tabla | Query real / método | Existente | Candidato y motivo |
| ---| ---| ---| ---|
| usuario | UsuarioRepository.findByEmail | UNIQUE email + IX_usuario_email | El segundo índice por email parece redundante con el unique; revisar definiciones/uso antes de quitar |
| perfil_convivencia | buscarActivosPorDistrito; presupuesto; findAll de matching | UNIQUE usuario; IX(distrito_preferido, presupuesto_min, presupuesto_max) | Contains+LOWER no aprovecha seek inicial; distrito canónico exacto haría útil compuesto. No indexar todos los 13 hábitos |
| publicacion_roomie | buscarPublicaciones ORDER BY fechaPublicacion DESC | PK y FK sin índice automático | Evaluar (estado, fecha_publicacion DESC, id_publicacion DESC); tipo opcional puede necesitar variante si es selectivo |
| publicacion_roomie | findByUsuarioIdUsuarioOrderByFechaPublicacionDesc | Sin índice de autor | (id_usuario, fecha_publicacion DESC, id_publicacion DESC), cubriendo lista propia sin barrido global |
| publicacion_roomie | validar FK y visibilidad de habitación vinculada | FK agregada por 002 | (id_habitacion) si se consulta/actualiza por vínculo; la FK no crea índice por sí sola |
| habitacion | búsqueda estado activa, destacado/fecha; precio/distrito opcionales | IX(distrito, precio, estado) | Evaluar (estado, destacada DESC, fecha_publicacion DESC, id_habitacion DESC); variante de precio solo con plan selectivo |
| habitacion | contar cupo por propietario y estado; lista propia | IX(id_propietario) | (id_propietario, estado) para cuota; comparar coste con índice de propietario/fecha para lista. No necesariamente ambos |
| match_resultado | listarMatchesPorUsuario origen/porcentaje | UNIQUE(origen, destino); IX(origen, porcentaje DESC) | Índice existente útil; agregar desempate id/versiones vía diseño cache si se necesita, no duplicar el mismo prefijo |
| match_resultado | invalidar ambos participantes/versiones | UNIQUE empieza por origen | Índice por destino si se elige invalidación inversa frecuente; batch de pares canónicos puede cambiar este plan |
| solicitud_contacto | recibidas/enviadas ORDER fechaSolicitud | IX(receptor, estado), IX(emisor, estado) | (receptor, fecha_solicitud DESC, id_solicitud DESC) y emisor equivalente si los listados dominan; comparar con filtrado por estado futuro |
| solicitud_contacto | un pendiente por par, nuevos intentos históricos | UNIQUE direccional global | Migrar a par canónico con UNIQUE filtrado de pendiente; no índice único global del par para todo historial |
| contacto_roomie | existeContactoDesbloqueado a/b o b/a | UNIQUE id_solicitud | Par canónico UNIQUE; listado por participante puede requerir índices en cada columna o modelo de membresía |
| notificacion | usuario/leido opcional + fecha; count no leídas | IX(usuario, leido) | Evaluar (usuario, leido, fecha_creacion DESC, id_notificacion DESC) y coste de listado sin filtro; un único índice no elimina necesariamente ambos sorts |
| lead_habitacion | propietario a través de habitación, estado, fecha | UNIQUE(habitacion, interesado); IX(habitacion, estado) | Extender por fecha si plan usa habitación→leads; (interesado, fecha_lead DESC, id_lead DESC) para mis intereses |
| favorito_usuario | propios ORDER fechaFavorito | UNIQUE(usuario, favorito), IX(usuario) | Evaluar (usuario, fecha_favorito DESC, id_favorito DESC); eliminar redundancia solo tras medir |
| reporte_usuario | estado opcional ORDER fechaReporte | IX(estado) | Evaluar (estado, fecha_reporte DESC, id_reporte DESC); si sin filtro predomina considerar orden por fecha |
| reporte_habitacion | mismo patrón | Sin índice por estado/fecha | (estado, fecha_reporte DESC, id_reporte_habitacion DESC) según carga real |
| imagen_habitacion / imagen_publicacion | padre ORDER orden; count por padre | PK; sin índice único de principal | (padre, orden, id_imagen); UNIQUE filtrado padre WHERE principal=1 para integridad tras sanear |
| suscripcion_propietario | última activa por propietario | Sin unicidad activa | UNIQUE filtrado propietario WHERE estado='activo'; consulta de vigencia además debe aplicar fechas |

No se detectó ordenamiento controlado por string arbitrario de usuario en JPQL. Todas las concatenaciones revisadas agregan fragmentos constantes y enlazan valores por parámetros. Los filtros opcionales, OR, LOWER y comodines pueden empeorar planes sin convertirse en SQL injection. Evitar proponer índices solo por nombres de tablas.

## Frontend: integración y estados por pantalla

Evaluación por código, no sesión visual autenticada. “Sí” indica implementación presente, no prueba automatizada pasada. Los errores de callbacks de HttpClient no implican por sí solos memory leak; las peticiones terminan, pero búsquedas concurrentes necesitan cancelación o control de generación.

| Pantalla | Loading | Error | Empty | Success/acción | Integración/defecto |
| ---| ---| ---| ---| ---| ---|
| Home | Sí, varias cargas | Parcial; fallas se vuelven vacío/perfil faltante | Sí | Datos/cálculo sugerido | Autenticado coordina variaspeticiones manualmente; no estado granular |
| Matches | Sí | Sí | Sí | Calcular/favorito/solicitud | Score viejo; POST devuelve lista completa y luego GET; sin protección completa de dobleclick |
| Favoritos | Sí | Sí | Sí | Quitar/solicitud | DTO filtra propiedad de lista, pero expone email ajeno |
| Solicitudes | Sí; contador espera 2 listas y bandera intereses | Sí, compartido | Sí | Aceptar/rechazar | Cancelar/reenvío no existen; modales sin manejo accesible de foco |
| Contactos | Sí | Parcial por contacto | Sí | Abrir medios permitidos | 2+NHTTP; errores individuales parecen ausencia; PII en consola |
| Notificaciones | Sí | Sí | Sí | Leer/leer-todas | Filtro y conteo de página; destinos muertos |
| Habitaciones/detalle | Sí | Sí | Sí | Interés | Imágenes registradas no se muestran; detalle tiene placeholder |
| MisHabitaciones | Sí | Sí | Sí | CRUD parcial/imagen/estado | Primera 100; regla cliente activas difiere de backend activas+pausadas |
| Publicaciones/lista | Sí | Sí | Sí | Búsqueda | Orden/compatibilidad solo página 6 |
| Publicación detalle | Sí | Sí | Sí | Solicitud | Anónimo falla en backend; gate login de solicitud insuficiente |
| MisPublicaciones | Sí | Sí | Sí | Crear/editar/estado/borrar | Primera 50; habitación vinculada de primeros 50 del catálogo; imágenes no integradas |
| MiCuenta/perfil | Sí | Sí, con inferencias por mensaje/400 | Perfil/contacto opcional | Guardar | GET→PUT descripción y restricciones inconsistentes |
| Propietario registro/planes/panel | Sí | Sí/parcial | Sí/parcial | Alta/cambio/confirmación | Métricas sobre primeras 100, simulación sin pago y expiración indefinida |
| Admin dashboard | No | No | No | No | Placeholder |
| Admin reportes | No | No | No | No | Tablas ficticias y botones sin handlers |

### Componentes compartidos y organización

| Componente | Estado actual | Decisión propuesta posterior |
| ---| ---| ---|
| room-card | Vacío, sin uso | Implementar si se comparte contrato de catálogo/Home; primero resolver imagen principal |
| profile-card | Vacío, sin uso | Extraer tarjeta común de matches/favoritos solo donde semántica coincida |
| pagination | Vacío, sin uso | Implementar contrato PageResponse + eventos y accesibilidad; reutilizar |
| notification-card | Vacío, sin uso | Eliminar si queda una única vista y no hay beneficio; no borrar página de notificaciones |
| compatibility-badge | Vacío, sin uso | Implementar con score/cobertura/versiones; unificar umbrales/etiquetas |
| empty-state | Vacío, sin uso | Implementar estado común con mensaje y acción contextual |
| report-modal | Vacío, sin uso | Implementar creación de reportes sobre API real y modal accesible |
| image-manager-modal | Funcional para habitación | Conservar; corregir accesibilidad, carga/errores y extender solo después de definir contrato |
| navbar | Funcional con subscripciones canceladas | Conservar; mover estilos repetidos y coherencia de sesión |

CSS global debe contener base/tokens/utilidades, estilos compartidos en componente y reglas de página en su feature. Comparación visual antes/después es requisito de refactor, no fue realizada por falta de navegador. No se propone rediseño, cambio de paleta, eliminación de flujo ni adopción de otra librería de UI. Las magnitudes físicas están infladas por líneas en blanco: MatchService 1.141 líneas/715 no vacías y PublicacionRoomieService 1.590/920; reducir líneas no sustituye separar responsabilidades.

## Backend, HTTP y reglas de negocio

La autoridad de seguridad está en backend, y hay ownership correcto en la mayoría de mutaciones por IDs. Añadir `@EnableMethodSecurity` y `@PreAuthorize` a administración es defensa adicional útil, pero las policies de propiedad/cupo/estado deben permanecer en servicio y repositorio. No basta escribir `hasRole(PROPIETARIO)` para proteger idHabitacion; tampoco se debe usar USUARIO exclusivo para funciones normales que hoy usan propietarios.

Contrato propuesto: 401 ausencia/credencial inválida; 403 autenticado sin permiso; 404 recurso inexistente o no visible bajo política consistente; 409 conflicto/versión/transición; 400 payload inválido; 429 abuso; 503 dependencia no disponible. Mantener envelope actual mientras cliente migra, con códigos estables y errores por campo. No usar substring del mensaje español para decidir si falta un perfil. Los 404 de recurso ajeno pueden elegirse para no revelar existencia, pero debe ser una política deliberada y uniforme, no un 400 accidental.

No se confirmó una NullPointerException productiva reproducible. `validarPresupuesto` presupone requests validados y requiere pruebas unitarias de entrada; métodos de servicio no son frontera HTTP por sí solos. Optional se usa generalmente con orElseThrow, pero excepciones de dominio están mal clasificadas. Leer rol/usuario fuera de transacción usa asociaciones EAGER actuales; refactor a LAZY exige cubrir el mapeo dentro de transacción/proyección y mantener `open-in-view=false`. La falta de readOnly en algunas lecturas es mejora menor, no se reporta como brecha de seguridad.

## Base de datos y migraciones

La matriz completa de 168 campos está en arquitectura; los hallazgos prioritarios son RM-19/33/34/35. SQL usa VARCHAR y no declara collation: no puede asegurarse soporte de todos los caracteres ni equivalencias de email/distrito/acentos sin medir SQL Server real. No cambiar a NVARCHAR de forma ciega: estudiar almacenamiento/índices/clientes y requerimiento de Unicode. Estados strings y checks divergen de transiciones de API: cancelada, alquilada y eliminada no implican endpoints existentes.

Flyway es recomendable por drift y upgrade, pero **no se incorporó**. Plan seguro: inventariar versión/collation/DDL real; backup con restore ensayado; reconciliar diferencias; construir V1 no destructiva para instalaciones nuevas y V2 de vivienda, V3 de imagen/invariantes aprobadas; marcar baseline existente solo si corresponde exactamente al estado comprobado. Si una BD ya tiene 002 parcialmente aplicada, primero reconciliarla, no esconder el error subiendo baselineVersion. Mantener seed de roles/Gratis idempotente y separar datos demo. Deshabilitar clean en producción, ejecutar migraciones con cuenta separada y validar esquema al arranque tras reconciliación. Los cambios posteriores son append-only y las correcciones hacia adelante; rollback destructivo exige restore planificado y conocer RPO/RTO. No mover un DROP DATABASE a V1 y llamarlo migración segura.

## PR abiertos y Git

Se revisaron todos los abiertos devueltos por GitHub: 2. Se comparó cada head con el SHA actual de main, además de la comparación desde merge-base. La lista PR puede conservar una base SHA histórica: no se usó como sustituto del main real fijado.

| PR | Estado GitHub | Comparación contra main fijado | Clasificación | Recomendación |
| ---| ---| ---| ---| ---|
| [#2 — Step 3: Upgrade Java Target - Compile: SUCCESS](https://github.com/1621-nicolas/roommatch/pull/2) | Abierto, mergeable=false, dirty; head1f930f34e15c | 2commits por delante/6 por detrás. Título/body hablan Java 25, pero head actual declara java.version 17 y compiler release 17. Su POM añade Lombok/procesador y carece de H2 presente en main. Snapshot head difiere en 54 archivos (3.413+/1.724−) porque carece de cambios posteriores de main | **NO RECOMENDADO**; título obsoleto y necesitaría rebase | No migrar Java ni reemplazar main con este árbol. Evaluar si queda algún cambio útil aislado; no hay justificación de Java 25 |
| [#3 — Fix/roommatch integral2026-09-06](https://github.com/1621-nicolas/roommatch/pull/3) | Abierto, mergeable=false, dirty; heade05a7afee8a5 | 55commits por delante/5 por detrás desde merge-base; el diff directo de árboles con main tiene solo 3 archivos, 2+/5−. Mayor parte ya está incorporada | **REQUIERE REBASE**; gran parte obsoleta | Revisar únicamente diferencias necesarias sobre main actual; no reintroducir fixes ya superados |

Las 3 diferencias directas de #3 son: properties exige JWT_SECRET sin fallback; template panel usa `propietario?.limiteHabitaciones` en vez de `$any(propietario).limiteHabitaciones`; tsconfig.app quita rootDir. La primera mejora el fallo por secreto ausente, pero no sustituye separación completa de perfiles/validación. Las otras dos deben probarse con el Angular actual y el error de nulabilidad que main ya corrigió. “Ahead 55” no significa 55 cambios funcionales aún faltantes. El diff directo de head#2 tampoco es la lista de cambios que un merge correcto aplicaría: refleja divergencia histórica y se documenta para evitar reemplazos regresivos.

Main figura protected=false. Proponer PR obligatorio, checks obligatorios con nombres exactos, bloqueo de checks fallidos y restricción de push directo/bypass conforme a los colaboradores. No se modificó protección, ruleset, permisos ni PR. Futura implementación en `audit/roommatch-hardening` o equivalente, nunca main; no merge sin autorización.

## Testing y CI/CD

Cobertura porcentual desconocida: no hay informe JaCoCo/coverage generado. Existen 1 test de contexto backend y 2 tests mínimos frontend fallidos. Las familias de pruebas del registro son un plan pendiente, no tests ya creados. Prioridad: secreto/401/403/ownership/privacidad, sanciones/cupo/concurrencia, matching y cache, solicitudes/contactos, publicaciones/imágenes, reportes/admin y clientes Angular.

Pipeline objetivo inicial: backend `./mvnw --batch-mode clean verify` (incluye fase test) y frontend `npm ci`, `npm run build`, `npm test -- --watch=false`. Cuando haya integración SQL Server, Failsafe/driver real como gate de cambios relevantes. Añadir frontend tests después de reparar la suite, sin `continue-on-error`; no omitir pruebas para que verde. Lint necesita configuración inexistente; incorporarlo por etapas. Auditorías de dependencias con triage, feed/cache y excepciones con fecha, sin bloquear por falsos positivos sin análisis. Reportes de cobertura y tests como artefactos; duración objetivo de pipeline a calibrar sobre runner, no promesa sin medición.

## Deuda técnica priorizada por beneficio/costo

| Grupo | Beneficio | Costo relativo | Orden |
| ---| ---| ---| ---|
| JWT fail-fast, email público, sanción/reactivación | Muy alto: elimina vías concretas de daño | Bajo–medio | Sprint 0 |
| Test frontend, baseline backend y CI tests | Muy alto: hace visible la regresión | Bajo–medio | Sprint 0 |
| Esquema imágenes y constraints críticos | Alto: instalaciones y datos coherentes | Medio con migración | Sprint 0/1 |
| Estados solicitudes, cupo/plan y conservación | Alto: reglas de negocio confiables | Medio; depende de decisiones | Sprint 1 |
| Matching vigente, gradual y batch | Alto: propuesta central del producto | Medio–alto | Sprint 2 |
| Administración real | Alto: moderación utilizable | Medio | Sprint 3 |
| HTTP, contratos, paginación y errores UX | Alto: integración predecible | Medio | Sprint 1/4 |
| Componentes/CSS/accesibilidad | Medio–alto: uso y mantenimiento | Medio; verificación visual | Sprint 4 |
| Lazy routes/índices candidatos | Variable por mediciones | Bajo–medio | Después de métricas |
| Flyway, TLS, secretos operativos, restore y deployment | Alto para producción | Medio–alto | Base temprana; completar Sprint 5 |
| Cambio de Java/stack/microservicios/almacenamiento externo | Beneficio no demostrado | Alto | No recomendado actualmente |

## Roadmap y criterio de cierre

Detalle en `ROOMMATCH_ROADMAP.md`: Sprint 0 seguridad/gates, Sprint 1 integridad, Sprint 2 matching, Sprint 3 admin, Sprint 4 calidad/UX, Sprint 5 producción. Las decisiones D-01 a D-09 quedan explícitas; no fueron asumidas como autorización para implementarlas. Se pueden preparar fixes técnicos independientes después de revisar este informe, pero nunca elegir por cuenta propia una regla de negocio que cambia el producto.

**Estado al entregar:** auditoría estática y baseline documentados; validación dinámica parcial y bloqueada en backend/SQL/navegador; ninguna corrección implementada. No se declara RoomMatch terminado. Faltan compilación/verify backend local reproducible, frontend tests verdes, reglas críticas probadas, CI completo, eliminación de P0 condicional, P1 resueltos o aceptados con evidencia, admin real, cache vigente, DTO/SQL alineados, UX sin mocks y documentación actualizada con hechos. README queda sin editar por la orden de esta fase.

