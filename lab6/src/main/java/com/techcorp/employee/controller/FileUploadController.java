package com.techcorp.employee.controller;

import com.techcorp.employee.service.*;
import com.techcorp.employee.model.*;
import com.techcorp.employee.exception.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.file.Path;
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


    @Value("${spring.servlet.multipart.max-file-size}")
    private String maxFileSize;


    @PostMapping("/import/csv")
    public ResponseEntity<ImportSummary> importCsv(@RequestParam("file") MultipartFile file) {
        ImportSummary summary = new ImportSummary();

        try {
            // 1. Walidacja pliku
            fileStorageService.validateFile(file);
            fileStorageService.validateFileType(file, new String[]{".csv"});

            // 2. DODAJ TĘ LINIĘ - walidacja rozmiaru (10MB limit)
            fileStorageService.validateFileSize(file, 10L * 1024 * 1024);

            // 3. Zapisz plik w katalogu uploads
            String filePath = fileStorageService.storeFile(file, "uploads");

            // 4. Przekaż ścieżkę do ImportService
            summary = importService.importFromCsv(filePath);

            return ResponseEntity.ok(summary);

        } catch (InvalidFileException e) {
            summary.addError("Invalid file: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(summary);
        } catch (Exception e) {
            summary.addError("Import failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(summary);
        }
    }

    @PostMapping("/import/xml")
    public ResponseEntity<ImportSummary> importXml(@RequestParam("file") MultipartFile file) {
        try {
            fileStorageService.validateFile(file);
            fileStorageService.validateFileType(file, new String[]{".xml"});

            // For XML import - similar to CSV but would need XML parser
            ImportSummary summary = new ImportSummary();
            summary.addError("XML import not yet implemented");
            return ResponseEntity.ok(summary);

        } catch (Exception e) {
            ImportSummary errorSummary = new ImportSummary();
            errorSummary.addError("Import failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorSummary);
        }
    }

    // === EKSPORT RAPORTÓW ===

    @GetMapping("/export/csv")
    public ResponseEntity<Resource> exportCsv(@RequestParam(required = false) String company) {
        try {
            Path csvPath = reportGeneratorService.generateCsvReport(company);
            Resource resource = fileStorageService.loadFileAsResource(
                    csvPath.getFileName().toString(), "reports");

            String contentType = "text/csv";
            String filename = csvPath.getFileName().toString();

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + filename + "\"")
                    .body(resource);

        } catch (Exception e) {
            throw new FileStorageException("Could not generate CSV report: " + e.getMessage(), e);
        }
    }

    @GetMapping("/reports/statistics/{companyName}")
    public ResponseEntity<Resource> generateStatisticsReport(@PathVariable String companyName) {
        try {
            Path pdfPath = reportGeneratorService.generateStatisticsPdf(companyName);
            Resource resource = fileStorageService.loadFileAsResource(
                    pdfPath.getFileName().toString(), "reports");

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + pdfPath.getFileName().toString() + "\"")
                    .body(resource);

        } catch (Exception e) {
            throw new FileStorageException("Could not generate statistics report", e);
        }
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
    public ResponseEntity<Resource> downloadDocument(
            @PathVariable String email,
            @PathVariable String documentId) {

        EmployeeDocument document = documentService.getDocument(documentId);

        if (!document.getEmployeeEmail().equalsIgnoreCase(email)) {
            throw new FileNotFoundException("Document not found for employee: " + email);
        }

        Resource resource = fileStorageService.loadFileAsResource(
                document.getFileName(), "documents/" + email);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + document.getOriginalFileName() + "\"")
                .body(resource);
    }

    @DeleteMapping("/documents/{email}/{documentId}")
    public ResponseEntity<Void> deleteDocument(
            @PathVariable String email,
            @PathVariable String documentId) {

        EmployeeDocument document = documentService.getDocument(documentId);

        if (!document.getEmployeeEmail().equalsIgnoreCase(email)) {
            throw new FileNotFoundException("Document not found for employee: " + email);
        }

        documentService.deleteDocument(documentId);
        return ResponseEntity.noContent().build();
    }

    // === ZDJĘCIA PRACOWNIKÓW ===

    @PostMapping("/photos/{email}")
    public ResponseEntity<String> uploadPhoto(
            @PathVariable String email,
            @RequestParam("file") MultipartFile file) {

        try {
            // Validate employee exists
            if (!employeeService.employeeExists(email)) {
                throw new EmployeeNotFoundException("Employee not found with email: " + email);
            }

            fileStorageService.validateFile(file);
            fileStorageService.validateImageFile(file);
            fileStorageService.validateFileSize(file, 2 * 1024 * 1024); // 2MB - TERAZ RZUCA WYJĄTEK

            // Use email as filename to ensure one photo per employee
            String fileExtension = getFileExtension(file.getOriginalFilename());
            String fileName = email.replace("@", "_").replace(".", "_") + fileExtension;

            fileStorageService.storeFile(file, "photos");

            // Update employee record
            employeeService.findEmployeeByEmail(email).ifPresent(employee -> {
                employee.setPhotoFileName(fileName);
            });

            return ResponseEntity.ok("Photo uploaded successfully: " + fileName);

        } catch (InvalidFileException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/photos/{email}")
    public ResponseEntity<Resource> getPhoto(@PathVariable String email) {
        try {
            // Find employee to get photo filename
            String photoFileName = employeeService.findEmployeeByEmail(email)
                    .map(Employee::getPhotoFileName)
                    .orElseThrow(() -> new FileNotFoundException("Employee not found or has no photo"));

            Resource resource = fileStorageService.loadFileAsResource(photoFileName, "photos");

            // Determine content type based on file extension
            String contentType = photoFileName.toLowerCase().endsWith(".png") ?
                    "image/png" : "image/jpeg";

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + photoFileName + "\"")
                    .body(resource);

        } catch (FileNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/photos/{email}")
    public ResponseEntity<Void> deletePhoto(@PathVariable String email) {
        try {
            String photoFileName = employeeService.findEmployeeByEmail(email)
                    .map(Employee::getPhotoFileName)
                    .orElse(null);

            if (photoFileName != null) {
                fileStorageService.deleteFile(photoFileName, "photos");

                // Clear photo reference in employee record
                employeeService.findEmployeeByEmail(email).ifPresent(employee -> {
                    employee.setPhotoFileName(null);
                });
            }

            return ResponseEntity.noContent().build();

        } catch (Exception e) {
            throw new FileStorageException("Could not delete photo", e);
        }
    }

    private String getFileExtension(String fileName) {
        if (fileName == null) return ".jpg";
        int lastDotIndex = fileName.lastIndexOf(".");
        return lastDotIndex > 0 ? fileName.substring(lastDotIndex) : ".jpg";
    }
}