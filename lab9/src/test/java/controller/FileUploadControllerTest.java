package com.techcorp.employee.controller;

import com.techcorp.employee.exception.InvalidFileException;
import com.techcorp.employee.model.*;
import com.techcorp.employee.service.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(FileUploadController.class)
class FileUploadControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private FileStorageService fileStorageService;

    @MockBean
    private ImportService importService;

    @MockBean
    private ReportGeneratorService reportGeneratorService;

    @MockBean
    private EmployeeDocumentService documentService;

    @MockBean
    private EmployeeService employeeService;

    private MockMultipartFile validCsvFile;
    private MockMultipartFile validXmlFile;
    private MockMultipartFile imageFile;
    private MockMultipartFile documentFile;
    private ImportSummary successSummary;
    private ImportSummary errorSummary;

    @BeforeEach
    void setUp() {
        // Przykładowe pliki testowe
        validCsvFile = new MockMultipartFile(
                "file",
                "employees.csv",
                "text/csv",
                "firstName,lastName,email,company,position,salary\nJohn,Doe,john@example.com,TechCorp,PROGRAMMER,9000.75".getBytes()
        );

        validXmlFile = new MockMultipartFile(
                "file",
                "employees.xml",
                "application/xml",
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?><employees><employee><firstName>John</firstName><lastName>Doe</lastName><email>john@example.com</email><company>TechCorp</company><position>PROGRAMMER</position><salary>9000.75</salary></employee></employees>".getBytes()
        );

        imageFile = new MockMultipartFile(
                "file",
                "photo.jpg",
                "image/jpeg",
                new byte[1024]
        );

        documentFile = new MockMultipartFile(
                "file",
                "contract.pdf",
                "application/pdf",
                "PDF content".getBytes()
        );

        // Przykładowe wyniki importu - bazujemy tylko na dostępnych polach
        successSummary = new ImportSummary();
        // Domyślny ImportSummary bez błędów

        errorSummary = new ImportSummary();
        errorSummary.addError("Line 1: Invalid email format");
    }

    // === TESTY IMPORTU CSV ===

    @Test
    void importCsv_WithValidFile_ShouldReturnSuccess() throws Exception {
        // Given
        when(importService.importCsvFile(any(MultipartFile.class))).thenReturn(successSummary);

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/files/import/csv")
                        .file(validCsvFile)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors").isEmpty());

        verify(importService, times(1)).importCsvFile(any(MultipartFile.class));
    }

    @Test
    void importCsv_WithImportErrors_ShouldReturnBadRequest() throws Exception {
        // Given
        when(importService.importCsvFile(any(MultipartFile.class))).thenReturn(errorSummary);

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/files/import/csv")
                        .file(validCsvFile)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors.length()").value(1))
                .andExpect(jsonPath("$.errors[0]").value("Line 1: Invalid email format"));

        verify(importService, times(1)).importCsvFile(any(MultipartFile.class));
    }

    @Test
    void importCsv_WithEmptyFile_ShouldHandleGracefully() throws Exception {
        // Given
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file", "empty.csv", "text/csv", new byte[0]
        );

        ImportSummary emptySummary = new ImportSummary();

        when(importService.importCsvFile(any(MultipartFile.class))).thenReturn(emptySummary);

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/files/import/csv")
                        .file(emptyFile)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors").isEmpty());

        verify(importService, times(1)).importCsvFile(any(MultipartFile.class));
    }

    // === TESTY IMPORTU XML ===

    @Test
    void importXml_WithValidFile_ShouldReturnSuccess() throws Exception {
        // Given
        when(importService.importXmlFile(any(MultipartFile.class))).thenReturn(successSummary);

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/files/import/xml")
                        .file(validXmlFile)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors").isEmpty());

        verify(importService, times(1)).importXmlFile(any(MultipartFile.class));
    }

    @Test
    void importXml_WithImportErrors_ShouldReturnBadRequest() throws Exception {
        // Given
        when(importService.importXmlFile(any(MultipartFile.class))).thenReturn(errorSummary);

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/files/import/xml")
                        .file(validXmlFile)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors.length()").value(1))
                .andExpect(jsonPath("$.errors[0]").value("Line 1: Invalid email format"));

        verify(importService, times(1)).importXmlFile(any(MultipartFile.class));
    }

    @Test
    void importXml_WithMalformedXml_ShouldReturnBadRequest() throws Exception {
        // Given
        MockMultipartFile malformedXmlFile = new MockMultipartFile(
                "file", "malformed.xml", "application/xml",
                "<invalid><xml></invalid>".getBytes()
        );

        ImportSummary xmlErrorSummary = new ImportSummary();
        xmlErrorSummary.addError("Invalid XML structure");

        when(importService.importXmlFile(any(MultipartFile.class))).thenReturn(xmlErrorSummary);

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/files/import/xml")
                        .file(malformedXmlFile)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors[0]").value("Invalid XML structure"));

        verify(importService, times(1)).importXmlFile(any(MultipartFile.class));
    }

    // === TESTY EKSPORTU RAPORTÓW ===

    @Test
    void exportCsv_WithValidRequest_ShouldReturnCsvFile() throws Exception {
        // Given
        String csvContent = "firstName,lastName,email,company,position,salary\nJohn,Doe,john@example.com,TechCorp,PROGRAMMER,9000.75";
        Resource csvResource = new ByteArrayResource(csvContent.getBytes());

        Map<String, Object> reportData = new HashMap<>();
        reportData.put("contentType", "text/csv");
        reportData.put("contentDisposition", "attachment");
        reportData.put("fileName", "employees.csv");
        reportData.put("resource", csvResource);

        when(reportGeneratorService.getCsvReportData("TechCorp")).thenReturn(reportData);

        // When & Then
        mockMvc.perform(get("/api/files/export/csv")
                        .param("company", "TechCorp"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/csv"))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"employees.csv\""))
                .andExpect(content().string(csvContent));

        verify(reportGeneratorService, times(1)).getCsvReportData("TechCorp");
    }

    @Test
    void exportCsv_WithoutCompany_ShouldReturnCsvFile() throws Exception {
        // Given
        String csvContent = "firstName,lastName,email,company,position,salary\nAll,Employees,all@example.com,AllCompanies,PROGRAMMER,9000.75";
        Resource csvResource = new ByteArrayResource(csvContent.getBytes());

        Map<String, Object> reportData = new HashMap<>();
        reportData.put("contentType", "text/csv");
        reportData.put("contentDisposition", "attachment");
        reportData.put("fileName", "employees.csv");
        reportData.put("resource", csvResource);

        when(reportGeneratorService.getCsvReportData(null)).thenReturn(reportData);

        // When & Then
        mockMvc.perform(get("/api/files/export/csv"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/csv"))
                .andExpect(content().string(csvContent));

        verify(reportGeneratorService, times(1)).getCsvReportData(null);
    }

    @Test
    void generateStatisticsReport_WithValidCompany_ShouldReturnPdf() throws Exception {
        // Given
        byte[] pdfContent = "%PDF-1.4 sample pdf content".getBytes();
        Resource pdfResource = new ByteArrayResource(pdfContent);

        Map<String, Object> reportData = new HashMap<>();
        reportData.put("contentType", MediaType.APPLICATION_PDF_VALUE);
        reportData.put("contentDisposition", "attachment");
        reportData.put("fileName", "statistics_TechCorp.pdf");
        reportData.put("resource", pdfResource);

        when(reportGeneratorService.getStatisticsReportData("TechCorp")).thenReturn(reportData);

        // When & Then
        mockMvc.perform(get("/api/files/reports/statistics/{companyName}", "TechCorp"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"statistics_TechCorp.pdf\""))
                .andExpect(content().bytes(pdfContent));

        verify(reportGeneratorService, times(1)).getStatisticsReportData("TechCorp");
    }

    // === TESTY DOKUMENTÓW PRACOWNIKÓW ===

    @Test
    void uploadDocument_WithValidData_ShouldReturnCreated() throws Exception {
        // Given
        EmployeeDocument sampleDocument = createSampleDocument();
        when(documentService.storeDocument(eq("john@example.com"), any(MultipartFile.class), eq(DocumentType.CONTRACT)))
                .thenReturn(sampleDocument);

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/files/documents/{email}", "john@example.com")
                        .file(documentFile)
                        .param("type", "CONTRACT")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.employeeEmail").value("john@example.com"))
                .andExpect(jsonPath("$.fileType").value("CONTRACT"))
                .andExpect(jsonPath("$.originalFileName").value("contract.pdf"));

        verify(documentService, times(1)).storeDocument(eq("john@example.com"), any(MultipartFile.class), eq(DocumentType.CONTRACT));
    }

    @Test
    void getEmployeeDocuments_WithValidEmail_ShouldReturnDocumentList() throws Exception {
        // Given
        EmployeeDocument sampleDocument = createSampleDocument();
        List<EmployeeDocument> documents = Arrays.asList(sampleDocument);
        when(documentService.getEmployeeDocuments("john@example.com")).thenReturn(documents);

        // When & Then
        mockMvc.perform(get("/api/files/documents/{email}", "john@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].employeeEmail").value("john@example.com"))
                .andExpect(jsonPath("$[0].fileType").value("CONTRACT"));

        verify(documentService, times(1)).getEmployeeDocuments("john@example.com");
    }

    @Test
    void downloadDocument_WithValidIds_ShouldReturnDocument() throws Exception {
        // Given
        byte[] documentContent = "PDF content".getBytes();
        Resource documentResource = new ByteArrayResource(documentContent);

        ResponseEntity<Resource> responseEntity = ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"contract.pdf\"")
                .body(documentResource);

        when(documentService.downloadDocument("john@example.com", "doc123")).thenReturn(responseEntity);

        // When & Then
        mockMvc.perform(get("/api/files/documents/{email}/{documentId}", "john@example.com", "doc123"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"contract.pdf\""))
                .andExpect(content().bytes(documentContent));

        verify(documentService, times(1)).downloadDocument("john@example.com", "doc123");
    }

    @Test
    void deleteDocument_WithValidIds_ShouldReturnNoContent() throws Exception {
        // Given
        doNothing().when(documentService).deleteDocument("john@example.com", "doc123");

        // When & Then
        mockMvc.perform(delete("/api/files/documents/{email}/{documentId}", "john@example.com", "doc123"))
                .andExpect(status().isNoContent());

        verify(documentService, times(1)).deleteDocument("john@example.com", "doc123");
    }

    // === TESTY ZDJĘĆ PRACOWNIKÓW ===

    @Test
    void uploadPhoto_WithValidPhoto_ShouldReturnSuccess() throws Exception {
        // Given
        when(employeeService.uploadEmployeePhoto(eq("john@example.com"), any(MultipartFile.class)))
                .thenReturn(ResponseEntity.ok("Photo uploaded successfully: profile_photo.jpg"));

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/files/photos/{email}", "john@example.com")
                        .file(imageFile)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(content().string("Photo uploaded successfully: profile_photo.jpg"));

        verify(employeeService, times(1)).uploadEmployeePhoto(eq("john@example.com"), any(MultipartFile.class));
    }

    @Test
    void getPhoto_WithValidEmail_ShouldReturnPhoto() throws Exception {
        // Given
        byte[] photoContent = new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF}; // JPEG header
        Resource photoResource = new ByteArrayResource(photoContent);

        ResponseEntity<Resource> responseEntity = ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .body(photoResource);

        when(employeeService.getEmployeePhoto("john@example.com")).thenReturn(responseEntity);

        // When & Then
        mockMvc.perform(get("/api/files/photos/{email}", "john@example.com"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_JPEG))
                .andExpect(content().bytes(photoContent));

        verify(employeeService, times(1)).getEmployeePhoto("john@example.com");
    }

    @Test
    void deletePhoto_WithValidEmail_ShouldReturnNoContent() throws Exception {
        // Given
        doNothing().when(employeeService).deleteEmployeePhoto("john@example.com");

        // When & Then
        mockMvc.perform(delete("/api/files/photos/{email}", "john@example.com"))
                .andExpect(status().isNoContent());

        verify(employeeService, times(1)).deleteEmployeePhoto("john@example.com");
    }

    // === TESTY BŁĘDÓW I WYJĄTKÓW ===

    @Test
    void uploadPhoto_WithFileTooLarge_ShouldReturnBadRequest() throws Exception {
        // Given
        when(employeeService.uploadEmployeePhoto(eq("john@example.com"), any(MultipartFile.class)))
                .thenThrow(new InvalidFileException("File size exceeds maximum allowed size: 2097152 bytes"));

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/files/photos/{email}", "john@example.com")
                        .file(imageFile)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest());

        verify(employeeService, times(1)).uploadEmployeePhoto(eq("john@example.com"), any(MultipartFile.class));
    }

    @Test
    void getPhoto_WithNonExistingPhoto_ShouldReturnNotFound() throws Exception {
        // Given
        when(employeeService.getEmployeePhoto("nonexistent@example.com"))
                .thenReturn(ResponseEntity.notFound().build());

        // When & Then
        mockMvc.perform(get("/api/files/photos/{email}", "nonexistent@example.com"))
                .andExpect(status().isNotFound());

        verify(employeeService, times(1)).getEmployeePhoto("nonexistent@example.com");
    }

    @Test
    void uploadDocument_WithInvalidDocumentType_ShouldReturnBadRequest() throws Exception {
        // When & Then - Próba użycia nieprawidłowego typu dokumentu
        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/files/documents/{email}", "john@example.com")
                        .file(documentFile)
                        .param("type", "INVALID_TYPE")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest());
    }

    @Test
    void exportCsv_WithServiceException_ShouldPropagateException() throws Exception {
        // Given
        when(reportGeneratorService.getCsvReportData(anyString()))
                .thenThrow(new RuntimeException("Report generation failed"));

        // When & Then
        mockMvc.perform(get("/api/files/export/csv")
                        .param("company", "TechCorp"))
                .andExpect(status().is5xxServerError());

        verify(reportGeneratorService, times(1)).getCsvReportData("TechCorp");
    }

    @Test
    void getAllEndpoints_WithInvalidPaths_ShouldReturnNotFound() throws Exception {
        // Test nieistniejących endpointów
        mockMvc.perform(get("/api/files/nonexistent"))
                .andExpect(status().isNotFound());

        mockMvc.perform(post("/api/files/invalid/endpoint"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getEmployeeDocuments_WithNoDocuments_ShouldReturnEmptyList() throws Exception {
        // Given
        when(documentService.getEmployeeDocuments("empty@example.com")).thenReturn(Arrays.asList());

        // When & Then
        mockMvc.perform(get("/api/files/documents/{email}", "empty@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(documentService, times(1)).getEmployeeDocuments("empty@example.com");
    }

    @Test
    void importCsv_WithServiceException_ShouldHandleGracefully() throws Exception {
        // Given
        when(importService.importCsvFile(any(MultipartFile.class)))
                .thenThrow(new RuntimeException("Service unavailable"));

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/files/import/csv")
                        .file(validCsvFile)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().is5xxServerError());

        verify(importService, times(1)).importCsvFile(any(MultipartFile.class));
    }

    @Test
    void importCsv_WithMultipleErrors_ShouldReturnAllErrors() throws Exception {
        // Given
        ImportSummary multipleErrorsSummary = new ImportSummary();
        multipleErrorsSummary.addError("Line 1: Invalid email format");
        multipleErrorsSummary.addError("Line 2: Missing required field");
        multipleErrorsSummary.addError("Line 3: Invalid position");

        when(importService.importCsvFile(any(MultipartFile.class))).thenReturn(multipleErrorsSummary);

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/files/import/csv")
                        .file(validCsvFile)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.length()").value(3));

        verify(importService, times(1)).importCsvFile(any(MultipartFile.class));
    }

    @Test
    void importXml_WithServiceException_ShouldHandleGracefully() throws Exception {
        // Given
        when(importService.importXmlFile(any(MultipartFile.class)))
                .thenThrow(new RuntimeException("XML parsing error"));

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/files/import/xml")
                        .file(validXmlFile)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().is5xxServerError());

        verify(importService, times(1)).importXmlFile(any(MultipartFile.class));
    }

    // === METODY POMOCNICZE ===

    private EmployeeDocument createSampleDocument() {
        EmployeeDocument document = new EmployeeDocument();
        document.setEmployeeEmail("john@example.com");
        document.setFileName("contract_123.pdf");
        document.setOriginalFileName("contract.pdf");
        document.setFileType(DocumentType.CONTRACT);
        document.setFileSize(2048L);
        return document;
    }



    // === TESTY DOKUMENTÓW DZIAŁÓW ===

    @Test
    void uploadDepartmentDocument_WithValidFile_ShouldReturnCreated() throws Exception {
        // Given
        when(fileStorageService.storeDepartmentDocument(any(MultipartFile.class), eq(1L)))
                .thenReturn("department_document.pdf");

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/files/department-documents/{departmentId}", 1L)
                        .file(documentFile)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated())
                .andExpect(content().string("Document uploaded successfully: department_document.pdf"));

        verify(fileStorageService, times(1)).storeDepartmentDocument(any(MultipartFile.class), eq(1L));
    }

    @Test
    void uploadDepartmentDocument_WithStorageException_ShouldReturnBadRequest() throws Exception {
        // Given
        when(fileStorageService.storeDepartmentDocument(any(MultipartFile.class), eq(1L)))
                .thenThrow(new RuntimeException("Storage error"));

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/files/department-documents/{departmentId}", 1L)
                        .file(documentFile)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Error uploading document: Storage error"));

        verify(fileStorageService, times(1)).storeDepartmentDocument(any(MultipartFile.class), eq(1L));
    }

    @Test
    void getDepartmentDocuments_WithValidDepartmentId_ShouldReturnDocumentList() throws Exception {
        // Given
        List<String> documentNames = Arrays.asList("doc1.pdf", "doc2.docx", "doc3.pdf");
        when(fileStorageService.getDepartmentDocumentNames(1L)).thenReturn(documentNames);

        // When & Then
        mockMvc.perform(get("/api/files/department-documents/{departmentId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0]").value("doc1.pdf"))
                .andExpect(jsonPath("$[1]").value("doc2.docx"))
                .andExpect(jsonPath("$[2]").value("doc3.pdf"));

        verify(fileStorageService, times(1)).getDepartmentDocumentNames(1L);
    }

    @Test
    void getDepartmentDocuments_WithNoDocuments_ShouldReturnEmptyList() throws Exception {
        // Given
        when(fileStorageService.getDepartmentDocumentNames(2L)).thenReturn(Arrays.asList());

        // When & Then
        mockMvc.perform(get("/api/files/department-documents/{departmentId}", 2L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(fileStorageService, times(1)).getDepartmentDocumentNames(2L);
    }

    @Test
    void getDepartmentDocuments_WithServiceException_ShouldReturnBadRequest() throws Exception {
        // Given
        when(fileStorageService.getDepartmentDocumentNames(3L))
                .thenThrow(new RuntimeException("Department not found"));

        // When & Then
        mockMvc.perform(get("/api/files/department-documents/{departmentId}", 3L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());

        verify(fileStorageService, times(1)).getDepartmentDocumentNames(3L);
    }

    @Test
    void downloadDepartmentDocument_WithValidData_ShouldReturnDocument() throws Exception {
        // Given
        byte[] documentContent = "Department document content".getBytes();
        Resource documentResource = new ByteArrayResource(documentContent);

        when(fileStorageService.loadDepartmentDocument("department_doc.pdf", 1L))
                .thenReturn(documentResource);

        // When & Then
        mockMvc.perform(get("/api/files/department-documents/{departmentId}/{fileName}", 1L, "department_doc.pdf"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"department_doc.pdf\""))
                .andExpect(content().bytes(documentContent));

        verify(fileStorageService, times(1)).loadDepartmentDocument("department_doc.pdf", 1L);
    }

    @Test
    void downloadDepartmentDocument_WithNonExistingDocument_ShouldReturnNotFound() throws Exception {
        // Given
        when(fileStorageService.loadDepartmentDocument("nonexistent.pdf", 1L))
                .thenThrow(new RuntimeException("Document not found"));

        // When & Then
        mockMvc.perform(get("/api/files/department-documents/{departmentId}/{fileName}", 1L, "nonexistent.pdf"))
                .andExpect(status().isNotFound());

        verify(fileStorageService, times(1)).loadDepartmentDocument("nonexistent.pdf", 1L);
    }

    @Test
    void deleteDepartmentDocument_WithValidData_ShouldReturnNoContent() throws Exception {
        // Given
        when(fileStorageService.deleteDepartmentDocument("document_to_delete.pdf", 1L))
                .thenReturn(true);

        // When & Then
        mockMvc.perform(delete("/api/files/department-documents/{departmentId}/{fileName}", 1L, "document_to_delete.pdf"))
                .andExpect(status().isNoContent());

        verify(fileStorageService, times(1)).deleteDepartmentDocument("document_to_delete.pdf", 1L);
    }

    @Test
    void deleteDepartmentDocument_WithNonExistingDocument_ShouldReturnNotFound() throws Exception {
        // Given
        when(fileStorageService.deleteDepartmentDocument("nonexistent.pdf", 1L))
                .thenReturn(false);

        // When & Then
        mockMvc.perform(delete("/api/files/department-documents/{departmentId}/{fileName}", 1L, "nonexistent.pdf"))
                .andExpect(status().isNotFound());

        verify(fileStorageService, times(1)).deleteDepartmentDocument("nonexistent.pdf", 1L);
    }

    @Test
    void deleteDepartmentDocument_WithServiceException_ShouldReturnBadRequest() throws Exception {
        // Given
        when(fileStorageService.deleteDepartmentDocument("problematic.pdf", 1L))
                .thenThrow(new RuntimeException("Delete operation failed"));

        // When & Then
        mockMvc.perform(delete("/api/files/department-documents/{departmentId}/{fileName}", 1L, "problematic.pdf"))
                .andExpect(status().isBadRequest());

        verify(fileStorageService, times(1)).deleteDepartmentDocument("problematic.pdf", 1L);
    }


    @Test
    void exportCsv_WithEmptyCompany_ShouldHandleCorrectly() throws Exception {
        // Given
        String csvContent = "firstName,lastName,email,company,position,salary";
        Resource csvResource = new ByteArrayResource(csvContent.getBytes());

        Map<String, Object> reportData = new HashMap<>();
        reportData.put("contentType", "text/csv");
        reportData.put("contentDisposition", "attachment");
        reportData.put("fileName", "employees.csv");
        reportData.put("resource", csvResource);

        when(reportGeneratorService.getCsvReportData("")).thenReturn(reportData);

        // When & Then
        mockMvc.perform(get("/api/files/export/csv")
                        .param("company", ""))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/csv"));

        verify(reportGeneratorService, times(1)).getCsvReportData("");
    }

    @Test
    void generateStatisticsReport_WithSpecialCharactersInCompanyName_ShouldHandleCorrectly() throws Exception {
        // Given
        String companyName = "Tech-Corp & Partners";
        byte[] pdfContent = "%PDF-1.4".getBytes();
        Resource pdfResource = new ByteArrayResource(pdfContent);

        Map<String, Object> reportData = new HashMap<>();
        reportData.put("contentType", MediaType.APPLICATION_PDF_VALUE);
        reportData.put("contentDisposition", "attachment");
        reportData.put("fileName", "statistics_Tech-Corp_&_Partners.pdf");
        reportData.put("resource", pdfResource);

        when(reportGeneratorService.getStatisticsReportData(companyName)).thenReturn(reportData);

        // When & Then
        mockMvc.perform(get("/api/files/reports/statistics/{companyName}", companyName))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF));

        verify(reportGeneratorService, times(1)).getStatisticsReportData(companyName);
    }

    @Test
    void uploadDocument_WithDifferentDocumentTypes_ShouldHandleAllTypes() throws Exception {
        // Given
        EmployeeDocument contractDoc = createSampleDocument();
        contractDoc.setFileType(DocumentType.CONTRACT);

        when(documentService.storeDocument(eq("john@example.com"), any(MultipartFile.class), eq(DocumentType.CONTRACT)))
                .thenReturn(contractDoc);

        // Test all document types
        for (DocumentType type : DocumentType.values()) {
            EmployeeDocument doc = createSampleDocument();
            doc.setFileType(type);

            when(documentService.storeDocument(eq("john@example.com"), any(MultipartFile.class), eq(type)))
                    .thenReturn(doc);

            // When & Then
            mockMvc.perform(MockMvcRequestBuilders.multipart("/api/files/documents/{email}", "john@example.com")
                            .file(documentFile)
                            .param("type", type.name())
                            .contentType(MediaType.MULTIPART_FORM_DATA))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.fileType").value(type.name()));

            verify(documentService, times(1)).storeDocument(eq("john@example.com"), any(MultipartFile.class), eq(type));
        }
    }

    @Test
    void downloadDocument_WithInvalidDocumentId_ShouldReturnNotFound() throws Exception {
        // Given
        when(documentService.downloadDocument("john@example.com", "invalid-id"))
                .thenReturn(ResponseEntity.notFound().build());

        // When & Then
        mockMvc.perform(get("/api/files/documents/{email}/{documentId}", "john@example.com", "invalid-id"))
                .andExpect(status().isNotFound());

        verify(documentService, times(1)).downloadDocument("john@example.com", "invalid-id");
    }

    @Test
    void deleteDocument_WithNonExistingDocument_ShouldHandleGracefully() throws Exception {
        // Given
        doThrow(new RuntimeException("Document not found"))
                .when(documentService).deleteDocument("john@example.com", "nonexistent");

        // When & Then
        mockMvc.perform(delete("/api/files/documents/{email}/{documentId}", "john@example.com", "nonexistent"))
                .andExpect(status().is5xxServerError());

        verify(documentService, times(1)).deleteDocument("john@example.com", "nonexistent");
    }
}