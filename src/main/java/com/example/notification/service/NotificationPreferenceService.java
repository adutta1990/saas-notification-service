package com.example.notification.service;

import com.example.notification.entity.UserNotificationPreference;
import com.example.notification.enums.NotificationType;
import com.example.notification.repository.UserNotificationPreferenceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationPreferenceService {

    private final UserNotificationPreferenceRepository preferenceRepository;

    public boolean canSendNotification(String tenantId, NotificationType notificationType) {
        Optional<UserNotificationPreference> preferenceOpt =
                preferenceRepository.findByUserTenantIdAndNotificationType(tenantId, notificationType);

        if (preferenceOpt.isEmpty()) {
            log.debug("No preferences found for tenant {} and type {}, allowing notification",
                    tenantId, notificationType);
            return true;
        }

        UserNotificationPreference preference = preferenceOpt.get();

        if (!preference.getEnabled()) {
            log.info("Notifications disabled for tenant {} and type {}", tenantId, notificationType);
            return false;
        }

        if (!isWithinDeliveryHours(preference)) {
            log.info("Current time is outside delivery hours for tenant {} and type {}",
                    tenantId, notificationType);
            return false;
        }

        return true;
    }

    private boolean isWithinDeliveryHours(UserNotificationPreference preference) {
        if (preference.getDeliveryHourStart() == null || preference.getDeliveryHourEnd() == null) {
            return true;
        }

        int currentHour = LocalDateTime.now().getHour();
        int startHour = preference.getDeliveryHourStart();
        int endHour = preference.getDeliveryHourEnd();

        if (startHour <= endHour) {
            return currentHour >= startHour && currentHour <= endHour;
        } else {
            return currentHour >= startHour || currentHour <= endHour;
        }
    }
}