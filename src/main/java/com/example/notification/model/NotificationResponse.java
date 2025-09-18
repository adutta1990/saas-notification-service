package com.example.notification.model;

import com.example.notification.enums.NotificationStatus;
import com.example.notification.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {

    private String id;
    private String tenantId;
    private NotificationType type;
    private String recipient;
    private NotificationStatus status;
    private String message;
    private String errorMessage;
    private LocalDateTime createdAt;
    private LocalDateTime sentAt;

    public NotificationResponse(String id, String tenantId, NotificationType type, String recipient, NotificationStatus status) {
        this.id = id;
        this.tenantId = tenantId;
        this.type = type;
        this.recipient = recipient;
        this.status = status;
        this.createdAt = LocalDateTime.now();
    }
}