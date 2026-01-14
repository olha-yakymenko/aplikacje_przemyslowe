package com.techcorp.employee.service;

import com.techcorp.employee.model.AuditLog;
import com.techcorp.employee.repository.AuditLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@SpringBootTest
@Transactional
class AdminServiceBatchDeleteTest {

    @Autowired
    private AdminService adminService;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @BeforeEach
    void setUp() {
        for (int i = 0; i < 1000; i++) {
            AuditLog log = new AuditLog("Test log " + i, "TEST", "TestEntity", (long) i);
            if (i < 500) {
                log.setEventDate(LocalDateTime.now().minusDays(10));
            } else {
                log.setEventDate(LocalDateTime.now().minusDays(1));
            }
            auditLogRepository.save(log);
        }
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @Rollback(false) // aby zobaczyć SQL
    void purgeOldLogs_ShouldExecuteSingleDeleteQuery() {
        // Given
        long countBefore = auditLogRepository.count();

        // When
        int deletedCount = adminService.purgeOldLogs(LocalDateTime.now().minusDays(5));
        long countAfter = auditLogRepository.count();

        // Then
        assertAll(
                () -> assertThat(countBefore).isEqualTo(1000),
                () -> assertThat(deletedCount).isEqualTo(500),
                () -> assertThat(countAfter).isEqualTo(500)
        );
    }

    @Test
    void purgeAllLogs_ShouldExecuteSingleDeleteAllQuery() {
        // When
        adminService.purgeAllLogs();
        entityManager.flush();
        long countAfter = auditLogRepository.count();

        // Then
        assertAll(
                () -> assertThat(countAfter).isZero()
        );
    }
}
