////package com.techcorp.employee.controller;
////
////import com.techcorp.employee.service.*;
////import com.techcorp.employee.model.*;
////import com.techcorp.employee.exception.*;
////import org.springframework.beans.factory.annotation.Autowired;
////import org.springframework.core.io.Resource;
////import org.springframework.core.io.UrlResource;
////import org.springframework.http.HttpHeaders;
////import org.springframework.http.HttpStatus;
////import org.springframework.http.MediaType;
////import org.springframework.http.ResponseEntity;
////import org.springframework.web.bind.annotation.*;
////import org.springframework.web.multipart.MultipartFile;
////
////import java.io.IOException;
////import java.nio.file.Files;
////import java.nio.file.Path;
////import java.util.List;
////
////@RestController
////@RequestMapping("/api/files")
////public class FileUploadController {
////
////    @Autowired
////    private FileStorageService fileStorageService;
////
////    @Autowired
////    private ImportService importService;
////
////    @Autowired
////    private ReportGeneratorService reportGeneratorService;
////
////    @Autowired
////    private EmployeeDocumentService documentService;
////
////    @Autowired
////    private EmployeeService employeeService;
////
////    // === IMPORT PLIKÓW ===
////
////    @PostMapping("/import/csv")
////    public ResponseEntity<ImportSummary> importCsv(@RequestParam("file") MultipartFile file) {
////        ImportSummary summary = new ImportSummary();
////
////        try {
////            // Walidacja pliku
////            fileStorageService.validateFile(file);
////            fileStorageService.validateFileType(file, new String[]{".csv"});
////            fileStorageService.validateFileSize(file, 10L * 1024 * 1024);
////
////            // Zapis pliku i import
////            String fileName = fileStorageService.storeFile(file, "uploads");
////            Path uploadsDir = fileStorageService.getFileStorageLocation().resolve("uploads");
////            String filePath = uploadsDir.resolve(fileName).toString();
////
////            summary = importService.importFromCsv(filePath);
////            return ResponseEntity.ok(summary);
////
////        } catch (InvalidFileException e) {
////            summary.addError("Invalid file: " + e.getMessage());
////            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(summary);
////        } catch (Exception e) {
////            summary.addError("Import failed: " + e.getMessage());
////            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(summary);
////        }
////    }
////
////    @PostMapping("/import/xml")
////    public ResponseEntity<ImportSummary> importXml(@RequestParam("file") MultipartFile file) {
////        ImportSummary summary = new ImportSummary();
////
////        try {
////            // Walidacja pliku
////            fileStorageService.validateFile(file);
////            fileStorageService.validateFileType(file, new String[]{".xml"});
////            fileStorageService.validateFileSize(file, 10L * 1024 * 1024);
////
////            // Zapis pliku i import
////            String fileName = fileStorageService.storeFile(file, "uploads");
////            Path uploadsDir = fileStorageService.getFileStorageLocation().resolve("uploads");
////            String filePath = uploadsDir.resolve(fileName).toString();
////
////            summary = importService.importFromXml(filePath);
////            return ResponseEntity.ok(summary);
////
////        } catch (InvalidFileException e) {
////            summary.addError("Invalid file: " + e.getMessage());
////            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(summary);
////        } catch (Exception e) {
////            summary.addError("XML import failed: " + e.getMessage());
////            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(summary);
////        }
////    }
////
////    // === EKSPORT RAPORTÓW ===
////
////    @GetMapping("/export/csv")
////    public ResponseEntity<Resource> exportCsv(@RequestParam(required = false) String company) {
////        try {
////            Path csvPath = reportGeneratorService.generateCsvReport(company);
////
////            // Sprawdź różne lokalizacje pliku
////            if (!Files.exists(csvPath)) {
////                Path reportsDir = fileStorageService.getReportsStorageLocation();
////                String fileName = csvPath.getFileName().toString();
////                Path alternativePath = reportsDir.resolve(fileName);
////
////                if (Files.exists(alternativePath)) {
////                    csvPath = alternativePath;
////                } else {
////                    throw new FileNotFoundException("CSV file not found at: " + csvPath.toAbsolutePath());
////                }
////            }
////
////            Resource resource = new UrlResource(csvPath.toUri());
////            if (!resource.exists()) {
////                throw new FileNotFoundException("CSV file not found");
////            }
////
////            return createFileResponse(resource, "text/csv", csvPath.getFileName().toString());
////
////        } catch (Exception e) {
////            throw new FileStorageException("Could not generate CSV report: " + e.getMessage(), e);
////        }
////    }
////
////    @GetMapping("/reports/statistics/{companyName}")
////    public ResponseEntity<Resource> generateStatisticsReport(@PathVariable String companyName) {
////        try {
////            Path pdfPath = reportGeneratorService.generateStatisticsPdf(companyName);
////
////            Resource resource = new UrlResource(pdfPath.toUri());
////            if (!resource.exists()) {
////                throw new FileNotFoundException("PDF file not found at: " + pdfPath.toAbsolutePath());
////            }
////
////            return createFileResponse(resource, MediaType.APPLICATION_PDF_VALUE, pdfPath.getFileName().toString());
////
////        } catch (Exception e) {
////            throw new FileStorageException("Could not generate statistics report", e);
////        }
////    }
////
////    // === DOKUMENTY PRACOWNIKÓW ===
////
////    @PostMapping("/documents/{email}")
////    public ResponseEntity<EmployeeDocument> uploadDocument(
////            @PathVariable String email,
////            @RequestParam("file") MultipartFile file,
////            @RequestParam("type") DocumentType documentType) {
////
////        EmployeeDocument document = documentService.storeDocument(email, file, documentType);
////        return ResponseEntity.status(HttpStatus.CREATED).body(document);
////    }
////
////    @GetMapping("/documents/{email}")
////    public ResponseEntity<List<EmployeeDocument>> getEmployeeDocuments(@PathVariable String email) {
////        List<EmployeeDocument> documents = documentService.getEmployeeDocuments(email);
////        return ResponseEntity.ok(documents);
////    }
////
////    @GetMapping("/documents/{email}/{documentId}")
////    public ResponseEntity<Resource> downloadDocument(
////            @PathVariable String email,
////            @PathVariable String documentId) {
////
////        EmployeeDocument document = documentService.getDocument(documentId);
////
////        if (!document.getEmployeeEmail().equalsIgnoreCase(email)) {
////            throw new FileNotFoundException("Document not found for employee: " + email);
////        }
////
////        Resource resource = fileStorageService.loadFileAsResource(
////                document.getFileName(), "documents/" + email);
////
////        return createFileResponse(resource, MediaType.APPLICATION_OCTET_STREAM_VALUE, document.getOriginalFileName());
////    }
////
////    @DeleteMapping("/documents/{email}/{documentId}")
////    public ResponseEntity<Void> deleteDocument(
////            @PathVariable String email,
////            @PathVariable String documentId) {
////
////        EmployeeDocument document = documentService.getDocument(documentId);
////
////        if (!document.getEmployeeEmail().equalsIgnoreCase(email)) {
////            throw new FileNotFoundException("Document not found for employee: " + email);
////        }
////
////        documentService.deleteDocument(documentId);
////        return ResponseEntity.noContent().build();
////    }
////
////    // === ZDJĘCIA PRACOWNIKÓW ===
////
////    @PostMapping("/photos/{email}")
////    public ResponseEntity<String> uploadPhoto(
////            @PathVariable String email,
////            @RequestParam("file") MultipartFile file) {
////
////        try {
////            // Walidacja pracownika
////            if (!employeeService.employeeExists(email)) {
////                throw new EmployeeNotFoundException("Employee not found with email: " + email);
////            }
////
////            // Walidacja pliku
////            fileStorageService.validateFile(file);
////            fileStorageService.validateImageFile(file);
////            fileStorageService.validateFileSize(file, 2 * 1024 * 1024); // 2MB
////
////            // Utwórz nazwę pliku na podstawie emaila
////            String fileExtension = getFileExtension(file.getOriginalFilename());
////            String customFileName = email.replace("@", "_").replace(".", "_") + fileExtension;
////
////            // Zapisz plik
////            String savedFileName = fileStorageService.storeFileWithCustomName(file, "photos", customFileName);
////
////            // Zaktualizuj pracownika
////            employeeService.findEmployeeByEmail(email).ifPresent(employee -> {
////                employee.setPhotoFileName(savedFileName);
////            });
////
////            return ResponseEntity.ok("Photo uploaded successfully: " + savedFileName);
////
////        } catch (InvalidFileException e) {
////            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
////        } catch (Exception e) {
////            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
////        }
////    }
////
////    @GetMapping("/photos/{email}")
////    public ResponseEntity<Resource> getPhoto(@PathVariable String email) {
////        try {
////            // Znajdź pracownika i nazwę pliku zdjęcia
////            String photoFileName = employeeService.findEmployeeByEmail(email)
////                    .map(Employee::getPhotoFileName)
////                    .orElseThrow(() -> new FileNotFoundException("Employee not found or has no photo"));
////
////            // Załaduj zdjęcie
////            Path photosDir = fileStorageService.getPhotosStorageLocation();
////            Path photoPath = photosDir.resolve(photoFileName);
////
////            Resource resource = new UrlResource(photoPath.toUri());
////            if (!resource.exists()) {
////                return ResponseEntity.notFound().build();
////            }
////
////            // Określ typ zawartości
////            String contentType = determineImageContentType(photoFileName);
////
////            return createImageResponse(resource, contentType, photoFileName);
////
////        } catch (FileNotFoundException e) {
////            return ResponseEntity.notFound().build();
////        } catch (Exception e) {
////            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
////        }
////    }
////
////    @DeleteMapping("/photos/{email}")
////    public ResponseEntity<Void> deletePhoto(@PathVariable String email) {
////        try {
////            String photoFileName = employeeService.findEmployeeByEmail(email)
////                    .map(Employee::getPhotoFileName)
////                    .orElse(null);
////
////            if (photoFileName != null) {
////                fileStorageService.deleteFile(photoFileName, "photos");
////
////                // Wyczyść referencję do zdjęcia
////                employeeService.findEmployeeByEmail(email).ifPresent(employee -> {
////                    employee.setPhotoFileName(null);
////                });
////            }
////
////            return ResponseEntity.noContent().build();
////
////        } catch (Exception e) {
////            throw new FileStorageException("Could not delete photo", e);
////        }
////    }
////
////    // === PRYWATNE METODY POMOCNICZE ===
////
////    private ResponseEntity<Resource> createFileResponse(Resource resource, String contentType, String filename) {
////        return ResponseEntity.ok()
////                .contentType(MediaType.parseMediaType(contentType))
////                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
////                .body(resource);
////    }
////
////    private ResponseEntity<Resource> createImageResponse(Resource resource, String contentType, String filename) {
////        return ResponseEntity.ok()
////                .contentType(MediaType.parseMediaType(contentType))
////                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
////                .body(resource);
////    }
////
////    private String determineImageContentType(String filename) {
////        if (filename.toLowerCase().endsWith(".png")) {
////            return "image/png";
////        } else if (filename.toLowerCase().endsWith(".gif")) {
////            return "image/gif";
////        } else {
////            return "image/jpeg";
////        }
////    }
////
////    private String getFileExtension(String fileName) {
////        if (fileName == null) return ".jpg";
////        int lastDotIndex = fileName.lastIndexOf(".");
////        return lastDotIndex > 0 ? fileName.substring(lastDotIndex) : ".jpg";
////    }
////}
//
//
//
//
//
//
//
//
//package com.techcorp.employee.controller;
//
//import com.techcorp.employee.service.*;
//import com.techcorp.employee.model.*;
//import com.techcorp.employee.exception.*;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.core.io.Resource;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.util.List;
//
//@RestController
//@RequestMapping("/api/files")
//public class FileUploadController {
//
//    @Autowired
//    private FileStorageService fileStorageService;
//
//    @Autowired
//    private ImportService importService;
//
//    @Autowired
//    private ReportGeneratorService reportGeneratorService;
//
//    @Autowired
//    private EmployeeDocumentService documentService;
//
//    @Autowired
//    private EmployeeService employeeService;
//
//    // === IMPORT PLIKÓW ===
//
//    @PostMapping("/import/csv")
//    public ResponseEntity<ImportSummary> importCsv(@RequestParam("file") MultipartFile file) {
//        ImportSummary summary = importService.importCsvFile(file);
//        return createImportResponse(summary);
//    }
//
//    @PostMapping("/import/xml")
//    public ResponseEntity<ImportSummary> importXml(@RequestParam("file") MultipartFile file) {
//        ImportSummary summary = importService.importXmlFile(file);
//        return createImportResponse(summary);
//    }
//
//    // === EKSPORT RAPORTÓW ===
//
//    @GetMapping("/export/csv")
//    public ResponseEntity<Resource> exportCsv(@RequestParam(required = false) String company) {
//        return reportGeneratorService.exportCsvReport(company);
//    }
//
//    @GetMapping("/reports/statistics/{companyName}")
//    public ResponseEntity<Resource> generateStatisticsReport(@PathVariable String companyName) {
//        return reportGeneratorService.exportStatisticsReport(companyName);
//    }
//
//    // === DOKUMENTY PRACOWNIKÓW ===
//
//    @PostMapping("/documents/{email}")
//    public ResponseEntity<EmployeeDocument> uploadDocument(
//            @PathVariable String email,
//            @RequestParam("file") MultipartFile file,
//            @RequestParam("type") DocumentType documentType) {
//
//        EmployeeDocument document = documentService.storeDocument(email, file, documentType);
//        return ResponseEntity.status(HttpStatus.CREATED).body(document);
//    }
//
//    @GetMapping("/documents/{email}")
//    public ResponseEntity<List<EmployeeDocument>> getEmployeeDocuments(@PathVariable String email) {
//        List<EmployeeDocument> documents = documentService.getEmployeeDocuments(email);
//        return ResponseEntity.ok(documents);
//    }
//
//    @GetMapping("/documents/{email}/{documentId}")
//    public ResponseEntity<Resource> downloadDocument(
//            @PathVariable String email,
//            @PathVariable String documentId) {
//
//        return documentService.downloadDocument(email, documentId);
//    }
//
//    @DeleteMapping("/documents/{email}/{documentId}")
//    public ResponseEntity<Void> deleteDocument(
//            @PathVariable String email,
//            @PathVariable String documentId) {
//
//        documentService.deleteDocument(email, documentId);
//        return ResponseEntity.noContent().build();
//    }
//
//    // === ZDJĘCIA PRACOWNIKÓW ===
//
//    @PostMapping("/photos/{email}")
//    public ResponseEntity<String> uploadPhoto(
//            @PathVariable String email,
//            @RequestParam("file") MultipartFile file) {
//
//        return employeeService.uploadEmployeePhoto(email, file);
//    }
//
//    @GetMapping("/photos/{email}")
//    public ResponseEntity<Resource> getPhoto(@PathVariable String email) {
//        return employeeService.getEmployeePhoto(email);
//    }
//
//    @DeleteMapping("/photos/{email}")
//    public ResponseEntity<Void> deletePhoto(@PathVariable String email) {
//        employeeService.deleteEmployeePhoto(email);
//        return ResponseEntity.noContent().build();
//    }
//
//    // === PRYWATNE METODY POMOCNICZE ===
//
//    private ResponseEntity<ImportSummary> createImportResponse(ImportSummary summary) {
//        if (summary.hasErrors()) {
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(summary);
//        }
//        return ResponseEntity.ok(summary);
//    }
//}







package com.techcorp.employee.controller;

import com.techcorp.employee.service.*;
import com.techcorp.employee.model.*;
import com.techcorp.employee.exception.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/files")
public class FileUploadController {

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private ImportService importService;

    @Autowired
    private ReportGeneratorService reportGeneratorService;

    @Autowired
    private EmployeeDocumentService documentService;

    @Autowired
    private EmployeeService employeeService;

    // === IMPORT PLIKÓW ===

    @PostMapping("/import/csv")
    public ResponseEntity<ImportSummary> importCsv(@RequestParam("file") MultipartFile file) {
        ImportSummary summary = importService.importCsvFile(file);
        return createImportResponse(summary);
    }

    @PostMapping("/import/xml")
    public ResponseEntity<ImportSummary> importXml(@RequestParam("file") MultipartFile file) {
        ImportSummary summary = importService.importXmlFile(file);
        return createImportResponse(summary);
    }

    // === EKSPORT RAPORTÓW ===

    @GetMapping("/export/csv")
    public ResponseEntity<org.springframework.core.io.Resource> exportCsv(@RequestParam(required = false) String company) {
        return reportGeneratorService.exportCsvReport(company);
    }

    @GetMapping("/reports/statistics/{companyName}")
    public ResponseEntity<org.springframework.core.io.Resource> generateStatisticsReport(@PathVariable String companyName) {
        return reportGeneratorService.exportStatisticsReport(companyName);
    }

    // === DOKUMENTY PRACOWNIKÓW ===

    @PostMapping("/documents/{email}")
    public ResponseEntity<EmployeeDocument> uploadDocument(
            @PathVariable String email,
            @RequestParam("file") MultipartFile file,
            @RequestParam("type") DocumentType documentType) {

        EmployeeDocument document = documentService.storeDocument(email, file, documentType);
        return ResponseEntity.status(HttpStatus.CREATED).body(document);
    }

    @GetMapping("/documents/{email}")
    public ResponseEntity<List<EmployeeDocument>> getEmployeeDocuments(@PathVariable String email) {
        List<EmployeeDocument> documents = documentService.getEmployeeDocuments(email);
        return ResponseEntity.ok(documents);
    }

    @GetMapping("/documents/{email}/{documentId}")
    public ResponseEntity<org.springframework.core.io.Resource> downloadDocument(
            @PathVariable String email,
            @PathVariable String documentId) {

        return documentService.downloadDocument(email, documentId);
    }

    @DeleteMapping("/documents/{email}/{documentId}")
    public ResponseEntity<Void> deleteDocument(
            @PathVariable String email,
            @PathVariable String documentId) {

        documentService.deleteDocument(email, documentId);
        return ResponseEntity.noContent().build();
    }

    // === ZDJĘCIA PRACOWNIKÓW ===

    @PostMapping("/photos/{email}")
    public ResponseEntity<String> uploadPhoto(
            @PathVariable String email,
            @RequestParam("file") MultipartFile file) {

        return employeeService.uploadEmployeePhoto(email, file);
    }

    @GetMapping("/photos/{email}")
    public ResponseEntity<org.springframework.core.io.Resource> getPhoto(@PathVariable String email) {
        return employeeService.getEmployeePhoto(email);
    }

    @DeleteMapping("/photos/{email}")
    public ResponseEntity<Void> deletePhoto(@PathVariable String email) {
        employeeService.deleteEmployeePhoto(email);
        return ResponseEntity.noContent().build();
    }

    // === PRYWATNE METODY POMOCNICZE ===

    private ResponseEntity<ImportSummary> createImportResponse(ImportSummary summary) {
        if (summary.hasErrors()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(summary);
        }
        return ResponseEntity.ok(summary);
    }
}