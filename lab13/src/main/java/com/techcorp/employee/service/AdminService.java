package com.techcorp.employee.service;

import com.techcorp.employee.model.AuditLog;
import com.techcorp.employee.repository.AuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AdminService {

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Transactional
    public int purgeOldLogs(LocalDateTime cutoffDate) {
        int deletedCount = auditLogRepository.deleteByEventDateBefore(cutoffDate);
        return deletedCount;
    }


    @Transactional
    public void purgeAllLogs() {
        auditLogRepository.deleteAllInBatch();
    }

}