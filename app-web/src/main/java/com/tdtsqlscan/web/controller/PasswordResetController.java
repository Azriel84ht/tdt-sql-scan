package com.tdtsqlscan.web.controller;

import com.tdtsqlscan.web.domain.User;
import com.tdtsqlscan.web.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.util.UUID;

@Controller
public class PasswordResetController {

    @Autowired
    private UserService userService;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private TemplateEngine templateEngine;

    @Value("${app.name}")
    private String appName;

    @Value("${app.email.reset.subject}")
    private String resetSubject;

    @Value("${spring.mail.from}")
    private String fromAddress;

    @GetMapping("/forgot-password")
    public String showForgotPasswordForm() {
        return "forgot-password";
    }

    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam("email") String userEmail, Model model, RedirectAttributes redirectAttributes) {
        User user = userService.findUserByEmail(userEmail);
        if (user == null) {
            model.addAttribute("error", "No user found with that email address.");
            return "forgot-password";
        }

        String token = UUID.randomUUID().toString();
        userService.createPasswordResetTokenForUser(user, token);

        try {
            final String appUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
            final String resetUrl = appUrl + "/reset-password?token=" + token;

            // Prepare the evaluation context
            final Context ctx = new Context();
            ctx.setVariable("userName", user.getUsername());
            ctx.setVariable("appName", this.appName);
            ctx.setVariable("resetUrl", resetUrl);

            // Create the HTML body using Thymeleaf
            final String htmlContent = this.templateEngine.process("password-reset.html", ctx);

            // Prepare message using a Spring MimeMessageHelper
            final MimeMessage mimeMessage = this.mailSender.createMimeMessage();
            final MimeMessageHelper email = new MimeMessageHelper(mimeMessage, "UTF-8");
            email.setFrom(this.fromAddress);
            email.setSubject(this.resetSubject);
            email.setTo(user.getEmail());
            email.setText(htmlContent, true); // true = is HTML

            mailSender.send(mimeMessage);

        } catch (MessagingException e) {
            // In a real application, handle this exception properly
            e.printStackTrace();
            model.addAttribute("error", "An error occurred while trying to send the email.");
            return "forgot-password";
        }

        redirectAttributes.addFlashAttribute("message", "A password reset link has been sent to " + userEmail);
        return "redirect:/forgot-password";
    }

    @GetMapping("/reset-password")
    public String showResetPasswordForm(@RequestParam("token") String token, Model model, RedirectAttributes redirectAttributes) {
        String result = userService.validatePasswordResetToken(token);
        if (result != null) {
            redirectAttributes.addFlashAttribute("error", "Invalid or expired password reset token.");
            return "redirect:/login";
        }
        model.addAttribute("token", token);
        return "reset-password";
    }

    @PostMapping("/reset-password")
    public String handlePasswordReset(@RequestParam("token") String token, @RequestParam("password") String password, RedirectAttributes redirectAttributes) {
        String result = userService.validatePasswordResetToken(token);
        if (result != null) {
            redirectAttributes.addFlashAttribute("error", "Invalid or expired password reset token.");
            return "redirect:/login";
        }

        User user = userService.getUserByPasswordResetToken(token);
        if (user != null) {
            // Here we should force the user to change password on next login
            userService.changeUserPassword(user, password);
            redirectAttributes.addFlashAttribute("message", "You have successfully reset your password. Please log in with the new password.");
        } else {
            redirectAttributes.addFlashAttribute("error", "An unexpected error occurred.");
        }

        return "redirect:/login";
    }
}
