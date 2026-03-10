package com.securebank.audit;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Immutable;

import java.time.Instant;

@Entity
@Table(name = "audit_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Immutable  // ← Hibernate: no UPDATE ever generated
public class AuditLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String actorId;

    private String actorRole;

    @Column(nullable = false)
    private String action;       // e.g. "TRANSFER", "LOAN_APPROVED"

    private String entityType;   // e.g. "Account", "Loan"

    private Long entityId;

    @Column(columnDefinition = "TEXT")
    private String oldValue;     // JSON snapshot before

    @Column(columnDefinition = "TEXT")
    private String newValue;     // JSON snapshot after

    private String ipAddress;

    @Column(nullable = false, updatable = false)
    private Instant timestamp = Instant.now();
}