// AuditLogRepository.java
package com.techcorp.employee.repository;

import com.techcorp.employee.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
}