# Propuesta de arquitectura (alterna) — Sistema de Gestión de Procesos

Propuesta independiente de arquitectura para la Entrega 1 del proyecto
semestral (empresas, usuarios, procesos y modelado del diagrama con JPA +
capas + Thymeleaf), con el mismo alcance que
[`ArchProposalDaza`](../ArchProposalDaza) para poder compararse punto a
punto, pero con decisiones distintas en los tres puntos donde más impacta la
arquitectura: **empaquetado**, **multitenencia** y **coherencia del
diagrama**.

## Resumen de las diferencias frente a `ArchProposalDaza`

| Punto | ArchProposalDaza | Esta propuesta |
|---|---|---|
| Empaquetado | 2 paquetes de dominio (`gestion/`, `modelado/`), cada uno con varias entidades adentro | 1 paquete por entidad (vertical slice), agrupados bajo los mismos prefijos `gestion.` / `modelado.` solo como namespace; entre features solo se habla a través de la interfaz pública del `Service` del otro, nunca de su repositorio |
| Multitenencia | Columna `empresa_id` explícita + métodos `*AndEmpresaId` en cada repositorio, filtro manual en cada query | Columna `empresa_id` explícita (se mantiene como red de seguridad de esquema) **+** filtro automático de Hibernate (`@Filter`) activado por request, para que el aislamiento no dependa de que cada desarrollador recuerde usar el método correcto |
| Coherencia del diagrama | Validadores de servicio por operación (`ArcoValidator`, `MensajeValidator`, `RolProcesoValidator`), invocados explícitamente antes de guardar | `Proceso` como raíz de agregado (DDD-lite): `Pool`, `Lane`, `NodoFlujo`, `Arco`, `Mensaje`, `Correlacion` no tienen alta/edición propia fuera de `Proceso` — solo se mutan a través de métodos del agregado, que hacen imposible construir un estado incoherente en primer lugar |

Las 13 entidades, sus campos y las reglas de negocio del enunciado son las
mismas (vienen dadas por el enunciado del curso, no son una decisión de
arquitectura); lo que cambia es **quién puede tocarlas y cómo se protege
esa regla**.

## Documentos

- [`docs/adr/`](docs/adr) — decisiones de arquitectura, con opciones
  consideradas y consecuencias.
- [`docs/modelo-datos.md`](docs/modelo-datos.md) — diccionario de las 13
  entidades JPA.
- [`docs/diagrams/diagrama-clases.md`](docs/diagrams/diagrama-clases.md) —
  diagrama de clases (Mermaid).
- [`docs/diagrams/diagrama-estados-proceso.md`](docs/diagrams/diagrama-estados-proceso.md)
  — ciclo de vida de `Proceso`.
- [`docs/diseno-agregado-proceso.md`](docs/diseno-agregado-proceso.md) —
  diseño de servicios organizados alrededor del agregado `Proceso`.
- [`docs/contratos-repositorios.md`](docs/contratos-repositorios.md) —
  firmas de los repositorios Spring Data propuestos.
- [`docs/diseno-controladores-thymeleaf.md`](docs/diseno-controladores-thymeleaf.md)
  — rutas y vistas de la entrega 1.

Documento de diseño — no código. Alcance: Entrega 1 (JPA + capas +
Thymeleaf: empresas, usuarios, procesos, modelado del diagrama).
