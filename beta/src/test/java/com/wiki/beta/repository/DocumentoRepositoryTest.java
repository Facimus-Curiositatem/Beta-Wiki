package com.wiki.beta.repository;

import com.wiki.beta.model.DocumentoEntrega;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

class DocumentoRepositoryTest {

    private static final Pattern IMAGEN = Pattern.compile("src=\"img/([^\"]+)\"");

    private DocumentoRepository repositorio;

    @BeforeEach
    void setUp() {
        repositorio = new DocumentoRepository();
        repositorio.init();
    }

    @Test
    void findAll_devuelveLosOchoDocumentosEnOrden() {
        List<String> numeros = repositorio.findAll().stream().map(DocumentoEntrega::getNumero).toList();

        assertThat(numeros).containsExactly("01", "02", "03", "04", "05", "06", "07", "08");
    }

    @Test
    void cadaDocumento_tieneContenidoConIndiceYCuerpo() {
        assertThat(repositorio.findAll()).allSatisfy(documento -> {
            assertThat(documento.getContenido()).contains("class=\"doc-indice\"", "class=\"doc-cuerpo\"");
            assertThat(documento.getContenido()).doesNotContain("[Completar", "[Escribir");
        });
    }

    @Test
    void cadaDocumento_tieneSusDescargasYSusImagenes() {
        assertThat(repositorio.findAll()).allSatisfy(documento -> {
            assertThat(new ClassPathResource("static/documentos/descargas/" + documento.getArchivo() + ".pdf").exists())
                    .as("PDF de %s", documento.getArchivo()).isTrue();
            assertThat(new ClassPathResource("static/documentos/descargas/" + documento.getArchivo() + ".docx").exists())
                    .as("Word de %s", documento.getArchivo()).isTrue();
            Matcher imagen = IMAGEN.matcher(documento.getContenido());
            while (imagen.find()) {
                assertThat(new ClassPathResource("static/documentos/img/" + imagen.group(1)).exists())
                        .as("imagen %s", imagen.group(1)).isTrue();
            }
        });
    }

    @Test
    void findBySlug_encuentraSoloSlugsExistentes() {
        assertThat(repositorio.findBySlug("roles-y-seguridad")).map(DocumentoEntrega::getNumero).contains("08");
        assertThat(repositorio.findBySlug("no-existe")).isEmpty();
    }

    @Test
    void anteriorYSiguiente_respetanLosExtremos() {
        DocumentoEntrega primero = repositorio.findBySlug("resumen-ejecutivo").orElseThrow();
        DocumentoEntrega ultimo = repositorio.findBySlug("roles-y-seguridad").orElseThrow();

        assertThat(repositorio.anterior(primero)).isEmpty();
        assertThat(repositorio.siguiente(primero)).map(DocumentoEntrega::getSlug).contains("arquitectura-c4");
        assertThat(repositorio.anterior(ultimo)).map(DocumentoEntrega::getSlug).contains("pruebas-automatizadas");
        assertThat(repositorio.siguiente(ultimo)).isEmpty();
    }
}
