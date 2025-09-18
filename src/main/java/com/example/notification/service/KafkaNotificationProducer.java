package com.example.notification.service;

import com.example.notification.model.NotificationRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaNotificationProducer {

    private final KafkaTemplate<String, NotificationRequest> kafkaTemplate;

    @Value("${kafka.topic.notifications:notification-requests}")
    private String notificationTopic;

    public CompletableFuture<SendResult<String, NotificationRequest>> sendNotificationAsync(NotificationRequest request) {
        try {
            log.info("Sending notification to Kafka topic '{}' for tenant: {}, recipient: {}",
                    notificationTopic, request.getTenantId(), request.getRecipient());

            String key = request.getTenantId() + ":" + request.getType().toString();

            CompletableFuture<SendResult<String, NotificationRequest>> future =
                kafkaTemplate.send(notificationTopic, key, request);

            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("Successfully sent notification to Kafka: tenant={}, type={}, recipient={}, offset={}",
                            request.getTenantId(), request.getType(), request.getRecipient(),
                            result.getRecordMetadata().offset());
                } else {
                    log.error("Failed to send notification to Kafka: tenant={}, type={}, recipient={}, error={}",
                            request.getTenantId(), request.getType(), request.getRecipient(), ex.getMessage(), ex);
                }
            });

            return future;

        } catch (Exception e) {
            log.error("Error sending notification to Kafka: {}", e.getMessage(), e);
            CompletableFuture<SendResult<String, NotificationRequest>> failedFuture = new CompletableFuture<>();
            failedFuture.completeExceptionally(e);
            return failedFuture;
        }
    }

    public void sendNotificationSync(NotificationRequest request) {
        try {
            String key = request.getTenantId() + ":" + request.getType().toString();
            SendResult<String, NotificationRequest> result = kafkaTemplate.send(notificationTopic, key, request).get();
            log.info("Synchronously sent notification to Kafka: tenant={}, type={}, recipient={}, offset={}",
                    request.getTenantId(), request.getType(), request.getRecipient(),
                    result.getRecordMetadata().offset());
        } catch (Exception e) {
            log.error("Failed to send notification synchronously to Kafka: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to send notification to Kafka", e);
        }
    }
}