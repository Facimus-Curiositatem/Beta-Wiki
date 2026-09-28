# ADR-006: Reglas de borrado y de estado del proceso

## Estado
Aceptado. Cierra los puntos que `diseno-servicios-modelado.md` y
`diagrama-estados-proceso.md` dejaban abiertos: borrado de `Pool`, borrado de
`NodoFlujo` con arcos y la transición `despublicar()`.

## Contexto
Varias entidades tienen dependientes: un nodo tiene arcos, una lane tiene
nodos, un pool tiene lanes y un rol está referenciado por lanes. Para cada
caso había que decidir entre bloquear el borrado, borrar en cascada o hacer
baja lógica. También quedaba abierto si un proceso publicado puede volver a
borrador.

## Decisión

| Entidad | Tipo de borrado | Regla |
|---|---|---|
| `Proceso` | Lógico (`activo = false`) | Se permite en cualquier estado. Queda registrado en el historial. |
| `RolProceso` | Lógico (`activo = false`) | Bloqueado (`409`) si alguna lane lo usa. El mensaje lista los procesos afectados. |
| `Usuario` | Lógico (`activo = false`) | Se desactiva, no se borra, para conservar su autoría en el historial. |
| `Pool` | Físico | Bloqueado (`409`) si alguna de sus lanes tiene nodos. Si están vacías, se borran las lanes y luego el pool. |
| `Lane` | Físico | Bloqueado (`409`) si tiene actividades o gateways. |
| `Actividad` / `Gateway` | Físico | Se borran **en cascada** los arcos que entran o salen del nodo. |
| `Arco` | Físico | Sin dependientes. |
| `Mensaje` | Físico | Se borra en cascada su `Correlacion`. |
| `HistorialCambio` | Nunca | Solo se inserta. |

**Estado del proceso:**

- Un proceso nace en `BORRADOR`.
- `BORRADOR → PUBLICADO` se permite con `PATCH /api/v1/procesos/{id}`.
- **`PUBLICADO → BORRADOR` no se permite** (`409`). No existe `despublicar()`.
- No hay reactivación de un proceso eliminado.

**Permisos:** todos los `DELETE` del API están reservados al rol
`ADMINISTRADOR` (ver [seguridad.md](../seguridad.md)).

## Consecuencias
- La cascada de arcos al borrar un nodo evita arcos colgando, pero puede
  dejar otros nodos desconectados. El enunciado pide advertir sobre esos
  errores de modelado; queda pendiente un endpoint de validación del diagrama.
- **Pendientes detectados en el código:**
  - `PoolService` y `MensajeService` no filtran por `activo` al buscar el
    proceso, así que se pueden crear pools y mensajes en un proceso eliminado.
  - No existe filtro para consultar procesos eliminados (HU-06).
  - Se puede eliminar el pool inicial de la empresa.