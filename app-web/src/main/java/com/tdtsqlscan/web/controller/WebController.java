package com.tdtsqlscan.web.controller;

import com.tdtsqlscan.web.service.ContentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {

    @Autowired
    private ContentService contentService;

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("content", contentService.getContent());
        return "home";
    }

    @GetMapping("/app")
    public String app() {
        return "app";
    }
}
