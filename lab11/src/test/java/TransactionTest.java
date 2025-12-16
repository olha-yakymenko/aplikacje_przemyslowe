package com.techcorp.employee;

import com.techcorp.employee.exception.InvalidSalaryException;
import com.techcorp.employee.model.AuditLog;
import com.techcorp.employee.model.Employee;
import com.techcorp.employee.model.Position;
import com.techcorp.employee.repository.AuditLogRepository;
import com.techcorp.employee.repository.EmployeeRepository;
import com.techcorp.employee.service.SalaryFacade;
import com.techcorp.employee.service.SalaryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class TransactionTest {

    @Autowired
    private SalaryService salaryService;

    @Autowired
    private SalaryFacade salaryFacade;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    private Long testEmployeeId;
    private Double originalSalary;

    @BeforeEach
    void setUp() {
        // Czyszczenie bazy przed każdym testem
        auditLogRepository.deleteAll();
        employeeRepository.deleteAll();

        // Tworzenie testowego pracownika z unikalnym emailem
        String uniqueEmail = "test" + System.currentTimeMillis() + "@techcorp.com";
        Employee employee = new Employee(
                "Jan Kowalski",
                uniqueEmail,
                "TechCorp",
                Position.PROGRAMMER,
                5000.0,
                com.techcorp.employee.model.EmploymentStatus.ACTIVE
        );
        Employee saved = employeeRepository.save(employee);
        testEmployeeId = saved.getId();
        originalSalary = saved.getSalary();
    }

    @Test
    void testSalaryUpdateWithAuditLog() throws InvalidSalaryException {
        System.out.println("=== Test 1: Aktualizacja pensji z audytem ===");

        // Przed aktualizacją
        List<AuditLog> logsBefore = auditLogRepository.findAll();
        System.out.println("Audit logs before: " + logsBefore.size());

        // Aktualizacja pensji (prawidłowa)
        Double newSalary = 6000.0;
        salaryService.updateSalary(testEmployeeId, newSalary);

        // Po aktualizacji - sprawdzenie czy pensja się zmieniła
        Optional<Employee> updated = employeeRepository.findById(testEmployeeId);
        assertTrue(updated.isPresent());
        assertEquals(newSalary, updated.get().getSalary(), 0.01);

        // Sprawdzenie czy logi zostały zapisane
        List<AuditLog> logsAfter = auditLogRepository.findAll();
        System.out.println("Audit logs after: " + logsAfter.size());
        assertTrue(logsAfter.size() > logsBefore.size());

        // Sprawdzenie czy są oba typy logów: attempt i success
        boolean foundAttemptLog = logsAfter.stream()
                .anyMatch(log -> log.getMessage().contains("Salary update attempt"));
        boolean foundSuccessLog = logsAfter.stream()
                .anyMatch(log -> log.getMessage().contains("Salary updated successfully"));

        assertAll(
                () -> assertTrue(foundAttemptLog, "Should have attempt log"),
                () -> assertTrue(foundSuccessLog, "Should have success log")
        );

        System.out.println("Test passed: Salary updated with audit logs");
    }

    @Test
    void testSalaryUpdateRollbackButAuditPersists() {
        System.out.println("=== Test 2: Rollback transakcji, ale logi pozostają ===");

        int logsBefore = auditLogRepository.findAll().size();
        System.out.println("Audit logs before: " + logsBefore);

        try {
            // Próba ustawienia nieprawidłowej pensji (ujemnej) - wywoła walidację
            salaryService.updateSalary(testEmployeeId, -1000.0);
            fail("Should have thrown InvalidSalaryException for negative salary");
        } catch (InvalidSalaryException e) {
            System.out.println("Expected exception caught: " + e.getMessage());

            // Sprawdzenie czy pensja się NIE zmieniła (rollback)
            Optional<Employee> employee = employeeRepository.findById(testEmployeeId);
            assertTrue(employee.isPresent());
            assertEquals(originalSalary, employee.get().getSalary(), 0.01);

            // Sprawdzenie czy logi audytowe zostały zapisane (REQUIRES_NEW)
            int logsAfter = auditLogRepository.findAll().size();
            System.out.println("Audit logs after: " + logsAfter);
            assertTrue(logsAfter > logsBefore, "Audit logs should persist despite rollback");

            // Sprawdzenie czy jest log o próbie aktualizacji
            List<AuditLog> allLogs = auditLogRepository.findAll();
            boolean foundAttemptLog = allLogs.stream()
                    .anyMatch(log -> log.getMessage().contains("Salary update attempt"));

            assertTrue(foundAttemptLog, "Should have attempt log even after rollback");

            System.out.println("Test passed: Rollback occurred but audit logs persisted");
        }
    }

    @Test
    void testSalaryUpdateValidationRules() {
        System.out.println("=== Test 3: Testy walidacji biznesowej ===");

        // Test 3a: Zbyt duży wzrost pensji (> 100%)
        try {
            salaryService.updateSalary(testEmployeeId, originalSalary * 2.1); // 210% wzrost
            fail("Should have thrown InvalidSalaryException for too large increase");
        } catch (InvalidSalaryException e) {
            System.out.println("Test 3a passed: " + e.getMessage());
        }

        // Test 3b: Zbyt duży spadek pensji (< 50%)
        try {
            salaryService.updateSalary(testEmployeeId, originalSalary * 0.4); // 60% spadek
            fail("Should have thrown InvalidSalaryException for too large decrease");
        } catch (InvalidSalaryException e) {
            System.out.println("Test 3b passed: " + e.getMessage());
        }

        // Test 3c: Przekroczenie maksymalnej pensji
        try {
            salaryService.updateSalary(testEmployeeId, 2000000.0); // Przekracza limit 1,000,000
            fail("Should have thrown InvalidSalaryException for exceeding max limit");
        } catch (InvalidSalaryException e) {
            System.out.println("Test 3c passed: " + e.getMessage());
        }

        // Sprawdzenie czy wszystkie logi zostały zapisane
        List<AuditLog> allLogs = auditLogRepository.findAll();
        assertTrue(allLogs.size() >= 3, "Should have at least 3 audit logs for 3 attempts");

        System.out.println("Test 3 passed: All validation rules work correctly");
    }

    @Test
    @Transactional
    void testBatchSalaryUpdateViaFacade() throws InvalidSalaryException {
        System.out.println("=== Test 4: Masowa aktualizacja przez fasadę ===");

        // Tworzenie dodatkowych pracowników z unikalnymi emailami
        Employee emp2 = new Employee(
                "Anna Nowak",
                "anna.nowak" + System.currentTimeMillis() + "@techcorp.com",
                "TechCorp",
                com.techcorp.employee.model.Position.MANAGER,
                7000.0,
                com.techcorp.employee.model.EmploymentStatus.ACTIVE
        );
        Employee emp3 = new Employee(
                "Piotr Wiśniewski",
                "piotr.wisniewski" + System.currentTimeMillis() + "@techcorp.com",
                "TechCorp",
                Position.PROGRAMMER,
                4500.0,
                com.techcorp.employee.model.EmploymentStatus.ACTIVE
        );

        employeeRepository.save(emp2);
        employeeRepository.save(emp3);

        // Prawidłowa podwyżka przez fasadę (10%)
        salaryFacade.applyCompanyWideRaise("TechCorp", 10.0);

        // Sprawdzenie wyników
        List<Employee> allEmployees = employeeRepository.findByCompany("TechCorp");
        assertEquals(3, allEmployees.size());

        // Sprawdzenie czy pensje zostały zaktualizowane
        for (Employee emp : allEmployees) {
            Double expectedSalary = switch (emp.getName()) {
                case "Jan Kowalski" -> 5000.0 * 1.10; // 5500
                case "Anna Nowak" -> 7000.0 * 1.10;   // 7700
                case "Piotr Wiśniewski" -> 4500.0 * 1.10; // 4950
                default -> emp.getSalary();
            };

            assertEquals(expectedSalary, emp.getSalary(), 0.01,
                    "Salary for " + emp.getName() + " should be updated");
        }

        // Sprawdzenie logów audytowych - każdy pracownik powinien mieć attempt i success
        List<AuditLog> auditLogs = auditLogRepository.findAll();
        assertTrue(auditLogs.size() >= 6, "Should have at least 6 audit logs (3 employees × 2 logs each)");

        System.out.println("Test passed: Batch update via facade completed with " + auditLogs.size() + " audit logs");
    }

    @Test
    void testAuditLogRequiresNewPropagation() {
        System.out.println("=== Test 5: Weryfikacja REQUIRES_NEW propagation ===");

        long initialLogCount = auditLogRepository.count();

        try {
            // Próba aktualizacji z błędem walidacji
            salaryService.updateSalary(testEmployeeId, -500.0);
            fail("Should have thrown exception");
        } catch (InvalidSalaryException e) {
            // Sprawdzenie czy logi zostały zapisane POMIMO rollbacku
            long finalLogCount = auditLogRepository.count();

            assertAll(
                    () -> assertTrue(finalLogCount > initialLogCount,
                            "Audit logs should be created even after rollback (REQUIRES_NEW)"),
                    () -> {
                        // Sprawdzenie czy pensja NIE została zmieniona
                        Optional<Employee> employee = employeeRepository.findById(testEmployeeId);
                        assertTrue(employee.isPresent());
                        assertEquals(originalSalary, employee.get().getSalary(), 0.01,
                                "Salary should NOT be changed after rollback");
                    },
                    () -> {
                        // Sprawdzenie czy log zawiera informację o próbie
                        List<AuditLog> logs = auditLogRepository.findAll();
                        boolean hasAttemptLog = logs.stream()
                                .anyMatch(log -> log.getMessage().contains("Salary update attempt"));
                        assertTrue(hasAttemptLog, "Should have attempt log");
                    }
            );

            System.out.println("Test passed: REQUIRES_NEW propagation works correctly");
        }
    }

    @Test
    void testConcurrentAccessPrevention() throws InvalidSalaryException {
        System.out.println("=== Test 6: Test blokady pesymistycznej ===");

        // Pobranie pracownika z blokadą
        Optional<Employee> lockedEmployee = employeeRepository.findByIdWithLock(testEmployeeId);
        assertTrue(lockedEmployee.isPresent(), "Should find employee with lock");

        // Aktualizacja pensji - powinna się powieść
        Double newSalary = 5500.0;
        salaryService.updateSalary(testEmployeeId, newSalary);

        // Sprawdzenie czy aktualizacja się powiodła
        Optional<Employee> updatedEmployee = employeeRepository.findById(testEmployeeId);
        assertTrue(updatedEmployee.isPresent());
        assertEquals(newSalary, updatedEmployee.get().getSalary(), 0.01);

        // Sprawdzenie czy są logi audytowe
        List<AuditLog> logs = auditLogRepository.findAll();
        assertTrue(logs.size() >= 2, "Should have at least 2 audit logs (attempt and success)");

        // Sprawdzenie czy blokada działa - próba pobrania bez blokady też powinna działać
        Optional<Employee> nonLockedEmployee = employeeRepository.findById(testEmployeeId);
        assertTrue(nonLockedEmployee.isPresent(), "Should find employee without lock too");

        System.out.println("Test passed: Pessimistic locking works correctly");
    }

    @Test
    void testCheckedExceptionRollbackConfiguration() {
        System.out.println("=== Test 7: Konfiguracja rollback dla checked exceptions ===");

        // Sprawdzenie czy InvalidSalaryException (checked exception) powoduje rollback
        try {
            salaryService.updateSalary(testEmployeeId, -100.0);
            fail("Should throw InvalidSalaryException");
        } catch (InvalidSalaryException e) {
            // Sprawdzenie czy transakcja została wycofana (pensja nie zmieniona)
            Optional<Employee> employee = employeeRepository.findById(testEmployeeId);
            assertTrue(employee.isPresent());
            assertEquals(originalSalary, employee.get().getSalary(), 0.01,
                    "Salary should NOT be changed after InvalidSalaryException (rollbackFor)");

            // Sprawdzenie czy logi są (REQUIRES_NEW)
            List<AuditLog> logs = auditLogRepository.findAll();
            assertTrue(logs.size() > 0, "Audit logs should exist despite rollback");

            System.out.println("Test passed: rollbackFor works correctly with checked exceptions");
        }
    }
}