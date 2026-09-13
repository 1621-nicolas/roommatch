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
