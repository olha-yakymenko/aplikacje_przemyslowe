package com.techcorp.employee.service;

import com.techcorp.employee.exception.InvalidFileException;
import com.techcorp.employee.exception.FileNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class FileStorageServiceTest {

    @TempDir
    Path tempDir;

    private FileStorageService fileStorageService;
    private MultipartFile validFile;
    private MultipartFile emptyFile;

    @BeforeEach
    void setUp() {
        String uploadDir = tempDir.resolve("uploads").toString();
        String reportsDir = tempDir.resolve("reports").toString();
        String documentsDir = tempDir.resolve("documents").toString();
        String photosDir = tempDir.resolve("photos").toString();

        fileStorageService = new FileStorageService(uploadDir, reportsDir, documentsDir, photosDir);

        validFile = new MockMultipartFile(
                "file", "test.txt", "text/plain", "Hello World".getBytes()
        );

        emptyFile = new MockMultipartFile(
                "file", "empty.txt", "text/plain", new byte[0]
        );
    }

    @Test
    void storeFile_WithValidFile_ShouldStoreSuccessfully() throws Exception {
        // When
        String fileName = fileStorageService.storeFile(validFile, "test");

        // Then
        assertNotNull(fileName);
        assertTrue(fileName.endsWith(".txt"));

        Path storedFile = tempDir.resolve("uploads").resolve("test").resolve(fileName);
        assertTrue(Files.exists(storedFile));
    }

    @Test
    void storeFile_WithOriginalName_ShouldUseOriginalName() throws Exception {
        // When
        String fileName = fileStorageService.storeFile(validFile, "test", true);

        // Then
        assertEquals("test.txt", fileName);
    }

    @Test
    void storeFile_WithEmptyFile_ShouldThrowException() throws Exception {
        // When & Then
        assertThrows(InvalidFileException.class, () -> {
            fileStorageService.storeFile(emptyFile, "test");
        });
    }

    @Test
    void loadFileAsResource_WithExistingFile_ShouldReturnResource() throws Exception {
        // Given
        String fileName = fileStorageService.storeFile(validFile, "test");

        // When
        Resource resource = fileStorageService.loadFileAsResource(fileName, "test");

        // Then
        assertNotNull(resource);
        assertTrue(resource.exists());
    }

    @Test
    void loadFileAsResource_WithNonExistingFile_ShouldThrowException() throws Exception {
        // When & Then
        assertThrows(FileNotFoundException.class, () -> {
            fileStorageService.loadFileAsResource("nonexistent.txt", "test");
        });
    }

    @Test
    void deleteFile_WithExistingFile_ShouldDeleteSuccessfully() throws Exception {
        // Given
        String fileName = fileStorageService.storeFile(validFile, "test");

        // When
        boolean result = fileStorageService.deleteFile(fileName, "test");

        // Then
        assertTrue(result);
    }

    @Test
    void validateFileType_WithAllowedExtension_ShouldNotThrow() throws Exception {
        // Given
        MultipartFile csvFile = new MockMultipartFile(
                "file", "test.csv", "text/csv", "data".getBytes()
        );

        // When & Then - No exception should be thrown
        assertDoesNotThrow(() -> {
            fileStorageService.validateFileType(csvFile, new String[]{".csv", ".txt"});
        });
    }

    @Test
    void validateFileType_WithDisallowedExtension_ShouldThrowException() throws Exception {
        // Given
        MultipartFile exeFile = new MockMultipartFile(
                "file", "test.exe", "application/exe", "data".getBytes()
        );

        // When & Then
        assertThrows(InvalidFileException.class, () -> {
            fileStorageService.validateFileType(exeFile, new String[]{".csv", ".txt"});
        });
    }

    @Test
    void validateFileSize_WithValidSize_ShouldNotThrow() throws Exception {
        // Given
        MultipartFile smallFile = new MockMultipartFile(
                "file", "small.txt", "text/plain", new byte[1024] // 1KB
        );

        // When & Then - No exception should be thrown
        assertDoesNotThrow(() -> {
            fileStorageService.validateFileSize(smallFile, 2048); // 2KB max
        });
    }

    @Test
    void validateImageFile_WithValidImage_ShouldNotThrow() throws Exception {
        // Given
        MultipartFile jpgFile = new MockMultipartFile(
                "file", "test.jpg", "image/jpeg", new byte[1024]
        );

        // When & Then - No exception should be thrown
        assertDoesNotThrow(() -> {
            fileStorageService.validateImageFile(jpgFile);
        });
    }
}