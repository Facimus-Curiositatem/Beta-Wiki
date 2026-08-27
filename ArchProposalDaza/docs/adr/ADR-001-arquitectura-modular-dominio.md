# ADR-001: Estructura modular por dominio (no por capa técnica)

## Estado
Aceptado.

## Contexto
El backend tiene 13 entidades que caen naturalmente en dos áreas de negocio
distintas y con ciclos de vida distintos:

- **gestión**: `Empresa`, `Usuario`, `Proceso`, `HistorialCambio`,
  `RolProceso` — alta de empresa, usuarios, control de acceso, ciclo de vida
  del proceso como registro (borrador/publicado, activo/inactivo).
- **modelado**: `Pool`, `Lane`, `NodoFlujo`/`Actividad`/`Gateway`, `Arco`,
  `Mensaje`, `Correlacion` — el diagrama BPMN simplificado en sí, con sus
  propias reglas de coherencia (arcos no cruzan pools, mensajes sí, lanes
  atadas a un rol).

Hay que decidir cómo organizar paquetes/módulos del backend antes de escribir
controllers/services/repositories para las 13 entidades ya creadas
(`ArchProposalDaza/src/main/java/com/facimus/procesos/`).

## Opciones consideradas

1. **Capas clásicas** (`controller/`, `service/`, `repository/`, `model/` a
   nivel raíz, como en el prototipo actual `beta/`). Funciona bien con pocas
   entidades (el prototipo tiene 1). Con 13 entidades y dos dominios
   distintos, todo controller queda en una sola carpeta sin distinción de a
   qué dominio pertenece — para tocar "todo lo de Proceso" hay que saltar
   entre 4 carpetas distintas cada vez.

2. **Hexagonal / puertos y adaptadores**. Separa dominio de infraestructura
   con puertos explícitos, útil cuando hay múltiples adaptadores de entrada/
   salida reales (varias UIs, varias fuentes de datos) o se necesita aislar
   fuerte el dominio de frameworks. Aquí el dominio es JPA desde el día uno
   (no hay lógica de dominio pura sin persistencia que aislar) y solo hay un
   adaptador de salida (una base de datos relacional). El costo de las
   interfaces de puerto adicionales no compra nada concreto en este proyecto
   — es sobrecoste de abstracción sin caso de uso que lo justifique.

3. **Modular por dominio**: un paquete raíz por área de negocio
   (`gestion/`, `modelado/`), cada uno con su propio `controller/`,
   `service/`, `repository/` interno. Refleja directamente las dos familias
   de entidades ya definidas, y localiza el cambio: tocar `Proceso` completo
   (entidad + repo + servicio + controller) queda en un solo paquete.

## Decisión
Opción 3, modular por dominio: `gestion/` y `modelado/`, cada uno con su
controller/service/repository interno (ver estructura ya reflejada en
`ArchProposalDaza/src/main/java/com/facimus/procesos/`).

Reglas derivadas de esta decisión:

- **Dependencia cruzada permitida en un solo sentido**: `modelado.Lane`
  referencia `gestion.RolProceso` (el rol se asigna a nivel de lane). No hay
  dependencia inversa — `gestion` no conoce clases de `modelado`, salvo
  `Proceso` que mantiene la colección de `Pool` (dueño natural del árbol del
  diagrama).
- **Capa de servicio única y compartida** entre los controladores Thymeleaf
  (entrega 1) y los controladores REST (entrega 2): la lógica de negocio
  (validaciones, orquestación, filtro multiempresa) vive en `service/`, y
  ambos tipos de controller la consumen — no se duplica entre un
  `ProcesoController` Thymeleaf y un `ProcesoRestController` futuro.
- Cada módulo es dueño de sus propios repositorios Spring Data; no se
  comparten repositorios entre `gestion` y `modelado`.

## Consecuencias
- Al crear los repositorios/servicios (siguiente ítem del backlog), van dentro
  de `gestion/` o `modelado/` según a qué entidad pertenecen, no en una
  carpeta plana `service/`.
- Un cambio de regla de negocio que toque tanto gestión como modelado (ej.
  "no se puede publicar un Proceso sin al menos un Pool") vive como
  orquestación en el servicio de `gestion.Proceso`, que puede depender de
  servicios de `modelado`, nunca al revés — mantiene la dirección de
  dependencia de este ADR.
- Si en el futuro aparece una segunda fuente de datos o un caso real de
  múltiples adaptadores, esta decisión se reabre con un ADR nuevo; hoy no
  hay caso de uso que justifique hexagonal.
