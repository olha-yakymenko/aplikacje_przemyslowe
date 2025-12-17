package com.techcorp.employee.service;

import com.techcorp.employee.exception.InvalidSalaryException;
import com.techcorp.employee.model.Employee;
import com.techcorp.employee.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;


@Service
public class SalaryFacade {

    private final SalaryService salaryService;
    private final EmployeeRepository employeeRepository;
    private final AuditService auditService;

    @Autowired
    public SalaryFacade(SalaryService salaryService,
                        EmployeeRepository employeeRepository,
                        AuditService auditService) {
        this.salaryService = salaryService;
        this.employeeRepository = employeeRepository;
        this.auditService = auditService;
    }


    public void applyCompanyWideRaise(String company, BigDecimal percentageIncrease) {
        System.out.println("Starting company-wide raise for: " + company);
        auditService.logEvent("Starting company-wide raise for " + company + ": " + percentageIncrease + "%");

        List<Employee> employees = employeeRepository.findByCompany(company);
        int successCount = 0;
        int failureCount = 0;

        for (Employee employee : employees) {
            try {
                BigDecimal newSalary = calculateNewSalary(employee.getSalary(), percentageIncrease);
                salaryService.updateSalary(employee.getId(), newSalary);
                successCount++;
            } catch (InvalidSalaryException e) {
                auditService.logEvent("Failed to update salary for " +
                        employee.getName() + ": " + e.getMessage());
                failureCount++;
            } catch (Exception e) {
                auditService.logEvent("Unexpected error for " +
                        employee.getName() + ": " + e.getMessage());
                failureCount++;
            }
        }

        String summary = String.format(
                "Company-wide raise completed. Success: %d, Failures: %d, Total: %d",
                successCount, failureCount, employees.size()
        );

        auditService.logEvent(summary);
        System.out.println(summary);
    }


    private BigDecimal calculateNewSalary(BigDecimal currentSalary, BigDecimal percentageIncrease) {
        if (currentSalary == null || percentageIncrease == null) {
            throw new IllegalArgumentException("Arguments cannot be null");
        }
        BigDecimal multiplier = BigDecimal.ONE
                .add(percentageIncrease.divide(new BigDecimal("100"), 4, java.math.RoundingMode.HALF_UP));

        return currentSalary.multiply(multiplier)
                .setScale(2, java.math.RoundingMode.HALF_UP);
    }
}