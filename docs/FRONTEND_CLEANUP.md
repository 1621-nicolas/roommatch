# Limpieza sin retirar funcionalidad

Se retiraron cinco componentes compartidos sin imports ni usos de selector:
`room-card`, `profile-card`, `compatibility-badge`, `notification-card` y
`report-modal`. Sus clases estaban vacías y sus plantillas solo contenían
«… works!». Las pantallas funcionales de habitaciones, perfiles,
compatibilidad, notificaciones y reportes se conservan. Pagination,
EmptyState e ImageManagerModal permanecen en uso.

Después de sustituir Inicio y Navbar, se eliminaron 163 reglas globales
asociadas a sus clases antiguas. Para cada rama del selector se comprobó
que hubiera al menos una clase exclusiva antigua ausente en todos los
archivos HTML/TypeScript de `src`. No se eliminaron reglas generales solo
por parecer redundantes ni se movió CSS al global para ocultar budgets.

Resultado local: CSS compilado global de 75,48 a 60,26 kB; bundle inicial de
416,51 a 401,29 kB. Las 62 pruebas frontend y el build pasan. Permanece el
warning de `mis-publicaciones.css` (14,06 kB frente a 12 kB). La validación
visual real y la separación de otros estilos de página siguen pendientes.
