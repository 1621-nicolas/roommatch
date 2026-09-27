# Auditoría de dependencias

## Frontend

CI ejecuta `npm ci` desde el lockfile y `npm audit --audit-level=high`.
El 26/09/2026 el audit local de todas las dependencias, incluidas las de
desarrollo, devolvió cero vulnerabilidades conocidas. Esto es una instantánea,
no una garantía de que no aparezcan avisos nuevos.

## Backend

Tras `clean verify -Psqlserver`, CI genera `target/bom.json` con el plugin
CycloneDX Maven 2.9.3. Incluye dependencias directas, transitivas y de pruebas
resueltas por Maven. No se deducen versiones leyendo solo el POM ni se confunde
una compilación exitosa con una auditoría de vulnerabilidades.

OSV-Scanner 2.6.0 consulta ese inventario y escribe `target/osv.json`. Se fija
versión y SHA-256 del binario Linux AMD64 publicado por Google. CI conserva
ambos archivos 14 días en `backend-dependency-audit`; los hallazgos también
aparecen con el prefijo `MAVEN_FINDING` en el log.

El paso de análisis conserva su código de salida: un hallazgo o un fallo del
escáner no se transforma en éxito. La ausencia de informe produce un error
explícito. No hay exclusiones de vulnerabilidades ni actualizaciones automáticas.
Cada hallazgo requiere confirmar versión, alcance runtime/test, precondiciones,
exposición del código y parche compatible antes de cambiar una dependencia.

Esto analiza las bibliotecas declaradas/resueltas. No cubre por sí solo el SO
de la imagen de despliegue, dependencias embebidas sin metadata, secretos,
fallos propios de RoomMatch ni prueba que una ruta vulnerable sea alcanzable.
Los plugins Maven son herramientas de construcción, no componentes del SBOM
de la aplicación; necesitan revisión separada cuando haya un aviso relevante.

El primer resultado CI de este nuevo control está pendiente de ejecución.
Maven Central sigue sin resolverse por DNS en el entorno local de trabajo;
esa limitación no se usa para declarar limpia la auditoría.

## Repetir el análisis

```bash
cd roommatch-api/roommatch-api
./mvnw --batch-mode org.cyclonedx:cyclonedx-maven-plugin:2.9.3:makeAggregateBom \
  -DincludeTestScope=true -DoutputFormat=json -Dcyclonedx.skipAttach=true
osv-scanner scan source --lockfile=target/bom.json --format=json --output=target/osv.json
```

Fuentes comprobadas el 27/09/2026:

- [CycloneDX: dependencias y opciones de generación](https://cyclonedx.github.io/cyclonedx-maven-plugin/makeAggregateBom-mojo.html).
- [Versión estable 2.9.3 del plugin](https://github.com/CycloneDX/cyclonedx-maven-plugin/releases/tag/cyclonedx-maven-plugin-2.9.3).
- [OSV: análisis de SBOM mediante Package URLs](https://google.github.io/osv-scanner/usage/scan-source).
- [OSV: limitaciones de la resolución directa de POM](https://google.github.io/osv-scanner/supported-languages-and-lockfiles/).
- [OSV-Scanner 2.6.0 y binarios publicados](https://github.com/google/osv-scanner/releases/tag/v2.6.0).
