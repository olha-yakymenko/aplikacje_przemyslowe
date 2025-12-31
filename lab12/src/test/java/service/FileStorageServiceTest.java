package com.techcorp.employee.service;

import com.techcorp.employee.exception.FileStorageException;
import com.techcorp.employee.exception.InvalidFileException;
import com.techcorp.employee.exception.FileNotFoundException;
import com.techcorp.employee.exception.MaxUploadSizeExceededException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FileStorageServiceTest {

    @TempDir
    Path tempDir;

    private FileStorageService fileStorageService;
    private Path uploadsDir;
    private Path reportsDir;
    private Path documentsDir;
    private Path photosDir;

    @BeforeEach
    void setUp() throws IOException {
        uploadsDir = tempDir.resolve("uploads");
        reportsDir = tempDir.resolve("reports");
        documentsDir = tempDir.resolve("documents");
        photosDir = tempDir.resolve("photos");

        Files.createDirectories(uploadsDir);
        Files.createDirectories(reportsDir);
        Files.createDirectories(documentsDir);
        Files.createDirectories(photosDir);

        fileStorageService = new FileStorageService(
                uploadsDir.toString(),
                reportsDir.toString(),
                documentsDir.toString(),
                photosDir.toString()
        );
    }

    // ========== TESTY ZAPISU PLIKÓW ==========

    @Test
    @DisplayName("Should store file with UUID name in subdirectory")
    void storeFile_ShouldStoreFileWithUUIDName() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.csv", "text/csv", "content".getBytes()
        );

        String fileName = fileStorageService.storeFile(file, "test-subdir");

        assertAll(
                () -> assertNotNull(fileName),
                () -> assertTrue(fileName.endsWith(".csv")),
                () -> assertTrue(Files.exists(uploadsDir.resolve("test-subdir").resolve(fileName)))
        );
    }

    @Test
    @DisplayName("Should store file with original name when requested")
    void storeFile_WithOriginalName_ShouldStoreWithOriginalName() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "original.csv", "text/csv", "content".getBytes()
        );

        String fileName = fileStorageService.storeFile(file, "test-subdir", true);

        assertAll(
                () -> assertEquals("original.csv", fileName),
                () -> assertTrue(Files.exists(uploadsDir.resolve("test-subdir").resolve(fileName)))
        );
    }

    @Test
    @DisplayName("Should store file with custom name")
    void storeFileWithCustomName_ShouldStoreWithCustomName() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "original.csv", "text/csv", "content".getBytes()
        );

        String fileName = fileStorageService.storeFileWithCustomName(file, "custom", "custom-name.csv");

        assertAll(
                () -> assertEquals("custom-name.csv", fileName),
                () -> assertTrue(Files.exists(uploadsDir.resolve("custom").resolve(fileName)))
        );
    }

    @Test
    @DisplayName("Should store photo with normalized email as filename")
    void storePhoto_ShouldStoreWithNormalizedEmailAsName() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "photo.jpg", "image/jpeg", new byte[100]
        );

        String fileName = fileStorageService.storePhoto(file, "john@example.com");

        assertAll(
                () -> assertEquals("john_example.com.jpg", fileName),
                () -> assertTrue(Files.exists(uploadsDir.resolve("photos").resolve(fileName)))
        );
    }

    @Test
    @DisplayName("Should store report in report type subdirectory")
    void storeReport_ShouldStoreInReportSubdirectory() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "report.pdf", "application/pdf", "report content".getBytes()
        );

        String fileName = fileStorageService.storeReport(file, "monthly");

        assertAll(
                () -> assertNotNull(fileName),
                () -> assertTrue(Files.exists(uploadsDir.resolve("reports").resolve("monthly").resolve(fileName)))
        );
    }

    // ========== TESTY WALIDACJI PLIKÓW ==========

    @Test
    @DisplayName("Should validate file is not null")
    void validateFile_WithNullFile_ShouldThrowException() {
        assertThrows(InvalidFileException.class, () -> fileStorageService.validateFile(null));
    }

    @Test
    @DisplayName("Should validate file is not empty")
    void validateFile_WithEmptyFile_ShouldThrowException() {
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file", "test.csv", "text/csv", new byte[0]
        );

        assertThrows(InvalidFileException.class, () -> fileStorageService.validateFile(emptyFile));
    }

    @Test
    @DisplayName("Should validate file type with allowed extensions")
    void validateFileType_WithValidExtension_ShouldNotThrow() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.csv", "text/csv", "content".getBytes()
        );

        assertDoesNotThrow(() -> fileStorageService.validateFileType(file, new String[]{".csv", ".xml"}));
    }

    @Test
    @DisplayName("Should reject file with invalid extension")
    void validateFileType_WithInvalidExtension_ShouldThrowException() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.txt", "text/plain", "content".getBytes()
        );

        assertThrows(InvalidFileException.class,
                () -> fileStorageService.validateFileType(file, new String[]{".csv", ".xml"}));
    }

    @Test
    @DisplayName("Should validate file size")
    void validateFileSize_WithValidSize_ShouldNotThrow() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.csv", "text/csv", "content".getBytes()
        );

        assertDoesNotThrow(() -> fileStorageService.validateFileSize(file, 1024 * 1024));
    }

    @Test
    @DisplayName("Should reject oversized file")
    void validateFileSize_WithOversizedFile_ShouldThrowException() {
        byte[] largeContent = new byte[1024 * 1024 + 1];
        MockMultipartFile file = new MockMultipartFile(
                "file", "large.csv", "text/csv", largeContent
        );

        assertThrows(MaxUploadSizeExceededException.class,
                () -> fileStorageService.validateFileSize(file, 1024 * 1024));
    }

    @ParameterizedTest
    @ValueSource(strings = {".jpg", ".jpeg", ".png"})
    @DisplayName("Should validate image file types")
    void validateImageFile_WithValidImageTypes_ShouldNotThrow(String extension) {
        String mimeType = extension.equals(".jpg") ? "image/jpeg" : "image/" + extension.substring(1);
        MockMultipartFile file = new MockMultipartFile(
                "file", "photo" + extension, mimeType, new byte[100]
        );

        assertDoesNotThrow(() -> fileStorageService.validateImageFile(file));
    }

    // ========== TESTY ODCZYTU PLIKÓW ==========

    @Test
    @DisplayName("Should load existing file as resource")
    void loadFileAsResource_WithExistingFile_ShouldReturnResource() throws IOException {
        String testContent = "test content";
        Path testFile = uploadsDir.resolve("test-subdir").resolve("test.txt");
        Files.createDirectories(testFile.getParent());
        Files.write(testFile, testContent.getBytes());

        Resource resource = fileStorageService.loadFileAsResource("test.txt", "test-subdir");

        assertAll(
                () -> assertNotNull(resource),
                () -> assertTrue(resource.exists()),
                () -> assertTrue(resource.isReadable())
        );
    }

    @Test
    @DisplayName("Should throw exception for non-existing file")
    void loadFileAsResource_WithNonExistingFile_ShouldThrowException() {
        assertThrows(FileNotFoundException.class,
                () -> fileStorageService.loadFileAsResource("nonexistent.txt", "test-subdir"));
    }

    @Test
    @DisplayName("Should load photo by normalized email")
    void loadPhoto_WithExistingPhoto_ShouldReturnResource() throws IOException {
        String email = "john@example.com";
        String normalizedEmail = "john_example.com";
        Path photoFile = uploadsDir.resolve("photos").resolve(normalizedEmail + ".jpg");
        Files.createDirectories(photoFile.getParent());
        Files.write(photoFile, new byte[100]);

        Resource resource = fileStorageService.loadPhoto(email);

        assertAll(
                () -> assertNotNull(resource),
                () -> assertTrue(resource.exists())
        );
    }

    @Test
    @DisplayName("Should throw exception for non-existing photo")
    void loadPhoto_WithNonExistingPhoto_ShouldThrowException() {
        assertThrows(FileNotFoundException.class,
                () -> fileStorageService.loadPhoto("nonexistent@example.com"));
    }

    @Test
    @DisplayName("Should load document by filename and normalized email")
    void loadDocument_WithExistingDocument_ShouldReturnResource() throws IOException {
        String email = "john@example.com";
        String normalizedEmail = "john_example.com";
        String fileName = "contract.pdf";
        Path docDir = uploadsDir.resolve("documents").resolve(normalizedEmail);
        Path docFile = docDir.resolve(fileName);
        Files.createDirectories(docDir);
        Files.write(docFile, new byte[100]);

        Resource resource = fileStorageService.loadDocument(fileName, email);

        assertAll(
                () -> assertNotNull(resource),
                () -> assertTrue(resource.exists())
        );
    }

    // ========== TESTY USUWANIA PLIKÓW ==========

    @Test
    @DisplayName("Should delete existing file")
    void deleteFile_WithExistingFile_ShouldReturnTrue() throws IOException {
        Path testFile = uploadsDir.resolve("test-subdir").resolve("test.txt");
        Files.createDirectories(testFile.getParent());
        Files.write(testFile, "content".getBytes());

        boolean deleted = fileStorageService.deleteFile("test.txt", "test-subdir");

        assertAll(
                () -> assertTrue(deleted),
                () -> assertFalse(Files.exists(testFile))
        );
    }

    @Test
    @DisplayName("Should return false when deleting non-existing file")
    void deleteFile_WithNonExistingFile_ShouldReturnFalse() {
        boolean deleted = fileStorageService.deleteFile("nonexistent.txt", "test-subdir");

        assertFalse(deleted);
    }

    @Test
    @DisplayName("Should delete photo by normalized email")
    void deletePhoto_WithExistingPhoto_ShouldReturnTrue() throws IOException {
        String email = "john@example.com";
        String normalizedEmail = "john_example.com";
        Path photoFile = uploadsDir.resolve("photos").resolve(normalizedEmail + ".jpg");
        Files.createDirectories(photoFile.getParent());
        Files.write(photoFile, new byte[100]);

        boolean deleted = fileStorageService.deletePhoto(email);

        assertAll(
                () -> assertTrue(deleted),
                () -> assertFalse(Files.exists(photoFile))
        );
    }

    @Test
    @DisplayName("Should delete document by filename and normalized email")
    void deleteDocument_WithExistingDocument_ShouldReturnTrue() throws IOException {
        String email = "john@example.com";
        String normalizedEmail = "john_example.com";
        String fileName = "contract.pdf";
        Path docDir = uploadsDir.resolve("documents").resolve(normalizedEmail);
        Path docFile = docDir.resolve(fileName);
        Files.createDirectories(docDir);
        Files.write(docFile, new byte[100]);

        boolean deleted = fileStorageService.deleteDocument(fileName, email);

        assertAll(
                () -> assertTrue(deleted),
                () -> assertFalse(Files.exists(docFile))
        );
    }

    // ========== TESTY METOD UTILITY ==========

    @Test
    @DisplayName("Should check if file exists")
    void fileExists_WithExistingFile_ShouldReturnTrue() throws IOException {
        Path testFile = uploadsDir.resolve("test-subdir").resolve("test.txt");
        Files.createDirectories(testFile.getParent());
        Files.write(testFile, "content".getBytes());

        boolean exists = fileStorageService.fileExists("test.txt", "test-subdir");

        assertTrue(exists);
    }

    @Test
    @DisplayName("Should return false for non-existing file")
    void fileExists_WithNonExistingFile_ShouldReturnFalse() {
        boolean exists = fileStorageService.fileExists("nonexistent.txt", "test-subdir");

        assertFalse(exists);
    }

    @Test
    @DisplayName("Should get file size")
    void getFileSize_WithExistingFile_ShouldReturnSize() throws IOException {
        String content = "test content";
        Path testFile = uploadsDir.resolve("test-subdir").resolve("test.txt");
        Files.createDirectories(testFile.getParent());
        Files.write(testFile, content.getBytes());

        long size = fileStorageService.getFileSize("test.txt", "test-subdir");

        assertEquals(content.length(), size);
    }

    @Test
    @DisplayName("Should throw exception when getting size of non-existing file")
    void getFileSize_WithNonExistingFile_ShouldThrowException() {
        assertThrows(FileNotFoundException.class,
                () -> fileStorageService.getFileSize("nonexistent.txt", "test-subdir"));
    }

    // ========== TESTY BŁĘDÓW I WYJĄTKÓW ==========

    @Test
    @DisplayName("Should throw exception when storage directory doesn't exist")
    void constructor_WithNonExistentDirectories_ShouldThrowException() {
        Path nonExistentDir = tempDir.resolve("nonexistent");

        assertThrows(FileStorageException.class, () ->
                new FileStorageService(
                        nonExistentDir.toString(),
                        nonExistentDir.toString(),
                        nonExistentDir.toString(),
                        nonExistentDir.toString()
                ));
    }


    // ========== TESTY NORMALIZACJI NAZW ==========

    @Test
    @DisplayName("Should normalize file names with special characters")
    void storeFileWithCustomName_WithSpecialCharacters_ShouldNormalize() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "dirty name.txt", "text/plain", "content".getBytes()
        );

        String fileName = fileStorageService.storeFileWithCustomName(file, "test", "file with spaces & special*chars?.txt");

        assertAll(
                () -> assertNotNull(fileName),
                () -> assertFalse(fileName.contains(" ")),
                () -> assertFalse(fileName.contains("*")),
                () -> assertFalse(fileName.contains("?")),
                () -> assertEquals("file_with_spaces___special_chars_.txt", fileName)
        );
    }

    // ========== TESTY GETTERÓW ==========

    @Test
    @DisplayName("Should return correct storage locations")
    void getStorageLocations_ShouldReturnCorrectPaths() {
        assertAll(
                () -> assertEquals(uploadsDir, fileStorageService.getFileStorageLocation()),
                () -> assertEquals(reportsDir, fileStorageService.getReportsStorageLocation()),
                () -> assertEquals(documentsDir, fileStorageService.getDocumentsStorageLocation()),
                () -> assertEquals(photosDir, fileStorageService.getPhotosStorageLocation())
        );
    }

    // ========== DODATKOWE TESTY WYJĄTKÓW ==========

    @Test
    @DisplayName("Should throw InvalidFileException when file has path traversal")
    void validateFile_WithPathTraversalFilename_ShouldThrowInvalidFileException() {
        MockMultipartFile maliciousFile = new MockMultipartFile(
                "file", "../../etc/passwd", "text/plain", "content".getBytes()
        );

        assertThrows(InvalidFileException.class, () -> fileStorageService.validateFile(maliciousFile));
    }

    @Test
    @DisplayName("Should throw FileNotFoundException when loading non-existent document")
    void loadDocument_WithNonExistentDocument_ShouldThrowFileNotFoundException() {
        assertThrows(FileNotFoundException.class,
                () -> fileStorageService.loadDocument("nonexistent.pdf", "test@example.com"));
    }

    @Test
    @DisplayName("Should throw InvalidFileException for image with wrong MIME type")
    void validateImageFile_WithWrongMimeType_ShouldThrowInvalidFileException() {
        MockMultipartFile fakeImage = new MockMultipartFile(
                "file", "image.jpg", "text/plain", new byte[100]
        );

        assertThrows(InvalidFileException.class, () -> fileStorageService.validateImageFile(fakeImage));
    }

    @Test
    @DisplayName("Should throw MaxUploadSizeExceededException for oversized image")
    void validateImageFile_WithOversizedImage_ShouldThrowMaxUploadSizeExceededException() {
        byte[] largeImage = new byte[3 * 1024 * 1024];
        MockMultipartFile largeImageFile = new MockMultipartFile(
                "file", "large.jpg", "image/jpeg", largeImage
        );

        assertThrows(MaxUploadSizeExceededException.class,
                () -> fileStorageService.validateImageFile(largeImageFile));
    }

    @Test
    @DisplayName("Should throw FileNotFoundException when photos directory doesn't exist")
    void loadPhoto_WithMissingPhotosDirectory_ShouldThrowFileNotFoundException() throws IOException {
        Files.deleteIfExists(uploadsDir.resolve("photos"));

        assertThrows(FileNotFoundException.class,
                () -> fileStorageService.loadPhoto("test@example.com"));
    }

    @Test
    @DisplayName("Should throw InvalidFileException for path traversal attempt in load")
    void loadFileAsResource_WithPathTraversal_ShouldThrowInvalidFileException() {
        // Teraz powinno rzucać InvalidFileException zamiast SecurityException
        assertThrows(InvalidFileException.class,
                () -> fileStorageService.loadFileAsResource("../../../etc/passwd", "subdir"));
    }

    @Test
    @DisplayName("Should throw InvalidFileException for file with no extension when required")
    void validateFileType_WithNoExtension_ShouldThrowInvalidFileException() {
        MockMultipartFile noExtensionFile = new MockMultipartFile(
                "file", "noextension", "text/plain", "content".getBytes()
        );

        assertThrows(InvalidFileException.class,
                () -> fileStorageService.validateFileType(noExtensionFile, new String[]{".txt"}));
    }

    @Test
    @DisplayName("Should throw InvalidFileException for document with invalid type")
    void storeDocument_WithInvalidDocumentType_ShouldThrowInvalidFileException() {
        MockMultipartFile invalidDocument = new MockMultipartFile(
                "file", "script.exe", "application/octet-stream", new byte[100]
        );

        assertThrows(InvalidFileException.class,
                () -> fileStorageService.storeDocument(invalidDocument, "test@example.com"));
    }

    // ========== TESTY GRANICZNE ==========

    @Test
    @DisplayName("Should handle maximum allowed file size")
    void validateFileSize_WithMaximumAllowedSize_ShouldNotThrow() {
        byte[] maxSizeContent = new byte[10 * 1024 * 1024];
        MockMultipartFile maxSizeFile = new MockMultipartFile(
                "file", "max.csv", "text/csv", maxSizeContent
        );

        assertDoesNotThrow(() -> fileStorageService.validateFileSize(maxSizeFile, 10 * 1024 * 1024));
    }

    @Test
    @DisplayName("Should reject file exceeding maximum size by 1 byte")
    void validateFileSize_WithSizeExceedingByOneByte_ShouldThrowMaxUploadSizeExceededException() {
        byte[] oversizedContent = new byte[10 * 1024 * 1024 + 1];
        MockMultipartFile oversizedFile = new MockMultipartFile(
                "file", "oversized.csv", "text/csv", oversizedContent
        );

        assertThrows(MaxUploadSizeExceededException.class,
                () -> fileStorageService.validateFileSize(oversizedFile, 10 * 1024 * 1024));
    }

    @Test
    @DisplayName("Should handle very long filenames")
    void storeFile_WithVeryLongFilename_ShouldNormalizeSuccessfully() {
        String longName = "a".repeat(200) + ".txt";
        MockMultipartFile file = new MockMultipartFile(
                "file", longName, "text/plain", "content".getBytes()
        );

        assertDoesNotThrow(() -> fileStorageService.storeFile(file, "test", true));
    }

    // ========== TESTY WALIDACJI KONSTRUKTORA ==========

    @Test
    @DisplayName("Should throw FileStorageException when directory is a file")
    void constructor_WithFileInsteadOfDirectory_ShouldThrowFileStorageException() throws IOException {
        Path filePath = tempDir.resolve("file.txt");
        Files.write(filePath, "content".getBytes());

        assertThrows(FileStorageException.class, () ->
                new FileStorageService(
                        filePath.toString(),
                        reportsDir.toString(),
                        documentsDir.toString(),
                        photosDir.toString()
                ));
    }

    // ========== TESTY WIELOKROTNEGO ZAPISU ==========

    @Test
    @DisplayName("Should handle storing file with same name multiple times")
    void storeFile_WithSameNameMultipleTimes_ShouldOverwrite() {
        MockMultipartFile file1 = new MockMultipartFile(
                "file", "same.txt", "text/plain", "content1".getBytes()
        );
        MockMultipartFile file2 = new MockMultipartFile(
                "file", "same.txt", "text/plain", "content2".getBytes()
        );

        String fileName1 = fileStorageService.storeFile(file1, "test", true);
        String fileName2 = fileStorageService.storeFile(file2, "test", true);

        assertAll(
                () -> assertEquals("same.txt", fileName1),
                () -> assertEquals("same.txt", fileName2)
        );
    }

    // ========== TESTY NORMALIZACJI EKSTREMI ==========

    @Test
    @DisplayName("Should normalize extremely dirty filenames")
    void normalizeFileName_WithExtremelyDirtyName_ShouldProduceSafeName() {
        String dirtyName = "file/with\\multiple*problematic?chars\"<>|:.txt";
        MockMultipartFile file = new MockMultipartFile(
                "file", dirtyName, "text/plain", "content".getBytes()
        );

        String normalizedName = fileStorageService.storeFileWithCustomName(file, "test", dirtyName);

        assertAll(
                () -> assertNotNull(normalizedName),
                () -> assertFalse(normalizedName.contains("/")),
                () -> assertFalse(normalizedName.contains("\\")),
                () -> assertFalse(normalizedName.contains("*")),
                () -> assertFalse(normalizedName.contains("?")),
                () -> assertFalse(normalizedName.contains("\"")),
                () -> assertFalse(normalizedName.contains("<")),
                () -> assertFalse(normalizedName.contains(">")),
                () -> assertFalse(normalizedName.contains("|")),
                () -> assertFalse(normalizedName.contains(":"))
        );
    }

    // ========== NOWE TESTY DLA POPRAWIONEGO KODU ==========


    @Test
    @DisplayName("Should handle file with only dots in filename")
    void validateFile_WithOnlyDotsFilename_ShouldThrowInvalidFileException() {
        MockMultipartFile dotsFile = new MockMultipartFile(
                "file", "...", "text/plain", "content".getBytes()
        );

        assertThrows(InvalidFileException.class, () -> fileStorageService.validateFile(dotsFile));
    }


    @Test
    @DisplayName("Should normalize email with special characters")
    void normalizeFileName_WithSpecialEmail_ShouldProduceSafeName() {
        String email = "user+tag@example-domain.com";
        String normalized = "user_tag_example-domain.com";

        MockMultipartFile file = new MockMultipartFile(
                "file", "photo.jpg", "image/jpeg", new byte[100]
        );

        String fileName = fileStorageService.storePhoto(file, email);

        assertTrue(fileName.startsWith(normalized));
    }

    @Test
    @DisplayName("Should handle empty subdirectory")
    void storeFile_WithEmptySubdirectory_ShouldStoreInRoot() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.txt", "text/plain", "content".getBytes()
        );

        String fileName = fileStorageService.storeFile(file, "");

        assertAll(
                () -> assertNotNull(fileName),
                () -> assertTrue(Files.exists(uploadsDir.resolve(fileName)))
        );
    }

    @Test
    @DisplayName("Should handle null subdirectory")
    void storeFile_WithNullSubdirectory_ShouldStoreInRoot() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.txt", "text/plain", "content".getBytes()
        );

        String fileName = fileStorageService.storeFile(file, null);

        assertAll(
                () -> assertNotNull(fileName),
                () -> assertTrue(Files.exists(uploadsDir.resolve(fileName)))
        );
    }
}