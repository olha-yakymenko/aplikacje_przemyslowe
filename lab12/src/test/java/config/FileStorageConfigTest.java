package config;


import com.techcorp.employee.config.FileStorageConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@TestPropertySource(properties = {
        "app.upload.directory=/test/upload",
        "app.reports.directory=/test/reports",
        "app.documents.directory=/test/documents",
        "app.photos.directory=/test/photos"
})
class FileStorageConfigTest {

    private FileStorageConfig fileStorageConfig;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        fileStorageConfig = new FileStorageConfig();

        // Ustaw wartości pól za pomocą ReflectionTestUtils
        ReflectionTestUtils.setField(fileStorageConfig, "uploadDir",
                tempDir.resolve("upload").toString());
        ReflectionTestUtils.setField(fileStorageConfig, "reportsDir",
                tempDir.resolve("reports").toString());
        ReflectionTestUtils.setField(fileStorageConfig, "documentsDir",
                tempDir.resolve("documents").toString());
        ReflectionTestUtils.setField(fileStorageConfig, "photosDir",
                tempDir.resolve("photos").toString());
    }

    @Test
    void shouldCreateAllDirectoriesSuccessfully() throws Exception {
        // When
        fileStorageConfig.run();

        // Then
        assertTrue(Files.exists(tempDir.resolve("upload")));
        assertTrue(Files.exists(tempDir.resolve("reports")));
        assertTrue(Files.exists(tempDir.resolve("documents")));
        assertTrue(Files.exists(tempDir.resolve("photos")));
    }

    @Test
    void shouldHandleAlreadyExistingDirectories() throws Exception {
        // Given - create directories manually first
        Files.createDirectories(tempDir.resolve("upload"));
        Files.createDirectories(tempDir.resolve("reports"));
        Files.createDirectories(tempDir.resolve("documents"));
        Files.createDirectories(tempDir.resolve("photos"));

        // When & Then - should not throw exception
        assertDoesNotThrow(() -> fileStorageConfig.run());
    }

    @Test
    void shouldCreateDirectoriesWithNestedPaths() throws Exception {
        // Given - set nested directory paths
        ReflectionTestUtils.setField(fileStorageConfig, "uploadDir",
                tempDir.resolve("deep/nested/upload").toString());
        ReflectionTestUtils.setField(fileStorageConfig, "reportsDir",
                tempDir.resolve("very/deep/nested/reports").toString());

        // When
        fileStorageConfig.run();

        // Then
        assertTrue(Files.exists(tempDir.resolve("deep/nested/upload")));
        assertTrue(Files.exists(tempDir.resolve("very/deep/nested/reports")));
    }

    @Test
    void shouldRunWithoutParameters() throws Exception {
        // When & Then - should not throw exception with empty args
        assertDoesNotThrow(() -> fileStorageConfig.run());
        assertDoesNotThrow(() -> fileStorageConfig.run("arg1", "arg2"));
    }

    @Test
    void shouldInitializeWithCorrectPropertyValues() {
        // Given
        String expectedUploadDir = tempDir.resolve("upload").toString();
        String expectedReportsDir = tempDir.resolve("reports").toString();
        String expectedDocumentsDir = tempDir.resolve("documents").toString();
        String expectedPhotosDir = tempDir.resolve("photos").toString();

        // When
        String actualUploadDir = (String) ReflectionTestUtils.getField(fileStorageConfig, "uploadDir");
        String actualReportsDir = (String) ReflectionTestUtils.getField(fileStorageConfig, "reportsDir");
        String actualDocumentsDir = (String) ReflectionTestUtils.getField(fileStorageConfig, "documentsDir");
        String actualPhotosDir = (String) ReflectionTestUtils.getField(fileStorageConfig, "photosDir");

        // Then
        assertEquals(expectedUploadDir, actualUploadDir);
        assertEquals(expectedReportsDir, actualReportsDir);
        assertEquals(expectedDocumentsDir, actualDocumentsDir);
        assertEquals(expectedPhotosDir, actualPhotosDir);
    }
}