# Diseño de controladores + vistas Thymeleaf (entrega 1)

Alcance de la entrega 1: **empresas, usuarios, procesos**. No incluye el
editor del diagrama (`modelado`) — eso llega con la capa REST/Angular de la
entrega 2, o como Thymeleaf adicional si se decide antes; no está en este
backlog todavía. Documento de diseño — no código. Servicios que consume:
[diseno-servicios-gestion.md](diseno-servicios-gestion.md).

## Cómo se resuelve la "empresa activa" antes de Spring Security

Spring Security entra en la entrega final. Para la entrega 1 no hay
autenticación robusta, pero **igual hay que respetar el aislamiento
multiempresa** (ADR-002) — un controller nunca puede tomar `empresaId` de un
parámetro de la URL o del formulario sin más. Decisión para esta entrega:
login propio simple (`UsuarioService.autenticar`) que, si es válido, guarda
`empresaId` + `usuarioId` + `rolAcceso` en `HttpSession`. Todo controller de
`usuarios`/`procesos` lee `empresaId` de la sesión, nunca de la petición.
Esto es provisional: en la entrega final se reemplaza por el contexto de
seguridad de Spring Security, pero el hábito (empresa desde el contexto de
autenticación, no desde el request) es el mismo y no debería requerir tocar
los services.

## Rutas propuestas

| Método | Ruta | Controller | Responsabilidad | Vista |
|---|---|---|---|---|
| `GET` | `/empresas/registro` | `EmpresaController` | Formulario de alta de empresa + datos del admin inicial | `empresas/registro.html` |
| `POST` | `/empresas/registro` | `EmpresaController` | `EmpresaService.registrarEmpresa(...)`, redirige a `/login` | — |
| `GET` | `/login` | `SesionController` | Formulario de login | `sesion/login.html` |
| `POST` | `/login` | `SesionController` | `UsuarioService.autenticar(...)`, guarda sesión, redirige a `/procesos` | — |
| `POST` | `/logout` | `SesionController` | Invalida `HttpSession`, redirige a `/login` | — |
| `GET` | `/usuarios` | `UsuarioController` | Lista colaboradores de la empresa en sesión (requiere `ADMINISTRADOR`) | `usuarios/lista.html` |
| `GET` | `/usuarios/nuevo` | `UsuarioController` | Formulario alta colaborador | `usuarios/formulario.html` |
| `POST` | `/usuarios` | `UsuarioController` | `UsuarioService.crearColaborador(...)` | — |
| `GET` | `/usuarios/{id}/editar` | `UsuarioController` | Formulario editar rol de acceso | `usuarios/formulario.html` |
| `POST` | `/usuarios/{id}` | `UsuarioController` | `UsuarioService.cambiarRolAcceso(...)` | — |
| `GET` | `/procesos` | `ProcesoController` | Lista procesos activos de la empresa, con filtro por nombre/estado | `procesos/lista.html` |
| `GET` | `/procesos/nuevo` | `ProcesoController` | Formulario crear proceso | `procesos/formulario.html` |
| `POST` | `/procesos` | `ProcesoController` | `ProcesoService.crear(...)`, redirige al detalle | — |
| `GET` | `/procesos/{id}` | `ProcesoController` | Detalle de proceso + su `HistorialCambio` (`HistorialCambioService.listarPorProceso`) | `procesos/detalle.html` |
| `GET` | `/procesos/{id}/editar` | `ProcesoController` | Formulario editar | `procesos/formulario.html` |
| `POST` | `/procesos/{id}` | `ProcesoController` | `ProcesoService.editar(...)` | — |
| `POST` | `/procesos/{id}/publicar` | `ProcesoController` | `ProcesoService.publicar(...)` | — |
| `POST` | `/procesos/{id}/eliminar` | `ProcesoController` | `ProcesoService.eliminarLogico(...)` (baja lógica, nunca DELETE) | — |

## Vistas y fragmentos

Reutiliza el patrón ya existente en `beta/src/main/resources/templates/`
(`fragments/header.html`, `fragments/footer.html`, `layout.html`): cada
vista nueva se inserta en el mismo layout, no se duplica cabecera/pie.

```text
templates/
├── fragments/           (header, footer — reutilizados del prototipo actual)
├── sesion/
│   └── login.html
├── empresas/
│   └── registro.html
├── usuarios/
│   ├── lista.html
│   └── formulario.html
└── procesos/
    ├── lista.html
    ├── formulario.html
    └── detalle.html
```

## Manejo de errores

Un `@ControllerAdvice` compartido (mencionado en
[ADR-003](adr/ADR-003-validacion-coherencia-diagrama.md)) captura
`ReglaDiagramaInvalidaException` y cualquier excepción de validación de
`gestion` (ej. NIT/email duplicado), y las traduce a un mensaje de error
mostrado en la misma vista de formulario (patrón "flash attribute" +
re-render), igual que ya hace el formulario de contacto del prototipo actual
con sus mensajes de validación.
