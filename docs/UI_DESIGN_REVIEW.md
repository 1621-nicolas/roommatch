# Revisión de continuidad visual de RoomMatch

Fecha: 26 de septiembre de 2026. Modalidad: extensión y refinamiento del sistema existente, con criterio **Operate**: completar tareas con controles familiares, información legible, privacidad explícita y recuperación de errores.

La implementación conserva la identidad violeta de RoomMatch. La comparación de `roommatch-web/src/styles.css` con el commit incumbente `c65308e` encuentra **19 propiedades `--rm-*` con los mismos nombres y valores**, sin altas, bajas ni cambios. También conserva `Arial, Helvetica, sans-serif`. Esta revisión documenta esa continuidad y el alcance de la evidencia; no establece una identidad nueva ni autoriza cambios globales.

## Alcance y autoridad

Se examinaron Home público y autenticado, inicio de sesión, registro, navegación principal y de cuenta, contactos, resumen administrativo, reportes y el diálogo de recuperación ante HTTP 409. Las galerías públicas y sus componentes, todavía en implementación durante este pase, quedan fuera de la revisión y de sus resultados.

`PRODUCT.md`, `DESIGN.md` y `.impeccable/design.json` no existían al recibir este trabajo. La ausencia documental es preexistente: el código y sus capturas constituyen la evidencia del sistema incumbente. Se preservó esa situación, sin crear una metáfora de marca, un sistema de tokens paralelo ni un sidecar. Este pase escribe únicamente este documento.

Se aplicó el criterio de evidencia de `Impeccable/reference/document.md`: registrar valores y patrones observables. El handoff de extensión ordinaria de `reference/new-work.md` exige conservar el sistema y reportar lo comprobado; no corresponde ejecutar una entrevista de identidad ni reconstruir `DESIGN.md` como efecto secundario.

## Sistema observado y comparación

La referencia histórica se verificó con `git show c65308e:roommatch-web/src/styles.css`; los valores siguientes proceden del archivo actual, contrastado con esa versión. Son una descripción de la implementación, no una nueva especificación normativa.

| Aspecto | Evidencia observable | Continuidad y aplicación en el alcance |
| --- | --- | --- |
| Acento | `--rm-primary: #4f46e5`, `--rm-primary-dark: #3730a3`, `--rm-secondary: #7c3aed`, `--rm-purple-light: #ede9fe` | Los cuatro tokens permanecen iguales. Las superficies examinadas usan principalmente el primario, su variante oscura y el fondo violeta claro en acciones, enlaces, avisos de ayuda e iniciales. |
| Fondo y texto | `--rm-background: #f8fafc`, `--rm-surface: #ffffff`, `--rm-text: #0f172a`, `--rm-text-secondary: #475569`, `--rm-text-muted: #64748b` | Se mantienen los cinco valores. Fondo claro, superficies blancas y texto oscuro conservan el lenguaje existente. Los textos de apoyo locales usan `--rm-text-secondary`. |
| Límites de controles | `--rm-border: #e2e8f0`, `--rm-border-input: #cbd5e1` | Se conservan para separadores, tarjetas, tablas, campos y botones secundarios. |
| Estados | Éxito `#15803d` / `#dcfce7`; advertencia `#b45309` / `#fef3c7`; error `#991b1b` / `#fee2e2` | Los pares `--rm-success*`, `--rm-warning*` y `--rm-error*` siguen iguales. Administración distingue estado pendiente, acciones de sanción y mensajes de error con texto además de color. |
| Profundidad | `--rm-shadow: 0 12px 28px rgba(15, 23, 42, 0.08)`; `--rm-shadow-large: 0 18px 46px rgba(15, 23, 42, 0.08)` | Ambos tokens permanecen iguales. En este alcance predominan bordes y fondos; el menú de cuenta usa `--rm-shadow-large`. El diálogo mantiene su sombra local `0 20px 65px rgb(15 23 42 / .25)` y un backdrop `rgb(15 23 42 / .5)`. |
| Tipografía | Familia global `Arial, Helvetica, sans-serif`; formularios y controles heredan `font` | No se incorporan familias nuevas. Home usa `h1: clamp(2rem, 4vw, 3.25rem)`; auth, contactos y administración usan títulos entre `1.8rem` y `2.5rem`. Los `h2` locales oscilan entre `1.125rem` y `1.5rem`, con párrafos de interlineado `1.6`–`1.65`. Se registra lo existente sin convertir cada tamaño local en un token global. |
| Forma e interacción | Botones y campos de `8px` de radio en auth, contactos, administración y navbar; acciones Home de `10px`; tarjetas de `12px`–`14px`; paneles de ayuda de `16px` | Se mantiene el lenguaje redondeado. Los controles principales observados tienen altura mínima de `44px`; campos y envío de auth, `48px`. El foco visible local usa un contorno de `3px` con `--rm-primary` y separación de `3px` o `4px`. |

Fuentes de estilos comprobadas:

- [Estilos globales](../roommatch-web/src/styles.css).
- [Home](../roommatch-web/src/app/pages/home/home.css) y [formularios de autenticación](../roommatch-web/src/app/pages/auth/auth-form.css).
- [Contactos](../roommatch-web/src/app/pages/contactos/contactos.css) y [administración](../roommatch-web/src/app/pages/admin/admin.css).
- [Navbar](../roommatch-web/src/app/shared/components/navbar/navbar.css).

Los contenedores siguen centrados: Home y administración llegan a `1180px`; auth y contactos, a `1040px`; el interior de navegación, a `1228px`. Los ajustes son locales: Home pasa de tres a dos y una columna a `900px` y `600px`; auth apila a `760px`; contactos a `700px`; administración a `720px`; el menú principal se contrae a `1050px`. No se añadió una escala global de espaciado o breakpoints.

## Comportamiento reflejado en la interfaz

| Superficie | Evidencia de continuidad y uso |
| --- | --- |
| Home público | Acciones de búsqueda reconocibles, explicación de conexión aceptada y datos que cada persona decide compartir. Las capturas muestran contenido de prueba identificado, precio en `S/` y ubicaciones peruanas. |
| Home autenticado | Accesos a actividad, perfil y privacidad; resumen y secciones de resultados con carga, vacío y error separados. `home.html` ofrece reintentos por sección y conserva encabezados y enlaces visibles. |
| Login y registro | Etiquetas asociadas a campos, tipos nativos, `autocomplete`, ayuda de contraseña y botones con estado de envío. Los errores usan `role="alert"`; los avisos, `role="status"`. La captura de registro explica qué contacto se comparte, sin simular resultados de compatibilidad. |
| Navbar | Marca y acento existentes, enlaces convencionales, estado de ruta subrayado, botones de cuenta y menú móvil con `aria-expanded` y `aria-controls`. La prueba de teclado cubre Enter, Tab, Escape y retorno del foco al botón de cuenta. |
| Contactos | Explicación visible del consentimiento, enlace a configuración y distinción entre una conexión aceptada y la ausencia de datos compartidos. `contactos.ts` formatea fechas con `es-PE`; `contactos.html` da nombres accesibles a las acciones y anuncia nuevas pestañas. Esto describe la presentación, no certifica la autorización del servidor. |
| Administración | Métricas y estados con texto, tablas con encabezados, filtros nativos, historial y confirmación con motivo. Las columnas del dashboard usan `minmax(0,1fr)` y `min-width:0`; en móvil, sus tablas simples caben en la página. La tabla de reportes conserva scroll horizontal en su contenedor, con aviso visible y región enfocable. |
| Conflicto 409 | Se conserva el motivo y se explica cómo recuperar el estado. El diálogo reemplaza «Confirmar decisión» por «Actualizar estado». Al recuperar, cerrar o pulsar Escape, el código conserva un borrador identificado, elimina la selección obsoleta, consulta todos los estados y lleva el foco al título de la lista. `confirm()` bloquea nuevos envíos mientras existe conflicto. |

Fuentes funcionales muestreadas: `home/home.html`, `auth/login/login.html`, `auth/register/register.html`, `contactos/contactos.html`, `contactos/contactos.ts`, `shared/components/navbar/navbar.html`, `admin/dashboard/dashboard.html`, `admin/reportes/reportes.html`, `admin/reportes/reportes.ts` y sus pruebas pertinentes, bajo `roommatch-web/src/app/`.

## Capturas examinadas

Se abrieron las 16 capturas recibidas. Sus nombres corresponden a las superficies mostradas y permiten contrastar la paleta, jerarquía, controles y adaptación con el código. El viewport de escritorio es `1440 × 1000`; el móvil, `390 × 844`. Las capturas de página completa tienen mayor altura según contenido; el diálogo se capturó dentro del viewport.

Las rutas de la tabla son relativas a `.impeccable/review/`, un directorio de evidencia local excluido de Git. Se registran como texto porque esos PNG no se publican dentro del repositorio. No son datos de producción ni una nueva prueba contra el backend.

Las capturas de navegador se adjuntan a `playwright-report/index.html`, que CI distribuye en el artefacto `frontend-browser-reports` durante 14 días. La forma de obtener y reproducir esa evidencia está documentada en [BROWSER_TESTS.md](BROWSER_TESTS.md).

| Superficie | Escritorio | Móvil |
| --- | --- | --- |
| Home público | `home/desktop.png` · 1440 × 1540 | `home/mobile.png` · 390 × 2314 |
| Login | `login/desktop.png` · 1440 × 1000 | `login/mobile.png` · 390 × 1091 |
| Registro | `register/desktop.png` · 1440 × 1000 | `register/mobile.png` · 390 × 1570 |
| Home autenticado | `signed-home/desktop.png` · 1440 × 1746 | `signed-home/mobile.png` · 390 × 2521 |
| Contactos | `contacts/desktop.png` · 1440 × 1011 | `contacts/mobile.png` · 390 × 1114 |
| Dashboard administrativo | `admin-dashboard/desktop.png` · 1440 × 1431 | `admin-dashboard/mobile.png` · 390 × 2188 |
| Reportes | `admin-reports/desktop.png` · 1440 × 1000 | `admin-reports/mobile.png` · 390 × 982 |
| Conflicto de moderación | `moderation-conflict/desktop.png` · 1440 × 1000 | `moderation-conflict/mobile.png` · 390 × 844 |

## Resultado de revisión y pruebas

El veredicto independiente disponible declara **`disposition: ship` y `resolved` para la recuperación del conflicto 409**. Su alcance es exclusivamente ese arreglo: motivo conservado, bloqueo de reenvío y recuperación explícita, con el diálogo completo en ambos viewports. No equivale a aprobación de toda la superficie ni de RoomMatch completo.

Los archivos locales recibidos `audit-work/impeccable-finish-review.md` e `audit-work/impeccable-conflict-verdict.md`, situados junto al checkout, contienen actualmente pases de veredicto del arreglo. No se dispone allí de la revisión inicial completa. Por ello no se reconstruyen sus puntuaciones ni se presenta una aprobación global como evidencia conservada.

| Validación existente | Resultado y fuente | Qué respalda |
| --- | --- | --- |
| Navegador | **22 pruebas aprobadas**, `audit-work/browser-conflict-playwright.log` | `e2e/interface.spec.ts`, ejecutado en Chromium para ambos viewports, con fixtures aislados y CSP. Comprueba las siete pantallas, etiquetas, un título principal, ausencia de errores de script/consola y desbordamiento de página, teclado de navbar, colección mal formada, credenciales rechazadas y recuperación 409. |
| Unitarias del grupo | **69 pruebas aprobadas en 20 archivos**, `audit-work/browser-conflict-tests.log` | Evidencia de la ejecución recibida. `reportes.spec.ts` comprueba que un segundo `confirm()` no vuelva a enviar y que recuperar conserve el motivo, cierre el diálogo y reinicie la selección y el filtro. No son 69 pruebas exclusivas del diseño. |
| Comparación documental | 19 tokens `--rm-*` y familia global sin cambios frente a `c65308e`; 16 capturas examinadas | Continuidad del sistema incumbente en las superficies incluidas. Este pase no volvió a ejecutar las suites ni modificó código. |

La configuración inspeccionada fija `locale: es-PE`, `timezoneId: America/Lima` y cero reintentos. [BROWSER_TESTS.md](BROWSER_TESTS.md) describe la CSP tomada de `deployment/nginx.conf`, el servidor de prueba, la ejecución y la separación de fixtures. Las galerías no forman parte de los 22 resultados citados.

## Límites y estado documental

- **No certifica WCAG AA.** Las etiquetas, el foco visible, las pruebas de teclado y las capturas aportan evidencia puntual; no sustituyen una auditoría completa de contraste, lectores de pantalla, zoom, reflow, tecnologías de asistencia o todos los estados.
- La evidencia de navegador usa peticiones interceptadas y datos sintéticos. No valida autorización real, concurrencia real de moderadores, API viva, SQL Server ni un recorrido completo en despliegue.
- Los dos tamaños y Chromium no acreditan todos los dispositivos o navegadores. Las capturas muestran estados concretos; las pruebas cubren únicamente las interacciones declaradas.
- Los tokens y la tipografía observados se describen sin convertir variantes locales, estilos heredados fuera del alcance o defectos eventuales en reglas para nuevas superficies.
- La falta preexistente de `PRODUCT.md`, `DESIGN.md` y sidecar se informa sin repararla. Tampoco se reabre una búsqueda de defectos ni se amplía el veredicto independiente a galerías o cambios posteriores.

Una modificación posterior de estas superficies requiere revisar qué evidencia sigue vigente antes de reutilizar esta conclusión.
