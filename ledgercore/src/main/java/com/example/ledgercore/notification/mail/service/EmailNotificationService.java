package com.example.ledgercore.notification.mail.service;

import com.example.ledgercore.notification.mail.enums.EmailTemplateType;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.Year;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailNotificationService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.app.mail.from}")
    private String sender;

    @Value("${spring.app.name}")
    private String appName;

    @Retryable(
            retryFor = MailException.class,
            noRetryFor = IllegalArgumentException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000)
    )
    public void send(
            String recipient,
            EmailTemplateType templateType,
            Map<String, Object> variables
    ) {
        validateVariables(templateType, variables);

        try {
            Context context = new Context();
            context.setVariable("appName", appName);
            context.setVariable("year", Year.now().getValue());
            variables.forEach(context::setVariable);

            String htmlContent = templateEngine.process(
                    templateType.getTemplatePath(),
                    context
            );

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(new InternetAddress(sender, appName));
            helper.setTo(recipient);
            helper.setSubject(templateType.getSubject());
            helper.setText(htmlContent, true);

            mailSender.send(message);

            log.info(
                    "Email sent successfully recipient={} template={}",
                    recipient,
                    templateType
            );
        } catch (MailException e) {
            log.error(
                    "Failed to send email recipient={} template={}",
                    recipient,
                    templateType,
                    e
            );
            throw e;
        } catch (Exception e) {
            log.error(
                    "Failed to build email recipient={} template={}",
                    recipient,
                    templateType,
                    e
            );
            throw new IllegalStateException("Failed to build email", e);
        }
    }

    private void validateVariables(
            EmailTemplateType templateType,
            Map<String, Object> variables
    ) {
        var missingVariables = templateType.getRequiredVariables()
                .stream()
                .filter(variable -> !variables.containsKey(variable))
                .toList();

        if (!missingVariables.isEmpty()) {
            throw new IllegalArgumentException(
                    "Missing variables " + missingVariables +
                            " for template " + templateType
            );
        }
    }
}