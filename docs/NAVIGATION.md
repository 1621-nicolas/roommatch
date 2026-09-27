# Navegación y contexto de propietario

La navegación principal mantiene Inicio, Habitaciones, Publicaciones, Matches
y Solicitudes. Contactos, Favoritos, Notificaciones, publicaciones propias,
privacidad y opciones de propietario están en «Mi cuenta». Administración
sigue disponible para ADMIN; la autorización real permanece en el backend.

El menú usa enlaces y botones nativos, `aria-expanded`, controles asociados,
estado de ruta actual, cierre con Escape y devolución del foco al botón.
El menú móvil y el de cuenta no se abren simultáneamente. El desplegable
también se cierra al salir con el teclado o hacer clic fuera. La aplicación
incluye un enlace para saltar al contenido. No se introduce un `role=menu`
que exigiría un patrón de teclado distinto al de navegación por enlaces.

La consulta de propietario conserva el contrato `success/null` para quien
todavía no tiene ese perfil. Un error HTTP o una respuesta inválida se muestra
con reintento, en lugar de sugerir incorrectamente registrarse otra vez.
Al cerrar sesión o establecer un perfil recién creado se cancela cualquier
respuesta anterior pendiente, evitando reintroducir datos del contexto viejo.

Se reemplazó el CSS encapsulado de App que intentaba estilizar elementos
internos de Navbar; Navbar ahora posee sus propios estilos. Los estilos
globales históricos aún requieren limpieza de selectores sin consumidores.

Ocho pruebas nuevas verifican teclado, foco, destinos conservados, exclusión
de menús, errores recuperables y cancelación de respuestas antiguas. Con este
grupo pasan 62 pruebas frontend y el build. Falta validar visualmente los
puntos de ruptura, zoom y lectura asistida en un navegador real; estas pruebas
no constituyen una certificación de accesibilidad.
