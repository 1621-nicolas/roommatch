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

La compilación/prueba Maven local del 26/09 está bloqueada antes de compilar
por DNS de Maven Central. La ejecución de GitHub Actions con SQL Server es el
gate de aceptación de este cambio. La conexión visual de estas fotos se
entrega en el siguiente grupo de frontend.
