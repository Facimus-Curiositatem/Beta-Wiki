package com.wiki.beta.repository;

import com.wiki.beta.model.WikiPage;
import org.springframework.stereotype.Repository;

import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class WikiRepository {

    private final List<WikiPage> pages = new ArrayList<>();

    @PostConstruct
    public void init() {
        pages.add(new WikiPage(
                "Spring Boot",
                "spring-boot",
                "Spring Boot es un framework de Java que simplifica la creacion de aplicaciones web. Permite configurar proyectos rapidamente con dependencias predefinidas y un servidor embebido.",
                "Frameworks"
        ));
        pages.add(new WikiPage(
                "Thymeleaf",
                "thymeleaf",
                "Thymeleaf es un motor de plantillas para Java que permite generar vistas HTML del lado del servidor. Se integra nativamente con Spring Boot y soporta fragmentos reutilizables.",
                "Frameworks"
        ));
        pages.add(new WikiPage(
                "Arquitectura MVC",
                "arquitectura-mvc",
                "El patron Modelo-Vista-Controlador separa la logica de negocio, la presentacion y el control de flujo. En Spring Boot, los controladores manejan las peticiones, los modelos transportan datos y Thymeleaf renderiza las vistas.",
                "Arquitectura"
        ));
        pages.add(new WikiPage(
                "Control de Versiones",
                "control-de-versiones",
                "Git es el sistema de control de versiones mas utilizado. Permite trabajar en equipo con ramas independientes y fusionar cambios de forma controlada mediante pull requests.",
                "Herramientas"
        ));
        pages.add(new WikiPage(
                "API REST e Historias de Usuario",
                "api-rest-historias-usuario",
                "El backend Beta expone las 28 historias de usuario mediante una API REST versionada bajo /api/v1. Esta seccion funciona como indice general; cada HU tiene ademas su propia pagina navegable con su flujo y endpoints principales.",
                "Proyecto Beta"
        ));
        pages.add(new WikiPage(
                "Seguridad, multitenencia y permisos",
                "seguridad-multitenencia",
                "Cada solicitud autenticada obtiene usuario, empresa y rol desde el JWT. El backend deriva el tenant desde ApiPrincipal y evita confiar en identificadores de empresa enviados por el cliente. Los procesos compartidos son de solo lectura y los permisos para modificar pools y lanes se configuran por rol de acceso y empresa.",
                "Proyecto Beta"
        ));
        pages.add(new WikiPage(
                "Modelado BPMN y mensajeria",
                "modelado-bpmn-mensajeria",
                "El modelo REST incluye actividades tipadas, arcos con baja logica, gateways exclusivos, paralelos e inclusivos, pools, lanes y eventos de mensaje THROW, CATCH_INICIO y CATCH_INTERMEDIO. Los mensajes soportan correlacion, destinos externos y politicas de fallo. El sistema modela y valida estos flujos, pero no ejecuta procesos BPMN reales.",
                "Proyecto Beta"
        ));

        pages.add(new WikiPage(
                "HU-01 - Registro de empresa",
                "hu-01-registro-empresa",
                "POST /api/v1/empresas registra una empresa, valida su identificacion unica y crea el contexto inicial de administracion. La empresa queda como tenant de sus usuarios y procesos.",
                "Historias de Usuario REST"
        ));
        pages.add(new WikiPage(
                "HU-02 - Gestion de usuarios",
                "hu-02-gestion-usuarios",
                "POST /api/v1/usuarios permite al administrador crear colaboradores. Tambien existe el flujo de invitaciones con POST /api/v1/usuarios/invitaciones y aceptacion mediante token. Las contrasenas se almacenan con BCrypt.",
                "Historias de Usuario REST"
        ));
        pages.add(new WikiPage(
                "HU-03 - Inicio de sesion",
                "hu-03-inicio-sesion",
                "POST /api/v1/auth/login valida credenciales y entrega un JWT. El backend es stateless: las solicitudes posteriores usan Bearer Token y el tenant se obtiene del principal autenticado.",
                "Historias de Usuario REST"
        ));
        pages.add(new WikiPage(
                "HU-04 - Crear proceso",
                "hu-04-crear-proceso",
                "POST /api/v1/procesos crea un proceso en estado BORRADOR, activo y asociado a la empresa autenticada. Tambien crea la estructura inicial necesaria del proceso.",
                "Historias de Usuario REST"
        ));
        pages.add(new WikiPage(
                "HU-05 - Editar proceso",
                "hu-05-editar-proceso",
                "PUT /api/v1/procesos/{id} actualiza metadatos y PATCH /api/v1/procesos/{id} gestiona cambios parciales o de estado. Los cambios relevantes quedan registrados en historial.",
                "Historias de Usuario REST"
        ));
        pages.add(new WikiPage(
                "HU-06 - Eliminar proceso",
                "hu-06-eliminar-proceso",
                "DELETE /api/v1/procesos/{id} aplica baja logica. El proceso deja de aparecer en consultas normales, pero conserva trazabilidad y datos historicos.",
                "Historias de Usuario REST"
        ));
        pages.add(new WikiPage(
                "HU-07 - Consultar procesos",
                "hu-07-consultar-procesos",
                "GET /api/v1/procesos soporta consulta y filtros. GET /api/v1/procesos/{id}, /diagrama e /historial exponen el detalle, el modelo completo y la trazabilidad del proceso.",
                "Historias de Usuario REST"
        ));
        pages.add(new WikiPage(
                "HU-08 - Crear actividad",
                "hu-08-crear-actividad",
                "POST /api/v1/lanes/{laneId}/actividades crea una actividad dentro de una lane. Valida nombre unico dentro del proceso, tenant, pool valido y tipo de actividad.",
                "Historias de Usuario REST"
        ));
        pages.add(new WikiPage(
                "HU-09 - Editar actividad",
                "hu-09-editar-actividad",
                "PUT /api/v1/actividades/{id} permite modificar nombre, descripcion, tipo, posicion y lane segun el contrato. El backend conserva consistencia y registra auditoria.",
                "Historias de Usuario REST"
        ));
        pages.add(new WikiPage(
                "HU-10 - Eliminar actividad",
                "hu-10-eliminar-actividad",
                "GET /api/v1/actividades/{id}/impacto-eliminacion muestra advertencias antes de eliminar. DELETE /api/v1/actividades/{id} aplica baja logica y desactiva arcos relacionados.",
                "Historias de Usuario REST"
        ));
        pages.add(new WikiPage(
                "HU-11 - Crear arco",
                "hu-11-crear-arco",
                "POST /api/v1/arcos conecta dos nodos activos del mismo pool. No permite loops sobre el mismo nodo ni arcos duplicados, y exige condicion cuando el origen es un gateway exclusivo o inclusivo.",
                "Historias de Usuario REST"
        ));
        pages.add(new WikiPage(
                "HU-12 - Editar arco",
                "hu-12-editar-arco",
                "PUT /api/v1/arcos/{id} permite modificar etiqueta, condicion, origen y destino. Se vuelven a ejecutar las validaciones de coherencia del arco.",
                "Historias de Usuario REST"
        ));
        pages.add(new WikiPage(
                "HU-13 - Eliminar arco",
                "hu-13-eliminar-arco",
                "GET /api/v1/arcos/{id}/impacto-eliminacion informa si quedaran nodos sin entrada o salida. DELETE /api/v1/arcos/{id} aplica baja logica.",
                "Historias de Usuario REST"
        ));
        pages.add(new WikiPage(
                "HU-14 - Crear gateway",
                "hu-14-crear-gateway",
                "POST /api/v1/lanes/{laneId}/gateways crea gateways EXCLUSIVO, PARALELO o INCLUSIVO. El gateway queda asociado a una lane y al tenant autenticado.",
                "Historias de Usuario REST"
        ));
        pages.add(new WikiPage(
                "HU-15 - Editar gateway",
                "hu-15-editar-gateway",
                "PUT /api/v1/gateways/{id} cambia datos o tipo del gateway. Las validaciones consideran solo arcos activos y revisan las condiciones requeridas por gateways exclusivos e inclusivos.",
                "Historias de Usuario REST"
        ));
        pages.add(new WikiPage(
                "HU-16 - Eliminar gateway",
                "hu-16-eliminar-gateway",
                "GET /api/v1/gateways/{id}/impacto-eliminacion muestra impacto sobre la ramificacion. DELETE /api/v1/gateways/{id} realiza baja logica y desactiva conexiones relacionadas.",
                "Historias de Usuario REST"
        ));
        pages.add(new WikiPage(
                "HU-17 - Crear rol de proceso",
                "hu-17-crear-rol",
                "POST /api/v1/roles crea un rol reutilizable dentro de la empresa. El nombre debe ser unico entre roles activos del tenant.",
                "Historias de Usuario REST"
        ));
        pages.add(new WikiPage(
                "HU-18 - Editar rol de proceso",
                "hu-18-editar-rol",
                "PUT /api/v1/roles/{id} actualiza nombre y descripcion del rol. Las lanes que lo usan mantienen la referencia y el cambio puede registrarse en historial.",
                "Historias de Usuario REST"
        ));
        pages.add(new WikiPage(
                "HU-19 - Eliminar rol de proceso",
                "hu-19-eliminar-rol",
                "DELETE /api/v1/roles/{id} solo permite eliminar un rol que no este siendo usado por lanes. La eliminacion es logica.",
                "Historias de Usuario REST"
        ));
        pages.add(new WikiPage(
                "HU-20 - Consultar roles",
                "hu-20-consultar-roles",
                "GET /api/v1/roles y GET /api/v1/roles/consulta permiten listar, buscar y paginar roles. La consulta avanzada incluye los procesos donde se usa cada rol.",
                "Historias de Usuario REST"
        ));
        pages.add(new WikiPage(
                "HU-21 - Configurar pool",
                "hu-21-configurar-pool",
                "GET y POST /api/v1/procesos/{procesoId}/pools gestionan pools del proceso. GET, PUT y DELETE /api/v1/pools/{id} permiten consultar, editar y eliminar respetando caja negra, participante y permisos.",
                "Historias de Usuario REST"
        ));
        pages.add(new WikiPage(
                "HU-22 - Gestionar lanes",
                "hu-22-gestionar-lanes",
                "GET y POST /api/v1/pools/{poolId}/lanes administran lanes. GET, PUT y DELETE /api/v1/lanes/{id} operan una lane y PATCH /api/v1/pools/{poolId}/lanes/orden permite reordenarlas.",
                "Historias de Usuario REST"
        ));
        pages.add(new WikiPage(
                "HU-23 - Compartir procesos",
                "hu-23-compartir-procesos",
                "POST /api/v1/procesos/{procesoId}/compartidos comparte un proceso con otra empresa en solo lectura. DELETE revoca el acceso y GET /api/v1/procesos-compartidos lista los procesos recibidos.",
                "Historias de Usuario REST"
        ));
        pages.add(new WikiPage(
                "HU-24 - Permisos de estructura",
                "hu-24-permisos-estructura",
                "GET /api/v1/permisos-estructura consulta permisos por rol y PUT /api/v1/permisos-estructura/{rol} los modifica. SOLO_LECTURA nunca puede recibir permisos de escritura.",
                "Historias de Usuario REST"
        ));
        pages.add(new WikiPage(
                "HU-25 - Message Throw",
                "hu-25-message-throw",
                "Los eventos de mensaje se crean con POST /api/v1/lanes/{laneId}/eventos-mensaje. Un THROW debe pertenecer al pool origen y coincidir con el mensaje en nombre y correlacion.",
                "Historias de Usuario REST"
        ));
        pages.add(new WikiPage(
                "HU-26 - Notificacion externa",
                "hu-26-notificacion-externa",
                "Los mensajes pueden apuntar a CORREO, SERVICIO_WEB o COLA cuando el pool destino representa un SISTEMA_EXTERNO de caja negra. Se modela tambien la politica de fallo y una actividad de error opcional.",
                "Historias de Usuario REST"
        ));
        pages.add(new WikiPage(
                "HU-27 - Message Catch",
                "hu-27-message-catch",
                "Los eventos CATCH_INICIO y CATCH_INTERMEDIO usan los endpoints de eventos de mensaje. Un CATCH_INICIO no puede tener arcos entrantes activos.",
                "Historias de Usuario REST"
        ));
        pages.add(new WikiPage(
                "HU-28 - Correlacion de mensajes",
                "hu-28-correlacion",
                "GET, PUT y DELETE /api/v1/mensajes/{mensajeId}/correlacion administran la clave de correlacion. El backend evita combinaciones ambiguas de nombre y clave dentro del mismo proceso.",
                "Historias de Usuario REST"
        ));
    }

    public List<WikiPage> findAll() {
        return pages;
    }

    public Optional<WikiPage> findBySlug(String slug) {
        return pages.stream()
                .filter(page -> page.getSlug().equals(slug))
                .findFirst();
    }

    public List<String> findAllCategories() {
        return pages.stream()
                .map(WikiPage::getCategoria)
                .distinct()
                .toList();
    }

    public List<WikiPage> findByCategory(String categoria) {
        return pages.stream()
                .filter(page -> page.getCategoria().equals(categoria))
                .toList();
    }
}
