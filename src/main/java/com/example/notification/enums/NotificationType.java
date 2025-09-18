package com.example.notification.enums;

public enum NotificationType {
    OTP(1),
    IVRS(2),
    SMS(3),
    PUSH(4),
    EMAIL(5),
    MARKETING_EMAIL(6),
    NEWSLETTER(7);

    private final int priority;

    NotificationType(int priority) {
        this.priority = priority;
    }

    public int getPriority() {
        return priority;
    }
}