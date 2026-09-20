# Inicio: datos reales y estados de carga

La página de inicio conserva la identidad morada de RoomMatch y los accesos
existentes, con estilos locales y una composición adaptable a móvil. La
búsqueda de habitaciones y de personas son las acciones principales. Las
publicaciones también se muestran a usuarios autenticados.

Se eliminaron las personas, porcentajes y habitaciones de ejemplo visibles
al público. No se fabrican fotos de habitaciones: el DTO del listado no
incluye una imagen. Los resultados enlazan a sus detalles reales. El perfil
existente se presenta como editable, sin afirmar que todos sus campos estén
completos. Los matches muestran tanto compatibilidad como cobertura.

Cada consulta tiene estados independientes de carga, error y ausencia de
resultados. Los errores incluyen reintento. Una respuesta con `content` que
no sea un array no se interpreta como lista vacía. Un perfil 404 significa
que todavía no existe; el contacto admite además `success` con `data: null`,
según su contrato actual. Un 500/503 no se interpreta como falta de perfil.
Las peticiones reemplazadas o pendientes al abandonar la pantalla se cancelan.
Las respuestas notifican a Angular para actualizar la vista asíncrona.

Cinco pruebas verifican estos contratos, la separación de estados, el
reintento visible y la cancelación. La primera ejecución detectó un error de
actualización de vista (`NG0100`); se corrigió notificando a ChangeDetectorRef,
sin desactivar las comprobaciones de Angular.

La comprobación visual en navegador de escritorio/móvil sigue pendiente:
compilar y probar el DOM no certifica el diseño ni conformidad WCAG AA.
También queda pendiente retirar los estilos globales antiguos de Inicio
cuando se compruebe que ninguna otra pantalla los reutiliza.
