# ADR-004: Backend exclusivamente REST con autenticación JWT

## Estado
Aceptado. Reemplaza lo descrito en `diseno-controladores-thymeleaf.md`
(controladores MVC, vistas Thymeleaf y empresa activa en `HttpSession`).

## Contexto
La primera versión del backend se diseñó con controladores Thymeleaf que
renderizaban HTML y guardaban la empresa activa en la sesión HTTP. Para la
entrega de la API, el backend debe atender a cualquier cliente (Postman,
JMeter, un futuro frontend Angular) únicamente con HTTP + JSON.

Mantener la sesión del servidor obliga a guardar estado por usuario, complica
escalar a varias instancias y no encaja con un cliente SPA que consume la
API desde otro origen.

## Opciones consideradas

1. **Mantener Thymeleaf y agregar controladores REST en paralelo.**
   Dos capas web que mantener, dos mecanismos de autenticación (sesión y
   token) y el riesgo de duplicar lógica entre ambos tipos de controller.

2. **API REST con sesión HTTP (cookie `JSESSIONID`).**
   Reutiliza el mecanismo anterior, pero sigue siendo stateful, exige
   protección CSRF y complica el CORS con un frontend en otro origen.

3. **API REST stateless con Bearer JWT.**
   Cada petición lleva un token firmado con la identidad del usuario, su
   empresa y su rol. El servidor no guarda sesión.

## Decisión
Opción 3. El backend queda exclusivamente REST bajo `/api/v1/**`:

- Se eliminan Thymeleaf, los controladores MVC, las plantillas y `HttpSession`.
- `POST /api/v1/auth/login` valida credenciales (BCrypt) y devuelve un JWT
  firmado con HS256 que incluye los claims `usuarioId`, `empresaId` y `rol`.
  Expira por defecto a los 1800 s (`JWT_EXPIRATION_SECONDS`).
- `JwtAuthenticationFilter` valida el token en cada petición, comprueba que
  el usuario siga activo y construye un `ApiPrincipal`.
- **El `empresaId` sale siempre del `ApiPrincipal`, nunca del cuerpo ni de la
  URL.** Así se mantiene la regla de ADR-002 sin confiar en el cliente.
- `POST /api/v1/auth/logout` responde `204`. Como no hay estado en el
  servidor, cerrar sesión consiste en que el cliente descarte el token.
- Sesión `STATELESS` y CSRF desactivado (no hay cookies de sesión que proteger).
- Errores en formato `ProblemDetail` (RFC 9457), centralizados en
  `common/api/ApiExceptionHandler`.
- Autorización por rol de acceso en `security/SecurityConfig` (ver
  [seguridad.md](../seguridad.md)).

La capa de servicio y los repositorios se conservan sin cambios: la migración
solo reemplaza la capa web, lo que confirma la regla de ADR-001 de una capa de
servicio única.

## Consecuencias
- Toda petición protegida debe enviar `Authorization: Bearer <token>`.
  Sin token válido la respuesta es `401`; con un rol sin permiso, `403`.
- Un token emitido sigue siendo válido hasta que expira, aunque el usuario
  haga logout. Se mitiga con la expiración corta y con la comprobación de
  usuario activo en cada petición.
- Si `JWT_SECRET` no está definido, se genera una clave aleatoria al arrancar
  y los tokens se invalidan al reiniciar. En producción la variable es
  obligatoria.
- `diseno-controladores-thymeleaf.md` queda obsoleto; su reemplazo es
  [diseno-api-rest.md](../diseno-api-rest.md).
- Postman, JMeter y cualquier cliente deben guardar el token del login y
  enviarlo en cada petición.