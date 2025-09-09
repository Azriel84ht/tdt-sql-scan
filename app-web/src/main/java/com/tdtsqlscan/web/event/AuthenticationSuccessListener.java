package com.tdtsqlscan.web.event;

import com.tdtsqlscan.web.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class AuthenticationSuccessListener implements ApplicationListener<AuthenticationSuccessEvent> {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationSuccessListener.class);

    @Autowired
    private UserRepository userRepository;

    @Override
    public void onApplicationEvent(AuthenticationSuccessEvent event) {
        Object principal = event.getAuthentication().getPrincipal();
        logger.info("AuthenticationSuccessEvent: User '{}' logged in successfully.", event.getAuthentication().getName());
        logger.info("AuthenticationSuccessEvent: Principal object is of type: {}", (principal != null) ? principal.getClass().getName() : "null");
        logger.info("AuthenticationSuccessEvent: Principal object value: {}", principal);

        String username = ((UserDetails) event.getAuthentication().getPrincipal()).getUsername();
        userRepository.findByUsername(username).ifPresent(user -> {
            user.setLastLoginDate(new Date());
            userRepository.save(user);
        });
    }
}
