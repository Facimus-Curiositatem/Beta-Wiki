package com.wiki.beta.repository;

import java.util.Map;

/**
 * Contenido tecnico de la Wiki.
 *
 * Todo el HTML de esta clase es contenido estatico controlado por el proyecto.
 * La vista lo renderiza con th:utext para permitir titulos, listas y bloques de codigo.
 */
public final class WikiTechnicalContent {

    private WikiTechnicalContent() {
    }

    private static final Map<String, String> CONTENT = Map.ofEntries(
            Map.entry("api-rest-historias-usuario", apiRest()),
            Map.entry("seguridad-multitenencia", seguridad()),
            Map.entry("modelado-bpmn-mensajeria", modelado()),
            Map.entry("hu-01-registro-empresa", hu01()),
            Map.entry("hu-02-gestion-usuarios", hu02()),
            Map.entry("hu-03-inicio-sesion", hu03()),
            Map.entry("hu-04-crear-proceso", hu04()),
            Map.entry("hu-05-editar-proceso", hu05()),
            Map.entry("hu-06-eliminar-proceso", hu06()),
            Map.entry("hu-07-consultar-procesos", hu07()),
            Map.entry("hu-08-crear-actividad", hu08()),
            Map.entry("hu-09-editar-actividad", hu09()),
            Map.entry("hu-10-eliminar-actividad", hu10()),
            Map.entry("hu-11-crear-arco", hu11()),
            Map.entry("hu-12-editar-arco", hu12()),
            Map.entry("hu-13-eliminar-arco", hu13()),
            Map.entry("hu-14-crear-gateway", hu14()),
            Map.entry("hu-15-editar-gateway", hu15()),
            Map.entry("hu-16-eliminar-gateway", hu16()),
            Map.entry("hu-17-crear-rol", hu17()),
            Map.entry("hu-18-editar-rol", hu18()),
            Map.entry("hu-19-eliminar-rol", hu19()),
            Map.entry("hu-20-consultar-roles", hu20()),
            Map.entry("hu-21-configurar-pool", hu21()),
            Map.entry("hu-22-gestionar-lanes", hu22()),
            Map.entry("hu-23-compartir-procesos", hu23()),
            Map.entry("hu-24-permisos-estructura", hu24()),
            Map.entry("hu-25-message-throw", hu25()),
            Map.entry("hu-26-notificacion-externa", hu26()),
            Map.entry("hu-27-message-catch", hu27()),
            Map.entry("hu-28-correlacion", hu28())
    );

    public static String forSlug(String slug, String fallback) {
        return CONTENT.getOrDefault(slug, fallback);
    }

    private static String section(String title, String body) {
        return "<section class=\"doc-section\"><h2>" + title + "</h2>" + body + "</section>";
    }

    private static String endpoint(String method, String path, String description) {
        return "<div class=\"endpoint\"><span class=\"http-method\">" + method
                + "</span><code>" + path + "</code><p>" + description + "</p></div>";
    }

    private static String files(String... entries) {
        StringBuilder html = new StringBuilder("<ul class=\"file-list\">");
        for (String entry : entries) {
            html.append("<li>").append(entry).append("</li>");
        }
        return html.append("</ul>").toString();
    }

    private static String flow(String... steps) {
        StringBuilder html = new StringBuilder("<ol class=\"flow-list\">");
        for (String step : steps) {
            html.append("<li>").append(step).append("</li>");
        }
        return html.append("</ol>").toString();
    }

    private static String apiRest() {
        return """
            <p class="doc-intro">El backend de Beta se migró a una API REST real y versionada bajo <code>/api/v1</code>. La API no renderiza HTML ni usa sesiones de servidor: recibe JSON, aplica reglas de negocio y devuelve JSON con códigos HTTP coherentes.</p>
            """ +
            section("Arquitectura de una petición",
                flow(
                    "<strong>1. HTTP.</strong> El cliente invoca un endpoint de un <code>@RestController</code>.",
                    "<strong>2. Seguridad.</strong> <code>JwtAuthenticationFilter</code> lee <code>Authorization: Bearer ...</code>, valida el token con <code>JwtService</code> y construye un <code>ApiPrincipal</code>.",
                    "<strong>3. Tenant.</strong> Los controllers obtienen <code>empresaId</code> desde <code>ApiPrincipal</code>; no confiamos en un identificador de empresa enviado por el cliente.",
                    "<strong>4. DTO.</strong> El JSON entra por records como <code>ProcesoRequest</code>, <code>ActividadRequest</code> o <code>MensajeRequest</code>, validados con Jakarta Validation.",
                    "<strong>5. Service.</strong> La lógica de negocio vive en servicios como <code>ProcesoService</code>, <code>ActividadService</code> o <code>MensajeService</code>.",
                    "<strong>6. Repository.</strong> Los servicios consultan repositorios JPA filtrados por empresa para conservar el aislamiento multitenant.",
                    "<strong>7. Response.</strong> Las entidades se convierten a DTOs de salida y se responden con <code>ResponseEntity</code>."
                )) +
            section("Convenciones REST que usamos",
                "<ul><li><code>POST</code> crea recursos y responde <strong>201 Created</strong> con cabecera <code>Location</code>.</li>"
                + "<li><code>GET</code> consulta recursos y colecciones.</li>"
                + "<li><code>PUT</code> actualiza recursos.</li>"
                + "<li><code>PATCH</code> se usa cuando el cambio es parcial, por ejemplo el estado de un proceso.</li>"
                + "<li><code>DELETE</code> responde <strong>204 No Content</strong>; en los elementos que requieren trazabilidad se aplica baja lógica.</li>"
                + "<li>Las listas paginadas usan <code>PageResponse&lt;T&gt;</code> en lugar de exponer directamente <code>Page&lt;T&gt;</code>.</li></ul>") +
            section("Seguridad y autorización",
                "<p><code>SecurityConfig</code> configura la aplicación como <strong>stateless</strong>. Login, registro de empresa y aceptación de invitación son públicos; el resto exige JWT.</p>"
                + "<p>Los roles de acceso son <code>ADMINISTRADOR</code>, <code>EDITOR</code> y <code>SOLO_LECTURA</code>. Las operaciones destructivas y administrativas se restringen desde Spring Security y, para pools/lanes, también mediante <code>PermisoEstructuraService</code>.</p>") +
            section("Errores y validación",
                "<p><code>ApiExceptionHandler</code> centraliza los errores con <code>ProblemDetail</code>: 400 para JSON/parámetros inválidos, 401 para autenticación, 403 para permisos, 404 para recursos inexistentes y 409 para reglas de negocio.</p>") +
            section("Archivos base de la API REST",
                files(
                    "<code>security/SecurityConfig.java</code> — reglas de acceso y configuración stateless.",
                    "<code>security/JwtAuthenticationFilter.java</code> — autenticación Bearer por petición.",
                    "<code>security/JwtService.java</code> — generación y validación de JWT.",
                    "<code>security/ApiPrincipal.java</code> — usuario, empresa y rol autenticados.",
                    "<code>common/api/ApiExceptionHandler.java</code> — errores REST estandarizados.",
                    "<code>common/api/PageResponse.java</code> — contrato estable de paginación.",
                    "<code>gestion/controller/*Controller.java</code> — endpoints de gestión.",
                    "<code>modelado/controller/*Controller.java</code> — endpoints BPMN."
                )) +
            section("Ejemplo de recorrido",
                "<pre><code>POST /api/v1/procesos\nAuthorization: Bearer &lt;JWT&gt;\nContent-Type: application/json\n\n{\n  \"nombre\": \"Compras\",\n  \"descripcion\": \"Proceso de compras\",\n  \"categoria\": \"Operativo\"\n}\n\nProcesoController -> ProcesoService -> ProcesoRepository / PoolRepository\n                  -> HistorialCambioService -> ProcesoResponse (201)</code></pre>");
    }

    private static String seguridad() {
        return "<p class=\"doc-intro\">La seguridad combina autenticación JWT, autorización por rol y aislamiento por empresa.</p>"
            + section("Cómo funciona",
                flow(
                    "<code>AuthController</code> valida email/contraseña usando <code>UsuarioService</code>.",
                    "<code>JwtService</code> genera un token con <code>usuarioId</code>, <code>empresaId</code> y <code>rol</code>.",
                    "<code>JwtAuthenticationFilter</code> valida el token en cada petición y confirma que el usuario siga activo.",
                    "<code>ApiPrincipal</code> se inyecta en los controllers y aporta el tenant real.",
                    "<code>SecurityConfig</code> decide qué roles pueden usar cada ruta."
                ))
            + section("Regla multitenant",
                "<p>Los services y repositorios consultan con <code>empresaId</code>. De esta manera, aunque un cliente conozca el ID numérico de un recurso de otra empresa, no puede resolverlo dentro de su tenant.</p>")
            + section("Archivos", files(
                "<code>SecurityConfig.java</code>",
                "<code>JwtAuthenticationFilter.java</code>",
                "<code>JwtService.java</code>",
                "<code>ApiPrincipal.java</code>",
                "<code>UsuarioService.java</code>",
                "<code>PermisoEstructuraService.java</code>"
            ));
    }

    private static String modelado() {
        return "<p class=\"doc-intro\">El modelado BPMN se implementa con una jerarquía de nodos y servicios REST separados por recurso.</p>"
            + section("Modelo principal",
                "<p><code>NodoFlujo</code> es la base de <code>Actividad</code>, <code>Gateway</code> y <code>EventoMensaje</code>. Los nodos pertenecen a una <code>Lane</code>; las lanes pertenecen a un <code>Pool</code>; los arcos conectan nodos dentro del mismo pool.</p>")
            + section("Reglas antes de publicar",
                "<p><code>ValidacionModeloService</code> valida pools de caja negra, gateways divergentes, condiciones, Message Catch de inicio, Message Throw, receptores y correlación. La publicación solo continúa si el modelo es coherente.</p>")
            + section("Trazabilidad",
                "<p><code>AuditoriaModeladoService</code> registra los cambios BPMN en el historial del proceso usando el usuario autenticado.</p>")
            + section("Archivos", files(
                "<code>modelado/model/NodoFlujo.java</code>",
                "<code>modelado/model/Actividad.java</code>",
                "<code>modelado/model/Gateway.java</code>",
                "<code>modelado/model/EventoMensaje.java</code>",
                "<code>modelado/model/Arco.java</code>",
                "<code>modelado/model/Mensaje.java</code>",
                "<code>modelado/service/ValidacionModeloService.java</code>",
                "<code>modelado/service/AuditoriaModeladoService.java</code>"
            ));
    }

    private static String hu(String objective, String endpoints, String flowHtml, String filesHtml, String rules) {
        return "<p class=\"doc-intro\">" + objective + "</p>"
            + section("Contrato REST", endpoints)
            + section("Cómo lo resolvemos en código", flowHtml)
            + section("Archivos principales", filesHtml)
            + section("Reglas y validaciones", "<p>" + rules + "</p>");
    }

    private static String hu01() {
        return hu(
            "Permite registrar una empresa nueva junto con su administrador inicial.",
            endpoint("POST", "/api/v1/empresas", "Recibe RegistroEmpresaRequest y responde 201 con EmpresaResponse."),
            flow(
                "<code>EmpresaController.registrar()</code> recibe y valida el JSON.",
                "<code>EmpresaService.registrar()</code> comprueba el NIT, crea <code>Empresa</code> y el primer <code>Usuario</code> administrador.",
                "<code>PasswordEncoder</code> aplica BCrypt a la contraseña antes de persistir.",
                "<code>EmpresaRepository</code> y <code>UsuarioRepository</code> guardan la información."
            ),
            files(
                "<code>gestion/controller/EmpresaController.java</code>",
                "<code>gestion/controller/dto/RegistroEmpresaRequest.java</code>",
                "<code>gestion/controller/dto/EmpresaResponse.java</code>",
                "<code>gestion/service/EmpresaService.java</code>",
                "<code>gestion/repository/EmpresaRepository.java</code>",
                "<code>gestion/repository/UsuarioRepository.java</code>",
                "<code>gestion/model/Empresa.java</code>",
                "<code>gestion/model/Usuario.java</code>"
            ),
            "El NIT debe ser único. El endpoint es público para permitir el alta inicial. El administrador se crea dentro de la misma transacción de registro y su contraseña nunca se almacena en texto plano."
        );
    }

    private static String hu02() {
        return hu(
            "Administra colaboradores de una empresa e incorpora usuarios mediante creación directa o invitación.",
            endpoint("GET/POST/PATCH/DELETE", "/api/v1/usuarios/**", "CRUD administrativo de usuarios.")
                + endpoint("POST", "/api/v1/usuarios/invitaciones", "Genera una invitación temporal.")
                + endpoint("POST", "/api/v1/usuarios/invitaciones/{token}/aceptar", "Acepta la invitación y crea el colaborador."),
            flow(
                "<code>UsuarioController</code> resuelve las operaciones administrativas.",
                "<code>UsuarioService</code> crea, actualiza, lista y desactiva usuarios siempre dentro del tenant del principal.",
                "<code>InvitacionUsuarioController</code> delega en <code>InvitacionUsuarioService</code> el flujo por token.",
                "Al aceptar la invitación se reutiliza <code>UsuarioService.crearColaborador()</code>, evitando duplicar reglas."
            ),
            files(
                "<code>gestion/controller/UsuarioController.java</code>",
                "<code>gestion/controller/InvitacionUsuarioController.java</code>",
                "<code>gestion/service/UsuarioService.java</code>",
                "<code>gestion/service/InvitacionUsuarioService.java</code>",
                "<code>gestion/repository/UsuarioRepository.java</code>",
                "<code>gestion/repository/InvitacionUsuarioRepository.java</code>",
                "<code>gestion/model/InvitacionUsuario.java</code>",
                "<code>gestion/controller/dto/CrearUsuarioRequest.java</code>",
                "<code>gestion/controller/dto/AceptarInvitacionRequest.java</code>"
            ),
            "Solo ADMINISTRADOR gestiona usuarios. Se evita email duplicado, las invitaciones expiran a las 48 horas y una invitación usada no puede reutilizarse. Mientras no exista SMTP, el token se entrega al administrador para compartirlo manualmente."
        );
    }

    private static String hu03() {
        return hu(
            "Autentica usuarios y protege la API con Bearer JWT sin mantener HttpSession.",
            endpoint("POST", "/api/v1/auth/login", "Valida credenciales y devuelve LoginResponse con JWT.")
                + endpoint("POST", "/api/v1/auth/logout", "Cierra la sesión de forma stateless: el cliente descarta el token."),
            flow(
                "<code>AuthController.login()</code> llama <code>UsuarioService.autenticar()</code>.",
                "Las contraseñas se comparan mediante BCrypt.",
                "<code>JwtService.generarToken()</code> incluye usuarioId, empresaId y rol.",
                "<code>JwtAuthenticationFilter</code> valida cada Bearer token y reconstruye <code>ApiPrincipal</code>.",
                "<code>SecurityConfig</code> aplica autorización por rol."
            ),
            files(
                "<code>gestion/controller/AuthController.java</code>",
                "<code>gestion/service/UsuarioService.java</code>",
                "<code>security/JwtService.java</code>",
                "<code>security/JwtAuthenticationFilter.java</code>",
                "<code>security/ApiPrincipal.java</code>",
                "<code>security/SecurityConfig.java</code>",
                "<code>gestion/controller/dto/LoginRequest.java</code>",
                "<code>gestion/controller/dto/LoginResponse.java</code>"
            ),
            "Las credenciales inválidas responden 401 de forma genérica. El usuario del token debe seguir activo. El tenant y el rol efectivos se obtienen del principal autenticado."
        );
    }

    private static String hu04() {
        return hu(
            "Crea un proceso nuevo y deja preparada su estructura base para comenzar a modelar.",
            endpoint("POST", "/api/v1/procesos", "Crea un proceso en estado BORRADOR y responde 201."),
            flow(
                "<code>ProcesoController.crear()</code> toma empresaId y usuarioId desde <code>ApiPrincipal</code>.",
                "<code>ProcesoService.crear()</code> valida nombre único, crea <code>Proceso</code> y lo asocia a la empresa.",
                "El mismo servicio crea un <code>Pool</code> inicial de tipo EMPRESA con el nombre de la empresa.",
                "<code>HistorialCambioService</code> registra «Proceso creado»."
            ),
            files(
                "<code>gestion/controller/ProcesoController.java</code>",
                "<code>gestion/service/ProcesoService.java</code>",
                "<code>gestion/repository/ProcesoRepository.java</code>",
                "<code>modelado/repository/PoolRepository.java</code>",
                "<code>gestion/model/Proceso.java</code>",
                "<code>modelado/model/Pool.java</code>",
                "<code>gestion/controller/dto/ProcesoRequest.java</code>",
                "<code>gestion/controller/dto/ProcesoResponse.java</code>"
            ),
            "El nombre de un proceso activo debe ser único dentro de la empresa. Todo proceso nuevo inicia activo y en BORRADOR."
        );
    }

    private static String hu05() {
        return hu(
            "Permite modificar los datos del proceso y controlar sus cambios de estado con trazabilidad.",
            endpoint("PUT", "/api/v1/procesos/{id}", "Edita nombre, descripción y categoría.")
                + endpoint("PATCH", "/api/v1/procesos/{id}", "Cambia el estado mediante CambiarEstadoProcesoRequest."),
            flow(
                "<code>ProcesoController</code> separa edición de metadatos y cambio de estado.",
                "<code>ProcesoService.editarDatos()</code> actualiza los campos y fecha de modificación.",
                "<code>ProcesoService.cambiarEstado()</code> ejecuta las reglas de transición.",
                "Cuando el nuevo estado es PUBLICADO se ejecuta <code>ValidacionModeloService.validarParaPublicacion()</code>.",
                "Cada cambio se registra con <code>HistorialCambioService</code>."
            ),
            files(
                "<code>gestion/controller/ProcesoController.java</code>",
                "<code>gestion/service/ProcesoService.java</code>",
                "<code>modelado/service/ValidacionModeloService.java</code>",
                "<code>gestion/service/HistorialCambioService.java</code>",
                "<code>gestion/controller/dto/EditarProcesoRequest.java</code>",
                "<code>gestion/controller/dto/CambiarEstadoProcesoRequest.java</code>"
            ),
            "No se permite volver de PUBLICADO a BORRADOR. Antes de publicar se valida coherencia BPMN completa."
        );
    }

    private static String hu06() {
        return hu(
            "Retira un proceso de las consultas normales sin perder su información histórica.",
            endpoint("DELETE", "/api/v1/procesos/{id}", "Realiza una baja lógica y responde 204."),
            flow(
                "<code>ProcesoController.eliminar()</code> identifica usuario y empresa desde el JWT.",
                "<code>ProcesoService.eliminarLogico()</code> cambia <code>activo=false</code> y actualiza fechaModificacion.",
                "<code>ProcesoRepository</code> persiste el cambio.",
                "<code>HistorialCambioService</code> registra «Proceso eliminado (baja lógica)»."
            ),
            files(
                "<code>gestion/controller/ProcesoController.java</code>",
                "<code>gestion/service/ProcesoService.java</code>",
                "<code>gestion/repository/ProcesoRepository.java</code>",
                "<code>gestion/model/Proceso.java</code>",
                "<code>gestion/service/HistorialCambioService.java</code>"
            ),
            "La operación destructiva está restringida a ADMINISTRADOR. La consulta normal de detalle usa findByIdAndEmpresaIdAndActivoTrue."
        );
    }

    private static String hu07() {
        return hu(
            "Consulta procesos con filtros, paginación, detalle, historial y una vista completa del diagrama.",
            endpoint("GET", "/api/v1/procesos", "Lista paginada con filtros nombre, estado, categoría y activo.")
                + endpoint("GET", "/api/v1/procesos/{id}", "Detalle del proceso e historial.")
                + endpoint("GET", "/api/v1/procesos/{id}/diagrama", "Devuelve pools, lanes, nodos, arcos y mensajes.")
                + endpoint("GET", "/api/v1/procesos/{id}/historial", "Devuelve la trazabilidad del proceso."),
            flow(
                "<code>ProcesoService.buscar()</code> construye filtros con <code>ProcesoSpecifications</code>.",
                "<code>PageResponse</code> estabiliza el contrato de paginación.",
                "<code>ProcesoDiagramaService</code> carga pools, lanes, nodos, arcos y mensajes por lotes.",
                "El controller transforma cada entidad a su DTO Response antes de devolver JSON."
            ),
            files(
                "<code>gestion/controller/ProcesoController.java</code>",
                "<code>gestion/service/ProcesoService.java</code>",
                "<code>gestion/repository/ProcesoSpecifications.java</code>",
                "<code>gestion/service/ProcesoDiagramaService.java</code>",
                "<code>gestion/service/dto/ProcesoDiagrama.java</code>",
                "<code>gestion/controller/dto/ProcesoDiagramaResponse.java</code>",
                "<code>common/api/PageResponse.java</code>"
            ),
            "Todas las consultas están limitadas al tenant autenticado. El diagrama excluye elementos dados de baja lógica."
        );
    }

    private static String hu08() {
        return hu(
            "Crea actividades BPMN dentro de una lane y las clasifica por tipo.",
            endpoint("POST", "/api/v1/lanes/{laneId}/actividades", "Crea una Actividad y responde 201."),
            flow(
                "<code>ActividadController.crear()</code> recibe <code>ActividadRequest</code>.",
                "Si no llega tipo, se conserva compatibilidad usando la variante legacy de <code>ActividadService.crear()</code>.",
                "<code>ActividadService</code> valida lane, pool, nombre único y crea el nodo.",
                "<code>NodoFlujoRepository</code> persiste porque Actividad hereda de NodoFlujo.",
                "<code>AuditoriaModeladoService</code> registra el cambio."
            ),
            files(
                "<code>modelado/controller/ActividadController.java</code>",
                "<code>modelado/service/ActividadService.java</code>",
                "<code>modelado/model/Actividad.java</code>",
                "<code>modelado/model/TipoActividad.java</code>",
                "<code>modelado/repository/NodoFlujoRepository.java</code>",
                "<code>modelado/repository/LaneRepository.java</code>",
                "<code>modelado/controller/dto/ActividadRequest.java</code>",
                "<code>modelado/controller/dto/ActividadResponse.java</code>"
            ),
            "No se crean nodos internos en pools de caja negra y el nombre del nodo debe ser único en el proceso."
        );
    }

    private static String hu09() {
        return hu(
            "Edita una actividad existente, incluyendo su tipo, posición y lane responsable.",
            endpoint("PUT", "/api/v1/actividades/{id}", "Actualiza una actividad y devuelve ActividadResponse."),
            flow(
                "<code>ActividadController.editar()</code> selecciona la firma compatible según venga o no tipoActividad.",
                "<code>ActividadService.editar()</code> obtiene el nodo por empresa, valida el nuevo nombre y la lane destino.",
                "La entidad conserva sus arcos porque se modifica el nodo existente, no se reemplaza por otro.",
                "La modificación queda registrada con <code>AuditoriaModeladoService</code>."
            ),
            files(
                "<code>modelado/controller/ActividadController.java</code>",
                "<code>modelado/service/ActividadService.java</code>",
                "<code>modelado/repository/NodoFlujoRepository.java</code>",
                "<code>modelado/repository/LaneRepository.java</code>",
                "<code>modelado/model/Actividad.java</code>",
                "<code>modelado/controller/dto/ActividadRequest.java</code>"
            ),
            "La actividad y la lane deben pertenecer a la misma empresa/proceso. Los nombres duplicados son rechazados."
        );
    }

    private static String hu10() {
        return hu(
            "Evalúa el efecto de eliminar una actividad y luego la da de baja sin romper físicamente la trazabilidad.",
            endpoint("GET", "/api/v1/actividades/{id}/impacto-eliminacion", "Informa advertencias de continuidad.")
                + endpoint("DELETE", "/api/v1/actividades/{id}", "Desactiva actividad y arcos asociados."),
            flow(
                "<code>ActividadService.evaluarImpactoEliminacion()</code> inspecciona arcos entrantes y salientes.",
                "<code>ImpactoEliminacion</code> encapsula si se rompe continuidad y la lista de advertencias.",
                "<code>ActividadService.eliminar()</code> pone el nodo en <code>activo=false</code> y desactiva sus arcos.",
                "<code>AuditoriaModeladoService</code> registra la baja."
            ),
            files(
                "<code>modelado/service/ActividadService.java</code>",
                "<code>modelado/service/dto/ImpactoEliminacion.java</code>",
                "<code>modelado/controller/dto/ImpactoEliminacionResponse.java</code>",
                "<code>modelado/repository/ArcoRepository.java</code>",
                "<code>modelado/repository/NodoFlujoRepository.java</code>"
            ),
            "Solo se consideran elementos activos al calcular impacto. DELETE está restringido a ADMINISTRADOR."
        );
    }

    private static String hu11() {
        return hu(
            "Conecta dos nodos del flujo mediante un arco BPMN dentro del mismo pool.",
            endpoint("POST", "/api/v1/arcos", "Crea el arco con origen, destino, etiqueta y condición."),
            flow(
                "<code>ArcoController.crear()</code> recibe <code>ArcoRequest</code>.",
                "<code>ArcoService.crear()</code> resuelve ambos nodos con <code>NodoFlujoRepository</code>.",
                "El servicio comprueba pool, duplicados y reglas del nodo destino/origen.",
                "<code>ArcoRepository</code> persiste el arco y <code>AuditoriaModeladoService</code> registra el cambio."
            ),
            files(
                "<code>modelado/controller/ArcoController.java</code>",
                "<code>modelado/service/ArcoService.java</code>",
                "<code>modelado/repository/ArcoRepository.java</code>",
                "<code>modelado/repository/NodoFlujoRepository.java</code>",
                "<code>modelado/model/Arco.java</code>",
                "<code>modelado/controller/dto/ArcoRequest.java</code>"
            ),
            "Origen y destino deben ser distintos, activos y pertenecer al mismo pool. No se permiten duplicados activos. Un CATCH_INICIO no puede recibir arco entrante."
        );
    }

    private static String hu12() {
        return hu(
            "Permite modificar la información del arco y, cuando aplica, reasignar origen/destino.",
            endpoint("PUT", "/api/v1/arcos/{id}", "Actualiza etiqueta, condición y referencias del arco."),
            flow(
                "<code>ArcoController.editar()</code> recibe <code>EditarArcoRequest</code>.",
                "<code>ArcoService.editar()</code> vuelve a resolver nodos y repetir las mismas reglas de coherencia usadas al crear.",
                "Se modifica la misma entidad para conservar identidad y trazabilidad."
            ),
            files(
                "<code>modelado/controller/ArcoController.java</code>",
                "<code>modelado/service/ArcoService.java</code>",
                "<code>modelado/controller/dto/EditarArcoRequest.java</code>",
                "<code>modelado/repository/ArcoRepository.java</code>",
                "<code>modelado/repository/NodoFlujoRepository.java</code>"
            ),
            "Las condiciones son obligatorias cuando corresponden a salidas de gateways exclusivos/inclusivos. Los cambios se limitan al tenant autenticado."
        );
    }

    private static String hu13() {
        return hu(
            "Permite anticipar el impacto y eliminar lógicamente un arco.",
            endpoint("GET", "/api/v1/arcos/{id}/impacto-eliminacion", "Detecta nodos que quedarían desconectados.")
                + endpoint("DELETE", "/api/v1/arcos/{id}", "Marca el arco como inactivo."),
            flow(
                "<code>ArcoService.evaluarImpactoEliminacion()</code> busca entradas/salidas alternativas.",
                "<code>ArcoService.eliminar()</code> cambia <code>activo=false</code>.",
                "La auditoría registra origen y destino del arco eliminado."
            ),
            files(
                "<code>modelado/controller/ArcoController.java</code>",
                "<code>modelado/service/ArcoService.java</code>",
                "<code>modelado/service/dto/ImpactoEliminacion.java</code>",
                "<code>modelado/repository/ArcoRepository.java</code>"
            ),
            "Los arcos inactivos se ignoran en las validaciones posteriores del modelo."
        );
    }

    private static String hu14() {
        return hu(
            "Crea gateways BPMN para representar decisiones, paralelismo o inclusión.",
            endpoint("POST", "/api/v1/lanes/{laneId}/gateways", "Crea un gateway EXCLUSIVO, PARALELO o INCLUSIVO."),
            flow(
                "<code>GatewayController.crear()</code> recibe <code>GatewayRequest</code>.",
                "<code>GatewayService.crear()</code> valida la lane, pool y nombre.",
                "<code>NodoFlujoRepository</code> persiste el gateway como nodo de flujo.",
                "<code>AuditoriaModeladoService</code> registra la creación."
            ),
            files(
                "<code>modelado/controller/GatewayController.java</code>",
                "<code>modelado/service/GatewayService.java</code>",
                "<code>modelado/model/Gateway.java</code>",
                "<code>modelado/model/TipoGateway.java</code>",
                "<code>modelado/controller/dto/GatewayRequest.java</code>",
                "<code>modelado/repository/NodoFlujoRepository.java</code>"
            ),
            "No se crean gateways dentro de pools de caja negra. El nombre debe ser único dentro del proceso."
        );
    }

    private static String hu15() {
        return hu(
            "Edita un gateway y mantiene coherentes las condiciones de sus arcos al cambiar de tipo.",
            endpoint("PUT", "/api/v1/gateways/{id}", "Actualiza nombre, tipo y posición."),
            flow(
                "<code>GatewayService.editar()</code> obtiene el gateway activo.",
                "Al cambiar a PARALELO se limpian condiciones incompatibles de los arcos salientes activos.",
                "Al cambiar desde PARALELO a EXCLUSIVO/INCLUSIVO se comprueba que los arcos activos tengan condiciones.",
                "La edición se persiste y audita."
            ),
            files(
                "<code>modelado/controller/GatewayController.java</code>",
                "<code>modelado/service/GatewayService.java</code>",
                "<code>modelado/repository/ArcoRepository.java</code>",
                "<code>modelado/repository/NodoFlujoRepository.java</code>",
                "<code>modelado/model/TipoGateway.java</code>"
            ),
            "Los arcos eliminados lógicamente no participan en estas validaciones; solo se consideran los activos."
        );
    }

    private static String hu16() {
        return hu(
            "Evalúa el efecto de retirar un gateway y luego aplica su baja lógica junto con conexiones relacionadas.",
            endpoint("GET", "/api/v1/gateways/{id}/impacto-eliminacion", "Informa si se afecta la ramificación.")
                + endpoint("DELETE", "/api/v1/gateways/{id}", "Desactiva gateway y arcos conectados."),
            flow(
                "<code>GatewayService.evaluarImpactoEliminacion()</code> inspecciona entradas y salidas activas.",
                "<code>GatewayService.eliminar()</code> pone el nodo y sus arcos en inactivo.",
                "La operación se registra mediante <code>AuditoriaModeladoService</code>."
            ),
            files(
                "<code>modelado/controller/GatewayController.java</code>",
                "<code>modelado/service/GatewayService.java</code>",
                "<code>modelado/repository/NodoFlujoRepository.java</code>",
                "<code>modelado/repository/ArcoRepository.java</code>",
                "<code>modelado/service/dto/ImpactoEliminacion.java</code>"
            ),
            "DELETE está restringido a ADMINISTRADOR y no elimina físicamente la evidencia histórica."
        );
    }

    private static String hu17() {
        return hu(
            "Crea roles de proceso reutilizables por las lanes de una empresa.",
            endpoint("POST", "/api/v1/roles", "Crea un RolProceso y responde 201."),
            flow(
                "<code>RolProcesoController.crear()</code> recibe <code>RolProcesoRequest</code>.",
                "<code>RolProcesoService.crear()</code> valida nombre único y resuelve la empresa.",
                "<code>RolProcesoRepository</code> persiste el nuevo rol."
            ),
            files(
                "<code>gestion/controller/RolProcesoController.java</code>",
                "<code>gestion/service/RolProcesoService.java</code>",
                "<code>gestion/repository/RolProcesoRepository.java</code>",
                "<code>gestion/model/RolProceso.java</code>",
                "<code>gestion/controller/dto/RolProcesoRequest.java</code>"
            ),
            "Solo ADMINISTRADOR puede crear roles. El nombre debe ser único entre roles activos de la empresa."
        );
    }

    private static String hu18() {
        return hu(
            "Edita nombre y descripción de un rol sin romper las lanes que ya lo referencian.",
            endpoint("PUT", "/api/v1/roles/{id}", "Actualiza el rol y devuelve su información de uso."),
            flow(
                "<code>RolProcesoService.editar()</code> carga el rol activo del tenant.",
                "Valida que el nuevo nombre no choque con otro rol activo.",
                "Como las lanes referencian la misma entidad, el cambio se refleja sin reasignaciones.",
                "Si el rol está utilizado, el servicio registra historial en los procesos afectados."
            ),
            files(
                "<code>gestion/service/RolProcesoService.java</code>",
                "<code>gestion/repository/RolProcesoRepository.java</code>",
                "<code>modelado/repository/LaneRepository.java</code>",
                "<code>gestion/service/HistorialCambioService.java</code>"
            ),
            "La edición está reservada a ADMINISTRADOR y limitada a la empresa autenticada."
        );
    }

    private static String hu19() {
        return hu(
            "Evita eliminar un rol que todavía está siendo usado por lanes y aplica baja lógica cuando es seguro.",
            endpoint("DELETE", "/api/v1/roles/{id}", "Desactiva el rol si no tiene usos."),
            flow(
                "<code>RolProcesoService.eliminar()</code> obtiene el rol.",
                "<code>LaneRepository</code> calcula si existen lanes asociadas.",
                "Si hay usos se lanza <code>ReglaNegocioException</code> indicando que debe reasignarse primero.",
                "Si no hay usos, el rol queda inactivo."
            ),
            files(
                "<code>gestion/controller/RolProcesoController.java</code>",
                "<code>gestion/service/RolProcesoService.java</code>",
                "<code>modelado/repository/LaneRepository.java</code>",
                "<code>gestion/model/RolProceso.java</code>"
            ),
            "La integridad referencial funcional tiene prioridad: no se permite dejar lanes apuntando a un rol eliminado."
        );
    }

    private static String hu20() {
        return hu(
            "Consulta roles, su cantidad de usos y los procesos en los que participan.",
            endpoint("GET", "/api/v1/roles", "Lista roles con contador de usos.")
                + endpoint("GET", "/api/v1/roles/consulta", "Busca por nombre con paginación y lista procesos asociados.")
                + endpoint("GET", "/api/v1/roles/{id}", "Devuelve un rol y su uso actual."),
            flow(
                "<code>RolProcesoService.listarConUso()</code> combina rol + conteo de lanes.",
                "<code>buscarConProcesos()</code> usa Pageable en <code>RolProcesoRepository</code>.",
                "<code>LaneRepository</code> permite derivar los nombres de los procesos donde aparece el rol.",
                "<code>RolProcesoConsultaResponse</code> expone esos datos al cliente."
            ),
            files(
                "<code>gestion/controller/RolProcesoController.java</code>",
                "<code>gestion/service/RolProcesoService.java</code>",
                "<code>gestion/repository/RolProcesoRepository.java</code>",
                "<code>modelado/repository/LaneRepository.java</code>",
                "<code>gestion/service/dto/RolProcesoConsulta.java</code>",
                "<code>gestion/controller/dto/RolProcesoConsultaResponse.java</code>"
            ),
            "La paginación ocurre en base de datos, no con subList en memoria."
        );
    }

    private static String hu21() {
        return hu(
            "Administra pools de un proceso y diferencia participantes internos, externos y cajas negras.",
            endpoint("GET/POST", "/api/v1/procesos/{procesoId}/pools", "Lista o crea pools.")
                + endpoint("GET/PUT/DELETE", "/api/v1/pools/{id}", "Consulta, edita o elimina un pool."),
            flow(
                "<code>PoolController</code> coordina la API.",
                "Antes de escribir, <code>PermisoEstructuraService</code> valida la operación según rol.",
                "<code>PoolService</code> valida proceso, participante y reglas de caja negra.",
                "<code>PoolRepository</code> persiste la estructura y <code>AuditoriaModeladoService</code> registra cambios."
            ),
            files(
                "<code>modelado/controller/PoolController.java</code>",
                "<code>modelado/service/PoolService.java</code>",
                "<code>modelado/repository/PoolRepository.java</code>",
                "<code>modelado/model/Pool.java</code>",
                "<code>modelado/model/TipoParticipante.java</code>",
                "<code>gestion/service/PermisoEstructuraService.java</code>",
                "<code>modelado/controller/dto/PoolRequest.java</code>",
                "<code>modelado/controller/dto/EditarPoolRequest.java</code>"
            ),
            "Un pool de caja negra no puede contener elementos internos. La eliminación se bloquea si sus lanes contienen nodos activos."
        );
    }

    private static String hu22() {
        return hu(
            "Gestiona lanes dentro de pools, su rol responsable y su orden visual.",
            endpoint("GET/POST", "/api/v1/pools/{poolId}/lanes", "Lista o crea lanes.")
                + endpoint("GET/PUT/DELETE", "/api/v1/lanes/{id}", "Consulta, edita o elimina lanes.")
                + endpoint("PATCH", "/api/v1/pools/{poolId}/lanes/orden", "Reordena lanes mediante una lista de IDs."),
            flow(
                "<code>LaneController</code> valida permisos estructurales.",
                "<code>LaneService</code> resuelve pool y <code>RolProceso</code> del tenant.",
                "<code>reordenar()</code> verifica que la lista corresponda al pool y actualiza el campo orden.",
                "<code>LaneRepository</code> persiste la estructura."
            ),
            files(
                "<code>modelado/controller/LaneController.java</code>",
                "<code>modelado/service/LaneService.java</code>",
                "<code>modelado/repository/LaneRepository.java</code>",
                "<code>modelado/repository/PoolRepository.java</code>",
                "<code>gestion/repository/RolProcesoRepository.java</code>",
                "<code>modelado/controller/dto/ReordenarLanesRequest.java</code>"
            ),
            "No se crean lanes dentro de pools de caja negra. No se elimina una lane con nodos activos; primero deben reasignarse."
        );
    }

    private static String hu23() {
        return hu(
            "Comparte un proceso entre empresas sin transferir su propiedad y en modo solo lectura.",
            endpoint("POST", "/api/v1/procesos/{procesoId}/compartidos", "Concede acceso a otra empresa.")
                + endpoint("DELETE", "/api/v1/procesos/{procesoId}/compartidos/{empresaInvitadaId}", "Revoca el acceso.")
                + endpoint("GET", "/api/v1/procesos-compartidos", "Lista procesos recibidos.")
                + endpoint("GET", "/api/v1/procesos-compartidos/{procesoId}", "Devuelve el detalle completo compartido."),
            flow(
                "<code>ProcesoCompartidoController</code> expone las operaciones.",
                "<code>ProcesoCompartidoService.compartir()</code> valida propietario, invitada y evita auto-compartir.",
                "<code>ProcesoCompartidoRepository</code> conserva la relación y permite reactivar una compartición revocada.",
                "<code>obtenerCompartido()</code> arma una vista de solo lectura cargando pools, lanes, nodos, arcos y mensajes."
            ),
            files(
                "<code>gestion/controller/ProcesoCompartidoController.java</code>",
                "<code>gestion/service/ProcesoCompartidoService.java</code>",
                "<code>gestion/repository/ProcesoCompartidoRepository.java</code>",
                "<code>gestion/model/ProcesoCompartido.java</code>",
                "<code>gestion/service/dto/ProcesoCompartidoDetalle.java</code>",
                "<code>gestion/controller/dto/ProcesoCompartidoDetalleResponse.java</code>"
            ),
            "Solo ADMINISTRADOR comparte o revoca. La empresa invitada recibe únicamente lectura y la propiedad/tenant original del proceso no cambia."
        );
    }

    private static String hu24() {
        return hu(
            "Permite configurar qué roles de acceso pueden modificar la estructura de pools y lanes.",
            endpoint("GET", "/api/v1/permisos-estructura", "Consulta permisos por rol.")
                + endpoint("PUT", "/api/v1/permisos-estructura/{rol}", "Actualiza permisos de estructura."),
            flow(
                "<code>PermisoEstructuraController</code> solo está disponible para ADMINISTRADOR.",
                "<code>PermisoEstructuraService</code> obtiene configuración persistida o defaults.",
                "<code>PoolController</code> y <code>LaneController</code> llaman <code>validar()</code> antes de cada escritura.",
                "<code>OperacionEstructura</code> representa crear/editar/eliminar pool o lane."
            ),
            files(
                "<code>gestion/controller/PermisoEstructuraController.java</code>",
                "<code>gestion/service/PermisoEstructuraService.java</code>",
                "<code>gestion/service/OperacionEstructura.java</code>",
                "<code>gestion/model/PermisoEstructura.java</code>",
                "<code>gestion/repository/PermisoEstructuraRepository.java</code>",
                "<code>modelado/controller/PoolController.java</code>",
                "<code>modelado/controller/LaneController.java</code>"
            ),
            "SOLO_LECTURA nunca puede recibir permisos de escritura. Los permisos son por empresa y por rol."
        );
    }

    private static String hu25() {
        return hu(
            "Modela el envío de un mensaje BPMN mediante un evento Message Throw asociado a un mensaje.",
            endpoint("POST", "/api/v1/lanes/{laneId}/eventos-mensaje", "Crea un EventoMensaje de tipo THROW.")
                + endpoint("POST", "/api/v1/procesos/{procesoId}/mensajes", "Crea el mensaje y lo relaciona con el Throw."),
            flow(
                "<code>EventoMensajeController</code> crea el nodo THROW usando <code>EventoMensajeService</code>.",
                "<code>MensajeController</code> recibe eventoThrowId en <code>MensajeRequest</code>.",
                "<code>MensajeService</code> resuelve el evento desde <code>NodoFlujoRepository</code> y valida que sea THROW.",
                "También verifica que el Throw pertenezca al pool origen y coincida en nombre/correlación."
            ),
            files(
                "<code>modelado/controller/EventoMensajeController.java</code>",
                "<code>modelado/service/EventoMensajeService.java</code>",
                "<code>modelado/controller/MensajeController.java</code>",
                "<code>modelado/service/MensajeService.java</code>",
                "<code>modelado/model/EventoMensaje.java</code>",
                "<code>modelado/model/TipoEventoMensaje.java</code>",
                "<code>modelado/model/Mensaje.java</code>"
            ),
            "Un mensaje completo para publicación necesita Message Throw. La coherencia final también se comprueba en ValidacionModeloService."
        );
    }

    private static String hu26() {
        return hu(
            "Documenta mensajes enviados a sistemas externos y qué debe ocurrir si la notificación falla.",
            endpoint("POST/PUT", "/api/v1/procesos/{procesoId}/mensajes / /api/v1/mensajes/{id}", "Configura destino externo y política de fallo."),
            flow(
                "<code>MensajeRequest</code> y <code>EditarMensajeRequest</code> incluyen tipoDestinoExterno, destinoExterno, politicaFalloNotificacion y actividadErrorId.",
                "<code>MensajeService.validarDestinoExterno()</code> comprueba el pool destino.",
                "Si la política es DERIVAR_ACTIVIDAD_ERROR, el servicio resuelve y valida la actividad dentro del mismo proceso.",
                "Los datos se guardan en <code>Mensaje</code>; Beta documenta la integración, no ejecuta correo/web service/cola reales."
            ),
            files(
                "<code>modelado/service/MensajeService.java</code>",
                "<code>modelado/model/Mensaje.java</code>",
                "<code>modelado/model/TipoDestinoExterno.java</code>",
                "<code>modelado/model/PoliticaFalloNotificacion.java</code>",
                "<code>modelado/controller/dto/MensajeRequest.java</code>",
                "<code>modelado/controller/dto/EditarMensajeRequest.java</code>"
            ),
            "El pool destino debe ser SISTEMA_EXTERNO y caja negra, el destino debe quedar documentado y la política de fallo es obligatoria. DERIVAR_ACTIVIDAD_ERROR exige una actividad válida."
        );
    }

    private static String hu27() {
        return hu(
            "Modela recepción de mensajes mediante Message Catch de inicio o intermedio.",
            endpoint("POST/PUT", "/api/v1/lanes/{laneId}/eventos-mensaje / /api/v1/eventos-mensaje/{id}", "Crea o edita CATCH_INICIO y CATCH_INTERMEDIO."),
            flow(
                "<code>EventoMensajeService</code> crea/edita el nodo receptor.",
                "El Catch se relaciona con el mensaje desde <code>MensajeService</code> mediante eventoCatchId.",
                "<code>ArcoService</code> impide crear un arco entrante hacia CATCH_INICIO.",
                "<code>ValidacionModeloService</code> vuelve a comprobar la regla antes de publicar."
            ),
            files(
                "<code>modelado/controller/EventoMensajeController.java</code>",
                "<code>modelado/service/EventoMensajeService.java</code>",
                "<code>modelado/service/ArcoService.java</code>",
                "<code>modelado/service/ValidacionModeloService.java</code>",
                "<code>modelado/model/EventoMensaje.java</code>"
            ),
            "Un CATCH_INICIO no puede tener arcos entrantes activos. Los arcos eliminados lógicamente no bloquean la validación."
        );
    }

    private static String hu28() {
        return hu(
            "Define cómo un mensaje se asocia al caso/proceso correcto mediante una clave de correlación.",
            endpoint("GET", "/api/v1/mensajes/{mensajeId}/correlacion", "Consulta la correlación.")
                + endpoint("PUT", "/api/v1/mensajes/{mensajeId}/correlacion", "Crea o actualiza el criterio.")
                + endpoint("DELETE", "/api/v1/mensajes/{mensajeId}/correlacion", "Elimina la correlación."),
            flow(
                "<code>CorrelacionController</code> delega en <code>CorrelacionService</code>.",
                "<code>CorrelacionService.definir()</code> sincroniza el criterio con <code>Mensaje.claveCorrelacion</code>.",
                "Se valida que Throw/Catch utilicen la misma clave cuando están presentes.",
                "El servicio detecta mensajes ambiguos con mismo nombre + clave dentro del proceso.",
                "<code>ValidacionModeloService</code> exige correlación definida antes de publicar."
            ),
            files(
                "<code>modelado/controller/CorrelacionController.java</code>",
                "<code>modelado/service/CorrelacionService.java</code>",
                "<code>modelado/repository/CorrelacionRepository.java</code>",
                "<code>modelado/repository/MensajeRepository.java</code>",
                "<code>modelado/model/Correlacion.java</code>",
                "<code>modelado/model/Mensaje.java</code>",
                "<code>modelado/service/ValidacionModeloService.java</code>"
            ),
            "La combinación nombre + clave no puede ser ambigua. Al borrar la correlación también se limpia la clave del mensaje para mantener consistencia."
        );
    }
}
