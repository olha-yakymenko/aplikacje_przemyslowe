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

    /**
     * Wydajne czyszczenie starych logów za pomocą batch delete
     * Wykonuje tylko JEDNO zapytanie DELETE
     */
    @Transactional
    public int purgeOldLogs(LocalDateTime cutoffDate) {
        // Bez streamowania - bezpośrednie wywołanie repozytorium
        // (Zakładamy, że w AuditLogRepository dodamy odpowiednią metodę)
        int deletedCount = auditLogRepository.deleteByEventDateBefore(cutoffDate);
        return deletedCount;
    }

    /**
     * Alternatywna wersja - usuwa wszystkie logi
     */
    @Transactional
    public void purgeAllLogs() {
        // Wydajne usuwanie wszystkich logów jednym zapytaniem
        auditLogRepository.deleteAllInBatch();
    }

    /**
     * Usuwanie logów z danego zakresu dat
     */
    @Transactional
    public int purgeLogsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return auditLogRepository.deleteByEventDateBetween(startDate, endDate);
    }
}