# Diseño de servicios — módulo `modelado`

Especificación de responsabilidades de la capa de servicio para `Pool`,
`Lane`, `NodoFlujo`/`Actividad`/`Gateway`, `Arco`, `Mensaje`, `Correlacion`
(ver [modelo-datos.md](modelo-datos.md), [ADR-001](adr/ADR-001-arquitectura-modular-dominio.md)).
Documento de diseño — no código.

## Regla transversal (ADR-002)

Igual que en `gestion`: todo método recibe `empresaId` explícito y usa los
métodos `*AndEmpresaId` de [contratos-repositorios.md](contratos-repositorios.md).

## PoolService

| Método | Responsabilidad |
|---|---|
| `crear(procesoId, empresaId, nombre, tipoParticipante)` | Alta simple, atado al `Proceso`. |
| `listarPorProceso(procesoId, empresaId)` | Lectura para el editor de diagrama. |
| `eliminar(poolId, empresaId)` | **Abierto**: el enunciado no define si un `Pool` se puede eliminar una vez tiene `Lane`s o `Mensaje`s asociados. Queda pendiente de un ADR futuro si hace falta antes de implementarlo — por ahora no asumir eliminación física libre. |

## LaneService

| Método | Responsabilidad |
|---|---|
| `crear(poolId, rolProcesoId, empresaId, nombre)` | Valida coherencia antes de guardar: el `RolProceso` referenciado debe pertenecer al mismo `Proceso` que el `Pool` (`rolProceso.proceso.id == pool.proceso.id`) — si no, es una `Lane` con rol de otro proceso, dato corrupto. |
| `listarPorPool(poolId, empresaId)` | Lectura para el editor. |

## NodoFlujoService (Actividad / Gateway)

| Método | Responsabilidad |
|---|---|
| `crearActividad(laneId, empresaId, nombre, descripcion)` | Alta de un `NodoFlujo` concreto tipo `Actividad`, atado a la `Lane`. |
| `crearGateway(laneId, empresaId, nombre, tipoGateway)` | Alta de un `NodoFlujo` concreto tipo `Gateway`. |
| `listarPorLane(laneId, empresaId)` | Devuelve ambos subtipos mezclados (`NodoFlujoRepository.findAllByLaneIdAndEmpresaId`), la vista distingue por `tipoGateway == null` o por el discriminador. |
| `eliminar(nodoId, empresaId)` | **Abierto**: borrar un nodo referenciado por `Arco`s (como origen o destino) deja arcos huérfanos. Antes de implementar: decidir si se restringe (no se puede borrar mientras tenga arcos) o se cascadea (se borran también sus arcos). No asumir sin ADR. |

## ArcoService

| Método | Responsabilidad |
|---|---|
| `crear(origenId, destinoId, empresaId, etiqueta)` | Carga `origen` y `destino` vía `NodoFlujoRepository.findByIdAndEmpresaId`. Invoca `ArcoValidator.validarMismoPool(origen, destino)` — [ADR-003](adr/ADR-003-validacion-coherencia-diagrama.md): compara `origen.lane.pool.id` contra `destino.lane.pool.id`; si difieren, lanza `ReglaDiagramaInvalidaException` **antes** de guardar. |
| `listarPorNodo(nodoId, empresaId)` | Combina `findAllByOrigenIdAndEmpresaId` + `findAllByDestinoIdAndEmpresaId` para pintar entradas/salidas de un nodo en el editor. |

## MensajeService

| Método | Responsabilidad |
|---|---|
| `crear(poolOrigenId, poolDestinoId, empresaId, nombre, criterioCorrelacion)` | Carga ambos `Pool`. Invoca `MensajeValidator.validarPoolsDistintosYMismoProceso(poolOrigen, poolDestino)` — [ADR-003](adr/ADR-003-validacion-coherencia-diagrama.md): deben ser pools distintos y pertenecer al mismo `Proceso` (a diferencia del `Arco`, el `Mensaje` sí cruza pools, pero no cruza procesos). Crea el `Mensaje` **y** su `Correlacion` en la misma operación (relación `1—1` con `cascade = ALL`, ver modelo-datos.md) — no hay alta de `Correlacion` independiente. |
| `listarPorPool(poolId, empresaId)` | Combina enviados/recibidos, para la vista de un `Pool`. |

No hay `CorrelacionService` independiente: `Correlacion` no tiene ciclo de
vida propio, se gestiona siempre como parte de la creación/edición de su
`Mensaje` (cascade), consistente con que el modelo no define un caso de uso
donde se cree una correlación sin mensaje.
