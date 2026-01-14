package com.techcorp.employee.repository;

import com.techcorp.employee.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    // Metoda batch delete będzie dostępna z JpaRepository

    /**
     * Wydajne usuwanie logów starszych niż podana data
     */
    @Modifying
    @Query("DELETE FROM AuditLog a WHERE a.eventDate < :cutoffDate")
    int deleteByEventDateBefore(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Wydajne usuwanie logów z zakresu dat
     */
    @Modifying
    @Query("DELETE FROM AuditLog a WHERE a.eventDate BETWEEN :startDate AND :endDate")
    int deleteByEventDateBetween(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);


}