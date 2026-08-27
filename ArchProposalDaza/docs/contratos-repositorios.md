# Contratos de repositorios

Especificación de los repositorios Spring Data propuestos para las 13
entidades (ver [modelo-datos.md](modelo-datos.md)). Son contratos de diseño
— firmas de método a implementar cuando exista `proyecto-backend`, no
código funcional hoy. Aplican la regla de [ADR-002](adr/ADR-002-multitenencia-empresa-id.md):
todo repositorio, salvo el de `Empresa`, expone sus consultas con
`empresaId` como parámetro obligatorio.

## Contrato base — `RepositorioTenant<T>`

Todo repositorio de una entidad que cuelga de `Empresa` extiende esta base
en vez de `JpaRepository` directamente, para no poder exponer por accidente
un `findById` sin filtro de tenant:

```
interface RepositorioTenant<T extends EntidadEmpresa> extends JpaRepository<T, Long> {
    Optional<T> findByIdAndEmpresaId(Long id, Long empresaId)
    List<T>     findAllByEmpresaId(Long empresaId)
    boolean     existsByIdAndEmpresaId(Long id, Long empresaId)
}
```

## Bloque 1 — Gestión

| Repositorio | Extiende | Métodos propios |
|---|---|---|
| `EmpresaRepository` | `JpaRepository<Empresa, Long>` (único sin tenant, es la raíz) | `findByNit(String nit)`, `existsByNit(String nit)` |
| `UsuarioRepository` | `RepositorioTenant<Usuario>` | `findByEmpresaIdAndEmail(Long empresaId, String email)`, `existsByEmpresaIdAndEmail(...)` |
| `ProcesoRepository` | `RepositorioTenant<Proceso>` | `findAllByEmpresaIdAndActivoTrue(Long empresaId)`, `findAllByEmpresaIdAndEstadoAndActivoTrue(Long empresaId, EstadoProceso estado)`, `findAllByEmpresaIdAndNombreContainingIgnoreCaseAndActivoTrue(Long empresaId, String nombre)` |
| `HistorialCambioRepository` | `RepositorioTenant<HistorialCambio>` | `findAllByProcesoIdAndEmpresaIdOrderByFechaCambioDesc(Long procesoId, Long empresaId)` |
| `RolProcesoRepository` | `RepositorioTenant<RolProceso>` | `findAllByProcesoIdAndEmpresaId(Long procesoId, Long empresaId)` |

## Bloque 2 — Modelado

| Repositorio | Extiende | Métodos propios |
|---|---|---|
| `PoolRepository` | `RepositorioTenant<Pool>` | `findAllByProcesoIdAndEmpresaId(Long procesoId, Long empresaId)` |
| `LaneRepository` | `RepositorioTenant<Lane>` | `findAllByPoolIdAndEmpresaId(Long poolId, Long empresaId)`, `existsByRolProcesoIdAndEmpresaId(Long rolProcesoId, Long empresaId)` — esta última alimenta el `RolProcesoValidator` de [ADR-003](adr/ADR-003-validacion-coherencia-diagrama.md) para bloquear el borrado de un rol "en uso" |
| `NodoFlujoRepository` | `RepositorioTenant<NodoFlujo>` | `findAllByLaneIdAndEmpresaId(Long laneId, Long empresaId)` — polimórfico, devuelve `Actividad` y `Gateway` mezclados (misma tabla, `SINGLE_TABLE`) |
| `ArcoRepository` | `RepositorioTenant<Arco>` | `findAllByOrigenIdAndEmpresaId(...)`, `findAllByDestinoIdAndEmpresaId(...)` |
| `MensajeRepository` | `RepositorioTenant<Mensaje>` | `findAllByPoolOrigenIdAndEmpresaId(...)`, `findAllByPoolDestinoIdAndEmpresaId(...)` |
| `CorrelacionRepository` | `RepositorioTenant<Correlacion>` | `findByMensajeIdAndEmpresaId(Long mensajeId, Long empresaId)` |

13 entidades → 13 repositorios (1 a 1), consistente con
[ADR-001](adr/ADR-001-arquitectura-modular-dominio.md): cada uno vive en
`gestion/repository/` o `modelado/repository/` según el módulo dueño de su
entidad.

## Nota sobre la resolución de `empresaId` anidado

Spring Data resuelve `findByEmpresaId` sobre el campo `empresa` (tipo
`Empresa`, con propiedad `id`) como navegación anidada automática — no hace
falta una columna `empresaId` explícita en la entidad para que el nombre del
método funcione, siempre que no exista una propiedad plana con ese nombre
que genere ambigüedad. Mismo mecanismo aplica a `ProcesoId`, `PoolId`,
`LaneId`, `RolProcesoId`, `OrigenId`, `DestinoId`, etc. sobre sus campos de
relación correspondientes.
