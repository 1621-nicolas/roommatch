# Reglas de negocio verificables

Estas decisiones conservan las funciones existentes y aplican el encargo de endurecimiento.
Los cambios de política se concentran en configuración, pruebas y este documento.

## Solicitudes de contacto

- Cada envío es un intento nuevo; el historial se conserva.
- Puede existir como máximo una solicitud `pendiente` entre dos personas, sin importar dirección.
- Solo quien recibe puede aceptar o rechazar. Solo quien envía puede cancelar.
- `aceptada`, `rechazada` y `cancelada` son terminales; no se reescribe un intento anterior.
- La aceptación crea un contacto bilateral una sola vez y respeta las preferencias de visibilidad.
- El mismo emisor puede reintentar al cumplirse 30 días desde un rechazo o 24 horas desde una cancelación.
  Son límites antiinsistencia configurables con `app.solicitudes.espera-rechazo` y
  `app.solicitudes.espera-cancelacion`. La persona receptora puede iniciar un nuevo intento
  en la dirección contraria sin esa espera. Los límites generales de abuso siguen aplicándose.
- Una cuenta suspendida no puede enviar ni aceptar para desbloquear contacto.
- Los servicios bloquean los dos usuarios en orden ascendente durante la transacción.
  SQL Server protege además una pendiente por pareja y un contacto por pareja con índices únicos.
- V5 se detiene si descubre duplicados históricos; exige conciliarlos, sin eliminar filas automáticamente.

## Habitaciones y planes

- El cupo cuenta habitaciones activas y pausadas. Alquiladas y archivadas (`eliminada`) no consumen cupo.
- Un downgrade que no admite el inventario actual responde 409 sin cambiar plan ni habitaciones.
  El propietario decide qué marcar como alquilado o archivar. Una habitación archivada conserva
  imágenes, leads y reportes, y no puede reactivarse. Una alquilada puede reactivarse si hay cupo.
- Pausar solo pasa de activa a pausada, manteniendo cupo. Reactivar siempre vuelve a comprobarlo.
- Una fecha de fin alcanzada o un plan inactivo impiden publicación/reactivación y visibilidad pública.
  El catálogo académico todavía no procesa cobros: cambiar de plan no representa un pago ni genera
  facturas. No se inventa una renovación automática. Si hay fecha de fin, se respeta.
- Para pasar a un plan que no destaca, el propietario debe quitar antes sus destacados. Puede editar
  contenido y quitar destacados tras la expiración. V6 retira destacados históricos incompatibles
  con las capacidades de su plan; no elimina habitaciones.
- Todas las operaciones de cupo y suscripción bloquean el mismo propietario. La BD admite una sola
  suscripción activa por propietario; `@Version` evita sobrescribir sanciones con una edición concurrente.
- Una sanción tiene una marca separada (`bloqueada`) y no se revierte con pausar/activar. V6 recupera
  sanciones históricas desde los reportes sancionados. Su revisión corresponde a administración.
- Crear un perfil de propietario conserva el rol ADMIN. El modelo continúa con un rol de usuario
  más la capacidad asociada al perfil de propietario; no incorpora una tabla multirrol innecesaria.

## Publicaciones roomie

- Una publicación `busco_compartir` puede referenciar una habitación pública de otra persona.
  La referencia expresa interés en compartir y no acredita propiedad, disponibilidad garantizada
  ni autorización para administrar el anuncio. La UI lo identifica como referencia a un anuncio.
- La API exige visibilidad pública al vincular o reactivar esa referencia. Si luego se pausa,
  alquila, bloquea o expira la habitación, oculta sus datos en la publicación mediante una carga
  batch de IDs visibles. No mantiene una copia pública de información retirada.
- Eliminar es lógico: `estado=eliminada`. Se conservan publicación, imágenes y relaciones;
  la publicación desaparece de los listados habituales y no puede editarse ni reactivarse.
  Los endpoints públicos tampoco muestran las imágenes de publicaciones retiradas o de autores
  suspendidos. El propietario puede consultar sus propias imágenes autenticado.
- Una publicación cerrada puede reabrirse; no se confunde con una publicación eliminada.
- La consulta de detalle pública admite visitantes. Solo el propietario puede modificar estados
  y contenido; la API comprueba su identidad. El listado carga autores y viviendas en un fetch
  y compatibilidades en lote, en vez de una consulta por tarjeta.

## Interés por una habitación y datos de contacto

El interesado puede escribir un correo opcional para que el propietario responda. Ese valor se guarda con la consulta; nunca se obtiene implícitamente del correo de inicio de sesión, tampoco para consultas históricas. El formulario explica quién lo recibe antes de enviar. Las consultas conservan su historial; se mantiene una por usuario/habitación, protegida por `UQ_lead_unico`. No se agrega reenvío de leads en este cambio. Los propietarios gestionan sus propias consultas aunque su plan haya expirado, siempre que su cuenta de propietario esté activa. No se pueden enviar consultas a habitaciones retiradas, bloqueadas o de suscripción expirada.

## Galerías y URLs externas

Cada anuncio admite hasta cinco imágenes. Se inserta en una posición de 1 a cantidad + 1, desplazando las siguientes; eliminar cierra el hueco. La primera es principal y eliminar la principal elige la primera restante. Todas las mutaciones bloquean el anuncio padre antes de leer las imágenes. SQL Server respalda una sola principal y posiciones únicas 1–5 (también impiden más de cinco filas). V9 ordena datos existentes de forma estable y detiene la migración si ya hay más de cinco, para revisión sin borrar imágenes.

Producción acepta URLs HTTPS absolutas de hasta 255 caracteres codificados, sin credenciales, controles ni fragmentos. Desarrollo permite además HTTP local. No se descarga la URL en el servidor: no hay validación de existencia ni SSRF por fetch del backend. La disponibilidad, cambios de contenido y seguimiento por el proveedor externo siguen siendo limitaciones; la interfaz debe presentar alternativa si falla la carga. No se incorpora almacenamiento externo administrado para este alcance.

Las imágenes públicas obedecen la visibilidad del anuncio y la política de URL. El propietario puede consultar las suyas, incluso archivadas, para conservar historial; un anuncio archivado no permite modificar su galería. Las URLs antiguas incompatibles no se borran ni se reescriben suponiendo un host HTTPS equivalente.

## Edición de preferencias y descripción

El GET del perfil devuelve `version`. El PUT completo y el PATCH `/api/perfil/me/descripcion` envían esa versión: si otro guardado ya la cambió, se devuelve 409 y se conserva el borrador para que el usuario recargue y compare. Hibernate también comprueba la versión en el UPDATE para cerrar la carrera entre lectura y escritura. Los clientes anteriores deben actualizarse para enviar este campo; omitirlo produce 400, no un overwrite silencioso.

El PATCH acepta únicamente descripción y versión; permite vaciar la descripción y no modifica presupuesto ni hábitos. Las preferencias categóricas aceptan los valores reales del formulario; no se convierten silenciosamente valores desconocidos de perfiles históricos. Los presupuestos respetan DECIMAL(10,2). Los nuevos timestamps del perfil se escriben en UTC; no se reinterpretan timestamps históricos sin conocer su zona original.
