package com.tdtsqlscan.web.config;

import com.tdtsqlscan.web.domain.User;
import com.tdtsqlscan.web.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class CustomAuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Autowired
    private UserRepository userRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User user = userRepository.findByUsername(userDetails.getUsername()).orElse(null);

        if (user != null && user.isMustChangePassword()) {
            getRedirectStrategy().sendRedirect(request, response, "/change-password");
        } else {
            // Set default target url if not configured
            String targetUrl = determineTargetUrl(authentication);
            getRedirectStrategy().sendRedirect(request, response, targetUrl);
        }
    }

    protected String determineTargetUrl(Authentication authentication) {
        // Fallback to a default URL if none is configured
        return "/app";
    }
}
