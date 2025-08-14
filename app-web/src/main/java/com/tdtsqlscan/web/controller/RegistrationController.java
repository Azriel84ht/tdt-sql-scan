package com.tdtsqlscan.web.controller;

import com.tdtsqlscan.web.domain.User;
import com.tdtsqlscan.web.dto.RegistrationResult;
import com.tdtsqlscan.web.dto.UserDto;
import com.tdtsqlscan.web.event.OnRegistrationCompleteEvent;
import com.tdtsqlscan.web.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Controller
public class RegistrationController {

    @Autowired
    private UserService userService;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new UserDto());
        return "register";
    }

    @PostMapping("/register")
    public String registerUserAccount(@ModelAttribute("user") UserDto userDto, WebRequest request, Model model) {
        try {
            RegistrationResult result = userService.registerNewUserAccount(userDto);
            User registered = result.getUser();

            final String appUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
            eventPublisher.publishEvent(new OnRegistrationCompleteEvent(registered, appUrl));

            if (result.isResent()) {
                model.addAttribute("message", "This email is already registered but not yet verified. A new verification email has been sent to " + userDto.getEmail() + ". Please check your inbox. Your original registration data will be used.");
            } else {
                model.addAttribute("message", "A verification email has been sent to " + userDto.getEmail() + ". Please check your inbox to activate your account.");
            }
        } catch (Exception ex) {
            model.addAttribute("error", ex.getMessage());
            return "register";
        }
        return "register";
    }

    @GetMapping("/verify")
    public String confirmRegistration(Model model, @RequestParam("token") String token) {
        String result = userService.validateVerificationToken(token);
        if (result == null) {
            return "redirect:/login?verified=true";
        }
        model.addAttribute("error", result);
        return "redirect:/login?verified=false";
    }
}
