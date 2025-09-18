package com.example.notification.queue;

import com.example.notification.model.NotificationRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.util.Comparator;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;

@Component
public class PriorityNotificationQueue {
    
    @Value("${notification.queue.max-size:10000}")
    private int maxSize;
    
    private final PriorityBlockingQueue<NotificationRequest> queue;

    public PriorityNotificationQueue() {
        this.queue = new PriorityBlockingQueue<>(1000, 
            Comparator.comparing((NotificationRequest n) -> n.getType().getPriority())
                     .thenComparing(n -> n.getScheduledAt() != null ? n.getScheduledAt() : java.time.LocalDateTime.now()));
    }

    public boolean offer(NotificationRequest request) {
        if (queue.size() >= maxSize) {
            return false;
        }
        return queue.offer(request);
    }

    public NotificationRequest poll(long timeout, TimeUnit unit) throws InterruptedException {
        return queue.poll(timeout, unit);
    }
}