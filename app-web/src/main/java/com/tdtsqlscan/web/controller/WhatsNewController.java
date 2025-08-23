package com.tdtsqlscan.web.controller;

import com.tdtsqlscan.web.service.WhatsNewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/whats-new")
public class WhatsNewController {

    private final WhatsNewService whatsNewService;

    @Autowired
    public WhatsNewController(WhatsNewService whatsNewService) {
        this.whatsNewService = whatsNewService;
    }

    @GetMapping
    public String listWhatsNew(Model model) {
        model.addAttribute("whatsnews", whatsNewService.findAll());
        return "whats-new";
    }
}
