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
