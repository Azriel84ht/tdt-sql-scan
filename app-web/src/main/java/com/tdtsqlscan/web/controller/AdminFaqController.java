package com.tdtsqlscan.web.controller;

import com.tdtsqlscan.web.domain.Faq;
import com.tdtsqlscan.web.service.FaqService;
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
@RequestMapping("/admin/faqs")
public class AdminFaqController {

    private final FaqService faqService;

    @Value("${tinymce.api.key}")
    private String tinymceApiKey;

    @Autowired
    public AdminFaqController(FaqService faqService) {
        this.faqService = faqService;
    }

    @GetMapping
    public String listFaqs(Model model) {
        model.addAttribute("faqs", faqService.findAll());
        return "admin/faqs/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("faq", new Faq());
        model.addAttribute("tinymceApiKey", tinymceApiKey);
        return "admin/faqs/form";
    }

    @PostMapping
    public String createFaq(@ModelAttribute Faq faq, RedirectAttributes redirectAttributes) {
        faqService.save(faq);
        redirectAttributes.addFlashAttribute("successMessage", "FAQ created successfully!");
        return "redirect:/admin/faqs";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        return faqService.findById(id)
                .map(faq -> {
                    model.addAttribute("faq", faq);
                    model.addAttribute("tinymceApiKey", tinymceApiKey);
                    return "admin/faqs/form";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("errorMessage", "FAQ not found!");
                    return "redirect:/admin/faqs";
                });
    }

    @PostMapping("/edit/{id}")
    public String updateFaq(@PathVariable Long id, @ModelAttribute Faq faq, RedirectAttributes redirectAttributes) {
        return faqService.findById(id)
                .map(existingFaq -> {
                    existingFaq.setQuestion(faq.getQuestion());
                    existingFaq.setAnswer(faq.getAnswer());
                    faqService.save(existingFaq);
                    redirectAttributes.addFlashAttribute("successMessage", "FAQ updated successfully!");
                    return "redirect:/admin/faqs";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("errorMessage", "FAQ not found!");
                    return "redirect:/admin/faqs";
                });
    }

    @PostMapping("/delete/{id}")
    public String deleteFaq(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        faqService.deleteById(id);
        redirectAttributes.addFlashAttribute("successMessage", "FAQ deleted successfully!");
        return "redirect:/admin/faqs";
    }
}
