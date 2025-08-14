package com.tdtsqlscan.web.event;

import com.tdtsqlscan.web.domain.User;
import com.tdtsqlscan.web.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.UUID;

@Component
public class RegistrationListener implements ApplicationListener<OnRegistrationCompleteEvent> {

    @Autowired
    private UserService service;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private TemplateEngine templateEngine;

    @Value("${app.name}")
    private String appName;

    @Value("${app.email.registration.subject}")
    private String registrationSubject;

    @Override
    public void onApplicationEvent(final OnRegistrationCompleteEvent event) {
        try {
            this.confirmRegistration(event);
        } catch (MessagingException e) {
            // In a real application, handle this exception properly (e.g., log it, queue the email for retry)
            e.printStackTrace();
        }
    }

    private void confirmRegistration(final OnRegistrationCompleteEvent event) throws MessagingException {
        final User user = event.getUser();
        final String token = UUID.randomUUID().toString();
        service.createVerificationTokenForUser(user, token);

        final MimeMessage email = constructMimeMessage(event, user, token);
        mailSender.send(email);
    }

    private MimeMessage constructMimeMessage(final OnRegistrationCompleteEvent event, final User user, final String token) throws MessagingException {
        // Prepare the evaluation context
        final Context ctx = new Context();
        ctx.setVariable("userName", user.getUsername());
        ctx.setVariable("appName", this.appName);
        final String confirmationUrl = event.getAppUrl() + "/verify?token=" + token;
        ctx.setVariable("confirmationUrl", confirmationUrl);

        // Create the HTML body using Thymeleaf
        final String htmlContent = this.templateEngine.process("email-verification.html", ctx);

        // Prepare message using a Spring MimeMessageHelper
        final MimeMessage mimeMessage = this.mailSender.createMimeMessage();
        final MimeMessageHelper message = new MimeMessageHelper(mimeMessage, "UTF-8");
        message.setSubject(this.registrationSubject);
        message.setTo(user.getEmail());
        message.setText(htmlContent, true); // true = is HTML

        return mimeMessage;
    }
}
