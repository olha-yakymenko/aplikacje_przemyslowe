package com.techcorp.employee.service;

import com.techcorp.employee.exception.InvalidSalaryException;
import com.techcorp.employee.model.Employee;
import com.techcorp.employee.repository.EmployeeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

@Service
public class SalaryService {

    private static final Logger logger = LoggerFactory.getLogger(SalaryService.class);
    private static final BigDecimal MAX_INCREASE_PERCENT = new BigDecimal("100.0");
    private static final BigDecimal MAX_DECREASE_PERCENT = new BigDecimal("50.0");
    private static final BigDecimal MAX_SALARY_LIMIT = new BigDecimal("1000000.00");
    private static final BigDecimal MIN_SALARY = BigDecimal.ZERO;
    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100.00");

    private final EmployeeRepository employeeRepository;
    private final AuditService auditService;

    @Autowired
    public SalaryService(EmployeeRepository employeeRepository, AuditService auditService) {
        this.employeeRepository = employeeRepository;
        this.auditService = auditService;
    }


    @Transactional(
            isolation = Isolation.REPEATABLE_READ,
            rollbackFor = {InvalidSalaryException.class, RuntimeException.class},
            timeout = 30
    )
    public void updateSalary(Long employeeId, BigDecimal newSalary) throws InvalidSalaryException {
        logger.info("Starting salary update with PESSIMISTIC_WRITE lock for employee ID: {}", employeeId);

        Employee employee = findEmployeeWithLock(employeeId);
        BigDecimal oldSalary = employee.getSalary();

        auditService.logSalaryUpdateAttempt(
                employee.getId(),
                employee.getName(),
                oldSalary.doubleValue(),
                newSalary.doubleValue()
        );

        validateSalary(newSalary, oldSalary);

        employee.setSalary(newSalary);
        employeeRepository.save(employee);

        auditService.logSalaryUpdateSuccess(
                employee.getId(),
                employee.getName(),
                oldSalary.doubleValue(),
                newSalary.doubleValue()
        );

        logger.info("Salary updated for: {} (ID: {})", employee.getName(), employeeId);
    }


    private Employee findEmployeeWithLock(Long employeeId) throws InvalidSalaryException {
        Optional<Employee> employeeOpt = employeeRepository.findByIdWithLock(employeeId);

        if (employeeOpt.isEmpty()) {
            String errorMsg = "Employee not found with ID: " + employeeId;
            auditService.logSalaryUpdateFailure(employeeId, "Unknown", errorMsg);
            throw new InvalidSalaryException(errorMsg);
        }

        return employeeOpt.get();
    }


    private void validateSalary(BigDecimal newSalary, BigDecimal oldSalary) throws InvalidSalaryException {
        if (newSalary == null || newSalary.compareTo(MIN_SALARY) < 0) {
            throw new InvalidSalaryException("Salary must be a positive number");
        }

        if (newSalary.compareTo(MAX_SALARY_LIMIT) > 0) {
            throw new InvalidSalaryException(
                    String.format("Salary exceeds maximum allowed limit (%s)",
                            MAX_SALARY_LIMIT.toPlainString())
            );
        }

        validateSalaryChangePercentage(oldSalary, newSalary);
    }


    private void validateSalaryChangePercentage(BigDecimal oldSalary, BigDecimal newSalary)
            throws InvalidSalaryException {

        if (oldSalary.compareTo(BigDecimal.ZERO) == 0) {
            if (newSalary.compareTo(BigDecimal.ZERO) > 0) {
                throw new InvalidSalaryException("Cannot calculate percentage increase from zero salary");
            }
            return;
        }

        BigDecimal changePercentage = calculatePercentageChange(oldSalary, newSalary);

        if (changePercentage.compareTo(MAX_INCREASE_PERCENT) > 0) {
            throw new InvalidSalaryException(
                    String.format("Salary increase too large (%s -> %s). Max %s%% increase allowed.",
                            oldSalary.setScale(2, RoundingMode.HALF_UP).toPlainString(),
                            newSalary.setScale(2, RoundingMode.HALF_UP).toPlainString(),
                            MAX_INCREASE_PERCENT.toPlainString())
            );
        }

        if (changePercentage.compareTo(MAX_DECREASE_PERCENT.negate()) < 0) {
            throw new InvalidSalaryException(
                    String.format("Salary decrease too large (%s -> %s). Max %s%% decrease allowed.",
                            oldSalary.setScale(2, RoundingMode.HALF_UP).toPlainString(),
                            newSalary.setScale(2, RoundingMode.HALF_UP).toPlainString(),
                            MAX_DECREASE_PERCENT.toPlainString())
            );
        }
    }


    private BigDecimal calculatePercentageChange(BigDecimal oldValue, BigDecimal newValue) {
        if (oldValue.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        return newValue.subtract(oldValue)
                .divide(oldValue, 4, RoundingMode.HALF_UP)
                .multiply(ONE_HUNDRED);
    }
}