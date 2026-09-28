# Historias de usuario y funcionamiento REST de Beta

> **Auditoría:** rama `main` de [Beta-back](https://github.com/Facimus-Curiositatem/Beta-back), commit `6fa7df30f6371a5b885ebcd775d190a67d16325a`.
>
> Los criterios se contrastaron con las [historias de usuario oficiales del curso](https://desarrolloweb.click/proyecto/hitoriasusuario/).

## Cómo leer esta página

Estados:

- ✅ **Cumplida:** la API actual cubre los criterios principales de la historia en backend.
- 🟡 **Parcial:** existe implementación REST, pero faltan uno o más criterios.
- 🔴 **Pendiente:** el concepto central todavía no existe en el modelo actual.

En criterios que corresponden principalmente a UI —por ejemplo, solicitar una confirmación antes de eliminar o redibujar visualmente un diagrama— se documenta la responsabilidad del backend y se deja la interacción visual para el cliente.

---

# Gestión de empresas y usuarios

## HU-01 — Registro de empresa ✅

**Objetivo:** crear el espacio independiente de una empresa y su administrador inicial.

**Endpoint principal**

```http
POST /api/v1/empresas
```

No requiere JWT porque es el punto de entrada para registrar una organización.

**Request conceptual**

```json
{
  "nombreEmpresa": "Empresa Demo",
  "nit": "900123456",
  "correoContacto": "contacto@demo.com",
  "nombreAdmin": "Administrador",
  "emailAdmin": "admin@demo.com",
  "passwordAdmin": "..."
}
```

**Funcionamiento**

1. `EmpresaController` valida el DTO.
2. `EmpresaService` comprueba que el NIT no esté repetido.
3. Crea la empresa.
4. Crea el primer usuario con rol `ADMINISTRADOR`.
5. La contraseña se almacena con BCrypt.
6. Devuelve `201 Created` y `Location`.

**Estado:** cumple el registro, NIT único, administrador inicial y aislamiento por empresa.

---

## HU-02 — Registro y gestión de usuarios 🟡

**Endpoints**

```http
GET    /api/v1/usuarios
POST   /api/v1/usuarios
GET    /api/v1/usuarios/{id}
PATCH  /api/v1/usuarios/{id}
DELETE /api/v1/usuarios/{id}
```

**Permiso:** reservado a `ADMINISTRADOR`.

**Funcionamiento**

- crear colaboradores dentro de la empresa autenticada;
- asignar `ADMINISTRADOR`, `EDITOR` o `LECTOR`;
- cambiar rol;
- activar/desactivar;
- consultar usuarios del tenant;
- un usuario desactivado permanece en base de datos.

**Pendiente frente a la HU:** la historia exige un flujo de **invitación por correo**. La API actual crea directamente el usuario con contraseña; no existe todavía token de invitación ni envío de correo.

---

## HU-03 — Inicio de sesión ✅

**Endpoints**

```http
POST /api/v1/auth/login
POST /api/v1/auth/logout
```

**Funcionamiento**

1. Se valida correo y contraseña.
2. La contraseña se contrasta con BCrypt.
3. Un login válido genera un JWT con la identidad y tenant.
4. Las peticiones posteriores usan `Authorization: Bearer <token>`.
5. Un fallo de autenticación devuelve un mensaje genérico.
6. La API es stateless; logout significa descartar el token del lado cliente.

**Aislamiento:** `ApiPrincipal` determina `empresaId`, `usuarioId` y rol.

**Observación:** la excepción de procesos compartidos entre empresas depende de HU-23, que aún está pendiente.

---

# Gestión de procesos

## HU-04 — Crear proceso ✅

**Endpoint**

```http
POST /api/v1/procesos
```

**Permisos:** `ADMINISTRADOR` o `EDITOR`.

**Request**

```json
{
  "nombre": "Proceso de ventas",
  "descripcion": "...",
  "categoria": "Comercial"
}
```

**Reglas**

- el proceso pertenece a la empresa autenticada;
- el nombre es único entre procesos activos de esa empresa;
- nace en estado `BORRADOR`;
- se crea automáticamente el pool inicial de la empresa;
- se registra el cambio en `HistorialCambio`;
- responde `201 Created`.

---

## HU-05 — Editar proceso ✅

**Endpoints**

```http
PUT   /api/v1/procesos/{id}
PATCH /api/v1/procesos/{id}
```

`PUT` modifica datos básicos. `PATCH` cambia el estado.

**Reglas**

- solo ADMINISTRADOR/EDITOR;
- conserva unicidad de nombre;
- permite modificar nombre, descripción y categoría;
- el estado puede cambiar de `BORRADOR` a `PUBLICADO`;
- un proceso publicado no vuelve a borrador;
- los cambios se registran en historial.

---

## HU-06 — Eliminar proceso ✅

**Endpoint**

```http
DELETE /api/v1/procesos/{id}
```

**Permiso:** `ADMINISTRADOR`.

La eliminación es lógica: `activo=false`.

**Consulta de inactivos**

```http
GET /api/v1/procesos?activo=false
```

Sin el parámetro `activo`, el listado devuelve solo procesos activos.

La baja queda registrada en historial.

---

## HU-07 — Consultar procesos 🟡

**Endpoints**

```http
GET /api/v1/procesos
GET /api/v1/procesos/{id}
GET /api/v1/procesos/{id}/historial
```

**Filtros soportados**

- `nombre`
- `estado`
- `categoria`
- `activo`
- `pagina`

El listado utiliza `PageResponse<T>`.

**Estado parcial:** el backend expone los recursos que forman el diagrama mediante endpoints específicos, pero no existe un endpoint único que entregue el diagrama BPMN completo; además, todavía no existe un modelo de **Evento BPMN**, exigido por la historia.

---

# Actividades

## HU-08 — Crear actividad 🟡

**Endpoints**

```http
POST /api/v1/lanes/{laneId}/actividades
GET  /api/v1/lanes/{laneId}/actividades
GET  /api/v1/actividades/{id}
```

**Implementado**

- nombre;
- descripción;
- posición X/Y;
- asociación obligatoria a una lane;
- validación de nombre de nodo único dentro del proceso;
- aislamiento por empresa.

**Falta**

- atributo/tipo de actividad definido por la HU;
- registrar la creación en el historial del proceso.

---

## HU-09 — Editar actividad 🟡

**Endpoint**

```http
PUT /api/v1/actividades/{id}
```

La edición permite nombre, descripción, posición y `laneId`.

**Reglas implementadas**

- puede moverse entre lanes;
- no puede moverse a otro proceso;
- si se mueve entre pools y tiene arcos conectados, se rechaza para no crear arcos cross-pool;
- conserva arcos cuando el movimiento es válido;
- mantiene unicidad del nombre del nodo.

**Falta**

- tipo de actividad;
- historial del cambio.

---

## HU-10 — Eliminar actividad 🟡

**Endpoint**

```http
DELETE /api/v1/actividades/{id}
```

**Permiso:** ADMINISTRADOR.

Antes de eliminar la actividad, el servicio elimina sus arcos entrantes y salientes.

**Falta**

- la eliminación actual es física, mientras la HU exige lógica;
- no se registra en historial;
- el backend no advierte si quedan elementos desconectados.

---

# Arcos

## HU-11 — Crear arco 🟡

**Endpoints**

```http
POST /api/v1/arcos
GET  /api/v1/arcos/{id}
GET  /api/v1/pools/{poolId}/arcos
```

**Reglas**

- origen y destino diferentes;
- origen y destino deben pertenecer al mismo pool;
- no permite dos arcos idénticos;
- si el origen es gateway EXCLUSIVO o INCLUSIVO, exige condición;
- todos los IDs se resuelven dentro del tenant autenticado.

**Falta:** la HU permite conectar también eventos; actualmente solo existen actividades y gateways como nodos de flujo.

---

## HU-12 — Editar arco 🟡

**Endpoint**

```http
PUT /api/v1/arcos/{id}
```

Permite editar:

- `origenId`;
- `destinoId`;
- etiqueta;
- condición.

Reaplica las validaciones de creación.

**Falta:** registrar la modificación en el historial del proceso.

---

## HU-13 — Eliminar arco 🟡

**Endpoint**

```http
DELETE /api/v1/arcos/{id}
```

**Permiso:** ADMINISTRADOR.

La conexión se elimina correctamente.

**Falta**

- historial;
- advertencia de nodos sin camino de entrada/salida;
- la operación actual es eliminación física.

---

# Gateways

## HU-14 — Crear gateway 🟡

**Endpoints**

```http
POST /api/v1/lanes/{laneId}/gateways
GET  /api/v1/lanes/{laneId}/gateways
GET  /api/v1/gateways/{id}
```

Tipos soportados:

- `EXCLUSIVO`
- `PARALELO`
- `INCLUSIVO`

Los arcos salientes de EXCLUSIVO/INCLUSIVO deben tener condición.

**Falta**

- validación global de que un gateway divergente tenga al menos dos arcos salientes;
- historial de creación.

---

## HU-15 — Editar gateway 🟡

**Endpoint**

```http
PUT /api/v1/gateways/{id}
```

Permite cambiar nombre, tipo y posición.

**Reglas adicionales implementadas**

- nombre de nodo único por proceso;
- al pasar EXCLUSIVO/INCLUSIVO → PARALELO se limpian las condiciones de los arcos salientes;
- al pasar PARALELO → EXCLUSIVO/INCLUSIVO se rechaza el cambio si existen arcos salientes sin condición.

**Falta**

- advertencia/validación de condiciones mutuamente excluyentes;
- historial de modificación.

---

## HU-16 — Eliminar gateway 🟡

**Endpoint**

```http
DELETE /api/v1/gateways/{id}
```

**Permiso:** ADMINISTRADOR.

Se eliminan también sus arcos entrantes y salientes.

**Falta**

- eliminación lógica;
- historial;
- advertencia de incoherencia del flujo después de eliminarlo.

---

# Roles de proceso

## HU-17 — Crear rol de proceso ✅

**Endpoint**

```http
POST /api/v1/roles
```

**Permiso:** ADMINISTRADOR.

Crea un rol con nombre y descripción, asociado a la empresa. El nombre se valida como único dentro del catálogo activo de la empresa.

Los roles de proceso se reutilizan en las lanes.

---

## HU-18 — Editar rol de proceso 🟡

**Endpoint**

```http
PUT /api/v1/roles/{id}
```

**Permiso:** ADMINISTRADOR.

Al editar el rol se conserva su ID, por lo que las lanes existentes siguen apuntando al mismo rol.

**Falta:** registrar el cambio en el historial.

---

## HU-19 — Eliminar rol de proceso ✅

**Endpoint**

```http
DELETE /api/v1/roles/{id}
```

**Permiso:** ADMINISTRADOR.

**Reglas**

- busca lanes que usan el rol;
- si está en uso, rechaza la eliminación;
- informa los procesos en los que se utiliza;
- si no está en uso, realiza baja lógica (`activo=false`).

La confirmación previa a la eliminación corresponde al cliente.

---

## HU-20 — Consultar roles 🟡

**Endpoints**

```http
GET /api/v1/roles
GET /api/v1/roles/{id}
```

La respuesta incluye nombre, descripción, número de usos y si puede eliminarse.

**Falta frente a la HU**

- búsqueda por nombre;
- paginación;
- devolver explícitamente la lista de procesos donde se utiliza el rol, no solo el conteo.

---

# Pools y lanes

## HU-21 — Configurar pool por empresa 🟡

**Endpoints**

```http
GET    /api/v1/procesos/{procesoId}/pools
POST   /api/v1/procesos/{procesoId}/pools
GET    /api/v1/pools/{id}
PUT    /api/v1/pools/{id}
DELETE /api/v1/pools/{id}
```

Los pools poseen nombre, tipo de participante, bandera de caja negra y orden.

El proceso crea automáticamente un pool inicial para la empresa propietaria.

**Falta**

- impedir de forma explícita que un pool marcado como caja negra tenga lanes/nodos internos;
- historial de cambios de pool;
- eliminación lógica uniforme.

---

## HU-22 — Pool y lane ✅

**Endpoints**

```http
GET    /api/v1/pools/{poolId}/lanes
POST   /api/v1/pools/{poolId}/lanes
GET    /api/v1/lanes/{id}
PUT    /api/v1/lanes/{id}
PATCH  /api/v1/pools/{poolId}/lanes/orden
DELETE /api/v1/lanes/{id}
```

**Reglas implementadas**

- cada lane pertenece a un pool;
- cada lane referencia un rol de proceso;
- una actividad pertenece exactamente a una lane;
- una actividad puede cambiar de lane mediante HU-09;
- se pueden reordenar lanes;
- el reordenamiento exige exactamente todas las lanes, sin duplicados y del mismo pool;
- no se elimina una lane que contenga nodos.

**Observación:** el sistema elimina físicamente una lane vacía; la regla global de trazabilidad podría requerir evolucionar esto a baja lógica.

---

## HU-23 — Compartir procesos entre empresas 🔴

La historia exige marcar un proceso como compartido con empresas invitadas y acceso de solo lectura.

**Estado actual:** no existe en `Proceso` ni en la API un modelo de:

- proceso compartido;
- empresas invitadas;
- permisos read-only cross-tenant;
- historial del estado de compartición.

Por tanto, la excepción de aislamiento entre empresas todavía no está implementada.

---

## HU-24 — Roles y permisos de pool 🟡

La parte de roles de proceso está integrada:

- las lanes solo reciben `RolProceso` existentes del tenant;
- no se puede eliminar un rol en uso.

La API también aplica permisos por rol de acceso:

- LECTOR: GET;
- EDITOR: modificaciones permitidas;
- ADMINISTRADOR: operaciones sensibles y DELETE.

**Falta:** la HU pide poder **configurar** qué roles de acceso pueden crear/editar/eliminar pools y lanes. Actualmente la matriz está definida estáticamente en `SecurityConfig`, no por pool.

---

# Mensajes y colaboración

## HU-25 — Message Throw 🟡

**Endpoints actuales de mensajes**

```http
GET    /api/v1/procesos/{procesoId}/mensajes
POST   /api/v1/procesos/{procesoId}/mensajes
GET    /api/v1/mensajes/{id}
PUT    /api/v1/mensajes/{id}
DELETE /api/v1/mensajes/{id}
```

El modelo `Mensaje` conecta:

- proceso;
- pool origen;
- pool destino;
- nombre;
- contenido.

No permite que origen y destino sean el mismo pool.

**Estado parcial:** esto modela la comunicación entre pools, pero todavía no existe un nodo BPMN explícito **Message Throw**, ni la validación con un Message Catch correspondiente, ni esquema de datos/historial completo requerido por la HU.

---

## HU-26 — Notificaciones externas 🔴

La HU requiere modelar mensajes hacia sistemas externos y documentar su mecanismo, por ejemplo correo, servicio web o cola.

Actualmente no existe en `main` un modelo para:

- tipo de destino externo;
- EMAIL / WEB_SERVICE / QUEUE;
- datos de configuración del destino;
- comportamiento ante fallo.

Los pools externos existen, pero no cubren esta historia por sí solos.

---

## HU-27 — Message Catch 🔴

La HU exige elementos BPMN Message Catch de:

- inicio;
- intermedio.

Actualmente no existe entidad/DTO/controller de Evento o Message Catch.

Por tanto todavía no se puede:

- distinguir inicio/intermedio;
- impedir arcos entrantes a un catch de inicio;
- declarar datos esperados;
- asociar Throw/Catch por nombre;
- marcar origen externo.

---

## HU-28 — Correlación de mensajes 🟡

**Endpoints**

```http
GET    /api/v1/mensajes/{mensajeId}/correlacion
PUT    /api/v1/mensajes/{mensajeId}/correlacion
DELETE /api/v1/mensajes/{mensajeId}/correlacion
```

El modelo permite definir un criterio de correlación por mensaje.

**Falta frente a la HU**

- coherencia explícita entre Message Throw y Message Catch;
- advertir catch intermedio sin correlación;
- detectar ambigüedad por mismo nombre + misma clave;
- documentar/representar la política cuando no existe un caso en espera.

Estas reglas dependen también de implementar HU-25/HU-27 como elementos BPMN completos.

---

# Resumen de cumplimiento

| HU | Estado | Tema |
|---|---|---|
| HU-01 | ✅ | Registro de empresa |
| HU-02 | 🟡 | Usuarios |
| HU-03 | ✅ | Login |
| HU-04 | ✅ | Crear proceso |
| HU-05 | ✅ | Editar proceso |
| HU-06 | ✅ | Eliminar proceso |
| HU-07 | 🟡 | Consultar procesos |
| HU-08 | 🟡 | Crear actividad |
| HU-09 | 🟡 | Editar actividad |
| HU-10 | 🟡 | Eliminar actividad |
| HU-11 | 🟡 | Crear arco |
| HU-12 | 🟡 | Editar arco |
| HU-13 | 🟡 | Eliminar arco |
| HU-14 | 🟡 | Crear gateway |
| HU-15 | 🟡 | Editar gateway |
| HU-16 | 🟡 | Eliminar gateway |
| HU-17 | ✅ | Crear rol |
| HU-18 | 🟡 | Editar rol |
| HU-19 | ✅ | Eliminar rol |
| HU-20 | 🟡 | Consultar roles |
| HU-21 | 🟡 | Pools |
| HU-22 | ✅ | Lanes |
| HU-23 | 🔴 | Compartir procesos |
| HU-24 | 🟡 | Permisos de pool |
| HU-25 | 🟡 | Message Throw |
| HU-26 | 🔴 | Notificación externa |
| HU-27 | 🔴 | Message Catch |
| HU-28 | 🟡 | Correlación |

## Próximas prioridades sugeridas

1. HU-23 — procesos compartidos entre empresas.
2. HU-27 — modelar Evento/Message Catch.
3. HU-25 — Message Throw como nodo BPMN real.
4. HU-26 — destinos externos.
5. tipo de actividad para HU-08/HU-09.
6. historial de modificaciones de elementos BPMN.
7. estrategia de eliminación lógica para todos los elementos.
8. validación de coherencia global del diagrama.
9. completar búsqueda/paginación de roles.
10. endurecer reglas de pools caja negra.

---

## Fuente oficial

- [Listado completo de historias de usuario](https://desarrolloweb.click/proyecto/hitoriasusuario/)
