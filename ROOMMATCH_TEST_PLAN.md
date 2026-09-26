# ROOMMATCH — Plan de pruebas y baseline de calidad

## Ejecución actual y límites

El CI de la rama ejecuta `./mvnw --batch-mode clean verify -Psqlserver`, con
169 pruebas unitarias y 18 de integración, sin fallos ni omisiones en la
última ejecución comprobada. La integración incluye SQL Server Testcontainers,
no se sustituye por H2. Publicaciones: 4 consultas por página de 10/20/50;
contactos: 3 consultas para esos mismos tamaños.

El frontend ejecuta `npm ci`, `npm audit --audit-level=high`, build y tests.
Las pruebas alcanzaron 69 casos tras renovar acceso/registro, confirmados en
el [CI 35490022064](https://github.com/1621-nicolas/roommatch/actions/runs/35490022064).
Ver el estado más reciente del PR para el gate exacto de cada commit. No se interpreta el conteo como porcentaje de cobertura.

Hay pruebas de seguridad, matching, concurrencia SQL, privacidad, planes,
moderación y contratos/estados frontend. Faltan pruebas de navegador de los
flujos completos, accesibilidad asistida y despliegue real. Maven local sigue
bloqueado por resolución del repositorio, por lo que su resultado no se
presenta como una ejecución local aprobada.

**El catálogo siguiente conserva los casos propuestos en la auditoría inicial.**
Que un caso aparezca listado no significa que exista una prueba implementada;
los archivos de test y los resultados de CI son la evidencia de ejecución.

---

## Documento de auditoría original (baseline)

Baseline: 12 de septiembre de 2026. Informe emitido: 13 de septiembre de 2026. Código fijado al commit indicado y revalidado antes de entregar. Repositorio: [1621-nicolas/roommatch](https://github.com/1621-nicolas/roommatch). Rama auditada: `main`. Commit fijado: [8308ed4cde23](https://github.com/1621-nicolas/roommatch/commit/8308ed4cde239bbc03752df985269134d6de52bf).

Fase ejecutada: auditoría, sin correcciones del producto, cambios de dependencias, commits, merges ni ajustes de configuración del repositorio. Las propuestas y casos de prueba de estos documentos **no están implementados**. Evidencia estática y resultados de comandos se distinguen de pruebas dinámicas pendientes. No se certifica preparación para producción.


## Estado actual comprobado

| Capa | Pruebas existentes | Resultado | Qué no demuestran |
| ---| ---| ---| ---|
| Backend | RoommatchApiApplicationTests.contextLoads | CI del SHA auditado: 1 pasada, 0 fallos, 0 errores, 0 omitidas. Local bloqueado antes de compilar por Maven | Seguridad, controllers, matching, negocio, ownership, SQL real |
| Frontend | app.spec: should create the app; should render title | 2 fallidas, 0 omitidas; NG0201 ActivatedRoute | Ningún contrato de servicio/guard/UI. Aserción Hello, roommatch-web también desactualizada |
| Integración SQL | Ninguna | No ejecutada; sin SQL Server/Docker disponible | DDL, constraints reales, collation, locks, índices, precision |
| E2E/accesibilidad | Ninguna configurada | Revisión estática; Chromium no disponible y descarga falló | Navegación integrada, contraste, foco/render/responsive |
| Coverage | Sin informe ni configuración JaCoCo/coverage | Desconocida | No se informa 0% ni 100% por intuición |

Baseline completo y warnings están en ROOMMATCH_AUDIT.md. Los IDs siguientes son **casos diseñados para implementar**, no una suite creada o resultados verdes. Respetan la instrucción de auditar antes de cambiar código. No eliminar contextLoads ni rebajar assertions para conseguir CI verde; repararlo y añadir pruebas que realmente detecten regresiones.

## Estrategia por nivel

| Nivel | Herramienta compatible con stack | Alcance | Frecuencia |
| ---| ---| ---| ---|
| Unitario puro | JUnit 5 y assertions del starter-test | Algoritmo puro, políticas de cupo, transiciones, validación | Cada grupo/PR; rápido, sin BD |
| Servicio | Mockito/JUnit 5 | Orquestación, principal→ownership, datos entregados a repositorios | Cada PR; no confundir mock con integridad SQL |
| HTTP/seguridad | MockMvc con cadena real; spring-security-test | 401/403, claims, JSON, permisos, errores, DTO privado/público | Cada PR que toque API o seguridad |
| Persistencia real | SQL Server Testcontainers y migraciones reales | FK/UNIQUE/CHECK, DECIMAL, fechas, filtros/orden, transacciones/carreras | Gate para cambios BD/seguridad/negocio; ampliable a todos los PR según tiempo |
| Frontend unitario/integración de servicios | Angular TestBed + Vitest; provideHttpClientTesting, HttpTestingController y router de prueba | URL/env/interceptor, payloads/status, formularios/estados/guards | Cada grupo/PR junto con build |
| E2E seleccionado | Navegador con frontend+API+SQL Server desechables | Flujos críticos entre dos usuarios y admin | Smoke en PR relevantes; suite completa previa a release |
| Accesibilidad/visual | axe como apoyo + teclado/lector/contraste/capturas | WCAG 2.1 AA objetivo; preservar diseño | Cambios UI y revisión de release |
| Performance | Hibernate Statistics/datasource proxy + SQL actual plans + dataset sintético | Consultas, memoria, IO, ranking/latencia | Antes/después de optimización, no cronómetro como test unitario frágil |

Testcontainers tiene módulo SQL Server y requiere aceptación de la licencia de su imagen. Elegir tag/digest acorde al servidor objetivo, Java 17 compatible y runner con memoria/Docker; no dar por configurado ese entorno. [Documentación oficial del módulo SQL Server](https://java.testcontainers.org/modules/databases/mssqlserver/).

## Fixtures y contrato de aserciones

Usar A/B/C activos con perfiles completos, S suspendido, ADMIN y propietario P; contraseñas únicas ficticias. A/B nunca representan usuarios reales. Habitaciones Ha/Hb, publicaciones Pa/Pb, reportes y leads pertenecen a actores distintos. Planes de fixture con límites 1 y 10 y permisos de destacar opuestos; el seed actual solo crea Gratis. Reloj fijo, datos con IDs recuperados al insertar (sin presumir IDs identity), fechas explícitas, presupuesto monetario exacto y restauración aislada por caso.

En cada rechazo de escritura comprobar **status + cuerpo + ausencia de efectos en BD + ausencia de notificación/contacto**. En cada autorización comprobar control positivo para el dueño legítimo; no cubrir ownership únicamente esperando errores. Capturar SQL de forma que no registre PII de datos reales. Fixtures concurrentes deben tener transacciones independientes y barreras/latches, no sleeps que “a veces” producen una carrera.

Contrato objetivo pendiente de implementación: 401 no autenticado, 403 sin permiso, 404 no existe/no visible según política, 409 conflicto, 400 payload inválido, 429 límite, 503 BD. Tests de caracterización deben reflejar primero el defecto actual si es necesario y luego demostrar la corrección; no reescribir expectativa a 400 para ocultar el error.

## Seguridad y configuración: primera prioridad

| ID | Caso | Resultado que debe garantizar la corrección |
| ---| ---| ---|
| SEC-01 | Startup production sin JWT_SECRET, vacía, corta o literal de ejemplo; development/test explícitos | Production falla antes de aceptar tráfico; local/test funcionan con su clave; no secreto en log |
| SEC-02 | Token válido; firma alterada; clave de otro entorno; algoritmo no permitido; token sin firma | Solo válido admitido; 401 para API protegida; nunca auth por payload decodificado |
| SEC-03 | exp faltante, pasada, igual a now, futura, TTL negativo/excesivo y skew configurado | Contrato definido, sin 500 por token inválido, límites deterministas con Clock |
| SEC-04 | Claims issuer/audience/sub inválidos; subject de usuario inexistente | 401 sin NPE ni datos internos; no aceptar token de otro servicio |
| SEC-05 | Token de fixture firmado con fallback conocido | Production no inicia con ese valor; firma antigua rechazada tras rotación |
| SEC-06 | Swagger y docs sin login en development/production | Público local; deshabilitado/restringido según decisión productiva; ADMIN legítimo cuando aplique |
| SEC-07 | Usuario suspendido después de emitir token; cambio de rol posterior | Suspensión bloquea siguiente petición; autoridad viene de BD y se conserva comportamiento normal de propietario |
| SEC-08 | 401 frente a 403 con cadena real; token inválido en recurso público/protegido; BD falla al cargar usuario | Contrato explícito, sin redirección HTML; infraestructura no encubierta como credencial incorrecta |
| SEC-09 | Logout cliente y reutilización de copia del token; rotación/tokenVersion si se adopta | Riesgo residual documentado antes del cambio; revocación real después según D-09 |
| SEC-10 | Claims de respuesta y token; cambiar nombres/apellidos | Nombres/apellidos innecesarios fuera del JWT; no confundir rol de cliente con autorización |
| SEC-11 | Rate limit login/register/matches/reportes/solicitudes, ventana y reloj | 429/Retry-After, recuperación al vencer ventana, IP confiable y controles por actor/par |
| SEC-12 | Contraseñas cortas, largas, Unicode de 2/4 bytes, espacios, pegar, comunes y BCrypt 72 bytes | Política consistente cliente/servidor; nunca truncado silencioso ni hash de contraseña distinta |
| SEC-13 | URL http/https/javascript/data/file, host inválido, credenciales en URL, exceso de 255, 404 remoto | Validación de API/presentación según política; ningún fetch backend arbitrario |
| CFG-01 | Matriz development/test/production e import local | Configuración local no contamina production; URL/clave/Swagger coherentes |
| CFG-02 | SQL certificado válido/no confiable/hostname incorrecto y usuario limitado | Production valida TLS y mínimo privilegio; desarrollo local preservado |

Test adicional del filtro: downstream lanza excepción al procesar un token inválido; comprobar que chain.doFilter se invoca exactamente una vez y no reintenta una operación de negocio. El riesgo se encontró en estructura try/catch, aún no reproducido.

## Ownership y privacidad

| ID | Casos | Aserciones |
| ---| ---| ---|
| OWN-01 | Todos los endpoints con IDs de la matriz de seguridad: cambiar A→B/C | Se valida propiedad/rol en servidor; no efectos sobre el recurso ajeno |
| OWN-02 | Editar/pausar/activar habitación; imagen por idImagen ajeno | ID+propietario protegidos; control positivo del dueño; moderación y cuota no se evaden |
| OWN-03 | Editar/pausar/activar/cerrar/borrar publicación e imágenes ajenas | ID+autor protegidos; soft/hard según D-03; IDs en cuerpo no elevan privilegios |
| OWN-04 | idLead de otro propietario; idSolicitud aceptada por emisor/C; idNotificacion ajena | Solo dueño del lead/receptor de solicitud/dueño de notificación; sin notificación lateral |
| OWN-05 | idHabitacion en publicación propia: propia/ajena/autorizada/inactiva/inexistente | Resultado según D-01, no asumir que public=authorized |
| OWN-06 | Recursos pausados/eliminados/sancionados y autor suspendido en endpoints públicos/imágenes | Política efectiva uniforme; privado propietario permite gestionar lo autorizado |
| PRIV-01 | A/B sin contacto y B mostrarEmail=false; agregar favorito/calcular/listar | Ningún email de acceso de B en JSON de descubrimiento ni HTML/consola |
| PRIV-02 | Dueño / A acepta a B / B acepta a A / pendiente / rechazado / cancelado / C; 32 combinaciones de 5 flags | Dueño ve todos sus datos; autorizado ve exactamente flags true; tercero no ve contacto; null no se cambia a cadena accidental |
| PRIV-03 | Logs/consola con email/teléfono/token/password centinela | No aparecen secretos/PII innecesaria; sí correlationId/evento técnico seguro |
| PRIV-04 | Lead con preferencias privadas y consentimiento elegido | Solo canales/valores aprobados en D-09; no filtración implícita del email de login |

En PRIV-02, recorrer las 32 combinaciones con y sin relación y direcciones inversas. Incluir propietario consultando /me cuando todos sus flags son false; la ruta /desbloqueado/self puede rechazar sin impedir acceso propio. Probar que una relación histórica no autoriza a C y que un idContacto fabricado no selecciona otro usuario. Definir efecto de suspensión/revocación antes de cambiar expectativas.

## Matching: exhaustividad sobre dominios pequeños y propiedades

La propuesta de 13 criterios/fórmulas/pesos está en el informe principal. Tests deben usar versión/configuración aprobadas y comprobar cada contribución, score, cobertura y explicación. No fijar porcentajes arbitrarios que solo copien la implementación. Separar fixtures de comportamiento actual de las expectativas aprobadas de v2.

| ID | Dominio / ejemplo | Resultado esperado |
| ---| ---| ---|
| MAT-01 | Perfil completo idéntico | 100 y cobertura 100%; contribuciones suman 100; cada criterio explica coincidencia |
| MAT-02 | Cada criterio cambiado por separado con los demás fijos | Solo esa contribución cambia, incluidas alcohol/gastos/fecha antes ignoradas |
| MAT-03 | Limpieza: los 25 pares de 1..5 | `1−abs(a−b)/4`; 4/5=0,75 y 1/5=0; monotonía en distancia |
| MAT-04 | Ruido: 25 pares; sociabilidad: 25 pares | Mismas propiedades sin depender de orden A/B |
| MAT-05 | Escalas 0,6,null; texto vacío/desconocido/normalizado | Datos inválidos rechazados en entrada; legacy desconocido reduce cobertura, no coincidencia |
| MAT-06 | Presupuestos iguales, disjuntos, contenidos y parcialmente superpuestos | Fórmula Jaccard aprobada; ejemplos del informe calculados con enteros/BigDecimal |
| MAT-07 | Presupuesto fijo 0/0,800/800; punto dentro de intervalo; extremos tocándose | Sin división por 0; identidad 1, disjuntos 0; intersección de 1 céntimo tratada explícitamente |
| MAT-08 | Min>max, negativos, null parcial, más de 2 decimales, máximos DECIMAL(10,2) | Validación de API; ninguna overflow ni conversión float inadvertida |
| MAT-09 | Fechas 0/15/30/60/61 días, invertidas, año bisiesto, null y pasada | 1/0,75/0,5/0/0 con horizonte 60 provisional; simetría y política de fechas pasadas |
| MAT-10 | Horario 4×4 y visitas 3×3 | Recorrer toda matriz aprobada; misma categoría 1; parcial definido, no inferido por texto |
| MAT-11 | Mascotas/fumar/alcohol 2×2; gastos 2×2 | Recorrer cada matriz; no ignorar campos; semántica de tolerancia según D-05 |
| MAT-12 | Convivencia 4×4 | 16 entradas de matriz aprobada, simétrica y explicaciones consistentes |
| MAT-13 | Sin datos, solo distrito, falta cada criterio y combinaciones críticas | null/cobertura 0 si nada; solo distrito score 100 pero cobertura 14% y “insuficiente”; no subirlo al primer lugar como 100% fiable |
| MAT-14 | Perfiles aleatorios válidos y permutaciones A/B | Simetría, score 0..100, cobertura 0..1, determinismo y no mutación de argumentos |
| MAT-15 | Casos cerca de redondeo x.xx5 y contribuciones pequeñas | BigDecimal HALF_UP a 2 decimales solo al final; no redondeos acumulados/NaN |
| MAT-16 | Explicaciones y versión | Peso, score parcial, criterio faltante y aporte verificables; suma coincide con índice; sin email/PII extra |
| MAT-17 | Calcular A/B; actualizar A; actualizar B; cache inverso | Ninguna lectura retorna versiones anteriores como vigentes |
| MAT-18 | Cambiar solo algorithmVersion/pesos | Cache previo invalidado aunque perfiles no cambien |
| MAT-19 | Barrera entre lectura de perfiles y guardar resultado; perfil cambia concurrentemente | No publicar resultado viejo como actual; descartar/reintentar con límite |
| MAT-20 | Suspender/eliminar B después de cache; nuevos candidatos y empates | B fuera del ranking efectivo; orden estable; candidato nuevo puede entrar donde corresponda |
| MAT-21 | Ranking cambia fuera de primera página tras edición | Orden/filtrado global correcto antes de paginar; no renovar solo página visible |

Para MAT-06/07 recorrer exhaustivamente pequeños intervalos enteros 0..10 y comparar con una implementación de referencia por conjuntos de céntimos, independiente de la fórmula optimizada. En dominio reducido hay 66 intervalos válidos y 4.356 pares, suficientes para detectar errores de inclusividad/división. No crear miles de pruebas de contexto Spring para ello: son pruebas unitarias puras parametrizadas. Se pueden añadir propiedades con semilla reproducible, sin introducir una biblioteca nueva si JUnit basta.

## Solicitudes, contactos, planes y concurrencia

| ID | Secuencia | Invariante |
| ---| ---| ---|
| SOL-01 | A envía a B | 1 pendiente, actor A, notificación B; sin contacto |
| SOL-02 | B receptor acepta | 1 aceptada, fechaRespuesta, 1 contacto simétrico y notificación A |
| SOL-03 | B rechaza | Rechazada sin contacto; fecha y notificación según contrato |
| SOL-04 | A intenta aceptar su envío o C acepta | 403/sin cambios |
| SOL-05 | A envía a B; B rechaza; pasa cooldown; A envía otra vez | Nuevo intento según D-02; historial anterior intacto |
| SOL-06 | A envía a B; A cancela; luego B envía a A | Cancelación solo emisor pendiente; nuevo intento permitido según cooldown acordado |
| SOL-07 | Reenviar durante pendiente o tras contacto aceptado | 409/idempotencia definida; nunca duplicar vínculos |
| SOL-08 | Auto-solicitud, usuario inactivo/inexistente, mensaje 500/501 | Validación/ownership; sin escritura |
| SOL-09 | A→B y B→A concurrentes | Como máximo 1 pendiente por par canónico |
| SOL-10 | Dos aceptar simultáneos | 1 contacto, 1 evento lógico; segunda idempotente o 409 documentado |
| SOL-11 | Aceptar vs rechazar concurrentes | Solo una transición gana; si rechazo gana, 0 contactos |
| SOL-12 | Aceptar vs cancelar / rollback al crear notificación | Atomicidad completa, sin estado a medias |
| PLAN-01 | Límite 1 con 0/1 habitaciones computables | Primera permitida, segunda rechazada; definición computable acordada |
| PLAN-02 | Plan 10 con 8→plan 1 | Bloqueo o selección de conservación según D-04; no borrado automático |
| PLAN-03 | Pausadas→downgrade→activar todas | Nunca exceder cupo efectivo ni evadir selección anterior |
| PLAN-04 | Activa/pausada/alquilada/eliminada en count | Semántica explícita de qué consume cupo; cliente coincide con servidor |
| PLAN-05 | fechaFin pasada/igual a now/futura, estado activo/vencido/cancelado | Entitlement efectivo por reloj/estado; no aceptar solo string activo |
| PLAN-06 | Destacar con/sin permiso; downgrade con destacadas previas | Permiso consistente y reconciliación aprobada |
| PLAN-07 | Reactivar habitación sancionada | Denegado aunque haya cupo |
| PLAN-08 | Dos altas con una plaza libre | Solo una confirmada; BD/lock evita sobrecupo |
| PLAN-09 | Dos cambios de plan simultáneos | Como máximo 1 suscripción activa y decisiones serializables |
| PLAN-10 | Upgrade/downgrade mismo plan/inactivo/inexistente y plan con estadísticas | Status coherente; no cobro ficticio; permisos en servidor |
| ROLE-01 | Alta propietario desde USUARIO/PROPIETARIO/ADMIN | Rol/capacidad según D-06; ADMIN no se pierde accidentalmente |
| ROLE-02 | Propietario usa perfil/favoritos/solicitudes; ADMIN sin perfil visita panel | Funciones normales preservadas y error/flujo de perfil claro |
| CON-01 | Registro email equivalente concurrente | UNIQUE real impide duplicado; error consistente, no 500 |
| CON-02 | Favorito duplicado concurrente | 1 fila por par; respuesta idempotente/conflicto definida |
| CON-03 | Primer perfil/contacto/propietario concurrente | 1 por usuario; sin doble notificación/rol incoherente |
| CON-04 | Lead duplicado concurrente | 1 por habitación/interesado y respuesta correcta |
| CON-05 | Suscripciones y cupos | Casos PLAN-08/09 sobre SQL Server, no repositorio mock |
| CON-06 | Principal de imagen y límite 5 | Casos IMG con transacciones independientes |

En migraciones de par canónico, probar constraints con inserts SQL directos además del servicio. No marcar cooldown arbitrario como aprobado; congelar valores de D-02 en fixtures una vez decididos. Retrying automático no debe duplicar mensajes o notificaciones.

## Publicaciones, imágenes y moderación

| ID | Casos clave | Resultado |
| ---| ---| ---|
| PUB-01 | Listado/detalle anónimo y autenticado | Activa visible; anónimo sin score/claims privados; autenticado con score vigente |
| PUB-02 | Pausada/cerrada/eliminada; autor suspendido | Ocultas públicamente según policy; dueño mantiene gestión permitida |
| PUB-03 | Habitación vinculada pasa a inactiva/sancionada | No seguir publicando datos o representación no autorizada |
| PUB-04 | Tres tipos de publicación, vínculos roommatch/externa/null, presupuestos parciales | Validación cruzada de combinación, texto y moneda; nulls coherentes |
| PUB-05 | Borrado publicación con imágenes y referencias | D-03 respeta FK/historial; no borrar datos fuera del agregado |
| PUB-06 | Intentar reactivar eliminada/cerrada o editar con versión vieja | Transiciones permitidas explícitas; conflicto apropiado |
| IMG-01 | Base+002 y migración de principal | Columna presente; CRUD funciona contra SQL Server real |
| IMG-02 | 0 a 5 imágenes; sexta y dos altas desde 4 | Máximo 5 incluso concurrente |
| IMG-03 | Primera imagen y cambio de principal | Una principal según contrato; operación idempotente |
| IMG-04 | Borrar principal con otras; borrar última | Sucesora determinista o ninguna si vacía; habitaciones/publicaciones coherentes |
| IMG-05 | Dos principales simultáneas y selección ya principal | UNIQUE/CAS+contexto JPA correctos; nunca 0 involuntario o 2 |
| IMG-06 | Orden nulo/0/negativo/duplicado; propiedad ajena | Defaults/validación consistente; orden estable con desempate id |
| IMG-07 | URLs y tamaño; JSON masivo limitado | Ver SEC-13; sin 500 por SQL CHECK de dato inválido |
| IMG-08 | Catálogo principal/galería real, imagen 404 | UI usa dato real, fallback, alt y sin N peticiones HTTP por tarjeta |
| MOD-01 | Sancionar habitación y activar como dueño | Sanción perdura hasta acción admin permitida |
| MOD-02 | Levantamiento y estado comercial previo | Recuperación acorde a decisión, sin activar automáticamente lo que estaba pausado voluntariamente |
| MOD-03 | Suspender usuario y consultar todos sus recursos/caches | Policy de visibilidad efectiva uniforme |
| MOD-04 | Revisar/rechazar/sancionar sobre cada estado previo | Máquina de estados coherente; no reporte rechazado con sanción inexplicada |
| MOD-05 | Dos revisores/reintento de sanción | CAS/idempotencia y audit trail sin eventos contradictorios |
| MOD-06 | Abuso de reportes, duplicados y autoinforme | Límite/validación; conservar reporte legítimo y razones |
| ADMIN-01 | Usuario normal accede dashboard/reportes/acciones | 403 backend; no basta guard |
| ADMIN-02 | Métricas de dataset conocido | Contadores exactos con definición de totalMatches dirigido; sin datos de pago inventados |
| ADMIN-03 | Filtros/paginación estado/tipo de reporte | Servidor y UI coinciden, empty legítimo y error diferenciado |
| ADMIN-04 | Revisar/rechazar/sancionar/cancelar confirmación | Acción única y refresco de datos; no escritura al cancelar |
| ADMIN-05 | Loading, error, empty, success en admin | Sin nombres/fechas ficticias ni botones inertes |

## SQL, contratos, frontend y rendimiento

| Familia | Casos definidos |
| ---| ---|
| DB-01/02 | Fresh install y esquema existente reconciliado; cada entidad valida tabla/columna/tipo; diferencia principal detectada; callbacks/defaults |
| DB-03/04 | Límites VARCHAR/DECIMAL, nulls, constraints de escalas/edad/presupuesto; Unicode/acentos/collation y normalización email |
| DB-05/06 | FK/cascades/borrado, UNIQUE de parejas/usuario; estados imposibles y consistencia de vínculo vivienda |
| DB-07 | Migraciones append-only, base sin DROP para upgrade, checksum y corrección hacia adelante; ensayo de restore con datos de prueba |
| DTO-01/02 | Contratos JSON públicos/privados/request; propiedades extra rol/estado ignoradas o rechazadas según contrato; tipos/null de interfaces TS |
| HTTP-01/02 | Matriz 401/403/404/409/400/429/503, envelope; validación de campos, JSON malformado y tipo de parámetro incorrecto |
| HTTP-03/04 | Error interno sin stack/PII; paginación size 1,máximo, máximo+1,0,negativo, page extrema; valores de filtros inválidos |
| PERFIL-01/02 | Crear/editar perfil completo con validación; PATCH de descripción solo cambia ese campo, versión protege presupuesto/hábitos |
| WEB-01/02 | AuthService/interceptor/guards: URLs dev/prod, token solo API, login sin Bearer, 401/403/503, sesión expirada y rol local manipulado |
| WEB-03/04 | Detalle público y retorno a login; galerías reales y fallback sin fugas |
| WEB-05/06 | Más de una página, filtros, orden y contadores globales y panel con dataset superior a 50/100/1.000 |
| WEB-07/08 | Carga parcial de Home/contactos, error 503 frente a ausencia, retry; rutas de notificación existentes |
| WEB-09/10 | CSP/URL externa/link seguro y ciclo de sesión; formularios con error por campo y guardado sin overwrite |
| WEB-11/12 | Apariencia tras CSS/componentes y lazy routes, recarga deep-link, móvil/escritorio; bundle comparado |
| A11Y-01/02 | lang=es, estructura/labels/errores asociados, nombres accesibles y estado anunciado |
| A11Y-03/04 | Modales: foco inicial, trampa, Escape, retorno; teclado y botones no solo color/icono |
| A11Y-05/06 | Contraste AA real, zoom 200/400%, 390/1440 px, lector y navegación; axe como apoyo |
| PERF-01/02 | N=100/1.000/10.000; memoria, lecturas/escrituras, ranking y dataset 100.000 solo tras proteger recursos |
| PERF-03 | Páginas de publicaciones de 10/20/50,hit directo/inverso/miss/autores repetidos/anónimo; consultas acotadas por batch |
| PERF-04/05 | Planes de consulta e índices ligados a filtros reales; paginación estable y límites |
| PERF-06 | 16 agregaciones de dashboard y definición de contadores; comparar mejora sin cache obsoleta |
| DEP-01/02 | npm/Maven advisories por versión/alcanzabilidad; scan antes/después y lock sin upgrades mayores implícitos |
| CI-01/02 | Tests se ejecutan realmente, reportes, señal de fallo y checks requeridos bajo autorización de settings |

H2 no reproduce de forma suficiente T-SQL/GO, filtered indexes, SQL Server UNIQUE/NULL y collation, modos de bloqueo/deadlocks, precisiones/overflow/truncamiento, dialect/paginación ni esquema manual con missing columns. Puede conservarse como smoke rápido; las invariantes que dependen de SQL Server van contra ese motor. `ddl-auto=create-drop` de H2 no es prueba de que base+002 sean compatibles.

## Ejecución por grupo y criterios de salida

Cada grupo pequeño debe explicar propósito/archivos, implementar en rama de auditoría, añadir/regenerar los tests necesarios, ejecutar backend tests, frontend tests y build, revisar regresiones, mostrar resultados y crear commit semántico. Comandos objetivo tras resolver configuración/permiso:

```bash
# En roommatch-api/roommatch-api
./mvnw --batch-mode clean test
./mvnw --batch-mode clean verify
```

```bash
# En roommatch-web
npm ci
npm test -- --watch=false
npm run build
```

En CI puede bastar clean verify en backend, porque incluye test, evitando ejecutarlos dos veces sin beneficio. Local por grupo se sigue el contrato acordado; no atribuir verify verde a un clean test previo. Cambios de esquema/ownership/estado activan SQL Server y pruebas concurrentes. Si el entorno vuelve a bloquear Maven/containers, registrar bloqueo y no declarar el grupo verificado.

Cobertura: medir líneas/ramas para descubrir huecos, revisar manualmente ramas críticas de seguridad y transiciones; no imponer 100% global. Priorizar casos que fallarían antes de la corrección y cubran efectos reales. No escribir tests de getters, plantillas vacías ni assertions que repitan exactamente el algoritmo sin oráculo independiente.

Salida de fase de implementación: builds y suites verdes; CI completo; ninguna fuga de email/ownership ni sanción/cupo evadible; cache actual; esquema SQL real compatible; casos concurrentes garantizados; admin integrado; flows sin mocks; accesibilidad/UX verificadas y README con comandos reales. Esta auditoría todavía no cumple esa salida y no la presenta como lograda.
