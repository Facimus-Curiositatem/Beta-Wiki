package com.wiki.beta.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class WikiPage {

    private static final int MAX_RESUMEN = 180;

    private String titulo;
    private String slug;
    private String resumen;
    private String contenido;
    private String categoria;

    public WikiPage(String titulo, String slug, String contenido, String categoria) {
        this.titulo = titulo;
        this.slug = slug;
        this.contenido = contenido;
        this.categoria = categoria;
        this.resumen = crearResumen(contenido);
    }

    private static String crearResumen(String contenido) {
        if (contenido == null) {
            return "";
        }
        String texto = contenido
                .replaceAll("<[^>]+>", " ")
                .replace("&rarr;", " -> ")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&amp;", "&")
                .replaceAll("\\s+", " ")
                .trim();
        if (texto.length() <= MAX_RESUMEN) {
            return texto;
        }
        return texto.substring(0, MAX_RESUMEN - 3).trim() + "...";
    }
}
