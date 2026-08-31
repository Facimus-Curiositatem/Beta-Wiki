# Controladores Thymeleaf — Entrega 1

Alcance: empresas, usuarios, procesos y roles de proceso con vistas
server-side. El modelado del diagrama (pools, lanes, nodos, arcos, mensajes)
se implementa en la Entrega 2 con Angular.

## Sesion

Login simple con `HttpSession`. Se guardan `empresaId`, `usuarioId` y
`rolAcceso`. Sin Spring Security (se agrega en Entrega 3).

## Rutas

### Sesion y empresa

| Metodo | Ruta | Vista | Que hace |
|--------|------|-------|----------|
| GET | /login | sesion/login | Formulario de login |
| POST | /login | redirect | Autentica, crea sesion, redirige a /procesos |
| GET | /logout | redirect | Invalida sesion, redirige a /login |
| GET | /empresas/registro | empresas/registro | Formulario de registro |
| POST | /empresas/registro | redirect | Crea empresa + admin, redirige a /login |

### Usuarios (solo admin)

| Metodo | Ruta | Vista | Que hace |
|--------|------|-------|----------|
| GET | /usuarios | usuarios/lista | Lista usuarios de la empresa |
| GET | /usuarios/nuevo | usuarios/formulario | Formulario de alta |
| POST | /usuarios | redirect | Crea usuario, redirige a /usuarios |
| GET | /usuarios/{id}/editar | usuarios/formulario | Formulario de edicion |
| POST | /usuarios/{id} | redirect | Actualiza usuario |
| POST | /usuarios/{id}/desactivar | redirect | Desactiva usuario |

### Procesos

| Metodo | Ruta | Vista | Que hace |
|--------|------|-------|----------|
| GET | /procesos | procesos/lista | Lista paginada con busqueda y filtros |
| GET | /procesos/nuevo | procesos/formulario | Formulario de creacion |
| POST | /procesos | redirect | Crea proceso |
| GET | /procesos/{id} | procesos/detalle | Detalle + historial de cambios |
| GET | /procesos/{id}/editar | procesos/formulario | Formulario de edicion |
| POST | /procesos/{id} | redirect | Actualiza proceso |
| POST | /procesos/{id}/publicar | redirect | Cambia estado a PUBLICADO |
| POST | /procesos/{id}/eliminar | redirect | Eliminacion logica |

### Roles de proceso (solo admin)

| Metodo | Ruta | Vista | Que hace |
|--------|------|-------|----------|
| GET | /roles | roles/lista | Lista roles de la empresa |
| GET | /roles/nuevo | roles/formulario | Formulario de creacion |
| POST | /roles | redirect | Crea rol |
| GET | /roles/{id}/editar | roles/formulario | Formulario de edicion |
| POST | /roles/{id} | redirect | Actualiza rol |
| POST | /roles/{id}/eliminar | redirect | Elimina rol (valida no en uso) |

**Total: 22 rutas.**

## Manejo de errores

Un `@ControllerAdvice` global captura `ReglaNegocioException` y la traduce a
un flash attribute que la vista muestra como alerta. Los errores de validacion
de formulario se manejan con `BindingResult`.

## Control de acceso (Entrega 1)

Verificacion manual en cada controlador a partir de la sesion:

```java
RolAcceso rol = (RolAcceso) session.getAttribute("rolAcceso");
if (rol != RolAcceso.ADMINISTRADOR) {
    return "redirect:/procesos";
}
```

En la Entrega 3, esto se reemplaza por `@PreAuthorize` de Spring Security.
