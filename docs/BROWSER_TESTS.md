# Pruebas de interfaz en navegador

La suite `roommatch-web/e2e` ejecuta el build de producción en Chromium,
en escritorio (1440 × 1000) y móvil (390 × 844), con idioma español de Perú.
El servidor de pruebas aplica la CSP leída de `deployment/nginx.conf`.

```bash
cd roommatch-web
npm ci
npm run build
npx playwright install --with-deps --only-shell chromium
npm run test:browser
```

`ROOMMATCH_CHROMIUM_PATH` permite indicar un Chromium local compatible cuando
el entorno no permite descargar el navegador habitual. No es necesario en CI.
La ejecución local del 26/09/2026 utilizó Chromium 153.0.8010.0: **34 pruebas
aprobadas, cero fallos, cero omitidas**, sin reintentos automáticos.

## Qué comprueba

- Inicio público y autenticado, acceso, registro, contactos, resumen
  administrativo y reportes: sin errores JavaScript/CSP ni desbordamiento
  horizontal de la página, un título principal y campos etiquetados.
- Navegación de cuenta con Enter/Tab/Escape y regreso del foco; menú móvil.
- Error recuperable cuando el API devuelve una colección mal formada.
- Credenciales rechazadas: error visible y formulario disponible nuevamente.
- Moderación: foco inicial, motivo obligatorio, cancelación con Escape y
  conservación del texto cuando otra decisión provoca un conflicto HTTP 409.
  El conflicto impide repetir el envío; permite actualizar los reportes de
  todos los estados y conserva el motivo como borrador explícito hasta
  descartarlo o salir de la pantalla.

La primera revisión detectó un desbordamiento de 638 px en un viewport de
390 px del dashboard. Las columnas flexibles y sus tablas simples ahora caben
en móvil. La tabla de reportes conserva desplazamiento horizontal dentro de
su contenedor y un aviso visible, sin desplazar toda la página.

## Evidencia y límites

Las 12 pruebas añadidas de galerías verifican fotos del API en catálogos y
detalles de habitaciones/publicaciones, selección principal y cambio de foto,
ausencia de peticiones API por tarjeta, `no-referrer`, error con reintento y
fallback ante una URL que devuelve 404. Sus ocho capturas también se adjuntan.
El SVG rotulado como foto de prueba vive exclusivamente en el interceptor e2e.

Las capturas de las siete pantallas y del diálogo se adjuntan al informe HTML
`playwright-report/index.html`. CI publica `frontend-browser-reports` durante
14 días, incluso ante fallos. Trazas y capturas de fallos quedan en
`test-results/`; ambos directorios se excluyen de Git.

Los datos de `e2e/fixtures.ts` son exclusivamente de prueba. Playwright
intercepta los endpoints; las peticiones no configuradas fallan. El servidor
de prueba nunca responde como si un backend inexistente hubiera funcionado.
El JWT de prueba no es válido ante la API real. Los fixtures no se incluyen
en la aplicación compilada.

Estas pruebas no certifican autorización del servidor, interacción real con
SQL Server, todos los navegadores, lectores de pantalla, zoom ni WCAG AA.
Las pruebas API/SQL Server siguen siendo independientes. Queda pendiente un
recorrido completo navegador → API → SQL Server en el entorno de despliegue.
La regresión de colección mal formada no reproduce por sí sola el error
reportado `t.value.map is not a function`, cuya traza original sigue faltando.
