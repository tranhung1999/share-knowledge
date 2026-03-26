package com.expense.management.service;

import com.expense.management.entity.AuditLog;
import com.expense.management.entity.User;
import com.expense.management.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    @Async
    public void log(User user, String action, String entityType, Long entityId,
                    String oldValues, String newValues, String ipAddress) {
        try {
            AuditLog entry = AuditLog.builder()
                    .user(user)
                    .action(action)
                    .entityType(entityType)
                    .entityId(entityId)
                    .oldValues(oldValues)
                    .newValues(newValues)
                    .ipAddress(ipAddress)
                    .build();
            auditLogRepository.save(entry);
        } catch (Exception e) {
            log.error("Failed to write audit log: action={}, entityType={}, entityId={}",
                    action, entityType, entityId, e);
        }
    }
}
