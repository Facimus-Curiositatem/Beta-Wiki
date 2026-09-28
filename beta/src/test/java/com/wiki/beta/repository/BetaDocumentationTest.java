package com.wiki.beta.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class BetaDocumentationTest {

    @Test
    void documenta_las_28_historias_de_usuario() {
        var pages = BetaDocumentation.pages();

        long historias = pages.stream()
                .filter(page -> "Historias de Usuario REST".equals(page.getCategoria()))
                .count();

        assertEquals(28, historias);
        assertTrue(pages.stream().anyMatch(page -> "hu-01-registro-empresa".equals(page.getSlug())));
        assertTrue(pages.stream().anyMatch(page -> "hu-28-correlacion".equals(page.getSlug())));
    }

    @Test
    void cada_historia_incluye_flujo_archivos_y_reglas() {
        BetaDocumentation.pages().stream()
                .filter(page -> "Historias de Usuario REST".equals(page.getCategoria()))
                .forEach(page -> {
                    assertTrue(page.getContenido().contains("Endpoints REST"), page.getSlug());
                    assertTrue(page.getContenido().contains("Como lo resolvemos en codigo"), page.getSlug());
                    assertTrue(page.getContenido().contains("Archivos involucrados"), page.getSlug());
                    assertTrue(page.getContenido().contains("Reglas y validaciones importantes"), page.getSlug());
                });
    }

    @Test
    void pagina_api_explica_la_arquitectura_rest() {
        var api = BetaDocumentation.pages().stream()
                .filter(page -> "api-rest-historias-usuario".equals(page.getSlug()))
                .findFirst()
                .orElseThrow();

        assertTrue(api.getContenido().contains("JwtAuthenticationFilter"));
        assertTrue(api.getContenido().contains("ApiPrincipal"));
        assertTrue(api.getContenido().contains("Controller"));
        assertTrue(api.getContenido().contains("Service"));
        assertTrue(api.getContenido().contains("Repository"));
        assertTrue(api.getContenido().contains("ProblemDetail"));
        assertTrue(api.getContenido().contains("PageResponse"));
    }
}
