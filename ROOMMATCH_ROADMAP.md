# ROOMMATCH — Roadmap de corrección y decisiones pendientes

## Seguimiento actual — 20 de septiembre de 2026

La implementación fue autorizada después de entregar la auditoría. Las
propuestas originales se conservan debajo como registro de planificación;
la rama ya existe y contiene correcciones verificadas. Las políticas que se
aplican ahora están documentadas en [BUSINESS_RULES](docs/BUSINESS_RULES.md).

| Sprint | Avance comprobado | Trabajo aún pendiente |
| --- | --- | --- |
| 0: seguridad | Claves/perfiles, privacidad, abuso, JWT y gates CI | Análisis Maven completo y revisión residual |
| 1: integridad | Solicitudes, planes, ownership, galerías, versiones y contactos | Matriz final DTO/JPA/SQL y flujos completos en navegador |
| 2: matching | 13 criterios, cobertura, lectura vigente, batch y pruebas | Medición de carga SQL a escala y validación de experiencia |
| 3: admin | Dashboard real, filtros, revisión/sanción e historial | QA visual y flujos de moderación de extremo a extremo |
| 4: calidad y UX | Inicio, navegación, acceso, registro y contactos renovados | Resto de pantallas, galerías públicas, accesibilidad y CSS de publicaciones |
| 5: producción | Flyway, configuración de proxy/CSP, CI con SQL Server | Infraestructura real, recuperación, documentación legal y protección de main |

El PR sigue en borrador y no debe fusionarse solo porque CI esté verde.
Consultar [pendientes y evidencia](docs/IMPLEMENTATION_STATUS.md).

---

## Documento de auditoría original (baseline)

Baseline: 12 de septiembre de 2026. Informe emitido: 13 de septiembre de 2026. Código fijado al commit indicado y revalidado antes de entregar. Repositorio: [1621-nicolas/roommatch](https://github.com/1621-nicolas/roommatch). Rama auditada: `main`. Commit fijado: [8308ed4cde23](https://github.com/1621-nicolas/roommatch/commit/8308ed4cde239bbc03752df985269134d6de52bf).

Fase ejecutada: auditoría, sin correcciones del producto, cambios de dependencias, commits, merges ni ajustes de configuración del repositorio. Las propuestas y casos de prueba de estos documentos **no están implementados**. Evidencia estática y resultados de comandos se distinguen de pruebas dinámicas pendientes. No se certifica preparación para producción.


## Orden de trabajo

Primero resolver seguridad y hacer visibles las regresiones; después integridad, matching, administración, UX y operación. Mantener Angular 21 standalone, Java 17/Spring Boot 3.5.x y SQL Server. No hay evidencia que justifique migrar a Java 25, reemplazar stack, usar microservicios o añadir almacenamiento externo de imágenes ahora.

La auditoría entrega 45 hallazgos, matrices y casos de prueba. **No activa implementación de reglas no aprobadas.** Antes de esa fase: revisar decisiones siguientes; crear `audit/roommatch-hardening` desde main actualizado; conservar cambios ajenos y comprobar divergencias. No modificar main directamente ni hacer merge sin autorización. No cambiar protección/rulesets sin permiso específico.

## Decisiones que requieren negocio o arquitectura explícita

| ID | Pregunta real | Alternativas y costo/impacto | Propuesta para decidir; no aplicada |
| ---| ---| ---| ---|
| D-01 | ¿Qué significa vincular habitación a busco_compartir? | Referencia pública de interés permite a usuarios sin propiedad buscar compañía; autorización del dueño implica consentimiento/modelo extra; solo propia elimina el caso actual de usuario común | Conservar referencia si esa es la intención, con texto que no implique representación; exigir autorización si se ofrece en nombre del dueño. Esperar decisión |
| D-02 | ¿Cuándo puede reenviarse una solicitud y quién cancela? | Bloqueo permanente actual evita insistencia pero rompe reintentos; nuevo intento con cooldown conserva historial; reabrir misma fila simplifica pero pierde historia | Historial de intentos, un pendiente por par, cancelación de emisor pendiente y cooldown configurable. Definir duración tras rechazo/cancelación y revocación/bloqueo futuro |
| D-03 | ¿Conservar publicaciones eliminadas y por cuánto tiempo? | Hard delete borra historia; soft delete preserva auditoría pero requiere retención y purga | Soft delete con estado eliminada, no reactivable por usuario, imágenes no públicas; retención y purga explícitas. No fijar plazo legal sin requisitos |
| D-04 | ¿Qué cuenta para cupo y cómo se hace downgrade/expiry? | Activas+pausadas preserva creación actual; solo activas permite más borradores; bloquear downgrade vs selección de conservación vs período de gracia | Mantener activas+pausadas salvo decisión distinta; bloquear downgrade incompatible y permitir elegir qué conservar, sin borrar/pausar 7 habitaciones automáticamente. Definir permisos de destacar/estadísticas y vencimiento |
| D-05 | ¿Qué significa “compatibilidad” y qué criterios son excluyentes? | Índice de preferencias explicable vs probabilidad real que necesita datos; mascotas/fumar como conducta o tolerancia; presupuesto aporte individual o alquiler total | Aprobar 13 criterios, pesos provisionales, matrices y cobertura propuestos; ningún hard filter sin decisión. Calibrar después con datos consentidos |
| D-06 | ¿Cómo combina ADMIN la capacidad de propietario? | Preservar ADMIN y perfil propietario; rechazar alta admin; multirol requiere tabla/migración/guards más amplios | Preservar rol administrativo y usar perfil para capacidad; multirol solo ante necesidad futura. Confirmar política de alta antes de cambiar |
| D-07 | ¿Qué implica sanción, quién la levanta y qué transiciones de reporte existen? | Pausa comercial actual es reversible; bloqueo admin separado puede ser temporal/permanente; retención de historial/appeal amplía alcance | Estado comercial separado de moderación, actor/motivo/fecha y levantamiento admin explícito. Definir efectos sobre autor, vínculos y contactos |
| D-08 | ¿Búsqueda de distrito exacta o contiene? | Contains actual permite textos parciales y más scans; catálogo canónico exacto mejora consistencia/índices pero cambia búsqueda | Mantener búsqueda actual inicialmente; normalización/catálogo después de acordar experiencia. No cambiar filtro solo para hacer útil un índice |
| D-09a | ¿Duración de sesión, cierre remoto y transporte? | Bearer/localStorage requiere contención XSS; memoria cambia persistencia; cookie HttpOnly añade CSRF/CORS/ciclo de sesión | Endurecer Bearer primero; TTL y revocación según necesidad; cookie/refresh solo con justificación y pruebas |
| D-09b | ¿Qué canal se comparte al enviar interés en habitación? | Email de acceso automático actual; emailContacto con preferencia; consentimiento separado específico | Nunca divulgar email de login implícitamente; canal elegido/consentido con texto claro. Confirmar cómo conserva comunicación el propietario |
| D-09c | ¿Recuperación/verificación de email y pago son parte de esta entrega? | Hoy no existen: enlace recuperación es inerte y planes no tienen facturación | Priorizar corrección del enlace/UX y documentar simulación; implementar estos flujos solo si alcance aprobado, sin mails/pagos ficticios |

D-01 es especialmente importante: ownership de la publicación ya se comprueba; una referencia a habitación pública no necesariamente requiere ser dueño. No corregirla con `habitacion.propietario == autor` sin aceptar que cambia quién puede usar busco_compartir. Eliminar filtros de privacidad en otra ruta tampoco se justifica por hacer más fácil el contacto.

## Sprint 0 — Errores críticos, seguridad y baseline confiable

Objetivo: impedir despliegue con firma pública, cortar fugas concretas, proteger moderación básica y dejar tests/CI capaces de fallar ante regresiones.

| Grupo / commit sugerido | Archivos/capas afectados | Cambios propuestos | Gate |
| ---| ---| ---| ---|
| `test(web): repair application test setup` | app.spec, configuración tests si necesaria | Providers router y assertions reales; mantener tests significativos | 2 tests originales reparados + nuevo smoke; build |
| `test(api): establish security and privacy regression tests` | src/test; fixtures | SEC/PRIV/OWN mínimos que detecten problemas actuales | JUnit/MockMvc con Java 17 |
| `fix(security): require production signing key` | properties por perfil, JwtService, tests de startup | Sin fallback productivo; clave/TTL validados; conservar local explícito | SEC-01..05, CFG |
| `fix(privacy): remove account email from discovery` | MatchResponse/FavoritoResponse, interfaces TS, consumidores | No email ajeno en JSON/UI/consola; leads tras D-09b | PRIV-01..04, frontend contratos |
| `fix(security): enforce moderation visibility` | Habitacion/Reporte policy, state/migration mínima | Rechazar reactivación sancionada; separar bloqueo según D-07 | MOD-01/OWN-06, integración SQL |
| `fix(security): bound authentication abuse` | Auth/filter/policies, configuración, pruebas | Rate limit y política contraseña analizada; estado HTTP coherente | SEC-08/11/12 sin falsos bloqueos |
| `chore(ci): execute web tests and backend verify` | workflow, permiso del wrapper | Tests frontend obligatorios; verify backend; artefactos | CI rojo si test falla, no continue-on-error |
| `fix(database): align publication image schema` | Migración aditiva, model/tests SQL | principal y backfill seguros; sin DROP en BD existente | DB-01/IMG-01 en SQL Server |

Resolver acceso Maven/SQL de prueba para ejecutar gates; CI histórico verde no basta. Revisar advisories runtime high y preparar patches compatibles aislados, sin upgrades mayores. La estrategia de migración empieza aquí aunque su operación productiva se complete en Sprint 5.

Salida: producción no arranca con clave ausente/de ejemplo; emails de descubrimiento desaparecen; sanción no se revierte por dueño; pruebas críticas detectan regresiones; npm tests/build y Maven verify ejecutados, no solo intentados. Si D-07 aún falta, el grupo de sanción queda bloqueado por decisión documentada, no se inventa una regla.

## Sprint 1 — Integridad funcional y datos

Objetivo: transiciones, ownership, cupos y contratos consistentes bajo concurrencia.

| Grupo | Archivos/capas | Resultado y tests |
| ---| ---| ---|
| `fix(requests): define contact request lifecycle` | SolicitudContactoService/Repository/Controller, constraints, Solicitudes TS/modelos | D-02, cancelar, reenvío, historial; SOL-01..12 |
| `fix(contacts): enforce symmetric privacy and relation integrity` | ContactoUsuario/ContactoRoomie, DTO, Contactos TS | 32 combinaciones de flags, dueño propio, batch paginado y sin PII; PRIV/OWN |
| `fix(plans): apply quota and subscription policy` | HabitacionService, PlanPropietarioService, PropietarioService, UNIQUE SQL, páginas de propietario | D-04/06, caso 10 → 1 con 8, reactivar pausadas, expiry, destaque; PLAN/CON/ROLE |
| `fix(publications): enforce visibility and lifecycle` | PublicacionRoomie, ImagenPublicacion, DTO, UI, SQL | Detalle anónimo, D-01/D-03, visibilidad del vínculo y borrado; PUB/OWN |
| `fix(images): protect image invariants` | Servicios/repositorios de imágenes, constraints, gestor | Límite 5, principal/orden/concurrencia y URL; IMG |
| `fix(api): normalize domain errors and payload contracts` | Excepciones/advice/controllers, DTO/TS, validadores | 404/403/409/400 consistentes; dimensiones/precision/null alineados; HTTP/DTO/DB |

Los checks UNIQUE/FK existentes se preservan hasta tener migración aprobada que conserve su intención. No resolver reenviar borrando filas anteriores. No resolver downgrade borrando habitaciones. No convertir todas las relaciones a LAZY sin probar mapeo/consulta. Cambios de errores deben acompañarse de frontend para que ya no interprete todos los 400 como “sin perfil”.

Salida: cada invariante nueva tiene prueba de servicio y, si depende de BD, prueba SQL Server concurrente. Comandos de frontend/backend verdes después de cada grupo. Estados de negocio aprobados y reflejados en SQL/API/UI.

## Sprint 2 — Matching correcto, explicable y vigente

Objetivo: cumplir los 13 campos del perfil, explicar compatibilidad y evitar datos/rankings obsoletos.

1. `refactor(matches): isolate pure compatibility calculation`: extraer servicio puro sin cambiar resultados todavía; tests de caracterización del algoritmo actual.
2. `fix(matches): implement approved weighted compatibility`: pesos/matrices/fórmulas D-05, null/cobertura, explicaciones; MAT-01..16 y oráculo independiente de presupuestos.
3. `fix(matches): version cached results and rebuild valid ranking`: versiones de ambos perfiles+algoritmo, exclusión de inactivos y ranking antes de paginar; MAT-17..21.
4. `fix(publications): batch compatibility lookups`: IDs distintos, proyecciones/mapa de cache; página 10/20/50 con queries acotadas, equivalencia de DTO.
5. `perf(matches): bound candidate processing after measurement`: solo si datos justifican, batch, top-K o pool acordado; no aproximación silenciosa que elimine candidatos válidos.

Archivos: MatchService/MatchResultadoRepository/PerfilConvivenciaService, modelos/migraciones, PublicacionRoomieService, DTO e interfaces/clientes de matches/publicaciones. Métricas: consultas, CPU/heap, número de candidatos, cache hit, vigencia, tiempo p95 y ranking; no tomar el porcentaje del modelo como probabilidad de convivencia.

Salida: no hay lecturas de cache con versiones anteriores; actualizar cualquiera de los dos perfiles cambia cálculo cuando corresponde; cambio de algoritmo invalida resultados previos; tests exhaustivos y rendimiento medido sobre datasets declarados.

## Sprint 3 — Administración real

Objetivo: reemplazar placeholders por flujos conectados y moderación trazable.

| Grupo | Alcance |
| ---| ---|
| `feat(admin): connect dashboard metrics` | AdminService/modelos, 16 métricas reales del backend, definición de totales y fecha de actualización, loading/error/empty |
| `feat(admin): implement report queues and actions` | Reportes de usuario/habitación, filtros de servidor, paginación, revisar/rechazar/sancionar, confirmaciones, accesibilidad |
| `fix(reports): enforce transitions and moderation audit` | D-07, CAS/idempotencia, actor/motivo/fecha, historial, protección de métodos de seguridad |
| `feat(reports): connect user report submission` | ReportModal funcional, reutilizable, desde recursos pertinentes, validaciones y límites |

No inventar nombres, reportes, ingresos, roles extra ni resultados de pago. No agregar UI de rechazo que deje una sanción vigente sin explicación. Admin no obtiene acceso genérico a teléfonos privados por ser admin salvo una necesidad y permiso explícitos. Pruebas ADMIN-01..05/MOD y servicios frontend reales.

Salida: los datos mostrados provienen de API; filtros/acciones funcionan; usuario no admin recibe 403 servidor; no hay mocks visibles ni botones inertes; historial de decisión inspeccionable.

## Sprint 4 — Calidad, UX y accesibilidad

Objetivo: integrar imágenes, corregir filtros/contadores/errores y reducir duplicación preservando apariencia.

| Grupo | Archivos/capas | Validación |
| ---| ---| ---|
| `fix(web): use environment API configuration` | API config, todos los servicios, interceptor, build configs | Desarrollo con localhost funciona; API de producción correcta; no token fuera de origen |
| `fix(web): handle loading errors and global pagination` | Home/Matches/Favoritos/Solicitudes/Contactos/Notificaciones/Propietario | Error frente a vacío, retry, paginación 2+, métricas reales y no leídas globales |
| `fix(web): display published room and publication images` | Catálogos/detalles/modelos/proyecciones | Galerías reales, sin N peticiones HTTP por tarjeta, 404 y fallback/alt/HTTPS |
| `fix(profile): patch description without overwriting preferences` | MiCuenta, PerfilController/DTO/Service | PATCH acotado y control de versiones |
| `refactor(web): reuse shared UI contracts` | EmptyState/Pagination/CompatibilityBadge/ReportModal y tarjetas decididas | Funciones anteriores preservadas, reglas no duplicadas |
| `refactor(styles): scope styles without visual drift` | styles.css, app.css, CSS de feature/componente | Capturas antes/después; móvil/escritorio; warning CSS resuelto por reducción real |
| `fix(a11y): labels language focus and async announcements` | index.html, formularios, modales, navegación | WCAG 2.1 AA: teclado/lector/contraste/zoom + axe |
| `chore(web): remove confirmed unused duplicate model` | ead-habitacion-response y código sin uso que se decida retirar | Referencias/build/test; no borrar funcionalidades |

Lazy routes pueden entrar como commit independiente con medición de bundle. No migrar todos los formularios ni reemplazar Bootstrap sin beneficio probado. Revisar enlaces de recuperación/notificaciones según D-09c y pantallas existentes.

Salida: pantallas asíncronas con loading/error/empty/success, flujos integrados sin mocks, accesibilidad y responsive medidos, CSS con alcance claro y apariencia preservada.

## Sprint 5 — Producción y deployment

Objetivo: convertir configuración y datos en operación reproducible, sin asumir que compilar equivale a desplegar bien.

1. `chore(db): adopt reviewed migration history`: Flyway con baseline exacto, instalación nueva y upgrade de datos previos, correcciones hacia adelante y restore ensayado; sin clean/bootstrap peligroso en production.
2. `chore(deploy): define production runtime configuration`: secrets/TLS/CORS/Swagger/headers/APIURL, health/readiness sin filtrar información, pool/timeouts medidos, cuenta SQL mínima, backups y logs redactados. Elegir hosting con usuario; no desplegar sin autorización/objetivo concreto.
3. `chore(deps): apply reviewed compatible security patches`: inventario/SBOM Maven completo y npm triage; patches pequeños, changelogs/impacto/tests; no major automático.
4. `chore(ci): strengthen required gates and artifacts`: tests/coverage/lint con configuración real, audits, SQL Server con gates críticos, tiempos/retención, permisos mínimos; acciones según versiones compatibles.
5. Proponer y, **solo con permiso**, aplicar protección de main: PR, revisión, checks reales requeridos, bloqueo de fallidos, restricciones de push/bypass consciente. No mezclar PR#2/#3 por título.
6. `docs: document verified RoomMatch behavior and operations`: actualizar README únicamente con comandos probados, arquitectura real, decisiones aprobadas, migraciones, límites del producto, workflows y runbooks.

El baseline de una BD existente debe distinguirse de las migraciones baseline para instalaciones nuevas que describe el [tutorial oficial de Flyway](https://documentation.red-gate.com/flyway/reference/tutorials/tutorial-baseline-migrations). La propuesta exige inventario y reconciliación antes de marcar versiones; no activar baselineOnMigrate para esconder diferencias. La configuración exacta y el módulo SQL Server compatibles deben validarse al implementar.

Salida: despliegue de ensayo con datos no reales, secreto requerido, TLS validado, migraciones sin pérdida, restore robusto, smokes/E2E, escaneos analizados y evidencia de los checks. Publicación/merge/protección de rama conservan sus autorizaciones explícitas; ningún cambio así se realizó en auditoría.

## Protocolo por grupo y commits

1. Exponer qué cambia, por qué y archivos afectados; indicar decisión D aprobada cuando corresponda.
2. Implementar en rama aislada desde main revalidado; un problema coherente por grupo.
3. Añadir test que falle con el comportamiento defectuoso y pase con corrección, más controles positivos para preservación de funciones.
4. Backend tests/verify; frontend tests/build; SQL Server/E2E según riesgo. Reportar errores, warnings y comprobaciones no ejecutadas honestamente.
5. Revisar diff/contratos/SQL/seguridad/UX y resultados; commit semántico pequeño con evidencias.
6. PR con problema, cambio, resultado, riesgos y pruebas. No hacer 50 cambios en un commit; no merge sin autorización.

No fijar semanas exactas sin disponibilidad/capacidad del equipo. Los sprints son paquetes de alcance y gates, no promesas de duración. Se pueden adelantar acciones técnicas independientes (p.ej. setup tests o DTO privacidad) una vez se pase formalmente a implementación; decisiones de negocio bloquean solo sus grupos correspondientes.

## Matriz de terminación

| Condición pedida | Estado auditado | Evidencia necesaria para cerrarla |
| ---| ---| ---|
| Frontend compila | Sí con warning | Build sin regresiones y presupuesto revisado |
| Backend compila | CI histórico sí; local bloqueado | clean verify actual reproducible |
| Tests pasan | No: frontend 2 fallidos; backend solo 1 smoke CI | Suites nuevas verdes e informes |
| CI pasa y cubre mínimo | CI verde pero incompleto | Backend verify + frontend build/tests ejecutados |
| Reglas críticas probadas | No | SEC/MAT/OWN/PRIV/SOL/PLAN/MOD/DB del plan |
| Sin P0 | No bajo fallback desplegable | Configuración de producción rechaza clave pública/ausente |
| P1 resueltos/justificados | No | Cada ficha con fix o excepción concreta/plazo/responsable |
| Seguridad revisada | Estática sí; dinámica parcial | Pruebas/scan/entorno productivo verificados |
| Frontend y backend integrados | Parcial | Admin/galerías/rutas/contratos reales |
| DTO y BD alineados | No | Matriz resuelta+SQL Server tests |
| Admin funciona | No | Acciones/métricas reales y permisos |
| Matches vigentes | No | Versiones, ranking y pruebas concurrentes |
| Ownership protegido | Parcial; varios controles correctos | Matriz de IDs y decisiones de vínculo/sanción cerradas |
| Sin mock visible | No | Admin y placeholders conectados o alcance documentado |
| Documentación real | Parcial | README actualizado después de verificar correcciones |

La entrega actual termina la fase documental de auditoría con límites expresos. **RoomMatch como producto continúa abierto y no se considera terminado.**
