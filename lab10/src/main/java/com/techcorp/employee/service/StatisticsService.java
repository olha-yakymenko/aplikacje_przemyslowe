

package com.techcorp.employee.service;

import com.techcorp.employee.dto.CompanyStatisticsDTO;
import com.techcorp.employee.model.*;
import com.techcorp.employee.repository.EmployeeRepository;
import com.techcorp.employee.repository.DepartmentRepository;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@Validated
public class StatisticsService {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private DepartmentRepository departmentRepository;


    public Map<String, Object> getAllStatistics() {
        Map<String, Object> statistics = new HashMap<>();

        // OGÓLNE STATYSTYKI - W SQL
        statistics.put("totalEmployees", employeeRepository.countAllEmployees());
        statistics.put("avgSalary", getSafeDouble(employeeRepository.findAverageSalary()));
        statistics.put("maxSalary", getSafeDouble(employeeRepository.findMaxSalary()));
        statistics.put("minSalary", getSafeDouble(employeeRepository.findMinSalary()));
        statistics.put("totalSalaryCost", getSafeDouble(employeeRepository.findTotalSalaryCost()));

        // STATYSTYKI PER FIRMA
        statistics.put("companyStats", getCompanyStatisticsFromSQL());

        // ROZKŁAD STANOWISK
        statistics.put("positionDistribution", getPositionDistributionFromSQL());

        // ROZKŁAD STATUSÓW
        statistics.put("statusDistribution", getStatusDistributionFromSQL());

        // STATYSTYKI DEPARTAMENTÓW
        statistics.put("totalDepartments", departmentRepository.count());
        statistics.put("employeesWithoutDept",
                getSafeLong(employeeRepository.countEmployeesWithoutDepartment()));

        return statistics;
    }

    public CompanyStatistics getCompanyStatistics(
            @NotBlank(message = "Company name cannot be blank") String companyName) {        // Użyj DTO z repozytorium i nowego konstruktora
        CompanyStatisticsDTO dto = employeeRepository.getCompanyStatisticsDTO(companyName)
                .orElse(new CompanyStatisticsDTO(companyName, 0L, 0.0, 0.0, ""));

        // Użyj poprawnego konstruktora z 5 parametrami
        return new CompanyStatistics(
                dto.getCompanyName(),
                dto.getEmployeeCount(),
                dto.getAverageSalary(),
                dto.getHighestSalary(),
                dto.getTopEarnerName()  // To jest poprawny 5-ty parametr
        );
    }

    public List<Employee> getEmployeesByCompany(
            @NotBlank(message = "Company name cannot be blank") String companyName) {
        return employeeRepository.findByCompany(companyName, PageRequest.of(0, 1000))
                .getContent();
    }

    // ===== METODY DLA STATISTICSCONTROLLER (REST API) =====

    public Map<String, Double> getAverageSalary(
            @Size(max = 100) String company) {        Map<String, Double> response = new HashMap<>();

        Double avgSalary = (company != null && !company.trim().isEmpty())
                ? employeeRepository.findAverageSalaryByCompany(company)
                : employeeRepository.findAverageSalary();

        response.put("averageSalary", getSafeDouble(avgSalary));
        return response;
    }

    public CompanyStatisticsDTO getCompanyStatisticsDTO(
            @NotBlank(message = "Company name cannot be blank") String companyName) {        return employeeRepository.getCompanyStatisticsDTO(companyName)
                .orElse(new CompanyStatisticsDTO(companyName, 0L, 0.0, 0.0, ""));
    }

    public Map<String, Integer> getPositionStatistics() {
        return employeeRepository.getPositionStatistics().stream()
                .collect(Collectors.toMap(
                        row -> ((Position) row[0]).name(),
                        row -> ((Long) row[1]).intValue()
                ));
    }

    public Map<String, Integer> getEmploymentStatusStatistics() {
        return employeeRepository.getStatusStatistics().stream()
                .collect(Collectors.toMap(
                        row -> ((EmploymentStatus) row[0]).name(),
                        row -> ((Long) row[1]).intValue()
                ));
    }

    // ===== METODY POMOCNICZE =====

    private Map<String, CompanyStatistics> getCompanyStatisticsFromSQL() {
        List<CompanyStatisticsDTO> dtos = employeeRepository.getCompanyStatisticsDTO();

        return dtos.stream()
                .collect(Collectors.toMap(
                        CompanyStatisticsDTO::getCompanyName,
                        dto -> new CompanyStatistics(
                                dto.getCompanyName(),
                                dto.getEmployeeCount(),
                                dto.getAverageSalary(),
                                dto.getHighestSalary(),
                                dto.getTopEarnerName()
                        )
                ));
    }

    private Map<String, Long> getPositionDistributionFromSQL() {
        List<Object[]> results = employeeRepository.getPositionStatistics();

        Map<String, Long> distribution = new HashMap<>();
        for (Object[] row : results) {
            Position position = (Position) row[0];
            Long count = (Long) row[1];
            distribution.put(position.name(), count);
        }
        return distribution;
    }

    private Map<String, Long> getStatusDistributionFromSQL() {
        List<Object[]> results = employeeRepository.getStatusStatistics();

        Map<String, Long> distribution = new HashMap<>();
        for (Object[] row : results) {
            EmploymentStatus status = (EmploymentStatus) row[0];
            Long count = (Long) row[1];
            distribution.put(status.name(), count);
        }
        return distribution;
    }

    // ===== ZAAWANSOWANE STATYSTYKI =====

    public Map<String, Object> getAdvancedStatistics() {
        Map<String, Object> advanced = new HashMap<>();

        // TOP 10 najlepiej zarabiających
        List<Employee> topEarners = employeeRepository.findTop10HighestPaidEmployees(
                PageRequest.of(0, 10, Sort.by("salary").descending())
        );
        advanced.put("topEarners", topEarners);

        // Pracownicy poniżej średniej
        List<Employee> belowAvg = employeeRepository.findEmployeesBelowAverageSalary();
        advanced.put("belowAverageCount", belowAvg.size());

        // Statystyki per departament
        advanced.put("departmentStats", getDepartmentStatistics());

        return advanced;
    }

    private Map<String, Object> getDepartmentStatistics() {
        List<Department> departments = departmentRepository.findAll();

        return departments.stream()
                .collect(Collectors.toMap(
                        Department::getName,
                        dept -> {
                            Map<String, Object> deptStats = new HashMap<>();
                            deptStats.put("employeeCount",
                                    getSafeLong(employeeRepository.countEmployeesByDepartment(dept.getId())));
                            deptStats.put("avgSalary",
                                    calculateDeptAvgSalary(dept.getId()));
                            return deptStats;
                        }
                ));
    }

    private Double calculateDeptAvgSalary(Long deptId) {
        // Oblicz średnią pensję dla departamentu
        List<Employee> deptEmployees = employeeRepository.findByDepartmentId(deptId);

        if (deptEmployees.isEmpty()) {
            return 0.0;
        }

        return deptEmployees.stream()
                .mapToDouble(Employee::getSalary)
                .average()
                .orElse(0.0);
    }

    // ===== METODY POMOCNICZE DLA NULL SAFETY =====

    private Double getSafeDouble(Double value) {
        return value != null ? value : 0.0;
    }

    private Long getSafeLong(Long value) {
        return value != null ? value : 0L;
    }
}