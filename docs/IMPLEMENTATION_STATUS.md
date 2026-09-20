# Estado de implementación

Actualizado el 20 de septiembre de 2026. Trabajo en
[`audit/roommatch-hardening`, PR #4](https://github.com/1621-nicolas/roommatch/pull/4).
No se ha modificado `main` ni realizado merge. Los cinco informes de la raíz
conservan la auditoría inicial contra `8308ed4` y distinguen sus datos de los
cambios posteriores. **RoomMatch aún no cumple todos los criterios de cierre.**

## Evidencia comprobada

- [CI de navegación](https://github.com/1621-nicolas/roommatch/actions/runs/35489699513):
  backend 169 unitarias + 18 integración, cero fallos/omisiones; frontend 62
  pruebas, build y audit aprobados. SQL Server real mediante Testcontainers.
- [CI de limpieza](https://github.com/1621-nicolas/roommatch/actions/runs/35489950681):
  ambos jobs aprobados.
- [CI de acceso/registro](https://github.com/1621-nicolas/roommatch/actions/runs/35490022064):
  backend y frontend aprobados; 69 pruebas frontend también verificadas localmente.
- `npm audit`: cero vulnerabilidades conocidas tras parches compatibles.
- Bundle inicial local: 389,24 kB; CSS global: 48,21 kB. Sigue el warning de
  `mis-publicaciones.css`: 14,06 kB frente al presupuesto de 12 kB.
- Consultas SQL medidas en integración: publicaciones 4 y contactos 3 por
  página, tanto para 10 como para 20 y 50 resultados. No son pruebas de carga.

## Trabajo implementado

| Área | Resultado | Referencia |
| --- | --- | --- |
| Producción y JWT | Sin secreto fallback productivo; validación de clave, TTL, firma, issuer/audience y estado/rol actual; sin nombres/apellidos en el token | `security/`, `config/`, tests backend; deployment/README.md |
| Entornos | No mezclar production con development/test; SQL cifrado y certificado validado; Swagger solo development | Configuración y pruebas de arranque |
| Abuso | Límites para autenticación y escrituras sensibles; cuenta e IP; headers de proxy solo con peer autorizado | AbuseProtectionFilter, ClientIpResolver |
| Contraseñas | Registro de al menos 15 caracteres, máximo BCrypt 72 bytes, rechazo de claves débiles conocidas | PasswordPolicy y pruebas |
| Privacidad | Sin email de login en discovery; leads con canal opcional explícito; contacto sujeto a relación bilateral y flags | BUSINESS_RULES.md, CONTACTS.md |
| Solicitudes | Cancelación, historial/reintento con espera; roles de emisor/receptor; una pendiente por pareja y un contacto bilateral | V5, servicios y pruebas SQL |
| Planes | Cupo activas+pausadas, downgrade incompatible rechazado, reactivación/expiración y bloqueo administrativo respetados | BUSINESS_RULES.md, V6 |
| Roles | Conversión de propietario conserva ADMIN; perfil de propietario como capacidad adicional | PropietarioService |
| Matching | 13 criterios ponderados, compatibilidad parcial, presupuesto gradual, cobertura y cálculo vigente al leer | MATCHING.md |
| Publicaciones | Ownership, referencia pública explícita, visibilidad y borrado lógico; compatibilidad batch | BUSINESS_RULES.md, V7 |
| Galerías | Máximo cinco, orden/principal bajo lock y constraints SQL; URLs HTTPS en producción | V9, ImageUrlPolicy |
| Edición | PATCH de descripción y versiones para evitar sobrescrituras de perfil/contacto | V10/V12, tests API/web |
| Admin | Métricas reales, reportes/filtros/paginación, resolución con motivo e historial inmutable | ADMIN.md, V11 |
| Frontend | API relativa, interceptor limitado al API, sesión vencida, rutas lazy, admin/contactos/inicio/auth/nav conectados | HOME.md, NAVIGATION.md, AUTH_FORMS.md |
| Limpieza | Cinco placeholders sin usos y archivo typo retirados; estilos antiguos eliminados tras comprobar consumidores | FRONTEND_CLEANUP.md |
| CI y migraciones | Tests frontend, verify SQL Server, audit npm; Flyway V1–V12 sin upgrade destructivo | Workflow, database/README.md |

## Pendientes que impiden cerrar el proyecto

1. Ejecutar navegador real: escritorio/móvil, teclado, foco, zoom, contraste,
   flujos completos y lectura asistida. No hay capturas actuales que permitan
   declarar el diseño verificado ni conformidad WCAG AA.
2. Completar renovación y revisión funcional del resto de pantallas. Verificar
   galerías en resultados/detalles públicos, filtros sobre páginas parciales,
   estados asíncronos y formularios de habitaciones/publicaciones/solicitudes.
3. Consolidar el inventario y la matriz campo por campo con el esquema después
   de V12 y todos los DTO actuales. El inventario histórico no es una matriz
   final de la rama corregida.
4. Completar auditoría de dependencias Maven. La resolución local de Maven no
   funciona; CI compila y prueba, pero eso no equivale a un escaneo de CVE.
5. Revisar URLs de foto de perfil y listados sin paginación restantes; medir
   consultas y carga reales de búsqueda/matching a escalas mayores.
6. Recuperación de contraseña, verificación de correo y documentos legales
   aún no implementados. No hay enlaces ficticios que simulen esos flujos.
7. Resolver el CSS de Mis publicaciones y modularización restante sin subir
   budgets para ocultar el warning.
8. Validar infraestructura real, backup/restauración, proxy/TLS/CSP y operación
   de migraciones sobre una copia representativa de datos existentes.
9. La protección de main sigue desactivada (verificada hoy). Configurar PR y
   checks obligatorios requiere la autorización específica del repositorio;
   se mantiene como propuesta y no se cambia automáticamente.

## Riesgos aceptados para esta arquitectura, no eliminados

- Bearer en localStorage: XSS puede robar el token. Logout es local; una copia
  robada puede durar hasta la expiración. No se ha introducido refresh/cookies
  ni revocación por sesión. La suspensión se consulta en cada request.
- Rate limit en memoria de una instancia; requiere otra estrategia si se
  despliegan varias réplicas y verificación de IP en la red real.
- URLs externas: pueden desaparecer o rastrear visitas. No se descarga contenido
  desde el backend ni se ha implementado almacenamiento administrado.
- Las nuevas migraciones paran ante ciertas inconsistencias históricas. No
  se limpian datos de usuarios silenciosamente para hacer pasar el arranque.
- El matching es un índice explicable de preferencias, no una probabilidad
  estadística validada de éxito de convivencia.

El error reportado `t.value.map is not a function` no se ha reproducido con una
traza atribuible a RoomMatch. Se agregaron validaciones de arrays en contratos
concretos, pero no se afirma que eso resuelva aquel error sin evidencia.

Los PR #2 (Java 25) y #3 siguen abiertos con sus heads originales; no se han
fusionado ni cerrado. Se conserva Java 17. La revisión histórica de ambos
está en el informe de auditoría.
