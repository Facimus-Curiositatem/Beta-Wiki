# ADR-007: Las validaciones de coherencia viven dentro de cada servicio

## Estado
Aceptado. Reemplaza la parte de implementación de ADR-003 (clases
validadoras dedicadas y `ReglaDiagramaInvalidaException`). El principio de
ADR-003 se mantiene: la coherencia se valida en la capa de servicio, no en la
entidad ni con Bean Validation.

## Contexto
ADR-003 proponía una clase validadora por operación (`ArcoValidator`,
`MensajeValidator`, `RolProcesoValidator`) y una excepción propia
`ReglaDiagramaInvalidaException`. Al implementar, cada regla resultó ser de
pocas líneas y usada en un solo lugar, y el backend ya tenía una excepción
genérica para cualquier regla de negocio.

## Opciones consideradas

1. **Implementar lo de ADR-003 tal cual.** Tres clases más con un único
   llamador cada una, y dos excepciones con el mismo significado HTTP.
2. **Validar dentro del método de servicio y lanzar `ReglaNegocioException`.**
   La regla queda junto a la operación que protege.

## Decisión
Opción 2:

- Cada método de servicio valida antes de persistir o eliminar. Ejemplos:
  - `ArcoService.crear()`: origen distinto de destino, mismo pool, sin arco
    duplicado y condición obligatoria en gateways exclusivos e inclusivos.
  - `MensajeService.crear()`: pool de origen distinto del de destino.
  - `RolProcesoService.eliminar()`: el rol no está en uso.
  - `PoolService.eliminar()` y `LaneService.eliminar()`: no tienen nodos.
- Una regla violada lanza `common/ReglaNegocioException`, que
  `ApiExceptionHandler` traduce a `409 Conflict`.
- Un recurso inexistente **o de otra empresa** lanza
  `RecursoNoEncontradoException`, que se traduce a `404`. Así no se revela si
  el recurso existe en otra empresa.
- Los errores de formato de campos (`@NotBlank`, `@Size`, etc.) siguen en los
  DTO de entrada y responden `400`.

## Consecuencias
- Si una regla crece o se reutiliza en varios servicios, se extrae a una clase
  propia; hasta entonces se mantiene en línea.
- Las reglas se prueban con tests del servicio, no con tests de validadores
  aislados.
- **Pendiente:** la condición de los arcos debe exigirse en los arcos que
  **salen** de un gateway exclusivo o inclusivo. Hoy `ArcoService` la exige
  cuando el **destino** es el gateway. Hay que cambiar `destino` por `origen`
  en esa validación. El ejemplo del enunciado lo confirma: "Sí/No" va en las
  flechas que salen del gateway.