package com.wiki.beta.controller;

import com.wiki.beta.repository.DocumentoRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class DocumentoController {

    private final DocumentoRepository documentoRepository;

    public DocumentoController(DocumentoRepository documentoRepository) {
        this.documentoRepository = documentoRepository;
    }

    @GetMapping("/documentos")
    public String documentos(Model model) {
        model.addAttribute("documentos", documentoRepository.findAll());
        return "documentos";
    }

    @GetMapping("/documentos/{slug}")
    public String documento(@PathVariable String slug, Model model) {
        var documento = documentoRepository.findBySlug(slug);
        if (documento.isEmpty()) {
            return "redirect:/documentos";
        }
        model.addAttribute("documento", documento.get());
        model.addAttribute("anterior", documentoRepository.anterior(documento.get()).orElse(null));
        model.addAttribute("siguiente", documentoRepository.siguiente(documento.get()).orElse(null));
        return "documento";
    }
}
