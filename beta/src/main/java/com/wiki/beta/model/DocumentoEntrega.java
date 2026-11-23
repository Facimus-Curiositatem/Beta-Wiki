package com.wiki.beta.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

/** Documento de la entrega final que el visor muestra en HTML y ofrece para descargar en PDF y Word. */
@Getter
@AllArgsConstructor
public class DocumentoEntrega {

    private final String numero;
    private final String slug;
    private final String titulo;
    private final String descripcion;
    /** Nombre base de los archivos: fragmento HTML, PDF y Word comparten este nombre. */
    private final String archivo;
    private final int paginas;
    private final String contenido;
}
