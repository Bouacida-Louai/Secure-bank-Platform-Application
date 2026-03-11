package com.securebank.repositories;

import com.securebank.audit.AuditLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLogEntity, Long> {

    List<AuditLogEntity> findAllByActorId(String actorId);
    List<AuditLogEntity> findAllByEntityTypeAndEntityId(String entityType, Long entityId);
    List<AuditLogEntity> findAllByOrderByTimestampDesc();
}
