package com.wiki.beta.controller;

import com.wiki.beta.repository.DocumentoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(DocumentoController.class)
@Import(DocumentoRepository.class)
class DocumentoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void lista_muestraLosOchoDocumentos() throws Exception {
        mockMvc.perform(get("/documentos"))
                .andExpect(status().isOk())
                .andExpect(view().name("documentos"))
                .andExpect(model().attribute("documentos", hasSize(8)))
                .andExpect(content().string(containsString("Resumen ejecutivo del proyecto")))
                .andExpect(content().string(containsString("Roles y manejo de seguridad")))
                .andExpect(content().string(containsString("/documentos/descargas/04_Pruebas_Carga.pdf")));
    }

    @Test
    void visor_muestraElDocumentoConIndiceYDescargas() throws Exception {
        mockMvc.perform(get("/documentos/arquitectura-c4"))
                .andExpect(status().isOk())
                .andExpect(view().name("documento"))
                .andExpect(content().string(containsString("Arquitectura de software - Modelo C4")))
                .andExpect(content().string(containsString("class=\"doc-indice\"")))
                .andExpect(content().string(containsString("Nivel 1 - Contexto")))
                .andExpect(content().string(containsString("/documentos/descargas/02_Arquitectura_C4.docx")));
    }

    @Test
    void visor_enlazaElDocumentoAnteriorYElSiguiente() throws Exception {
        mockMvc.perform(get("/documentos/despliegue"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("/documentos/arquitectura-c4")))
                .andExpect(content().string(containsString("/documentos/pruebas-de-carga")));
    }

    @Test
    void visor_conSlugInexistente_redirigeALaLista() throws Exception {
        mockMvc.perform(get("/documentos/no-existe"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/documentos"));
    }

    @Test
    void descargas_seSirvenComoRecursosEstaticos() throws Exception {
        mockMvc.perform(get("/documentos/descargas/08_Roles_y_Seguridad.pdf"))
                .andExpect(status().isOk());
    }
}
