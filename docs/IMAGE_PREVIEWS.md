# Imágenes de los anuncios públicos

`HabitacionResponse` y `PublicacionRoomieResponse` incluyen
`imagenPrincipal: string | null`. El servidor selecciona la imagen principal
válida; si la principal histórica tiene una URL no permitida, utiliza la
primera válida según orden e ID. Sin fotos válidas devuelve `null`.

La búsqueda carga las fotos de los IDs de la página en una sola consulta
escalar. No inicializa una entidad padre por imagen, no descarga las URLs y
no añade una petición HTTP por tarjeta. La visibilidad del anuncio se resuelve
antes de añadir sus fotos. Una habitación pausada, bloqueada o no disponible
sigue devolviendo 404 en el detalle público y su galería.

La política de URL de las galerías también se aplica a la miniatura: HTTPS en
producción, sin credenciales ni fragmentos, hasta 255 caracteres codificados.
Los datos históricos inválidos se conservan para corregirlos, sin publicarlos.
Una URL válida puede dejar de existir o alojar otro contenido; el navegador
debe mostrar un estado de imagen no disponible y no una foto inventada.

## Verificación

- Unitarias: selección de principal/fallback, URL histórica no permitida,
  normalización, página de 50 elementos con una sola carga de imágenes,
  IDs vacíos/duplicados y visibilidad antes de consultar fotos.
- Integración SQL Server: 51 habitaciones de propietarios distintos, fotos
  principales/ausentes/inválidas y páginas de 10/20/50; presupuesto de tres
  consultas (datos con relaciones necesarias, total e imágenes).
- Publicaciones: se amplía el test existente de 10/20/50 para comprobar fotos
  reales de BD y máximo cinco consultas. La quinta es la carga de imágenes;
  las cuatro anteriores siguen siendo página, total, compatibilidad y
  referencias de vivienda. Es una medición de consultas, no un SLA de carga.

La compilación/prueba Maven local del 26/09 quedó bloqueada antes de compilar
por DNS de Maven Central. [GitHub Actions con SQL Server](https://github.com/1621-nicolas/roommatch/actions/runs/36246998165)
aprobó 184 pruebas unitarias y 19 de integración, sin fallos ni omisiones.
Las consultas medidas fueron tres para habitaciones y cinco para publicaciones
en los tres tamaños de página.

## Presentación en Angular

Los catálogos consumen `imagenPrincipal` directamente, sin consultar una galería
por tarjeta. El detalle consulta su galería una sola vez y permite seleccionar
la foto. `ListingImage` valida la URL antes de insertarla, usa `no-referrer` y
reserva espacio; un error de descarga muestra texto de recuperación, sin foto
inventada. `ListingGallery` diferencia error del API, carga y galería vacía,
ofrece reintento y cancela peticiones al cambiar de anuncio o salir.

La revisión y los límites visuales están en [GALLERY_UI.md](GALLERY_UI.md).
La validación local del grupo aprobó 75 pruebas unitarias frontend y 34 de
Chromium (12 de galerías), con cero fallos u omisiones. Las fotos sintéticas
rotuladas de las pruebas se limitan a `e2e/`; el producto muestra URLs del API.
El recorrido de navegador utiliza fixtures; no sustituye una prueba completa
navegador → API → SQL Server.
