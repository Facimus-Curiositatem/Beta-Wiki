package com.wiki.beta.repository;

import com.wiki.beta.model.WikiPage;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class WikiRepository {

    private static final Logger log = LoggerFactory.getLogger(WikiRepository.class);

    /** Carpeta del classpath donde Maven copia ArchProposalDaza/docs (ver pom.xml). */
    private static final String CARPETA_DOCS = "/docs/";

    /** Orden en que aparecen las categorias de documentos en la wiki. */
    private static final Map<String, Integer> ORDEN_CATEGORIAS = Map.of(
            "Documentacion del proyecto", 0,
            "Decisiones de arquitectura (ADR)", 1,
            "Diagramas", 2);

    /** Documentos que se muestran en la pagina de inicio. */
    private static final List<String> DESTACADOS = List.of(
            "cobertura-hu", "diseno-api-rest", "seguridad",
            "modelo-datos", "diagrama-clases", "trazabilidad-hu-codigo");

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
        pages.addAll(cargarDocumentos());
    }

    /** Lee todos los .md de classpath:docs/ y los convierte en paginas. */
    private List<WikiPage> cargarDocumentos() {
        List<WikiPage> documentos = new ArrayList<>();
        try {
            Resource[] recursos = new PathMatchingResourcePatternResolver()
                    .getResources("classpath*:docs/**/*.md");
            for (Resource recurso : recursos) {
                String url = recurso.getURL().toString();
                String ruta = URLDecoder.decode(
                        url.substring(url.lastIndexOf(CARPETA_DOCS) + CARPETA_DOCS.length()), StandardCharsets.UTF_8);
                String markdown = recurso.getContentAsString(StandardCharsets.UTF_8);
                documentos.add(DocumentoParser.aPagina(ruta, markdown));
            }
        } catch (IOException e) {
            log.error("No se pudieron leer los documentos de la wiki", e);
        }
        documentos.sort(Comparator
                .comparing((WikiPage p) -> ORDEN_CATEGORIAS.getOrDefault(p.getCategoria(), 99))
                .thenComparing(WikiPage::getSlug));
        log.info("Documentos cargados en la wiki: {}", documentos.size());
        return documentos;
    }

    public List<WikiPage> findAll() {
        return pages;
    }

    public List<WikiPage> findDestacadas() {
        return DESTACADOS.stream()
                .map(this::findBySlug)
                .flatMap(Optional::stream)
                .toList();
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