package com.tdtsqlscan.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import com.tdtsqlscan.web.domain.HomepageContent;
import com.tdtsqlscan.web.repository.SuggestionRepository;
import com.tdtsqlscan.web.service.ContentService;
import com.tdtsqlscan.web.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private ContentService contentService;

    @Autowired
    private UserService userService;

    @Autowired
    private SuggestionRepository suggestionRepository;

    @GetMapping
    public String adminHome() {
        return "admin/dashboard";
    }

    @GetMapping("/content")
    public String showContentForm(Model model) {
        model.addAttribute("content", contentService.getContent());
        return "admin/content";
    }

    @PostMapping("/content")
    public String saveContent(@ModelAttribute HomepageContent content,
                              @RequestParam(name = "showPopup", required = false) String showPopupCheckbox,
                              Model model) {
        HomepageContent existingContent = contentService.getContent();
        existingContent.setHeroTitle(content.getHeroTitle());
        existingContent.setHeroSubtitle(content.getHeroSubtitle());
        existingContent.setNewsSection(content.getNewsSection());
        existingContent.setPopupMessage(content.getPopupMessage());
        existingContent.setShowPopup(showPopupCheckbox != null);

        contentService.saveContent(existingContent);

        model.addAttribute("message", "Homepage content updated successfully!");
        model.addAttribute("content", existingContent);
        return "admin/content";
    }

    @GetMapping("/users")
    public String showUserList(Model model) {
        model.addAttribute("users", userService.findAllUsers());
        return "admin/users";
    }

    @GetMapping("/suggestions")
    public String showSuggestions(Model model) {
        model.addAttribute("suggestions", suggestionRepository.findAll());
        return "admin/suggestions";
    }

    @PostMapping("/suggestions/delete/{id}")
    public String deleteSuggestion(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        suggestionRepository.deleteById(id);
        redirectAttributes.addFlashAttribute("message", "Suggestion deleted successfully.");
        return "redirect:/admin/suggestions";
    }
}
