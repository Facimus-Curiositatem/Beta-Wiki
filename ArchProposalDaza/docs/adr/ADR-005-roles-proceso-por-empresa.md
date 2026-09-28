# ADR-005: Los roles de proceso pertenecen a la empresa, no a cada proceso

## Estado
Aceptado. Modifica lo descrito para `RolProceso` en la versión anterior de
`modelo-datos.md`, donde el rol colgaba de un `Proceso`.

## Contexto
Un rol de proceso (Analista, Supervisor, Auditor) es la función responsable
de un grupo de actividades y se asigna a nivel de `Lane`. El diseño inicial
lo definía por proceso: cada proceso tenía su propia lista de roles.

Las historias HU-17 a HU-20 tratan los roles como un catálogo: se crean,
editan, eliminan y consultan como recurso propio, con nombre único y con la
lista de procesos donde se usan. Esto supone que un mismo rol se reutiliza en
varios procesos de la empresa.

## Opciones consideradas

1. **Rol por proceso** (diseño inicial). Cada proceso repite "Analista",
   "Supervisor", etc. No se puede responder "¿en qué procesos se usa el rol
   Analista?" porque son filas distintas, y renombrarlo exige editarlo en
   cada proceso.

2. **Rol por empresa.** Un catálogo único por empresa, referenciado desde las
   lanes de cualquier proceso por clave foránea.

## Decisión
Opción 2. `RolProceso` extiende `EntidadEmpresa` y ya no tiene relación con
`Proceso`:

- Nombre único por empresa (sin distinguir mayúsculas), entre roles activos.
- Campo `activo` para la baja lógica (HU-19).
- Recurso REST propio: `/api/v1/roles`, reservado al administrador para
  escritura.
- `Lane` referencia al rol por FK (`rol_proceso_id`). Por eso renombrar un rol
  se refleja en todas las lanes sin copiar datos (HU-18).
- La consulta devuelve cuántas lanes usan cada rol y el indicador `enUso` (HU-20).

## Consecuencias
- Un rol no se puede eliminar mientras alguna lane lo referencie. El servicio
  responde `409` con la lista de procesos afectados (ver ADR-006).
- El campo `procesosQueLoUsan` de la respuesta cuenta **lanes**, no procesos.
  Si se quiere contar procesos distintos, hay que agruparlos por proceso en
  `RolProcesoService.contarUsos()`.
- **Pendiente:** `LaneService` busca el rol sin filtrar por `activo`, así que
  hoy se puede asignar un rol eliminado a una lane nueva. Debe usar
  `findByIdAndEmpresaIdAndActivoTrue`.