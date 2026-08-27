# ADR-003: Dónde vive la validación de coherencia del diagrama

## Estado
Aceptado.

## Contexto
El enunciado fija reglas de coherencia que no son validables campo a campo:

- Una `Actividad`/`Gateway` pertenece a una `Lane` (ya lo garantiza la FK
  `lane_id nullable = false` en `NodoFlujo` — esto es integridad referencial,
  no necesita validador de negocio).
- Un `Arco` **no cruza pools**: `arco.origen` y `arco.destino` deben colgar,
  a través de su `Lane`, del mismo `Pool`.
- Un `Mensaje` **sí cruza pools**: `poolOrigen` y `poolDestino` deben ser
  distintos, y ambos pertenecer al mismo `Proceso`.
- Un `RolProceso` no se puede eliminar mientras alguna `Lane` lo referencie
  ("en uso").

Todas requieren cargar entidades relacionadas y comparar (`origen.lane.pool`
vs `destino.lane.pool`; existencia de `Lane` con ese `rolProceso`, etc.). Ni
`@NotNull`/`@Size` de Bean Validation ni una constraint de base de datos
alcanzan solas: la FK evita basura referencial, pero no sabe qué significa
"mismo pool" o "en uso".

## Opciones consideradas

1. **`@AssertTrue` en la propia entidad** (Bean Validation), leyendo las
   relaciones lazy directamente en el método de validación.
   - Se dispara implícitamente en cualquier `@Valid` (binding de formulario,
     `@RequestBody`), no solo al guardar — puede evaluarse en momentos donde
     las relaciones no están cargadas o la entidad está a medio construir.
   - Acopla la entidad a que exista una sesión de Hibernate activa para
     poder leer `origen.lane.pool`. Una entidad no debería necesitar eso
     para saber si es válida.

2. **Un único `ProcesoValidator` monolítico** en el servicio de `gestion`,
   que valida "todo lo del proceso" en un solo lugar.
   - Junta reglas de naturaleza distinta (unas se disparan al crear un
     `Arco`, otras al eliminar un `RolProceso`, otras al crear un
     `Mensaje`) y de módulos distintos (`gestion` y `modelado`) en una clase
     que crece sin límite y no refleja la separación de ADR-001.

3. **Validadores de servicio, uno por operación/entidad, ubicados en el
   módulo dueño de la regla**, invocados explícitamente antes de persistir o
   eliminar: `modelado.service.ArcoValidator` (antes de crear/editar un
   `Arco`), `modelado.service.MensajeValidator` (antes de crear un
   `Mensaje`), `gestion.service.RolProcesoValidator` (antes de eliminar un
   `RolProceso`). Cada uno recibe las entidades ya cargadas por el service
   que lo invoca (no dispara cargas ocultas) y lanza una excepción de
   dominio si la regla se rompe.

## Decisión
Opción 3. La validación de coherencia vive en clases de validador dedicadas,
dentro del módulo (`gestion`/`modelado`) dueño de la entidad que protegen, no
en la entidad ni en un validador único. Se invocan explícitamente desde el
método de servicio correspondiente antes del `save`/`delete`:

```
ArcoService.crear(...)
    -> carga origen y destino
    -> ArcoValidator.validarMismoPool(origen, destino)
    -> arcoRepository.save(...)

RolProcesoService.eliminar(...)
    -> laneRepository.existsByRolProceso(rol)
    -> si existe: RolProcesoValidator.lanzarSiEnUso(...)
    -> si no: rolProcesoRepository.delete(...)
```

Cada validador lanza una excepción de dominio (`ReglaDiagramaInvalidaException`,
`RuntimeException` no verificada) con un mensaje claro de qué regla se violó.
Un `@ControllerAdvice` compartido la traduce a mensaje de error en la vista
Thymeleaf (entrega 1) y, más adelante, a un `4xx` en el controller REST
(entrega 2) — mismo mecanismo, sin duplicar la traducción del error.

## Consecuencias
- Al escribir los servicios de `modelado` (siguiente ítem del backlog:
  `ArcoService`, `MensajeService`), cada operación de escritura que toque una
  regla de coherencia debe invocar su validador antes de persistir — no se
  asume que la regla ya se cumple porque el formulario la pedía.
- Al escribir el servicio de eliminación de `RolProceso` en `gestion`, debe
  consultar `Lane` antes de borrar, no confiar solo en que la FK lance un
  error de integridad (el error de integridad no da un mensaje de negocio
  legible, y puede no dispararse si la FK no está bien indexada/configurada).
- Los validadores son testeables de forma aislada (entran entidades en
  memoria, salen `void` o excepción) sin levantar contexto de Spring ni
  sesión de Hibernate — no dependen de fetch perezoso.
