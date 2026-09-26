# Acceso y registro sin contenido simulado

Login y Registro comparten estilos locales de formulario, manteniendo sus
rutas y campos. Se retiraron personas/porcentajes ficticios, anuncios con ID
fijo, una falsa búsqueda de rutas y afirmaciones de verificación de anuncios
que el sistema no demuestra. También se retiraron enlaces a recuperación,
términos y privacidad que apuntaban a la misma pantalla.

Esto **no implementa recuperación de contraseña ni documentos legales**.
Ambas capacidades requieren trabajo antes de una salida pública completa;
no se presentan enlaces inertes ni una aceptación de términos inexistentes.

Los inputs conservan etiquetas y autocomplete. La ayuda está asociada mediante
`aria-describedby`; el error se anuncia y el formulario indica carga.
Los envíos simultáneos se bloquean y una petición pendiente se cancela al
destruir el componente. Cancelar la suscripción evita efectos tardíos en el
cliente; no deshace una operación que el servidor ya haya procesado.

Se mantiene la contraseña exacta y se normalizan los correos antes del envío.
Registro valida edad 18–120 y mantiene la política de contraseña vigente.
El registro exitoso conduce a Login con mensaje de confirmación. Los destinos
de login según rol siguen siendo perfil, propietario y administración.

Siete casos comprueban errores asíncronos, bloqueo de duplicados, tres roles,
edad, datos enviados y cancelación. El test de login envía el evento real del
formulario para verificar la actualización de Angular. Se conservaron los
gates de pruebas; no se deshabilitó la detección de cambios.

Se retiraron además 128 reglas CSS globales antiguas sin consumidores en
HTML/TypeScript. La comprobación visual en navegador y con lector de pantalla
permanece pendiente.
