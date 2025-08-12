package com.tdtsqlscan.web.controller;

import com.tdtsqlscan.web.domain.Suggestion;
import com.tdtsqlscan.web.domain.User;
import com.tdtsqlscan.web.repository.SuggestionRepository;
import com.tdtsqlscan.web.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class SuggestionController {

    @Autowired
    private SuggestionRepository suggestionRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/suggestions")
    public String showSuggestionForm(Model model) {
        return "suggestions";
    }

    @PostMapping("/suggestions")
    public String submitSuggestion(@RequestParam String suggestionText, @AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails != null) {
            userRepository.findByUsername(userDetails.getUsername()).ifPresent(user -> {
                Suggestion suggestion = new Suggestion();
                suggestion.setText(suggestionText);
                suggestion.setUser(user);
                suggestionRepository.save(suggestion);
                model.addAttribute("message", "Thank you for your suggestion!");
            });
        }
        return "suggestions";
    }
}
