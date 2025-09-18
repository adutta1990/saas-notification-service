package com.example.notification.service;

import com.example.notification.config.TenantConfiguration;
import com.example.notification.model.NotificationRequest;
import com.example.notification.model.NotificationResponse;
import com.example.notification.enums.NotificationStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailNotificationService {

    private final TenantConfigurationService tenantConfigService;

    public NotificationResponse sendEmailNotification(NotificationRequest request) {
        String notificationId = UUID.randomUUID().toString();

        try {
            TenantConfiguration tenantConfig = tenantConfigService.getTenantConfiguration(request.getTenantId());

            if (tenantConfig == null || tenantConfig.getEmailConfig() == null) {
                log.error("Email configuration not found for tenant: {}", request.getTenantId());
                return createErrorResponse(notificationId, request, "Email configuration not found");
            }

            JavaMailSender mailSender = createMailSender(tenantConfig.getEmailConfig());

            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(request.getRecipient());
            message.setSubject(request.getSubject() != null ? request.getSubject() : "Notification");
            message.setText(processTemplate(request.getMessage(), request.getTemplateData()));

            Map<String, String> emailConfig = tenantConfig.getEmailConfig();
            if (emailConfig.containsKey("from")) {
                message.setFrom(emailConfig.get("from"));
            }

            mailSender.send(message);

            log.info("Email notification sent successfully to {} for tenant {}",
                    request.getRecipient(), request.getTenantId());

            return createSuccessResponse(notificationId, request);

        } catch (Exception e) {
            log.error("Failed to send email notification to {} for tenant {}: {}",
                    request.getRecipient(), request.getTenantId(), e.getMessage(), e);
            return createErrorResponse(notificationId, request, e.getMessage());
        }
    }

    private JavaMailSender createMailSender(Map<String, String> emailConfig) {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();

        mailSender.setHost(emailConfig.getOrDefault("host", "localhost"));
        mailSender.setPort(Integer.parseInt(emailConfig.getOrDefault("port", "587")));
        mailSender.setUsername(emailConfig.get("username"));
        mailSender.setPassword(emailConfig.get("password"));

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", emailConfig.getOrDefault("auth", "true"));
        props.put("mail.smtp.starttls.enable", emailConfig.getOrDefault("starttls", "true"));
        props.put("mail.debug", emailConfig.getOrDefault("debug", "false"));

        return mailSender;
    }

    private String processTemplate(String template, Map<String, Object> templateData) {
        if (templateData == null || templateData.isEmpty()) {
            return template;
        }

        String processedMessage = template;
        for (Map.Entry<String, Object> entry : templateData.entrySet()) {
            String placeholder = "{{" + entry.getKey() + "}}";
            processedMessage = processedMessage.replace(placeholder, String.valueOf(entry.getValue()));
        }

        return processedMessage;
    }

    private NotificationResponse createSuccessResponse(String id, NotificationRequest request) {
        NotificationResponse response = new NotificationResponse(
            id, request.getTenantId(), request.getType(),
            request.getRecipient(), NotificationStatus.SENT
        );
        response.setMessage("Email sent successfully");
        response.setSentAt(LocalDateTime.now());
        return response;
    }

    private NotificationResponse createErrorResponse(String id, NotificationRequest request, String errorMessage) {
        NotificationResponse response = new NotificationResponse(
            id, request.getTenantId(), request.getType(),
            request.getRecipient(), NotificationStatus.FAILED
        );
        response.setErrorMessage(errorMessage);
        return response;
    }
}