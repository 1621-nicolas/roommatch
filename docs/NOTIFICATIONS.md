# Notificaciones: privacidad, filtros y paginación

## Contrato del API

Todas las rutas requieren autenticación; el ID del destinatario procede del
principal autenticado, no de un parámetro enviado por Angular.

| Ruta | Comportamiento |
| --- | --- |
| `GET /api/notificaciones?page=0&size=10` | Notificaciones de la cuenta, con total y páginas |
| `GET /api/notificaciones?leido=false&page=0&size=10` | Filtra en SQL antes de paginar y contar |
| `GET /api/notificaciones/no-leidas/count` | Total sin leer de toda la cuenta |
| `PUT /api/notificaciones/{id}/leer` | Lectura idempotente; 404 si el ID no pertenece a la cuenta |
| `PUT /api/notificaciones/leer-todas` | Actualiza exclusivamente las no leídas de la cuenta; devuelve cuántas cambió |

Las páginas admiten `page` 0–10000 y `size` 1–100; fuera de rango se devuelve
400. El orden es `fechaCreacion DESC, idNotificacion DESC`, estable incluso
cuando varias notificaciones tienen exactamente la misma fecha.

La relación con `Usuario` es lazy: para el DTO basta el identificador, sin
cargar los datos personales ni el rol del destinatario. Las lecturas del
servicio usan transacciones `readOnly`. No se añaden índices nuevos sin una
medición que lo justifique; se conserva `IX_notificacion_usuario_leido`.

## Pruebas de servidor

Se añaden pruebas HTTP para 401 y límites de página, y una integración SQL
Server con 23 notificaciones de una cuenta y otra de un usuario distinto.
Comprueba una primera página completamente leída con 13 pendientes posteriores,
las dos páginas del filtro, el orden ante fechas idénticas, lectura idempotente,
rechazo de ID ajeno y aislamiento del marcado masivo. El presupuesto del
listado es dos consultas, datos y total.

La ejecución Maven local del 27/09/2026 se bloqueó antes de compilar porque no
resuelve el DNS de Maven Central. GitHub Actions ejecutó `clean verify -Psqlserver`
en [36339247668](https://github.com/1621-nicolas/roommatch/actions/runs/36339247668):
**186 pruebas unitarias y 20 de integración aprobadas**, sin omisiones. El log
SQL Server registra `NOTIFICATION_QUERIES size=10 queries=2`.

## Interfaz

Angular envía `leido=false` al servidor al elegir «Sin leer». La cifra pendiente
procede de `/no-leidas/count`, independientemente de los diez elementos visibles.
La lista, el contador y las acciones conservan errores y reintentos separados.
No se interpreta un contador fallido como cero ni una respuesta mal formada
como una lista vacía. Cambiar de filtro cancela la petición anterior.

Los botones de lectura impiden envíos repetidos mientras la operación está en
curso. Tras confirmar una lectura se consultan nuevamente listado y contador;
si la última página queda vacía, se vuelve a la última página disponible. El
marcado masivo conserva las notificaciones nuevas que puedan llegar después.
La navegación solo admite rutas internas; la lectura no activa enlaces externos.

La verificación local del 27/09/2026 aprobó **84 pruebas unitarias en 25 archivos**
y **44 pruebas Chromium** con viewports de escritorio y móvil. El build finalizó
con el warning heredado de presupuesto CSS de `mis-publicaciones.css`
(14.06 kB frente a 12 kB). Las pruebas de navegador interceptan el API y no
sustituyen una prueba del despliegue conectado a SQL Server.
