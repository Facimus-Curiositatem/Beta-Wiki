# Diseño de la API REST

Reemplaza a `diseno-controladores-thymeleaf.md` (ver
[ADR-004](adr/ADR-004-migracion-rest-jwt.md)). Documentación interactiva en
`/swagger-ui.html` con el backend en ejecución.

## Convenciones

- Base: `/api/v1`. Formato: JSON.
- Autenticación: `Authorization: Bearer <token>` en todo salvo login y
  registro de empresa (ver [seguridad.md](seguridad.md)).
- La empresa sale del token. Ningún endpoint recibe `empresaId`.
- Entrada y salida con DTO (`*Request` / `*Response`), nunca entidades JPA.

| Operación | Método | Respuesta correcta |
|---|---|---|
| Crear | `POST` | `201 Created` con cabecera `Location` |
| Consultar | `GET` | `200 OK` |
| Reemplazar datos | `PUT` | `200 OK` |
| Cambio parcial (estado, rol, activo) | `PATCH` | `200 OK` |
| Eliminar | `DELETE` | `204 No Content` |

## Errores

Todas las respuestas de error usan `ProblemDetail` (RFC 9457):

```json
{
  "type": "about:blank",
  "title": "Regla de negocio violada",
  "status": 409,
  "detail": "El origen y el destino de un arco deben pertenecer al mismo pool.",
  "instance": "/api/v1/arcos"
}
```

| Código | Cuándo |
|---|---|
| `400` | Campo inválido, JSON mal formado o parámetro con tipo incorrecto |
| `401` | Sin token, token inválido o expirado, o credenciales incorrectas |
| `403` | El rol no tiene permiso para la operación |
| `404` | El recurso no existe o pertenece a otra empresa |
| `409` | Se viola una regla de negocio (nombre duplicado, rol en uso, arco entre pools...) |
| `500` | Error inesperado (se registra en el log, sin detalles al cliente) |

## Paginación

`GET /api/v1/procesos` devuelve una página de 10 elementos ordenados por
`fechaModificacion` descendente. Parámetro `pagina` (desde 0):

```json
{ "content": [ ... ], "page": 0, "size": 10, "totalElements": 23, "totalPages": 3 }
```

## Endpoints

### Autenticación y empresas

| Método | Ruta | HU | Descripción |
|---|---|---|---|
| `POST` | `/auth/login` | 03 | Devuelve `accessToken`, `tokenType`, `expiresIn` y `usuario` |
| `POST` | `/auth/logout` | 03 | `204`. El cliente descarta el token |
| `POST` | `/empresas` | 01 | Registra la empresa y su usuario administrador |

### Usuarios (solo administrador)

| Método | Ruta | HU | Descripción |
|---|---|---|---|
| `GET` | `/usuarios` | 02 | Usuarios de la empresa |
| `POST` | `/usuarios` | 02 | Crea un colaborador |
| `GET` | `/usuarios/{id}` | 02 | Detalle |
| `PATCH` | `/usuarios/{id}` | 02 | Cambia rol o estado activo |
| `DELETE` | `/usuarios/{id}` | 02 | Desactiva (baja lógica) |

### Procesos

| Método | Ruta | HU | Descripción |
|---|---|---|---|
| `GET` | `/procesos?nombre=&estado=&categoria=&pagina=` | 07 | Búsqueda paginada |
| `POST` | `/procesos` | 04 | Crea en `BORRADOR` con el pool de la empresa |
| `GET` | `/procesos/{id}` | 07 | Detalle con historial |
| `PUT` | `/procesos/{id}` | 05 | Edita nombre, descripción y categoría |
| `PATCH` | `/procesos/{id}` | 05 | Cambia el estado (`BORRADOR → PUBLICADO`) |
| `GET` | `/procesos/{id}/historial` | 07 | Historial de cambios |
| `DELETE` | `/procesos/{id}` | 06 | Baja lógica |

### Roles de proceso

| Método | Ruta | HU | Descripción |
|---|---|---|---|
| `GET` | `/roles` | 20 | Lista con `procesosQueLoUsan` y `enUso` |
| `POST` | `/roles` | 17 | Crea (solo administrador) |
| `GET` | `/roles/{id}` | 20 | Detalle con uso |
| `PUT` | `/roles/{id}` | 18 | Edita (solo administrador) |
| `DELETE` | `/roles/{id}` | 19 | Baja lógica; `409` si está en uso |

### Pools y lanes

| Método | Ruta | HU | Descripción |
|---|---|---|---|
| `GET` | `/procesos/{procesoId}/pools` | 21 | Pools del proceso por `orden` |
| `POST` | `/procesos/{procesoId}/pools` | 21 | Agrega un participante |
| `GET` | `/pools/{id}` | 21 | Detalle |
| `PUT` | `/pools/{id}` | 21 | Edita nombre y tipo |
| `DELETE` | `/pools/{id}` | 21 | `409` si tiene nodos |
| `GET` | `/pools/{poolId}/lanes` | 22 | Lanes del pool por `orden` |
| `POST` | `/pools/{poolId}/lanes` | 22 | Crea una lane con su rol |
| `GET` | `/lanes/{id}` | 22 | Detalle |
| `PUT` | `/lanes/{id}` | 22 | Edita nombre y rol |
| `DELETE` | `/lanes/{id}` | 22 | `409` si tiene nodos |

### Actividades y gateways

| Método | Ruta | HU | Descripción |
|---|---|---|---|
| `GET` | `/lanes/{laneId}/actividades` | 08 | Actividades de la lane |
| `POST` | `/lanes/{laneId}/actividades` | 08 | Crea con nombre único en el proceso |
| `GET` | `/actividades/{id}` | 08 | Detalle |
| `PUT` | `/actividades/{id}` | 09 | Edita nombre, descripción y posición |
| `DELETE` | `/actividades/{id}` | 10 | Borra también sus arcos |
| `GET` | `/lanes/{laneId}/gateways` | 14 | Gateways de la lane |
| `POST` | `/lanes/{laneId}/gateways` | 14 | Crea (`EXCLUSIVO`, `PARALELO`, `INCLUSIVO`) |
| `GET` | `/gateways/{id}` | 14 | Detalle |
| `PUT` | `/gateways/{id}` | 15 | Edita |
| `DELETE` | `/gateways/{id}` | 16 | Borra también sus arcos |

### Arcos

| Método | Ruta | HU | Descripción |
|---|---|---|---|
| `GET` | `/pools/{poolId}/arcos` | 11 | Arcos del pool |
| `POST` | `/arcos` | 11 | Crea entre dos nodos del mismo pool |
| `GET` | `/arcos/{id}` | 11 | Detalle |
| `PUT` | `/arcos/{id}` | 12 | Edita etiqueta y condición |
| `DELETE` | `/arcos/{id}` | 13 | Elimina |

### Mensajes y correlación

| Método | Ruta | HU | Descripción |
|---|---|---|---|
| `GET` | `/procesos/{procesoId}/mensajes` | 25 | Mensajes del proceso |
| `POST` | `/procesos/{procesoId}/mensajes` | 25 | Crea entre dos pools distintos |
| `GET` | `/mensajes/{id}` | 25 | Detalle |
| `PUT` | `/mensajes/{id}` | 25 | Edita nombre y contenido |
| `DELETE` | `/mensajes/{id}` | 25 | Borra también la correlación |
| `GET` | `/mensajes/{mensajeId}/correlacion` | 28 | Criterio del mensaje; `404` si no tiene |
| `PUT` | `/mensajes/{mensajeId}/correlacion` | 28 | Crea o reemplaza el criterio (idempotente) |

## Herramientas de prueba

- **Swagger:** `/swagger-ui.html`.
- **Postman:** `postman/Facimus-Procesos-API.postman_collection.json`.
- **JMeter:** planes en `jmeter/`.

Pendientes en estas herramientas:

- En Postman, el script de login lee `json.empresaId` y `json.usuarioId`, pero
  `LoginResponse` los trae dentro de `usuario` (`json.usuario.id`).
- En JMeter, `plan-procesos-crud.jmx` usa `/procesos` sin `/api/v1`, y
  ningún plan envía el token Bearer, así que las peticiones protegidas
  responden `401`.