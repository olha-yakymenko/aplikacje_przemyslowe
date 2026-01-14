package com.techcorp.employee.repository;

import com.techcorp.employee.model.AuditLog;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditLogReportRepository extends PagingAndSortingRepository<AuditLog, Long> {

    // Metody specyficzne dla raportowania
    // Paginacja i sortowanie są już dostępne dzięki PagingAndSortingRepository
}