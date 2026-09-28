package com.wiki.beta.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class WikiPage {

    private String titulo;
    private String slug;
    /** Texto corto que se muestra en las tarjetas. */
    private String contenido;
    private String categoria;
    /** Documento completo en Markdown. Es null en las paginas escritas a mano. */
    private String markdown;

    public WikiPage(String titulo, String slug, String contenido, String categoria) {
        this(titulo, slug, contenido, categoria, null);
    }

    public WikiPage(String titulo, String slug, String contenido, String categoria, String markdown) {
        this.titulo = titulo;
        this.slug = slug;
        this.contenido = contenido;
        this.categoria = categoria;
        this.markdown = markdown;
    }

    /** true si la pagina viene de un archivo .md de ArchProposalDaza/docs. */
    public boolean isDocumento() {
        return markdown != null;
    }
}