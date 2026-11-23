package com.wiki.beta.repository;

import com.wiki.beta.model.DocumentoEntrega;
import jakarta.annotation.PostConstruct;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class DocumentoRepository {

    private static final String CARPETA_FRAGMENTOS = "documentos/";

    private final List<DocumentoEntrega> documentos = new ArrayList<>();

    @PostConstruct
    public void init() {
        agregar("01", "resumen-ejecutivo", "Resumen ejecutivo del proyecto",
                "Problema, alcance, solución y resumen consolidado de las pruebas y hallazgos del proyecto.",
                "01_Resumen_Ejecutivo", 4);
        agregar("02", "arquitectura-c4", "Arquitectura de software - Modelo C4",
                "Arquitectura del sistema en los cuatro niveles del modelo C4 y decisiones de diseño.",
                "02_Arquitectura_C4", 6);
        agregar("03", "despliegue", "Guía de despliegue",
                "Ambientes, configuración, pipeline de CI/CD, operación y evidencias del despliegue.",
                "03_Despliegue", 6);
        agregar("04", "pruebas-de-carga", "Detalle de pruebas de carga y rendimiento",
                "Escenarios JMeter con hasta 50 usuarios, latencias, throughput y consumo de CPU y memoria.",
                "04_Pruebas_Carga", 7);
        agregar("05", "pruebas-funcionales", "Detalle de pruebas funcionales - Frontend y Backend",
                "Casos funcionales del frontend y del backend, ejecución y registro de defectos.",
                "05_Pruebas_Funcionales", 6);
        agregar("06", "pruebas-unitarias-y-sonarqube", "Detalle de pruebas unitarias y análisis con SonarQube",
                "Pruebas JUnit y Karma, cobertura y resultados del análisis estático en SonarCloud.",
                "06_Unitarias_SonarQube", 6);
        agregar("07", "pruebas-automatizadas", "Detalle de pruebas automatizadas",
                "Suites de Selenium, Newman, JMeter y JUnit, su ejecución en CI y sus resultados.",
                "07_Pruebas_Automatizadas", 6);
        agregar("08", "roles-y-seguridad", "Roles y manejo de seguridad",
                "Roles, matriz de permisos, autenticación con JWT, controles del backend y pruebas de seguridad.",
                "08_Roles_y_Seguridad", 7);
    }

    private void agregar(String numero, String slug, String titulo, String descripcion, String archivo, int paginas) {
        documentos.add(new DocumentoEntrega(numero, slug, titulo, descripcion, archivo, paginas, leerFragmento(archivo)));
    }

    private static String leerFragmento(String archivo) {
        ClassPathResource recurso = new ClassPathResource(CARPETA_FRAGMENTOS + archivo + ".html");
        try (InputStream entrada = recurso.getInputStream()) {
            return new String(entrada.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo leer el documento " + archivo, e);
        }
    }

    public List<DocumentoEntrega> findAll() {
        return List.copyOf(documentos);
    }

    public Optional<DocumentoEntrega> findBySlug(String slug) {
        return documentos.stream()
                .filter(documento -> documento.getSlug().equals(slug))
                .findFirst();
    }

    public Optional<DocumentoEntrega> anterior(DocumentoEntrega documento) {
        int posicion = documentos.indexOf(documento);
        return posicion > 0 ? Optional.of(documentos.get(posicion - 1)) : Optional.empty();
    }

    public Optional<DocumentoEntrega> siguiente(DocumentoEntrega documento) {
        int posicion = documentos.indexOf(documento);
        return posicion >= 0 && posicion < documentos.size() - 1
                ? Optional.of(documentos.get(posicion + 1))
                : Optional.empty();
    }
}
