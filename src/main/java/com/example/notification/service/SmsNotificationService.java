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
public class SmsNotificationService {

    public NotificationResponse sendSmsNotification(NotificationRequest request) {
        String notificationId = UUID.randomUUID().toString();
        log.info("Sending SMS notification to: {}", request.getRecipient());

        try {
            simulateSmsDelivery(request.getRecipient(), request.getMessage());
            log.info("SMS notification sent successfully to: {}", request.getRecipient());

            return createSuccessResponse(notificationId, request);
        } catch (Exception e) {
            log.error("Failed to send SMS notification to: {}", request.getRecipient(), e);
            return createErrorResponse(notificationId, request, e.getMessage());
        }
    }

    private void simulateSmsDelivery(String phoneNumber, String message) throws Exception {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            throw new Exception("Invalid phone number");
        }

        if (message == null || message.trim().isEmpty()) {
            throw new Exception("Invalid message content");
        }

        if (message.length() > 160) {
            log.warn("SMS message length exceeds 160 characters, message will be split");
        }

        Thread.sleep(50);

        if (phoneNumber.startsWith("888")) {
            throw new Exception("Simulated SMS delivery failure");
        }

        log.debug("SMS sent to {} with content: {}", phoneNumber, message);
    }

    private NotificationResponse createSuccessResponse(String id, NotificationRequest request) {
        NotificationResponse response = new NotificationResponse(
            id, request.getTenantId(), request.getType(),
            request.getRecipient(), NotificationStatus.SENT
        );
        response.setMessage("SMS sent successfully");
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