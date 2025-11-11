//package com.techcorp.employee.service;
//
//import com.techcorp.employee.model.EmployeeDocument;
//import com.techcorp.employee.model.DocumentType;
//import com.techcorp.employee.exception.EmployeeNotFoundException;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.mock.web.MockMultipartFile;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.util.List;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.*;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//class EmployeeDocumentServiceTest {
//
//    @Mock
//    private FileStorageService fileStorageService;
//
//    @Mock
//    private EmployeeService employeeService;
//
//    private EmployeeDocumentService documentService;
//    private MultipartFile validDocument;
//
//    @BeforeEach
//    void setUp() {
//        documentService = new EmployeeDocumentService(fileStorageService, employeeService);
//
//        validDocument = new MockMultipartFile(
//                "file", "document.pdf", "application/pdf", new byte[2048]
//        );
//    }
//
//    @Test
//    void storeDocument_WithValidData_ShouldStoreDocument() throws Exception {
//        // Given
//        String email = "test@company.com";
//        String storedFileName = "uuid_document.pdf";
//
//        when(employeeService.employeeExists(email)).thenReturn(true);
//        when(fileStorageService.storeFile(any(), anyString())).thenReturn(storedFileName);
//
//        // When
//        EmployeeDocument result = documentService.storeDocument(
//                email, validDocument, DocumentType.CONTRACT);
//
//        // Then
//        assertNotNull(result);
//        assertEquals(email, result.getEmployeeEmail());
//        assertEquals(storedFileName, result.getFileName());
//        assertEquals(DocumentType.CONTRACT, result.getFileType());
//        verify(fileStorageService).storeFile(any(), eq("documents/" + email));
//    }
//
//    @Test
//    void storeDocument_WithNonExistingEmployee_ShouldThrowException() throws Exception {
//        // Given
//        String email = "nonexisting@company.com";
//        when(employeeService.employeeExists(email)).thenReturn(false);
//
//        // When & Then
//        assertThrows(EmployeeNotFoundException.class, () -> {
//            documentService.storeDocument(email, validDocument, DocumentType.CONTRACT);
//        });
//    }
//
//    @Test
//    void getEmployeeDocuments_ShouldReturnDocumentsForEmployee() throws Exception {
//        // Given
//        String email1 = "test1@company.com";
//        String email2 = "test2@company.com";
//
//        // Store documents for different employees
//        when(employeeService.employeeExists(anyString())).thenReturn(true);
//        when(fileStorageService.storeFile(any(), anyString())).thenReturn("file.pdf");
//
//        documentService.storeDocument(email1, validDocument, DocumentType.CONTRACT);
//        documentService.storeDocument(email2, validDocument, DocumentType.CONTRACT);
//
//        // When
//        List<EmployeeDocument> documents = documentService.getEmployeeDocuments(email1);
//
//        // Then
//        assertEquals(1, documents.size());
//        assertTrue(documents.stream().allMatch(doc -> doc.getEmployeeEmail().equals(email1)));
//    }
//
//    @Test
//    void getDocument_WithExistingId_ShouldReturnDocument() throws Exception {
//        // Given
//        String email = "test@company.com";
//        when(employeeService.employeeExists(email)).thenReturn(true);
//        when(fileStorageService.storeFile(any(), anyString())).thenReturn("file.pdf");
//
//        EmployeeDocument storedDoc = documentService.storeDocument(
//                email, validDocument, DocumentType.CONTRACT);
//
//        // When
//        EmployeeDocument retrievedDoc = documentService.getDocument(storedDoc.getId());
//
//        // Then
//        assertNotNull(retrievedDoc);
//        assertEquals(storedDoc.getId(), retrievedDoc.getId());
//    }
//
//    @Test
//    void getDocument_WithNonExistingId_ShouldThrowException() throws Exception {
//        // When & Then
//        assertThrows(com.techcorp.employee.exception.FileNotFoundException.class, () -> {
//            documentService.getDocument("nonexistent-id");
//        });
//    }
//
//    @Test
//    void deleteDocument_WithExistingId_ShouldDeleteDocument() throws Exception {
//        // Given
//        String email = "test@company.com";
//        when(employeeService.employeeExists(email)).thenReturn(true);
//        when(fileStorageService.storeFile(any(), anyString())).thenReturn("file.pdf");
//
//        EmployeeDocument storedDoc = documentService.storeDocument(
//                email, validDocument, DocumentType.CONTRACT);
//
//        // When
//        documentService.deleteDocument(storedDoc.getId());
//
//        // Then
//        assertThrows(com.techcorp.employee.exception.FileNotFoundException.class, () -> {
//            documentService.getDocument(storedDoc.getId());
//        });
//        verify(fileStorageService).deleteFile("file.pdf", "documents/" + email);
//    }
//}






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
    private MultipartFile validPdf;
    private MultipartFile validDoc;

    @BeforeEach
    void setUp() {
        documentService = new EmployeeDocumentService(fileStorageService, employeeService);

        validPdf = new MockMultipartFile(
                "file", "document.pdf", "application/pdf", new byte[2048]
        );

        validDoc = new MockMultipartFile(
                "file", "contract.doc", "application/msword", new byte[2048]
        );
    }

    @Test
    void storeDocument_WithValidPdf_ShouldStoreDocument() {
        // Given
        String email = "test@company.com";
        String storedFileName = "uuid_document.pdf";

        when(employeeService.employeeExists(email)).thenReturn(true);
        when(fileStorageService.storeFile(any(MultipartFile.class), eq("documents/" + email)))
                .thenReturn(storedFileName);

        // When
        EmployeeDocument result = documentService.storeDocument(
                email, validPdf, DocumentType.CONTRACT);

        // Then
        assertNotNull(result);
        assertEquals(email, result.getEmployeeEmail());
        assertEquals(storedFileName, result.getFileName());
        assertEquals("document.pdf", result.getOriginalFileName());
        assertEquals(DocumentType.CONTRACT, result.getFileType());
        assertNotNull(result.getId());
        assertNotNull(result.getUploadDate());

        verify(fileStorageService).validateFile(validPdf);
        verify(fileStorageService).validateFileType(validPdf, new String[]{".pdf", ".doc", ".docx", ".txt"});
        verify(fileStorageService).validateFileSize(validPdf, 10 * 1024 * 1024);
    }

    @Test
    void storeDocument_WithValidWordDocument_ShouldStoreDocument() {
        // Given
        String email = "test@company.com";
        String storedFileName = "uuid_contract.doc";

        when(employeeService.employeeExists(email)).thenReturn(true);
        when(fileStorageService.storeFile(any(MultipartFile.class), eq("documents/" + email)))
                .thenReturn(storedFileName);

        // When
        EmployeeDocument result = documentService.storeDocument(
                email, validDoc, DocumentType.CONTRACT);

        // Then
        assertNotNull(result);
        assertEquals(storedFileName, result.getFileName());
    }

    @Test
    void storeDocument_WithNonExistingEmployee_ShouldThrowException() {
        // Given
        String email = "nonexisting@company.com";
        when(employeeService.employeeExists(email)).thenReturn(false);

        // When & Then
        assertThrows(EmployeeNotFoundException.class, () -> {
            documentService.storeDocument(email, validPdf, DocumentType.CONTRACT);
        });

        verify(fileStorageService, never()).storeFile(any(), anyString());
    }

    @Test
    void getEmployeeDocuments_ShouldReturnOnlyDocumentsForSpecificEmployee() {
        // Given
        String email1 = "test1@company.com";
        String email2 = "test2@company.com";

        when(employeeService.employeeExists(anyString())).thenReturn(true);
        when(fileStorageService.storeFile(any(MultipartFile.class), anyString()))
                .thenReturn("file1.pdf", "file2.pdf", "file3.pdf");

        // Store documents for different employees
        documentService.storeDocument(email1, validPdf, DocumentType.CONTRACT);
        documentService.storeDocument(email2, validPdf, DocumentType.CERTIFICATE);
        documentService.storeDocument(email1, validDoc, DocumentType.ID_CARD);

        // When
        List<EmployeeDocument> documentsForEmail1 = documentService.getEmployeeDocuments(email1);
        List<EmployeeDocument> documentsForEmail2 = documentService.getEmployeeDocuments(email2);

        // Then
        assertEquals(2, documentsForEmail1.size());
        assertEquals(1, documentsForEmail2.size());

        assertTrue(documentsForEmail1.stream().allMatch(doc -> doc.getEmployeeEmail().equals(email1)));
        assertTrue(documentsForEmail2.stream().allMatch(doc -> doc.getEmployeeEmail().equals(email2)));
    }

    @Test
    void getEmployeeDocuments_ShouldReturnEmptyListForEmployeeWithNoDocuments() {
        // Given
        String email = "no-docs@company.com";

        // When
        List<EmployeeDocument> documents = documentService.getEmployeeDocuments(email);

        // Then
        assertNotNull(documents);
        assertTrue(documents.isEmpty());
    }

    @Test
    void getDocument_WithExistingId_ShouldReturnDocument() {
        // Given
        String email = "test@company.com";
        when(employeeService.employeeExists(email)).thenReturn(true);
        when(fileStorageService.storeFile(any(MultipartFile.class), anyString())).thenReturn("file.pdf");

        EmployeeDocument storedDoc = documentService.storeDocument(email, validPdf, DocumentType.CONTRACT);

        // When
        EmployeeDocument retrievedDoc = documentService.getDocument(storedDoc.getId());

        // Then
        assertNotNull(retrievedDoc);
        assertEquals(storedDoc.getId(), retrievedDoc.getId());
        assertEquals(storedDoc.getEmployeeEmail(), retrievedDoc.getEmployeeEmail());
        assertEquals(storedDoc.getFileName(), retrievedDoc.getFileName());
    }

    @Test
    void getDocument_WithNonExistingId_ShouldThrowException() {
        // When & Then
        assertThrows(com.techcorp.employee.exception.FileNotFoundException.class, () -> {
            documentService.getDocument("nonexistent-id");
        });
    }

    @Test
    void deleteDocument_WithExistingId_ShouldDeleteDocumentAndFile() {
        // Given
        String email = "test@company.com";
        when(employeeService.employeeExists(email)).thenReturn(true);
        when(fileStorageService.storeFile(any(MultipartFile.class), anyString())).thenReturn("file.pdf");

        EmployeeDocument storedDoc = documentService.storeDocument(email, validPdf, DocumentType.CONTRACT);
        String documentId = storedDoc.getId();

        // When
        documentService.deleteDocument(documentId);

        // Then - Document should be removed from storage
        assertThrows(com.techcorp.employee.exception.FileNotFoundException.class, () -> {
            documentService.getDocument(documentId);
        });

        // Verify file deletion was called
        verify(fileStorageService).deleteFile("file.pdf", "documents/" + email);
    }

    @Test
    void deleteDocument_WithNonExistingId_ShouldDoNothing() {
        // Given
        String nonExistingId = "non-existing-id";

        // When
        documentService.deleteDocument(nonExistingId);

        // Then - No exception should be thrown, no file operations
        verify(fileStorageService, never()).deleteFile(anyString(), anyString());
    }

    @Test
    void documentExists_WithExistingId_ShouldReturnTrue() {
        // Given
        String email = "test@company.com";
        when(employeeService.employeeExists(email)).thenReturn(true);
        when(fileStorageService.storeFile(any(MultipartFile.class), anyString())).thenReturn("file.pdf");

        EmployeeDocument storedDoc = documentService.storeDocument(email, validPdf, DocumentType.CONTRACT);

        // When
        boolean exists = documentService.documentExists(storedDoc.getId());

        // Then
        assertTrue(exists);
    }

    @Test
    void documentExists_WithNonExistingId_ShouldReturnFalse() {
        // When
        boolean exists = documentService.documentExists("non-existing-id");

        // Then
        assertFalse(exists);
    }


}