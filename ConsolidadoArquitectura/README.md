# Consolidado de arquitectura — Sistema de Gestion de Procesos

Propuesta consolidada y minimalista para la Entrega 1 del proyecto semestral.

## Principio rector

Implementar exactamente lo que pide el enunciado: 13 entidades, 4 enums, 8
elementos obligatorios de modelado. Sin entidades adicionales, sin
abstracciones especulativas, sin capas que el proyecto no necesita.

## Decisiones de arquitectura

### Empaquetado: modular por dominio (2 paquetes)

```
com.facimus.procesos
├── gestion/          Empresa, Usuario, Proceso, HistorialCambio, RolProceso
│   ├── controller/
│   ├── service/
│   ├── repository/
│   └── model/
└── modelado/         Pool, Lane, NodoFlujo, Actividad, Gateway, Arco, Mensaje, Correlacion
    ├── controller/
    ├── service/
    ├── repository/
    └── model/
```

**Por que:** Dos paquetes alineados con los dos bloques del enunciado. Suficiente
para separar responsabilidades sin fragmentar en 11 paquetes que complican la
navegacion.

### Multi-tenencia: columna directa + metodos explicitos

Toda entidad que pertenece a una empresa extiende `EntidadEmpresa`
(`@MappedSuperclass` con campo `empresa`). Los repositorios exponen metodos
`*AndEmpresaId` para acotar las consultas.

**Por que:** Es lo mas simple. No requiere interceptores, filtros de Hibernate
ni configuracion adicional. El aislamiento se verifica en cada query.

### Validacion: en la capa de servicio, antes de guardar

Cada servicio valida las reglas de negocio antes de persistir. Sin validadores
separados, sin agregados DDD, sin anotaciones custom. Si la regla falla, se
lanza una excepcion que el controlador traduce a mensaje de error.

**Por que:** Las reglas del enunciado son pocas y claras (arcos no cruzan pools,
mensajes si, roles no se borran si estan en uso). No justifican infraestructura
adicional.

### Herencia JPA: SINGLE_TABLE para NodoFlujo

`NodoFlujo` es abstracta. `Actividad` y `Gateway` la extienden con
`@Inheritance(SINGLE_TABLE)` y columna discriminadora `tipo_nodo`.

**Por que:** Una sola tabla, sin joins, consultas simples. Solo 2 subtipos con
pocos campos propios.

### Sesion y autenticacion (Entrega 1)

Login simple con `HttpSession`: se guarda `empresaId`, `usuarioId` y
`rolAcceso`. El controlador lee la sesion para filtrar datos por empresa.

**Por que:** Spring Security se aplica en la Entrega 3. Para la Entrega 1,
HttpSession es suficiente.

## Documentos

- [`modelo-datos.md`](modelo-datos.md) — 13 entidades, campos, reglas y diagrama de clases.
- [`servicios.md`](servicios.md) — Capa de servicios y mapeo a las 28 HU.
- [`controladores-entrega1.md`](controladores-entrega1.md) — Rutas Thymeleaf para la Entrega 1.
