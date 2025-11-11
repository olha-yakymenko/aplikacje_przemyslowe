package com.techcorp.employee.service;

import com.techcorp.employee.model.EmployeeDocument;
import com.techcorp.employee.model.DocumentType;
import com.techcorp.employee.exception.EmployeeNotFoundException;
import com.techcorp.employee.exception.FileNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentStorageServiceTest {

    @Mock
    private FileStorageService fileStorageService;

    @Mock
    private EmployeeService employeeService;

    private DocumentStorageService documentStorageService;

    private MultipartFile testFile;
    private MultipartFile anotherFile;

    @BeforeEach
    void setUp() {
        documentStorageService = new DocumentStorageService(fileStorageService, employeeService);

        testFile = new MockMultipartFile(
                "file", "test.pdf", "application/pdf", "test content".getBytes()
        );

        anotherFile = new MockMultipartFile(
                "file", "contract.docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                "contract content".getBytes()
        );
    }

    @Test
    void storeDocument_WithValidData_ShouldStoreDocument() {
        // Given
        String employeeEmail = "john.doe@company.com";
        String storedFileName = "uuid_test.pdf";

        when(employeeService.employeeExists(employeeEmail)).thenReturn(true);
        when(fileStorageService.storeFile(any(MultipartFile.class), eq("documents/" + employeeEmail)))
                .thenReturn(storedFileName);

        // When
        EmployeeDocument result = documentStorageService.storeDocument(employeeEmail, testFile, DocumentType.CONTRACT);

        // Then
        assertNotNull(result);
        assertEquals(employeeEmail, result.getEmployeeEmail());
        assertEquals(storedFileName, result.getFileName());
        assertEquals("test.pdf", result.getOriginalFileName());
        assertEquals(DocumentType.CONTRACT, result.getFileType());
        assertEquals("documents/" + employeeEmail + "/" + storedFileName, result.getFilePath());
        assertEquals(testFile.getSize(), result.getFileSize());
        assertNotNull(result.getId());

        verify(employeeService).employeeExists(employeeEmail);
        verify(fileStorageService).validateFile(testFile);
        verify(fileStorageService).storeFile(testFile, "documents/" + employeeEmail);
    }

    @Test
    void storeDocument_WithNonExistingEmployee_ShouldThrowException() {
        // Given
        String employeeEmail = "nonexisting@company.com";
        when(employeeService.employeeExists(employeeEmail)).thenReturn(false);

        // When & Then
        EmployeeNotFoundException exception = assertThrows(EmployeeNotFoundException.class, () -> {
            documentStorageService.storeDocument(employeeEmail, testFile, DocumentType.CONTRACT);
        });

        assertEquals("Employee not found with email: " + employeeEmail, exception.getMessage());
        verify(employeeService).employeeExists(employeeEmail);
        verifyNoInteractions(fileStorageService);
    }

    @Test
    void storeDocument_WithDifferentDocumentTypes_ShouldStoreCorrectly() {
        // Given
        String employeeEmail = "john.doe@company.com";
        when(employeeService.employeeExists(employeeEmail)).thenReturn(true);
        when(fileStorageService.storeFile(any(MultipartFile.class), anyString()))
                .thenReturn("stored_file.pdf");

        // When - użyj tylko istniejących typów dokumentów z Twojego enum
        EmployeeDocument contractDoc = documentStorageService.storeDocument(employeeEmail, testFile, DocumentType.CONTRACT);
        EmployeeDocument idCardDoc = documentStorageService.storeDocument(employeeEmail, testFile, DocumentType.ID_CARD);
        EmployeeDocument certificateDoc = documentStorageService.storeDocument(employeeEmail, testFile, DocumentType.CERTIFICATE);
        EmployeeDocument otherDoc = documentStorageService.storeDocument(employeeEmail, testFile, DocumentType.OTHER);

        // Then
        assertEquals(DocumentType.CONTRACT, contractDoc.getFileType());
        assertEquals(DocumentType.ID_CARD, idCardDoc.getFileType());
        assertEquals(DocumentType.CERTIFICATE, certificateDoc.getFileType());
        assertEquals(DocumentType.OTHER, otherDoc.getFileType());
    }

    @Test
    void getEmployeeDocuments_ShouldReturnDocumentsForSpecificEmployee() {
        // Given
        String employee1 = "john.doe@company.com";
        String employee2 = "jane.smith@company.com";

        when(employeeService.employeeExists(anyString())).thenReturn(true);
        when(fileStorageService.storeFile(any(MultipartFile.class), anyString()))
                .thenReturn("stored_file.pdf");

        // Store documents for different employees
        EmployeeDocument doc1 = documentStorageService.storeDocument(employee1, testFile, DocumentType.CONTRACT);
        EmployeeDocument doc2 = documentStorageService.storeDocument(employee1, anotherFile, DocumentType.ID_CARD);
        EmployeeDocument doc3 = documentStorageService.storeDocument(employee2, testFile, DocumentType.CONTRACT);

        // When
        List<EmployeeDocument> employee1Documents = documentStorageService.getEmployeeDocuments(employee1);
        List<EmployeeDocument> employee2Documents = documentStorageService.getEmployeeDocuments(employee2);

        // Then
        assertEquals(2, employee1Documents.size());
        assertEquals(1, employee2Documents.size());

        assertTrue(employee1Documents.stream().allMatch(doc -> doc.getEmployeeEmail().equals(employee1)));
        assertTrue(employee2Documents.stream().allMatch(doc -> doc.getEmployeeEmail().equals(employee2)));
    }

    @Test
    void getEmployeeDocuments_WithNoDocuments_ShouldReturnEmptyList() {
        // Given
        String employeeEmail = "empty@company.com";

        // When
        List<EmployeeDocument> documents = documentStorageService.getEmployeeDocuments(employeeEmail);

        // Then
        assertNotNull(documents);
        assertTrue(documents.isEmpty());
    }

    @Test
    void getEmployeeDocuments_ShouldBeCaseInsensitive() {
        // Given
        String employeeEmail = "John.Doe@Company.com";
        String lowerCaseEmail = "john.doe@company.com";

        when(employeeService.employeeExists(employeeEmail)).thenReturn(true);
        when(fileStorageService.storeFile(any(MultipartFile.class), anyString()))
                .thenReturn("stored_file.pdf");

        documentStorageService.storeDocument(employeeEmail, testFile, DocumentType.CONTRACT);

        // When
        List<EmployeeDocument> documents = documentStorageService.getEmployeeDocuments(lowerCaseEmail);

        // Then
        assertEquals(1, documents.size());
        assertEquals(employeeEmail, documents.get(0).getEmployeeEmail());
    }

    @Test
    void getDocument_WithExistingId_ShouldReturnDocument() {
        // Given
        String employeeEmail = "john.doe@company.com";
        when(employeeService.employeeExists(employeeEmail)).thenReturn(true);
        when(fileStorageService.storeFile(any(MultipartFile.class), anyString()))
                .thenReturn("stored_file.pdf");

        EmployeeDocument storedDoc = documentStorageService.storeDocument(employeeEmail, testFile, DocumentType.CONTRACT);

        // When
        EmployeeDocument retrievedDoc = documentStorageService.getDocument(storedDoc.getId());

        // Then
        assertNotNull(retrievedDoc);
        assertEquals(storedDoc.getId(), retrievedDoc.getId());
        assertEquals(storedDoc.getEmployeeEmail(), retrievedDoc.getEmployeeEmail());
        assertEquals(storedDoc.getFileName(), retrievedDoc.getFileName());
    }

    @Test
    void getDocument_WithNonExistingId_ShouldThrowException() {
        // Given
        String nonExistingId = "non-existing-id";

        // When & Then
        FileNotFoundException exception = assertThrows(FileNotFoundException.class, () -> {
            documentStorageService.getDocument(nonExistingId);
        });

        assertEquals("Document not found with ID: " + nonExistingId, exception.getMessage());
    }


    @Test
    void deleteDocument_WithExistingId_ShouldDeleteDocument() {
        // Given
        String employeeEmail = "john.doe@company.com";
        when(employeeService.employeeExists(employeeEmail)).thenReturn(true);
        when(fileStorageService.storeFile(any(MultipartFile.class), anyString()))
                .thenReturn("stored_file.pdf");

        EmployeeDocument storedDoc = documentStorageService.storeDocument(employeeEmail, testFile, DocumentType.CONTRACT);
        String documentId = storedDoc.getId();

        // When
        documentStorageService.deleteDocument(documentId);

        // Then
        FileNotFoundException exception = assertThrows(FileNotFoundException.class, () -> {
            documentStorageService.getDocument(documentId);
        });

        verify(fileStorageService).deleteFile(storedDoc.getFileName(), "documents/" + employeeEmail);
    }

    @Test
    void deleteDocument_WithNonExistingId_ShouldDoNothing() {
        // Given
        String nonExistingId = "non-existing-id";

        // When
        documentStorageService.deleteDocument(nonExistingId);

        // Then - No exception should be thrown
        assertDoesNotThrow(() -> documentStorageService.deleteDocument(nonExistingId));
        verifyNoInteractions(fileStorageService);
    }


    @Test
    void deleteDocument_ShouldDeleteFileFromStorage() {
        // Given
        String employeeEmail = "john.doe@company.com";
        String storedFileName = "uuid_test.pdf";

        when(employeeService.employeeExists(employeeEmail)).thenReturn(true);
        when(fileStorageService.storeFile(any(MultipartFile.class), eq("documents/" + employeeEmail)))
                .thenReturn(storedFileName);

        EmployeeDocument storedDoc = documentStorageService.storeDocument(employeeEmail, testFile, DocumentType.CONTRACT);

        // When
        documentStorageService.deleteDocument(storedDoc.getId());

        // Then
        verify(fileStorageService).deleteFile(storedFileName, "documents/" + employeeEmail);
    }

    @Test
    void documentExists_WithExistingId_ShouldReturnTrue() {
        // Given
        String employeeEmail = "john.doe@company.com";
        when(employeeService.employeeExists(employeeEmail)).thenReturn(true);
        when(fileStorageService.storeFile(any(MultipartFile.class), anyString()))
                .thenReturn("stored_file.pdf");

        EmployeeDocument storedDoc = documentStorageService.storeDocument(employeeEmail, testFile, DocumentType.CONTRACT);

        // When
        boolean exists = documentStorageService.documentExists(storedDoc.getId());

        // Then
        assertTrue(exists);
    }

    @Test
    void documentExists_WithNonExistingId_ShouldReturnFalse() {
        // Given
        String nonExistingId = "non-existing-id";

        // When
        boolean exists = documentStorageService.documentExists(nonExistingId);

        // Then
        assertFalse(exists);
    }


    @Test
    void storeDocument_MultipleDocuments_ShouldHaveUniqueIds() {
        // Given
        String employeeEmail = "john.doe@company.com";
        when(employeeService.employeeExists(employeeEmail)).thenReturn(true);
        when(fileStorageService.storeFile(any(MultipartFile.class), anyString()))
                .thenReturn("stored_file.pdf");

        // When
        EmployeeDocument doc1 = documentStorageService.storeDocument(employeeEmail, testFile, DocumentType.CONTRACT);
        EmployeeDocument doc2 = documentStorageService.storeDocument(employeeEmail, anotherFile, DocumentType.ID_CARD);
        EmployeeDocument doc3 = documentStorageService.storeDocument(employeeEmail, testFile, DocumentType.CERTIFICATE);

        // Then
        assertNotEquals(doc1.getId(), doc2.getId());
        assertNotEquals(doc1.getId(), doc3.getId());
        assertNotEquals(doc2.getId(), doc3.getId());
    }

    @Test
    void storeDocument_ShouldCallFileValidation() {
        // Given
        String employeeEmail = "john.doe@company.com";
        when(employeeService.employeeExists(employeeEmail)).thenReturn(true);
        when(fileStorageService.storeFile(any(MultipartFile.class), anyString()))
                .thenReturn("stored_file.pdf");

        // When
        documentStorageService.storeDocument(employeeEmail, testFile, DocumentType.CONTRACT);

        // Then
        verify(fileStorageService).validateFile(testFile);
    }

    @Test
    void storeDocument_ShouldHandleAllDocumentTypes() {
        // Given
        String employeeEmail = "john.doe@company.com";
        when(employeeService.employeeExists(employeeEmail)).thenReturn(true);
        when(fileStorageService.storeFile(any(MultipartFile.class), anyString()))
                .thenReturn("stored_file.pdf");

        // When - test all available document types from your enum
        for (DocumentType type : DocumentType.values()) {
            EmployeeDocument doc = documentStorageService.storeDocument(employeeEmail, testFile, type);
            assertEquals(type, doc.getFileType());
        }

        // Then
        verify(employeeService, times(DocumentType.values().length)).employeeExists(employeeEmail);
        verify(fileStorageService, times(DocumentType.values().length)).validateFile(testFile);
        verify(fileStorageService, times(DocumentType.values().length)).storeFile(testFile, "documents/" + employeeEmail);
    }

    @Test
    void getEmployeeDocuments_ShouldReturnSortedByUploadDateDescending() throws InterruptedException {
        // Given
        String employeeEmail = "john.doe@company.com";
        when(employeeService.employeeExists(employeeEmail)).thenReturn(true);
        when(fileStorageService.storeFile(any(MultipartFile.class), anyString()))
                .thenReturn("stored_file.pdf");

        // Store documents with slight delay to ensure different timestamps
        EmployeeDocument doc1 = documentStorageService.storeDocument(employeeEmail, testFile, DocumentType.CONTRACT);
        Thread.sleep(10); // Small delay
        EmployeeDocument doc2 = documentStorageService.storeDocument(employeeEmail, anotherFile, DocumentType.ID_CARD);

        // When
        List<EmployeeDocument> documents = documentStorageService.getEmployeeDocuments(employeeEmail);

        // Then - should be sorted by upload date descending (newest first)
        assertEquals(2, documents.size());
        // The second document should be first because it was uploaded later
        assertEquals(doc2.getId(), documents.get(0).getId());
        assertEquals(doc1.getId(), documents.get(1).getId());
        assertTrue(documents.get(0).getUploadDate().compareTo(documents.get(1).getUploadDate()) >= 0);
    }

    @Test
    void storeDocument_ShouldHandleMultipleEmployeesCorrectly() {
        // Given
        String employee1 = "employee1@company.com";
        String employee2 = "employee2@company.com";

        when(employeeService.employeeExists(anyString())).thenReturn(true);
        when(fileStorageService.storeFile(any(MultipartFile.class), anyString()))
                .thenReturn("stored_file.pdf");

        // When
        EmployeeDocument doc1 = documentStorageService.storeDocument(employee1, testFile, DocumentType.CONTRACT);
        EmployeeDocument doc2 = documentStorageService.storeDocument(employee2, anotherFile, DocumentType.ID_CARD);
        EmployeeDocument doc3 = documentStorageService.storeDocument(employee1, testFile, DocumentType.CERTIFICATE);

        // Then
        List<EmployeeDocument> employee1Docs = documentStorageService.getEmployeeDocuments(employee1);
        List<EmployeeDocument> employee2Docs = documentStorageService.getEmployeeDocuments(employee2);

        assertEquals(2, employee1Docs.size());
        assertEquals(1, employee2Docs.size());
        assertTrue(employee1Docs.stream().allMatch(doc -> doc.getEmployeeEmail().equals(employee1)));
        assertTrue(employee2Docs.stream().allMatch(doc -> doc.getEmployeeEmail().equals(employee2)));
    }

    @Test
    void deleteDocument_ShouldNotAffectOtherDocuments() {
        // Given
        String employeeEmail = "john.doe@company.com";
        when(employeeService.employeeExists(employeeEmail)).thenReturn(true);
        when(fileStorageService.storeFile(any(MultipartFile.class), anyString()))
                .thenReturn("stored_file.pdf");

        EmployeeDocument doc1 = documentStorageService.storeDocument(employeeEmail, testFile, DocumentType.CONTRACT);
        EmployeeDocument doc2 = documentStorageService.storeDocument(employeeEmail, anotherFile, DocumentType.ID_CARD);
        EmployeeDocument doc3 = documentStorageService.storeDocument(employeeEmail, testFile, DocumentType.CERTIFICATE);

        // When
        documentStorageService.deleteDocument(doc2.getId());

        // Then
        List<EmployeeDocument> documents = documentStorageService.getEmployeeDocuments(employeeEmail);
        assertEquals(2, documents.size());
        assertTrue(documents.stream().anyMatch(doc -> doc.getId().equals(doc1.getId())));
        assertTrue(documents.stream().anyMatch(doc -> doc.getId().equals(doc3.getId())));
        assertFalse(documents.stream().anyMatch(doc -> doc.getId().equals(doc2.getId())));

        verify(fileStorageService).deleteFile(doc2.getFileName(), "documents/" + employeeEmail);
    }
}