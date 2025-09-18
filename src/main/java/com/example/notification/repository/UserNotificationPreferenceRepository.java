package com.example.notification.repository;

import com.example.notification.entity.UserNotificationPreference;
import com.example.notification.enums.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserNotificationPreferenceRepository extends JpaRepository<UserNotificationPreference, Long> {
    List<UserNotificationPreference> findByUserTenantId(String tenantId);
    Optional<UserNotificationPreference> findByUserTenantIdAndNotificationType(String tenantId, NotificationType notificationType);
    void deleteByUserTenantIdAndNotificationType(String tenantId, NotificationType notificationType);
}