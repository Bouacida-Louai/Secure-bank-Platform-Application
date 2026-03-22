package com.securebank.audit.controller;

import com.securebank.audit.AuditLogEntity;
import com.securebank.repositories.AuditLogRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/audit")
@RequiredArgsConstructor
@Tag(name = "Audit", description = "Immutable audit trail — AUDITOR & RISK_ANALYST only")
public class AuditController {

    private final AuditLogRepository auditLogRepository;

    @GetMapping("/logs")
    @PreAuthorize("hasAnyRole('AUDITOR','RISK_ANALYST','SUPER_ADMIN')")
    @Operation(
            summary = "Get all audit logs",
            description = "Returns all audit logs ordered by timestamp descending"
    )
    public ResponseEntity<List<AuditLogEntity>> getAllLogs() {
        return ResponseEntity.ok(
                auditLogRepository.findAllByOrderByTimestampDesc()
        );
    }

    @GetMapping("/logs/{entityType}/{entityId}")
    @PreAuthorize("hasAnyRole('AUDITOR','RISK_ANALYST','SUPER_ADMIN')")
    @Operation(
            summary = "Get logs by entity",
            description = "Returns all audit logs for a specific entity (e.g. Account, Loan)"
    )
    public ResponseEntity<List<AuditLogEntity>> getEntityLogs(
            @PathVariable String entityType,
            @PathVariable Long entityId) {
        return ResponseEntity.ok(
                auditLogRepository
                        .findAllByEntityTypeAndEntityId(entityType, entityId)
        );
    }

    @GetMapping("/logs/actor/{actorId}")
    @PreAuthorize("hasAnyRole('AUDITOR','SUPER_ADMIN')")
    @Operation(
            summary = "Get logs by actor",
            description = "Returns all audit logs performed by a specific user (Keycloak ID)"
    )
    public ResponseEntity<List<AuditLogEntity>> getActorLogs(
            @PathVariable String actorId) {
        return ResponseEntity.ok(
                auditLogRepository.findAllByActorId(actorId)
        );
    }
}