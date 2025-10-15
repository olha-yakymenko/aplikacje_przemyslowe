
import src.model.Employee;
import src.model.ImportSummary;
import src.model.Position;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import src.service.ImportService;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class ImportServiceTest {

    @TempDir
    File tempDir;

    @Test
    void testImportFromCsvSuccess() throws IOException {
        // Create test CSV file
        File csvFile = new File(tempDir, "test.csv");
        try (FileWriter writer = new FileWriter(csvFile)) {
            writer.write("firstName,lastName,email,company,position,salary\n");
            writer.write("John,Doe,john@email.com,TechCorp,PROGRAMMER,9000\n");
            writer.write("Jane,Smith,jane@email.com,TechCorp,MANAGER,15000\n");
        }

        ImportService importService = new ImportService();
        ImportSummary summary = importService.importFromCsv(csvFile.getAbsolutePath());

        assertEquals(2, summary.getImportedCount());
        assertTrue(summary.getErrors().isEmpty());
        assertEquals(2, importService.getEmployeeService().getEmployeeCount());
    }

    @Test
    void testImportFromCsvWithErrors() throws IOException {
        File csvFile = new File(tempDir, "test.csv");
        try (FileWriter writer = new FileWriter(csvFile)) {
            writer.write("firstName,lastName,email,company,position,salary\n");
            writer.write("John,Doe,john@email.com,TechCorp,PROGRAMMER,9000\n");
            writer.write("Jane,Smith,,TechCorp,MANAGER,15000\n"); // Missing email
            writer.write("Bob,Wilson,bob@email.com,TechCorp,INVALID_POSITION,5000\n"); // Invalid position
            writer.write("Alice,Brown,alice@email.com,TechCorp,PROGRAMMER,-1000\n"); // Negative salary
        }

        ImportService importService = new ImportService();
        ImportSummary summary = importService.importFromCsv(csvFile.getAbsolutePath());

        assertEquals(1, summary.getImportedCount());
        assertEquals(3, summary.getErrors().size());
    }

    @Test
    void testImportFromCsvEmptyFile() throws IOException {
        File csvFile = new File(tempDir, "test.csv");
        try (FileWriter writer = new FileWriter(csvFile)) {
            writer.write("firstName,lastName,email,company,position,salary\n");
        }

        ImportService importService = new ImportService();
        ImportSummary summary = importService.importFromCsv(csvFile.getAbsolutePath());

        assertEquals(0, summary.getImportedCount());
        assertTrue(summary.getErrors().isEmpty());
    }

    @Test
    void testImportFromCsvWithEmptyLines() throws IOException {
        File csvFile = new File(tempDir, "test.csv");
        try (FileWriter writer = new FileWriter(csvFile)) {
            writer.write("firstName,lastName,email,company,position,salary\n");
            writer.write("John,Doe,john@email.com,TechCorp,PROGRAMMER,9000\n");
            writer.write("\n"); // Empty line
            writer.write("Jane,Smith,jane@email.com,TechCorp,MANAGER,15000\n");
        }

        ImportService importService = new ImportService();
        ImportSummary summary = importService.importFromCsv(csvFile.getAbsolutePath());

        assertEquals(2, summary.getImportedCount());
        assertTrue(summary.getErrors().isEmpty());
    }
}