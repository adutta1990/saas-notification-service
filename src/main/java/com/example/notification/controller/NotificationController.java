package com.example.notification.controller;

import com.example.notification.model.BulkNotificationRequest;
import com.example.notification.model.NotificationRequest;
import com.example.notification.model.NotificationResponse;
import com.example.notification.service.NotificationService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {
    
    private static final Logger logger = LoggerFactory.getLogger(NotificationController.class);
    
    @Autowired
    private NotificationService notificationService;

    @PostMapping("/send")
    public ResponseEntity<NotificationResponse> sendNotification(@Valid @RequestBody NotificationRequest request) {
        logger.info("Received single notification request for tenant: {}, type: {}", 
                   request.getTenantId(), request.getType());
        
        try {
            NotificationResponse response = notificationService.sendSingleNotification(request);
            
            if (response.getStatus() == com.example.notification.enums.NotificationStatus.RATE_LIMITED) {
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(response);
            } else if (response.getStatus() == com.example.notification.enums.NotificationStatus.FAILED) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error processing single notification request", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/send/bulk")
    public ResponseEntity<List<NotificationResponse>> sendBulkNotification(@Valid @RequestBody BulkNotificationRequest request) {
        logger.info("Received bulk notification request for tenant: {}, type: {}, recipients: {}", 
                   request.getTenantId(), request.getType(), request.getRecipients().size());
        
        try {
            List<NotificationResponse> responses = notificationService.sendBulkNotification(request);
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            logger.error("Error processing bulk notification request", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Notification service is running");
    }
}