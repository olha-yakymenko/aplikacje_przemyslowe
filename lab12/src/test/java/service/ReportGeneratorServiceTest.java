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

import java.math.BigDecimal;
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
                createEmployee("John Doe", "john@techcorp.com", "TechCorp", Position.PROGRAMMER, new BigDecimal(8000)),
                createEmployee("Jane Smith", "jane@techcorp.com", "TechCorp", Position.MANAGER, new BigDecimal(12000))
        );

        when(employeeService.getEmployeesByCompany(company)).thenReturn(employees);
        when(fileStorageService.getReportsStorageLocation()).thenReturn(reportsDir);

        // When
        Path csvPath = reportGeneratorService.generateCsvReport(company);

        // Then - zgrupowanie wszystkich asercji związanych z generowaniem raportu
        assertAll("CSV report generation with company filter",
                () -> {
                    assertNotNull(csvPath, "Generated CSV path should not be null");
                    assertTrue(Files.exists(csvPath), "CSV file should exist");
                },
                () -> {
                    String fileName = csvPath.getFileName().toString();
                    assertEquals("employees_TechCorp.csv", fileName, "File name should match pattern");
                },
                () -> {
                    String content = Files.readString(csvPath);
                    assertAll("CSV content verification",
                            () -> assertTrue(content.contains("John Doe"), "Should contain John Doe"),
                            () -> assertTrue(content.contains("Jane Smith"), "Should contain Jane Smith"),
                            () -> assertTrue(content.contains("PROGRAMMER"), "Should contain PROGRAMMER"),
                            () -> assertTrue(content.contains("MANAGER"), "Should contain MANAGER"),
                            () -> assertTrue(content.contains("Name,Email,Company,Position,Salary,Status"),
                                    "Should contain CSV header")
                    );
                }
        );
    }

    @Test
    void generateCsvReport_WithoutCompany_ShouldGenerateAllEmployeesReport() throws Exception {
        // Given
        List<Employee> employees = Arrays.asList(
                createEmployee("John Doe", "john@techcorp.com", "TechCorp", Position.PROGRAMMER, new BigDecimal(8000)),
                createEmployee("Bob Wilson", "bob@other.com", "OtherCorp", Position.INTERN, new BigDecimal(3000))
        );

        when(employeeService.getAllEmployees()).thenReturn(employees);
        when(fileStorageService.getReportsStorageLocation()).thenReturn(reportsDir);

        // When
        Path csvPath = reportGeneratorService.generateCsvReport(null);

        // Then - asercje dla raportu wszystkich pracowników
        assertAll("All employees CSV report generation",
                () -> {
                    assertNotNull(csvPath, "Generated CSV path should not be null");
                    assertTrue(Files.exists(csvPath), "CSV file should exist");
                },
                () -> {
                    String fileName = csvPath.getFileName().toString();
                    assertEquals("employees_all.csv", fileName, "File name should be 'employees_all.csv'");
                },
                () -> {
                    String content = Files.readString(csvPath);
                    assertAll("CSV content verification for all employees",
                            () -> assertTrue(content.contains("TechCorp"), "Should contain TechCorp"),
                            () -> assertTrue(content.contains("OtherCorp"), "Should contain OtherCorp"),
                            () -> assertTrue(content.contains("8000.00"), "Should contain salary 8000.00"),
                            () -> assertTrue(content.contains("3000.00"), "Should contain salary 3000.00")
                    );
                }
        );
    }

    @Test
    void generateCsvReport_WithEmptyCompany_ShouldGenerateAllEmployeesReport() throws Exception {
        // Given
        List<Employee> employees = Arrays.asList(
                createEmployee("John Doe", "john@techcorp.com", "TechCorp", Position.PROGRAMMER, new BigDecimal(8000))
        );

        when(employeeService.getAllEmployees()).thenReturn(employees);
        when(fileStorageService.getReportsStorageLocation()).thenReturn(reportsDir);

        // When
        Path csvPath = reportGeneratorService.generateCsvReport("");

        // Then - zgrupowanie asercji dla pustej nazwy firmy
        assertAll("CSV report with empty company name",
                () -> {
                    assertNotNull(csvPath, "Generated CSV path should not be null");
                    assertTrue(Files.exists(csvPath), "CSV file should exist");
                },
                () -> {
                    String fileName = csvPath.getFileName().toString();
                    assertEquals("employees_all.csv", fileName, "Should generate 'all' report for empty company");
                }
        );
    }

    @Test
    void generateCsvReport_WithWhitespaceCompany_ShouldGenerateAllEmployeesReport() throws Exception {
        // Given
        List<Employee> employees = Arrays.asList(
                createEmployee("John Doe", "john@techcorp.com", "TechCorp", Position.VICE_PRESIDENT, new BigDecimal(18000))
        );

        when(employeeService.getAllEmployees()).thenReturn(employees);
        when(fileStorageService.getReportsStorageLocation()).thenReturn(reportsDir);

        // When
        Path csvPath = reportGeneratorService.generateCsvReport("   ");

        // Then - asercje dla whitespace w nazwie firmy
        assertAll("CSV report with whitespace company name",
                () -> {
                    assertNotNull(csvPath, "Generated CSV path should not be null");
                    assertTrue(Files.exists(csvPath), "CSV file should exist");
                },
                () -> {
                    String fileName = csvPath.getFileName().toString();
                    assertEquals("employees_all.csv", fileName, "Should generate 'all' report for whitespace company");
                }
        );
    }

    @Test
    void generateCsvReport_WithSpecialCharactersInCompanyName_ShouldSanitizeFileName() throws Exception {
        // Given
        String company = "Tech Corp & Partners";
        List<Employee> employees = Arrays.asList(
                createEmployee("John Doe", "john@techcorp.com", company, Position.MANAGER, new BigDecimal(12000))
        );

        when(employeeService.getEmployeesByCompany(company)).thenReturn(employees);
        when(fileStorageService.getReportsStorageLocation()).thenReturn(reportsDir);

        // When
        Path csvPath = reportGeneratorService.generateCsvReport(company);

        // Then - asercje dla specjalnych znaków w nazwie firmy
        assertAll("CSV report with special characters in company name",
                () -> {
                    assertNotNull(csvPath, "Generated CSV path should not be null");
                    assertTrue(Files.exists(csvPath), "CSV file should exist");
                },
                () -> {
                    String fileName = csvPath.getFileName().toString();
                    assertEquals("employees_Tech_Corp_&_Partners.csv", fileName,
                            "Should sanitize special characters in file name");
                }
        );
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

        // Then - asercje dla pustego raportu
        assertAll("Empty CSV report generation",
                () -> {
                    assertNotNull(csvPath, "Generated CSV path should not be null");
                    assertTrue(Files.exists(csvPath), "CSV file should exist");
                },
                () -> {
                    String content = Files.readString(csvPath);
                    assertAll("Empty CSV content verification",
                            () -> assertTrue(content.contains("Name,Email,Company,Position,Salary,Status"),
                                    "Should contain CSV header"),
                            () -> assertEquals(1, content.lines().count(),
                                    "Should have only header line for empty report")
                    );
                }
        );
    }

    @Test
    void generateStatisticsPdf_WithSpecialCharactersInCompanyName_ShouldSanitizeFileName() throws Exception {
        // Given
        String companyName = "Tech Corp & Partners";
        // 👇 POPRAWIONY KONSTRUKTOR - dodaj wszystkie wymagane parametry
        CompanyStatistics stats = new CompanyStatistics(companyName, 3, BigDecimal.valueOf(50000.0), BigDecimal.valueOf(75000.0));
        stats.setHighestPaidEmployee("Jane Smith"); // 👈 DODAJ SETTER

        when(employeeService.getCompanyStatistics(companyName)).thenReturn(stats);
        when(fileStorageService.getReportsStorageLocation()).thenReturn(reportsDir);

        // When
        Path pdfPath = reportGeneratorService.generateStatisticsPdf(companyName);

        // Then - asercje dla PDF ze specjalnymi znakami
        assertAll("PDF report with special characters in company name",
                () -> {
                    assertNotNull(pdfPath, "Generated PDF path should not be null");
                    assertTrue(Files.exists(pdfPath), "PDF file should exist");
                },
                () -> {
                    String fileName = pdfPath.getFileName().toString();
                    assertEquals("statistics_Tech_Corp_&_Partners.pdf", fileName,
                            "Should sanitize special characters in PDF file name");
                }
        );
    }

    @Test
    void generateCsvReport_ShouldHandleAllPositionTypes() throws Exception {
        // Given
        String company = "MixedCorp";
        List<Employee> employees = Arrays.asList(
                createEmployee("President CEO", "ceo@company.com", company, Position.PRESIDENT, new BigDecimal(25000)),
                createEmployee("Vice President", "vp@company.com", company, Position.VICE_PRESIDENT, new BigDecimal(18000)),
                createEmployee("Team Manager", "manager@company.com", company, Position.MANAGER, new BigDecimal(12000)),
                createEmployee("Senior Programmer", "dev@company.com", company, Position.PROGRAMMER, new BigDecimal(10000)),
                createEmployee("Summer Intern", "intern@company.com", company, Position.INTERN, new BigDecimal(3000))
        );

        when(employeeService.getEmployeesByCompany(company)).thenReturn(employees);
        when(fileStorageService.getReportsStorageLocation()).thenReturn(reportsDir);

        // When
        Path csvPath = reportGeneratorService.generateCsvReport(company);

        // Then - asercje dla wszystkich typów pozycji
        assertAll("CSV report with all position types",
                () -> {
                    assertNotNull(csvPath, "Generated CSV path should not be null");
                    assertTrue(Files.exists(csvPath), "CSV file should exist");
                },
                () -> {
                    String content = Files.readString(csvPath);
                    assertAll("Position types verification in CSV",
                            () -> assertTrue(content.contains("PRESIDENT"), "Should contain PRESIDENT"),
                            () -> assertTrue(content.contains("VICE_PRESIDENT"), "Should contain VICE_PRESIDENT"),
                            () -> assertTrue(content.contains("MANAGER"), "Should contain MANAGER"),
                            () -> assertTrue(content.contains("PROGRAMMER"), "Should contain PROGRAMMER"),
                            () -> assertTrue(content.contains("INTERN"), "Should contain INTERN")
                    );
                },
                () -> {
                    String content = Files.readString(csvPath);
                    assertAll("Salary formatting verification",
                            () -> assertTrue(content.contains("25000.00"), "Should contain 25000.00"),
                            () -> assertTrue(content.contains("18000.00"), "Should contain 18000.00"),
                            () -> assertTrue(content.contains("12000.00"), "Should contain 12000.00"),
                            () -> assertTrue(content.contains("10000.00"), "Should contain 10000.00"),
                            () -> assertTrue(content.contains("3000.00"), "Should contain 3000.00")
                    );
                }
        );
    }

    @Test
    void convertToCsv_ShouldFormatEmployeeDataCorrectly() {
        // Given
        ReportGeneratorService service = new ReportGeneratorService();
        Employee employee = createEmployee("John The Boss Doe", "john.doe@company.com", "Test Corp", Position.PRESIDENT, new BigDecimal(25000));

        // When
        String csvLine = service.convertToCsv(employee);

        // Then - użyj kropki jako separatora dziesiętnego
        assertAll("CSV line formatting",
                () -> {
                    String expectedLine = "\"John The Boss Doe\",john.doe@company.com,\"Test Corp\",PRESIDENT,25000.00,ACTIVE";
                    assertEquals(expectedLine, csvLine, "CSV line should be properly formatted");
                }
        );
    }

    @Test
    void generateCsvReport_ShouldHandleIOException() {
        // Given
        String company = "TechCorp";
        List<Employee> employees = Arrays.asList(
                createEmployee("John Doe", "john@techcorp.com", "TechCorp", Position.PROGRAMMER, new BigDecimal(8000))
        );

        when(employeeService.getEmployeesByCompany(company)).thenReturn(employees);
        when(fileStorageService.getReportsStorageLocation()).thenReturn(Path.of("/invalid/path/"));

        // When & Then
        assertThrows(Exception.class, () -> {
            reportGeneratorService.generateCsvReport(company);
        }, "Should throw exception for invalid path");
    }

    private Employee createEmployee(String name, String email, String company, Position position, BigDecimal salary) {
        try {
            Employee employee = new Employee(name, email, company, position, salary, EmploymentStatus.ACTIVE);
            employee.setPhotoFileName("photo.jpg");
            return employee;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create test employee", e);
        }
    }
}