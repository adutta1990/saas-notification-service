package com.example.notification.entity;

import com.example.notification.enums.NotificationStatus;
import com.example.notification.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("notification_audit")
public class NotificationAudit {

    @PrimaryKey
    private UUID id;

    @Column("tenant_id")
    private String tenantId;

    @Column("notification_type")
    private NotificationType notificationType;

    @Column("recipient")
    private String recipient;

    @Column("message")
    private String message;

    @Column("subject")
    private String subject;

    @Column("template_data")
    private Map<String, String> templateData;

    @Column("status")
    private NotificationStatus status;

    @Column("error_message")
    private String errorMessage;

    @Column("created_at")
    private LocalDateTime createdAt;

    @Column("sent_at")
    private LocalDateTime sentAt;

    @Column("retry_count")
    private Integer retryCount;

    @Column("callback_url")
    private String callbackUrl;

    public NotificationAudit(String tenantId, NotificationType notificationType, String recipient,
                           String message, String subject, NotificationStatus status) {
        this.id = UUID.randomUUID();
        this.tenantId = tenantId;
        this.notificationType = notificationType;
        this.recipient = recipient;
        this.message = message;
        this.subject = subject;
        this.status = status;
        this.createdAt = LocalDateTime.now();
        this.retryCount = 0;
    }
}