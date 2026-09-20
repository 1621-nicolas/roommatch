# ROOMMATCH — Seguridad, privacidad y revisión OWASP

## Actualización de seguridad — rama de correcciones

Ya se exige clave de producción, separación de perfiles y TLS SQL validado.
JWT usa HS256, issuer/audience, identidad numérica y TTL limitado; el filtro
consulta rol y estado actuales. Swagger solo puede habilitarse en desarrollo.
Hay autorización administrativa en servicios, políticas de ownership,
controles de abuso y proxies de confianza explícitos. Se retiró el correo de
acceso de los DTO de descubrimiento y de los leads no consentidos.

La sesión sigue utilizando Bearer en localStorage: no se ha migrado a cookies
ni añadido refresh por defecto. Un token robado puede seguir siendo válido
hasta su expiración; cerrar sesión es local. El rate limit vive en una
instancia y se reinicia con ella. El CSP y reverse proxy son configuración de
despliegue propuesta, no evidencia de un servidor público ya protegido.

`npm audit` informó cero avisos tras los parches compatibles; el análisis
transitivo de vulnerabilidades Maven sigue pendiente. Ver
[dependencias](docs/DEPENDENCIES.md), [despliegue](deployment/README.md) y
[estado de implementación](docs/IMPLEMENTATION_STATUS.md).

**El informe OWASP siguiente es la evidencia histórica del baseline**, con sus
clasificaciones originales. No se atribuyen incidentes reales ni una
certificación de seguridad a las pruebas automatizadas.

---

## Documento de auditoría original (baseline)

Baseline: 12 de septiembre de 2026. Informe emitido: 13 de septiembre de 2026. Código fijado al commit indicado y revalidado antes de entregar. Repositorio: [1621-nicolas/roommatch](https://github.com/1621-nicolas/roommatch). Rama auditada: `main`. Commit fijado: [8308ed4cde23](https://github.com/1621-nicolas/roommatch/commit/8308ed4cde239bbc03752df985269134d6de52bf).

Fase ejecutada: auditoría, sin correcciones del producto, cambios de dependencias, commits, merges ni ajustes de configuración del repositorio. Las propuestas y casos de prueba de estos documentos **no están implementados**. Evidencia estática y resultados de comandos se distinguen de pruebas dinámicas pendientes. No se certifica preparación para producción.


## Dictamen

La principal debilidad de autenticación es el fallback público de JWT (RM-01, P0 si se despliega con él); la principal fuga de datos confirmada es el email de acceso ajeno en DTO de matches/favoritos (RM-02, P1). La reactivación de una habitación sancionada elude moderación (RM-17, P1). No se comprobó una intrusión, SQL injection explotable ni robo de token en un despliegue real. Los hallazgos se sustentan en el commit fijado, baseline y advisories de versiones; las pruebas contra SQL Server y HTTP de backend quedaron pendientes por bloqueo Maven/ausencia de BD.

Fuentes de código principales: [SecurityConfig](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-api/roommatch-api/src/main/java/com/roommatch/config/SecurityConfig.java), [JwtService](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-api/roommatch-api/src/main/java/com/roommatch/security/JwtService.java), [JwtAuthenticationFilter](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-api/roommatch-api/src/main/java/com/roommatch/security/JwtAuthenticationFilter.java), [DTO](https://github.com/1621-nicolas/roommatch/tree/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-api/roommatch-api/src/main/java/com/roommatch/dto). Las fichas RM con líneas individuales, impacto, reproducción, solución y tests están en `ROOMMATCH_AUDIT.md`.

## Superficies de confianza

Visitante puede leer catálogo/planes/imágenes declarados públicos; cuenta autenticada administra sus datos y contacta otros; propietario administra sus habitaciones/leads; ADMIN consulta métricas y modera reportes. El principal del backend se construye con Usuario de BD, no con idUsuario de un formulario. Un ID secuencial no es por sí solo vulnerabilidad; falta de autorización sobre el objeto sí lo sería. Los datos enviados por Angular, los valores de localStorage, URLs de imágenes y el contenido de un JWT sin verificar son entrada no confiable.

Hay una sola API y un firmante HMAC; la clave no necesita ser asimétrica solo por estar en JWT. El frontend nunca necesita conocer el secreto de firma. Un token firmado no está cifrado: quien lo tiene puede leer sus claims. No hay refresh ni cookies de autenticación actualmente. El email de acceso se expone como subject y en ciertos DTO, lo que debe minimizarse.

## Revisión OWASP por categoría

Las etiquetas CONFIRMADO/RIESGO/NO APLICA describen la observación, no un escaneo que certifique ausencia de vulnerabilidades. Se revisan áreas OWASP relevantes, sin asignar un CVSS inventado ni declarar todos los riesgos como exploits.

| Área | Clasificación | Evidencia / conclusión | Prioridad y acción |
| ---| ---| ---| ---|
| Broken Access Control / BOLA | CONFIRMADO en moderación; RIESGO en vínculo ajeno | Dueño puede activar habitación sancionada; ownership de la mayoría de mutaciones sí está presente. Asociar habitación ajena no escribe la habitación y depende de D-01 | RM-08/17/22; policy de estado y consentimiento |
| Exposición excesiva de datos | CONFIRMADO | MatchResponse.email y FavoritoResponse.email no exigen contacto; LeadResponse revela email de acceso a dueño interesado | RM-02; DTO de descubrimiento mínimo |
| Fallos criptográficos / secretos | CONFIRMADO en configuración | Fallback de clave pública y HTTP/SQL sin obligación productiva TLS | RM-01/03; fail-fast, claves aleatorias externas y TLS |
| Identificación/autenticación | CONFIRMADO control ausente; RIESGO de ataque | Mínimo 6, sin throttling; login 400 y mensajes de estado diferenciados; 24 h sin revocación por sesión | RM-05/06/07 |
| Configuración insegura | CONFIRMADO en repo | Swagger global, import local global, URL API localhost. Hosting/productivo no inspeccionado | RM-03/04/23 |
| SQL/JPQL injection | NO confirmada | HabitacionBusquedaRepository concatena solo fragmentos constantes; valores se enlazan con setParameter. @Query/named queries parametrizadas; ordenamiento fijo | Preservar parámetros y tests de caracteres especiales; no reportar SQLi solo por StringBuilder |
| XSS DOM/reflejado/almacenado | RIESGO, sin exploit propio probado | No se halló innerHTML sin control, bypassSecurityTrust*, eval ni plantilla compilada desde texto de usuario. Angular bindings ordinarios; URL backend no validada y advisories Angular pendientes | RM-04/21/40; CSP, sanitización y parches analizados |
| CSRF | NO APLICA al flujo Bearer clásico; reevaluar si cookies | Autorización enviada explícitamente por interceptor, sin cookie de sesión. csrf.disable coherente con ese modelo; stateless por sí solo no elimina CSRF si luego se autentica con cookies | D-09; token CSRF/origen si se migra transporte |
| SSRF | NO APLICA al flujo actual | Backend almacena strings de URL; no las descarga con HTTP client, proxy ni importador | No introducir fetch arbitrario al “validar existencia” |
| Mass assignment | NO confirmado | Requests definidos y copia explícita; usuario no admite rol/estado/passwordHash desde payload. Registro asigna USUARIO de BD | Mantener DTO; permiso de destacada/relación tiene policy incompleta RM-16 |
| Diseño inseguro / estados | CONFIRMADO y RIESGO concurrente | Solicitud no reenvía; cuota/expiry mal aplicados; aceptar/rechazar sin atomicidad; no historial admin | RM-13/14/16/20/35/37 |
| Recursos sin restricciones | RIESGO | Matching findAll+escrituras; size sin máximo uniforme; listas completas; reportes sin cuotas | RM-06/11/12/36 |
| Integridad software/supply chain | RIESGO | 23 paquetes npm afectados por advisory; no scan Maven completo; acciones por tag, rama sin checks obligatorios | RM-40/41; lock/SBOM/triage/checks |
| Logging/monitorización | CONFIRMADO PII en frontend; RIESGO en logs driver | Contactos/leads se imprimen; no se halló dump explícito de JWT/password en aplicación. Sin auditoría persistente de acciones admin | RM-37/43; redacción y trazabilidad mínima |
| Manejo de condiciones excepcionales | CONFIRMADO / RIESGO | IllegalArgumentException → 400 mezcla categorías; filtro captura caída BD como JWT inválido; falta manejo específico de JSON/tipos malformados | RM-07; contrato 401/403/404/409/400/503 y tests |
| Archivos/XXE/deserialización/command injection | NO APLICA a rutas encontradas | No upload de archivos, procesamiento XML, deserialización de objetos arbitrarios ni shell invocado desde API | Reevaluar si se añade esa capacidad, no inventar ruta vulnerable |

## JWT y Spring Security: control por control

| Control | Estado real | Propuesta |
| ---| ---| ---|
| Firma | `parseSignedClaims` + verifyWith(key); rechazo de token no firmado/alterado esperado de JJWT | Tests de firma, algoritmo admitido y clave por entorno; fijar política de algoritmo |
| Construcción de clave | UTF-8 literal, Keys.hmacShaKeyFor al emitir/leer; default 76 bytes | Entropía real y longitud apropiada al algoritmo; validación startup. No confundir longitud con secreto |
| Algoritmo | signWith(key) lo elige a partir de la clave; no está fijado como HS256 | Si HS256, generar≥32 bytes aleatorios; si HS512,≥64. Cambiar codificación a Base64 debe ser transición explícita |
| Expiración | Default 86.400.000 ms; se exige exp posterior a now; doble lectura/parsing | TTL positivo acotado, Clock/test borde; parsear una vez; skew pequeño y explícito si necesario |
| Subject | email normalizado resuelto a Usuario | Identificador estable para minimizar PII y evitar acoplar identidad a cambio de email |
| Claims | idUsuario, nombres, apellidos, rol; iat/exp | Quitar nombres/apellidos; rol solo si cliente lo usa como indicación, no autoridad; id estable, iss/aud y versión si necesaria |
| Issuer/audience | No generados ni exigidos | Separación de contexto/entornos; evita aceptar token de otro servicio con clave accidentalmente compartida |
| Token inválido | Se limpia contexto y continúa; broad catch incluye fallos no JWT | Respuesta 401 consistente para API protegida; tratar infraestructura como 503, sin filtrar causas internas |
| Usuarios suspendidos | Filtro comprueba estado activo de BD en cada request | Conservar; también excluir contenido suspendido de catálogos/cache |
| Roles/authorities | ROLE_ + nombreRol de BD; ignora claims de rol para autoridad | Conservar; metodo admin adicional; prevenir self-democión al alta de propietario |
| 401/403 | Advice MVC tiene handlers, pero filtro no define entrypoint JSON | Contrato unificado; verificar en MockMvc con cadena real, no asumir status por anotaciones MVC |
| Logout | Solo removeItem local | Documentar hasta aprobar revocación; tokenVersion si se quiere cerrar todas las sesiones |
| Refresh | Ausente | No requerido por defecto; considerar solo si reingreso frecuente demostrado como problema |
| Revocación por token | No jti/denylist | Si negocio exige sesión individual, agregar jti y estado con TTL hasta exp; costo de lookup/almacenamiento |
| Rotación | Una clave, sin kid/transición | Rotación planificada con claves por entorno; emergencia invalida sesiones. Nunca aceptar indefinidamente la clave comprometida |
| Token robado | Se acepta hasta exp, suspensión o cambio de clave | Reducir exposición, TTL, no PII, CSP y política de cierre; no prometer “inrobable” por cambiar almacenamiento |

La guía OWASP explica que JWT necesita decisiones explícitas sobre almacenamiento, revocación y ciclo de vida; no convierte refresh o cookies en requisito universal. RoomMatch puede endurecer Bearer sin reconstruir toda la sesión. [OWASP JSON Web Token](https://cheatsheetseries.owasp.org/cheatsheets/JSON_Web_Token_Cheat_Sheet.html).

El filtro contiene `filterChain.doFilter` dentro del try en el caso inválido y otro al final. Si el downstream lanza una excepción que alcanza ese catch, existe riesgo de continuar la cadena otra vez; necesita un test de filtro con downstream que falla. No se afirma doble ejecución habitual de cada petición inválida. Limitar try/catch al parseo/autenticación y ejecutar cadena exactamente una vez simplifica ese riesgo.

## Entornos y transporte

| Configuración | Development | Test | Production propuesta |
| ---| ---| ---| ---|
| Perfil | Activación explícita local | application-test con H2 solo unitarios/smoke; SQL Server para integración | Perfil exigido por despliegue, sin importar archivo local |
| JWT | Secreto local explícito no compartido | Clave fija solo de fixture | Secreto de gestor/entorno, aleatorio, sin fallback y validado al arrancar |
| SQL | Puede conservar encrypt=false/trust=true para no romper entorno actual | BD desechable; licencia/imagen SQL Server verificadas | encrypt=true y trustServerCertificate=false con certificado/nombre válidos |
| Credenciales BD | sa solo si el entorno local lo necesita | Cuenta efímera aislada | Cuenta app mínimo privilegio; migrador separado |
| Swagger | Público útil | Habilitado si se prueba contrato | Deshabilitado o ADMIN, por decisión de operación |
| API SPA | localhost:8081/api | URL inyectada/mock del test | /api bajo mismo origen o HTTPS explícito; nunca localhost del visitante |
| CORS | localhost:4200 explícito | Orígenes de fixtures | Lista estricta de origen; no wildcard con credenciales |
| Headers | Política compatible con desarrollo | Tests de política | CSP verificada, TLS/headers del servidor de la SPA y API |

Microsoft documenta que encrypt/trustServerCertificate controlan cifrado y validación del certificado; habilitar trust no equivale a validar identidad del servidor. La propuesta productiva requiere infraestructura de certificados, no solo cambiar un boolean. [Microsoft JDBC: conexión cifrada](https://learn.microsoft.com/en-us/sql/connect/jdbc/connecting-with-ssl-encryption?view=sql-server-ver17).

### A) Bearer + localStorage reforzado frente a B) cookie

| Aspecto | A: Bearer + localStorage | B: Secure/HttpOnly/SameSite cookie |
| ---| ---| ---|
| Cambio de arquitectura | Bajo; conserva interceptor/API actual | Medio–alto: emisión/eliminación de cookie, cliente credentials, filtro y CORS |
| XSS | Script del origen puede leer/exfiltrar token; CSP/sanitización reducen riesgo, no lo eliminan | HttpOnly impide leer cookie desde JS, pero XSS todavía puede operar como usuario |
| CSRF | Token no se adjunta automáticamente por navegador | Cookie sí; protección CSRF/origen y SameSite según topología |
| Persistencia | Sobrevive cierre del navegador; sessionStorage limitaría una pestaña pero sigue accesible a JS | Max-Age/expiración configurada; sesión/control del servidor según diseño |
| Mismo origen | Simple; URL base/interceptor coherentes | Más simple con frontend y API en mismo sitio/origen público |
| Orígenes distintos | CORS con Authorization y preflight | Credentials, orígenes explícitos y SameSite acorde. Cross-site real puede exigir None+Secure y sufrir restricciones de cookies de terceros |
| Logout | Borrar copia local; no revoca copia robada | Borrar cookie tampoco revoca copia robada de JWT si backend no tiene estado de revocación |
| Complejidad de QA | XSS, CORS, exp, 401 y scopes de interceptor | Lo anterior + CSRF, dominios/path, login/logout, preflight, SameSite, navegadores |
| Uso académico actual | Propuesta inicial de menor cambio, con riesgo residual documentado | Justificable si el despliegue y requisitos priorizan evitar extracción de tokens por JS |

**Recomendación para la primera corrección:** mantener A provisionalmente, reparar clave/PII/abuso/dependencias y UX401; comprobar CSP, URLs y ausencia de sinks peligrosos. D-09 decide si la persistencia y amenaza justifican B. No migrar a cookies en una refactorización silenciosa. Alternativa de token en memoria reduce persistencia pero obliga a decidir reingreso o renovación; no se introduce un refresh para compensar sin analizarlo.

Angular recomienda tratar plantillas como código confiable y mantener sus mecanismos de sanitización; CSP/Trusted Types aportan contención adicional. La app no usa bypassSecurityTrust* según búsqueda. [Seguridad de Angular](https://angular.dev/best-practices/security).

## Abuso y contraseñas: diseño proporcional

Propuesta configurable para una instancia inicial, valores sujetos a pruebas/decisión operativa: login por IP con límite de ráfaga y contador de fallos por cuenta normalizada; tras 5 fallos/15 min aplicar demora creciente acotada (sin bloqueo permanente). Registro 5/hora/IP como punto de partida, con excepción para NAT universitario legítimo. Solicitudes 20/día/actor más unicidad de pendiente y cooldown por par; reportes 10/día/actor y deduplicación del mismo asunto pendiente; calcular matches 1/minuto/actor con ráfaga 2 y cache. Son umbrales para afinar con datos, no capacidades existentes ni recomendación de atacar un sistema para medirlas.

Implementación inicial: filtro/servicio de límite con reloj inyectable y counters con vencimiento; 429 y Retry-After; claves que no registren email en claro; IP del proxy solo si proxy confiable. En varias réplicas, un contador local puede multiplicar el cupo: migrar a estado compartido o gateway si ese escenario existe. CAPTCHA solo tras indicio de abuso, no a todos por defecto. No enviar mensajes/email sin autorización o infraestructura.

Sin MFA, la propuesta de mínimo 15 caracteres y frases largas sin composición forzada sigue la guía de autenticación. Permitir pegar y gestores; no rotación periódica obligatoria; lista de contraseñas comunes y respuestas no enumerables. [OWASP Authentication](https://cheatsheetseries.owasp.org/cheatsheets/Authentication_Cheat_Sheet.html). BCrypt tiene límite 72 bytes, distinto de 100 caracteres del DTO: validar explícitamente y probar Unicode, nunca truncar silenciosamente. Mantener BCrypt inicialmente implica comunicar ese límite; soportar≥64 caracteres Unicode arbitrarios puede justificar un encoder alternativo con transición/rehash y análisis propio. [OWASP Password Storage](https://cheatsheetseries.owasp.org/cheatsheets/Password_Storage_Cheat_Sheet.html).

El coste BCrypt actual es el default del encoder; debe medirse en el hardware objetivo antes de incrementarlo. No hay evidencia de que actualizar Java 25 resuelva política de contraseña. Recuperación/cambio de contraseña y verificación de email no existen hoy: D-09 decide alcance, tokens de un solo uso, expiración y reautenticación si se añaden.

## Privacidad: rutas directas y rutas laterales

| Actor/relación | Endpoint contacto | Campos devueltos por implementación actual |
| ---| ---| ---|
| Dueño de sus datos | GET /api/contactos/me | Todos sus datos y flags; fromEntity con privacidad desactivada para sí mismo; si falta registro, respuesta opcional |
| A aceptó a B / B aceptó a A | GET /api/contactos/desbloqueado/{otro} | Consulta simétrica ContactoRoomie; solo campos cuyos mostrarX son true |
| Solicitud pendiente, rechazada o ningún vínculo | Mismo endpoint | Debe denegar por inexistencia de ContactoRoomie; no validar solo un botón/guard |
| A pide su propio ID vía desbloqueado | Mismo endpoint | Servicio rechaza auto-consulta; /me garantiza acceso propio |
| UsuarioC ajeno | Mismo endpoint | No tiene relación y se rechaza |
| Visitante sin token | /contactos/** | Protegido por cualquier autenticado; body/status del filtro pendiente de test real |
| A marca B como favorito | POST /api/favoritos/{B} | **Email de login de B expuesto, incluso sin relación y mostrarEmail=false** |
| A recibe match con B | /api/matches | **Email de login de B expuesto sin consultar flags** |
| Dueño recibe lead | /api/leads/propietario | Email de acceso del interesado incluido; consentimiento de ese dato no definido |

Teléfono/WhatsApp/Instagram/Facebook/emailContacto sí se nulifican según flag en ContactoUsuarioResponse para otros. MostrarEmail=false no basta para proteger email de autenticación por las fugas laterales. El DTO de contacto también devuelve flags de visibilidad; no son el secreto principal, pero se pueden minimizar para otro usuario. El propietario puede consultar siempre sus propios datos mediante /me. La existencia de ContactoRoomie basta hoy: no hay revocación/bloqueo de contacto ni verificación de que la solicitud continúe aceptada; la carrera RM-14 puede dejar estado inconsistente. Definir si suspensión/cancelación/levantamiento afecta relaciones previas antes de introducir nueva política.

Los DTO públicos de habitaciones, publicaciones y planes no incluyen teléfono, WhatsApp ni email de autenticación. Sí incluyen IDs, nombre público/propietario, edad/ocupación/foto de autor, descripción/dirección referencial y datos de vivienda vinculada. Dirección referencial de publicación externa es explícitamente pública por el DTO: no introducir dirección exacta privada sin separar campos. Los endpoints de imágenes necesitan policy de visibilidad; acceso público a URLs de recursos pausados se mantiene actualmente.

## Autorización exhaustiva de endpoints con IDs

La siguiente matriz enumera cada operación cuyo path recibe un ID. Operaciones /me y listados sin ID también están en la matriz de 69endpoints de arquitectura. El idHabitacion en PublicacionRoomieRequest es el caso adicional de ID en cuerpo tratado en RM-08/D-01; no hay idUsuario editable en UsuarioRequest. `idPlan` de catálogo no es un recurso que el propietario deba poseer: sí requiere elegibilidad/vigencia/cupo, que hoy falla.


| Verbo y endpoint | Regla comprobada | Fuente |
| --- | --- | --- |
| GET /api/contactos/desbloqueado/{idUsuario} | ContactoRoomie simétrico requerido; preferencias en DTO; RM-02 fuga por otros DTO | [ContactoUsuarioController: 235](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-api/roommatch-api/src/main/java/com/roommatch/controller/ContactoUsuarioController.java#L235) |
| POST /api/favoritos/{idUsuarioFavorito} | Principal delimita dueño; alta a usuario activo; DELETE por par propio. RM-02 email | [FavoritoController: 23](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-api/roommatch-api/src/main/java/com/roommatch/controller/FavoritoController.java#L23) |
| DELETE /api/favoritos/{idUsuarioFavorito} | Principal delimita dueño; alta a usuario activo; DELETE por par propio. RM-02 email | [FavoritoController: 62](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-api/roommatch-api/src/main/java/com/roommatch/controller/FavoritoController.java#L62) |
| GET /api/habitaciones/{idHabitacion} | Público; habitación activa; falta estado efectivo del autor/moderación (RM-22) | [HabitacionController: 107](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-api/roommatch-api/src/main/java/com/roommatch/controller/HabitacionController.java#L107) |
| PUT /api/habitaciones/{idHabitacion} | Perfil propietario del principal; ID+propietario en edición/estado; RM-16/17 policy insuficiente | [HabitacionController: 118](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-api/roommatch-api/src/main/java/com/roommatch/controller/HabitacionController.java#L118) |
| PUT /api/habitaciones/{idHabitacion}/pausar | Perfil propietario del principal; ID+propietario en edición/estado; RM-16/17 policy insuficiente | [HabitacionController: 136](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-api/roommatch-api/src/main/java/com/roommatch/controller/HabitacionController.java#L136) |
| PUT /api/habitaciones/{idHabitacion}/activar | Perfil propietario del principal; ID+propietario en edición/estado; RM-16/17 policy insuficiente | [HabitacionController: 152](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-api/roommatch-api/src/main/java/com/roommatch/controller/HabitacionController.java#L152) |
| POST /api/imagenes-habitacion/habitacion/{idHabitacion} | Cadena imagen→padre→propietario/autor comparada con principal; RM-20 concurrencia | [ImagenHabitacionController: 45](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-api/roommatch-api/src/main/java/com/roommatch/controller/ImagenHabitacionController.java#L45) |
| GET /api/imagenes-habitacion/habitacion/{idHabitacion} | Público; estado/visibilidad del padre incompleto (RM-22) | [ImagenHabitacionController: 118](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-api/roommatch-api/src/main/java/com/roommatch/controller/ImagenHabitacionController.java#L118) |
| PUT /api/imagenes-habitacion/{idImagen}/principal | Cadena imagen→padre→propietario/autor comparada con principal; RM-20 concurrencia | [ImagenHabitacionController: 175](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-api/roommatch-api/src/main/java/com/roommatch/controller/ImagenHabitacionController.java#L175) |
| DELETE /api/imagenes-habitacion/{idImagen} | Cadena imagen→padre→propietario/autor comparada con principal; RM-20 concurrencia | [ImagenHabitacionController: 243](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-api/roommatch-api/src/main/java/com/roommatch/controller/ImagenHabitacionController.java#L243) |
| POST /api/publicaciones-roomie/{idPublicacion}/imagenes | Cadena imagen→padre→propietario/autor comparada con principal; RM-20 concurrencia | [ImagenPublicacionController: 25](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-api/roommatch-api/src/main/java/com/roommatch/controller/ImagenPublicacionController.java#L25) |
| GET /api/publicaciones-roomie/{idPublicacion}/imagenes | Público; estado/visibilidad del padre incompleto (RM-22) | [ImagenPublicacionController: 44](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-api/roommatch-api/src/main/java/com/roommatch/controller/ImagenPublicacionController.java#L44) |
| PUT /api/publicaciones-roomie/imagenes/{idImagen}/principal | Cadena imagen→padre→propietario/autor comparada con principal; RM-20 concurrencia | [ImagenPublicacionController: 56](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-api/roommatch-api/src/main/java/com/roommatch/controller/ImagenPublicacionController.java#L56) |
| DELETE /api/publicaciones-roomie/imagenes/{idImagen} | Cadena imagen→padre→propietario/autor comparada con principal; RM-20 concurrencia | [ImagenPublicacionController: 73](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-api/roommatch-api/src/main/java/com/roommatch/controller/ImagenPublicacionController.java#L73) |
| POST /api/leads/habitacion/{idHabitacion} | Mis=principal; alta a habitación activa no propia; cambios/lista propietario por ID+dueño; RM-02 privacidad | [LeadHabitacionController: 28](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-api/roommatch-api/src/main/java/com/roommatch/controller/LeadHabitacionController.java#L28) |
| PUT /api/leads/{idLead}/estado | Mis=principal; alta a habitación activa no propia; cambios/lista propietario por ID+dueño; RM-02 privacidad | [LeadHabitacionController: 98](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-api/roommatch-api/src/main/java/com/roommatch/controller/LeadHabitacionController.java#L98) |
| PUT /api/notificaciones/{idNotificacion}/leer | Principal delimita lista/conteo; ID+usuario para leer; leer-todas solo propio | [NotificacionController: 60](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-api/roommatch-api/src/main/java/com/roommatch/controller/NotificacionController.java#L60) |
| PUT /api/planes/cambiar/{idPlan} | Perfil propietario del principal; plan objetivo activo, política incompleta RM-16 | [PlanPropietarioController: 133](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-api/roommatch-api/src/main/java/com/roommatch/controller/PlanPropietarioController.java#L133) |
| GET /api/publicaciones-roomie/{idPublicacion} | PermitAll pero helper exige usuario: detalle anónimo roto (RM-25) | [PublicacionRoomieController: 227](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-api/roommatch-api/src/main/java/com/roommatch/controller/PublicacionRoomieController.java#L227) |
| PUT /api/publicaciones-roomie/{idPublicacion} | Autor del principal; ID+autor en escrituras; vínculo habitación sin regla acordada (RM-08) | [PublicacionRoomieController: 347](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-api/roommatch-api/src/main/java/com/roommatch/controller/PublicacionRoomieController.java#L347) |
| PUT /api/publicaciones-roomie/{idPublicacion}/pausar | Autor del principal; ID+autor en escrituras; vínculo habitación sin regla acordada (RM-08) | [PublicacionRoomieController: 406](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-api/roommatch-api/src/main/java/com/roommatch/controller/PublicacionRoomieController.java#L406) |
| PUT /api/publicaciones-roomie/{idPublicacion}/activar | Autor del principal; ID+autor en escrituras; vínculo habitación sin regla acordada (RM-08) | [PublicacionRoomieController: 459](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-api/roommatch-api/src/main/java/com/roommatch/controller/PublicacionRoomieController.java#L459) |
| PUT /api/publicaciones-roomie/{idPublicacion}/cerrar | Autor del principal; ID+autor en escrituras; vínculo habitación sin regla acordada (RM-08) | [PublicacionRoomieController: 512](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-api/roommatch-api/src/main/java/com/roommatch/controller/PublicacionRoomieController.java#L512) |
| DELETE /api/publicaciones-roomie/{idPublicacion} | Autor del principal; ID+autor en escrituras; vínculo habitación sin regla acordada (RM-08) | [PublicacionRoomieController: 565](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-api/roommatch-api/src/main/java/com/roommatch/controller/PublicacionRoomieController.java#L565) |
| POST /api/reportes/usuarios/{idUsuarioReportado} | Revisar | [ReporteController: 27](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-api/roommatch-api/src/main/java/com/roommatch/controller/ReporteController.java#L27) |
| POST /api/reportes/habitaciones/{idHabitacion} | Revisar | [ReporteController: 53](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-api/roommatch-api/src/main/java/com/roommatch/controller/ReporteController.java#L53) |
| PUT /api/reportes/admin/usuarios/{idReporte}/revisar | ADMIN en SecurityConfig y validarAdmin en servicio; estados/rastreo RM-17/37 | [ReporteController: 135](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-api/roommatch-api/src/main/java/com/roommatch/controller/ReporteController.java#L135) |
| PUT /api/reportes/admin/habitaciones/{idReporte}/revisar | ADMIN en SecurityConfig y validarAdmin en servicio; estados/rastreo RM-17/37 | [ReporteController: 161](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-api/roommatch-api/src/main/java/com/roommatch/controller/ReporteController.java#L161) |
| PUT /api/reportes/admin/usuarios/{idReporte}/sancionar | ADMIN en SecurityConfig y validarAdmin en servicio; estados/rastreo RM-17/37 | [ReporteController: 187](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-api/roommatch-api/src/main/java/com/roommatch/controller/ReporteController.java#L187) |
| PUT /api/reportes/admin/habitaciones/{idReporte}/sancionar | ADMIN en SecurityConfig y validarAdmin en servicio; estados/rastreo RM-17/37 | [ReporteController: 211](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-api/roommatch-api/src/main/java/com/roommatch/controller/ReporteController.java#L211) |
| POST /api/solicitudes/{idUsuarioReceptor} | Emisor/receptor actual; aceptar/rechazar solo receptor y pendiente; carreras y reenvío RM-13/14 | [SolicitudContactoController: 25](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-api/roommatch-api/src/main/java/com/roommatch/controller/SolicitudContactoController.java#L25) |
| PUT /api/solicitudes/{idSolicitud}/aceptar | Emisor/receptor actual; aceptar/rechazar solo receptor y pendiente; carreras y reenvío RM-13/14 | [SolicitudContactoController: 81](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-api/roommatch-api/src/main/java/com/roommatch/controller/SolicitudContactoController.java#L81) |
| PUT /api/solicitudes/{idSolicitud}/rechazar | Emisor/receptor actual; aceptar/rechazar solo receptor y pendiente; carreras y reenvío RM-13/14 | [SolicitudContactoController: 105](https://github.com/1621-nicolas/roommatch/blob/8308ed4cde239bbc03752df985269134d6de52bf/roommatch-api/roommatch-api/src/main/java/com/roommatch/controller/SolicitudContactoController.java#L105) |



Prueba transversal posterior: A/B con recursos propios, C sin relación y ADMIN. Variar cada ID sin modificar principal; asegurar ausencia de cambios laterales/notificaciones/contactos en errores. Probar ID negativo, 0,inexistente, ajeno, activo/inactivo y concurrencia; confirmar acceso legítimo del propietario y autorización específica admin. No sustituir estos casos por tests del guard de Angular.

## Dependencias: snapshot npm del 12 septiembre

El comando se ejecutó sin modificar lock. Versiones y severidades provienen de npm audit; paquetes que heredan una vulnerabilidad aparecen además del paquete raíz, por lo que hay duplicación de cadenas. FixAvailable no significa que se haya probado su compatibilidad. La clasificación runtime/tooling es preliminar según dependencias raíz y árbol; la explotación exige las precondiciones de cada advisory.


| Paquete | Versión(es) lock | Severidad audit | Alcance preliminar | Advisories / propagación | Fix reportado |
| --- | --- | --- | --- | --- | --- |
| @angular/build | 21.2.18 | moderate | tooling/transitiva (ver árbol) | vía undici | True |
| @angular/common | 21.2.17 | high | runtime | vía @angular/core; [GHSA-jhpw-976m-542j](https://github.com/advisories/GHSA-jhpw-976m-542j); [GHSA-p297-fm68-3q8c](https://github.com/advisories/GHSA-p297-fm68-3q8c) | True |
| @angular/compiler | 21.2.17 | high | runtime | [GHSA-jj27-h5hq-8x99](https://github.com/advisories/GHSA-jj27-h5hq-8x99); [GHSA-hh8m-fm6v-7cvg](https://github.com/advisories/GHSA-hh8m-fm6v-7cvg) | True |
| @angular/compiler-cli | 21.2.17 | moderate | tooling/transitiva (ver árbol) | vía @angular/compiler; vía @babel/core | True |
| @angular/core | 21.2.17 | high | runtime | vía @angular/compiler; [GHSA-jj27-h5hq-8x99](https://github.com/advisories/GHSA-jj27-h5hq-8x99); [GHSA-hh8m-fm6v-7cvg](https://github.com/advisories/GHSA-hh8m-fm6v-7cvg) | True |
| @angular/forms | 21.2.17 | moderate | runtime | vía @angular/common; vía @angular/core; vía @angular/platform-browser | True |
| @angular/platform-browser | 21.2.17 | moderate | runtime | vía @angular/common; vía @angular/core | True |
| @angular/router | 21.2.17 | moderate | runtime | vía @angular/common; vía @angular/core; vía @angular/platform-browser | True |
| @babel/core | 7.29.0 | low | tooling/transitiva (ver árbol) | [GHSA-4x5r-pxfx-6jf8](https://github.com/advisories/GHSA-4x5r-pxfx-6jf8) | True |
| @hono/node-server | 1.19.14 | moderate | tooling/transitiva (ver árbol) | [GHSA-frvp-7c67-39w9](https://github.com/advisories/GHSA-frvp-7c67-39w9) | True |
| @vitest/mocker | 4.1.9 | moderate | tooling/transitiva (ver árbol) | [GHSA-82fw-gwwq-j7x9](https://github.com/advisories/GHSA-82fw-gwwq-j7x9) | True |
| baseline-browser-mapping | 2.10.41 | moderate | tooling/transitiva (ver árbol) | [GHSA-w5vr-8v7q-w6rv](https://github.com/advisories/GHSA-w5vr-8v7q-w6rv) | True |
| brace-expansion | 5.0.7 | high | tooling/transitiva (ver árbol) | [GHSA-mh99-v99m-4gvg](https://github.com/advisories/GHSA-mh99-v99m-4gvg); [GHSA-rgw5-rvv9-x895](https://github.com/advisories/GHSA-rgw5-rvv9-x895) | True |
| browserslist | 4.28.4 | high | tooling/transitiva (ver árbol) | [GHSA-c83g-rgw3-j3cx](https://github.com/advisories/GHSA-c83g-rgw3-j3cx); [GHSA-73wf-gq98-2v4g](https://github.com/advisories/GHSA-73wf-gq98-2v4g) | True |
| fast-uri | 3.1.3 | high | tooling/transitiva (ver árbol) | [GHSA-v2hh-gcrm-f6hx](https://github.com/advisories/GHSA-v2hh-gcrm-f6hx); [GHSA-7p8r-x3mc-p8w7](https://github.com/advisories/GHSA-7p8r-x3mc-p8w7); [GHSA-5jgf-p345-68v8](https://github.com/advisories/GHSA-5jgf-p345-68v8); [GHSA-f65p-4m7j-42xc](https://github.com/advisories/GHSA-f65p-4m7j-42xc); [GHSA-fph4-wmhf-6fwf](https://github.com/advisories/GHSA-fph4-wmhf-6fwf); [GHSA-jqff-g426-hqxp](https://github.com/advisories/GHSA-jqff-g426-hqxp) | True |
| hono | 4.12.27 | moderate | tooling/transitiva (ver árbol) | [GHSA-8j4g-w8fx-2239](https://github.com/advisories/GHSA-8j4g-w8fx-2239); [GHSA-f23p-vx2j-j53r](https://github.com/advisories/GHSA-f23p-vx2j-j53r); [GHSA-79qm-7rj5-m7r9](https://github.com/advisories/GHSA-79qm-7rj5-m7r9); [GHSA-54fx-42gc-7vw4](https://github.com/advisories/GHSA-54fx-42gc-7vw4); [GHSA-gqvv-2mrq-wpjv](https://github.com/advisories/GHSA-gqvv-2mrq-wpjv); [GHSA-g6gw-c38x-mqfc](https://github.com/advisories/GHSA-g6gw-c38x-mqfc); [GHSA-crvj-82cr-hjcx](https://github.com/advisories/GHSA-crvj-82cr-hjcx) | True |
| ip-address | 10.2.0 | high | tooling/transitiva (ver árbol) | [GHSA-mwp4-54f8-5fhr](https://github.com/advisories/GHSA-mwp4-54f8-5fhr); [GHSA-4xrf-jv44-h6hh](https://github.com/advisories/GHSA-4xrf-jv44-h6hh); [GHSA-22jq-vg5j-6vgg](https://github.com/advisories/GHSA-22jq-vg5j-6vgg) | True |
| nanoid | 3.3.15 | high | tooling/transitiva (ver árbol) | [GHSA-28wg-ghj8-5hjv](https://github.com/advisories/GHSA-28wg-ghj8-5hjv); [GHSA-2v37-7h3g-55p8](https://github.com/advisories/GHSA-2v37-7h3g-55p8) | True |
| postcss | 8.5.16 | high | tooling/transitiva (ver árbol) | [GHSA-fxqj-rqcc-2cmp](https://github.com/advisories/GHSA-fxqj-rqcc-2cmp); [GHSA-r28c-9q8g-f849](https://github.com/advisories/GHSA-r28c-9q8g-f849) | True |
| qs | 6.15.3 | moderate | tooling/transitiva (ver árbol) | [GHSA-x5fp-wj9c-mxmx](https://github.com/advisories/GHSA-x5fp-wj9c-mxmx); [GHSA-4mjr-xmp4-gh2g](https://github.com/advisories/GHSA-4mjr-xmp4-gh2g) | True |
| tar | 7.5.19 | high | tooling/transitiva (ver árbol) | [GHSA-r292-9mhp-454m](https://github.com/advisories/GHSA-r292-9mhp-454m) | True |
| undici | 6.27.0, 7.28.0 | high | tooling/transitiva (ver árbol) | [GHSA-8xcm-r25x-g524](https://github.com/advisories/GHSA-8xcm-r25x-g524); [GHSA-8xcm-r25x-g524](https://github.com/advisories/GHSA-8xcm-r25x-g524); [GHSA-4cwx-7wf7-3272](https://github.com/advisories/GHSA-4cwx-7wf7-3272); [GHSA-m8rv-5g2x-5cg5](https://github.com/advisories/GHSA-m8rv-5g2x-5cg5); [GHSA-m8rv-5g2x-5cg5](https://github.com/advisories/GHSA-m8rv-5g2x-5cg5); [GHSA-jr45-8vmc-qm54](https://github.com/advisories/GHSA-jr45-8vmc-qm54); [GHSA-v3r7-h72x-cjcm](https://github.com/advisories/GHSA-v3r7-h72x-cjcm); [GHSA-v3r7-h72x-cjcm](https://github.com/advisories/GHSA-v3r7-h72x-cjcm) | True |
| vitest | 4.1.9 | moderate | tooling/transitiva (ver árbol) | vía @vitest/mocker; [GHSA-82fw-gwwq-j7x9](https://github.com/advisories/GHSA-82fw-gwwq-j7x9) | True |



Angular runtime 21.2.17 requiere atención: advisories de sanitización/i18n/host bindings deben comprobarse con sus condiciones concretas; no se halló uso de plantilla no confiable ni i18n dinámico en este proyecto. Avisos de TransferCache/SSR no se demostraron alcanzables porque la app es SPA sin SSR configurado. Vitest/mocker, build y demás herramientas no se publican automáticamente con el runtime de la SPA; aun así importan en desarrollo/CI. No hay evidencia suficiente para declarar cada high como XSS explotable aquí. Los enlaces de la tabla son las fuentes exactas de advisories devueltas por npm.

Maven directo: Spring Boot 3.5.16 gestiona starters web/data-jpa/security/validation/mail y SQL Server driver; JJWT 0.12.6 y springdoc 2.8.17 fijados; devtools opcional runtime; starters-test, security-test y H2 de test. El job CI muestra Hibernate 6.6.53.Final. No se pudo resolver localmente parent ni árbol transitivo, así que **auditoría de vulnerabilidades Maven pendiente**, no “sin vulnerabilidades”. Completar SBOM/dependency tree con versiones efectivas y scanner alimentado por advisories oficiales; revisar dependencias sin uso como mail sin confundirlo con vulnerabilidad.

## Condiciones de aprobación de seguridad

No cerrar RM-01 con “variable recomendada en README”: production debe fallar al arrancar si falta/se usa clave de ejemplo. No cerrar RM-02 solo ocultando email en HTML: JSON y logs deben dejar de exponerlo. No cerrar RM-17 solo quitando el botón de activar: API debe bloquearlo. No cerrar RM-14 sin prueba concurrente y constraint SQL real. No cerrar RM-40 con audit fix --force ni actualizaciones mayores no justificadas. Incluir controles positivos para no bloquear al dueño legítimo, contactos aceptados en ambos sentidos y funciones normales de propietario.

Gate: tests de seguridad/ownership/privacidad del plan, configuración productiva verificada, cero P0 bajo configuración desplegable, P1 resueltos o aceptación concreta con responsable/plazo/evidencia. Pendiente aprobación de reglas D-01/02/03/04/05/06/07/09; ninguna migración de arquitectura de sesión ni cambio de reglas se aplicó.
