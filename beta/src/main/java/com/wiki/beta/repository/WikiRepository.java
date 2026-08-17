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
