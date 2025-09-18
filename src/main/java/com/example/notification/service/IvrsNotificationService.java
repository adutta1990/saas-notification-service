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
public class IvrsNotificationService {

    public NotificationResponse sendIvrsNotification(NotificationRequest request) {
        String notificationId = UUID.randomUUID().toString();
        log.info("Sending IVRS notification to: {}", request.getRecipient());

        try {
            simulateIvrsCall(request.getRecipient(), request.getMessage());
            log.info("IVRS notification sent successfully to: {}", request.getRecipient());

            return createSuccessResponse(notificationId, request);
        } catch (Exception e) {
            log.error("Failed to send IVRS notification to: {}", request.getRecipient(), e);
            return createErrorResponse(notificationId, request, e.getMessage());
        }
    }

    private void simulateIvrsCall(String phoneNumber, String message) throws Exception {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            throw new Exception("Invalid phone number");
        }

        if (message == null || message.trim().isEmpty()) {
            throw new Exception("Invalid message content");
        }

        Thread.sleep(100);

        if (phoneNumber.startsWith("999")) {
            throw new Exception("Simulated IVRS failure");
        }

        log.debug("IVRS call initiated to {} with message: {}", phoneNumber, message);
    }

    private NotificationResponse createSuccessResponse(String id, NotificationRequest request) {
        NotificationResponse response = new NotificationResponse(
            id, request.getTenantId(), request.getType(),
            request.getRecipient(), NotificationStatus.SENT
        );
        response.setMessage("IVRS call completed successfully");
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