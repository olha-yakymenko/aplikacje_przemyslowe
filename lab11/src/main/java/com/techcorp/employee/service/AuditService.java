package com.techcorp.employee.service;

import com.techcorp.employee.model.AuditLog;
import com.techcorp.employee.repository.AuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    @Autowired
    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logEvent(String message) {
        logEvent(message, "GENERAL", null, null);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logEvent(String message, String eventType, String affectedEntity, Long entityId) {
        AuditLog log = new AuditLog();
        log.setMessage(message);
        log.setEventDate(LocalDateTime.now());
        log.setEventType(eventType);
        log.setAffectedEntity(affectedEntity);
        log.setEntityId(entityId);

        auditLogRepository.save(log);
        System.out.println("Audit log saved: " + message);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logSalaryUpdateAttempt(Long employeeId, String employeeName,
                                       Double oldSalary, Double newSalary) {
        String message = String.format(
                "Salary update attempt - Employee: %s (ID: %d), Old: %.2f, New: %.2f, Change: %.2f%%",
                employeeName, employeeId, oldSalary, newSalary,
                ((newSalary - oldSalary) / oldSalary * 100)
        );

        logEvent(message, "SALARY_UPDATE_ATTEMPT", "Employee", employeeId);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logSalaryUpdateSuccess(Long employeeId, String employeeName,
                                       Double oldSalary, Double newSalary) {
        String message = String.format(
                "Salary updated successfully - Employee: %s (ID: %d), Old: %.2f, New: %.2f",
                employeeName, employeeId, oldSalary, newSalary
        );

        logEvent(message, "SALARY_UPDATE_SUCCESS", "Employee", employeeId);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logSalaryUpdateFailure(Long employeeId, String employeeName,
                                       String errorMessage) {
        String message = String.format(
                "Salary update failed - Employee: %s (ID: %d), Error: %s",
                employeeName, employeeId, errorMessage
        );

        logEvent(message, "SALARY_UPDATE_FAILURE", "Employee", employeeId);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logBusinessRuleViolation(Long employeeId, String employeeName,
                                         String ruleDescription) {
        String message = String.format(
                "Business rule violation - Employee: %s (ID: %d), Rule: %s",
                employeeName, employeeId, ruleDescription
        );

        logEvent(message, "BUSINESS_RULE_VIOLATION", "Employee", employeeId);
    }
}