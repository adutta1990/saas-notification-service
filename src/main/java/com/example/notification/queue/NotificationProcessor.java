package com.example.notification.queue;

import com.example.notification.enums.NotificationStatus;
import com.example.notification.model.NotificationRequest;
import com.example.notification.model.NotificationResponse;
import com.example.notification.ratelimit.RateLimitService;
import com.example.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationProcessor {

    private final PriorityNotificationQueue notificationQueue;
    private final RateLimitService rateLimitService;
    private final NotificationService notificationService;
    
    private ExecutorService executorService;
    private volatile boolean running = true;

    @PostConstruct
    public void startProcessing() {
        executorService = Executors.newFixedThreadPool(5);
        
        for (int i = 0; i < 5; i++) {
            executorService.submit(this::processNotifications);
        }
    }

    @Async
    private void processNotifications() {
        while (running) {
            try {
                NotificationRequest request = notificationQueue.poll(1, TimeUnit.SECONDS);
                if (request != null) {
                    processNotification(request);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                log.error("Error processing notification", e);
            }
        }
    }

    private void processNotification(NotificationRequest request) {
        if (rateLimitService.isAllowed(request.getTenantId(), 1)) {
            NotificationResponse response = notificationService.sendNotification(request);

            if (response.getStatus() == NotificationStatus.SENT) {
                log.info("Notification sent successfully for tenant: {}, type: {}, id: {}",
                        request.getTenantId(), request.getType(), response.getId());
            } else {
                log.error("Failed to send notification for tenant: {}, type: {}, error: {}",
                        request.getTenantId(), request.getType(), response.getErrorMessage());

                if (request.getRetryCount() < 3) {
                    request.setRetryCount(request.getRetryCount() + 1);
                    notificationQueue.offer(request);
                }
            }
        } else {
            log.warn("Rate limit exceeded for tenant: {}, requeueing notification", request.getTenantId());
            notificationQueue.offer(request);
        }
    }

    public void shutdown() {
        running = false;
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}