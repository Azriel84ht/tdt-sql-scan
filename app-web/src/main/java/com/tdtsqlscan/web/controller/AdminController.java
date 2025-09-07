package com.tdtsqlscan.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import com.tdtsqlscan.web.repository.SuggestionRepository;
import com.tdtsqlscan.web.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private SuggestionRepository suggestionRepository;

    @GetMapping
    public String adminHome() {
        return "admin/dashboard";
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
