package com.techcorp.employee.service;

import com.techcorp.employee.exception.InvalidSalaryException;
import com.techcorp.employee.model.Employee;
import com.techcorp.employee.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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


    public void applyCompanyWideRaise(String company, Double percentageIncrease) {
        System.out.println("Starting company-wide raise for: " + company);
        auditService.logEvent("Starting company-wide raise for " + company + ": " + percentageIncrease + "%");

        List<Employee> employees = employeeRepository.findByCompany(company);
        int successCount = 0;
        int failureCount = 0;

        for (Employee employee : employees) {
            try {
                Double newSalary = calculateNewSalary(employee.getSalary(), percentageIncrease);
                // Wywołanie przez wstrzyknięty serwis (proxy Springa) - transakcja działa
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

    /**
     * Podwyżka dla pracowników z określonego stanowiska.
     */
    public void applyPositionBasedRaise(String positionName, Double percentageIncrease) {
        System.out.println("Starting position-based raise for: " + positionName);

        // Pobranie wszystkich pracowników z danego stanowiska
        List<Employee> allEmployees = employeeRepository.findAll();
        List<Employee> filteredEmployees = allEmployees.stream()
                .filter(e -> e.getPosition().name().equalsIgnoreCase(positionName))
                .toList();

        processBatchRaise(filteredEmployees, percentageIncrease, "position " + positionName);
    }

    /**
     * Podwyżka dla pracowników bez departamentu.
     */
    public void applyRaiseForEmployeesWithoutDepartment(Double percentageIncrease) {
        System.out.println("Starting raise for employees without department");

        List<Employee> employees = employeeRepository.findByDepartmentIsNull();
        processBatchRaise(employees, percentageIncrease, "employees without department");
    }

    // Metoda pomocnicza do przetwarzania wsadowego
    private void processBatchRaise(List<Employee> employees, Double percentageIncrease, String groupName) {
        auditService.logEvent("Starting raise for " + groupName + ": " + percentageIncrease + "%");

        int successCount = 0;
        int failureCount = 0;

        for (Employee employee : employees) {
            try {
                Double newSalary = calculateNewSalary(employee.getSalary(), percentageIncrease);
                // Wywołanie przez wstrzyknięty serwis - transakcja działa
                salaryService.updateSalary(employee.getId(), newSalary);
                successCount++;
            } catch (InvalidSalaryException e) {
                auditService.logEvent("Failed to update salary for " +
                        employee.getName() + " in " + groupName + ": " + e.getMessage());
                failureCount++;
            }
        }

        String summary = String.format(
                "Raise for %s completed. Success: %d, Failures: %d, Total: %d",
                groupName, successCount, failureCount, employees.size()
        );

        auditService.logEvent(summary);
        System.out.println(summary);
    }

    private Double calculateNewSalary(Double currentSalary, Double percentageIncrease) {
        return currentSalary * (1 + percentageIncrease / 100);
    }
}