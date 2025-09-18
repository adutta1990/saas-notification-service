package com.example.notification.dto;

import com.example.notification.enums.NotificationType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserPreferenceRequest {

    @NotNull
    private NotificationType notificationType;

    @NotNull
    private Boolean enabled;

    @Min(0)
    @Max(23)
    private Integer deliveryHourStart = 0;

    @Min(0)
    @Max(23)
    private Integer deliveryHourEnd = 23;

    @Min(1)
    private Integer maxFrequencyPerDay;
}