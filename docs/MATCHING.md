# Índice de compatibilidad por preferencias · preferences-v2

El resultado mide semejanza de preferencias declaradas. No es una probabilidad de
éxito, una evaluación de personalidad ni una garantía. Los pesos son una política
explícita y revisable del producto; no se presentan como pesos aprendidos de datos.
No se puntúan edad, nombres, universidad, ocupación ni fotografía.

| Criterio | Peso | Justificación | Similitud entre 0 y 1 |
|---|---:|---|---|
| Distrito | 14 | Ubicación afecta viabilidad cotidiana | Igual tras normalizar acentos, espacios y mayúsculas: 1; distinto: 0 |
| Presupuesto mensual personal | 18 | Restricción económica principal | Jaccard de intervalos monetarios inclusivos, explicado debajo |
| Fecha de mudanza | 8 | Coordinar disponibilidad temporal | `max(0, 1 − abs(días de diferencia)/60)` |
| Limpieza | 12 | Frecuencia de fricción doméstica | `1 − abs(a−b)/4`, escala 1 a 5 |
| Ruido | 10 | Descanso y trabajo en casa | Misma fórmula ordinal |
| Sociabilidad | 6 | Tiempo compartido deseado | Misma fórmula ordinal |
| Horario | 7 | Rutinas y descanso | Igual: 1; variable con fijo: 0,5; dos fijos distintos: 0 |
| Visitas | 6 | Uso del espacio común | Rangos bajas/moderadas/frecuentes: `1 − abs(rangoA−rangoB)/2` |
| Mascotas | 5 | Condiciones de convivencia | si/no iguales: 1; distintos: 0 |
| Fumar | 6 | Preferencia importante de salud y espacio | si/no iguales: 1; distintos: 0 |
| Alcohol | 2 | Preferencia adicional, sin sobredimensionarla | si/no iguales: 1; distintos: 0 |
| Reparto de gastos | 4 | Evitar expectativas económicas contradictorias | divididos/proporcional iguales: 1; distintos: 0 |
| Convivencia | 2 | Resumen que se solapa con otros criterios | Matriz siguiente |
| **Total** | **100** | | |

El formulario actual describe preferencias si/no, sin distinguir conducta propia y
tolerancia hacia otra persona. No se infiere esa tolerancia ni se inventan reglas
asimétricas. Una futura pregunta explícita permitiría mejorar mascotas, humo y alcohol.

| Convivencia | Tranquila | Social | Independiente | Mixta |
|---|---:|---:|---:|---:|
| Tranquila | 1 | 0 | 0,5 | 0,5 |
| Social | 0 | 1 | 0 | 0,5 |
| Independiente | 0,5 | 0 | 1 | 0,5 |
| Mixta | 0,5 | 0,5 | 0,5 | 1 |

## Presupuesto

Se convierten soles a céntimos enteros exactos. Para `[a,b]` y `[c,d]`:

```
I = max(0, min(b,d) − max(a,c) + 1)
U = (b−a+1) + (d−c+1) − I
similitud = I/U
```

El `+1` representa el céntimo mínimo; permite presupuestos fijos sin dividir por cero.
Intervalos idénticos puntúan 1; disjuntos 0. Compartir solamente un extremo no da 1.
Un presupuesto fijo dentro de un rango amplio resulta viable, pero poco semejante.
Por eso `hayImporteComun` se informa por separado: viabilidad no equivale a semejanza.
Los importes negativos, invertidos, fuera de DECIMAL(10,2) o con fracciones de céntimo
se consideran inválidos. No se redondean silenciosamente.

## Datos incompletos y límites

Un criterio nulo, vacío, fuera de escala o con categoría desconocida se excluye del
denominador. Dos textos desconocidos iguales no obtienen puntos. La cobertura es la
suma de pesos evaluables. Con menos de 70 puntos de cobertura no se devuelve porcentaje.
Con cobertura suficiente, `índice = 100 × Σ(peso × similitud) / Σ(pesos conocidos)`.
La respuesta conserva la cobertura y el detalle de cada criterio para explicar el índice.
El resultado se redondea a dos decimales; las contribuciones usan doce decimales internos.

La fecha opcional reduce la cobertura a 92 si falta. La ventana de 60 días es una
decisión de coordinación, no un umbral estadísticamente validado. El cálculo compara
fechas declaradas y no cambia por el simple paso del reloj; una fecha antigua debe
invitar a actualizar el perfil, sin borrar a la persona de la búsqueda.

## Validación

Las pruebas cubren los 25 pares ordinales, los 4.356 pares de intervalos inclusivos
entre 0 y 10 céntimos contra un oráculo independiente de conjuntos, fechas y año
bisiesto, categorías parciales, simetría, límites monetarios, datos ausentes y el
efecto exacto de alcohol, gastos y fecha de mudanza sobre el resultado.
