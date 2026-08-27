# Diseño de servicios — módulo `gestion`

Especificación de responsabilidades de la capa de servicio para
`Empresa`, `Usuario`, `Proceso`, `HistorialCambio`, `RolProceso` (ver
[modelo-datos.md](modelo-datos.md), [ADR-001](adr/ADR-001-arquitectura-modular-dominio.md)).
Documento de diseño — no código.

> **Nota de corrección**: el backlog original agrupaba "CRUD de proceso con
> historial de cambios" bajo servicios de `modelado`. `Proceso` y
> `HistorialCambio` viven en `gestion` según ADR-001 (son las entidades que
> el paquete `gestion` lista explícitamente), así que su servicio va aquí,
> no en `modelado`.

## Regla transversal (ADR-002)

Todo método de servicio de este módulo recibe `empresaId` explícito (nunca
se infiere de la entidad ya cargada sin pasar el parámetro), y usa los
métodos `*AndEmpresaId` de [contratos-repositorios.md](contratos-repositorios.md).
Al crear una entidad hija, el servicio asigna `empresa` desde el padre o el
contexto de sesión — nunca acepta un `empresaId` suelto del cliente como
dato de formulario.

## EmpresaService

| Método | Responsabilidad |
|---|---|
| `registrarEmpresa(nombre, nit, datosAdmin)` | Valida `nit` único (`EmpresaRepository.existsByNit`), crea `Empresa`, y en la misma transacción crea el `Usuario` administrador inicial (`rolAcceso = ADMINISTRADOR`) delegando a `UsuarioService`. Si falla la creación del admin, no debe quedar una `Empresa` huérfana — todo o nada. |
| `buscarPorNit(nit)` | Lectura simple, usada en validaciones de unicidad y en login. |

## UsuarioService

| Método | Responsabilidad |
|---|---|
| `crearColaborador(empresaId, nombre, email, passwordPlano, rolAcceso)` | Valida email único **por empresa** (`existsByEmpresaIdAndEmail`), hashea el password, persiste. Quién puede invocarlo (solo `ADMINISTRADOR`) es una regla de autorización — en la entrega 1 se valida a mano en el controller (sesión), en la entrega final la impone Spring Security. |
| `cambiarRolAcceso(usuarioId, empresaId, nuevoRol)` | Carga con `findByIdAndEmpresaId`, actualiza `rolAcceso`. |
| `listarPorEmpresa(empresaId)` | Lectura para la vista de administración de usuarios. |
| `autenticar(email, passwordPlano)` | Busca usuario por email (sin filtro de empresa — email es el identificador de login, la empresa se resuelve *a partir* del usuario encontrado, no al revés), compara hash. Usado por el login de la entrega 1 (ver [diseno-controladores-thymeleaf.md](diseno-controladores-thymeleaf.md)). |

## ProcesoService

| Método | Responsabilidad |
|---|---|
| `crear(empresaId, nombre, descripcion, categoria, autorId)` | `estado = BORRADOR`, `activo = true`, `fechaCreacion = now()`. Registra un `HistorialCambio` ("Proceso creado") vía `HistorialCambioService`. |
| `editar(procesoId, empresaId, cambios, autorId)` | Carga con `findByIdAndEmpresaId`, aplica cambios, `fechaModificacion = now()`. Registra `HistorialCambio` describiendo qué campos cambiaron. |
| `publicar(procesoId, empresaId, autorId)` | `estado = BORRADOR → PUBLICADO` (ver [diagrama-estados-proceso.md](diagrams/diagrama-estados-proceso.md)). Registra `HistorialCambio`. |
| `eliminarLogico(procesoId, empresaId, autorId)` | `activo = false`. **Nunca** `DELETE` físico. Registra `HistorialCambio`. |
| `buscar(empresaId, filtroNombre, filtroEstado)` | Solo procesos con `activo = true` salvo que se pida explícitamente lo contrario — usa `findAllByEmpresaIdAndActivoTrue` / variantes de `contratos-repositorios.md`. |

## HistorialCambioService

| Método | Responsabilidad |
|---|---|
| `registrar(proceso, autor, descripcion)` | Solo `INSERT`. Invocado internamente por `ProcesoService` en cada operación de escritura — no se expone para editar ni borrar entradas de historial (es auditoría). |
| `listarPorProceso(procesoId, empresaId)` | Orden descendente por fecha (`findAllByProcesoIdAndEmpresaIdOrderByFechaCambioDesc`), para la vista de detalle de proceso. |

## RolProcesoService

| Método | Responsabilidad |
|---|---|
| `crear(procesoId, empresaId, nombre)` | Alta simple, sin restricción de nombres predefinidos (Analista/Supervisor/Auditor son ejemplos, no un enum cerrado). |
| `listarPorProceso(procesoId, empresaId)` | Lectura, para poblar el selector de rol al crear una `Lane`. |
| `eliminar(rolProcesoId, empresaId)` | **Antes de borrar**, invoca al validador de [ADR-003](adr/ADR-003-validacion-coherencia-diagrama.md): consulta `LaneRepository.existsByRolProcesoIdAndEmpresaId`; si existe alguna `Lane` que lo referencia, lanza `ReglaDiagramaInvalidaException` y no borra. |
