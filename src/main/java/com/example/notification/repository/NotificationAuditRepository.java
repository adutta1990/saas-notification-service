package com.example.notification.repository;

import com.example.notification.entity.NotificationAudit;
import com.example.notification.enums.NotificationStatus;
import com.example.notification.enums.NotificationType;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationAuditRepository extends CassandraRepository<NotificationAudit, UUID> {

    @Query("SELECT * FROM notification_audit WHERE tenant_id = ?0 ALLOW FILTERING")
    List<NotificationAudit> findByTenantId(String tenantId);

    @Query("SELECT * FROM notification_audit WHERE tenant_id = ?0 AND notification_type = ?1 ALLOW FILTERING")
    List<NotificationAudit> findByTenantIdAndNotificationType(String tenantId, NotificationType notificationType);

    @Query("SELECT * FROM notification_audit WHERE tenant_id = ?0 AND status = ?1 ALLOW FILTERING")
    List<NotificationAudit> findByTenantIdAndStatus(String tenantId, NotificationStatus status);

    @Query("SELECT * FROM notification_audit WHERE tenant_id = ?0 AND created_at >= ?1 AND created_at <= ?2 ALLOW FILTERING")
    List<NotificationAudit> findByTenantIdAndCreatedAtBetween(String tenantId, LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT * FROM notification_audit WHERE tenant_id = ?0")
    Slice<NotificationAudit> findByTenantIdWithPaging(String tenantId, Pageable pageable);
}