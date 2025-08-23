package com.tdtsqlscan.web.controller;

import com.tdtsqlscan.web.domain.WhatsNew;
import com.tdtsqlscan.web.service.WhatsNewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/whats-new")
public class AdminWhatsNewController {

    private final WhatsNewService whatsNewService;

    @Value("${tinymce.api.key}")
    private String tinymceApiKey;

    @Autowired
    public AdminWhatsNewController(WhatsNewService whatsNewService) {
        this.whatsNewService = whatsNewService;
    }

    @GetMapping
    public String listWhatsNew(Model model) {
        model.addAttribute("whatsnews", whatsNewService.findAll());
        return "admin/whats-new/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("whatsnew", new WhatsNew());
        model.addAttribute("tinymceApiKey", tinymceApiKey);
        return "admin/whats-new/form";
    }

    @PostMapping
    public String createWhatsNew(@ModelAttribute WhatsNew whatsNew, RedirectAttributes redirectAttributes) {
        whatsNewService.save(whatsNew);
        redirectAttributes.addFlashAttribute("successMessage", "What's New created successfully!");
        return "redirect:/admin/whats-new";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        return whatsNewService.findById(id)
                .map(whatsNew -> {
                    model.addAttribute("whatsnew", whatsNew);
                    model.addAttribute("tinymceApiKey", tinymceApiKey);
                    return "admin/whats-new/form";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("errorMessage", "What's New not found!");
                    return "redirect:/admin/whats-new";
                });
    }

    @PostMapping("/edit/{id}")
    public String updateWhatsNew(@PathVariable Long id, @ModelAttribute WhatsNew whatsNew, RedirectAttributes redirectAttributes) {
        return whatsNewService.findById(id)
                .map(existingWhatsNew -> {
                    existingWhatsNew.setTitle(whatsNew.getTitle());
                    existingWhatsNew.setContent(whatsNew.getContent());
                    whatsNewService.save(existingWhatsNew);
                    redirectAttributes.addFlashAttribute("successMessage", "What's New updated successfully!");
                    return "redirect:/admin/whats-new";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("errorMessage", "What's New not found!");
                    return "redirect:/admin/whats-new";
                });
    }

    @PostMapping("/delete/{id}")
    public String deleteWhatsNew(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        whatsNewService.deleteById(id);
        redirectAttributes.addFlashAttribute("successMessage", "What's New deleted successfully!");
        return "redirect:/admin/whats-new";
    }
}
