package com.wiki.beta.controller;

import com.wiki.beta.repository.WikiRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class WikiController {

    private final WikiRepository wikiRepository;

    public WikiController(WikiRepository wikiRepository) {
        this.wikiRepository = wikiRepository;
    }

    @GetMapping("/")
    public String inicio(Model model) {
        model.addAttribute("paginas", wikiRepository.findAll());
        model.addAttribute("categorias", wikiRepository.findAllCategories());
        return "inicio";
    }

    @GetMapping("/secciones")
    public String secciones(Model model) {
        model.addAttribute("paginas", wikiRepository.findAll());
        model.addAttribute("categorias", wikiRepository.findAllCategories());
        return "secciones";
    }

    @GetMapping("/secciones/{slug}")
    public String detalle(@PathVariable String slug, Model model) {
        var pagina = wikiRepository.findBySlug(slug);
        if (pagina.isEmpty()) {
            return "redirect:/secciones";
        }
        model.addAttribute("pagina", pagina.get());
        return "detalle";
    }

    @GetMapping("/contactenos")
    public String contactenos() {
        return "contactenos";
    }

    @PostMapping("/contactenos")
    public String contactenosPost(@RequestParam String nombre,
                                  @RequestParam String email,
                                  @RequestParam String telefono,
                                  @RequestParam String asunto,
                                  @RequestParam String mensaje,
                                  RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("enviado", true);
        redirectAttributes.addFlashAttribute("nombreUsuario", nombre);
        return "redirect:/contactenos";
    }
}
