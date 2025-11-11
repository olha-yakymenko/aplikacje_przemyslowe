package com.techcorp.employee.service;

import com.techcorp.employee.model.EmployeeDocument;
import com.techcorp.employee.model.DocumentType;
import com.techcorp.employee.exception.EmployeeNotFoundException;
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
class EmployeeDocumentServiceTest {

    @Mock
    private FileStorageService fileStorageService;

    @Mock
    private EmployeeService employeeService;

    private EmployeeDocumentService documentService;
    private MultipartFile validDocument;

    @BeforeEach
    void setUp() {
        documentService = new EmployeeDocumentService(fileStorageService, employeeService);

        validDocument = new MockMultipartFile(
                "file", "document.pdf", "application/pdf", new byte[2048]
        );
    }

    @Test
    void storeDocument_WithValidData_ShouldStoreDocument() throws Exception {
        // Given
        String email = "test@company.com";
        String storedFileName = "uuid_document.pdf";

        when(employeeService.employeeExists(email)).thenReturn(true);
        when(fileStorageService.storeFile(any(), anyString())).thenReturn(storedFileName);

        // When
        EmployeeDocument result = documentService.storeDocument(
                email, validDocument, DocumentType.CONTRACT);

        // Then
        assertNotNull(result);
        assertEquals(email, result.getEmployeeEmail());
        assertEquals(storedFileName, result.getFileName());
        assertEquals(DocumentType.CONTRACT, result.getFileType());
        verify(fileStorageService).storeFile(any(), eq("documents/" + email));
    }

    @Test
    void storeDocument_WithNonExistingEmployee_ShouldThrowException() throws Exception {
        // Given
        String email = "nonexisting@company.com";
        when(employeeService.employeeExists(email)).thenReturn(false);

        // When & Then
        assertThrows(EmployeeNotFoundException.class, () -> {
            documentService.storeDocument(email, validDocument, DocumentType.CONTRACT);
        });
    }

    @Test
    void getEmployeeDocuments_ShouldReturnDocumentsForEmployee() throws Exception {
        // Given
        String email1 = "test1@company.com";
        String email2 = "test2@company.com";

        // Store documents for different employees
        when(employeeService.employeeExists(anyString())).thenReturn(true);
        when(fileStorageService.storeFile(any(), anyString())).thenReturn("file.pdf");

        documentService.storeDocument(email1, validDocument, DocumentType.CONTRACT);
        documentService.storeDocument(email2, validDocument, DocumentType.CONTRACT);

        // When
        List<EmployeeDocument> documents = documentService.getEmployeeDocuments(email1);

        // Then
        assertEquals(1, documents.size());
        assertTrue(documents.stream().allMatch(doc -> doc.getEmployeeEmail().equals(email1)));
    }

    @Test
    void getDocument_WithExistingId_ShouldReturnDocument() throws Exception {
        // Given
        String email = "test@company.com";
        when(employeeService.employeeExists(email)).thenReturn(true);
        when(fileStorageService.storeFile(any(), anyString())).thenReturn("file.pdf");

        EmployeeDocument storedDoc = documentService.storeDocument(
                email, validDocument, DocumentType.CONTRACT);

        // When
        EmployeeDocument retrievedDoc = documentService.getDocument(storedDoc.getId());

        // Then
        assertNotNull(retrievedDoc);
        assertEquals(storedDoc.getId(), retrievedDoc.getId());
    }

    @Test
    void getDocument_WithNonExistingId_ShouldThrowException() throws Exception {
        // When & Then
        assertThrows(com.techcorp.employee.exception.FileNotFoundException.class, () -> {
            documentService.getDocument("nonexistent-id");
        });
    }

    @Test
    void deleteDocument_WithExistingId_ShouldDeleteDocument() throws Exception {
        // Given
        String email = "test@company.com";
        when(employeeService.employeeExists(email)).thenReturn(true);
        when(fileStorageService.storeFile(any(), anyString())).thenReturn("file.pdf");

        EmployeeDocument storedDoc = documentService.storeDocument(
                email, validDocument, DocumentType.CONTRACT);

        // When
        documentService.deleteDocument(storedDoc.getId());

        // Then
        assertThrows(com.techcorp.employee.exception.FileNotFoundException.class, () -> {
            documentService.getDocument(storedDoc.getId());
        });
        verify(fileStorageService).deleteFile("file.pdf", "documents/" + email);
    }
}