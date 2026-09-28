# Arquitectura actual de Beta-back — Servicio RESTful

> **Fuente de verdad:** rama `main` de [Beta-back](https://github.com/Facimus-Curiositatem/Beta-back), auditada sobre el commit `6fa7df30f6371a5b885ebcd775d190a67d16325a` (merge del PR #17).
>
> Esta página describe la arquitectura **vigente** del backend. Los documentos antiguos de este repositorio que hablan de controladores Thymeleaf corresponden a propuestas o entregas históricas y **no representan la arquitectura actual de Beta-back**.

## 1. Qué es Beta

Beta es un editor y visualizador de procesos empresariales basado en conceptos BPMN. El backend permite administrar empresas, usuarios, procesos, roles de proceso y los elementos que forman el modelo: pools, lanes, actividades, gateways, arcos, mensajes y correlaciones.

El backend actual no renderiza vistas HTML. Se expone como una API RESTful y se comunica mediante HTTP + JSON.

## 2. Arquitectura general

```text
Cliente
  |
  | HTTP + JSON
  v
/api/v1/**
  |
  v
Spring Security
  |
Bearer JWT
  |
ApiPrincipal
  |
REST Controllers
  |
DTOs
  |
Services
  |
Repositories tenant-aware
  |
JPA / Hibernate
  |
PostgreSQL (producción) / H2 (desarrollo)
```

### Principios

- API versionada bajo `/api/v1/**`.
- Autenticación con Bearer JWT.
- API stateless: no usa `HttpSession` para identidad.
- El tenant se obtiene desde el usuario autenticado.
- Los clientes no deciden el `empresaId`.
- Los controllers trabajan con DTOs, no exponen entidades JPA directamente.
- Las reglas de negocio están en Services.
- Los repositories filtran por empresa para prevenir IDOR y fugas entre tenants.
- Los errores de API se centralizan mediante `ProblemDetail`.
- Las respuestas paginadas usan un DTO estable `PageResponse<T>`.

## 3. Autenticación y autorización

### Login

```http
POST /api/v1/auth/login
```

El usuario envía correo y contraseña. Si las credenciales son válidas, el backend genera un JWT.

El token se usa en las siguientes peticiones:

```http
Authorization: Bearer <token>
```

### Roles de acceso

Beta distingue los **roles de acceso** de los **roles de proceso**.

Roles de acceso:

- `ADMINISTRADOR`: administración completa y operaciones sensibles.
- `EDITOR`: consulta y modificación de recursos permitidos.
- `LECTOR`: consulta, sin modificación.

Roles de proceso:

- representan funciones del negocio;
- se asignan a lanes;
- no son identidades de usuarios.

## 4. Multitenencia

Toda entidad de negocio pertenece a una empresa.

El flujo de seguridad es:

```text
JWT
 ↓
ApiPrincipal
 ↓
empresaId
 ↓
Controller
 ↓
Service
 ↓
Repository tenant-aware
```

Esto evita que un usuario de la Empresa A consulte o modifique recursos de la Empresa B alterando IDs en la URL.

La suite incluye pruebas específicas de aislamiento multiempresa e IDOR.

## 5. Recursos principales

| Recurso | Base REST |
|---|---|
| Autenticación | `/api/v1/auth` |
| Empresas | `/api/v1/empresas` |
| Usuarios | `/api/v1/usuarios` |
| Procesos | `/api/v1/procesos` |
| Roles de proceso | `/api/v1/roles` |
| Pools | `/api/v1/procesos/{procesoId}/pools` |
| Lanes | `/api/v1/pools/{poolId}/lanes` |
| Actividades | `/api/v1/lanes/{laneId}/actividades` |
| Gateways | `/api/v1/lanes/{laneId}/gateways` |
| Arcos | `/api/v1/arcos` |
| Mensajes | `/api/v1/procesos/{procesoId}/mensajes` |
| Correlaciones | `/api/v1/mensajes/{mensajeId}/correlacion` |

## 6. Flujo típico de una petición

Ejemplo: crear una actividad.

```text
POST /api/v1/lanes/{laneId}/actividades
        |
        v
Spring Security valida JWT y rol
        |
        v
ActividadController
        |
        v
ActividadRequest
        |
        v
ActividadService
        |
        +--> valida tenant
        +--> valida lane
        +--> valida nombre
        |
        v
NodoFlujoRepository
        |
        v
ActividadResponse + 201 Created
```

## 7. Semántica HTTP

La API usa convenciones REST:

- `GET`: consultar.
- `POST`: crear.
- `PUT`: editar recursos completos.
- `PATCH`: cambios parciales o de estado.
- `DELETE`: eliminar/desactivar según el recurso.

Respuestas comunes:

- `200 OK`
- `201 Created` + `Location`
- `204 No Content`
- `400 Bad Request`
- `401 Unauthorized`
- `403 Forbidden`
- `404 Not Found`
- `409 Conflict`

## 8. Estado actual del dominio BPMN

Actualmente están implementados en el backend:

- empresas y usuarios;
- procesos e historial de procesos;
- roles de proceso;
- pools;
- lanes;
- actividades;
- gateways;
- arcos;
- mensajes entre pools;
- correlación básica de mensajes.

Todavía existen historias de usuario que requieren ampliar el dominio, especialmente:

- eventos BPMN;
- tipo de actividad;
- Message Throw y Message Catch como elementos BPMN explícitos;
- notificaciones externas;
- compartición de procesos entre empresas;
- trazabilidad completa de cambios de elementos BPMN;
- eliminación lógica uniforme para todos los elementos.

El detalle se documenta en [Historias de usuario y API REST](historias-usuario-rest.md).

## 9. Calidad

El proyecto utiliza:

- JUnit;
- pruebas de integración;
- pruebas de autorización por rol;
- pruebas de aislamiento multiempresa;
- ArchUnit;
- JaCoCo;
- GitHub Actions;
- Docker;
- OpenAPI / Swagger.

Swagger está disponible al ejecutar el backend en:

```text
/swagger-ui/index.html
```

## 10. Referencias

- [Repositorio Beta-back](https://github.com/Facimus-Curiositatem/Beta-back)
- [Historias de usuario oficiales](https://desarrolloweb.click/proyecto/hitoriasusuario/)
- [Historias de usuario y API REST](historias-usuario-rest.md)
