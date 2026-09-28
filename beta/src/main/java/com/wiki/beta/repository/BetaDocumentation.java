package com.wiki.beta.repository;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.wiki.beta.model.WikiPage;

/**
 * Documentacion tecnica del proyecto Beta.
 *
 * El contenido se mantiene separado del repositorio en memoria para que las
 * historias de usuario puedan crecer sin convertir WikiRepository en un archivo
 * dificil de mantener.
 */
final class BetaDocumentation {

    private static final String CATEGORIA_HU = "Historias de Usuario REST";

    private BetaDocumentation() {
    }

    static List<WikiPage> pages() {
        var pages = new java.util.ArrayList<>(List.of(
                apiRest(),
                seguridad(),
                modelado(),
                hu01(), hu02(), hu03(), hu04(), hu05(), hu06(), hu07(),
                hu08(), hu09(), hu10(), hu11(), hu12(), hu13(), hu14(),
                hu15(), hu16(), hu17(), hu18(), hu19(), hu20(), hu21(),
                hu22(), hu23(), hu24(), hu25(), hu26(), hu27(), hu28()));
        pages.addAll(BetaProjectGuides.pages());
        return List.copyOf(pages);
    }

    private static WikiPage apiRest() {
        String contenido = """
                <div class="wiki-doc">
                    <p class="wiki-resumen">
                        Beta-back se implemento como una API REST versionada bajo <code>/api/v1</code>.
                        El frontend o cualquier cliente HTTP no accede directamente a entidades JPA:
                        envia JSON a un Controller, el Controller delega la regla de negocio a un Service
                        y el Service utiliza repositories para consultar o persistir el modelo.
                    </p>

                    <h2>Arquitectura de una peticion</h2>
                    <ol>
                        <li><strong>Cliente HTTP:</strong> envia una peticion a <code>/api/v1/**</code>.</li>
                        <li><strong>JWT:</strong> <code>JwtAuthenticationFilter</code> lee
                            <code>Authorization: Bearer &lt;token&gt;</code>, valida el token con
                            <code>JwtService</code> y reconstruye el usuario vigente.</li>
                        <li><strong>Principal:</strong> se crea un <code>ApiPrincipal</code> con
                            <code>usuarioId</code>, <code>empresaId</code>, rol y correo. El tenant
                            siempre sale de este principal y nunca de un <code>empresaId</code> enviado
                            por el cliente.</li>
                        <li><strong>Autorizacion:</strong> <code>SecurityConfig</code> decide si el rol
                            puede acceder al endpoint. Los GET requieren autenticacion, las operaciones
                            sensibles se restringen a ADMINISTRADOR y las demas escrituras permiten
                            ADMINISTRADOR o EDITOR.</li>
                        <li><strong>Controller:</strong> recibe DTOs de request, extrae el
                            <code>ApiPrincipal</code>, llama al Service y construye un DTO de response.</li>
                        <li><strong>Service:</strong> concentra reglas de negocio, validaciones,
                            aislamiento por empresa, auditoria y transacciones.</li>
                        <li><strong>Repository:</strong> Spring Data JPA ejecuta las consultas. Los
                            repositories tenant-aware incluyen la empresa en sus busquedas para evitar
                            acceso cruzado entre organizaciones.</li>
                        <li><strong>Respuesta:</strong> el Controller devuelve JSON mediante
                            <code>ResponseEntity</code>.</li>
                    </ol>

                    <div class="wiki-flow">
                        Cliente JSON &rarr; JWT/Security &rarr; Controller &rarr; Service &rarr;
                        Repository/JPA &rarr; Base de datos &rarr; Response DTO
                    </div>

                    <h2>Convenciones HTTP</h2>
                    <ul>
                        <li><code>200 OK</code> para consultas y actualizaciones exitosas.</li>
                        <li><code>201 Created</code> + cabecera <code>Location</code> para recursos creados.</li>
                        <li><code>204 No Content</code> para eliminaciones y operaciones sin cuerpo de respuesta.</li>
                        <li><code>400 Bad Request</code> para JSON o parametros invalidos.</li>
                        <li><code>401 Unauthorized</code> cuando falta autenticacion valida.</li>
                        <li><code>403 Forbidden</code> cuando el usuario esta autenticado pero no tiene permiso.</li>
                        <li><code>404 Not Found</code> para recursos inexistentes o fuera del tenant.</li>
                        <li><code>409 Conflict</code> cuando se viola una regla de negocio.</li>
                    </ul>

                    <h2>Errores consistentes</h2>
                    <p>
                        <code>ApiExceptionHandler</code> centraliza las excepciones y devuelve
                        <code>ProblemDetail</code>. Esto evita respuestas diferentes por Controller y
                        mantiene una forma uniforme para validaciones, recursos no encontrados y reglas
                        de negocio.
                    </p>

                    <h2>Paginacion y contratos estables</h2>
                    <p>
                        La API no expone directamente <code>Page&lt;T&gt;</code> de Spring. Utiliza
                        <code>PageResponse&lt;T&gt;</code> con <code>content</code>, pagina, tamano,
                        total de elementos y total de paginas. Esto desacopla el contrato REST del
                        framework interno.
                    </p>

                    <h2>Archivos principales de la API REST</h2>
                    <ul class="file-list">
                        <li><code>security/SecurityConfig.java</code> - reglas de autorizacion y modo stateless.</li>
                        <li><code>security/JwtAuthenticationFilter.java</code> - autenticacion de cada request.</li>
                        <li><code>security/JwtService.java</code> - generacion y validacion de JWT.</li>
                        <li><code>security/ApiPrincipal.java</code> - identidad autenticada y tenant.</li>
                        <li><code>common/api/ApiExceptionHandler.java</code> - errores REST con ProblemDetail.</li>
                        <li><code>common/api/PageResponse.java</code> - contrato de paginacion.</li>
                        <li><code>config/OpenApiConfig.java</code> - documentacion OpenAPI/Swagger.</li>
                        <li><code>gestion/controller/**</code> - endpoints de empresa, usuarios, procesos y roles.</li>
                        <li><code>modelado/controller/**</code> - endpoints BPMN.</li>
                    </ul>

                    <h2>Ejemplo real: Controller -> Service -> Repository</h2>
                    <p>
                        El siguiente ejemplo resume el patron que repetimos en el backend. El Controller
                        no decide reglas de negocio ni recibe el tenant desde el JSON: toma
                        <code>empresaId</code> desde <code>ApiPrincipal</code> y delega en el Service.
                    </p>
                    <pre class="code-block"><code>@PostMapping
public ResponseEntity&lt;ProcesoResponse&gt; crear(
        @Validated @RequestBody ProcesoRequest request,
        @AuthenticationPrincipal ApiPrincipal principal) {

    Proceso proceso = procesoService.crear(
            principal.empresaId(),
            principal.usuarioId(),
            request.nombre(),
            request.descripcion(),
            request.categoria());

    return ResponseEntity
            .created(URI.create("/api/v1/procesos/" + proceso.getId()))
            .body(ProcesoResponse.of(proceso));
}</code></pre>
                    <p>
                        Dentro de <code>ProcesoService.crear()</code> se valida el nombre, se busca la
                        Empresa y el Usuario mediante repositories, se persiste el Proceso, se crea el
                        Pool inicial y se registra el historial. Ese mismo reparto de responsabilidades
                        se aplica en ActividadService, ArcoService, GatewayService, MensajeService y los
                        demas servicios.
                    </p>

                    <h2>Mapa de historias de usuario</h2>
                    <div class="hu-index">
                        <p><strong>Empresa, usuarios y seguridad:</strong>
                            <a href="/secciones/hu-01-registro-empresa">HU-01</a>,
                            <a href="/secciones/hu-02-gestion-usuarios">HU-02</a>,
                            <a href="/secciones/hu-03-inicio-sesion">HU-03</a>.
                        </p>
                        <p><strong>Procesos:</strong>
                            <a href="/secciones/hu-04-crear-proceso">HU-04</a> a
                            <a href="/secciones/hu-07-consultar-procesos">HU-07</a>.
                        </p>
                        <p><strong>Actividades, arcos y gateways:</strong>
                            <a href="/secciones/hu-08-crear-actividad">HU-08</a> a
                            <a href="/secciones/hu-16-eliminar-gateway">HU-16</a>.
                        </p>
                        <p><strong>Roles, pools, lanes y comparticion:</strong>
                            <a href="/secciones/hu-17-crear-rol">HU-17</a> a
                            <a href="/secciones/hu-24-permisos-estructura">HU-24</a>.
                        </p>
                        <p><strong>Mensajeria BPMN:</strong>
                            <a href="/secciones/hu-25-message-throw">HU-25</a> a
                            <a href="/secciones/hu-28-correlacion">HU-28</a>.
                        </p>
                    </div>

                    <h2>Por que esta arquitectura</h2>
                    <p>
                        Separamos Controller, Service y Repository para que HTTP no contenga reglas de
                        negocio y para que la persistencia no conozca detalles de presentacion. Esta
                        separacion facilita pruebas unitarias, evita duplicacion de reglas y permite
                        evolucionar el frontend sin cambiar el dominio.
                    </p>
                </div>
                """;
        return new WikiPage("API REST - Arquitectura del backend", "api-rest-historias-usuario",
                contenido, "Proyecto Beta");
    }

    private static WikiPage seguridad() {
        return pagina("Seguridad, multitenencia y permisos", "seguridad-multitenencia",
                """
                <div class="wiki-doc">
                    <p class="wiki-resumen">
                        Beta es multitenant: cada Empresa es un tenant y ningun usuario debe consultar
                        o modificar informacion de otra empresa salvo el caso explicito de procesos
                        compartidos en solo lectura.
                    </p>
                    <h2>Como se identifica el tenant</h2>
                    <p>
                        Al iniciar sesion, <code>JwtService</code> incluye en el token
                        <code>usuarioId</code>, <code>empresaId</code> y rol. En cada peticion
                        <code>JwtAuthenticationFilter</code> valida el token, comprueba que el usuario
                        siga activo y crea <code>ApiPrincipal</code>. Los Controllers toman
                        <code>principal.empresaId()</code>; por diseño no confian en un tenant enviado
                        dentro del JSON.
                    </p>
                    <h2>Roles de acceso</h2>
                    <ul>
                        <li><strong>ADMINISTRADOR:</strong> administra usuarios, roles, permisos,
                            comparticion y eliminaciones sensibles.</li>
                        <li><strong>EDITOR:</strong> modela procesos donde SecurityConfig y la politica
                            de estructura lo permiten.</li>
                        <li><strong>SOLO_LECTURA:</strong> consulta informacion, especialmente procesos
                            compartidos, sin capacidad de escritura.</li>
                    </ul>
                    <h2>Archivos clave</h2>
                    <ul class="file-list">
                        <li><code>security/SecurityConfig.java</code></li>
                        <li><code>security/JwtAuthenticationFilter.java</code></li>
                        <li><code>security/JwtService.java</code></li>
                        <li><code>security/ApiPrincipal.java</code></li>
                        <li><code>common/EntidadEmpresa.java</code></li>
                        <li><code>common/RepositorioTenant.java</code></li>
                        <li><code>gestion/model/RolAcceso.java</code></li>
                        <li><code>gestion/service/PermisoEstructuraService.java</code></li>
                    </ul>
                </div>
                """, "Proyecto Beta");
    }

    private static WikiPage modelado() {
        return pagina("Modelado BPMN y mensajeria", "modelado-bpmn-mensajeria",
                """
                <div class="wiki-doc">
                    <p class="wiki-resumen">
                        El dominio BPMN se representa con entidades propias y no como un JSON libre.
                        Pool y Lane organizan el proceso; los nodos de flujo heredan de
                        <code>NodoFlujo</code>; los arcos conectan nodos y los mensajes conectan pools.
                    </p>
                    <h2>Estructura del modelo</h2>
                    <ul>
                        <li><code>Pool</code> representa un participante.</li>
                        <li><code>Lane</code> divide internamente un Pool y puede asociarse a un
                            <code>RolProceso</code>.</li>
                        <li><code>NodoFlujo</code> concentra nombre, posicion, lane y estado activo.</li>
                        <li><code>Actividad</code>, <code>Gateway</code> y <code>EventoMensaje</code>
                            son tipos concretos de nodo.</li>
                        <li><code>Arco</code> representa el flujo secuencial dentro de un mismo Pool.</li>
                        <li><code>Mensaje</code> representa comunicacion entre pools y puede vincular
                            Message Throw, Message Catch, destino externo y politica de fallo.</li>
                        <li><code>Correlacion</code> define como asociar un mensaje con el caso correcto.</li>
                    </ul>
                    <h2>Coherencia antes de publicar</h2>
                    <p>
                        <code>ValidacionModeloService</code> carga pools, lanes, nodos, arcos y mensajes
                        por proceso y valida reglas globales antes de permitir que
                        <code>ProcesoService</code> cambie el estado a PUBLICADO.
                    </p>
                    <h2>Trazabilidad</h2>
                    <p>
                        <code>AuditoriaModeladoService</code> reutiliza
                        <code>HistorialCambioService</code> para registrar cambios del modelo con el
                        usuario autenticado. Las eliminaciones principales se manejan como baja logica
                        para conservar informacion historica.
                    </p>
                    <h2>Archivos clave</h2>
                    <ul class="file-list">
                        <li><code>modelado/model/NodoFlujo.java</code></li>
                        <li><code>modelado/model/Actividad.java</code></li>
                        <li><code>modelado/model/Gateway.java</code></li>
                        <li><code>modelado/model/EventoMensaje.java</code></li>
                        <li><code>modelado/model/Arco.java</code></li>
                        <li><code>modelado/model/Mensaje.java</code></li>
                        <li><code>modelado/model/Correlacion.java</code></li>
                        <li><code>modelado/service/ValidacionModeloService.java</code></li>
                        <li><code>modelado/service/AuditoriaModeladoService.java</code></li>
                    </ul>
                </div>
                """, "Proyecto Beta");
    }

    private static WikiPage hu01() {
        return hu("HU-01", "Registro de empresa", "hu-01-registro-empresa",
                "Permite registrar una organizacion nueva y dejar creado su administrador inicial.",
                new String[] {
                        endpoint("POST", "/api/v1/empresas", "registra la empresa y responde 201 Created.")
                },
                new String[] {
                        "EmpresaController recibe RegistroEmpresaRequest y delega a EmpresaService.",
                        "EmpresaService valida que el NIT no este registrado, crea Empresa y crea el Usuario administrador inicial.",
                        "La contrasena se codifica con PasswordEncoder/BCrypt antes de persistirla.",
                        "EmpresaResponse devuelve informacion publica de la empresa; la contrasena nunca sale en el response."
                },
                new String[] {
                        archivo("gestion/controller/EmpresaController.java", "endpoint REST."),
                        archivo("gestion/service/EmpresaService.java", "reglas de registro."),
                        archivo("gestion/repository/EmpresaRepository.java", "persistencia y validacion de NIT."),
                        archivo("gestion/repository/UsuarioRepository.java", "persistencia del administrador."),
                        archivo("gestion/model/Empresa.java", "entidad empresa."),
                        archivo("gestion/model/Usuario.java", "administrador inicial."),
                        archivo("gestion/controller/dto/RegistroEmpresaRequest.java", "entrada JSON."),
                        archivo("gestion/controller/dto/EmpresaResponse.java", "salida JSON.")
                },
                new String[] {
                        "El NIT debe ser unico.",
                        "El administrador inicial queda asociado a la empresa recien creada.",
                        "POST /api/v1/empresas es publico; las operaciones posteriores requieren JWT."
                });
    }

    private static WikiPage hu02() {
        return hu("HU-02", "Gestion e invitacion de usuarios", "hu-02-gestion-usuarios",
                "El administrador puede crear, consultar, actualizar, desactivar e invitar colaboradores de su empresa.",
                new String[] {
                        endpoint("GET", "/api/v1/usuarios", "lista colaboradores activos del tenant."),
                        endpoint("POST", "/api/v1/usuarios", "crea un colaborador."),
                        endpoint("GET", "/api/v1/usuarios/{id}", "consulta un usuario."),
                        endpoint("PATCH", "/api/v1/usuarios/{id}", "actualiza rol o estado."),
                        endpoint("DELETE", "/api/v1/usuarios/{id}", "desactiva un usuario."),
                        endpoint("POST", "/api/v1/usuarios/invitaciones", "crea invitacion temporal."),
                        endpoint("POST", "/api/v1/usuarios/invitaciones/{token}/aceptar", "acepta la invitacion.")
                },
                new String[] {
                        "UsuarioController toma empresaId del ApiPrincipal y nunca del request.",
                        "UsuarioService aplica las reglas de alta, actualizacion, desactivacion y BCrypt.",
                        "InvitacionUsuarioService genera un UUID, asigna expiracion de 48 horas y evita invitaciones activas duplicadas.",
                        "Al aceptar el token se reutiliza UsuarioService.crearColaborador y la invitacion queda marcada como usada."
                },
                new String[] {
                        archivo("gestion/controller/UsuarioController.java", "CRUD de colaboradores."),
                        archivo("gestion/controller/InvitacionUsuarioController.java", "flujo de invitaciones."),
                        archivo("gestion/service/UsuarioService.java", "reglas del usuario."),
                        archivo("gestion/service/InvitacionUsuarioService.java", "token y expiracion."),
                        archivo("gestion/repository/UsuarioRepository.java", "usuarios tenant-aware."),
                        archivo("gestion/repository/InvitacionUsuarioRepository.java", "invitaciones."),
                        archivo("gestion/model/Usuario.java", "colaborador."),
                        archivo("gestion/model/InvitacionUsuario.java", "credencial temporal.")
                },
                new String[] {
                        "SecurityConfig restringe /api/v1/usuarios/** a ADMINISTRADOR.",
                        "Aceptar una invitacion es publico porque el token funciona como credencial temporal.",
                        "Las invitaciones expiradas se limpian antes de crear nuevas invitaciones.",
                        "Mientras no exista SMTP, el token se entrega al administrador para compartirlo manualmente."
                });
    }

    private static WikiPage hu03() {
        return hu("HU-03", "Inicio y cierre de sesion", "hu-03-inicio-sesion",
                "Autentica usuarios mediante JWT sin guardar sesiones HTTP en el servidor.",
                new String[] {
                        endpoint("POST", "/api/v1/auth/login", "valida credenciales y devuelve Bearer JWT."),
                        endpoint("POST", "/api/v1/auth/logout", "confirma cierre logico; el cliente descarta el token.")
                },
                new String[] {
                        "AuthController llama UsuarioService.autenticar con correo y contrasena.",
                        "Si las credenciales son validas, JwtService genera un token con usuarioId, empresaId y rol.",
                        "JwtAuthenticationFilter valida el Bearer token en cada request y comprueba que el usuario siga activo.",
                        "ApiPrincipal queda disponible en los Controllers mediante @AuthenticationPrincipal."
                },
                new String[] {
                        archivo("gestion/controller/AuthController.java", "login/logout."),
                        archivo("gestion/service/UsuarioService.java", "autenticacion contra BCrypt."),
                        archivo("security/JwtService.java", "firma y valida JWT."),
                        archivo("security/JwtAuthenticationFilter.java", "autenticacion por request."),
                        archivo("security/ApiPrincipal.java", "identidad autenticada."),
                        archivo("security/SecurityConfig.java", "SessionCreationPolicy.STATELESS.")
                },
                new String[] {
                        "No se usa HttpSession.",
                        "El mensaje de credenciales invalidas es generico.",
                        "Un token valido no autentica a un usuario que haya sido desactivado.",
                        "El tenant del resto de la API nace del empresaId incluido en ApiPrincipal."
                });
    }

    private static WikiPage hu04() {
        return hu("HU-04", "Crear proceso", "hu-04-crear-proceso",
                "Crea un proceso nuevo dentro de la empresa autenticada y prepara su estructura inicial.",
                new String[] {
                        endpoint("POST", "/api/v1/procesos", "crea el proceso y devuelve 201 + Location.")
                },
                new String[] {
                        "ProcesoController obtiene empresaId y usuarioId del ApiPrincipal.",
                        "ProcesoService verifica que no exista otro proceso activo con el mismo nombre.",
                        "El proceso se crea BORRADOR, activo y con fechas de creacion/modificacion.",
                        "Se crea automaticamente un Pool inicial con el nombre de la empresa, tipo EMPRESA y cajaNegra=false.",
                        "HistorialCambioService registra 'Proceso creado' con el usuario autor."
                },
                new String[] {
                        archivo("gestion/controller/ProcesoController.java", "endpoint."),
                        archivo("gestion/service/ProcesoService.java", "creacion y reglas."),
                        archivo("gestion/repository/ProcesoRepository.java", "persistencia."),
                        archivo("modelado/repository/PoolRepository.java", "pool inicial."),
                        archivo("gestion/service/HistorialCambioService.java", "auditoria."),
                        archivo("gestion/model/Proceso.java", "entidad principal."),
                        archivo("modelado/model/Pool.java", "participante inicial.")
                },
                new String[] {
                        "Nombre unico entre procesos activos de la empresa.",
                        "Siempre se crea en estado BORRADOR.",
                        "El usuario que crea debe pertenecer al mismo tenant."
                });
    }

    private static WikiPage hu05() {
        return hu("HU-05", "Editar y cambiar estado de un proceso", "hu-05-editar-proceso",
                "Permite modificar metadatos y controlar el ciclo de vida del proceso.",
                new String[] {
                        endpoint("PUT", "/api/v1/procesos/{id}", "edita nombre, descripcion y categoria."),
                        endpoint("PATCH", "/api/v1/procesos/{id}", "cambia el estado del proceso.")
                },
                new String[] {
                        "ProcesoController separa la edicion de datos del cambio de estado.",
                        "ProcesoService.editarDatos vuelve a validar unicidad del nombre y actualiza fechaModificacion.",
                        "ProcesoService.cambiarEstado evita transiciones invalidas.",
                        "Cuando el nuevo estado es PUBLICADO se ejecuta ValidacionModeloService antes de guardar.",
                        "Cada cambio queda en HistorialCambioService."
                },
                new String[] {
                        archivo("gestion/controller/ProcesoController.java", "PUT y PATCH."),
                        archivo("gestion/service/ProcesoService.java", "ciclo de vida."),
                        archivo("modelado/service/ValidacionModeloService.java", "coherencia antes de publicar."),
                        archivo("gestion/service/HistorialCambioService.java", "trazabilidad."),
                        archivo("gestion/controller/dto/EditarProcesoRequest.java", "edicion."),
                        archivo("gestion/controller/dto/CambiarEstadoProcesoRequest.java", "estado.")
                },
                new String[] {
                        "Un proceso PUBLICADO no puede volver a BORRADOR.",
                        "Publicar exige que pools, gateways, eventos y mensajes sean coherentes.",
                        "El nombre editado no puede duplicar otro proceso activo del tenant."
                });
    }

    private static WikiPage hu06() {
        return hu("HU-06", "Eliminar proceso", "hu-06-eliminar-proceso",
                "Retira un proceso de la operacion normal sin destruir su informacion historica.",
                new String[] {
                        endpoint("DELETE", "/api/v1/procesos/{id}", "realiza baja logica y devuelve 204.")
                },
                new String[] {
                        "ProcesoController delega en ProcesoService.eliminarLogico.",
                        "El Service obtiene el proceso dentro del tenant y al usuario autor.",
                        "Se cambia activo=false y se actualiza fechaModificacion.",
                        "HistorialCambioService registra la baja logica."
                },
                new String[] {
                        archivo("gestion/controller/ProcesoController.java", "DELETE."),
                        archivo("gestion/service/ProcesoService.java", "baja logica."),
                        archivo("gestion/repository/ProcesoRepository.java", "persistencia."),
                        archivo("gestion/service/HistorialCambioService.java", "registro del cambio.")
                },
                new String[] {
                        "No se hace DELETE fisico de la entidad Proceso.",
                        "Las consultas normales usan obtener() sobre procesos activos.",
                        "GET /api/v1/procesos permite filtrar por activo cuando se necesita consulta administrativa."
                });
    }

    private static WikiPage hu07() {
        return hu("HU-07", "Consultar procesos y diagrama completo", "hu-07-consultar-procesos",
                "Permite buscar procesos, consultar su detalle, historial y recuperar el diagrama completo en una sola respuesta.",
                new String[] {
                        endpoint("GET", "/api/v1/procesos", "busca y pagina procesos."),
                        endpoint("GET", "/api/v1/procesos/{id}", "detalle + historial."),
                        endpoint("GET", "/api/v1/procesos/{id}/historial", "trazabilidad."),
                        endpoint("GET", "/api/v1/procesos/{id}/diagrama", "modelo completo del proceso.")
                },
                new String[] {
                        "ProcesoSpecifications construye filtros por empresa, nombre, estado, categoria y activo.",
                        "ProcesoController convierte Page<Proceso> a PageResponse<ProcesoResponse>.",
                        "ProcesoDiagramaService carga pools, lanes, nodos, arcos y mensajes por lotes.",
                        "El Service clasifica NodoFlujo en Actividad, Gateway y EventoMensaje y excluye elementos inactivos.",
                        "ProcesoDiagramaResponse expone un contrato agregado listo para el cliente."
                },
                new String[] {
                        archivo("gestion/controller/ProcesoController.java", "consultas."),
                        archivo("gestion/service/ProcesoService.java", "busqueda."),
                        archivo("gestion/repository/ProcesoSpecifications.java", "filtros dinamicos."),
                        archivo("gestion/service/ProcesoDiagramaService.java", "ensamble BPMN."),
                        archivo("common/api/PageResponse.java", "paginacion."),
                        archivo("gestion/controller/dto/ProcesoDiagramaResponse.java", "response agregado.")
                },
                new String[] {
                        "Todas las consultas se limitan al empresaId autenticado.",
                        "El diagrama ignora nodos, arcos y mensajes dados de baja.",
                        "La pagina tiene tamano estable y orden por fecha de modificacion descendente."
                });
    }

    private static WikiPage hu08() {
        return hu("HU-08", "Crear actividad", "hu-08-crear-actividad",
                "Agrega una tarea BPMN a una lane del proceso.",
                new String[] {
                        endpoint("POST", "/api/v1/lanes/{laneId}/actividades", "crea la actividad.")
                },
                new String[] {
                        "ActividadController transforma ActividadRequest en parametros de dominio.",
                        "ActividadService busca la Lane dentro del tenant y valida que su Pool no sea caja negra.",
                        "El nombre se valida contra todos los nodos del proceso para evitar colisiones.",
                        "Si el cliente no envia tipoActividad se conserva compatibilidad usando el tipo por defecto.",
                        "AuditoriaModeladoService registra la creacion."
                },
                new String[] {
                        archivo("modelado/controller/ActividadController.java", "endpoint."),
                        archivo("modelado/service/ActividadService.java", "reglas."),
                        archivo("modelado/repository/NodoFlujoRepository.java", "nombres y nodos."),
                        archivo("modelado/repository/LaneRepository.java", "lane destino."),
                        archivo("modelado/model/Actividad.java", "entidad."),
                        archivo("modelado/model/TipoActividad.java", "tipos soportados."),
                        archivo("modelado/controller/dto/ActividadRequest.java", "request.")
                },
                new String[] {
                        "No se crean actividades dentro de pools de caja negra.",
                        "El nombre del nodo es unico dentro del proceso.",
                        "La actividad queda activa y posicionada con X/Y."
                });
    }

    private static WikiPage hu09() {
        return hu("HU-09", "Editar actividad", "hu-09-editar-actividad",
                "Modifica datos visuales y de negocio de una actividad, incluido el cambio de lane.",
                new String[] {
                        endpoint("PUT", "/api/v1/actividades/{id}", "actualiza actividad.")
                },
                new String[] {
                        "ActividadController envia nombre, descripcion, posicion, laneId y tipo al Service.",
                        "ActividadService obtiene la actividad activa y vuelve a validar el nombre.",
                        "Si cambia lane, el Service comprueba que la lane destino pertenezca al mismo proceso y sea valida.",
                        "Los arcos existentes se conservan porque la identidad del nodo no cambia.",
                        "AuditoriaModeladoService registra la edicion."
                },
                new String[] {
                        archivo("modelado/controller/ActividadController.java", "PUT."),
                        archivo("modelado/service/ActividadService.java", "edicion y movimiento."),
                        archivo("modelado/repository/NodoFlujoRepository.java", "actividad y unicidad."),
                        archivo("modelado/repository/LaneRepository.java", "lane destino."),
                        archivo("modelado/controller/dto/ActividadRequest.java", "datos editables.")
                },
                new String[] {
                        "No se permite mover la actividad a una lane de otro proceso.",
                        "No se permite moverla a estructura interna de un pool de caja negra.",
                        "La edicion respeta baja logica: solo se obtiene una actividad activa."
                });
    }

    private static WikiPage hu10() {
        return hu("HU-10", "Eliminar actividad", "hu-10-eliminar-actividad",
                "Permite anticipar el impacto de borrar una actividad y luego realizar una baja logica segura.",
                new String[] {
                        endpoint("GET", "/api/v1/actividades/{id}/impacto-eliminacion", "calcula advertencias."),
                        endpoint("DELETE", "/api/v1/actividades/{id}", "desactiva actividad y conexiones.")
                },
                new String[] {
                        "ActividadService.evaluarImpactoEliminacion revisa arcos entrantes y salientes activos.",
                        "Se informa si nodos vecinos pueden quedar desconectados.",
                        "Al eliminar, la actividad pasa a activo=false.",
                        "Los arcos entrantes y salientes tambien se desactivan; no se eliminan fisicamente.",
                        "La baja se registra en AuditoriaModeladoService."
                },
                new String[] {
                        archivo("modelado/controller/ActividadController.java", "impacto + DELETE."),
                        archivo("modelado/service/ActividadService.java", "analisis y baja."),
                        archivo("modelado/service/dto/ImpactoEliminacion.java", "resultado de dominio."),
                        archivo("modelado/controller/dto/ImpactoEliminacionResponse.java", "respuesta REST."),
                        archivo("modelado/repository/ArcoRepository.java", "conexiones.")
                },
                new String[] {
                        "El impacto se calcula antes de modificar datos.",
                        "La baja logica conserva trazabilidad.",
                        "Las consultas posteriores filtran la actividad inactiva."
                });
    }

    private static WikiPage hu11() {
        return hu("HU-11", "Crear arco", "hu-11-crear-arco",
                "Conecta dos nodos activos para representar flujo secuencial dentro de un Pool.",
                new String[] {
                        endpoint("POST", "/api/v1/arcos", "crea una conexion.")
                },
                new String[] {
                        "ArcoController recibe origenId, destinoId, etiqueta y condicion.",
                        "ArcoService obtiene ambos NodoFlujo dentro de la empresa.",
                        "Se valida que origen y destino esten activos, sean distintos y pertenezcan al mismo Pool.",
                        "ArcoRepository verifica que no exista otro arco activo con el mismo origen y destino.",
                        "Si el origen es gateway EXCLUSIVO o INCLUSIVO se exige condicion.",
                        "No se permite crear un arco entrante hacia un CATCH_INICIO."
                },
                new String[] {
                        archivo("modelado/controller/ArcoController.java", "POST."),
                        archivo("modelado/service/ArcoService.java", "reglas de conectividad."),
                        archivo("modelado/repository/ArcoRepository.java", "duplicados y consultas."),
                        archivo("modelado/repository/NodoFlujoRepository.java", "origen/destino."),
                        archivo("modelado/model/Arco.java", "entidad."),
                        archivo("modelado/controller/dto/ArcoRequest.java", "entrada JSON.")
                },
                new String[] {
                        "Los arcos no cruzan pools.",
                        "Un CATCH_INICIO no recibe flujo secuencial.",
                        "Los duplicados se calculan solo contra arcos activos."
                });
    }

    private static WikiPage hu12() {
        return hu("HU-12", "Editar arco", "hu-12-editar-arco",
                "Permite modificar etiqueta, condicion y, cuando se solicita, los extremos del arco.",
                new String[] {
                        endpoint("PUT", "/api/v1/arcos/{id}", "actualiza el arco.")
                },
                new String[] {
                        "ArcoController usa EditarArcoRequest, separado del request de creacion.",
                        "ArcoService obtiene el arco activo y resuelve el nuevo origen/destino si fueron enviados.",
                        "Se reutiliza la misma validacion estructural utilizada al crear: nodos activos, mismo pool, no duplicado y reglas de gateway/eventos.",
                        "AuditoriaModeladoService registra el cambio."
                },
                new String[] {
                        archivo("modelado/controller/ArcoController.java", "PUT."),
                        archivo("modelado/service/ArcoService.java", "edicion."),
                        archivo("modelado/controller/dto/EditarArcoRequest.java", "campos editables."),
                        archivo("modelado/repository/ArcoRepository.java", "persistencia."),
                        archivo("modelado/service/AuditoriaModeladoService.java", "historial.")
                },
                new String[] {
                        "Editar los extremos no salta las reglas de creacion.",
                        "Los gateways exclusivos/inclusivos siguen requiriendo condiciones.",
                        "No se revive automaticamente un arco dado de baja."
                });
    }

    private static WikiPage hu13() {
        return hu("HU-13", "Eliminar arco", "hu-13-eliminar-arco",
                "Evalua si una conexion es critica antes de desactivarla.",
                new String[] {
                        endpoint("GET", "/api/v1/arcos/{id}/impacto-eliminacion", "informa posibles desconexiones."),
                        endpoint("DELETE", "/api/v1/arcos/{id}", "baja logica del arco.")
                },
                new String[] {
                        "ArcoService revisa si el nodo origen conserva otra salida activa y si el destino conserva otra entrada activa.",
                        "Las advertencias se devuelven con ImpactoEliminacionResponse.",
                        "DELETE cambia activo=false y conserva el registro en base de datos.",
                        "AuditoriaModeladoService registra la conexion eliminada."
                },
                new String[] {
                        archivo("modelado/controller/ArcoController.java", "impacto y DELETE."),
                        archivo("modelado/service/ArcoService.java", "analisis."),
                        archivo("modelado/service/dto/ImpactoEliminacion.java", "resultado."),
                        archivo("modelado/model/Arco.java", "campo activo."),
                        archivo("modelado/repository/ArcoRepository.java", "arcos alternativos.")
                },
                new String[] {
                        "Una eliminacion puede requerir confirmacion del cliente si rompe continuidad.",
                        "Los arcos inactivos dejan de participar en validaciones BPMN."
                });
    }

    private static WikiPage hu14() {
        return hu("HU-14", "Crear gateway", "hu-14-crear-gateway",
                "Agrega un punto de decision o paralelismo al modelo.",
                new String[] {
                        endpoint("POST", "/api/v1/lanes/{laneId}/gateways", "crea gateway.")
                },
                new String[] {
                        "GatewayController recibe GatewayRequest con nombre, tipo y posicion.",
                        "GatewayService obtiene la lane, evita pools de caja negra y valida nombre unico de nodo.",
                        "Se persiste un Gateway como NodoFlujo especializado.",
                        "AuditoriaModeladoService registra la creacion."
                },
                new String[] {
                        archivo("modelado/controller/GatewayController.java", "POST."),
                        archivo("modelado/service/GatewayService.java", "reglas."),
                        archivo("modelado/model/Gateway.java", "entidad."),
                        archivo("modelado/model/TipoGateway.java", "EXCLUSIVO, PARALELO, INCLUSIVO."),
                        archivo("modelado/repository/NodoFlujoRepository.java", "unicidad y persistencia."),
                        archivo("modelado/controller/dto/GatewayRequest.java", "request.")
                },
                new String[] {
                        "El gateway debe pertenecer a una lane interna valida.",
                        "Su nombre no puede colisionar con otro nodo del proceso.",
                        "Las reglas de cantidad de salidas se verifican al publicar el proceso."
                });
    }

    private static WikiPage hu15() {
        return hu("HU-15", "Editar gateway", "hu-15-editar-gateway",
                "Permite cambiar nombre, tipo y posicion manteniendo coherencia con sus arcos activos.",
                new String[] {
                        endpoint("PUT", "/api/v1/gateways/{id}", "actualiza gateway.")
                },
                new String[] {
                        "GatewayService obtiene el nodo activo y valida el nuevo nombre.",
                        "Al cambiar a PARALELO limpia condiciones que dejan de tener sentido.",
                        "Al cambiar desde PARALELO hacia EXCLUSIVO/INCLUSIVO comprueba que los arcos activos tengan condicion.",
                        "Los arcos dados de baja se ignoran para no bloquear una edicion legitima.",
                        "AuditoriaModeladoService registra la edicion."
                },
                new String[] {
                        archivo("modelado/controller/GatewayController.java", "PUT."),
                        archivo("modelado/service/GatewayService.java", "cambio de tipo."),
                        archivo("modelado/repository/ArcoRepository.java", "arcos salientes activos."),
                        archivo("modelado/repository/NodoFlujoRepository.java", "gateway.")
                },
                new String[] {
                        "Solo los arcos activos afectan la validacion.",
                        "EXCLUSIVO e INCLUSIVO requieren condiciones de salida.",
                        "PARALELO no necesita condiciones."
                });
    }

    private static WikiPage hu16() {
        return hu("HU-16", "Eliminar gateway", "hu-16-eliminar-gateway",
                "Analiza la ramificacion afectada y aplica baja logica al gateway y sus conexiones.",
                new String[] {
                        endpoint("GET", "/api/v1/gateways/{id}/impacto-eliminacion", "calcula impacto."),
                        endpoint("DELETE", "/api/v1/gateways/{id}", "desactiva gateway.")
                },
                new String[] {
                        "GatewayService.evaluarImpactoEliminacion cuenta entradas y salidas activas.",
                        "Si el gateway participa en una ramificacion se devuelven advertencias.",
                        "DELETE desactiva el Gateway y todos sus arcos entrantes/salientes.",
                        "La operacion se audita en el historial."
                },
                new String[] {
                        archivo("modelado/controller/GatewayController.java", "impacto/DELETE."),
                        archivo("modelado/service/GatewayService.java", "analisis y baja."),
                        archivo("modelado/service/dto/ImpactoEliminacion.java", "resultado."),
                        archivo("modelado/repository/ArcoRepository.java", "conexiones."),
                        archivo("modelado/service/AuditoriaModeladoService.java", "auditoria.")
                },
                new String[] {
                        "No se hace hard delete del NodoFlujo.",
                        "Los arcos inactivos no se consideran en impactos futuros."
                });
    }

    private static WikiPage hu17() {
        return hu("HU-17", "Crear rol de proceso", "hu-17-crear-rol",
                "Crea roles de negocio reutilizables que luego pueden asignarse a lanes.",
                new String[] {
                        endpoint("POST", "/api/v1/roles", "crea rol de proceso.")
                },
                new String[] {
                        "RolProcesoController recibe RolProcesoRequest.",
                        "RolProcesoService valida que no exista otro rol activo con el mismo nombre en la empresa.",
                        "El rol se guarda asociado al tenant y activo."
                },
                new String[] {
                        archivo("gestion/controller/RolProcesoController.java", "POST."),
                        archivo("gestion/service/RolProcesoService.java", "reglas."),
                        archivo("gestion/repository/RolProcesoRepository.java", "persistencia."),
                        archivo("gestion/model/RolProceso.java", "entidad."),
                        archivo("gestion/controller/dto/RolProcesoRequest.java", "request.")
                },
                new String[] {
                        "SecurityConfig restringe /api/v1/roles/** a ADMINISTRADOR.",
                        "El nombre es unico entre roles activos de la empresa."
                });
    }

    private static WikiPage hu18() {
        return hu("HU-18", "Editar rol de proceso", "hu-18-editar-rol",
                "Actualiza nombre y descripcion de un rol sin romper las lanes que lo referencian.",
                new String[] {
                        endpoint("PUT", "/api/v1/roles/{id}", "edita el rol.")
                },
                new String[] {
                        "RolProcesoService obtiene el rol activo del tenant y valida unicidad del nuevo nombre.",
                        "Se actualiza la misma entidad, por lo que las lanes conservan su referencia.",
                        "El Service busca las lanes que usan el rol y registra el cambio en los procesos afectados cuando existe usuario autenticado."
                },
                new String[] {
                        archivo("gestion/controller/RolProcesoController.java", "PUT."),
                        archivo("gestion/service/RolProcesoService.java", "edicion."),
                        archivo("modelado/repository/LaneRepository.java", "procesos que usan el rol."),
                        archivo("gestion/service/HistorialCambioService.java", "trazabilidad.")
                },
                new String[] {
                        "No se reemplaza el id del rol.",
                        "La edicion mantiene relaciones existentes.",
                        "Solo ADMINISTRADOR puede modificar roles."
                });
    }

    private static WikiPage hu19() {
        return hu("HU-19", "Eliminar rol de proceso", "hu-19-eliminar-rol",
                "Evita eliminar roles todavia usados por el modelo y usa baja logica.",
                new String[] {
                        endpoint("DELETE", "/api/v1/roles/{id}", "desactiva el rol.")
                },
                new String[] {
                        "RolProcesoService obtiene el rol activo.",
                        "LaneRepository cuenta lanes que referencian ese rol.",
                        "Si existe uso, se lanza ReglaNegocioException.",
                        "Si no existe uso, activo=false y se guarda nuevamente."
                },
                new String[] {
                        archivo("gestion/controller/RolProcesoController.java", "DELETE."),
                        archivo("gestion/service/RolProcesoService.java", "validacion de uso."),
                        archivo("modelado/repository/LaneRepository.java", "referencias."),
                        archivo("gestion/model/RolProceso.java", "baja logica.")
                },
                new String[] {
                        "No se permite dejar lanes apuntando a un rol eliminado.",
                        "La eliminacion es logica, no fisica."
                });
    }

    private static WikiPage hu20() {
        return hu("HU-20", "Consultar roles de proceso", "hu-20-consultar-roles",
                "Lista roles, muestra su uso y permite busqueda paginada por nombre.",
                new String[] {
                        endpoint("GET", "/api/v1/roles", "lista roles con cantidad de usos."),
                        endpoint("GET", "/api/v1/roles/{id}", "detalle y conteo."),
                        endpoint("GET", "/api/v1/roles/consulta?nombre=&pagina=", "busqueda paginada con procesos de uso.")
                },
                new String[] {
                        "RolProcesoService.listarConUso combina cada rol con el conteo de LaneRepository.",
                        "buscarConProcesos usa Pageable directamente en RolProcesoRepository.",
                        "Por cada rol de la pagina se obtienen nombres distintos de procesos donde aparece.",
                        "RolProcesoConsultaResponse incluye procesos y bandera enUso."
                },
                new String[] {
                        archivo("gestion/controller/RolProcesoController.java", "consultas."),
                        archivo("gestion/service/RolProcesoService.java", "agregacion."),
                        archivo("gestion/repository/RolProcesoRepository.java", "paginacion."),
                        archivo("modelado/repository/LaneRepository.java", "usos."),
                        archivo("gestion/controller/dto/RolProcesoConsultaResponse.java", "response.")
                },
                new String[] {
                        "La paginacion ocurre en base de datos, no con subList en memoria.",
                        "La consulta solo devuelve roles activos del tenant."
                });
    }

    private static WikiPage hu21() {
        return hu("HU-21", "Configurar pools", "hu-21-configurar-pool",
                "Administra participantes BPMN del proceso, incluida la semantica de caja negra.",
                new String[] {
                        endpoint("GET", "/api/v1/procesos/{procesoId}/pools", "lista pools."),
                        endpoint("POST", "/api/v1/procesos/{procesoId}/pools", "crea pool."),
                        endpoint("GET", "/api/v1/pools/{id}", "detalle."),
                        endpoint("PUT", "/api/v1/pools/{id}", "edita."),
                        endpoint("DELETE", "/api/v1/pools/{id}", "elimina cuando la estructura lo permite.")
                },
                new String[] {
                        "PoolController valida permisos dinamicos con PermisoEstructuraService antes de escribir.",
                        "PoolService asocia el Pool al proceso y empresa correctos.",
                        "TipoParticipante diferencia EMPRESA, participante externo u otros tipos del modelo.",
                        "cajaNegra indica que no se modela estructura interna.",
                        "La auditoria de modelado registra los cambios."
                },
                new String[] {
                        archivo("modelado/controller/PoolController.java", "CRUD."),
                        archivo("modelado/service/PoolService.java", "reglas."),
                        archivo("gestion/service/PermisoEstructuraService.java", "autorizacion dinamica."),
                        archivo("modelado/repository/PoolRepository.java", "persistencia."),
                        archivo("modelado/model/Pool.java", "entidad."),
                        archivo("modelado/model/TipoParticipante.java", "tipo de participante.")
                },
                new String[] {
                        "Un Pool de caja negra no puede contener estructura interna al publicar.",
                        "Eliminar un Pool con elementos activos se bloquea.",
                        "Los permisos de crear/editar/eliminar Pool se configuran por rol."
                });
    }

    private static WikiPage hu22() {
        return hu("HU-22", "Gestionar lanes", "hu-22-gestionar-lanes",
                "Organiza responsabilidades dentro de un Pool y permite asignar roles y orden visual.",
                new String[] {
                        endpoint("GET", "/api/v1/pools/{poolId}/lanes", "lista lanes."),
                        endpoint("POST", "/api/v1/pools/{poolId}/lanes", "crea lane."),
                        endpoint("GET", "/api/v1/lanes/{id}", "detalle."),
                        endpoint("PUT", "/api/v1/lanes/{id}", "edita nombre/rol."),
                        endpoint("PATCH", "/api/v1/pools/{poolId}/lanes/orden", "reordena lanes."),
                        endpoint("DELETE", "/api/v1/lanes/{id}", "elimina si no contiene nodos activos.")
                },
                new String[] {
                        "LaneController consulta PermisoEstructuraService para cada escritura.",
                        "LaneService valida el Pool y el RolProceso del mismo tenant.",
                        "El campo orden controla la posicion relativa dentro del Pool.",
                        "Reordenar recibe una lista de laneIds y actualiza los valores de orden.",
                        "Antes de eliminar se verifica que no existan NodoFlujo activos."
                },
                new String[] {
                        archivo("modelado/controller/LaneController.java", "CRUD + orden."),
                        archivo("modelado/service/LaneService.java", "reglas."),
                        archivo("modelado/repository/LaneRepository.java", "persistencia."),
                        archivo("gestion/repository/RolProcesoRepository.java", "rol asignado."),
                        archivo("modelado/repository/NodoFlujoRepository.java", "contenido de lane."),
                        archivo("modelado/controller/dto/ReordenarLanesRequest.java", "orden.")
                },
                new String[] {
                        "No se permite estructura interna en Pool de caja negra.",
                        "El rol asignado debe pertenecer a la empresa.",
                        "No se elimina una lane que todavia contiene nodos activos."
                });
    }

    private static WikiPage hu23() {
        return hu("HU-23", "Compartir procesos entre empresas", "hu-23-compartir-procesos",
                "Permite que otra empresa consulte un proceso sin transferir propiedad ni habilitar escritura.",
                new String[] {
                        endpoint("GET", "/api/v1/procesos/{procesoId}/compartidos", "empresas invitadas."),
                        endpoint("POST", "/api/v1/procesos/{procesoId}/compartidos", "comparte proceso."),
                        endpoint("DELETE", "/api/v1/procesos/{procesoId}/compartidos/{empresaInvitadaId}", "revoca."),
                        endpoint("GET", "/api/v1/procesos-compartidos", "procesos recibidos."),
                        endpoint("GET", "/api/v1/procesos-compartidos/{procesoId}", "detalle de solo lectura.")
                },
                new String[] {
                        "ProcesoCompartidoService mantiene la Empresa propietaria del Proceso.",
                        "ProcesoCompartido almacena empresaInvitada, activo y soloLectura=true.",
                        "Al consultar un proceso recibido se arma ProcesoCompartidoDetalle con pools, lanes, nodos, arcos y mensajes activos.",
                        "Revocar acceso cambia activo=false; un registro previo puede reactivarse al compartir nuevamente.",
                        "HistorialCambioService registra compartir y revocar."
                },
                new String[] {
                        archivo("gestion/controller/ProcesoCompartidoController.java", "endpoints."),
                        archivo("gestion/service/ProcesoCompartidoService.java", "reglas y vista agregada."),
                        archivo("gestion/repository/ProcesoCompartidoRepository.java", "permisos persistidos."),
                        archivo("gestion/model/ProcesoCompartido.java", "relacion entre empresas."),
                        archivo("gestion/service/dto/ProcesoCompartidoDetalle.java", "detalle de dominio."),
                        archivo("gestion/controller/dto/ProcesoCompartidoDetalleResponse.java", "JSON.")
                },
                new String[] {
                        "No se puede compartir un proceso con su propia empresa.",
                        "La empresa invitada recibe solo lectura.",
                        "Solo ADMINISTRADOR comparte o revoca.",
                        "El acceso compartido no rompe aislamiento de escritura del tenant."
                });
    }

    private static WikiPage hu24() {
        return hu("HU-24", "Permisos configurables de estructura", "hu-24-permisos-estructura",
                "Configura por empresa y RolAcceso quien puede crear, editar o eliminar Pools y Lanes.",
                new String[] {
                        endpoint("GET", "/api/v1/permisos-estructura", "consulta politica actual."),
                        endpoint("PUT", "/api/v1/permisos-estructura/{rol}", "actualiza politica.")
                },
                new String[] {
                        "PermisoEstructuraController solo es accesible a ADMINISTRADOR.",
                        "PermisoEstructuraService obtiene configuracion persistida o genera defaults por rol.",
                        "PoolController y LaneController llaman validar(...) antes de cada escritura.",
                        "OperacionEstructura identifica CREAR/EDITAR/ELIMINAR para Pool y Lane."
                },
                new String[] {
                        archivo("gestion/controller/PermisoEstructuraController.java", "configuracion."),
                        archivo("gestion/service/PermisoEstructuraService.java", "politica."),
                        archivo("gestion/model/PermisoEstructura.java", "entidad."),
                        archivo("gestion/service/OperacionEstructura.java", "operaciones."),
                        archivo("modelado/controller/PoolController.java", "enforcement."),
                        archivo("modelado/controller/LaneController.java", "enforcement.")
                },
                new String[] {
                        "SOLO_LECTURA no puede recibir permisos de escritura.",
                        "Los permisos son por empresa; una politica de un tenant no afecta otro.",
                        "SecurityConfig y PermisoEstructuraService se complementan: seguridad global + regla dinamica."
                });
    }

    private static WikiPage hu25() {
        return hu("HU-25", "Message Throw", "hu-25-message-throw",
                "Modela el punto desde el cual un Pool envia un mensaje hacia otro participante.",
                new String[] {
                        endpoint("POST", "/api/v1/lanes/{laneId}/eventos-mensaje", "crea EventoMensaje tipo THROW."),
                        endpoint("POST", "/api/v1/procesos/{procesoId}/mensajes", "vincula Throw con un Mensaje."),
                        endpoint("GET/PUT/DELETE", "/api/v1/eventos-mensaje/{id}", "consulta y mantenimiento.")
                },
                new String[] {
                        "EventoMensajeController crea el nodo THROW dentro de una Lane.",
                        "EventoMensajeService valida nombre unico de nodo, Pool interno valido y datos de correlacion.",
                        "MensajeController recibe eventoThrowId y MensajeService resuelve el EventoMensaje activo.",
                        "MensajeService valida que el evento sea realmente THROW, pertenezca al Pool origen y coincida en nombre/correlacion."
                },
                new String[] {
                        archivo("modelado/controller/EventoMensajeController.java", "evento."),
                        archivo("modelado/service/EventoMensajeService.java", "reglas del nodo."),
                        archivo("modelado/controller/MensajeController.java", "mensaje."),
                        archivo("modelado/service/MensajeService.java", "vinculo Throw/Mensaje."),
                        archivo("modelado/model/EventoMensaje.java", "nodo BPMN."),
                        archivo("modelado/model/TipoEventoMensaje.java", "THROW/CATCH.")
                },
                new String[] {
                        "THROW debe estar en el Pool origen.",
                        "Nombre y clave de correlacion deben coincidir con el Mensaje.",
                        "Al publicar, todo Mensaje debe tener un Throw asociado."
                });
    }

    private static WikiPage hu26() {
        return hu("HU-26", "Notificacion a sistema externo", "hu-26-notificacion-externa",
                "Documenta una salida hacia correo, servicio web o cola sin ejecutar realmente la integracion.",
                new String[] {
                        endpoint("POST", "/api/v1/procesos/{procesoId}/mensajes", "crea mensaje con destino externo."),
                        endpoint("PUT", "/api/v1/mensajes/{id}", "actualiza destino/politica de fallo.")
                },
                new String[] {
                        "MensajeRequest incluye tipoDestinoExterno, destinoExterno, politicaFalloNotificacion y actividadErrorId.",
                        "MensajeService exige que el Pool destino sea SISTEMA_EXTERNO y caja negra.",
                        "Se documenta el destino concreto y una PoliticaFalloNotificacion.",
                        "DERIVAR_ACTIVIDAD_ERROR obliga a seleccionar una Actividad activa del mismo proceso.",
                        "La configuracion queda persistida en Mensaje y se expone con MensajeResponse."
                },
                new String[] {
                        archivo("modelado/controller/MensajeController.java", "API."),
                        archivo("modelado/service/MensajeService.java", "validaciones externas."),
                        archivo("modelado/model/Mensaje.java", "configuracion persistida."),
                        archivo("modelado/model/TipoDestinoExterno.java", "CORREO/SERVICIO_WEB/COLA."),
                        archivo("modelado/model/PoliticaFalloNotificacion.java", "comportamiento ante fallo."),
                        archivo("modelado/controller/dto/MensajeRequest.java", "contrato.")
                },
                new String[] {
                        "No se envia realmente correo ni se invoca un servicio externo: se modela el comportamiento.",
                        "Un destino externo no usa Message Catch interno.",
                        "La politica de fallo es obligatoria cuando existe destino externo."
                });
    }

    private static WikiPage hu27() {
        return hu("HU-27", "Message Catch", "hu-27-message-catch",
                "Modela el punto donde un participante recibe un mensaje, ya sea al iniciar o durante un proceso.",
                new String[] {
                        endpoint("POST", "/api/v1/lanes/{laneId}/eventos-mensaje", "crea CATCH_INICIO o CATCH_INTERMEDIO."),
                        endpoint("PUT", "/api/v1/eventos-mensaje/{id}", "cambia datos/tipo del Catch."),
                        endpoint("POST/PUT", "/api/v1/procesos/{procesoId}/mensajes", "vincula el Catch al Mensaje.")
                },
                new String[] {
                        "EventoMensajeService persiste el nodo con TipoEventoMensaje CATCH_INICIO o CATCH_INTERMEDIO.",
                        "Al editar a CATCH_INICIO comprueba que no existan arcos entrantes activos.",
                        "MensajeService valida que el Catch pertenezca al Pool destino.",
                        "Tambien valida coincidencia de nombre y clave de correlacion con el Mensaje."
                },
                new String[] {
                        archivo("modelado/controller/EventoMensajeController.java", "CRUD evento."),
                        archivo("modelado/service/EventoMensajeService.java", "reglas Catch."),
                        archivo("modelado/service/MensajeService.java", "emparejamiento."),
                        archivo("modelado/repository/ArcoRepository.java", "arcos entrantes."),
                        archivo("modelado/model/EventoMensaje.java", "nodo.")
                },
                new String[] {
                        "CATCH_INICIO no puede tener flujo entrante activo.",
                        "Los arcos dados de baja no bloquean el cambio a CATCH_INICIO.",
                        "El Catch debe pertenecer al Pool destino del Mensaje."
                });
    }

    private static WikiPage hu28() {
        return hu("HU-28", "Correlacion de mensajes", "hu-28-correlacion",
                "Define el criterio utilizado para relacionar un mensaje recibido con el caso correcto.",
                new String[] {
                        endpoint("GET", "/api/v1/mensajes/{mensajeId}/correlacion", "consulta correlacion."),
                        endpoint("PUT", "/api/v1/mensajes/{mensajeId}/correlacion", "crea o actualiza."),
                        endpoint("DELETE", "/api/v1/mensajes/{mensajeId}/correlacion", "elimina la definicion.")
                },
                new String[] {
                        "CorrelacionController delega todo en CorrelacionService.",
                        "CorrelacionService obtiene el Mensaje del mismo tenant.",
                        "La clave se valida contra Throw/Catch cuando existen.",
                        "Se evita que dos mensajes del mismo proceso tengan la misma combinacion nombre + clave y generen ambiguedad.",
                        "La entidad Correlacion y el campo claveCorrelacion de Mensaje se mantienen sincronizados."
                },
                new String[] {
                        archivo("modelado/controller/CorrelacionController.java", "API."),
                        archivo("modelado/service/CorrelacionService.java", "reglas."),
                        archivo("modelado/repository/CorrelacionRepository.java", "persistencia."),
                        archivo("modelado/repository/MensajeRepository.java", "deteccion de ambiguedad."),
                        archivo("modelado/model/Correlacion.java", "entidad."),
                        archivo("modelado/model/Mensaje.java", "clave asociada.")
                },
                new String[] {
                        "La correlacion debe ser coherente con los eventos del mensaje.",
                        "La combinacion nombre + clave no puede ser ambigua dentro del proceso.",
                        "Eliminar correlacion limpia tambien la clave almacenada en Mensaje."
                });
    }

    private static WikiPage hu(String codigo, String titulo, String slug, String resumen,
            String[] endpoints, String[] flujo, String[] archivos, String[] reglas) {
        String contenido = """
                <div class="wiki-doc">
                    <p class="wiki-resumen">%s</p>
                    <h2>Endpoints REST</h2>
                    %s
                    <h2>Como lo resolvemos en codigo</h2>
                    <ol>%s</ol>
                    <h2>Archivos involucrados</h2>
                    <ul class="file-list">%s</ul>
                    <h2>Reglas y validaciones importantes</h2>
                    <ul>%s</ul>
                    <div class="wiki-result">
                        <strong>Resultado:</strong> la historia queda expuesta como un contrato REST,
                        con reglas concentradas en Service, aislamiento por tenant y respuestas DTO.
                    </div>
                </div>
                """.formatted(resumen, lista(endpoints), items(flujo), items(archivos), items(reglas));
        return new WikiPage(codigo + " - " + titulo, slug, contenido, CATEGORIA_HU);
    }

    private static WikiPage pagina(String titulo, String slug, String contenido, String categoria) {
        return new WikiPage(titulo, slug, contenido, categoria);
    }

    private static String endpoint(String metodo, String ruta, String descripcion) {
        return "<li><span class=\"http-method\">" + metodo + "</span> <code>" + ruta
                + "</code> - " + descripcion + "</li>";
    }

    private static String archivo(String ruta, String descripcion) {
        String rutaCompleta = ruta.startsWith("src/")
                ? ruta
                : "src/main/java/com/facimus/procesos/" + ruta;
        return "<code>" + rutaCompleta + "</code> - " + descripcion;
    }

    private static String lista(String[] elementos) {
        return "<ul class=\"endpoint-list\">" + items(elementos) + "</ul>";
    }

    private static String items(String[] elementos) {
        return Arrays.stream(elementos)
                .map(elemento -> "<li>" + elemento + "</li>")
                .collect(Collectors.joining());
    }
}
