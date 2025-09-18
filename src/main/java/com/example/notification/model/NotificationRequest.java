package com.example.notification.model;

import com.example.notification.enums.NotificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequest {

    @NotBlank
    private String tenantId;

    @NotNull
    private NotificationType type;

    @NotBlank
    private String recipient;

    @NotBlank
    private String message;

    private String subject;
    private Map<String, Object> templateData;
    private LocalDateTime scheduledAt;
    private int retryCount = 0;
    private String callbackUrl;

    public NotificationRequest(String tenantId, NotificationType type, String recipient, String message) {
        this.tenantId = tenantId;
        this.type = type;
        this.recipient = recipient;
        this.message = message;
    }
}