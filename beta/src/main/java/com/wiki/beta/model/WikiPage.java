package com.wiki.beta.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WikiPage {

    private String titulo;
    private String slug;
    private String contenido;
    private String categoria;
}
