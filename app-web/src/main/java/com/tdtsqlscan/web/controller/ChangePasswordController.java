package com.tdtsqlscan.web.controller;

import com.tdtsqlscan.web.domain.User;
import com.tdtsqlscan.web.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Controller
public class ChangePasswordController {

    @Autowired
    private UserService userService;

    @GetMapping("/change-password")
    public String showChangePasswordForm() {
        return "change-password";
    }

    @PostMapping("/change-password")
    public String processChangePassword(@RequestParam("password") String newPassword,
                                        @RequestParam("confirmPassword") String confirmPassword,
                                        @AuthenticationPrincipal UserDetails userDetails,
                                        RedirectAttributes redirectAttributes,
                                        HttpServletRequest request, HttpServletResponse response) {

        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("error", "Passwords do not match.");
            return "redirect:/change-password";
        }

        User user = userService.findUserByUsername(userDetails.getUsername());
        if (user != null) {
            userService.changeUserPassword(user, newPassword);

            // Log the user out after password change
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null) {
                new SecurityContextLogoutHandler().logout(request, response, auth);
            }

            redirectAttributes.addFlashAttribute("message", "Your password has been changed successfully. Please log in again.");
            return "redirect:/login?changeSuccess";
        }

        redirectAttributes.addFlashAttribute("error", "An error occurred while changing your password.");
        return "redirect:/change-password";
    }
}
