# URL de foto de perfil

La actualización de `/api/usuarios/me` reutiliza `ImageUrlPolicy`, igual que
las galerías. Una foto nueva o reenviada al guardar debe ser una URL HTTPS
absoluta de hasta 255 caracteres codificados, sin credenciales, fragmentos,
controles ni puertos inválidos. Desarrollo conserva su configuración explícita
que admite HTTP. Un valor vacío quita la foto opcional.

La validación ocurre antes de modificar nombres, edad u otros campos de la
entidad. Una URL inválida devuelve 400 y no guarda parcialmente los datos.
Once casos de prueba cubren esquemas rechazados, URL codificada, retirada de
foto, desarrollo, usuario inexistente y conservación de credenciales.

No se descarga la imagen desde el backend. Esto no valida que exista, que
su contenido sea apropiado ni evita el seguimiento por el servidor externo.
Los registros históricos no se borran ni se migran silenciosamente: si su URL
no cumple al guardar el perfil, el usuario debe corregirla o vaciarla. Sigue
pendiente revisar la presentación/fallback de esas fotos en todas las vistas.

Validación local del 26 de septiembre: frontend 69 pruebas y build aprobados;
Maven no alcanzó la compilación por fallo DNS de repo.maven.apache.org. La
validación del backend se realiza en el CI de este commit, con SQL Server.
