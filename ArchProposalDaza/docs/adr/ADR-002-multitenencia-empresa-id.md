# ADR-002: Multi-tenencia por filtro `empresa_id`

## Estado
Aceptado.

## Contexto
El sistema es multiempresa: cada `Proceso` y todo lo que cuelga de él (pools,
lanes, nodos, arcos, mensajes, correlaciones, roles de proceso, historial)
pertenece a una única `Empresa`, y un usuario de una empresa nunca debe poder
leer ni escribir datos de otra. La cadena de entidades tiene varios niveles
(`Empresa -> Proceso -> Pool -> Lane -> NodoFlujo -> Arco`), así que "saber si
una fila pertenece a la empresa del usuario" puede requerir 2-4 saltos de join
según la entidad.

## Opciones consideradas

1. **Derivar `empresa_id` por join**, sin columna propia en las tablas de
   `modelado`. Ej.: para filtrar `Pool` por empresa, unir con `Proceso`.
   - Ventaja: sin redundancia, un solo lugar de verdad para la relación.
   - Riesgo: cada repositorio nuevo necesita reconstruir el join completo
     correctamente. Si alguien escribe una query nueva sin ese join, no falla
     en compilación ni en tests obvios — filtra de más silenciosamente. Con
     `NodoFlujo` a 3 saltos de `Empresa`, es fácil equivocarse.

2. **Columna `empresa_id` directa en cada tabla dependiente de una empresa**,
   vía una superclase común (`EntidadEmpresa`, `@MappedSuperclass`) que aporta
   `id` + `empresa` (`@ManyToOne` obligatorio) a toda entidad de `gestion` y
   `modelado` excepto `Empresa` misma.
   - Ventaja: cualquier repositorio filtra con `WHERE empresa_id = :empresaId`
     sin joins, sin importar cuántos niveles de profundidad tenga la entidad.
     El campo es obligatorio a nivel de columna (`nullable = false`), así que
     una fila sin empresa ni siquiera se puede persistir.
   - Costo: redundancia — `empresa_id` en `Pool` es derivable de
     `Pool.proceso.empresa`. Hay que mantenerla consistente al crear filas
     (se asigna una vez, `updatable = false`, nunca cambia).

## Decisión
Opción 2. Toda entidad menos `Empresa` extiende `EntidadEmpresa`
(`common/EntidadEmpresa.java`), que centraliza `id` y `empresa`. La regla de
negocio "sin excepciones, sin importar qué tan trivial parezca la query
nueva" (ya fijada en `CLAUDE.md`) es más fácil de cumplir con una columna
directa que confiando en que cada desarrollador arme el join correcto: el
filtro por tenant se vuelve mecánico e igual en todos los repositorios, en
vez de un join distinto por profundidad de entidad.

La redundancia se acepta porque `empresa_id` es inmutable una vez creada la
fila (no hay caso de negocio donde un `Pool` cambie de empresa sin cambiar de
proceso), así que no hay riesgo real de que la copia y el origen diverjan.

## Consecuencias
- Todo repositorio Spring Data para entidades bajo `gestion`/`modelado`
  (salvo `EmpresaRepository`) debe exponer sus métodos de consulta con
  `empresaId` como parámetro obligatorio (ej. `findByIdAndEmpresaId`,
  `findAllByEmpresaId`), nunca `findById` a secas.
- Al crear una fila hija (ej. un `Pool` dentro de un `Proceso`), el servicio
  debe propagar `empresa` desde el padre, no aceptarla como input suelto del
  cliente — evita que un usuario de la empresa A intente crear una fila con
  `empresa_id` de la empresa B.
- Pendiente (fuera de este ADR): decidir si el filtro se automatiza con un
  `@Filter` de Hibernate o un aspecto/interceptor, o si queda explícito en
  cada método de repositorio. Mientras no se decida, va explícito.
