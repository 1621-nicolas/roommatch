# Administración

Las rutas `/admin/dashboard` y `/admin/reportes` consumen la API real. No incluyen registros de ejemplo. El resumen diferencia conteos vigentes del conteo histórico de matches; no afirma que las habitaciones con estado activo estén necesariamente visibles si el plan venció.

La lista separa usuarios y habitaciones, filtra por estado y pagina de diez en diez. El detalle muestra el estado del objetivo y un historial paginado. Los cambios requieren confirmación y motivo; un 409 conserva el motivo escrito y solicita revisar el estado. El navegador no concede permisos: filtros HTTP y métodos de servicio requieren ADMIN, y se revalida que su cuenta esté activa.

Las URLs de revisión y sanción conservan su estructura; ahora reciben `{"motivo":"..."}`. La restauración afecta al estado global de la cuenta/habitación, no borra el historial, y no publica automáticamente una habitación. Consulte [las reglas de moderación](BUSINESS_RULES.md#moderación-administrativa).

Se reutilizan componentes funcionales de paginación y estado vacío. Las rutas de funcionalidades se cargan bajo demanda. Las pruebas de interfaz usan datos identificados como prueba únicamente dentro de archivos `.spec.ts`; los componentes de producción no importan fixtures.

Verificación visual pendiente: el entorno local dejó de estar disponible durante esta fase. CI valida TypeScript, plantillas, build y pruebas de comportamiento, pero esas comprobaciones no equivalen a revisar la apariencia en un navegador.
