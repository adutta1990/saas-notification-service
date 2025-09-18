package com.example.notification.service;

import com.example.notification.enums.NotificationStatus;
import com.example.notification.model.NotificationRequest;
import com.example.notification.model.NotificationResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Slf4j
public class PushNotificationService {

    public NotificationResponse sendPushNotification(NotificationRequest request) {
        String notificationId = UUID.randomUUID().toString();
        log.info("Sending Push notification to: {}", request.getRecipient());

        try {
            simulatePushDelivery(request.getRecipient(), request.getMessage(), request.getSubject());
            log.info("Push notification sent successfully to: {}", request.getRecipient());

            return createSuccessResponse(notificationId, request);
        } catch (Exception e) {
            log.error("Failed to send Push notification to: {}", request.getRecipient(), e);
            return createErrorResponse(notificationId, request, e.getMessage());
        }
    }

    private void simulatePushDelivery(String deviceToken, String message, String title) throws Exception {
        if (deviceToken == null || deviceToken.trim().isEmpty()) {
            throw new Exception("Invalid device token");
        }

        if (message == null || message.trim().isEmpty()) {
            throw new Exception("Invalid message content");
        }

        Thread.sleep(30);

        if (deviceToken.startsWith("invalid")) {
            throw new Exception("Invalid device token - device not registered");
        }

        if (deviceToken.startsWith("expired")) {
            throw new Exception("Device token expired");
        }

        log.debug("Push notification sent to device {} with title: '{}' and message: '{}'",
                   deviceToken, title, message);
    }

    private NotificationResponse createSuccessResponse(String id, NotificationRequest request) {
        NotificationResponse response = new NotificationResponse(
            id, request.getTenantId(), request.getType(),
            request.getRecipient(), NotificationStatus.SENT
        );
        response.setMessage("Push notification sent successfully");
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