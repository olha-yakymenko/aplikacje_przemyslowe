package com.techcorp.employee.service;

import com.techcorp.employee.model.Employee;
import com.techcorp.employee.model.CompanyStatistics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReportGeneratorService {

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private FileStorageService fileStorageService;

    // W ReportGeneratorService popraw metodę generateCsvReport:
    public Path generateCsvReport(String company) throws IOException {
        List<Employee> employees;
        String fileName;

        if (company != null && !company.trim().isEmpty()) {
            employees = employeeService.getEmployeesByCompany(company);
            fileName = "employees_" + company.replace(" ", "_") + ".csv";
        } else {
            employees = employeeService.getAllEmployees();
            fileName = "employees_all.csv";
        }

        List<String> csvLines = employees.stream()
                .map(this::convertToCsv)
                .collect(Collectors.toList());

        // Add header
        csvLines.add(0, "Name,Email,Company,Position,Salary,Status");

        Path reportPath = fileStorageService.getReportsStorageLocation().resolve(fileName);
        Files.write(reportPath, csvLines);

        return reportPath;
    }

    public Path generateStatisticsPdf(String companyName) throws IOException {
        CompanyStatistics stats = employeeService.getCompanyStatistics(companyName);

        // Simple PDF generation - in real application use iText or PDFBox
        List<String> pdfContent = List.of(
                "Company Statistics Report",
                "Company: " + companyName,
                "Employee Count: " + stats.getEmployeeCount(),
                "Average Salary: " + String.format("%.2f", stats.getAverageSalary()),
                "Highest Paid Employee: " + stats.getHighestPaidEmployee(),
                "Generated on: " + java.time.LocalDateTime.now()
        );

        String fileName = "statistics_" + companyName.replace(" ", "_") + ".pdf";
        Path reportPath = fileStorageService.getReportsStorageLocation().resolve(fileName);
        Files.write(reportPath, pdfContent);

        return reportPath;
    }

    String convertToCsv(Employee employee) {
        return String.format("\"%s\",%s,\"%s\",%s,%.2f,%s",
                employee.getName(),
                employee.getEmail(),
                employee.getCompany(),
                employee.getPosition(),
                employee.getSalary(),
                employee.getStatus());
    }
}