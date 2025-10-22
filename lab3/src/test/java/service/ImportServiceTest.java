package service;

import src.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import src.exception.InvalidDataException;
import src.model.Employee;
import src.model.ImportSummary;
import src.model.Position;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ImportService Unit Tests")
class ImportServiceTest {

    @TempDir
    Path tempDir;
    private EmployeeService employeeService;
    private ImportService importService;

    @BeforeEach
    void setUp() {
        employeeService = new EmployeeService();
        importService = new ImportService(employeeService);
    }

    // ===== TESTOWANIE POPRAWNEGO IMPORTU =====

    @Test
    @DisplayName("Should import valid CSV file and return correct summary")
    void importFromCsv_WithValidData_ShouldImportSuccessfully() throws IOException {
        // Arrange
        File csvFile = createTestCsvFile("valid_data.csv",
                "firstName,lastName,email,company,position,salary",
                "Jan,Kowalski,jan.kowalski@test.com,TechCorp,MANAGER,15000.50",
                "Anna,Nowak,anna.nowak@test.com,TechCorp,PROGRAMMER,9000.75"
        );

        // Act
        ImportSummary summary = importService.importFromCsv(csvFile.getAbsolutePath());

        // Assert
        assertAll("Import summary validation",
                () -> assertEquals(2, summary.getImportedCount(),
                        "Should import exactly 2 employees"),
                () -> assertTrue(summary.getErrors().isEmpty(),
                        "Should have no errors"),
                () -> assertEquals(2, employeeService.getEmployeeCount(),
                        "Should add employees to service")
        );
    }


    // ===== TESTOWANIE OBSŁUGI BŁĘDÓW =====

    @Test
    @DisplayName("Should continue processing and collect errors for invalid records")
    void importFromCsv_WithMixedValidAndInvalidData_ShouldContinueProcessing() throws IOException {
        // Arrange
        File csvFile = createTestCsvFile("mixed_data.csv",
                "firstName,lastName,email,company,position,salary",
                "Jan,Kowalski,jan.kowalski@test.com,TechCorp,MANAGER,15000",  // valid
                "Anna,Nowak,,TechCorp,PROGRAMMER,9000",                       // empty email
                "Piotr,Wiśniewski,piotr@test.com,TechCorp,PROGRAMMER,8000"   // valid
        );

        // Act
        ImportSummary summary = importService.importFromCsv(csvFile.getAbsolutePath());

        // Assert
        assertAll("Mixed data import validation",
                () -> assertEquals(2, summary.getImportedCount(),
                        "Should import valid records"),
                () -> assertEquals(1, summary.getErrors().size(),
                        "Should collect errors for invalid records"),
                () -> assertTrue(summary.getErrors().get(0).toLowerCase().contains("email") ||
                                summary.getErrors().get(0).toLowerCase().contains("empty"),
                        "Error should mention email or empty field. Actual: " + summary.getErrors().get(0))
        );
    }



    @ParameterizedTest
    @CsvSource({
            ",Kowalski,jan@test.com,TechCorp,MANAGER,15000,First name cannot be empty",
            "Jan,,jan@test.com,TechCorp,MANAGER,15000,Last name cannot be empty",
            "Jan,Kowalski,,TechCorp,MANAGER,15000,Email cannot be empty",
            "Jan,Kowalski,jan@test.com,,MANAGER,15000,Company cannot be empty",
            "Jan,Kowalski,jan@test.com,TechCorp,INVALID_POSITION,15000,Invalid position",
            "Jan,Kowalski,jan@test.com,TechCorp,MANAGER,-1000,Salary cannot be negative",
            "Jan,Kowalski,invalid-email,TechCorp,MANAGER,15000,Invalid email format",
            "Jan,Kowalski,jan@test.com,TechCorp,MANAGER,abc,Invalid salary format",
            "Jan,Kowalski,jan@test.com,TechCorp,MANAGER,,Invalid salary format",
            "Jan,Kowalski,jan@test.com,TechCorp,MANAGER,123abc,Invalid salary format"
    })
    @DisplayName("Should throw InvalidDataException for various invalid inputs")
    void parseEmployeeFromCsv_WithInvalidData_ShouldThrowException(
            String firstName, String lastName, String email,
            String company, String position, String salary, String expectedError) {

        // Arrange
        String[] invalidFields = {firstName, lastName, email, company, position, salary};

        // Act & Assert
        InvalidDataException exception = assertThrows(
                InvalidDataException.class,
                () -> importService.parseEmployeeFromCsv(invalidFields, 1),
                "Should throw InvalidDataException for: " + expectedError
        );

        assertTrue(exception.getMessage().contains(expectedError) ||
                        exception.getMessage().toLowerCase().contains(expectedError.toLowerCase()),
                "Exception message should contain: " + expectedError + ". Actual: " + exception.getMessage());
    }




    @Test
    @DisplayName("Should throw InvalidDataException for invalid number of fields")
    void parseEmployeeFromCsv_WithInvalidFieldCount_ShouldThrowException() {
        // Arrange
        String[] insufficientFields = {"Jan", "Kowalski", "jan@test.com"};
        String[] excessiveFields = {"Jan", "Kowalski", "jan@test.com", "TechCorp", "MANAGER", "15000", "extra"};

        // Act & Assert - Test multiple invalid field counts
        assertAll("Field count validation",
                () -> {
                    InvalidDataException exception = assertThrows(InvalidDataException.class,
                            () -> importService.parseEmployeeFromCsv(insufficientFields, 1));
                    assertTrue(exception.getMessage().contains("Invalid number of fields"),
                            "Should mention invalid number of fields. Actual: " + exception.getMessage());
                },
                () -> {
                    InvalidDataException exception = assertThrows(InvalidDataException.class,
                            () -> importService.parseEmployeeFromCsv(excessiveFields, 1));
                    assertTrue(exception.getMessage().contains("Invalid number of fields"),
                            "Should mention invalid number of fields. Actual: " + exception.getMessage());
                }
        );
    }

    // ===== TESTOWANIE OBSŁUGI PLIKÓW =====

    @Test
    @DisplayName("Should handle non-existing file and return error summary")
    void importFromCsv_WithNonExistingFile_ShouldReturnErrorSummary() {
        // Arrange
        String nonExistingPath = tempDir.resolve("nonexisting_file.csv").toString();

        // Act
        ImportSummary summary = importService.importFromCsv(nonExistingPath);

        // Assert
        assertAll("Non-existing file handling",
                () -> assertEquals(0, summary.getImportedCount(),
                        "Should import zero records"),
                () -> assertFalse(summary.getErrors().isEmpty(),
                        "Should have errors"),
                () -> assertTrue(summary.getErrors().get(0).toLowerCase().contains("file") ||
                                summary.getErrors().get(0).toLowerCase().contains("cannot"),
                        "Error should mention file issue. Actual: " + summary.getErrors().get(0))
        );
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t", "\n"})
    @DisplayName("Should handle empty or blank file paths")
    void importFromCsv_WithEmptyOrBlankPath_ShouldReturnErrorSummary(String filePath) {
        // Act
        ImportSummary summary = importService.importFromCsv(filePath);

        // Assert
        assertAll("Empty path validation",
                () -> assertEquals(0, summary.getImportedCount(),
                        "Should import zero records for path: '" + filePath + "'"),
                () -> assertFalse(summary.getErrors().isEmpty(),
                        "Should have errors for path: '" + filePath + "'")
        );
    }

    // ===== TESTOWANIE SPECJALNYCH PRZYPADKÓW =====

    @Test
    @DisplayName("Should handle CSV with different line endings")
    void importFromCsv_WithDifferentLineEndings_ShouldImportSuccessfully() throws IOException {
        // Arrange
        File csvFile = tempDir.resolve("mixed_line_endings.csv").toFile();
        String content = "firstName,lastName,email,company,position,salary\r\n" +  // Windows
                "Jan,Kowalski,jan@test.com,TechCorp,MANAGER,15000\n" +     // Unix
                "Anna,Nowak,anna@test.com,TechCorp,PROGRAMMER,9000\r\n";   // Windows

        Files.writeString(csvFile.toPath(), content);

        // Act
        ImportSummary summary = importService.importFromCsv(csvFile.getAbsolutePath());

        // Assert
        assertAll("Line endings validation",
                () -> assertEquals(2, summary.getImportedCount(),
                        "Should import all records regardless of line endings"),
                () -> assertTrue(summary.getErrors().isEmpty(),
                        "Should have no errors"),
                () -> assertEquals(2, employeeService.getEmployeeCount(),
                        "Should add all employees to service")
        );
    }

    // ===== TESTOWANIE WALIDACJI DANYCH =====

    @ParameterizedTest
    @CsvSource({
            "  Jan  ,  Kowalski  ,  jan@test.com  , MANAGER, 15000, Jan Kowalski",
            "Anna  ,Nowak  ,  anna@test.com, PROGRAMMER, 9000, Anna Nowak"
    })
    @DisplayName("Should trim whitespace from all fields")
    void parseEmployeeFromCsv_WithWhitespace_ShouldTrimFields(
            String firstName, String lastName, String email,
            String position, String salary, String expectedName) throws InvalidDataException {

        // Arrange
        String[] fieldsWithWhitespace = {firstName, lastName, email, "TechCorp", position, salary};

        // Act
        Employee employee = importService.parseEmployeeFromCsv(fieldsWithWhitespace, 1);

        // Assert
        assertAll("Whitespace trimming validation",
                () -> assertEquals(expectedName, employee.getName(),
                        "Should trim and concatenate name properly"),
                () -> assertEquals(email.trim(), employee.getEmail(),
                        "Should trim email"),
                () -> assertEquals(Position.valueOf(position.trim()), employee.getPosition(),
                        "Should trim and parse position"),
                () -> assertEquals(Double.parseDouble(salary.trim()), employee.getSalary(),
                        "Should parse trimmed salary")
        );
    }

    // ===== TESTOWANIE GRANICZNYCH WARTOŚCI =====

    @ParameterizedTest
    @ValueSource(doubles = {0.0, 1000000.0})
    @DisplayName("Should accept valid salary boundary values")
    void parseEmployeeFromCsv_WithBoundarySalaries_ShouldCreateEmployee(double salary) throws InvalidDataException {
        // Arrange
        String[] fields = {"John", "Doe", "john@test.com", "TechCorp", "PROGRAMMER", String.valueOf(salary)};

        // Act & Assert
        assertDoesNotThrow(() -> {
            Employee employee = importService.parseEmployeeFromCsv(fields, 1);
            assertEquals(salary, employee.getSalary(), 0.001,
                    "Should correctly parse salary: " + salary);
        }, "Should accept salary: " + salary);
    }

    @Test
    @DisplayName("Should reject duplicate emails within the same import")
    void importFromCsv_WithDuplicateEmailsInSameFile_ShouldSkipDuplicates() throws IOException {
        // Arrange
        File csvFile = createTestCsvFile("duplicates.csv",
                "firstName,lastName,email,company,position,salary",
                "Jan,Kowalski,same@test.com,TechCorp,MANAGER,15000",
                "Anna,Nowak,same@test.com,TechCorp,PROGRAMMER,9000",  // duplicate
                "Piotr,Wiśniewski,other@test.com,TechCorp,PROGRAMMER,8000"
        );

        // Act
        ImportSummary summary = importService.importFromCsv(csvFile.getAbsolutePath());

        // Assert
        assertAll("Duplicate email handling",
                () -> assertEquals(2, summary.getImportedCount(),
                        "Should import only unique emails"),
                () -> assertEquals(1, summary.getErrors().size(),
                        "Should report duplicate error"),
                () -> assertTrue(summary.getErrors().get(0).toLowerCase().contains("duplicate") ||
                                summary.getErrors().get(0).toLowerCase().contains("already"),
                        "Error should mention duplicate. Actual: " + summary.getErrors().get(0))
        );
    }

    // ===== TESTOWANIE STANU PO IMPORTACJI =====

    @Test
    @DisplayName("Should maintain employee service state after failed import")
    void importFromCsv_AfterFailedImport_ShouldMaintainServiceState() throws IOException {
        // Arrange - First import some data
        File firstFile = createTestCsvFile("first.csv",
                "firstName,lastName,email,company,position,salary",
                "Jan,Kowalski,jan@test.com,TechCorp,MANAGER,15000"
        );
        importService.importFromCsv(firstFile.getAbsolutePath());

        int initialCount = employeeService.getEmployeeCount();

        // Arrange - Second file with errors
        File secondFile = createTestCsvFile("second.csv",
                "firstName,lastName,email,company,position,salary",
                "Invalid,,,,,",  // completely invalid
                "Anna,Nowak,anna@test.com,TechCorp,PROGRAMMER,9000"  // valid
        );

        // Act
        ImportSummary summary = importService.importFromCsv(secondFile.getAbsolutePath());

        // Assert
        assertAll("Service state validation",
                () -> assertEquals(1, summary.getImportedCount(),
                        "Should import one valid record from second file"),
                () -> assertEquals(initialCount + 1, employeeService.getEmployeeCount(),
                        "Should add only valid records to service"),
                () -> assertTrue(summary.getErrors().size() >= 1,
                        "Should report errors for invalid records")
        );
    }

    @Test
    @DisplayName("Should handle CSV with only headers and no data")
    void importFromCsv_WithOnlyHeaders_ShouldReturnEmptySummary() throws IOException {
        // Arrange
        File csvFile = createTestCsvFile("only_headers.csv",
                "firstName,lastName,email,company,position,salary"
        );

        // Act
        ImportSummary summary = importService.importFromCsv(csvFile.getAbsolutePath());

        // Assert
        assertAll("Empty file import",
                () -> assertEquals(0, summary.getImportedCount(),
                        "Should import zero records"),
                () -> assertTrue(summary.getErrors().isEmpty(),
                        "Should have no errors"),
                () -> assertEquals(0, employeeService.getEmployeeCount(),
                        "Should not add any employees to service")
        );
    }

    @Test
    @DisplayName("Should handle malformed CSV with unclosed quotes")
    void importFromCsv_WithMalformedCsv_ShouldReturnErrorSummary() throws IOException {
        // Arrange
        File csvFile = tempDir.resolve("malformed.csv").toFile();
        String content = "firstName,lastName,email,company,position,salary\n" +
                "\"Jan,Kowalski,jan@test.com,TechCorp,MANAGER,15000"; // Unclosed quote

        Files.writeString(csvFile.toPath(), content);

        // Act
        ImportSummary summary = importService.importFromCsv(csvFile.getAbsolutePath());

        // Assert
        assertAll("Malformed CSV handling",
                () -> assertEquals(0, summary.getImportedCount(),
                        "Should import zero records"),
                () -> assertFalse(summary.getErrors().isEmpty(),
                        "Should have CSV parsing errors"),
                () -> assertTrue(summary.getErrors().get(0).toLowerCase().contains("csv"),
                        "Error should mention CSV parsing")
        );
    }

    @ParameterizedTest
    @ValueSource(doubles = { -1000.0, Double.NEGATIVE_INFINITY})
    @DisplayName("Should reject negative salary values")
    void parseEmployeeFromCsv_WithNegativeSalaries_ShouldThrowException(double salary) {
        // Arrange
        String[] fields = {"John", "Doe", "john@test.com", "TechCorp", "PROGRAMMER", String.valueOf(salary)};

        // Act & Assert
        InvalidDataException exception = assertThrows(
                InvalidDataException.class,
                () -> importService.parseEmployeeFromCsv(fields, 1)
        );

        assertTrue(exception.getMessage().toLowerCase().contains("negative") ||
                        exception.getMessage().toLowerCase().contains("salary"),
                "Error should mention negative salary. Actual: " + exception.getMessage());
    }

    @ParameterizedTest
    @CsvSource({
            "invalid.email,Invalid email format",
            "missing@domain,Invalid email format",
            "@missing.local,Invalid email format",
            "missing@.com,Invalid email format",
            "missing@domain.,Invalid email format"
    })
    @DisplayName("Should reject various invalid email formats")
    void parseEmployeeFromCsv_WithInvalidEmails_ShouldThrowException(String invalidEmail, String expectedError) {
        // Arrange
        String[] fields = {"John", "Doe", invalidEmail, "TechCorp", "PROGRAMMER", "5000"};

        // Act & Assert
        InvalidDataException exception = assertThrows(
                InvalidDataException.class,
                () -> importService.parseEmployeeFromCsv(fields, 1)
        );

        assertTrue(exception.getMessage().toLowerCase().contains("email"),
                "Error should mention email validation. Actual: " + exception.getMessage());
    }


    @Test
    @DisplayName("Should create ImportService with default EmployeeService")
    void constructor_NoArguments_ShouldCreateWithDefaultService() {
        // Act
        ImportService importService = new ImportService();

        // Assert
        assertNotNull(importService.getEmployeeService());
    }

    @Test
    @DisplayName("Should return correct employee service instance")
    void getEmployeeService_ShouldReturnInjectedService() {
        // Arrange
        EmployeeService mockService = new EmployeeService();
        ImportService importService = new ImportService(mockService);

        // Act & Assert
        assertSame(mockService, importService.getEmployeeService());
    }



    @Test
    @DisplayName("Should skip empty lines in CSV")
    void importFromCsv_WithEmptyLines_ShouldSkipThem() throws IOException {
        // Arrange
        File csvFile = createTestCsvFile("empty_lines.csv",
                "firstName,lastName,email,company,position,salary",
                "Jan,Kowalski,jan@test.com,TechCorp,MANAGER,15000",
                "", // pusta linia
                "   ", // linia ze spacjami
                ",,,,,",
                "Anna,Nowak,anna@test.com,TechCorp,PROGRAMMER,9000"
        );

        // Act
        ImportSummary summary = importService.importFromCsv(csvFile.getAbsolutePath());

        // Assert
        assertEquals(2, summary.getImportedCount());
        assertTrue(summary.getErrors().isEmpty());
    }




    // ===== POMOCNICZE METODY =====

    private File createTestCsvFile(String filename, String... lines) throws IOException {
        File file = tempDir.resolve(filename).toFile();
        try (FileWriter writer = new FileWriter(file)) {
            for (String line : lines) {
                writer.write(line + System.lineSeparator());
            }
        }
        return file;
    }
}




