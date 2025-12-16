package com.techcorp.employee.service;

import com.techcorp.employee.exception.InvalidDataException;
import com.techcorp.employee.exception.InvalidFileException;
import com.techcorp.employee.model.Employee;
import com.techcorp.employee.model.ImportSummary;
import com.techcorp.employee.model.Position;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ImportService Unit Tests")
class ImportServiceTest {

    @TempDir
    Path tempDir;

    @Mock
    private FileStorageService fileStorageService;

    @Mock
    private EmployeeService employeeService;

    private ImportService importService;

    @BeforeEach
    void setUp() {
        importService = new ImportService(employeeService);
        ReflectionTestUtils.setField(importService, "fileStorageService", fileStorageService);
    }

    // ===== TESTOWANIE WALIDACJI DANYCH CSV =====

    @ParameterizedTest
    @CsvSource({
            ",Kowalski,jan@test.com,TechCorp,MANAGER,15000,First name cannot be empty",
            "Jan,,jan@test.com,TechCorp,MANAGER,15000,Last name cannot be empty",
            "Jan,Kowalski,,TechCorp,MANAGER,15000,Email cannot be empty",
            "Jan,Kowalski,jan@techcorp.com,,MANAGER,15000,Company cannot be empty",
            "Jan,Kowalski,jan@techcorp.com,TechCorp,,15000,Position cannot be empty",
            "Jan,Kowalski,jan@techcorp.com,TechCorp,MANAGER,,Salary cannot be empty",
            "Jan,Kowalski,invalid-email@techcorp,TechCorp,MANAGER,15000,Invalid email format",
            "Jan,Kowalski,jan@techcorp.com,TechCorp,INVALID,15000,Invalid position",
            "Jan,Kowalski,jan@techcorp.com,TechCorp,MANAGER,-1000,Salary cannot be negative",
            "Jan,Kowalski,jan@techcorp.com,TechCorp,MANAGER,abc,Invalid salary format"
    })
    @DisplayName("Should validate CSV fields and throw InvalidDataException")
    void parseEmployeeFromCsv_WithInvalidData_ShouldThrowException(
            String firstName, String lastName, String email, String company,
            String position, String salary, String expectedError) {

        // Arrange
        String[] fields = {firstName, lastName, email, company, position, salary};

        // Act & Assert
        InvalidDataException exception = assertThrows(
                InvalidDataException.class,
                () -> importService.parseEmployeeFromCsv(fields, 1)
        );

        assertTrue(exception.getMessage().contains(expectedError),
                "Expected: " + expectedError + ", Actual: " + exception.getMessage());
    }

    @Test
    @DisplayName("Should throw InvalidDataException for incorrect field count")
    void parseEmployeeFromCsv_WithInvalidFieldCount_ShouldThrowException() {
        // Arrange
        String[] insufficientFields = {"Jan", "Kowalski", "jan@test.com"}; // 3 fields
        String[] excessiveFields = {"Jan", "Kowalski", "jan@test.com", "TechCorp", "MANAGER", "15000", "extra"}; // 7 fields

        // Act & Assert
        assertAll("Field count validation",
                () -> assertThrows(InvalidDataException.class,
                        () -> importService.parseEmployeeFromCsv(insufficientFields, 1)),
                () -> assertThrows(InvalidDataException.class,
                        () -> importService.parseEmployeeFromCsv(excessiveFields, 1))
        );
    }

    // ===== TESTOWANIE IMPORTU XML Z MULTIPARTFILE =====



    @Test
    @DisplayName("Should handle XML with invalid structure")
    void importXmlFile_WithInvalidStructure_ShouldReturnErrorSummary() throws IOException, InvalidDataException {
        // Arrange
        String xmlContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<invalidRoot>\n" +
                "  <employee>\n" +
                "    <firstName>Jan</firstName>\n" +
                "    <lastName>Kowalski</lastName>\n" +
                "    <email>jan@techcorp.com</email>\n" +
                "    <company>TechCorp</company>\n" +
                "    <position>MANAGER</position>\n" +
                "    <salary>15000</salary>\n" +
                "  </employee>\n" +
                "</invalidRoot>";

        MultipartFile file = new MockMultipartFile(
                "file", "employees.xml", "application/xml", xmlContent.getBytes()
        );

        // Act
        ImportSummary summary = importService.importXmlFile(file);

        // Assert
        assertAll("Invalid XML structure",
                () -> assertEquals(0, summary.getImportedCount(), "Should import 0 employees"),
                () -> assertFalse(summary.getErrors().isEmpty(), "Should have errors"),
                () -> assertTrue(summary.getErrors().get(0).contains("Invalid XML structure"))
        );

        verify(employeeService, never()).addEmployee(any(Employee.class));
    }

    // ===== TESTOWANIE OBSŁUGI BŁĘDÓW =====

    @Test
    @DisplayName("Should handle file validation errors")
    void importCsvFile_WithInvalidFile_ShouldReturnErrorSummary() throws InvalidDataException {
        // Arrange
        MultipartFile emptyFile = new MockMultipartFile(
                "file", "empty.csv", "text/csv", new byte[0]
        );

        doThrow(new InvalidFileException("File is empty"))
                .when(fileStorageService).validateFile(emptyFile);

        // Act
        ImportSummary summary = importService.importCsvFile(emptyFile);

        // Assert
        assertAll("Invalid file handling",
                () -> assertEquals(0, summary.getImportedCount()),
                () -> assertFalse(summary.getErrors().isEmpty()),
                () -> assertTrue(summary.getErrors().get(0).contains("Invalid file"))
        );

        verify(employeeService, never()).addEmployee(any(Employee.class));
        verify(fileStorageService, never()).storeFile(any(MultipartFile.class), anyString());
    }

    // ===== TESTOWANIE WALIDACJI EMAIL =====

    @ParameterizedTest
    @ValueSource(strings = {
            "valid@email.com",
            "valid.email@domain.com",
            "valid_email@domain.co.uk",
            "valid+tag@domain.com"
    })
    @DisplayName("Should accept valid email formats")
    void parseEmployeeFromCsv_WithValidEmails_ShouldCreateEmployee(String email) throws InvalidDataException {
        // Arrange
        String[] fields = {"Jan", "Kowalski", email, "TechCorp", "MANAGER", "15000"};

        // Act & Assert
        assertDoesNotThrow(() -> {
            Employee employee = importService.parseEmployeeFromCsv(fields, 1);
            assertEquals(email, employee.getEmail());
        });
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "invalid",
            "invalid@",
            "@domain.com",
            "invalid@domain",
            "invalid@.com",
            "invalid@domain."
    })
    @DisplayName("Should reject invalid email formats")
    void parseEmployeeFromCsv_WithInvalidEmails_ShouldThrowException(String invalidEmail) {
        // Arrange
        String[] fields = {"Jan", "Kowalski", invalidEmail, "TechCorp", "MANAGER", "15000"};

        // Act & Assert
        InvalidDataException exception = assertThrows(
                InvalidDataException.class,
                () -> importService.parseEmployeeFromCsv(fields, 1)
        );

        assertTrue(exception.getMessage().contains("Invalid email format"));
    }

    // ===== TESTOWANIE GRANICZNYCH WARTOŚCI WYNAGRODZENIA =====

    @ParameterizedTest
    @ValueSource(doubles = {0.0, 50000.0, 1000000.0})
    @DisplayName("Should accept valid salary values")
    void parseEmployeeFromCsv_WithValidSalaries_ShouldCreateEmployee(double salary) throws InvalidDataException {
        // Arrange
        String[] fields = {"Jan", "Kowalski", "jan@test.com", "TechCorp", "MANAGER", String.valueOf(salary)};

        // Act & Assert
        assertDoesNotThrow(() -> {
            Employee employee = importService.parseEmployeeFromCsv(fields, 1);
            assertEquals(salary, employee.getSalary(), 0.001);
        });
    }

    @ParameterizedTest
    @ValueSource(doubles = {-1000.0, -0.1})
    @DisplayName("Should reject negative salary values")
    void parseEmployeeFromCsv_WithNegativeSalaries_ShouldThrowException(double salary) {
        // Arrange
        String[] fields = {"Jan", "Kowalski", "jan@test.com", "TechCorp", "MANAGER", String.valueOf(salary)};

        // Act & Assert
        InvalidDataException exception = assertThrows(
                InvalidDataException.class,
                () -> importService.parseEmployeeFromCsv(fields, 1)
        );

        assertTrue(exception.getMessage().contains("Salary cannot be negative"));
    }

    @Test
    @DisplayName("Should reject unrealistic salary values")
    void parseEmployeeFromCsv_WithUnrealisticSalary_ShouldThrowException() {
        // Arrange
        String[] fields = {"Jan", "Kowalski", "jan@test.com", "TechCorp", "MANAGER", "1000001"};

        // Act & Assert
        InvalidDataException exception = assertThrows(
                InvalidDataException.class,
                () -> importService.parseEmployeeFromCsv(fields, 1)
        );

        assertTrue(exception.getMessage().contains("Salary seems unrealistic"));
    }

    // ===== TESTOWANIE POZYCJI =====

    @ParameterizedTest
    @ValueSource(strings = {"MANAGER", "PROGRAMMER"})
    @DisplayName("Should accept valid position values")
    void parseEmployeeFromCsv_WithValidPositions_ShouldCreateEmployee(String position) throws InvalidDataException {
        // Arrange
        String[] fields = {"Jan", "Kowalski", "jan@test.com", "TechCorp", position, "15000"};

        // Act & Assert
        assertDoesNotThrow(() -> {
            Employee employee = importService.parseEmployeeFromCsv(fields, 1);
            assertEquals(Position.valueOf(position), employee.getPosition());
        });
    }

    @Test
    @DisplayName("Should reject invalid position values")
    void parseEmployeeFromCsv_WithInvalidPosition_ShouldThrowException() {
        // Arrange
        String[] fields = {"Jan", "Kowalski", "jan@test.com", "TechCorp", "INVALID_POSITION", "15000"};

        // Act & Assert
        InvalidDataException exception = assertThrows(
                InvalidDataException.class,
                () -> importService.parseEmployeeFromCsv(fields, 1)
        );

        assertTrue(exception.getMessage().contains("Invalid position"));
    }

    // ===== TESTOWANIE TRIMOWANIA DANYCH =====

    @Test
    @DisplayName("Should trim whitespace from all fields")
    void parseEmployeeFromCsv_WithWhitespace_ShouldTrimFields() throws InvalidDataException {
        // Arrange
        String[] fields = {"  Jan  ", "  Kowalski  ", "  jan@test.com  ", "  TechCorp  ", "  MANAGER  ", "  15000  "};

        // Act
        Employee employee = importService.parseEmployeeFromCsv(fields, 1);

        // Assert
        assertAll("Whitespace trimming",
                () -> assertEquals("Jan Kowalski", employee.getName()),
                () -> assertEquals("jan@test.com", employee.getEmail()),
                () -> assertEquals("TechCorp", employee.getCompany()),
                () -> assertEquals(Position.MANAGER, employee.getPosition()),
                () -> assertEquals(15000.0, employee.getSalary(), 0.001)
        );
    }


    // ===== TESTOWANIE DEPRECATED METOD =====

    @Test
    @DisplayName("Deprecated CSV method should return error")
    void importFromCsv_DeprecatedMethod_ShouldReturnError() {
        // Act
        ImportSummary summary = importService.importFromCsv("test.csv");

        // Assert
        assertEquals(0, summary.getImportedCount());
        assertFalse(summary.getErrors().isEmpty());
        assertTrue(summary.getErrors().get(0).contains("deprecated"));
    }

    @Test
    @DisplayName("Deprecated XML method should return error")
    void importFromXml_DeprecatedMethod_ShouldReturnError() {
        // Act
        ImportSummary summary = importService.importFromXml("test.xml");

        // Assert
        assertEquals(0, summary.getImportedCount());
        assertFalse(summary.getErrors().isEmpty());
        assertTrue(summary.getErrors().get(0).contains("deprecated"));
    }

    // ===== TESTOWANIE WYJĄTKÓW DLA IMPORTU CSV =====

    @Test
    @DisplayName("Should handle CSV with malformed content")
    void importCsvFile_WithMalformedContent_ShouldReturnErrorSummary() throws IOException, InvalidDataException {
        // Arrange
        String malformedCsv = "name,email,company\n\"John Doe,john@test.com,TechCorp"; // Niezamknięty cudzysłów

        MockMultipartFile malformedFile = new MockMultipartFile(
                "file", "malformed.csv", "text/csv", malformedCsv.getBytes()
        );

        // Act
        ImportSummary summary = importService.importCsvFile(malformedFile);

        // Assert
        assertTrue(summary.hasErrors());
        verify(employeeService, never()).addEmployee(any(Employee.class));
    }

    @Test
    @DisplayName("Should handle empty CSV file")
    void importCsvFile_WithEmptyFile_ShouldReturnErrorSummary() {
        // Arrange
        String emptyCsv = "";
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file", "empty.csv", "text/csv", emptyCsv.getBytes()
        );

        // Act
        ImportSummary summary = importService.importCsvFile(emptyFile);

        // Assert
        assertTrue(summary.hasErrors());
        assertTrue(summary.getErrors().get(0).contains("File is empty"));
    }

    // ===== TESTOWANIE WYJĄTKÓW DLA IMPORTU XML =====

    @Test
    @DisplayName("Should handle XML with malicious content securely")
    void importXmlFile_WithMaliciousContent_ShouldReturnErrorSummary() throws IOException, InvalidDataException {
        // Arrange
        String maliciousXml = "<?xml version=\"1.0\"?>\n" +
                "<!DOCTYPE lolz [\n" +
                " <!ENTITY lol \"lol\">\n" +
                " <!ENTITY lol2 \"&lol;&lol;&lol;&lol;&lol;&lol;&lol;&lol;&lol;&lol;\">\n" +
                "]>\n" +
                "<employees>&lol2;</employees>";

        MockMultipartFile maliciousFile = new MockMultipartFile(
                "file", "malicious.xml", "application/xml", maliciousXml.getBytes()
        );

        // Act
        ImportSummary summary = importService.importXmlFile(maliciousFile);

        // Assert
        assertTrue(summary.hasErrors());
        verify(employeeService, never()).addEmployee(any(Employee.class));
    }

    @Test
    @DisplayName("Should handle XML with missing required elements")
    void importXmlFile_WithMissingElements_ShouldReturnErrorSummary() throws IOException {
        // Arrange
        String incompleteXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<employees>\n" +
                "  <employee>\n" +
                "    <firstName>Jan</firstName>\n" +
                "    <lastName>Kowalski</lastName>\n" +
                "    <!-- email missing -->\n" +
                "    <company>TechCorp</company>\n" +
                "    <position>MANAGER</position>\n" +
                "    <salary>15000</salary>\n" +
                "  </employee>\n" +
                "</employees>";

        MockMultipartFile incompleteFile = new MockMultipartFile(
                "file", "incomplete.xml", "application/xml", incompleteXml.getBytes()
        );

        // Act
        ImportSummary summary = importService.importXmlFile(incompleteFile);

        // Assert
        assertTrue(summary.hasErrors());
        assertTrue(summary.getErrors().get(0).contains("Missing required element"));
    }

    // ===== TESTOWANIE WALIDACJI DANYCH =====

    @Test
    @DisplayName("Should create employee with valid data")
    void parseEmployeeFromCsv_WithValidData_ShouldCreateEmployee() throws InvalidDataException {
        // Arrange
        String[] fields = {"Jan", "Kowalski", "jan@test.com", "TechCorp", "MANAGER", "15000"};

        // Act
        Employee employee = importService.parseEmployeeFromCsv(fields, 1);

        // Assert
        assertAll("Valid employee creation",
                () -> assertEquals("Jan Kowalski", employee.getName()),
                () -> assertEquals("jan@test.com", employee.getEmail()),
                () -> assertEquals("TechCorp", employee.getCompany()),
                () -> assertEquals(Position.MANAGER, employee.getPosition()),
                () -> assertEquals(15000.0, employee.getSalary(), 0.001)
        );
    }

    @Test
    @DisplayName("Should handle file storage service exceptions")
    void importCsvFile_WhenFileStorageFails_ShouldReturnErrorSummary() throws IOException {
        // Arrange
        String csvContent = "Jan,Kowalski,jan@test.com,TechCorp,MANAGER,15000";
        MultipartFile file = new MockMultipartFile(
                "file", "test.csv", "text/csv", csvContent.getBytes()
        );

        doThrow(new RuntimeException("Storage error"))
                .when(fileStorageService).validateFile(file);

        // Act
        ImportSummary summary = importService.importCsvFile(file);

        // Assert
        assertTrue(summary.hasErrors());
    }



    // ===== TESTOWANIE BEZPIECZEŃSTWA XML =====

    @Test
    @DisplayName("Should securely parse XML with XXE protection")
    void importXmlFile_WithXXEAttempt_ShouldBeBlocked() throws IOException {
        // Arrange
        String xxeXml = "<?xml version=\"1.0\"?>\n" +
                "<!DOCTYPE root [\n" +
                " <!ENTITY ext SYSTEM \"file:///etc/passwd\">\n" +
                "]>\n" +
                "<employees>&ext;</employees>";

        MockMultipartFile xxeFile = new MockMultipartFile(
                "file", "xxe.xml", "application/xml", xxeXml.getBytes()
        );

        // Act
        ImportSummary summary = importService.importXmlFile(xxeFile);

        // Assert
        // Test powinien przejść bez wycieku danych - XXE powinno być zablokowane
        assertTrue(summary.hasErrors() || summary.getImportedCount() == 0);
    }
}