# Notificaciones: interfaz y revisión visual

Documentado el 27 de septiembre de 2026. Alcance: extensión ordinaria **Operate** de `/notificaciones`, con lista responsive, filtros y paginación del servidor, contador independiente, recuperación de errores y acciones accesibles por teclado.

El revisor independiente emitió **`disposition: ship`**, sin arreglos materiales requeridos dentro de este alcance. El build, las 84 pruebas unitarias frontend y las 44 pruebas Chromium de esta UI están aprobados **en la ejecución local recibida**. El backend de notificaciones tiene una confirmación de CI separada; ese resultado no acredita todavía el CI de la nueva UI.

## Continuidad con el sistema existente

`roommatch-web/src/styles.css` coincide íntegramente con `31867ab`: conserva **19 tokens `--rm-*`** y **Arial, Helvetica, sans-serif**. Se contrastaron los estilos locales anteriores de notificaciones con [notificaciones.css](../roommatch-web/src/app/pages/notificaciones/notificaciones.css). El revisor comparó además las capturas finales con `notifications-before`.

| Aspecto | Evidencia de la implementación | Relación con el incumbente |
| --- | --- | --- |
| Paleta | `--rm-primary` (`#4f46e5`), `--rm-primary-dark` (`#3730a3`), `--rm-purple-light` (`#ede9fe`), fondo claro y `--rm-surface` blanco | La ruta conserva la identidad violeta y reutiliza los tokens globales, sin modificarlos. |
| Texto y estados | `--rm-text`, `--rm-text-secondary`, pares `--rm-error*` y `--rm-success*` | Jerarquía y mensajes heredan los colores existentes. Los errores y el éxito se expresan también con texto y semántica. |
| Tipografía | Título `clamp(2rem,4vw,3rem)`, interlineado `1.15`, tracking `-.025em`; títulos de fila `1.125rem`; cuerpo `1.6`; metadatos `.85rem` | Se reduce localmente el título anterior, cuyo máximo era `4rem`, conservando la familia. Los controles heredan `font`; no se crea una escala global. |
| Controles | Botones nativos, mínimo `44px`, radio `8px`, padding `.65rem .85rem`; foco de `3px` con separación de `3px` | Controles familiares y foco violeta. La acción global usa un relleno sólido del primario; se retiran su gradiente y sombra anteriores sin cambiar la marca. |
| Filas | Fondo blanco, borde `--rm-border`, radio `12px`, padding `1.25rem`; sin sombra ornamental | La separación depende de borde, espacio y jerarquía. El estado pendiente añade fondo local `#faf9ff`, borde `#cfc8ff` y etiqueta «Sin leer»; no depende solo del color. Estos dos literales locales no se elevan a tokens globales. |
| Distribución | Contenedor máximo `1060px`; fila con icono, contenido flexible y acciones; cambio de disposición a `760px` | En móvil, encabezado y resumen se apilan y las acciones pasan a una línea propia que puede envolver. El contenido admite saltos de palabras largas. Los iconos reutilizan el componente SVG existente. |

La modernización local elimina la ceja del encabezado y conserva contador, filtros y acción global en el primer tramo. No establece una nueva composición para otras rutas. `PRODUCT.md`, `DESIGN.md` y `.impeccable/design.json` seguían ausentes al recibir el trabajo; se informa la ausencia preexistente y no se crean ni reparan esos archivos.

## Lista, contador y acciones

La [plantilla](../roommatch-web/src/app/pages/notificaciones/notificaciones.html) usa una lista `ul` con filas `li`, encabezados, fecha en `time`, tipo legible y acciones explícitas. La fila completa no es el único control pulsable. La [lógica de la página](../roommatch-web/src/app/pages/notificaciones/notificaciones.ts) gestiona los estados por separado.

| Operación | Comportamiento comprobado |
| --- | --- |
| Todas | Solicita `page` y `size=10`; omite el parámetro `leido`. Usa `totalElements`, `totalPages` y `number` de la respuesta, sin calcular los totales desde la página visible. |
| No leídas | Envía `leido=false` al servidor y vuelve a la primera página. No filtra únicamente los diez elementos ya descargados. Los botones de filtro pertenecen a un grupo con nombre y exponen `aria-pressed`. |
| Paginación | «Anterior» y «Siguiente» solicitan páginas del servidor y anuncian «Página N de M». Sus límites proceden de la respuesta. Se bloquean cambios durante una mutación; la lógica también evita navegar mientras carga. |
| Total sin leer | Usa una petición distinta a `/no-leidas/count`, independiente del filtro y de la página. Si falla, queda indisponible y ofrece «Reintentar contador»; no se sustituye por cero ni se oculta una lista válida. |
| Marcar leída | Envía el cambio, valida que la respuesta corresponda a la notificación y confirme `leido=true`, y después recarga lista y contador. Mientras procesa se evitan mutaciones duplicadas. Un rechazo conserva la fila y su estado sin leer, con mensaje y posibilidad de reintento. |
| Marcar todo como leído | Se habilita según el total sin leer del servidor, incluso si la página visible ya está leída. Se deshabilita mientras carga, cuenta o procesa, y cuando el total es cero o desconocido. El mensaje de éxito usa la cantidad devuelta; luego consulta otra vez el estado real, sin asumir que el contador quedó en cero. |
| Ver | Solo se ofrece cuando existe un destino interno admitido. Si la notificación no está leída, primero confirma su lectura y después navega; si falla la escritura, no navega. Una fila ya leída puede navegar directamente. |

[NotificacionService](../roommatch-web/src/app/core/services/notificacion.service.ts) utiliza `GET /notificaciones`, `GET /notificaciones/no-leidas/count`, `PUT /notificaciones/{id}/leer` y `PUT /notificaciones/leer-todas` bajo `API_BASE_URL`. La UI no envía un identificador arbitrario de propietario. El contrato y la validación del servidor se describen en [NOTIFICATIONS.md](NOTIFICATIONS.md).

Las peticiones anteriores de lista y contador se cancelan al sustituirlas y al destruir la ruta. Si una página solicitada mayor que cero queda vacía, se consulta una sola vez la última página disponible según la respuesta; esta recuperación no es recursiva. Los destinos se limitan a rutas internas que comienzan con una sola `/`, sin barras invertidas, espacios o controles; fuentes externas o mal formadas no generan el botón «Ver».

## Estados y recuperación

| Estado | Resultado visible y semántica |
| --- | --- |
| Carga de lista | Región con `aria-busy` y «Cargando notificaciones…» con `role="status"`. |
| Fallo de lista | Error con `role="alert"` y «Reintentar notificaciones». No aparece a la vez un mensaje de éxito vacío como «Estás al día». |
| Lista vacía válida | «Todavía no tienes notificaciones» en Todas; «Estás al día» en No leídas, con explicación correspondiente y `role="status"`. |
| Contador en consulta | «Consultando total sin leer…» dentro de una región `aria-live="polite"`. |
| Fallo de contador | Indisponibilidad explícita y «Reintentar contador». Reintentar esa operación no obliga a recargar la lista. |
| Fallo de acción | Mensaje separado de los errores de lista y contador; la acción vuelve a estar disponible al terminar la petición fallida. |
| Acción confirmada | Mensaje con `role="status"`; lista y contador se actualizan desde el servidor o se navega al destino interno tras confirmar la lectura. |

Los nombres accesibles de las acciones incluyen el título de la notificación. Los botones conservan foco visible y funcionamiento de teclado. La etiqueta «Sin leer» y los estados textuales complementan el color; estas medidas puntuales no constituyen una certificación de accesibilidad completa.

## Capturas y distribución de evidencia

La inspección visual acotada ya estaba cerrada al recibir este pase documental. El revisor abrió y validó las cuatro capturas finales y comparó las dos capturas anteriores. Aquí se comprobaron sus rutas y dimensiones, sin nuevas capturas ni otra búsqueda de defectos.

Las rutas siguientes son relativas a `.impeccable/review/`, directorio excluido de Git. Se registran como texto para evitar enlaces a PNG ausentes en GitHub. Los viewports fueron `1440 × 1000` y `390 × 844`; las alturas móviles mayores corresponden a páginas completas.

| Evidencia | Escritorio | Móvil |
| --- | --- | --- |
| Lista final | `notifications/desktop.png` · 1440 × 1000 | `notifications/mobile.png` · 390 × 1264 |
| Error de lista final | `notifications-error/desktop.png` · 1440 × 1000 | `notifications-error/mobile.png` · 390 × 867 |
| Referencia anterior | `notifications-before/desktop.png` · 1440 × 1000 | `notifications-before/mobile.png` · 390 × 1511 |

[e2e/notifications.spec.ts](../roommatch-web/e2e/notifications.spec.ts) adjunta las capturas finales al informe `playwright-report/index.html`. El workflow distribuye ese informe y `test-results/` mediante **`frontend-browser-reports`**, con retención de 14 días y publicación incluso ante fallos. [BROWSER_TESTS.md](BROWSER_TESTS.md) explica la ejecución y los artefactos. Este mecanismo de CI no implica que el cambio local de UI ya tenga una ejecución publicada y aprobada.

## Veredicto y validación

El archivo local `../audit-work/notifications-finish-review.md`, relativo a la raíz del checkout, conserva los cinco apartados `persistence`, `fidelity`, `ceiling`, `material_fixes` y `keep`. Su disposición es **`ship` para `/notificaciones`**, con TYPE, MATERIAL y GROUND en `match`, adaptación de jerarquía y controles, y ningún arreglo material requerido dentro de la evidencia revisada.

| Evidencia | Resultado | Alcance |
| --- | --- | --- |
| `../audit-work/notifications-tests.log` | **84 unitarias frontend aprobadas en 25 archivos** | Total de la suite local. Los nueve casos específicos de página y servicio cubren filtro y contador del servidor, respuesta mal formada, reintentos, cancelación, duplicados, página desaparecida, marcado global con nueva actividad y navegación tras lectura confirmada. |
| `../audit-work/notifications-final-browser.log` | **44 pruebas Chromium aprobadas**, incluidas **10 de notificaciones** | Cinco escenarios en escritorio y móvil: presentación bajo CSP, filtro y paginación más allá de la primera página, error de lista, error independiente del contador y marcado por teclado con fallo/reintento. |
| `../audit-work/notifications-final-build.log` | Build frontend completado | Conserva el warning heredado de `mis-publicaciones.css`: 14.06 kB frente al presupuesto de 12 kB. Es ajeno a este alcance. |
| `../audit-work/notifications-detector.json` | `[]` | Resultado de la única pasada entregada; no se ejecutó otro detector durante la documentación. |
| CI backend `36339247668` | **GREEN: 186 unitarias y 20 de integración** | Confirmación posterior del handoff. Incluye el backend de notificaciones y una medición de **2 consultas SQL para una página de 10**; no es una medida de latencia ni acredita el CI de la UI local. |

El informe visual se redactó cuando el CI backend todavía estaba pendiente. La confirmación posterior del handoff resuelve ese punto del servidor; se registra por separado para no atribuir al revisor un resultado que aún no tenía ni presentar la UI nueva como aprobada en CI antes de su publicación.

Fuentes de pruebas leídas: [notificaciones.spec.ts](../roommatch-web/src/app/pages/notificaciones/notificaciones.spec.ts), [notificacion.service.spec.ts](../roommatch-web/src/app/core/services/notificacion.service.spec.ts) y la suite de navegador enlazada arriba. No se repitieron las suites en este pase documental.

## Límites

Los escenarios Chromium utilizan actividad sintética, sesión de prueba y API interceptada. La primera página completamente leída y los pendientes posteriores prueban el contrato de interacción de la UI; no representan actividad real de cuentas. Las capturas usan notificaciones sin destino: la secuencia lectura→navegación está sustentada por código y pruebas unitarias, no por una navegación visible en esas imágenes.

El CI backend y las pruebas locales de UI son evidencias separadas. No se ha acreditado aquí un recorrido navegador→API viva→SQL Server de esta nueva UI en despliegue. La revisión tampoco certifica WCAG AA, lectores de pantalla, zoom, todos los navegadores o dispositivos, ni las demás rutas de RoomMatch.

Se conservan el sistema global y los archivos documentales ausentes. El warning CSS heredado y las limitaciones se registran sin convertirlos en reglas ni repararlos. Esta entrega escribe únicamente `docs/NOTIFICATIONS_UI.md`, sin cambios de código, otros documentos o commits.
