package com.wiki.beta.repository;

import com.wiki.beta.model.WikiPage;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public final class DocumentoParser {

    private static final int LARGO_RESUMEN = 180;
    private static final int LARGO_MINIMO_PARRAFO = 40;

    private DocumentoParser() {
    }

    /**
     * @param ruta     ruta relativa a la carpeta docs, con "/" como separador (ej. "adr/ADR-004-migracion-rest-jwt.md")
     * @param markdown contenido completo del archivo
     */
    public static WikiPage aPagina(String ruta, String markdown) {
        String texto = markdown.replace("\r\n", "\n");
        List<String> lineas = new ArrayList<>(List.of(texto.split("\n", -1)));

        String titulo = null;
        for (int i = 0; i < lineas.size(); i++) {
            String linea = lineas.get(i).trim();
            if (linea.isEmpty()) {
                continue;
            }
            if (linea.startsWith("# ")) {
                titulo = limpiar(linea.substring(2));
                lineas.remove(i);
            }
            break;
        }
        String cuerpo = String.join("\n", lineas).strip();
        if (titulo == null) {
            // Sin encabezado "# ": se usa la primera linea como titulo y no se repite en el resumen.
            titulo = primeraLineaCorta(lineas, slug(ruta));
            String tituloUsado = titulo;
            lineas.removeIf(l -> limpiar(l).equals(tituloUsado));
        }
        return new WikiPage(titulo, slug(ruta), resumen(lineas), categoria(ruta), cuerpo);
    }

    /** "adr/ADR-004-migracion-rest-jwt.md" -> "adr-004-migracion-rest-jwt". */
    public static String slug(String ruta) {
        String nombre = ruta.substring(ruta.lastIndexOf('/') + 1);
        if (nombre.toLowerCase(Locale.ROOT).endsWith(".md")) {
            nombre = nombre.substring(0, nombre.length() - 3);
        }
        return nombre.toLowerCase(Locale.ROOT);
    }

    public static String categoria(String ruta) {
        if (ruta.startsWith("adr/")) {
            return "Decisiones de arquitectura (ADR)";
        }
        if (ruta.startsWith("diagrams/")) {
            return "Diagramas";
        }
        return "Documentacion del proyecto";
    }

    /** Primer parrafo de "## Contexto" si existe; si no, el primer parrafo con contenido suficiente. */
    static String resumen(List<String> lineas) {
        List<String> parrafos = parrafos(lineas, true);
        if (parrafos.isEmpty()) {
            parrafos = parrafos(lineas, false);
        }
        String elegido = parrafos.stream()
                .filter(p -> p.length() >= LARGO_MINIMO_PARRAFO)
                .findFirst()
                .orElse(parrafos.isEmpty() ? "" : parrafos.get(0));
        return recortar(elegido);
    }

    private static List<String> parrafos(List<String> lineas, boolean soloContexto) {
        List<String> parrafos = new ArrayList<>();
        StringBuilder actual = new StringBuilder();
        boolean enCodigo = false;
        boolean enContexto = !soloContexto;

        for (String cruda : lineas) {
            String linea = cruda.trim();
            if (linea.startsWith("```")) {
                enCodigo = !enCodigo;
                cerrar(actual, parrafos);
                continue;
            }
            if (enCodigo) {
                continue;
            }
            if (linea.startsWith("#")) {
                cerrar(actual, parrafos);
                if (soloContexto) {
                    if (enContexto) {
                        break;
                    }
                    enContexto = linea.toLowerCase(Locale.ROOT).matches("#+\\s+contexto\\s*");
                }
                continue;
            }
            boolean noEsTexto = linea.isEmpty() || linea.startsWith("|") || linea.startsWith(">")
                    || linea.startsWith("<") || linea.startsWith("---") || linea.startsWith("- ")
                    || linea.startsWith("* ") || linea.matches("\\d+\\.\\s.*");
            if (noEsTexto) {
                cerrar(actual, parrafos);
                continue;
            }
            if (enContexto) {
                if (!actual.isEmpty()) {
                    actual.append(' ');
                }
                actual.append(linea);
            }
        }
        cerrar(actual, parrafos);
        return parrafos;
    }

    private static void cerrar(StringBuilder actual, List<String> parrafos) {
        if (!actual.isEmpty()) {
            parrafos.add(limpiar(actual.toString()));
            actual.setLength(0);
        }
    }

    private static String primeraLineaCorta(List<String> lineas, String porDefecto) {
        return lineas.stream()
                .map(String::trim)
                .filter(l -> !l.isEmpty())
                .findFirst()
                .filter(l -> l.length() <= 80)
                .map(DocumentoParser::limpiar)
                .orElse(porDefecto);
    }

    /** Quita la sintaxis Markdown mas comun para mostrar texto plano. */
    static String limpiar(String texto) {
        return texto
                .replaceAll("!?\\[([^\\]]*)]\\([^)]*\\)", "$1")
                .replaceAll("[`*]", "")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private static String recortar(String texto) {
        if (texto.length() <= LARGO_RESUMEN) {
            return texto;
        }
        int corte = texto.lastIndexOf(' ', LARGO_RESUMEN);
        return texto.substring(0, corte > 0 ? corte : LARGO_RESUMEN) + "...";
    }
}