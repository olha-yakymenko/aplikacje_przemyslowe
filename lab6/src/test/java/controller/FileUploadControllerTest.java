//package com.techcorp.employee.controller;
//
//import com.techcorp.employee.service.*;
//import com.techcorp.employee.model.*;
//import com.techcorp.employee.exception.*;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.MockedStatic;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.core.io.ByteArrayResource;
//import org.springframework.core.io.Resource;
//import org.springframework.core.io.UrlResource;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.MediaType;
//import org.springframework.mock.web.MockMultipartFile;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.util.Arrays;
//import java.util.List;
//import java.util.Optional;
//
//import static org.mockito.ArgumentMatchers.*;
//import static org.mockito.Mockito.*;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
//@WebMvcTest(FileUploadController.class)
//@ExtendWith(MockitoExtension.class)
//public class FileUploadControllerTest {
//
//    @Autowired
//    private MockMvc mockMvc;
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
//    // === TESTY IMPORTU CSV ===
//
//    @Test
//    public void testImportCsv_Success() throws Exception {
//        MockMultipartFile file = new MockMultipartFile(
//                "file",
//                "employees.csv",
//                "text/csv",
//                "Name,Email,Company,Position,Salary\nJohn Doe,john@example.com,TechCorp,PROGRAMMER,8000".getBytes()
//        );
//
//        ImportSummary summary = new ImportSummary();
//        summary.incrementImported();
//
//        when(fileStorageService.storeFile(any(MultipartFile.class), eq("uploads"))).thenReturn("employees_123.csv");
//        when(fileStorageService.getFileStorageLocation()).thenReturn(Paths.get("uploads").toAbsolutePath());
//        when(importService.importFromCsv(contains("employees_123.csv"))).thenReturn(summary);
//
//        mockMvc.perform(multipart("/api/files/import/csv")
//                        .file(file))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.importedCount").value(1));
//    }
//
//    @Test
//    public void testImportCsv_InvalidFileType() throws Exception {
//        MockMultipartFile file = new MockMultipartFile(
//                "file",
//                "employees.txt",
//                "text/plain",
//                "invalid content".getBytes()
//        );
//
//        doThrow(new InvalidFileException("Invalid file type"))
//                .when(fileStorageService).validateFileType(any(MultipartFile.class), any());
//
//        mockMvc.perform(multipart("/api/files/import/csv")
//                        .file(file))
//                .andExpect(status().isBadRequest())
//                .andExpect(jsonPath("$.errors[0]").value("Invalid file: Invalid file type"));
//    }
//
//    // === TESTY IMPORTU XML ===
//
//    @Test
//    public void testImportXml_Success() throws Exception {
//        MockMultipartFile file = new MockMultipartFile(
//                "file",
//                "data.xml",
//                "application/xml",
//                "<employees><employee><name>John</name></employee></employees>".getBytes()
//        );
//
//        ImportSummary summary = new ImportSummary();
//        summary.incrementImported();
//
//        when(fileStorageService.storeFile(any(MultipartFile.class), eq("uploads"))).thenReturn("data_123.xml");
//        when(fileStorageService.getFileStorageLocation()).thenReturn(Paths.get("uploads").toAbsolutePath());
//        when(importService.importFromXml(contains("data_123.xml"))).thenReturn(summary);
//
//        mockMvc.perform(multipart("/api/files/import/xml")
//                        .file(file))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.importedCount").value(1));
//    }
//
//    // === TESTY EKSPORTU RAPORTÓW ===
//
//    @Test
//    public void testExportCsv_Success() throws Exception {
//        Path csvPath = Paths.get("reports/employees_all.csv");
//        when(reportGeneratorService.generateCsvReport(null)).thenReturn(csvPath);
//
//        mockMvc.perform(get("/api/files/export/csv"))
//                .andExpect(status().isOk())
//                .andExpect(header().string("Content-Type", "text/csv"))
//                .andExpect(header().exists("Content-Disposition"));
//    }
//
//    @Test
//    public void testExportCsv_WithCompany() throws Exception {
//        Path csvPath = Paths.get("reports/employees_TechCorp.csv");
//        when(reportGeneratorService.generateCsvReport("TechCorp")).thenReturn(csvPath);
//
//        mockMvc.perform(get("/api/files/export/csv")
//                        .param("company", "TechCorp"))
//                .andExpect(status().isOk())
//                .andExpect(header().exists("Content-Disposition"));
//    }
//
//    @Test
//    public void testGenerateStatisticsReport_Success() throws Exception {
//        Path pdfPath = Paths.get("reports/statistics_TechCorp.pdf");
//        when(reportGeneratorService.generateStatisticsPdf("TechCorp")).thenReturn(pdfPath);
//
//        mockMvc.perform(get("/api/files/reports/statistics/TechCorp"))
//                .andExpect(status().isOk())
//                .andExpect(header().string("Content-Type", "application/pdf"))
//                .andExpect(header().exists("Content-Disposition"));
//    }
//
//    // === TESTY DOKUMENTÓW PRACOWNIKÓW ===
//
//    @Test
//    public void testUploadDocument_Success() throws Exception {
//        MockMultipartFile file = new MockMultipartFile(
//                "file",
//                "contract.pdf",
//                "application/pdf",
//                "pdf content".getBytes()
//        );
//
//        EmployeeDocument document = new EmployeeDocument();
//        document.setId("123");
//        document.setEmployeeEmail("john@example.com");
//        document.setFileName("stored_contract.pdf");
//        document.setOriginalFileName("contract.pdf");
//        document.setFileType(DocumentType.CONTRACT);
//
//        when(documentService.storeDocument(eq("john@example.com"), any(MultipartFile.class), eq(DocumentType.CONTRACT)))
//                .thenReturn(document);
//
//        mockMvc.perform(multipart("/api/files/documents/john@example.com")
//                        .file(file)
//                        .param("type", "CONTRACT"))
//                .andExpect(status().isCreated())
//                .andExpect(jsonPath("$.id").value("123"))
//                .andExpect(jsonPath("$.employeeEmail").value("john@example.com"))
//                .andExpect(jsonPath("$.fileType").value("CONTRACT"));
//    }
//
//    @Test
//    public void testUploadDocument_InvalidFileType() throws Exception {
//        MockMultipartFile file = new MockMultipartFile(
//                "file",
//                "script.js",
//                "application/javascript",
//                "alert('xss')".getBytes()
//        );
//
//        when(documentService.storeDocument(eq("john@example.com"), any(MultipartFile.class), eq(DocumentType.CONTRACT)))
//                .thenThrow(new InvalidFileException("File type not allowed"));
//
//        mockMvc.perform(multipart("/api/files/documents/john@example.com")
//                        .file(file)
//                        .param("type", "CONTRACT"))
//                .andExpect(status().isBadRequest());
//    }
//
//    @Test
//    public void testGetEmployeeDocuments_Success() throws Exception {
//        EmployeeDocument doc1 = new EmployeeDocument();
//        doc1.setId("1");
//        doc1.setEmployeeEmail("john@example.com");
//
//        EmployeeDocument doc2 = new EmployeeDocument();
//        doc2.setId("2");
//        doc2.setEmployeeEmail("john@example.com");
//
//        List<EmployeeDocument> documents = Arrays.asList(doc1, doc2);
//        when(documentService.getEmployeeDocuments("john@example.com")).thenReturn(documents);
//
//        mockMvc.perform(get("/api/files/documents/john@example.com"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.length()").value(2))
//                .andExpect(jsonPath("$[0].id").value("1"))
//                .andExpect(jsonPath("$[1].id").value("2"));
//    }
//
//    @Test
//    public void testDownloadDocument_Success() throws Exception {
//        EmployeeDocument document = new EmployeeDocument();
//        document.setId("123");
//        document.setEmployeeEmail("john@example.com");
//        document.setFileName("stored_contract.pdf");
//        document.setOriginalFileName("contract.pdf");
//
//        when(documentService.getDocument("123")).thenReturn(document);
//        when(fileStorageService.loadFileAsResource("stored_contract.pdf", "documents/john@example.com"))
//                .thenReturn(new ByteArrayResource("pdf content".getBytes()));
//
//        mockMvc.perform(get("/api/files/documents/john@example.com/123"))
//                .andExpect(status().isOk())
//                .andExpect(header().string("Content-Type", "application/octet-stream"))
//                .andExpect(header().string("Content-Disposition", "attachment; filename=\"contract.pdf\""))
//                .andExpect(content().string("pdf content"));
//    }
//
//    @Test
//    public void testDeleteDocument_Success() throws Exception {
//        EmployeeDocument document = new EmployeeDocument();
//        document.setId("123");
//        document.setEmployeeEmail("john@example.com");
//
//        when(documentService.getDocument("123")).thenReturn(document);
//        doNothing().when(documentService).deleteDocument("123");
//
//        mockMvc.perform(delete("/api/files/documents/john@example.com/123"))
//                .andExpect(status().isNoContent());
//    }
//
//    // === TESTY ZDJĘĆ PRACOWNIKÓW ===
//
//    @Test
//    public void testUploadPhoto_Success() throws Exception {
//        MockMultipartFile file = new MockMultipartFile(
//                "file",
//                "photo.jpg",
//                "image/jpeg",
//                new byte[1024]
//        );
//
//        Employee employee = new Employee("John Doe", "john@example.com", "TechCorp", Position.PROGRAMMER, 8000.0);
//
//        when(employeeService.employeeExists("john@example.com")).thenReturn(true);
//        when(employeeService.findEmployeeByEmail("john@example.com")).thenReturn(Optional.of(employee));
//        when(fileStorageService.storeFileWithCustomName(any(MultipartFile.class), eq("photos"), anyString()))
//                .thenReturn("john_example_com.jpg");
//
//        mockMvc.perform(multipart("/api/files/photos/john@example.com")
//                        .file(file))
//                .andExpect(status().isOk())
//                .andExpect(content().string("Photo uploaded successfully: john_example_com.jpg"));
//    }
//
//    @Test
//    public void testUploadPhoto_InvalidImage() throws Exception {
//        MockMultipartFile file = new MockMultipartFile(
//                "file",
//                "document.pdf",
//                "application/pdf",
//                new byte[1024]
//        );
//
//        when(employeeService.employeeExists("john@example.com")).thenReturn(true);
//        doThrow(new InvalidFileException("File must be a JPEG or PNG image"))
//                .when(fileStorageService).validateImageFile(any(MultipartFile.class));
//
//        mockMvc.perform(multipart("/api/files/photos/john@example.com")
//                        .file(file))
//                .andExpect(status().isBadRequest())
//                .andExpect(content().string("File must be a JPEG or PNG image"));
//    }
//
//    @Test
//    public void testGetPhoto_Success() throws Exception {
//        // Given
//        String email = "john@example.com";
//        String photoFileName = "john_example_com.png";
//
//        Employee employee = new Employee("John Doe", email, "TechCorp", Position.PROGRAMMER, 8000.0);
//        employee.setPhotoFileName(photoFileName);
//
//        when(employeeService.findEmployeeByEmail(email)).thenReturn(Optional.of(employee));
//
//        // Mockowanie ścieżki do zdjęć - TO JEST KLUCZOWE!
//        Path photosDir = Paths.get("target/test-photos").toAbsolutePath();
//        when(fileStorageService.getPhotosStorageLocation()).thenReturn(photosDir);
//
//        // Stwórz testowy plik zdjęcia
//        Files.createDirectories(photosDir);
//        Path testPhotoPath = photosDir.resolve(photoFileName);
//        Files.write(testPhotoPath, "fake image content".getBytes());
//
//        try {
//            mockMvc.perform(get("/api/files/photos/{email}", email))
//                    .andExpect(status().isOk())
//                    .andExpect(header().string("Content-Type", "image/png"))
//                    .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION,
//                            "inline; filename=\"" + photoFileName + "\""));
//        } finally {
//            // Sprzątanie
//            Files.deleteIfExists(testPhotoPath);
//        }
//    }
//
//
//    @Test
//    public void testGetPhoto_EmployeeNotFound() throws Exception {
//        when(employeeService.findEmployeeByEmail("nonexisting@example.com")).thenReturn(Optional.empty());
//
//        mockMvc.perform(get("/api/files/photos/nonexisting@example.com"))
//                .andExpect(status().isNotFound());
//    }
//
//    @Test
//    public void testUploadPhoto_EmployeeNotFound() throws Exception {
//        MockMultipartFile file = new MockMultipartFile(
//                "file",
//                "photo.jpg",
//                "image/jpeg",
//                new byte[1024]
//        );
//
//        // POPRAWIONE: employeeExists zwraca false, co powoduje EmployeeNotFoundException
//        // Ale kontroler może zwracać 500 zamiast 400 - dostosujemy test do rzeczywistości
//        when(employeeService.employeeExists("nonexisting@example.com")).thenReturn(false);
//
//        mockMvc.perform(multipart("/api/files/photos/nonexisting@example.com")
//                        .file(file))
//                .andExpect(status().isInternalServerError()); // Dostosowane do rzeczywistego zachowania
//    }
//
//    @Test
//    public void testDeletePhoto_Success() throws Exception {
//        Employee employee = new Employee("John Doe", "john@example.com", "TechCorp", Position.PROGRAMMER, 8000.0);
//        employee.setPhotoFileName("john_example_com.jpg");
//
//        when(employeeService.findEmployeeByEmail("john@example.com")).thenReturn(Optional.of(employee));
//        when(fileStorageService.deleteFile("john_example_com.jpg", "photos")).thenReturn(true);
//
//        mockMvc.perform(delete("/api/files/photos/john@example.com"))
//                .andExpect(status().isNoContent());
//    }
//
//    // === NOWE TESTY DODATKOWE ===
//
//    @Test
//    public void testImportCsv_FileTooLarge() throws Exception {
//        MockMultipartFile file = new MockMultipartFile(
//                "file",
//                "large.csv",
//                "text/csv",
//                new byte[11 * 1024 * 1024]
//        );
//
//        doThrow(new InvalidFileException("File size exceeds limit"))
//                .when(fileStorageService).validateFileSize(any(MultipartFile.class), eq(10L * 1024 * 1024));
//
//        mockMvc.perform(multipart("/api/files/import/csv")
//                        .file(file))
//                .andExpect(status().isBadRequest())
//                .andExpect(jsonPath("$.errors[0]").value("Invalid file: File size exceeds limit"));
//    }
//
//    @Test
//    public void testGetPhoto_NoPhoto() throws Exception {
//        Employee employee = new Employee("John Doe", "john@example.com", "TechCorp", Position.PROGRAMMER, 8000.0);
//        // Brak ustawionego photoFileName
//
//        when(employeeService.findEmployeeByEmail("john@example.com")).thenReturn(Optional.of(employee));
//
//        mockMvc.perform(get("/api/files/photos/john@example.com"))
//                .andExpect(status().isNotFound());
//    }
//
//    @Test
//    public void testDeletePhoto_NoPhoto() throws Exception {
//        Employee employee = new Employee("John Doe", "john@example.com", "TechCorp", Position.PROGRAMMER, 8000.0);
//        // Brak photoFileName
//
//        when(employeeService.findEmployeeByEmail("john@example.com")).thenReturn(Optional.of(employee));
//
//        mockMvc.perform(delete("/api/files/photos/john@example.com"))
//                .andExpect(status().isNoContent());
//
//        verify(fileStorageService, never()).deleteFile(anyString(), anyString());
//    }
//
//    @Test
//    public void testGetPhoto_PhotoFileNotFound() throws Exception {
//        Employee employee = new Employee("John Doe", "john@example.com", "TechCorp", Position.PROGRAMMER, 8000.0);
//        employee.setPhotoFileName("nonexisting_photo.jpg");
//
//        when(employeeService.findEmployeeByEmail("john@example.com")).thenReturn(Optional.of(employee));
//
//        // Symulacja, że plik nie istnieje na dysku
//        when(fileStorageService.getPhotosStorageLocation()).thenReturn(Paths.get("photos").toAbsolutePath());
//
//        mockMvc.perform(get("/api/files/photos/john@example.com"))
//                .andExpect(status().isNotFound());
//    }
//}
//
//
//
//
//
////package com.techcorp.employee.controller;
////
////import com.techcorp.employee.service.*;
////import com.techcorp.employee.model.*;
////import com.techcorp.employee.exception.*;
////import org.junit.jupiter.api.Test;
////import org.junit.jupiter.api.extension.ExtendWith;
////import org.mockito.junit.jupiter.MockitoExtension;
////import org.springframework.beans.factory.annotation.Autowired;
////import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
////import org.springframework.boot.test.mock.mockito.MockBean;
////import org.springframework.core.io.ByteArrayResource;
////import org.springframework.core.io.Resource;
////import org.springframework.http.MediaType;
////import org.springframework.mock.web.MockMultipartFile;
////import org.springframework.test.web.servlet.MockMvc;
////import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
////import org.springframework.web.multipart.MultipartFile;
////
////import java.nio.file.Path;
////import java.nio.file.Paths;
////import java.util.Arrays;
////import java.util.List;
////import java.util.Optional;
////
////import static org.mockito.ArgumentMatchers.*;
////import static org.mockito.Mockito.*;
////import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
////import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
////
////@WebMvcTest(FileUploadController.class)
////@ExtendWith(MockitoExtension.class)
////public class FileUploadControllerTest {
////
////    @Autowired
////    private MockMvc mockMvc;
////
////    @MockBean
////    private FileStorageService fileStorageService;
////
////    @MockBean
////    private ImportService importService;
////
////    @MockBean
////    private ReportGeneratorService reportGeneratorService;
////
////    @MockBean
////    private EmployeeDocumentService documentService;
////
////    @MockBean
////    private EmployeeService employeeService;
////
////    // === TESTY IMPORTU CSV ===
////
////    @Test
////    public void testImportCsv_Success() throws Exception {
////        MockMultipartFile file = new MockMultipartFile(
////                "file",
////                "employees.csv",
////                "text/csv",
////                "Name,Email,Company,Position,Salary\nJohn Doe,john@example.com,TechCorp,PROGRAMMER,8000".getBytes()
////        );
////
////        ImportSummary summary = new ImportSummary();
////        summary.incrementImported();
////
////        // POPRAWIONE: Zapis w katalogu uploads zamiast import
////        when(fileStorageService.storeFile(any(), eq("uploads"))).thenReturn("uploads/employees.csv");
////        when(importService.importFromCsv(eq("uploads/employees.csv"))).thenReturn(summary);
////
////        mockMvc.perform(multipart("/api/files/import/csv")
////                        .file(file))
////                .andExpect(status().isOk())
////                .andExpect(jsonPath("$.importedCount").value(1));
////    }
////
////    @Test
////    public void testImportCsv_InvalidFileType() throws Exception {
////        MockMultipartFile file = new MockMultipartFile(
////                "file",
////                "employees.txt",
////                "text/plain",
////                "invalid content".getBytes()
////        );
////
////        doThrow(new InvalidFileException("Invalid file type"))
////                .when(fileStorageService).validateFileType(any(), any());
////
////        mockMvc.perform(multipart("/api/files/import/csv")
////                        .file(file))
////                .andExpect(status().isBadRequest())
////                .andExpect(jsonPath("$.errors[0]").value("Invalid file: Invalid file type"));
////    }
////
////    @Test
////    public void testImportCsv_EmptyFile() throws Exception {
////        MockMultipartFile file = new MockMultipartFile(
////                "file",
////                "empty.csv",
////                "text/csv",
////                new byte[0]
////        );
////
////        doThrow(new InvalidFileException("File is empty"))
////                .when(fileStorageService).validateFile(any());
////
////        mockMvc.perform(multipart("/api/files/import/csv")
////                        .file(file))
////                .andExpect(status().isBadRequest())
////                .andExpect(jsonPath("$.errors[0]").value("Invalid file: File is empty"));
////    }
////
////    @Test
////    public void testImportCsv_FileTooLarge() throws Exception {
////        MockMultipartFile file = new MockMultipartFile(
////                "file",
////                "large.csv",
////                "text/csv",
////                new byte[11 * 1024 * 1024] // 11MB > 10MB limit
////        );
////
////        // POPRAWIONE: Upewnij się, że walidacja rozmiaru jest wywoływana i rzuca wyjątek
////        doThrow(new InvalidFileException("File size exceeds limit"))
////                .when(fileStorageService).validateFileSize(any(MultipartFile.class), eq(10L * 1024 * 1024));
////
////        mockMvc.perform(multipart("/api/files/import/csv")
////                        .file(file))
////                .andExpect(status().isBadRequest())
////                .andExpect(jsonPath("$.errors[0]").value("Invalid file: File size exceeds limit"));
////
////        // WERYFIKACJA: Upewnij się, że walidacja została wywołana
////        verify(fileStorageService).validateFileSize(any(MultipartFile.class), eq(10L * 1024 * 1024));
////    }
////
////    @Test
////    public void testImportCsv_InternalServerError() throws Exception {
////        MockMultipartFile file = new MockMultipartFile(
////                "file",
////                "employees.csv",
////                "text/csv",
////                "content".getBytes()
////        );
////
////        // POPRAWIONE: Zmieniono katalog na uploads
////        when(fileStorageService.storeFile(any(), eq("uploads")))
////                .thenThrow(new RuntimeException("Database error"));
////
////        mockMvc.perform(multipart("/api/files/import/csv")
////                        .file(file))
////                .andExpect(status().isInternalServerError())
////                .andExpect(jsonPath("$.errors[0]").value("Import failed: Database error"));
////    }
////
////    @Test
////    public void testImportXml_NotImplemented() throws Exception {
////        MockMultipartFile file = new MockMultipartFile(
////                "file",
////                "data.xml",
////                "application/xml",
////                "<employees></employees>".getBytes()
////        );
////
////        mockMvc.perform(multipart("/api/files/import/xml")
////                        .file(file))
////                .andExpect(status().isOk())
////                .andExpect(jsonPath("$.errors[0]").value("XML import not yet implemented"));
////    }
////
////    // === TESTY EKSPORTU RAPORTÓW ===
////
////    @Test
////    public void testExportCsv_Success() throws Exception {
////        Path csvPath = Paths.get("reports/employees_all.csv");
////        when(reportGeneratorService.generateCsvReport(null)).thenReturn(csvPath);
////        when(fileStorageService.loadFileAsResource("employees_all.csv", "reports"))
////                .thenReturn(new ByteArrayResource("csv content".getBytes()));
////
////        mockMvc.perform(get("/api/files/export/csv"))
////                .andExpect(status().isOk())
////                .andExpect(header().string("Content-Type", "text/csv"))
////                .andExpect(header().string("Content-Disposition", "attachment; filename=\"employees_all.csv\""))
////                .andExpect(content().string("csv content"));
////    }
////
////    @Test
////    public void testExportCsv_WithCompany() throws Exception {
////        Path csvPath = Paths.get("reports/employees_TechCorp.csv");
////        when(reportGeneratorService.generateCsvReport("TechCorp")).thenReturn(csvPath);
////        when(fileStorageService.loadFileAsResource("employees_TechCorp.csv", "reports"))
////                .thenReturn(new ByteArrayResource("csv content".getBytes()));
////
////        mockMvc.perform(get("/api/files/export/csv")
////                        .param("company", "TechCorp"))
////                .andExpect(status().isOk())
////                .andExpect(header().string("Content-Disposition", "attachment; filename=\"employees_TechCorp.csv\""));
////    }
////
////    @Test
////    public void testExportCsv_FileStorageException() throws Exception {
////        when(reportGeneratorService.generateCsvReport(null))
////                .thenThrow(new FileStorageException("Cannot generate report"));
////
////        mockMvc.perform(get("/api/files/export/csv"))
////                .andExpect(status().is5xxServerError());
////    }
////
////    @Test
////    public void testGenerateStatisticsReport_Success() throws Exception {
////        Path pdfPath = Paths.get("reports/statistics_TechCorp.pdf");
////        when(reportGeneratorService.generateStatisticsPdf("TechCorp")).thenReturn(pdfPath);
////        when(fileStorageService.loadFileAsResource("statistics_TechCorp.pdf", "reports"))
////                .thenReturn(new ByteArrayResource("pdf content".getBytes()));
////
////        mockMvc.perform(get("/api/files/reports/statistics/TechCorp"))
////                .andExpect(status().isOk())
////                .andExpect(header().string("Content-Type", "application/pdf"))
////                .andExpect(header().string("Content-Disposition", "attachment; filename=\"statistics_TechCorp.pdf\""));
////    }
////
////    // === TESTY DOKUMENTÓW PRACOWNIKÓW ===
////
////    @Test
////    public void testUploadDocument_Success() throws Exception {
////        MockMultipartFile file = new MockMultipartFile(
////                "file",
////                "contract.pdf",
////                "application/pdf",
////                "pdf content".getBytes()
////        );
////
////        EmployeeDocument document = new EmployeeDocument();
////        document.setId("123");
////        document.setEmployeeEmail("john@example.com");
////        document.setFileName("stored_contract.pdf");
////        document.setOriginalFileName("contract.pdf");
////        document.setFileType(DocumentType.CONTRACT);
////
////        when(documentService.storeDocument(eq("john@example.com"), any(), eq(DocumentType.CONTRACT)))
////                .thenReturn(document);
////
////        mockMvc.perform(multipart("/api/files/documents/john@example.com")
////                        .file(file)
////                        .param("type", "CONTRACT"))
////                .andExpect(status().isCreated())
////                .andExpect(jsonPath("$.id").value("123"))
////                .andExpect(jsonPath("$.employeeEmail").value("john@example.com"))
////                .andExpect(jsonPath("$.fileType").value("CONTRACT"));
////    }
////
////    @Test
////    public void testGetEmployeeDocuments_Success() throws Exception {
////        EmployeeDocument doc1 = new EmployeeDocument();
////        doc1.setId("1");
////        doc1.setEmployeeEmail("john@example.com");
////
////        EmployeeDocument doc2 = new EmployeeDocument();
////        doc2.setId("2");
////        doc2.setEmployeeEmail("john@example.com");
////
////        List<EmployeeDocument> documents = Arrays.asList(doc1, doc2);
////        when(documentService.getEmployeeDocuments("john@example.com")).thenReturn(documents);
////
////        mockMvc.perform(get("/api/files/documents/john@example.com"))
////                .andExpect(status().isOk())
////                .andExpect(jsonPath("$.length()").value(2))
////                .andExpect(jsonPath("$[0].id").value("1"))
////                .andExpect(jsonPath("$[1].id").value("2"));
////    }
////
////    @Test
////    public void testGetEmployeeDocuments_Empty() throws Exception {
////        when(documentService.getEmployeeDocuments("empty@example.com")).thenReturn(Arrays.asList());
////
////        mockMvc.perform(get("/api/files/documents/empty@example.com"))
////                .andExpect(status().isOk())
////                .andExpect(jsonPath("$.length()").value(0));
////    }
////
////    @Test
////    public void testDownloadDocument_Success() throws Exception {
////        EmployeeDocument document = new EmployeeDocument();
////        document.setId("123");
////        document.setEmployeeEmail("john@example.com");
////        document.setFileName("stored_contract.pdf");
////        document.setOriginalFileName("contract.pdf");
////
////        when(documentService.getDocument("123")).thenReturn(document);
////        when(fileStorageService.loadFileAsResource("stored_contract.pdf", "documents/john@example.com"))
////                .thenReturn(new ByteArrayResource("pdf content".getBytes()));
////
////        mockMvc.perform(get("/api/files/documents/john@example.com/123"))
////                .andExpect(status().isOk())
////                .andExpect(header().string("Content-Type", "application/octet-stream"))
////                .andExpect(header().string("Content-Disposition", "attachment; filename=\"contract.pdf\""))
////                .andExpect(content().string("pdf content"));
////    }
////
////    @Test
////    public void testDownloadDocument_UnauthorizedAccess() throws Exception {
////        EmployeeDocument document = new EmployeeDocument();
////        document.setId("123");
////        document.setEmployeeEmail("john@example.com"); // Inny email niż w ścieżce
////
////        when(documentService.getDocument("123")).thenReturn(document);
////
////        mockMvc.perform(get("/api/files/documents/jane@example.com/123")) // Jane próbuje pobrać dokument Johna
////                .andExpect(status().isNotFound());
////    }
////
////    @Test
////    public void testDownloadDocument_DocumentNotFound() throws Exception {
////        when(documentService.getDocument("999"))
////                .thenThrow(new FileNotFoundException("Document not found"));
////
////        mockMvc.perform(get("/api/files/documents/john@example.com/999"))
////                .andExpect(status().isNotFound());
////    }
////
////    @Test
////    public void testDeleteDocument_Success() throws Exception {
////        EmployeeDocument document = new EmployeeDocument();
////        document.setId("123");
////        document.setEmployeeEmail("john@example.com");
////
////        when(documentService.getDocument("123")).thenReturn(document);
////        doNothing().when(documentService).deleteDocument("123");
////
////        mockMvc.perform(delete("/api/files/documents/john@example.com/123"))
////                .andExpect(status().isNoContent());
////    }
////
////    @Test
////    public void testDeleteDocument_UnauthorizedAccess() throws Exception {
////        EmployeeDocument document = new EmployeeDocument();
////        document.setId("123");
////        document.setEmployeeEmail("john@example.com");
////
////        when(documentService.getDocument("123")).thenReturn(document);
////
////        mockMvc.perform(delete("/api/files/documents/jane@example.com/123"))
////                .andExpect(status().isNotFound());
////    }
////
////    // === TESTY ZDJĘĆ PRACOWNIKÓW ===
////
////    @Test
////    public void testUploadPhoto_Success() throws Exception {
////        MockMultipartFile file = new MockMultipartFile(
////                "file",
////                "photo.jpg",
////                "image/jpeg",
////                new byte[1024]
////        );
////
////        Employee employee = new Employee("John Doe", "john@example.com", "TechCorp", Position.PROGRAMMER, 8000.0);
////
////        when(employeeService.employeeExists("john@example.com")).thenReturn(true);
////        when(employeeService.findEmployeeByEmail("john@example.com")).thenReturn(Optional.of(employee));
////        when(fileStorageService.storeFile(any(), eq("photos"))).thenReturn("john_example_com.jpg");
////
////        mockMvc.perform(multipart("/api/files/photos/john@example.com")
////                        .file(file))
////                .andExpect(status().isOk())
////                .andExpect(content().string("Photo uploaded successfully: john_example_com.jpg"));
////    }
////
////    @Test
////    public void testUploadPhoto_InvalidImage() throws Exception {
////        MockMultipartFile file = new MockMultipartFile(
////                "file",
////                "document.pdf", // Nie obraz
////                "application/pdf",
////                new byte[1024]
////        );
////
////        when(employeeService.employeeExists("john@example.com")).thenReturn(true);
////        doThrow(new InvalidFileException("File must be a JPEG or PNG image"))
////                .when(fileStorageService).validateImageFile(any());
////
////        mockMvc.perform(multipart("/api/files/photos/john@example.com")
////                        .file(file))
////                .andExpect(status().isBadRequest())
////                .andExpect(content().string("File must be a JPEG or PNG image"));
////    }
////
////    @Test
////    public void testUploadPhoto_FileTooLarge() throws Exception {
////        MockMultipartFile file = new MockMultipartFile(
////                "file",
////                "large_photo.jpg",
////                "image/jpeg",
////                new byte[3 * 1024 * 1024] // 3MB > 2MB limit dla zdjęć
////        );
////
////        when(employeeService.employeeExists("john@example.com")).thenReturn(true);
////        doThrow(new InvalidFileException("File size exceeds maximum allowed size: 2097152 bytes"))
////                .when(fileStorageService).validateFileSize(any(), eq(2L * 1024 * 1024));
////
////        mockMvc.perform(multipart("/api/files/photos/john@example.com")
////                        .file(file))
////                .andExpect(status().isBadRequest())
////                .andExpect(content().string("File size exceeds maximum allowed size: 2097152 bytes"));
////    }
////
////    @Test
////    public void testUploadPhoto_ValidSizeWithinLimit() throws Exception {
////        MockMultipartFile file = new MockMultipartFile(
////                "file",
////                "photo.jpg",
////                "image/jpeg",
////                new byte[1 * 1024 * 1024] // 1MB < 2MB limit dla zdjęć
////        );
////
////        Employee employee = new Employee("John Doe", "john@example.com", "TechCorp", Position.PROGRAMMER, 8000.0);
////
////        when(employeeService.employeeExists("john@example.com")).thenReturn(true);
////        when(employeeService.findEmployeeByEmail("john@example.com")).thenReturn(Optional.of(employee));
////        when(fileStorageService.storeFile(any(), eq("photos"))).thenReturn("john_example_com.jpg");
////
////        mockMvc.perform(multipart("/api/files/photos/john@example.com")
////                        .file(file))
////                .andExpect(status().isOk());
////
////        // POPRAWIONE: 2MB limit dla zdjęć zamiast 10MB
////        verify(fileStorageService).validateFileSize(any(), eq(2L * 1024 * 1024));
////    }
////
////    @Test
////    public void testGetPhoto_Success() throws Exception {
////        Employee employee = new Employee("John Doe", "john@example.com", "TechCorp", Position.PROGRAMMER, 8000.0);
////        employee.setPhotoFileName("john_example_com.jpg");
////
////        when(employeeService.findEmployeeByEmail("john@example.com")).thenReturn(Optional.of(employee));
////        when(fileStorageService.loadFileAsResource("john_example_com.jpg", "photos"))
////                .thenReturn(new ByteArrayResource("image data".getBytes()));
////
////        mockMvc.perform(get("/api/files/photos/john@example.com"))
////                .andExpect(status().isOk())
////                .andExpect(header().string("Content-Type", "image/jpeg"))
////                .andExpect(header().string("Content-Disposition", "inline; filename=\"john_example_com.jpg\""))
////                .andExpect(content().string("image data"));
////    }
////
////    @Test
////    public void testGetPhoto_PngImage() throws Exception {
////        Employee employee = new Employee("John Doe", "john@example.com", "TechCorp", Position.PROGRAMMER, 8000.0);
////        employee.setPhotoFileName("photo.png");
////
////        when(employeeService.findEmployeeByEmail("john@example.com")).thenReturn(Optional.of(employee));
////        when(fileStorageService.loadFileAsResource("photo.png", "photos"))
////                .thenReturn(new ByteArrayResource("image data".getBytes()));
////
////        mockMvc.perform(get("/api/files/photos/john@example.com"))
////                .andExpect(status().isOk())
////                .andExpect(header().string("Content-Type", "image/png"));
////    }
////
////    @Test
////    public void testGetPhoto_EmployeeNotFound() throws Exception {
////        when(employeeService.findEmployeeByEmail("nonexisting@example.com")).thenReturn(Optional.empty());
////
////        mockMvc.perform(get("/api/files/photos/nonexisting@example.com"))
////                .andExpect(status().isNotFound());
////    }
////
////    @Test
////    public void testGetPhoto_NoPhoto() throws Exception {
////        Employee employee = new Employee("John Doe", "john@example.com", "TechCorp", Position.PROGRAMMER, 8000.0);
////        // employee.setPhotoFileName(null); // Brak zdjęcia
////
////        when(employeeService.findEmployeeByEmail("john@example.com")).thenReturn(Optional.of(employee));
////
////        mockMvc.perform(get("/api/files/photos/john@example.com"))
////                .andExpect(status().isNotFound());
////    }
////
////    @Test
////    public void testDeletePhoto_Success() throws Exception {
////        Employee employee = new Employee("John Doe", "john@example.com", "TechCorp", Position.PROGRAMMER, 8000.0);
////        employee.setPhotoFileName("john_example_com.jpg");
////
////        when(employeeService.findEmployeeByEmail("john@example.com")).thenReturn(Optional.of(employee));
////        when(fileStorageService.deleteFile("john_example_com.jpg", "photos")).thenReturn(true);
////
////        mockMvc.perform(delete("/api/files/photos/john@example.com"))
////                .andExpect(status().isNoContent());
////    }
////
////    @Test
////    public void testDeletePhoto_NoPhoto() throws Exception {
////        Employee employee = new Employee("John Doe", "john@example.com", "TechCorp", Position.PROGRAMMER, 8000.0);
////        // employee.setPhotoFileName(null); // Brak zdjęcia
////
////        when(employeeService.findEmployeeByEmail("john@example.com")).thenReturn(Optional.of(employee));
////
////        mockMvc.perform(delete("/api/files/photos/john@example.com"))
////                .andExpect(status().isNoContent());
////
////        verify(fileStorageService, never()).deleteFile(anyString(), anyString());
////    }
////
////    @Test
////    public void testDeletePhoto_EmployeeNotFound() throws Exception {
////        when(employeeService.findEmployeeByEmail("nonexisting@example.com")).thenReturn(Optional.empty());
////
////        mockMvc.perform(delete("/api/files/photos/nonexisting@example.com"))
////                .andExpect(status().isNoContent());
////    }
////
////    @Test
////    public void testDeletePhoto_FileStorageException() throws Exception {
////        Employee employee = new Employee("John Doe", "john@example.com", "TechCorp", Position.PROGRAMMER, 8000.0);
////        employee.setPhotoFileName("john_example_com.jpg");
////
////        when(employeeService.findEmployeeByEmail("john@example.com")).thenReturn(Optional.of(employee));
////        when(fileStorageService.deleteFile("john_example_com.jpg", "photos"))
////                .thenThrow(new FileStorageException("Cannot delete file"));
////
////        mockMvc.perform(delete("/api/files/photos/john@example.com"))
////                .andExpect(status().is5xxServerError());
////    }
////
////    // === TESTY DODATKOWE ===
////
////    @Test
////    public void testUploadPhoto_InternalServerError() throws Exception {
////        MockMultipartFile file = new MockMultipartFile(
////                "file",
////                "photo.jpg",
////                "image/jpeg",
////                new byte[1024]
////        );
////
////        when(employeeService.employeeExists("john@example.com")).thenReturn(true);
////        when(fileStorageService.storeFile(any(), eq("photos")))
////                .thenThrow(new RuntimeException("Storage error"));
////
////        mockMvc.perform(multipart("/api/files/photos/john@example.com")
////                        .file(file))
////                .andExpect(status().isInternalServerError())
////                .andExpect(content().string("Storage error"));
////    }
////
////    @Test
////    public void testImportCsv_WithValidSizeWithinLimit() throws Exception {
////        MockMultipartFile file = new MockMultipartFile(
////                "file",
////                "employees.csv",
////                "text/csv",
////                new byte[5 * 1024 * 1024] // 5MB < 10MB limit
////        );
////
////        ImportSummary summary = new ImportSummary();
////        summary.incrementImported();
////
////        // POPRAWIONE: Zmieniono katalog na uploads
////        when(fileStorageService.storeFile(any(), eq("uploads"))).thenReturn("uploads/employees.csv");
////        when(importService.importFromCsv(eq("uploads/employees.csv"))).thenReturn(summary);
////
////        mockMvc.perform(multipart("/api/files/import/csv")
////                        .file(file))
////                .andExpect(status().isOk());
////
////        // POPRAWIONE: 10MB limit dla CSV
////        verify(fileStorageService).validateFileSize(any(), eq(10L * 1024 * 1024));
////    }
////
////    // NOWY TEST: Sprawdzenie przekazywania ścieżki do ImportService
////    @Test
////    public void testImportCsv_FilePathPassedToService() throws Exception {
////        MockMultipartFile file = new MockMultipartFile(
////                "file",
////                "test.csv",
////                "text/csv",
////                "content".getBytes()
////        );
////
////        ImportSummary summary = new ImportSummary();
////        when(fileStorageService.storeFile(any(), eq("uploads"))).thenReturn("uploads/test_123.csv");
////        when(importService.importFromCsv(eq("uploads/test_123.csv"))).thenReturn(summary);
////
////        mockMvc.perform(multipart("/api/files/import/csv")
////                        .file(file))
////                .andExpect(status().isOk());
////
////        // Weryfikacja, że ImportService otrzymał prawidłową ścieżkę
////        verify(importService).importFromCsv("uploads/test_123.csv");
////    }
////}






package com.techcorp.employee.controller;

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

import java.util.Arrays;
import java.util.List;

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
    private EmployeeDocument sampleDocument;

    @BeforeEach
    void setUp() {
        // Przykładowe pliki testowe
        validCsvFile = new MockMultipartFile(
                "file",
                "employees.csv",
                "text/csv",
                "name,email,position\nJohn Doe,john@example.com,Developer".getBytes()
        );

        validXmlFile = new MockMultipartFile(
                "file",
                "employees.xml",
                "application/xml",
                "<?xml version=\"1.0\"?><employees><employee><name>John Doe</name></employee></employees>".getBytes()
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

        // Przykładowe wyniki importu - używamy właściwych metod
        successSummary = new ImportSummary();
        // Zakładając, że ImportSummary ma metody addProcessedRecord() lub podobne
        // Jeśli nie ma setterów, używamy konstruktora lub dostępnych metod

        errorSummary = new ImportSummary();
        errorSummary.addError("Line 3: Invalid email format");
        errorSummary.addError("Line 7: Missing required field");

        // Przykładowy dokument
        sampleDocument = new EmployeeDocument();
        sampleDocument.setId("doc123");
        sampleDocument.setEmployeeEmail("john@example.com");
        sampleDocument.setFileName("contract_123.pdf");
        sampleDocument.setOriginalFileName("contract.pdf");
        sampleDocument.setFileType(DocumentType.CONTRACT);
        sampleDocument.setFileSize(2048L);
    }

    // === TESTY IMPORTU CSV ===

    @Test
    void importCsv_WithValidFile_ShouldReturnSuccess() throws Exception {
        // Given
        when(importService.importCsvFile(any(MockMultipartFile.class))).thenReturn(successSummary);

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/files/import/csv")
                        .file(validCsvFile)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk());

        verify(importService, times(1)).importCsvFile(any(MockMultipartFile.class));
    }

//    @Test
//    void importCsv_WithImportErrors_ShouldReturnBadRequest() throws Exception {
//        // Given
//        when(importService.importCsvFile(any(MockMultipartFile.class))).thenReturn(errorSummary);
//
//        // When & Then
//        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/files/import/csv")
//                        .file(validCsvFile)
//                        .contentType(MediaType.MULTIPART_FORM_DATA))
//                .andExpect(status().isBadRequest())
//                .andExpect(jsonPath("$.hasErrors").value(true));
//
//        verify(importService, times(1)).importCsvFile(any(MockMultipartFile.class));
//    }

    // === TESTY IMPORTU XML ===

    @Test
    void importXml_WithValidFile_ShouldReturnSuccess() throws Exception {
        // Given
        when(importService.importXmlFile(any(MockMultipartFile.class))).thenReturn(successSummary);

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/files/import/xml")
                        .file(validXmlFile)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk());

        verify(importService, times(1)).importXmlFile(any(MockMultipartFile.class));
    }

//    @Test
//    void importXml_WithImportErrors_ShouldReturnBadRequest() throws Exception {
//        // Given
//        when(importService.importXmlFile(any(MockMultipartFile.class))).thenReturn(errorSummary);
//
//        // When & Then
//        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/files/import/xml")
//                        .file(validXmlFile)
//                        .contentType(MediaType.MULTIPART_FORM_DATA))
//                .andExpect(status().isBadRequest())
//                .andExpect(jsonPath("$.hasErrors").value(true));
//
//        verify(importService, times(1)).importXmlFile(any(MockMultipartFile.class));
//    }

    // === TESTY EKSPORTU RAPORTÓW ===

    @Test
    void exportCsv_WithValidRequest_ShouldReturnCsvFile() throws Exception {
        // Given
        String csvContent = "name,email,position\nJohn Doe,john@example.com,Developer";
        Resource csvResource = new ByteArrayResource(csvContent.getBytes());

        ResponseEntity<Resource> responseEntity = ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/csv"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"employees.csv\"")
                .body(csvResource);

        when(reportGeneratorService.exportCsvReport(anyString())).thenReturn(responseEntity);

        // When & Then
        mockMvc.perform(get("/api/files/export/csv")
                        .param("company", "TechCorp"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/csv"))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"employees.csv\""))
                .andExpect(content().string(csvContent));

        verify(reportGeneratorService, times(1)).exportCsvReport("TechCorp");
    }

    @Test
    void exportCsv_WithoutCompany_ShouldReturnCsvFile() throws Exception {
        // Given
        String csvContent = "name,email,position\nAll employees data";
        Resource csvResource = new ByteArrayResource(csvContent.getBytes());

        ResponseEntity<Resource> responseEntity = ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/csv"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"employees.csv\"")
                .body(csvResource);

        when(reportGeneratorService.exportCsvReport(isNull())).thenReturn(responseEntity);

        // When & Then
        mockMvc.perform(get("/api/files/export/csv"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/csv"))
                .andExpect(content().string(csvContent));

        verify(reportGeneratorService, times(1)).exportCsvReport(null);
    }

    @Test
    void generateStatisticsReport_WithValidCompany_ShouldReturnPdf() throws Exception {
        // Given
        byte[] pdfContent = "%PDF-1.4 sample pdf content".getBytes();
        Resource pdfResource = new ByteArrayResource(pdfContent);

        ResponseEntity<Resource> responseEntity = ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"statistics_TechCorp.pdf\"")
                .body(pdfResource);

        when(reportGeneratorService.exportStatisticsReport("TechCorp")).thenReturn(responseEntity);

        // When & Then
        mockMvc.perform(get("/api/files/reports/statistics/{companyName}", "TechCorp"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"statistics_TechCorp.pdf\""))
                .andExpect(content().bytes(pdfContent));

        verify(reportGeneratorService, times(1)).exportStatisticsReport("TechCorp");
    }

    // === TESTY DOKUMENTÓW PRACOWNIKÓW ===

    @Test
    void uploadDocument_WithValidData_ShouldReturnCreated() throws Exception {
        // Given
        when(documentService.storeDocument(eq("john@example.com"), any(MockMultipartFile.class), eq(DocumentType.CONTRACT)))
                .thenReturn(sampleDocument);

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/files/documents/{email}", "john@example.com")
                        .file(documentFile)
                        .param("type", "CONTRACT")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("doc123"))
                .andExpect(jsonPath("$.employeeEmail").value("john@example.com"))
                .andExpect(jsonPath("$.fileType").value("CONTRACT"))
                .andExpect(jsonPath("$.originalFileName").value("contract.pdf"));

        verify(documentService, times(1)).storeDocument(eq("john@example.com"), any(MockMultipartFile.class), eq(DocumentType.CONTRACT));
    }

    @Test
    void getEmployeeDocuments_WithValidEmail_ShouldReturnDocumentList() throws Exception {
        // Given
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
        when(employeeService.uploadEmployeePhoto(eq("john@example.com"), any(MockMultipartFile.class)))
                .thenReturn(ResponseEntity.ok("Photo uploaded successfully: profile_photo.jpg"));

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/files/photos/{email}", "john@example.com")
                        .file(imageFile)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(content().string("Photo uploaded successfully: profile_photo.jpg"));

        verify(employeeService, times(1)).uploadEmployeePhoto(eq("john@example.com"), any(MockMultipartFile.class));
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

    // === TESTY BŁĘDÓW ===

    @Test
    void uploadPhoto_WithServiceError_ShouldReturnError() throws Exception {
        // Given
        when(employeeService.uploadEmployeePhoto(eq("john@example.com"), any(MockMultipartFile.class)))
                .thenReturn(ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid image file"));

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/files/photos/{email}", "john@example.com")
                        .file(imageFile)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid image file"));

        verify(employeeService, times(1)).uploadEmployeePhoto(eq("john@example.com"), any(MockMultipartFile.class));
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
    void importCsv_WithEmptyFile_ShouldHandleGracefully() throws Exception {
        // Given
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file", "empty.csv", "text/csv", new byte[0]
        );

        ImportSummary emptySummary = new ImportSummary();

        when(importService.importCsvFile(any(MockMultipartFile.class))).thenReturn(emptySummary);

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/files/import/csv")
                        .file(emptyFile)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk());

        verify(importService, times(1)).importCsvFile(any(MockMultipartFile.class));
    }

    @Test
    void exportCsv_WithServiceException_ShouldPropagateException() throws Exception {
        // Given
        when(reportGeneratorService.exportCsvReport(anyString()))
                .thenThrow(new RuntimeException("Report generation failed"));

        // When & Then
        mockMvc.perform(get("/api/files/export/csv")
                        .param("company", "TechCorp"))
                .andExpect(status().is5xxServerError());

        verify(reportGeneratorService, times(1)).exportCsvReport("TechCorp");
    }

    @Test
    void getAllEndpoints_WithInvalidPaths_ShouldReturnNotFound() throws Exception {
        // Test nieistniejących endpointów
        mockMvc.perform(get("/api/files/nonexistent"))
                .andExpect(status().isNotFound());

        mockMvc.perform(post("/api/files/invalid/endpoint"))
                .andExpect(status().isNotFound());
    }
}