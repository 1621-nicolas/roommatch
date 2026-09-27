# Galerías públicas: implementación y revisión visual

Documentado el 27 de septiembre de 2026 con evidencia de la ejecución del 26 de septiembre. Alcance: imágenes de anuncios, galerías e iconos de los catálogos y detalles de habitaciones y publicaciones de roomies.

El veredicto independiente recibido es **`disposition: ship` para este grupo completo**, sin arreglos materiales pendientes dentro de su alcance. La dirección es **Operate**: consultar fotos con controles familiares y conservar el resto del anuncio cuando una imagen o la consulta falla. Este documento no amplía ese veredicto a todo RoomMatch.

## Continuidad del sistema existente

Se comparó `roommatch-web/src/styles.css` con `31867ab`: el archivo coincide íntegramente, incluidos sus **19 tokens `--rm-*`** y la familia **Arial, Helvetica, sans-serif**. Las ocho capturas mantienen el acento violeta, los fondos claros y las tarjetas blancas de las superficies existentes.

| Elemento observado | Implementación de galería | Relación con el sistema incumbente |
| --- | --- | --- |
| Color de controles | Selección `#4f46e5`, texto y borde al pasar el puntero `#3730a3`, borde base `#cbd5e1`, superficie blanca | Coinciden con `--rm-primary`, `--rm-primary-dark`, `--rm-border-input` y `--rm-surface`. El componente los expresa como literales; no modifica ni añade tokens globales. |
| Texto | Controles con `font: inherit`; párrafos con interlineado `1.6`; contador de `.9rem` con cifras tabulares | Hereda la familia existente. En las páginas de publicaciones, el ajuste local de `letter-spacing` de títulos pasa de `-.045em` a `-.025em`; no cambia la familia ni crea una escala global. |
| Forma y foco | Imagen de detalle con radio `12px`; botones de `8px`, altura mínima `44px`, padding `.65rem .9rem`; foco de `3px` con separación de `3px` | Conserva controles redondeados y el tratamiento violeta del foco. Los botones envuelven con `flex-wrap` y separación de `.5rem`. |
| Marco de imagen | Proporción `16:10`; `cover` en tarjetas y `contain` en detalle; fondo local `#eef1f6` | Reserva un marco estable y permite inspeccionar la imagen completa en detalle. Ese fondo local no se convierte en un nuevo token de marca. |
| Error | Panel local con borde `#e3a0a0`, fondo `#fff5f5`, radio `12px` y texto explicativo | Distingue el fallo de una galería vacía. Los valores se documentan como implementación local, sin promoverlos a una norma global. |
| Composición | Imagen al inicio de la tarjeta; galería antes de los datos del detalle; columna lateral en escritorio y flujo apilado en móvil | Conserva la estructura existente y sus fondos, bordes y sombras. La galería no añade una sombra propia ni un nuevo sistema de composición. |

Los estilos de las cuatro superficies se contrastaron con `31867ab`. El catálogo de habitaciones usa ahora `rooms-catalog-card` para evitar colisiones con `.room-card`; los detalles usan contenedores `div` en lugar de `main` anidados. Estos cambios locales no constituyen una identidad nueva.

`PRODUCT.md`, `DESIGN.md` y `.impeccable/design.json` seguían ausentes al recibir este pase. Se informa esa ausencia preexistente y se conserva el sistema observable, siguiendo el handoff de extensión ordinaria de Impeccable. No se crean reglas de marca, una metáfora, un archivo de tokens paralelo ni un sidecar.

## Componentes, datos y estados

| Pieza | Comportamiento comprobado en el código |
| --- | --- |
| [ListingImage](../roommatch-web/src/app/shared/components/listing-image/listing-image.ts) | Recibe la URL del anuncio, aplica `imageUrl`, reserva `16:10` y utiliza `loading="lazy"`, `decoding="async"` y `referrerpolicy="no-referrer"`. El texto alternativo procede del título; en detalle incluye el número de foto. Un error de descarga retira el `img` y muestra un mensaje, sin reintento infinito. Un cambio de entrada reinicia ese estado. |
| [ListingGallery](../roommatch-web/src/app/shared/components/listing-gallery/listing-gallery.ts) | Consulta las fotos del anuncio, selecciona la marcada como principal o la primera si no la hay, y muestra botones nativos «Foto N» con `aria-pressed`. El grupo tiene nombre accesible y el contador «Foto N de M» usa `role="status"`. Cambiar de foto actualiza la imagen sin volver a consultar la galería. Cancela la petición anterior al cambiar de anuncio o destruir el componente. |
| [ListingGalleryService](../roommatch-web/src/app/core/services/listing-gallery.service.ts) | Consulta `/imagenes-habitacion/habitacion/{id}` o `/publicaciones-roomie/{id}/imagenes` bajo la base del API. Valida el identificador y la forma de la respuesta, luego ordena por `orden` e `idImagen` sin mutar el array recibido. Una colección mal formada produce error, no una galería vacía exitosa. |
| [imageUrl](../roommatch-web/src/app/core/validation/image-url.ts) | Valida sintaxis antes de asignar la fuente al DOM: admite HTTPS en producción y HTTP también en desarrollo; rechaza valores vacíos, demasiado largos, con espacios internos o controles, credenciales, fragmento o puerto cero. Esta comprobación no garantiza la disponibilidad ni el contenido del servidor externo. |

Los catálogos utilizan `imagenPrincipal` de la respuesta del anuncio: no hacen una petición de galería por tarjeta. Habitaciones monta `ListingImage` aunque no haya URL y obtiene su estado sin foto; publicaciones solo monta ese componente cuando existe `imagenPrincipal`, conservando la tarjeta sin bloque fotográfico cuando falta.

El contrato del backend limita cada anuncio a **cinco imágenes**: `ImageUrlPolicy.position` rechaza una sexta y la migración `V9__gallery_invariants.sql` conserva las invariantes de orden y principal. La UI genera un botón por imagen recibida —«Foto 1» a «Foto 5» bajo ese contrato—; no impone por sí sola la cuota ni recorta silenciosamente la respuesta. Las capturas y fixtures de este pase usan dos fotos.

| Estado | Presentación y recuperación |
| --- | --- |
| Consulta en curso | Región «Fotos del anuncio» con `aria-busy`; «Cargando fotos…» con `role="status"`. |
| Consulta fallida o respuesta inválida | Mensaje con `role="alert"` y botón «Reintentar fotos». El resto del anuncio permanece disponible. |
| Consulta exitosa sin fotos | «Este anuncio todavía no tiene fotos.»; no se presenta como un fallo de red. |
| Fuente ausente o inválida en `ListingImage` | «Sin foto disponible». |
| Descarga de imagen rota | «No se pudo cargar esta foto». Los botones de selección permanecen disponibles para consultar otra imagen. |
| Foto disponible | Marco reservado, texto alternativo, selección identificada y contador; recorte `cover` en catálogo y encaje `contain` en detalle. |

Fuentes de integración: [Habitaciones](../roommatch-web/src/app/pages/habitaciones/habitaciones.html), [detalle de habitación](../roommatch-web/src/app/pages/habitacion-detalle/habitacion-detalle.html), [publicaciones](../roommatch-web/src/app/pages/publicaciones-roomie/publicaciones-list/publicaciones-list.html) y [detalle de publicación](../roommatch-web/src/app/pages/publicaciones-roomie/publicaciones-detail/publicaciones-detail.html), junto con sus archivos TypeScript y CSS. El límite procede de [ImageUrlPolicy](../roommatch-api/roommatch-api/src/main/java/com/roommatch/service/ImageUrlPolicy.java) y [V9](../roommatch-api/roommatch-api/src/main/resources/db/migration/V9__gallery_invariants.sql).

## Procedencia de imágenes e iconos

La aplicación consume URLs externas proporcionadas por el API. Esta extensión no introduce fotografías inventadas ni imágenes de demostración en producción. `no-referrer` se aplica a las fotos de anuncios renderizadas por `ListingImage`; no es una garantía de anonimato ante el alojamiento de imágenes ni se extiende automáticamente a otros `img`.

[Icon](../roommatch-web/src/app/shared/components/icon/icon.ts) incorpora 14 iconos mediante paths SVG de **Bootstrap Icons 1.13.1**, procedentes del paquete instalado. Se verificó que los 14 conjuntos de paths coinciden con sus SVG originales y que [LICENSE.bootstrap-icons.txt](../roommatch-web/src/app/shared/components/icon/LICENSE.bootstrap-icons.txt) coincide con la licencia MIT del paquete. Los SVG usan `currentColor`, un marco de `1em`, `aria-hidden="true"` y `focusable="false"`; las acciones conservan etiquetas de texto. No se usan esos iconos como sustitutos de fotografías.

Los SVG con «Foto de prueba de galería» están definidos exclusivamente en [e2e/gallery.spec.ts](../roommatch-web/e2e/gallery.spec.ts). Las peticiones a `images.example` y al API se interceptan en las pruebas. Ese material rotulado permite comprobar renderizado, selección y fallos; no representa inmuebles reales ni se importa desde `src/`.

## Capturas y distribución de evidencia

Se abrieron las ocho capturas entregadas y se contrastaron con los componentes: corresponden a las cuatro superficies indicadas, con imágenes en los catálogos y galería con controles en los detalles. Los viewports son `1440 × 1000` y `390 × 844`; la altura de cada PNG corresponde a una captura de página completa.

Las rutas siguientes son relativas a `.impeccable/review/`, directorio local excluido de Git. Se mantienen como texto para no crear enlaces a PNG ausentes en GitHub.

| Superficie | Escritorio | Móvil |
| --- | --- | --- |
| Catálogo de habitaciones | `rooms-with-photos/desktop.png` · 1440 × 1350 | `rooms-with-photos/mobile.png` · 390 × 1950 |
| Galería de habitación | `room-gallery/desktop.png` · 1440 × 1759 | `room-gallery/mobile.png` · 390 × 3116 |
| Catálogo de publicaciones | `publications-with-photos/desktop.png` · 1440 × 1682 | `publications-with-photos/mobile.png` · 390 × 2266 |
| Galería de publicación | `publication-gallery/desktop.png` · 1440 × 1763 | `publication-gallery/mobile.png` · 390 × 2305 |

La suite adjunta estas capturas al informe `playwright-report/index.html`. CI publica el informe y `test-results/` en el artefacto **`frontend-browser-reports`**, con retención de 14 días, incluso ante fallos. [BROWSER_TESTS.md](BROWSER_TESTS.md) describe cómo ejecutar la suite y obtener sus artefactos. La ejecución documentada aquí registra 34 pruebas en total, incluidas las 12 de galerías.

## Veredicto, validación y límites

El informe local `../audit-work/gallery-finish-review.md`, relativo a la raíz del checkout, contiene los cinco apartados de la revisión independiente: `persistence`, `fidelity`, `ceiling`, `material_fixes` y `keep`. Declara **`ship` para imágenes de anuncios, galerías e iconos en estas cuatro superficies**, con TYPE, MATERIAL y GROUND en `match`, adaptación móvil verificada y ningún arreglo material requerido en ese alcance.

| Evidencia recibida | Resultado | Alcance de lo comprobado |
| --- | --- | --- |
| `../audit-work/gallery-build.log` | Build completado | Compilación frontend. Permanece el warning heredado de presupuesto CSS en `mis-publicaciones.css`: 14.06 kB frente a 12 kB. No se repara ni se convierte en una regla de diseño en este pase. |
| `../audit-work/gallery-tests.log` | **75 unitarias aprobadas en 23 archivos** | Incluye carga, selección principal, cambio de foto, cancelación de peticiones anteriores, error/reintento/vacío, fuente inválida, descarga rota, orden y respuesta mal formada. El total corresponde a la suite, no a 75 pruebas exclusivas de galería. |
| `../audit-work/gallery-final-browser.log` | **34 pruebas Chromium aprobadas**, incluidas **12 de galerías** | Cuatro superficies y dos escenarios de recuperación en ambos viewports. Las pruebas de galería comprueban imagen cargada, selección, cantidad de consultas, ausencia del encabezado `Referer` en las peticiones interceptadas, errores JavaScript y desbordamiento horizontal. |
| `../audit-work/audit-gallery.json` | `metadata.vulnerabilities.total: 0` | Instantánea del audit de dependencias recibida; no equivale a una certificación de seguridad de la aplicación. |
| `../audit-work/gallery-detector.json` | Dos avisos de imagen «sin src» | El revisor los contrastó con las plantillas y confirmó falsos positivos del diagnóstico: los avatares usan `[src]="publicacion.foto"` dentro de `@if (publicacion.foto)`, con iniciales como alternativa. No se ejecutó de nuevo el detector. |

Los avatares preexistentes no usan `ListingImage`; los avisos descartados no acreditan para ellos el tratamiento de errores o `no-referrer` de las fotos de anuncios. Se conserva esa distinción sin ampliar el alcance del arreglo.

Este pase documental no ejecutó nuevamente las suites, no modificó código y no volvió a abrir una búsqueda de defectos. La evidencia utiliza fixtures y respuestas interceptadas: no valida una API viva, SQL Server, autorización real, disponibilidad futura de alojamientos externos ni fidelidad fotográfica de inmuebles reales. Tampoco certifica WCAG AA, lectores de pantalla, zoom o todos los navegadores y dispositivos.

Los filtros locales, el cálculo de compatibilidad y las afirmaciones heredadas de contacto no se aprueban por el hecho de aparecer junto a la galería. No se documenta ni revisa el grupo de notificaciones. Las ausencias documentales, el warning CSS y las observaciones heredadas indicadas se registran sin canonizarlos ni repararlos, porque esta entrega conserva el sistema y se limita a `docs/GALLERY_UI.md`.
