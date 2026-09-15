# Navegador, API y sesión

Desarrollo: `npm start` usa `/api/**` con el proxy local hacia el puerto 8081. Producción: `npm run build` selecciona `environment.production.ts` y mantiene `/api` relativo. No hay credenciales dentro de los environments.

`nginx.conf` es un ejemplo para servir `dist/roommatch-web/browser` en una red privada con un backend llamado `roommatch-api`. Requiere un ingreso HTTPS delante; no es por sí solo un despliegue TLS listo para Internet. No publiques el puerto de la API directamente. Comprueba dominio, certificado, red privada y reglas del ingreso antes de usarlo.

La CSP impide scripts externos e inline, frames y objetos; permite imágenes HTTPS externas y estilos inline requeridos por Angular y las vistas actuales. El build desactiva `inlineCritical` para no generar manejadores inline `onload` incompatibles con `script-src 'self'`. El encabezado `Referrer-Policy` evita enviar la ruta actual a proveedores de imágenes. La política debe enviarse también en errores y respuestas SPA. No se ha desplegado este ejemplo en un servidor real.

La sesión mantiene el Bearer en localStorage. La interfaz descarta valores dañados y tokens vencidos, limita el encabezado Authorization al origen y ruta de la API, y vuelve al login si el servidor rechaza la sesión. Los guards no autorizan operaciones: el backend verifica firma y estado/rol vigente en cada petición.

**Riesgo residual:** XSS en el origen puede leer el token. CSP, interpolación/sanitización Angular, ausencia de HTML confiado manualmente y revisión de dependencias reducen el riesgo sin eliminarlo. Logout borra la copia local; no revoca una copia robada. Los tokens duran dos horas por defecto. No se implementan refresh tokens.

Una cookie Secure + HttpOnly impediría leer el token desde JavaScript, pero un XSS aún podría ejecutar operaciones con la sesión. Requeriría cambiar emisión/transporte, logout, protección CSRF, pruebas y configuración SameSite/CORS. Para el alcance actual se conserva Bearer; revisar cookies o revocación de sesiones si se agregan pagos o acciones de mayor sensibilidad. No guardar tokens en URLs, logs o analítica.

## Proxy e IP del cliente

Por defecto no se confía en ningún proxy: login/registro se limitan por la dirección del socket. Para la topología del ejemplo, fija la dirección privada del Nginx y configura `TRUSTED_PROXY_IPS` en la API con esa dirección exacta. Admite varias IP literales separadas por comas, no nombres DNS, comodines ni rangos. No uses direcciones ilustrativas sin comprobar tu red. Solo esos peers pueden aportar una única `X-Real-IP` válida; las cabeceras ajenas, múltiples o inválidas no conceden cuotas nuevas. IPv4/IPv6 se normalizan. `X-Forwarded-For` no se usa en la API.

Nginx sobrescribe `X-Real-IP` con `$remote_addr`, nunca copia directamente un valor enviado por el cliente. Si existe otro ingreso TLS delante, configura el módulo real-IP de Nginx **solo** con las direcciones reales de ese ingreso y verifica que dicho ingreso también sobrescriba las cabeceras recibidas. Sin esa configuración, `$remote_addr` será la dirección del ingreso y sus clientes compartirán cuota. No publiques la API directamente ni permitas que terceros alcancen la API desde la IP autorizada. Mantén `server.forward-headers-strategy=none` para que el resolver pueda comprobar el peer real.

Verificación antes de desplegar: dos clientes reales deben tener cuotas separadas; cambiar `X-Real-IP` o `X-Forwarded-For` desde Internet no debe renovar la cuota; un peer no autorizado debe ignorar ambas cabeceras. Estas comprobaciones de red no se sustituyen con tests unitarios. Los contadores siguen siendo locales a una instancia; varias réplicas requieren límites compartidos o control equivalente en el ingreso.

## Separación de entornos

No combines `development`, `test` y `production`. La aplicación rechaza esa mezcla para evitar que un perfil local desactive controles de producción. Fuera de desarrollo/test exige `encrypt=true` y `trustServerCertificate=false`, cada uno una sola vez en `DB_URL`; propiedades TLS contradictorias o duplicadas fallan al arrancar. Las credenciales se configuran aparte.

Swagger solo es accesible con el perfil `development` y la documentación habilitada. Activar `springdoc.api-docs.enabled` por sí solo no publica documentación fuera de desarrollo, ni siquiera para un token de administrador. Desarrollo conserva su configuración local explícita.
