# Contactos y privacidad

## Contrato

`GET /api/contactos/desbloqueados?page=0&size=20` devuelve una página de conexiones del usuario autenticado. No admite un ID de usuario para seleccionar al propietario de la agenda. El servidor selecciona los IDs autorizados desde `contacto_roomie`, en ambas direcciones, y carga los datos de esa página en un único batch. Orden: fecha de desbloqueo e ID de conexión descendentes. Límites: página 0–1000; tamaño 1–100.

Una conexión aceptada puede no tener datos registrados (`contacto: null`) o tener todos sus campos privados. Ningún caso es un error de infraestructura. Un fallo HTTP se presenta como error con reintento, nunca como agenda vacía.

Se conservan `GET /me`, `GET /desbloqueado/{idUsuario}` y `POST/PUT /me`. El propietario ve sus propios datos, aunque todas las preferencias estén desactivadas. Un tercero sin conexión recibe 403. Los contactos conectados solo reciben teléfono, WhatsApp, Instagram, Facebook y email si su preferencia correspondiente está activada. No se utiliza el email de acceso como sustituto del email de contacto.

## Ediciones concurrentes

Para crear, se omite `version` o se envía `null`. Para modificar un registro existente, se envía la versión obtenida de `/me`. Una edición obsoleta recibe 409: el frontend mantiene el borrador y no reintenta automáticamente. Después de guardar adopta la nueva versión. Los clientes antiguos deben recargar/actualizarse antes de modificar contactos.

El bloqueo de la fila de usuario serializa la creación y las modificaciones; `UNIQUE(id_usuario)` y `@Version` protegen adicionalmente la integridad. La versión no se comparte con otros usuarios.

## Datos y enlaces

Nuevos registros: las cinco preferencias son privadas por defecto. V12 cambia el default de email a 0 sin modificar elecciones históricas. Las filas ya existentes conservan sus valores; no es posible deducir si una preferencia histórica provino de un consentimiento o del antiguo default.

Teléfono/WhatsApp: 7–15 dígitos, hasta 20 caracteres con espacios y signos de formato. Añadir código de país a WhatsApp para que el enlace sea útil. Redes: usuario o URL HTTPS del dominio de la red correspondiente; Facebook admite `profile.php?id=...`. No se aceptan dominios ajenos, credenciales embebidas ni enlaces de redirección. Email: máximo 150 caracteres, coherente con SQL. No se comprueba la existencia real de perfiles ni se realizan peticiones externas desde el servidor. Valores históricos no reconocidos se muestran como texto sin enlace; no se borran.

## Verificación

`PrivacyTest`: 32 combinaciones de preferencias, lectura propia y rechazo a terceros. `ContactPreferencesTest`: defaults, versión obsoleta, límites y formatos. `SqlServerIT`: conexiones en ambas direcciones, privacidad del batch, carreras de creación/edición y presupuesto máximo de tres consultas por página de 10, 20 y 50 contactos. El resultado medido debe consultarse en CI (`CONTACT_QUERIES`); el presupuesto de consultas no es una promesa de latencia.

Angular: contrato JSON, errores, reintento, cancelación de suscripciones, privacidad sin datos, enlaces históricos y versiones del formulario. La inspección visual de escritorio/móvil sigue pendiente mientras no haya navegador disponible. Los estilos de contactos son propios de la página; se eliminó su bloque global obsoleto sin tocar las reglas de otras pantallas.

## Límites conocidos

No existe un mecanismo de revocación bilateral de la conexión por el usuario; no se incorpora como cambio implícito de negocio. Cada persona puede ocultar sus datos mediante preferencias. Ocultarlos después no permite retirar información que otra persona ya haya copiado.
