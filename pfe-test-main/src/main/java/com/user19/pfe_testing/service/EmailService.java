package com.user19.pfe_testing.service;

import com.user19.pfe_testing.model.ApprovalStep;
import com.user19.pfe_testing.model.NotificationStep;
import com.user19.pfe_testing.util.KeycloakSecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.util.HashSet;
import java.util.List;


@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final KeycloakSecurityUtil keycloakSecurityUtil;


    @Value("${spring.mail.username}")
    private String fromEmail;

    @Async
    public void sendEmail(String to, String subject, String content) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, true);

            mailSender.send(message);
            log.info("Email sent successfully from {} to {}", fromEmail, to);
        } catch (MessagingException e) {
            log.error("Error while sending email to {}: {}", to, e.getMessage());
        }
    }

    public void notifyClient(String clientEmail, String clientName, String processStatus) {
        String subject = "Your Process Status Update";
        String content = String.format(
                "<h3>Hello %s,</h3>" +
                        "<p>Your process status has been updated to: <strong>%s</strong></p>" +
                        "<p>Please check the system for more details.</p>",
                clientName, processStatus
        );

        sendEmail(clientEmail, subject, content);
    }

    public void notifyValidators(ApprovalStep approvalStep) {
        List<String> validatorEmails= keycloakSecurityUtil.getValidatorsEmailsByRoles(new HashSet<>(approvalStep.getValidatorRoles()));

        String subject = "New Validation Request";
        String content = "<h3>Dear Validator,</h3>" +
                "<p>A new request requires your review.</p>" +
                "<p>Please log in to the system to take action.</p>";

        validatorEmails.forEach(email -> sendEmail(email, subject, content));
    }
    public void notifyReciptients(NotificationStep notificationStep) {


        String subject = "New Notification ";



        notificationStep.getRecipients().forEach(email -> sendEmail(email, subject, String.format(

                        "<p>%s</p>" ,notificationStep.getMessage()
                )));
    }
}
