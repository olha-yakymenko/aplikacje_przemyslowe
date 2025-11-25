package com.techcorp.employee.service;

import com.techcorp.employee.model.Employee;
import com.techcorp.employee.model.CompanyStatistics;
import com.techcorp.employee.model.Position;
import com.techcorp.employee.model.EmploymentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportGeneratorServiceTest {

    @Mock
    private EmployeeService employeeService;

    @Mock
    private FileStorageService fileStorageService;

    @InjectMocks
    private ReportGeneratorService reportGeneratorService;

    @TempDir
    Path tempDir;

    private Path reportsDir;

    @BeforeEach
    void setUp() throws Exception {
        reportsDir = tempDir.resolve("reports");
        Files.createDirectories(reportsDir);

        // Ustawienie locale na angielskie dla poprawnych testów z kropką jako separatorem
        Locale.setDefault(Locale.ENGLISH);
    }

    @Test
    void generateCsvReport_WithCompanyFilter_ShouldGenerateCompanyReport() throws Exception {
        // Given
        String company = "TechCorp";
        List<Employee> employees = Arrays.asList(
                createEmployee("John Doe", "john@techcorp.com", "TechCorp", Position.PROGRAMMER, 8000.0),
                createEmployee("Jane Smith", "jane@techcorp.com", "TechCorp", Position.MANAGER, 12000.0)
        );

        when(employeeService.getEmployeesByCompany(company)).thenReturn(employees);
        when(fileStorageService.getReportsStorageLocation()).thenReturn(reportsDir);

        // When
        Path csvPath = reportGeneratorService.generateCsvReport(company);

        // Then
        assertNotNull(csvPath);
        assertTrue(Files.exists(csvPath));

        // Sprawdzamy poprawną nazwę pliku
        String fileName = csvPath.getFileName().toString();
        assertEquals("employees_TechCorp.csv", fileName);

        String content = Files.readString(csvPath);
        assertTrue(content.contains("John Doe"));
        assertTrue(content.contains("Jane Smith"));
        assertTrue(content.contains("PROGRAMMER"));
        assertTrue(content.contains("MANAGER"));
        assertTrue(content.contains("Name,Email,Company,Position,Salary,Status"));
    }

    @Test
    void generateCsvReport_WithoutCompany_ShouldGenerateAllEmployeesReport() throws Exception {
        // Given
        List<Employee> employees = Arrays.asList(
                createEmployee("John Doe", "john@techcorp.com", "TechCorp", Position.PROGRAMMER, 8000.0),
                createEmployee("Bob Wilson", "bob@other.com", "OtherCorp", Position.INTERN, 3000.0)
        );

        when(employeeService.getAllEmployees()).thenReturn(employees);
        when(fileStorageService.getReportsStorageLocation()).thenReturn(reportsDir);

        // When
        Path csvPath = reportGeneratorService.generateCsvReport(null);

        // Then
        assertNotNull(csvPath);
        assertTrue(Files.exists(csvPath));

        // Poprawna nazwa pliku
        String fileName = csvPath.getFileName().toString();
        assertEquals("employees_all.csv", fileName);

        String content = Files.readString(csvPath);
        assertTrue(content.contains("TechCorp"));
        assertTrue(content.contains("OtherCorp"));
        assertTrue(content.contains("8000.00"));
        assertTrue(content.contains("3000.00"));
    }

    @Test
    void generateCsvReport_WithEmptyCompany_ShouldGenerateAllEmployeesReport() throws Exception {
        // Given
        List<Employee> employees = Arrays.asList(
                createEmployee("John Doe", "john@techcorp.com", "TechCorp", Position.PROGRAMMER, 8000.0)
        );

        when(employeeService.getAllEmployees()).thenReturn(employees);
        when(fileStorageService.getReportsStorageLocation()).thenReturn(reportsDir);

        // When
        Path csvPath = reportGeneratorService.generateCsvReport("");

        // Then
        assertNotNull(csvPath);
        assertTrue(Files.exists(csvPath));
        String fileName = csvPath.getFileName().toString();
        assertEquals("employees_all.csv", fileName);
    }

    @Test
    void generateCsvReport_WithWhitespaceCompany_ShouldGenerateAllEmployeesReport() throws Exception {
        // Given
        List<Employee> employees = Arrays.asList(
                createEmployee("John Doe", "john@techcorp.com", "TechCorp", Position.VICE_PRESIDENT, 18000.0)
        );

        when(employeeService.getAllEmployees()).thenReturn(employees);
        when(fileStorageService.getReportsStorageLocation()).thenReturn(reportsDir);

        // When
        Path csvPath = reportGeneratorService.generateCsvReport("   ");

        // Then
        assertNotNull(csvPath);
        assertTrue(Files.exists(csvPath));
        String fileName = csvPath.getFileName().toString();
        assertEquals("employees_all.csv", fileName);
    }

    @Test
    void generateCsvReport_WithSpecialCharactersInCompanyName_ShouldSanitizeFileName() throws Exception {
        // Given
        String company = "Tech Corp & Partners";
        List<Employee> employees = Arrays.asList(
                createEmployee("John Doe", "john@techcorp.com", company, Position.MANAGER, 12000.0)
        );

        when(employeeService.getEmployeesByCompany(company)).thenReturn(employees);
        when(fileStorageService.getReportsStorageLocation()).thenReturn(reportsDir);

        // When
        Path csvPath = reportGeneratorService.generateCsvReport(company);

        // Then
        assertNotNull(csvPath);
        assertTrue(Files.exists(csvPath));
        String fileName = csvPath.getFileName().toString();
        assertEquals("employees_Tech_Corp_&_Partners.csv", fileName);
    }

    @Test
    void generateCsvReport_WithNoEmployees_ShouldGenerateEmptyReport() throws Exception {
        // Given
        String company = "EmptyCorp";
        List<Employee> employees = Arrays.asList();

        when(employeeService.getEmployeesByCompany(company)).thenReturn(employees);
        when(fileStorageService.getReportsStorageLocation()).thenReturn(reportsDir);

        // When
        Path csvPath = reportGeneratorService.generateCsvReport(company);

        // Then
        assertNotNull(csvPath);
        assertTrue(Files.exists(csvPath));
        String content = Files.readString(csvPath);
        assertTrue(content.contains("Name,Email,Company,Position,Salary,Status"));
        // Only header line should be present
        assertEquals(1, content.lines().count());
    }

//    @Test
//    void generateStatisticsPdf_ShouldGenerateStatisticsReport() throws Exception {
//        // Given
//        String companyName = "TechCorp";
//        CompanyStatistics stats = new CompanyStatistics(5, 65000.0, "John Doe");
//
//        when(employeeService.getCompanyStatistics(companyName)).thenReturn(stats);
//        when(fileStorageService.getReportsStorageLocation()).thenReturn(reportsDir);
//
//        // When
//        Path pdfPath = reportGeneratorService.generateStatisticsPdf(companyName);
//
//        // Then
//        assertNotNull(pdfPath);
//        assertTrue(Files.exists(pdfPath));
//        String fileName = pdfPath.getFileName().toString();
//        assertEquals("statistics_TechCorp.pdf", fileName);
//
//        String content = Files.readString(pdfPath);
//        assertTrue(content.contains("Company Statistics Report"));
//        assertTrue(content.contains("TechCorp"));
//        assertTrue(content.contains("5"));
//        assertTrue(content.contains("65000.00"));
//        assertTrue(content.contains("John Doe"));
//        assertTrue(content.contains("Generated on:"));
//    }

    @Test
    void generateStatisticsPdf_WithSpecialCharactersInCompanyName_ShouldSanitizeFileName() throws Exception {
        // Given
        String companyName = "Tech Corp & Partners";
        // 👇 POPRAWIONY KONSTRUKTOR - dodaj wszystkie wymagane parametry
        CompanyStatistics stats = new CompanyStatistics(companyName, 3, 50000.0, 75000.0);
        stats.setHighestPaidEmployee("Jane Smith"); // 👈 DODAJ SETTER

        when(employeeService.getCompanyStatistics(companyName)).thenReturn(stats);
        when(fileStorageService.getReportsStorageLocation()).thenReturn(reportsDir);

        // When
        Path pdfPath = reportGeneratorService.generateStatisticsPdf(companyName);

        // Then
        assertNotNull(pdfPath);
        assertTrue(Files.exists(pdfPath));
        String fileName = pdfPath.getFileName().toString();
        assertEquals("statistics_Tech_Corp_&_Partners.pdf", fileName);
    }

    @Test
    void generateCsvReport_ShouldHandleAllPositionTypes() throws Exception {
        // Given
        String company = "MixedCorp";
        List<Employee> employees = Arrays.asList(
                createEmployee("President CEO", "ceo@company.com", company, Position.PRESIDENT, 25000.0),
                createEmployee("Vice President", "vp@company.com", company, Position.VICE_PRESIDENT, 18000.0),
                createEmployee("Team Manager", "manager@company.com", company, Position.MANAGER, 12000.0),
                createEmployee("Senior Programmer", "dev@company.com", company, Position.PROGRAMMER, 10000.0),
                createEmployee("Summer Intern", "intern@company.com", company, Position.INTERN, 3000.0)
        );

        when(employeeService.getEmployeesByCompany(company)).thenReturn(employees);
        when(fileStorageService.getReportsStorageLocation()).thenReturn(reportsDir);

        // When
        Path csvPath = reportGeneratorService.generateCsvReport(company);

        // Then
        assertNotNull(csvPath);
        assertTrue(Files.exists(csvPath));
        String content = Files.readString(csvPath);

        // Verify all positions are included (z underscore zgodnie z enum)
        assertTrue(content.contains("PRESIDENT"));
        assertTrue(content.contains("VICE_PRESIDENT"));
        assertTrue(content.contains("MANAGER"));
        assertTrue(content.contains("PROGRAMMER"));
        assertTrue(content.contains("INTERN"));

        // Verify salaries with dot as decimal separator
        assertTrue(content.contains("25000.00"));
        assertTrue(content.contains("18000.00"));
        assertTrue(content.contains("12000.00"));
        assertTrue(content.contains("10000.00"));
        assertTrue(content.contains("3000.00"));
    }

    @Test
    void convertToCsv_ShouldFormatEmployeeDataCorrectly() {
        // Given
        ReportGeneratorService service = new ReportGeneratorService();
        Employee employee = createEmployee("John The Boss Doe", "john.doe@company.com", "Test Corp", Position.PRESIDENT, 25000.0);

        // When
        String csvLine = service.convertToCsv(employee);

        // Then - użyj kropki jako separatora dziesiętnego
        String expectedLine = "\"John The Boss Doe\",john.doe@company.com,\"Test Corp\",PRESIDENT,25000.00,ACTIVE";
        assertEquals(expectedLine, csvLine);
    }

    @Test
    void generateCsvReport_ShouldHandleIOException() {
        // Given
        String company = "TechCorp";
        List<Employee> employees = Arrays.asList(
                createEmployee("John Doe", "john@techcorp.com", "TechCorp", Position.PROGRAMMER, 8000.0)
        );

        when(employeeService.getEmployeesByCompany(company)).thenReturn(employees);
        when(fileStorageService.getReportsStorageLocation()).thenReturn(Path.of("/invalid/path/"));

        // When & Then
        assertThrows(Exception.class, () -> {
            reportGeneratorService.generateCsvReport(company);
        });
    }

    private Employee createEmployee(String name, String email, String company, Position position, double salary) {
        try {
            Employee employee = new Employee(name, email, company, position, salary, EmploymentStatus.ACTIVE);
            employee.setPhotoFileName("photo.jpg");
            return employee;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create test employee", e);
        }
    }
}