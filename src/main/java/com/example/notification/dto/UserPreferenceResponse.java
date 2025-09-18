package com.example.notification.dto;

import com.example.notification.enums.NotificationType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserPreferenceResponse {
    private Long id;
    private NotificationType notificationType;
    private Boolean enabled;
    private Integer deliveryHourStart;
    private Integer deliveryHourEnd;
    private Integer maxFrequencyPerDay;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}