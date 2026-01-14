package com.techcorp.employee;

import com.techcorp.employee.exception.InvalidSalaryException;
import com.techcorp.employee.model.AuditLog;
import com.techcorp.employee.model.Employee;
import com.techcorp.employee.model.EmploymentStatus;
import com.techcorp.employee.model.Position;
import com.techcorp.employee.repository.AuditLogRepository;
import com.techcorp.employee.repository.EmployeeRepository;
import com.techcorp.employee.service.SalaryFacade;
import com.techcorp.employee.service.SalaryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc(addFilters = false)
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
    private BigDecimal originalSalary;

    @BeforeEach
    void setUp() {
        // Czyszczenie bazy przed każdym testem
        auditLogRepository.deleteAll();
        employeeRepository.deleteAll();

        // Tworzenie testowego pracownika z unikalnym emailem
        String uniqueEmail = "test" + System.currentTimeMillis() + "@techcorp.com";

        Employee employee = new Employee();
        employee.setName("Jan Kowalski");
        employee.setEmail(uniqueEmail);
        employee.setCompany("TechCorp");
        employee.setPosition(Position.PROGRAMMER);
        employee.setSalary(new BigDecimal("5000.00"));
        employee.setStatus(EmploymentStatus.ACTIVE);

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
        BigDecimal newSalary = new BigDecimal("6000.00");
        salaryService.updateSalary(testEmployeeId, newSalary);

        // Po aktualizacji - sprawdzenie czy pensja się zmieniła
        Optional<Employee> updated = employeeRepository.findById(testEmployeeId);
        assertTrue(updated.isPresent());
        assertEquals(0, newSalary.compareTo(updated.get().getSalary()));

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

    //1 podpunkt
    @Test
    void testSalaryUpdateRollbackButAuditPersists() {
        System.out.println("=== Test 2: Rollback transakcji, ale logi pozostają ===");

        int logsBefore = auditLogRepository.findAll().size();
        System.out.println("Audit logs before: " + logsBefore);

        try {
            // Próba ustawienia nieprawidłowej pensji (ujemnej) - wywoła walidację
            BigDecimal negativeSalary = new BigDecimal("-1000.00");
            salaryService.updateSalary(testEmployeeId, negativeSalary);
            fail("Should have thrown InvalidSalaryException for negative salary");
        } catch (InvalidSalaryException e) {
            System.out.println("Expected exception caught: " + e.getMessage());

            // Sprawdzenie czy pensja się NIE zmieniła (rollback)
            Optional<Employee> employee = employeeRepository.findById(testEmployeeId);
            assertTrue(employee.isPresent());
            assertEquals(0, originalSalary.compareTo(employee.get().getSalary()));

            // Sprawdzenie czy logi audytowe zostały zapisane (REQUIRES_NEW)
            int logsAfter = auditLogRepository.findAll().size();
            System.out.println("Audit logs after: " + logsAfter);

            // POWINNO BYĆ WIĘCEJ LOGÓW - to kluczowy test!
            assertTrue(logsAfter > logsBefore,
                    "Audit logs should persist despite rollback. Before: " + logsBefore + ", After: " + logsAfter);

            // Sprawdzenie czy jest log o próbie aktualizacji
            List<AuditLog> allLogs = auditLogRepository.findAll();
            boolean foundAttemptLog = allLogs.stream()
                    .anyMatch(log -> log.getMessage().contains("Salary update attempt"));

            assertTrue(foundAttemptLog, "Should have attempt log even after rollback");

            System.out.println("Test passed: Rollback occurred but audit logs persisted");
        }
    }

    @Test
    void testAuditLogRequiresNewPropagation() {
        System.out.println("=== Test 5: Weryfikacja REQUIRES_NEW propagation ===");

        long initialLogCount = auditLogRepository.count();
        System.out.println("Initial audit log count: " + initialLogCount);

        try {
            // Próba aktualizacji z błędem walidacji
            BigDecimal invalidSalary = new BigDecimal("-500.00");
            salaryService.updateSalary(testEmployeeId, invalidSalary);
            fail("Should have thrown exception");
        } catch (InvalidSalaryException e) {
            // Sprawdzenie czy logi zostały zapisane POMIMO rollbacku
            long finalLogCount = auditLogRepository.count();
            System.out.println("Final audit log count: " + finalLogCount);

            assertAll(
                    () -> assertTrue(finalLogCount > initialLogCount,
                            "Audit logs should be created even after rollback (REQUIRES_NEW). Initial: "
                                    + initialLogCount + ", Final: " + finalLogCount),
                    () -> {
                        // Sprawdzenie czy pensja NIE została zmieniona
                        Optional<Employee> employee = employeeRepository.findById(testEmployeeId);
                        assertTrue(employee.isPresent());
                        assertEquals(0, originalSalary.compareTo(employee.get().getSalary()),
                                "Salary should NOT be changed after rollback");
                    }
            );

            System.out.println("Test passed: REQUIRES_NEW propagation works correctly");
        }
    }

    @Test
    void testCheckedExceptionRollbackConfiguration() {
        System.out.println("=== Test 7: Konfiguracja rollback dla checked exceptions ===");

        long logsBefore = auditLogRepository.count();

        // Sprawdzenie czy InvalidSalaryException (checked exception) powoduje rollback
        try {
            BigDecimal invalidSalary = new BigDecimal("-100.00");
            salaryService.updateSalary(testEmployeeId, invalidSalary);
            fail("Should throw InvalidSalaryException");
        } catch (InvalidSalaryException e) {
            // Sprawdzenie czy transakcja została wycofana (pensja nie zmieniona)
            Optional<Employee> employee = employeeRepository.findById(testEmployeeId);
            assertTrue(employee.isPresent());
            assertEquals(0, originalSalary.compareTo(employee.get().getSalary()),
                    "Salary should NOT be changed after InvalidSalaryException (rollbackFor)");

            // Sprawdzenie czy logi są (REQUIRES_NEW)
            long logsAfter = auditLogRepository.count();
            assertTrue(logsAfter > logsBefore,
                    "Audit logs should exist despite rollback. Before: " + logsBefore + ", After: " + logsAfter);

            System.out.println("Test passed: rollbackFor works correctly with checked exceptions");
        }
    }

    @Test
    void testConcurrentAccessPrevention() throws InvalidSalaryException {
        System.out.println("=== Test 6: Test blokady pesymistycznej ===");

        // Pobranie pracownika z blokadą
        Optional<Employee> lockedEmployee = employeeRepository.findByIdWithLock(testEmployeeId);
        assertTrue(lockedEmployee.isPresent(), "Should find employee with lock");

        // Aktualizacja pensji - powinna się powieść
        BigDecimal newSalary = new BigDecimal("5500.00");
        salaryService.updateSalary(testEmployeeId, newSalary);

        // Sprawdzenie czy aktualizacja się powiodła
        Optional<Employee> updatedEmployee = employeeRepository.findById(testEmployeeId);
        assertTrue(updatedEmployee.isPresent());
        assertEquals(0, newSalary.compareTo(updatedEmployee.get().getSalary()));

        System.out.println("Test passed: Pessimistic locking works correctly");
    }

    @Test
    void testBatchSalaryUpdateViaFacade() {
        System.out.println("=== Test 4: Masowa aktualizacja przez fasadę ===");

        // Tworzenie dodatkowych pracowników z unikalnymi emailami
        createTestEmployee("Anna Nowak", "anna.nowak", new BigDecimal("7000.00"), Position.MANAGER);
        createTestEmployee("Piotr Wiśniewski", "piotr.wisniewski", new BigDecimal("4500.00"), Position.PROGRAMMER);

        // Logi przed aktualizacją
        long logsBefore = auditLogRepository.count();
        System.out.println("Audit logs before batch update: " + logsBefore);

        // Prawidłowa podwyżka przez fasadę (10%)
        salaryFacade.applyCompanyWideRaise("TechCorp", new BigDecimal("10.00"));

        // Sprawdzenie czy logi zostały zapisane
        long logsAfter = auditLogRepository.count();
        System.out.println("Audit logs after batch update: " + logsAfter);
        assertTrue(logsAfter > logsBefore, "Should have more audit logs after batch update");

        System.out.println("Test passed: Batch update via facade completed");
    }

    @Test
    void testSalaryUpdateValidationRules() {
        System.out.println("=== Test 3: Testy walidacji biznesowej ===");

        long initialLogCount = auditLogRepository.count();
        int testCount = 0;

        // Test 3a: Zbyt duży wzrost pensji (> 100%)
        try {
            // originalSalary = 5000, 210% = 10500
            BigDecimal tooHighIncrease = originalSalary.multiply(new BigDecimal("2.10"));
            salaryService.updateSalary(testEmployeeId, tooHighIncrease);
            fail("Should have thrown InvalidSalaryException for too large increase");
        } catch (InvalidSalaryException e) {
            testCount++;
            System.out.println("Test 3a passed: " + e.getMessage());
        }

        // Test 3b: Zbyt duży spadek pensji (< -50%)
        try {
            // 60% spadek = 40% original = 2000
            BigDecimal tooLargeDecrease = originalSalary.multiply(new BigDecimal("0.40"));
            salaryService.updateSalary(testEmployeeId, tooLargeDecrease);
            fail("Should have thrown InvalidSalaryException for too large decrease");
        } catch (InvalidSalaryException e) {
            testCount++;
            System.out.println("Test 3b passed: " + e.getMessage());
        }

        // Test 3c: Przekroczenie maksymalnej pensji
        try {
            BigDecimal exceedMax = new BigDecimal("2000000.00");
            salaryService.updateSalary(testEmployeeId, exceedMax);
            fail("Should have thrown InvalidSalaryException for exceeding max limit");
        } catch (InvalidSalaryException e) {
            testCount++;
            System.out.println("Test 3c passed: " + e.getMessage());
        }

        // Sprawdzenie czy wszystkie logi zostały zapisane
        long finalLogCount = auditLogRepository.count();
        System.out.println("Initial logs: " + initialLogCount + ", Final logs: " + finalLogCount);

        // Każdy test powinien dodać przynajmniej 1 log (attempt)
        assertTrue(finalLogCount >= initialLogCount + testCount,
                "Should have at least " + testCount + " audit logs for " + testCount + " attempts");

        System.out.println("Test 3 passed: All validation rules work correctly");
    }

    private void createTestEmployee(String name, String emailPrefix, BigDecimal salary, Position position) {
        String uniqueEmail = emailPrefix + System.currentTimeMillis() + "@techcorp.com";
        Employee employee = new Employee();
        employee.setName(name);
        employee.setEmail(uniqueEmail);
        employee.setCompany("TechCorp");
        employee.setPosition(position);
        employee.setSalary(salary);
        employee.setStatus(EmploymentStatus.ACTIVE);
        employeeRepository.save(employee);
    }
}