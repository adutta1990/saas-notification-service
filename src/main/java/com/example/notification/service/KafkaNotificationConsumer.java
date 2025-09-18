package com.example.notification.service;

import com.example.notification.entity.NotificationAudit;
import com.example.notification.enums.NotificationStatus;
import com.example.notification.model.NotificationRequest;
import com.example.notification.model.NotificationResponse;
import com.example.notification.repository.NotificationAuditRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaNotificationConsumer {

    private final NotificationAuditRepository auditRepository;
    private final IvrsNotificationService ivrsService;
    private final SmsNotificationService smsService;
    private final PushNotificationService pushService;
    private final EmailNotificationService emailService;

    @KafkaListener(topics = "${kafka.topic.notifications:notification-requests}")
    public void handleNotification(
            @Payload NotificationRequest request,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment ack) {

        log.info("Received notification from Kafka - Topic: {}, Partition: {}, Offset: {}, Tenant: {}, Type: {}, Recipient: {}",
                topic, partition, offset, request.getTenantId(), request.getType(), request.getRecipient());
        NotificationAudit audit = createInitialAudit(request);

        try {
            auditRepository.save(audit);
            NotificationResponse response = processNotification(request);
            updateAuditWithResponse(audit, response);
            auditRepository.save(audit);
            log.info("Successfully processed notification - ID: {}, Status: {}", audit.getId(), audit.getStatus());
        } catch (Exception e) {
            log.error("Failed to process notification for tenant: {}, recipient: {}, error: {}",
                    request.getTenantId(), request.getRecipient(), e.getMessage(), e);

            audit.setStatus(NotificationStatus.FAILED);
            audit.setErrorMessage(e.getMessage());
            auditRepository.save(audit);

        } finally {
            ack.acknowledge();
        }
    }

    private NotificationResponse processNotification(NotificationRequest request) {
        try {
            return switch (request.getType()) {
                case IVRS -> ivrsService.sendIvrsNotification(request);
                case SMS, OTP -> smsService.sendSmsNotification(request);
                case PUSH -> pushService.sendPushNotification(request);
                case MARKETING_EMAIL, NEWSLETTER, EMAIL -> emailService.sendEmailNotification(request);
            };
        } catch (Exception e) {
            log.error("Error processing notification type {}: {}", request.getType(), e.getMessage(), e);
            throw e;
        }
    }

    private NotificationAudit createInitialAudit(NotificationRequest request) {
        Map<String, String> templateData = convertTemplateData(request.getTemplateData());

        NotificationAudit audit = new NotificationAudit(
                request.getTenantId(),
                request.getType(),
                request.getRecipient(),
                request.getMessage(),
                request.getSubject(),
                NotificationStatus.PROCESSING
        );

        audit.setTemplateData(templateData);
        audit.setRetryCount(request.getRetryCount());
        audit.setCallbackUrl(request.getCallbackUrl());
        audit.setCreatedAt(LocalDateTime.now());

        return audit;
    }

    private void updateAuditWithResponse(NotificationAudit audit, NotificationResponse response) {
        audit.setStatus(response.getStatus());
        audit.setErrorMessage(response.getErrorMessage());
        if (response.getStatus() == NotificationStatus.SENT) {
            audit.setSentAt(LocalDateTime.now());
        }
    }

    private Map<String, String> convertTemplateData(Map<String, Object> templateData) {
        if (templateData == null) {
            return new HashMap<>();
        }

        Map<String, String> stringMap = new HashMap<>();
        for (Map.Entry<String, Object> entry : templateData.entrySet()) {
            stringMap.put(entry.getKey(), entry.getValue() != null ? entry.getValue().toString() : null);
        }
        return stringMap;
    }
}