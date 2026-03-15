package com.securebank.audit;

import com.securebank.common.UserContext;
import com.securebank.repositories.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    public void log(String action, String entityType,
                    Long entityId, Object oldValue, Object newValue) {
        try {
            AuditLogEntity auditLog = AuditLogEntity.builder()
                    .actorId(getCurrentActorId())
                    .actorRole(getCurrentActorRole())
                    .action(action)
                    .entityType(entityType)
                    .entityId(entityId)
                    .oldValue(oldValue != null ?
                            objectMapper.writeValueAsString(oldValue) : null)
                    .newValue(newValue != null ?
                            objectMapper.writeValueAsString(newValue) : null)
                    .timestamp(Instant.now())
                    .build();

            auditLogRepository.save(auditLog);
            log.info("Audit log: {} on {} id={}",
                    action, entityType, entityId);

        } catch (Exception e) {
            log.error("Failed to write audit log: {}", e.getMessage());
        }
    }

    // Overload for simple logging without snapshots
    public void log(String action, String entityType, Long entityId) {
        log(action, entityType, entityId, null, null);
    }

    private String getCurrentActorId() {
        try {
            return UserContext.getCurrentKeycloakId();
        } catch (Exception e) {
            return "system";
        }
    }

    private String getCurrentActorRole() {
        try {
            return UserContext.getCurrentRole();
        } catch (Exception e) {
            return "UNKNOWN";
        }
    }
}