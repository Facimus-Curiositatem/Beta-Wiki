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
                "El backend Beta expone las 28 historias de usuario mediante una API REST versionada bajo /api/v1. Incluye empresas, usuarios e invitaciones, autenticacion JWT, procesos e historial, actividades, arcos, gateways, roles, pools, lanes, procesos compartidos, permisos de estructura, eventos de mensaje y correlacion. La referencia tecnica completa se encuentra en HISTORIAS_USUARIO_REST.md.",
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
