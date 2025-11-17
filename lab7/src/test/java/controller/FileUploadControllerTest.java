//
//
//
//
//
//package com.techcorp.employee.controller;
//
//import com.techcorp.employee.exception.InvalidFileException;
//import com.techcorp.employee.model.*;
//import com.techcorp.employee.service.*;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.core.io.ByteArrayResource;
//import org.springframework.core.io.Resource;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.MediaType;
//import org.springframework.http.ResponseEntity;
//import org.springframework.mock.web.MockMultipartFile;
//import org.springframework.test.context.junit.jupiter.SpringExtension;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.util.Arrays;
//import java.util.List;
//
//import static org.mockito.ArgumentMatchers.*;
//import static org.mockito.Mockito.*;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
//@ExtendWith(SpringExtension.class)
//@WebMvcTest(FileUploadController.class)
//class FileUploadControllerTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @Autowired
//    private ObjectMapper objectMapper;
//
//    @MockBean
//    private FileStorageService fileStorageService;
//
//    @MockBean
//    private ImportService importService;
//
//    @MockBean
//    private ReportGeneratorService reportGeneratorService;
//
//    @MockBean
//    private EmployeeDocumentService documentService;
//
//    @MockBean
//    private EmployeeService employeeService;
//
//    private MockMultipartFile validCsvFile;
//    private MockMultipartFile validXmlFile;
//    private MockMultipartFile imageFile;
//    private MockMultipartFile documentFile;
//    private ImportSummary successSummary;
//    private ImportSummary errorSummary;
//    private EmployeeDocument sampleDocument;
//
//    @BeforeEach
//    void setUp() {
//        // Przykładowe pliki testowe
//        validCsvFile = new MockMultipartFile(
//                "file",
//                "employees.csv",
//                "text/csv",
//                "name,email,position\nJohn Doe,john@example.com,Developer".getBytes()
//        );
//
//        validXmlFile = new MockMultipartFile(
//                "file",
//                "employees.xml",
//                "application/xml",
//                "<?xml version=\"1.0\"?><employees><employee><name>John Doe</name></employee></employees>".getBytes()
//        );
//
//        imageFile = new MockMultipartFile(
//                "file",
//                "photo.jpg",
//                "image/jpeg",
//                new byte[1024]
//        );
//
//        documentFile = new MockMultipartFile(
//                "file",
//                "contract.pdf",
//                "application/pdf",
//                "PDF content".getBytes()
//        );
//
//        // Przykładowe wyniki importu - używamy właściwych metod
//        successSummary = new ImportSummary();
//        // Zakładając, że ImportSummary ma metody addProcessedRecord() lub podobne
//        // Jeśli nie ma setterów, używamy konstruktora lub dostępnych metod
//
//        errorSummary = new ImportSummary();
//        errorSummary.addError("Line 3: Invalid email format");
//        errorSummary.addError("Line 7: Missing required field");
//
//        // Przykładowy dokument
//        sampleDocument = new EmployeeDocument();
//        sampleDocument.setId("doc123");
//        sampleDocument.setEmployeeEmail("john@example.com");
//        sampleDocument.setFileName("contract_123.pdf");
//        sampleDocument.setOriginalFileName("contract.pdf");
//        sampleDocument.setFileType(DocumentType.CONTRACT);
//        sampleDocument.setFileSize(2048L);
//    }
//
//    // === TESTY IMPORTU CSV ===
//
//    @Test
//    void importCsv_WithValidFile_ShouldReturnSuccess() throws Exception {
//        // Given
//        when(importService.importCsvFile(any(MockMultipartFile.class))).thenReturn(successSummary);
//
//        // When & Then
//        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/files/import/csv")
//                        .file(validCsvFile)
//                        .contentType(MediaType.MULTIPART_FORM_DATA))
//                .andExpect(status().isOk());
//
//        verify(importService, times(1)).importCsvFile(any(MockMultipartFile.class));
//    }
//
//    // === TESTY IMPORTU XML ===
//
//    @Test
//    void importXml_WithValidFile_ShouldReturnSuccess() throws Exception {
//        // Given
//        when(importService.importXmlFile(any(MockMultipartFile.class))).thenReturn(successSummary);
//
//        // When & Then
//        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/files/import/xml")
//                        .file(validXmlFile)
//                        .contentType(MediaType.MULTIPART_FORM_DATA))
//                .andExpect(status().isOk());
//
//        verify(importService, times(1)).importXmlFile(any(MockMultipartFile.class));
//    }
//
////    @Test
////    void importXml_WithImportErrors_ShouldReturnBadRequest() throws Exception {
////        // Given
////        when(importService.importXmlFile(any(MockMultipartFile.class))).thenReturn(errorSummary);
////
////        // When & Then
////        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/files/import/xml")
////                        .file(validXmlFile)
////                        .contentType(MediaType.MULTIPART_FORM_DATA))
////                .andExpect(status().isBadRequest())
////                .andExpect(jsonPath("$.hasErrors").value(true));
////
////        verify(importService, times(1)).importXmlFile(any(MockMultipartFile.class));
////    }
//
//    // === TESTY EKSPORTU RAPORTÓW ===
//
//    @Test
//    void exportCsv_WithValidRequest_ShouldReturnCsvFile() throws Exception {
//        // Given
//        String csvContent = "name,email,position\nJohn Doe,john@example.com,Developer";
//        Resource csvResource = new ByteArrayResource(csvContent.getBytes());
//
//        ResponseEntity<Resource> responseEntity = ResponseEntity.ok()
//                .contentType(MediaType.parseMediaType("text/csv"))
//                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"employees.csv\"")
//                .body(csvResource);
//
//        when(reportGeneratorService.exportCsvReport(anyString())).thenReturn(responseEntity);
//
//        // When & Then
//        mockMvc.perform(get("/api/files/export/csv")
//                        .param("company", "TechCorp"))
//                .andExpect(status().isOk())
//                .andExpect(content().contentTypeCompatibleWith("text/csv"))
//                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"employees.csv\""))
//                .andExpect(content().string(csvContent));
//
//        verify(reportGeneratorService, times(1)).exportCsvReport("TechCorp");
//    }
//
//    @Test
//    void exportCsv_WithoutCompany_ShouldReturnCsvFile() throws Exception {
//        // Given
//        String csvContent = "name,email,position\nAll employees data";
//        Resource csvResource = new ByteArrayResource(csvContent.getBytes());
//
//        ResponseEntity<Resource> responseEntity = ResponseEntity.ok()
//                .contentType(MediaType.parseMediaType("text/csv"))
//                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"employees.csv\"")
//                .body(csvResource);
//
//        when(reportGeneratorService.exportCsvReport(isNull())).thenReturn(responseEntity);
//
//        // When & Then
//        mockMvc.perform(get("/api/files/export/csv"))
//                .andExpect(status().isOk())
//                .andExpect(content().contentTypeCompatibleWith("text/csv"))
//                .andExpect(content().string(csvContent));
//
//        verify(reportGeneratorService, times(1)).exportCsvReport(null);
//    }
//
//    @Test
//    void generateStatisticsReport_WithValidCompany_ShouldReturnPdf() throws Exception {
//        // Given
//        byte[] pdfContent = "%PDF-1.4 sample pdf content".getBytes();
//        Resource pdfResource = new ByteArrayResource(pdfContent);
//
//        ResponseEntity<Resource> responseEntity = ResponseEntity.ok()
//                .contentType(MediaType.APPLICATION_PDF)
//                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"statistics_TechCorp.pdf\"")
//                .body(pdfResource);
//
//        when(reportGeneratorService.exportStatisticsReport("TechCorp")).thenReturn(responseEntity);
//
//        // When & Then
//        mockMvc.perform(get("/api/files/reports/statistics/{companyName}", "TechCorp"))
//                .andExpect(status().isOk())
//                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
//                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"statistics_TechCorp.pdf\""))
//                .andExpect(content().bytes(pdfContent));
//
//        verify(reportGeneratorService, times(1)).exportStatisticsReport("TechCorp");
//    }
//
//    // === TESTY DOKUMENTÓW PRACOWNIKÓW ===
//
//    @Test
//    void uploadDocument_WithValidData_ShouldReturnCreated() throws Exception {
//        // Given
//        when(documentService.storeDocument(eq("john@example.com"), any(MockMultipartFile.class), eq(DocumentType.CONTRACT)))
//                .thenReturn(sampleDocument);
//
//        // When & Then
//        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/files/documents/{email}", "john@example.com")
//                        .file(documentFile)
//                        .param("type", "CONTRACT")
//                        .contentType(MediaType.MULTIPART_FORM_DATA))
//                .andExpect(status().isCreated())
//                .andExpect(jsonPath("$.id").value("doc123"))
//                .andExpect(jsonPath("$.employeeEmail").value("john@example.com"))
//                .andExpect(jsonPath("$.fileType").value("CONTRACT"))
//                .andExpect(jsonPath("$.originalFileName").value("contract.pdf"));
//
//        verify(documentService, times(1)).storeDocument(eq("john@example.com"), any(MockMultipartFile.class), eq(DocumentType.CONTRACT));
//    }
//
//    @Test
//    void getEmployeeDocuments_WithValidEmail_ShouldReturnDocumentList() throws Exception {
//        // Given
//        List<EmployeeDocument> documents = Arrays.asList(sampleDocument);
//        when(documentService.getEmployeeDocuments("john@example.com")).thenReturn(documents);
//
//        // When & Then
//        mockMvc.perform(get("/api/files/documents/{email}", "john@example.com"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.length()").value(1))
//                .andExpect(jsonPath("$[0].employeeEmail").value("john@example.com"))
//                .andExpect(jsonPath("$[0].fileType").value("CONTRACT"));
//
//        verify(documentService, times(1)).getEmployeeDocuments("john@example.com");
//    }
//
//    @Test
//    void downloadDocument_WithValidIds_ShouldReturnDocument() throws Exception {
//        // Given
//        byte[] documentContent = "PDF content".getBytes();
//        Resource documentResource = new ByteArrayResource(documentContent);
//
//        ResponseEntity<Resource> responseEntity = ResponseEntity.ok()
//                .contentType(MediaType.APPLICATION_OCTET_STREAM)
//                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"contract.pdf\"")
//                .body(documentResource);
//
//        when(documentService.downloadDocument("john@example.com", "doc123")).thenReturn(responseEntity);
//
//        // When & Then
//        mockMvc.perform(get("/api/files/documents/{email}/{documentId}", "john@example.com", "doc123"))
//                .andExpect(status().isOk())
//                .andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM))
//                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"contract.pdf\""))
//                .andExpect(content().bytes(documentContent));
//
//        verify(documentService, times(1)).downloadDocument("john@example.com", "doc123");
//    }
//
//    @Test
//    void deleteDocument_WithValidIds_ShouldReturnNoContent() throws Exception {
//        // Given
//        doNothing().when(documentService).deleteDocument("john@example.com", "doc123");
//
//        // When & Then
//        mockMvc.perform(delete("/api/files/documents/{email}/{documentId}", "john@example.com", "doc123"))
//                .andExpect(status().isNoContent());
//
//        verify(documentService, times(1)).deleteDocument("john@example.com", "doc123");
//    }
//
//    // === TESTY ZDJĘĆ PRACOWNIKÓW ===
//
//    @Test
//    void uploadPhoto_WithValidPhoto_ShouldReturnSuccess() throws Exception {
//        // Given
//        when(employeeService.uploadEmployeePhoto(eq("john@example.com"), any(MockMultipartFile.class)))
//                .thenReturn(ResponseEntity.ok("Photo uploaded successfully: profile_photo.jpg"));
//
//        // When & Then
//        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/files/photos/{email}", "john@example.com")
//                        .file(imageFile)
//                        .contentType(MediaType.MULTIPART_FORM_DATA))
//                .andExpect(status().isOk())
//                .andExpect(content().string("Photo uploaded successfully: profile_photo.jpg"));
//
//        verify(employeeService, times(1)).uploadEmployeePhoto(eq("john@example.com"), any(MockMultipartFile.class));
//    }
//
//    @Test
//    void getPhoto_WithValidEmail_ShouldReturnPhoto() throws Exception {
//        // Given
//        byte[] photoContent = new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF}; // JPEG header
//        Resource photoResource = new ByteArrayResource(photoContent);
//
//        ResponseEntity<Resource> responseEntity = ResponseEntity.ok()
//                .contentType(MediaType.IMAGE_JPEG)
//                .body(photoResource);
//
//        when(employeeService.getEmployeePhoto("john@example.com")).thenReturn(responseEntity);
//
//        // When & Then
//        mockMvc.perform(get("/api/files/photos/{email}", "john@example.com"))
//                .andExpect(status().isOk())
//                .andExpect(content().contentType(MediaType.IMAGE_JPEG))
//                .andExpect(content().bytes(photoContent));
//
//        verify(employeeService, times(1)).getEmployeePhoto("john@example.com");
//    }
//
//    @Test
//    void deletePhoto_WithValidEmail_ShouldReturnNoContent() throws Exception {
//        // Given
//        doNothing().when(employeeService).deleteEmployeePhoto("john@example.com");
//
//        // When & Then
//        mockMvc.perform(delete("/api/files/photos/{email}", "john@example.com"))
//                .andExpect(status().isNoContent());
//
//        verify(employeeService, times(1)).deleteEmployeePhoto("john@example.com");
//    }
//
//    // === TESTY BŁĘDÓW ===
//
//    @Test
//    void uploadPhoto_WithFileTooLarge_ShouldReturnFormattedError() throws Exception {
//        // Given
//        when(employeeService.uploadEmployeePhoto(eq("john@example.com"), any(MockMultipartFile.class)))
//                .thenThrow(new InvalidFileException("File size exceeds maximum allowed size: 2097152 bytes"));
//
//        // When & Then - GlobalExceptionHandler formatuje odpowiedź
//        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/files/photos/{email}", "john@example.com")
//                        .file(imageFile)
//                        .contentType(MediaType.MULTIPART_FORM_DATA))
//                .andExpect(status().isBadRequest())
//                .andExpect(jsonPath("$.message").value("File size exceeds maximum allowed size: 2097152 bytes"))
//                .andExpect(jsonPath("$.status").value(400));
//
//        verify(employeeService, times(1)).uploadEmployeePhoto(eq("john@example.com"), any(MockMultipartFile.class));
//    }
//
//    @Test
//    void getPhoto_WithNonExistingPhoto_ShouldReturnNotFound() throws Exception {
//        // Given
//        when(employeeService.getEmployeePhoto("nonexistent@example.com"))
//                .thenReturn(ResponseEntity.notFound().build());
//
//        // When & Then
//        mockMvc.perform(get("/api/files/photos/{email}", "nonexistent@example.com"))
//                .andExpect(status().isNotFound());
//
//        verify(employeeService, times(1)).getEmployeePhoto("nonexistent@example.com");
//    }
//
//    @Test
//    void uploadDocument_WithInvalidDocumentType_ShouldReturnBadRequest() throws Exception {
//        // When & Then - Próba użycia nieprawidłowego typu dokumentu
//        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/files/documents/{email}", "john@example.com")
//                        .file(documentFile)
//                        .param("type", "INVALID_TYPE")
//                        .contentType(MediaType.MULTIPART_FORM_DATA))
//                .andExpect(status().isBadRequest());
//    }
//
//    @Test
//    void importCsv_WithEmptyFile_ShouldHandleGracefully() throws Exception {
//        // Given
//        MockMultipartFile emptyFile = new MockMultipartFile(
//                "file", "empty.csv", "text/csv", new byte[0]
//        );
//
//        ImportSummary emptySummary = new ImportSummary();
//
//        when(importService.importCsvFile(any(MockMultipartFile.class))).thenReturn(emptySummary);
//
//        // When & Then
//        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/files/import/csv")
//                        .file(emptyFile)
//                        .contentType(MediaType.MULTIPART_FORM_DATA))
//                .andExpect(status().isOk());
//
//        verify(importService, times(1)).importCsvFile(any(MockMultipartFile.class));
//    }
//
//    @Test
//    void exportCsv_WithServiceException_ShouldPropagateException() throws Exception {
//        // Given
//        when(reportGeneratorService.exportCsvReport(anyString()))
//                .thenThrow(new RuntimeException("Report generation failed"));
//
//        // When & Then
//        mockMvc.perform(get("/api/files/export/csv")
//                        .param("company", "TechCorp"))
//                .andExpect(status().is5xxServerError());
//
//        verify(reportGeneratorService, times(1)).exportCsvReport("TechCorp");
//    }
//
//    @Test
//    void getAllEndpoints_WithInvalidPaths_ShouldReturnNotFound() throws Exception {
//        // Test nieistniejących endpointów
//        mockMvc.perform(get("/api/files/nonexistent"))
//                .andExpect(status().isNotFound());
//
//        mockMvc.perform(post("/api/files/invalid/endpoint"))
//                .andExpect(status().isNotFound());
//    }
//
//
//    @Test
//    void importCsv_WithFileTooLarge_ShouldReturnBadRequest() throws Exception {
//        // Given - plik większy niż 10MB
//        byte[] largeFileContent = new byte[11 * 1024 * 1024]; // 11MB
//        MockMultipartFile largeFile = new MockMultipartFile(
//                "file", "large_employees.csv", "text/csv", largeFileContent
//        );
//
//        ImportSummary errorSummary = new ImportSummary();
//        errorSummary.addError("Invalid file: File size exceeds maximum allowed size: 10485760 bytes (file: 11534336 bytes)");
//
//        when(importService.importCsvFile(any(MultipartFile.class))).thenReturn(errorSummary);
//
//        // When & Then
//        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/files/import/csv")
//                        .file(largeFile)
//                        .contentType(MediaType.MULTIPART_FORM_DATA))
//                .andExpect(status().isBadRequest())
//                .andExpect(jsonPath("$.errors[0]").value("Invalid file: File size exceeds maximum allowed size: 10485760 bytes (file: 11534336 bytes)"));
//
//        verify(importService, times(1)).importCsvFile(any(MultipartFile.class));
//    }
//}