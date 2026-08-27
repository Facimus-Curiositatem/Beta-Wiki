# Proyecto: Sistema de Gestión de Procesos Multiempresa

## Qué es esto
Visor/editor de procesos empresariales estilo BPMN simplificado, multiempresa (multi-tenant).
Curso de Desarrollo Web. El sistema **modela** procesos — no los ejecuta, no dispara flujos
automáticos, no envía mensajes reales, no se conecta a servidores de correo/colas/APIs reales.

Enunciado completo: ver `docs/enunciado.md`.

## Alcance de `ArchProposalDaza`

Este directorio es **solo la propuesta de arquitectura documentada**: ADRs, diccionario de
entidades, contratos de repositorios, diagramas. **No contiene ni debe contener código
funcional** (nada de `.java`, `pom.xml`, proyecto Maven compilable). El código real del proyecto
del curso se implementará más adelante en el repo `proyecto-backend` (ver decisión de repos
separados abajo), usando estos documentos como especificación. Si en algún momento se agrega
código aquí para "verificar que el diseño funciona", eso es un cambio de alcance — avisar antes
de hacerlo, no asumirlo.

## Calendario de entregas

| Entrega | Fecha | Peso | Contenido |
|---|---|---|---|
| 1ª — Dominio + Thymeleaf | 14/09/2026 | 15% | Spring Boot + JPA + Thymeleaf: empresas, usuarios, procesos |
| 2ª — REST + Angular | 21/10/2026 | 25% | API REST, SPA Angular, empaquetado Docker |
| Final — Seguridad y despliegue | 25/11/2026 | 20% | Spring Security, pruebas integración/E2E, despliegue |

**Fase actual: preparando la 1ª entrega.**

## Elementos obligatorios del dominio (los siete del modelado)

| Elemento | Qué representa |
|---|---|
| Pool | Participante del proceso (empresa, cliente, proveedor, sistema externo) |
| Lane | División interna del pool, agrupa actividades de un mismo rol |
| Actividad | Una tarea del proceso |
| Gateway | Punto de decisión: exclusiva, paralela o inclusiva |
| Arco | Secuencia de flujo entre actividades/gateways, dentro de un mismo pool |
| Mensaje | Comunicación entre pools (throw/catch) |
| Correlación | Criterio que identifica a qué caso corresponde un mensaje |

Rol de proceso (Analista, Supervisor, Auditor) es la función responsable, no la persona.

## Decisiones de arquitectura ya tomadas (no reabrir sin un ADR nuevo)

- **Repos separados**: `proyecto-backend` (Spring Boot) y `proyecto-frontend` (Angular, desde la
  entrega 2), coordinados con docker-compose. Elegido por separación clara de responsabilidades
  y presentación más profesional, sobre la alternativa de monorepo.
- **Estructura modular por dominio (ADR-001)** dentro del backend, no por capa técnica (se
  evaluaron capas clásicas y hexagonal; modular por dominio ganó por reflejar directamente el
  modelo de entidades sin el sobrecosto de abstracción de hexagonal). Detalle completo:
  `ArchProposalDaza/docs/adr/ADR-001-arquitectura-modular-dominio.md`.
  - `gestion/` → Empresa, Usuario, Proceso, HistorialCambio, RolProceso
  - `modelado/` → Pool, Lane, NodoFlujo, Actividad, Gateway, Arco, Mensaje, Correlacion
  - Cada módulo tiene su propio controller/service/repository interno.
  - Dependencia cruzada conocida: `Lane` (modelado) referencia `RolProceso` (gestión) — el rol
    se asigna a nivel de lane, no de actividad directamente.
  - Los controladores Thymeleaf (entrega 1) y REST (entrega 2) deben compartir la misma capa
    de servicio — no duplicar lógica de negocio entre los dos tipos de controller.
  - Paquete base de la propuesta: `com.facimus.procesos` (por el org de GitHub del equipo,
    Facimus-Curiositatem), con submódulos `gestion` y `modelado` como se listó arriba.
- **Multi-tenencia (ADR-002)**: toda entidad excepto `Empresa` extiende una superclase común
  `EntidadEmpresa` (`@MappedSuperclass`) que aporta `id` + `empresa` (`@ManyToOne` obligatorio,
  `empresa_id` columna directa, `updatable = false`). Así el filtro por tenant es una condición
  plana en cualquier repositorio (`WHERE empresa_id = :empresaId`), sin depender de reconstruir
  el join correcto en cada query nueva — crítico porque `NodoFlujo` está a 3 saltos de `Empresa`.
  Toda query de repositorio debe filtrar por `empresa_id`. Sin excepciones, sin importar qué tan
  trivial parezca la query nueva. Detalle completo: `ArchProposalDaza/docs/adr/ADR-002-multitenencia-empresa-id.md`.
- **Herencia JPA**: `NodoFlujo` es abstracta, estrategia `SINGLE_TABLE`, subtipos `Actividad` y `Gateway`.
- **Enums** (`RolAcceso`, `EstadoProceso`, `TipoParticipante`, `TipoGateway`): mapeados `STRING`,
  nunca `ORDINAL` (evita corromper datos si se reordena el enum).
- **Eliminación lógica**: `Proceso` usa booleano `activo`. Nunca DELETE físico.
- **Arco**: dos `@ManyToOne` autoreferenciados (origen/destino) sobre `NodoFlujo`.

## Reglas de negocio a validar (no a ejecutar)

- Las actividades pertenecen a una lane (ya garantizado por FK `lane_id nullable = false`,
  es integridad referencial, no necesita validador).
- Los arcos no cruzan pools; los mensajes sí.
- Los roles de proceso no se pueden eliminar mientras estén en uso.
- **ADR-003**: esta validación es lógica cross-entidad, no se resuelve solo con `@Valid`/Bean
  Validation. Vive en validadores de servicio dedicados por módulo y por operación
  (`modelado.service.ArcoValidator`, `modelado.service.MensajeValidator`,
  `gestion.service.RolProcesoValidator`), invocados explícitamente antes de `save`/`delete`,
  no en un `ProcesoValidator` monolítico ni en `@AssertTrue` sobre la entidad. Lanzan
  `ReglaDiagramaInvalidaException`, traducida a error de vista/HTTP por un `@ControllerAdvice`
  compartido. Detalle completo: `ArchProposalDaza/docs/adr/ADR-003-validacion-coherencia-diagrama.md`.

## Dónde vive la documentación

- `docs/adr/` — decisiones de arquitectura, formato ADR (contexto, opciones, trade-offs, consecuencias)
- `docs/diagrams/` — diagramas Mermaid (clases, componentes, secuencia, estados), embebidos en
  bloques ` ```mermaid ` dentro de archivos `.md` para que se rendericen sin herramientas extra
- `docs/modelo-datos.md` — diccionario de las 13 entidades (campos, tipos, relaciones, notas de
  diseño), la especificación para implementarlas cuando exista `proyecto-backend`
- `docs/contratos-repositorios.md` — firmas de los 13 repositorios Spring Data propuestos
  (contrato, no código): qué expone cada uno y por qué
- `docs/diseno-servicios-gestion.md` / `docs/diseno-servicios-modelado.md` — responsabilidades
  de cada servicio, qué validador de ADR-003 invoca cada uno, puntos abiertos (ej. borrado de
  Pool/NodoFlujo) sin decidir todavía
- `docs/diseno-controladores-thymeleaf.md` — rutas, vistas y cómo se resuelve la empresa activa
  en sesión antes de que exista Spring Security

## Backlog inmediato (entrega 1) — todo lo de abajo es documentación de diseño, no código.
## Marca lo ya hecho antes de empezar sesión.

- [x] ADR-001: arquitectura modular por dominio —
      `ArchProposalDaza/docs/adr/ADR-001-arquitectura-modular-dominio.md`
- [x] ADR-002: multi-tenencia por filtro `empresa_id` —
      `ArchProposalDaza/docs/adr/ADR-002-multitenencia-empresa-id.md`
- [x] ADR-003: dónde vive la validación de coherencia del diagrama —
      `ArchProposalDaza/docs/adr/ADR-003-validacion-coherencia-diagrama.md`
- [x] Diccionario de las 13 entidades del dominio (campos, tipos, relaciones) —
      `ArchProposalDaza/docs/modelo-datos.md`
- [x] Contratos de los 13 repositorios Spring Data (firmas, filtro `empresa_id`) —
      `ArchProposalDaza/docs/contratos-repositorios.md`
- [x] Diagrama de clases Mermaid generado desde el diccionario de entidades —
      `ArchProposalDaza/docs/diagrams/diagrama-clases.md`
- [x] Diseño de servicios `gestion` (documento): responsabilidades de alta de empresa, usuario
      admin inicial, colaboradores, roles de acceso —
      `ArchProposalDaza/docs/diseno-servicios-gestion.md`. Nota: `Proceso`/`HistorialCambio`
      quedaron aquí (no en `modelado`) por ser entidades de `gestion` según ADR-001; el backlog
      original los tenía mal ubicados.
- [x] Diseño de servicios `modelado` (documento): responsabilidades de Pool/Lane/NodoFlujo/
      Arco/Mensaje, y dónde se invoca cada validador de ADR-003 —
      `ArchProposalDaza/docs/diseno-servicios-modelado.md`. Deja abiertos (sin decidir):
      borrado de `Pool` y borrado de `NodoFlujo` con arcos asociados.
- [x] Diseño de controladores + vistas Thymeleaf (documento): rutas, vistas, y cómo se resuelve
      la empresa activa en sesión antes de Spring Security — para empresas, usuarios, procesos —
      `ArchProposalDaza/docs/diseno-controladores-thymeleaf.md`
- [x] Diagrama de estados de `EstadoProceso` —
      `ArchProposalDaza/docs/diagrams/diagrama-estados-proceso.md`. Deja abierta (sin decidir):
      si existe `despublicar()` (PUBLICADO → BORRADOR).

## Cómo trabajar en este proyecto

- Todo lo que se produce aquí es documento (`.md`), no código. Si una tarea del backlog "suena"
  a implementación (crear entidades, repositorios, servicios, controladores), el entregable es
  su especificación en un `.md` dentro de `docs/`, no un archivo `.java` ni un proyecto Maven.
- Antes de escribir documentación nueva, verifica el estado real de `ArchProposalDaza/docs/`
  (qué ya está especificado) en vez de asumir a partir de este archivo — este archivo puede
  quedar desactualizado.
- Si tomas una decisión de arquitectura nueva, créala como ADR en `docs/adr/` y añádela a la
  sección de decisiones de arriba.
- Al terminar una sesión, actualiza el checklist del backlog marcando lo completado.
