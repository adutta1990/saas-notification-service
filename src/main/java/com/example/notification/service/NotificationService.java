package com.example.notification.service;

import com.example.notification.enums.NotificationStatus;
import com.example.notification.enums.NotificationType;
import com.example.notification.model.BulkNotificationRequest;
import com.example.notification.model.NotificationRequest;
import com.example.notification.model.NotificationResponse;
import com.example.notification.queue.PriorityNotificationQueue;
import com.example.notification.ratelimit.RateLimitService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final PriorityNotificationQueue notificationQueue;
    private final RateLimitService rateLimitService;
    private final KafkaNotificationProducer kafkaProducer;
    private final IvrsNotificationService ivrsService;
    private final SmsNotificationService smsService;
    private final PushNotificationService pushService;
    private final EmailNotificationService emailService;
    private final NotificationPreferenceService preferenceService;

    @Value("${notification.use-kafka:true}")
    private boolean useKafka;

    public NotificationResponse sendSingleNotification(NotificationRequest request) {
        String notificationId = UUID.randomUUID().toString();

        if (!preferenceService.canSendNotification(request.getTenantId(), request.getType())) {
            log.info("Notification blocked by user preferences for tenant: {} and type: {}",
                    request.getTenantId(), request.getType());
            NotificationResponse response = new NotificationResponse(notificationId, request.getTenantId(),
                    request.getType(), request.getRecipient(), NotificationStatus.BLOCKED);
            response.setErrorMessage("Notification blocked by user preferences");
            return response;
        }

        if (rateLimitService.isAllowed(request.getTenantId(), 1)) {
            if (useKafka) {
                try {
                    kafkaProducer.sendNotificationAsync(request);
                    log.info("Notification sent to Kafka successfully with ID: {}", notificationId);
                    return new NotificationResponse(notificationId, request.getTenantId(),
                            request.getType(), request.getRecipient(),
                            NotificationStatus.PENDING);
                } catch (Exception e) {
                    log.error("Failed to send notification to Kafka: {}", e.getMessage(), e);
                    NotificationResponse response = new NotificationResponse(notificationId, request.getTenantId(),
                            request.getType(), request.getRecipient(),
                            NotificationStatus.FAILED);
                    response.setErrorMessage("Failed to send to Kafka: " + e.getMessage());
                    return response;
                }
            } else {
                boolean queued = notificationQueue.offer(request);
                if (queued) {
                    log.info("Notification queued successfully with ID: {}", notificationId);
                    return new NotificationResponse(notificationId, request.getTenantId(),
                            request.getType(), request.getRecipient(),
                            NotificationStatus.PENDING);
                } else {
                    log.error("Failed to queue notification, queue is full");
                    NotificationResponse response = new NotificationResponse(notificationId, request.getTenantId(),
                            request.getType(), request.getRecipient(),
                            NotificationStatus.FAILED);
                    response.setErrorMessage("Queue is full");
                    return response;
                }
            }
        } else {
            log.warn("Rate limit exceeded for tenant: {}", request.getTenantId());
            NotificationResponse response = new NotificationResponse(notificationId, request.getTenantId(),
                    request.getType(), request.getRecipient(),
                    NotificationStatus.RATE_LIMITED);
            response.setErrorMessage("Rate limit exceeded");
            return response;
        }
    }

    public List<NotificationResponse> sendBulkNotification(BulkNotificationRequest bulkRequest) {
        List<NotificationResponse> responses = new ArrayList<>();
        
        for (String recipient : bulkRequest.getRecipients()) {
            NotificationRequest request = new NotificationRequest(
                bulkRequest.getTenantId(),
                bulkRequest.getType(),
                recipient,
                bulkRequest.getMessage()
            );
            request.setSubject(bulkRequest.getSubject());
            request.setTemplateData(bulkRequest.getTemplateData());
            request.setScheduledAt(bulkRequest.getScheduledAt());
            request.setCallbackUrl(bulkRequest.getCallbackUrl());
            
            NotificationResponse response = sendSingleNotification(request);
            responses.add(response);
        }
        
        return responses;
    }

    public NotificationResponse sendNotification(NotificationRequest request) {
        try {
            if (!preferenceService.canSendNotification(request.getTenantId(), request.getType())) {
                log.info("Direct notification blocked by user preferences for tenant: {} and type: {}",
                        request.getTenantId(), request.getType());
                String notificationId = UUID.randomUUID().toString();
                NotificationResponse response = new NotificationResponse(notificationId, request.getTenantId(),
                        request.getType(), request.getRecipient(), NotificationStatus.BLOCKED);
                response.setErrorMessage("Notification blocked by user preferences");
                return response;
            }

            switch (request.getType()) {
                case IVRS:
                    return ivrsService.sendIvrsNotification(request);
                case SMS:
                case OTP:
                    return smsService.sendSmsNotification(request);
                case PUSH:
                    return pushService.sendPushNotification(request);
                case MARKETING_EMAIL:
                case NEWSLETTER:
                case EMAIL:
                    return emailService.sendEmailNotification(request);
                default:
                    throw new IllegalArgumentException("Unsupported notification type: " + request.getType());
            }
        } catch (Exception e) {
            log.error("Failed to send notification: {}", e.getMessage(), e);
            String notificationId = UUID.randomUUID().toString();
            NotificationResponse response = new NotificationResponse(notificationId, request.getTenantId(),
                    request.getType(), request.getRecipient(), NotificationStatus.FAILED);
            response.setErrorMessage(e.getMessage());
            return response;
        }
    }
}