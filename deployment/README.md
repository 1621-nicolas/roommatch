# Navegador, API y sesión

Desarrollo: `npm start` usa `/api/**` con el proxy local hacia el puerto 8081. Producción: `npm run build` selecciona `environment.production.ts` y mantiene `/api` relativo. No hay credenciales dentro de los environments.

`nginx.conf` es un ejemplo para servir `dist/roommatch-web/browser` en una red privada con un backend llamado `roommatch-api`. Requiere un ingreso HTTPS delante; no es por sí solo un despliegue TLS listo para Internet. No publiques el puerto de la API directamente. Comprueba dominio, certificado, red privada y reglas del ingreso antes de usarlo.

La CSP impide scripts externos e inline, frames y objetos; permite imágenes HTTPS externas y estilos inline requeridos por Angular y las vistas actuales. El build desactiva `inlineCritical` para no generar manejadores inline `onload` incompatibles con `script-src 'self'`. El encabezado `Referrer-Policy` evita enviar la ruta actual a proveedores de imágenes. La política debe enviarse también en errores y respuestas SPA. No se ha desplegado este ejemplo en un servidor real.

La sesión mantiene el Bearer en localStorage. La interfaz descarta valores dañados y tokens vencidos, limita el encabezado Authorization al origen y ruta de la API, y vuelve al login si el servidor rechaza la sesión. Los guards no autorizan operaciones: el backend verifica firma y estado/rol vigente en cada petición.

**Riesgo residual:** XSS en el origen puede leer el token. CSP, interpolación/sanitización Angular, ausencia de HTML confiado manualmente y revisión de dependencias reducen el riesgo sin eliminarlo. Logout borra la copia local; no revoca una copia robada. Los tokens duran dos horas por defecto. No se implementan refresh tokens.

Una cookie Secure + HttpOnly impediría leer el token desde JavaScript, pero un XSS aún podría ejecutar operaciones con la sesión. Requeriría cambiar emisión/transporte, logout, protección CSRF, pruebas y configuración SameSite/CORS. Para el alcance actual se conserva Bearer; revisar cookies o revocación de sesiones si se agregan pagos o acciones de mayor sensibilidad. No guardar tokens en URLs, logs o analítica.

El limitador actual protege por usuario y por dirección del socket. Detrás de un proxy, el límite de login/registro se comparte entre clientes del mismo proxy; nunca se debe confiar ciegamente en `X-Forwarded-For`. La configuración de IP de cliente y proxy confiable debe resolverse antes de exponer la instalación públicamente. Los contadores son locales a una instancia; varias réplicas requieren límites compartidos o control equivalente en el ingreso.
