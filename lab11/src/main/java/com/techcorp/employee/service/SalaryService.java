package com.techcorp.employee.service;

import com.techcorp.employee.exception.InvalidSalaryException;
import com.techcorp.employee.model.Employee;
import com.techcorp.employee.repository.EmployeeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class SalaryService {

    private static final Logger logger = LoggerFactory.getLogger(SalaryService.class);
    private static final double MAX_INCREASE_PERCENT = 100.0;
    private static final double MAX_DECREASE_PERCENT = 50.0;
    private static final double MAX_SALARY_LIMIT = 1_000_000.0;
    private static final double MIN_SALARY = 0.0;

    private final EmployeeRepository employeeRepository;
    private final AuditService auditService;

    @Autowired
    public SalaryService(EmployeeRepository employeeRepository, AuditService auditService) {
        this.employeeRepository = employeeRepository;
        this.auditService = auditService;
    }


    @Transactional(rollbackFor = {InvalidSalaryException.class, RuntimeException.class})
    public void updateSalary(Long employeeId, Double newSalary) throws InvalidSalaryException {
        logger.info("Starting salary update transaction for employee ID: {}", employeeId);

        Employee employee = findEmployeeWithLock(employeeId);
        Double oldSalary = employee.getSalary();

        logSalaryUpdateAttempt(employee, oldSalary, newSalary);
        validateSalary(newSalary, oldSalary);

        updateEmployeeSalary(employee, newSalary);
        logSalaryUpdateSuccess(employee, oldSalary, newSalary);

        logger.info("Salary update transaction completed successfully for: {}", employee.getName());
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

    private void logSalaryUpdateAttempt(Employee employee, Double oldSalary, Double newSalary) {
        auditService.logSalaryUpdateAttempt(
                employee.getId(),
                employee.getName(),
                oldSalary,
                newSalary
        );
    }

    private void updateEmployeeSalary(Employee employee, Double newSalary) {
        employee.setSalary(newSalary);
        logger.info("Salary updated in database for: {}", employee.getName());
    }

    private void logSalaryUpdateSuccess(Employee employee, Double oldSalary, Double newSalary) {
        auditService.logSalaryUpdateSuccess(
                employee.getId(),
                employee.getName(),
                oldSalary,
                newSalary
        );
    }

    private void validateSalary(Double newSalary, Double oldSalary) throws InvalidSalaryException {
        validateSalaryNotNullAndPositive(newSalary);
        validateSalaryLimit(newSalary);
        validateSalaryChangePercentage(oldSalary, newSalary);
    }

    private void validateSalaryNotNullAndPositive(Double newSalary) throws InvalidSalaryException {
        if (newSalary == null || newSalary < MIN_SALARY) {
            throw new InvalidSalaryException("Salary must be a positive number");
        }
    }

    private void validateSalaryLimit(Double newSalary) throws InvalidSalaryException {
        if (newSalary > MAX_SALARY_LIMIT) {
            throw new InvalidSalaryException(
                    String.format("Salary exceeds maximum allowed limit (%.2f)", MAX_SALARY_LIMIT)
            );
        }
    }

    private void validateSalaryChangePercentage(Double oldSalary, Double newSalary)
            throws InvalidSalaryException {

        if (oldSalary == 0.0) {
            if (newSalary > 0) {
                throw new InvalidSalaryException("Cannot calculate percentage increase from zero salary");
            }
            return;
        }

        double changePercentage = calculatePercentageChange(oldSalary, newSalary);

        if (changePercentage > MAX_INCREASE_PERCENT) {
            throw new InvalidSalaryException(
                    String.format("Salary increase too large (%.2f -> %.2f). Max %.0f%% increase allowed.",
                            oldSalary, newSalary, MAX_INCREASE_PERCENT)
            );
        }

        if (changePercentage < -MAX_DECREASE_PERCENT) {
            throw new InvalidSalaryException(
                    String.format("Salary decrease too large (%.2f -> %.2f). Max %.0f%% decrease allowed.",
                            oldSalary, newSalary, MAX_DECREASE_PERCENT)
            );
        }
    }

    private double calculatePercentageChange(Double oldValue, Double newValue) {
        if (oldValue == 0.0) {
            return 0.0;
        }

        return ((newValue - oldValue) / oldValue) * 100.0;
    }

}