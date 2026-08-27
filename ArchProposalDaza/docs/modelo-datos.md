# Modelo de datos — diccionario de entidades

Especificación de las 13 entidades JPA propuestas. Este documento es la
referencia para implementarlas cuando exista `proyecto-backend`; hoy no hay
código, solo el diseño. Paquete base propuesto: `com.facimus.procesos`, bajo
`gestion/` y `modelado/` — ver
[ADR-001](adr/ADR-001-arquitectura-modular-dominio.md). Relaciones y
cardinalidades completas en [diagrama-clases.md](diagrams/diagrama-clases.md).

## Convenciones generales

- `@Id` con `@GeneratedValue(strategy = GenerationType.IDENTITY)` en toda entidad.
- Toda entidad excepto `Empresa` hereda de una superclase común
  `EntidadEmpresa` (`@MappedSuperclass`) que aporta `id` + `empresa`
  (`@ManyToOne` obligatorio, columna `empresa_id` directa, `updatable = false`)
  — ver [ADR-002](adr/ADR-002-multitenencia-empresa-id.md). En las tablas de
  abajo, esa columna se omite por brevedad: se asume presente en todas
  salvo `Empresa`.
- Enums mapeados con `@Enumerated(EnumType.STRING)`, nunca `ORDINAL`.
- Ninguna entidad usa `DELETE` físico salvo que se indique lo contrario;
  `Proceso` usa eliminación lógica (`activo`).

## Enums

| Enum | Valores | Usado en |
|---|---|---|
| `RolAcceso` | `ADMINISTRADOR`, `EDITOR`, `LECTURA` | `Usuario.rolAcceso` |
| `EstadoProceso` | `BORRADOR`, `PUBLICADO` | `Proceso.estado` |
| `TipoParticipante` | `EMPRESA`, `CLIENTE`, `PROVEEDOR`, `SISTEMA_EXTERNO` | `Pool.tipoParticipante` |
| `TipoGateway` | `EXCLUSIVA`, `PARALELA`, `INCLUSIVA` | `Gateway.tipoGateway` |

## Bloque 1 — Gestión

### Empresa
Raíz del aislamiento multiempresa. No hereda de `EntidadEmpresa` (es ella
misma el tenant).

| Campo | Tipo | Notas |
|---|---|---|
| `id` | `Long` | PK |
| `nombre` | `String` | obligatorio |
| `nit` | `String` | obligatorio, único |
| `fechaRegistro` | `LocalDateTime` | obligatorio |

Relaciones: `1—N` con `Usuario`, `1—N` con `Proceso`.

### Usuario
| Campo | Tipo | Notas |
|---|---|---|
| `nombre` | `String` | obligatorio |
| `email` | `String` | obligatorio, único **por empresa** (`UNIQUE(empresa_id, email)`) |
| `passwordHash` | `String` | obligatorio; nunca se guarda password en claro |
| `rolAcceso` | `RolAcceso` | obligatorio |

Relaciones: `N—1` con `Empresa`; `1—N` con `HistorialCambio` (como autor).

### Proceso
| Campo | Tipo | Notas |
|---|---|---|
| `nombre` | `String` | obligatorio |
| `descripcion` | `String` (texto largo) | opcional |
| `categoria` | `String` | opcional |
| `estado` | `EstadoProceso` | obligatorio |
| `activo` | `boolean` | default `true`; eliminación lógica, nunca `DELETE` físico |
| `fechaCreacion` | `LocalDateTime` | obligatorio |
| `fechaModificacion` | `LocalDateTime` | opcional, se actualiza en cada cambio |

Relaciones: `N—1` con `Empresa`; `1—N` con `RolProceso`, `Pool`, `HistorialCambio`.

### HistorialCambio
Auditoría de cambios sobre un `Proceso`. Solo `INSERT`, nunca se edita ni se borra.

| Campo | Tipo | Notas |
|---|---|---|
| `proceso` | `Proceso` (`N—1`) | obligatorio |
| `autor` | `Usuario` (`N—1`) | obligatorio |
| `fechaCambio` | `LocalDateTime` | obligatorio |
| `descripcionCambio` | `String` (texto largo) | obligatorio |

### RolProceso
Rol funcional definido por proceso (Analista, Supervisor, Auditor, u otro
nombre libre) — no es un enum global, es dato por proceso.

| Campo | Tipo | Notas |
|---|---|---|
| `proceso` | `Proceso` (`N—1`) | obligatorio |
| `nombre` | `String` | obligatorio |

Relaciones: `1—N` con `Lane`. No se puede eliminar mientras alguna `Lane` lo
referencie — regla de negocio, ver [ADR-003](adr/ADR-003-validacion-coherencia-diagrama.md).

## Bloque 2 — Modelado del diagrama

### Pool
Participante del proceso: la empresa propietaria, un cliente, un proveedor o
un sistema externo.

| Campo | Tipo | Notas |
|---|---|---|
| `proceso` | `Proceso` (`N—1`) | obligatorio |
| `nombre` | `String` | obligatorio |
| `tipoParticipante` | `TipoParticipante` | obligatorio |

Relaciones: `1—N` con `Lane`; `1—N` con `Mensaje` como `poolOrigen`; `1—N`
con `Mensaje` como `poolDestino`.

### Lane
División interna de un `Pool` que agrupa las actividades de un mismo
`RolProceso`. **Este es el cruce real entre `modelado` y `gestion`** — el rol
se asigna a nivel de lane, no de actividad directamente (ver
[ADR-001](adr/ADR-001-arquitectura-modular-dominio.md)).

| Campo | Tipo | Notas |
|---|---|---|
| `pool` | `Pool` (`N—1`) | obligatorio |
| `rolProceso` | `RolProceso` (`N—1`) | obligatorio |
| `nombre` | `String` | obligatorio |

Relaciones: `1—N` con `NodoFlujo`.

### NodoFlujo *(abstracta)*
Base de `Actividad` y `Gateway`. Herencia `@Inheritance(SINGLE_TABLE)` con
columna discriminadora `tipo_nodo` (no `@MappedSuperclass`): así `Arco` y
`Lane` apuntan a "cualquier nodo" con una sola FK, en vez de un par de
columnas nullable por subtipo.

| Campo | Tipo | Notas |
|---|---|---|
| `lane` | `Lane` (`N—1`) | obligatorio |
| `nombre` | `String` | obligatorio |

Relaciones: `1—N` con `Arco` como `origen`; `1—N` con `Arco` como `destino`.

#### Actividad *(extiende NodoFlujo, discriminador `ACTIVIDAD`)*
| Campo | Tipo | Notas |
|---|---|---|
| `descripcion` | `String` (texto largo) | opcional |

#### Gateway *(extiende NodoFlujo, discriminador `GATEWAY`)*
| Campo | Tipo | Notas |
|---|---|---|
| `tipoGateway` | `TipoGateway` | obligatorio |

### Arco
Secuencia de flujo entre dos `NodoFlujo`. Regla "no cruza pools": lógica de
negocio, no de esquema — ver
[ADR-003](adr/ADR-003-validacion-coherencia-diagrama.md).

| Campo | Tipo | Notas |
|---|---|---|
| `origen` | `NodoFlujo` (`N—1`) | obligatorio, FK propia |
| `destino` | `NodoFlujo` (`N—1`) | obligatorio, FK propia |
| `etiqueta` | `String` | opcional (ej. "Sí"/"No" al salir de un gateway exclusivo) |

### Mensaje
Comunicación entre dos `Pool` (throw/catch). A diferencia del `Arco`, sí
cruza pools.

| Campo | Tipo | Notas |
|---|---|---|
| `poolOrigen` | `Pool` (`N—1`) | obligatorio |
| `poolDestino` | `Pool` (`N—1`) | obligatorio, distinto de `poolOrigen` |
| `nombre` | `String` | obligatorio |

Relaciones: `1—1` con `Correlacion`.

### Correlacion
Criterio que identifica a qué caso concreto del proceso corresponde un
`Mensaje` (ej. "n.º de solicitud").

| Campo | Tipo | Notas |
|---|---|---|
| `mensaje` | `Mensaje` (`1—1`) | obligatorio, único |
| `criterio` | `String` | obligatorio |
