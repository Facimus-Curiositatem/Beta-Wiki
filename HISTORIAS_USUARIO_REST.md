# Historias de usuario y API REST

> **Documentación navegable:** la aplicación Wiki contiene una página individual para cada HU (HU-01 a HU-28). Cada página explica endpoints, flujo Controller -> Service -> Repository, archivos involucrados y reglas de negocio. La sección `API REST - Arquitectura del backend` explica la arquitectura transversal de seguridad, DTOs, errores y paginación.

Este documento describe cómo se implementan las 28 historias de usuario del proyecto en el backend `Beta-back`.

La API usa el prefijo `/api/v1`, autenticación Bearer JWT, aislamiento por empresa y respuestas JSON. El identificador de empresa se obtiene del usuario autenticado y no se confía en un `empresaId` enviado por el cliente.

> Estado documentado: cambios incluidos en el PR #20 de `Beta-back` (`feature/completar-hu-01-28`).

## Gestión de empresas y usuarios

| HU | Historia | Implementación REST |
|---|---|---|
| HU-01 | Registro de empresa | `POST /api/v1/empresas`. Crea la empresa y su contexto inicial. |
| HU-02 | Registro de usuario en empresa | `POST /api/v1/usuarios` para alta administrativa. También existe `POST /api/v1/usuarios/invitaciones` y `POST /api/v1/usuarios/invitaciones/{token}/aceptar` para incorporación mediante invitación. |
| HU-03 | Inicio de sesión | `POST /api/v1/auth/login` devuelve el acceso autenticado mediante JWT. `POST /api/v1/auth/logout` cierra el flujo lógico de sesión del cliente. |

### Invitaciones

Las invitaciones duran 48 horas. Mientras el proyecto no tenga integración SMTP, el token se devuelve al administrador para que pueda compartirlo manualmente con la persona invitada. Debe tratarse como una credencial temporal.

Antes de crear una nueva invitación se eliminan invitaciones pendientes ya expiradas. La aceptación también rechaza explícitamente un token expirado.

## Gestión de procesos

| HU | Historia | Implementación REST |
|---|---|---|
| HU-04 | Crear proceso | `POST /api/v1/procesos`. El proceso nace en estado borrador y queda asociado a la empresa autenticada. |
| HU-05 | Editar proceso | `PUT /api/v1/procesos/{id}` actualiza sus datos. `PATCH /api/v1/procesos/{id}` gestiona cambios parciales/estado según el contrato actual. |
| HU-06 | Eliminar proceso | `DELETE /api/v1/procesos/{id}`. La eliminación es lógica para conservar trazabilidad. |
| HU-07 | Consultar procesos | `GET /api/v1/procesos`, `GET /api/v1/procesos/{id}`, `GET /api/v1/procesos/{id}/diagrama` y `GET /api/v1/procesos/{id}/historial`. |

### Publicación y validación

Antes de publicar un proceso se valida la coherencia del modelo BPMN. Entre otras reglas:

- los pools de caja negra no pueden contener elementos internos;
- un gateway divergente debe tener al menos dos salidas;
- gateways exclusivos e inclusivos requieren condiciones en sus salidas;
- un Message Catch de inicio no puede tener arcos entrantes activos;
- un mensaje publicable debe tener Message Throw;
- debe existir receptor interno o destino externo válido;
- la correlación debe estar definida y no ser ambigua.

La validación trabaja con consultas por lote para lanes, nodos y arcos del proceso.

## Actividades, arcos y gateways

### Actividades

| HU | Historia | Implementación REST |
|---|---|---|
| HU-08 | Crear actividad | `POST /api/v1/lanes/{laneId}/actividades`. |
| HU-09 | Editar actividad | `PUT /api/v1/actividades/{id}`. |
| HU-10 | Eliminar actividad | `GET /api/v1/actividades/{id}/impacto-eliminacion` permite revisar el impacto y `DELETE /api/v1/actividades/{id}` aplica baja lógica. |

Tipos soportados: `TAREA`, `USUARIO`, `SERVICIO`, `MANUAL`, `SCRIPT` y `REGLA_NEGOCIO`.

Los nombres son únicos dentro del proceso y los cambios se registran en el historial.

### Arcos

| HU | Historia | Implementación REST |
|---|---|---|
| HU-11 | Crear arco | `POST /api/v1/arcos`. |
| HU-12 | Editar arco | `PUT /api/v1/arcos/{id}`. |
| HU-13 | Eliminar arco | `GET /api/v1/arcos/{id}/impacto-eliminacion` y `DELETE /api/v1/arcos/{id}`. |

Reglas principales:

- origen y destino no pueden ser el mismo nodo;
- ambos nodos deben pertenecer al mismo pool;
- no se permiten arcos activos duplicados;
- la eliminación se maneja con `activo=false`;
- las validaciones ignoran arcos ya inactivos.

### Gateways

| HU | Historia | Implementación REST |
|---|---|---|
| HU-14 | Crear gateway | `POST /api/v1/lanes/{laneId}/gateways`. |
| HU-15 | Editar gateway | `PUT /api/v1/gateways/{id}`. |
| HU-16 | Eliminar gateway | `GET /api/v1/gateways/{id}/impacto-eliminacion` y `DELETE /api/v1/gateways/{id}`. |

Se soportan gateways `EXCLUSIVO`, `PARALELO` e `INCLUSIVO`.

Al cambiar el tipo de gateway solo se consideran arcos activos. Un arco eliminado lógicamente no bloquea cambios posteriores del gateway.

## Roles de proceso

| HU | Historia | Implementación REST |
|---|---|---|
| HU-17 | Crear rol de proceso | `POST /api/v1/roles`. |
| HU-18 | Editar rol de proceso | `PUT /api/v1/roles/{id}`. |
| HU-19 | Eliminar rol de proceso | `DELETE /api/v1/roles/{id}`. |
| HU-20 | Consultar roles de proceso | `GET /api/v1/roles`, `GET /api/v1/roles/{id}` y `GET /api/v1/roles/consulta`. |

La consulta avanzada de roles usa paginación de Spring Data en base de datos; ya no carga todos los registros para paginar en memoria. La respuesta también permite conocer en qué procesos se usa cada rol.

## Pools y lanes

| HU | Historia | Implementación REST |
|---|---|---|
| HU-21 | Configurar pool por empresa | `GET/POST /api/v1/procesos/{procesoId}/pools`, `GET/PUT/DELETE /api/v1/pools/{id}`. |
| HU-22 | Diferenciar pool y lane | `GET/POST /api/v1/pools/{poolId}/lanes`, `GET/PUT/DELETE /api/v1/lanes/{id}`, `PATCH /api/v1/pools/{poolId}/lanes/orden`. |
| HU-23 | Compartir procesos entre pools / empresas | `GET/POST /api/v1/procesos/{procesoId}/compartidos`, `DELETE /api/v1/procesos/{procesoId}/compartidos/{empresaInvitadaId}`, `GET /api/v1/procesos-compartidos` y `GET /api/v1/procesos-compartidos/{procesoId}`. |
| HU-24 | Asociar roles y permisos a un pool | `GET /api/v1/permisos-estructura` y `PUT /api/v1/permisos-estructura/{rol}`. |

### Compartición

El proceso sigue perteneciendo a la empresa propietaria. La empresa invitada obtiene una vista de solo lectura.

Una compartición revocada puede reactivarse posteriormente. Para revocar se consulta únicamente una compartición activa, evitando operar accidentalmente sobre registros históricos revocados.

### Permisos de estructura

Los permisos se configuran por rol de acceso y empresa para:

- crear, editar y eliminar pools;
- crear, editar y eliminar lanes.

El rol `SOLO_LECTURA` nunca puede recibir permisos de escritura.

## Mensajes y colaboración entre pools

### HU-25 — Message Throw

Endpoints relacionados:

- `POST /api/v1/lanes/{laneId}/eventos-mensaje`
- `GET /api/v1/eventos-mensaje/{id}`
- `PUT /api/v1/eventos-mensaje/{id}`
- `DELETE /api/v1/eventos-mensaje/{id}`
- `POST /api/v1/procesos/{procesoId}/mensajes`

Un evento de mensaje puede ser `THROW`, `CATCH_INICIO` o `CATCH_INTERMEDIO`.

El Throw debe pertenecer al pool origen y su nombre/correlación deben ser coherentes con el mensaje.

### HU-26 — Notificación a sistema externo

Un mensaje puede documentar un destino externo de tipo:

- `CORREO`;
- `SERVICIO_WEB`;
- `COLA`.

El pool destino debe ser `SISTEMA_EXTERNO` y estar configurado como caja negra.

También se documenta qué ocurre si la notificación falla:

- `CONTINUAR_FLUJO`;
- `DERIVAR_ACTIVIDAD_ERROR`;
- `FINALIZAR_PROCESO`.

Si se usa `DERIVAR_ACTIVIDAD_ERROR`, la actividad seleccionada debe pertenecer al mismo proceso.

El sistema modela estas integraciones; no envía correos, no consume servicios externos y no ejecuta colas reales.

### HU-27 — Message Catch

Los Message Catch se crean y editan mediante los endpoints de `eventos-mensaje`.

Un `CATCH_INICIO` no puede tener arcos entrantes activos. Los arcos que ya fueron eliminados lógicamente no bloquean esta validación.

### HU-28 — Correlación

Endpoints:

- `GET /api/v1/mensajes/{mensajeId}/correlacion`
- `PUT /api/v1/mensajes/{mensajeId}/correlacion`
- `DELETE /api/v1/mensajes/{mensajeId}/correlacion`

Cada mensaje puede documentar una clave de correlación. El backend valida la coherencia entre Throw, Catch y mensaje, y evita combinaciones ambiguas de nombre + clave dentro del mismo proceso.

También se documenta la política cuando un mensaje no encuentra un caso receptor:

- `DESCARTAR`;
- `INICIAR_CASO_NUEVO`.

## Edición y eliminación de mensajes

Endpoints:

- `GET /api/v1/procesos/{procesoId}/mensajes`
- `GET /api/v1/mensajes/{id}`
- `POST /api/v1/procesos/{procesoId}/mensajes`
- `PUT /api/v1/mensajes/{id}`
- `DELETE /api/v1/mensajes/{id}`

Los mensajes usan baja lógica mediante el campo `activo`. Las consultas normales solo devuelven mensajes activos.

La edición extendida tiene semántica parcial: si un campo opcional llega como `null`, se conserva el valor existente. Esto evita borrar por accidente el destino externo, la política de fallo o la política de correlación al modificar únicamente otro atributo.

## Historial y trazabilidad

Los cambios relevantes del modelado se registran mediante el servicio de auditoría en el historial del proceso. Esto cubre creación, edición, eliminación lógica y cambios de estructura.

## Alcance

Beta es un editor y visualizador de procesos. El backend valida y persiste el modelo, pero no ejecuta instancias BPMN reales.

No se ejecutan tareas, no se disparan procesos reales y no se entregan mensajes o notificaciones a sistemas externos.
