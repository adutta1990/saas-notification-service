package com.example.notification.model;

import com.example.notification.enums.NotificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BulkNotificationRequest {
    
    @NotBlank
    private String tenantId;
    
    @NotNull
    private NotificationType type;
    
    @NotEmpty
    private List<String> recipients;
    
    @NotBlank
    private String message;
    
    private String subject;
    private Map<String, Object> templateData;
    private LocalDateTime scheduledAt;
    private String callbackUrl;

}