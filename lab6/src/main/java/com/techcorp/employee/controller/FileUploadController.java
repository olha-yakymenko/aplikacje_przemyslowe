package com.techcorp.employee.controller;

import com.techcorp.employee.service.*;
import com.techcorp.employee.model.*;
import com.techcorp.employee.exception.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.file.Files;
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
            fileStorageService.validateFileSize(file, 10L * 1024 * 1024);

            // 2. POPRAWKA: Zapisz plik i uzyskaj pełną ścieżkę
            String fileName = fileStorageService.storeFile(file, "uploads");
            Path uploadsDir = fileStorageService.getFileStorageLocation().resolve("uploads");
            String filePath = uploadsDir.resolve(fileName).toString();

            // 3. Przekaż ścieżkę do ImportService
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

//    @PostMapping("/import/xml")
//    public ResponseEntity<ImportSummary> importXml(@RequestParam("file") MultipartFile file) {
//        try {
//            fileStorageService.validateFile(file);
//            fileStorageService.validateFileType(file, new String[]{".xml"});
//
//            // For XML import - similar to CSV but would need XML parser
//            ImportSummary summary = new ImportSummary();
//            summary.addError("XML import not yet implemented");
//            return ResponseEntity.ok(summary);
//
//        } catch (Exception e) {
//            ImportSummary errorSummary = new ImportSummary();
//            errorSummary.addError("Import failed: " + e.getMessage());
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorSummary);
//        }
//    }



    @PostMapping("/import/xml")
    public ResponseEntity<ImportSummary> importXml(@RequestParam("file") MultipartFile file) {
        ImportSummary summary = new ImportSummary();

        try {
            // 1. Walidacja pliku
            fileStorageService.validateFile(file);
            fileStorageService.validateFileType(file, new String[]{".xml"});
            fileStorageService.validateFileSize(file, 10L * 1024 * 1024);

            // 2. Zapisz plik w katalogu uploads
            String fileName = fileStorageService.storeFile(file, "uploads");

            // 3. POPRAWKA: Utwórz pełną ścieżkę do pliku
            Path uploadsDir = fileStorageService.getFileStorageLocation().resolve("uploads");
            String filePath = uploadsDir.resolve(fileName).toString();

            // 4. Przekaż ścieżkę do ImportService
            summary = importService.importFromXml(filePath);

            return ResponseEntity.ok(summary);

        } catch (InvalidFileException e) {
            summary.addError("Invalid file: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(summary);
        } catch (Exception e) {
            summary.addError("XML import failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(summary);
        }
    }

    // === EKSPORT RAPORTÓW ===

//    @GetMapping("/export/csv")
//    public ResponseEntity<Resource> exportCsv(@RequestParam(required = false) String company) {
//        try {
//            System.out.println("=== GENERATING CSV REPORT ===");
//            System.out.println("Company filter: " + company);
//
//            // 1. Generuj CSV
//            Path csvPath = reportGeneratorService.generateCsvReport(company);
//            System.out.println("CSV path: " + csvPath.toAbsolutePath());
//            System.out.println("CSV exists: " + Files.exists(csvPath));
//
//            // 2. POPRAWKA: Użyj bezpośredniej ścieżki zamiast loadFileAsResource
//            Resource resource = new UrlResource(csvPath.toUri());
//            System.out.println("Resource exists: " + resource.exists());
//
//            if (!resource.exists()) {
//                throw new FileNotFoundException("CSV file not found at: " + csvPath.toAbsolutePath());
//            }
//
//            // 3. Ustaw poprawne nagłówki
//            String contentType = "text/csv";
//            String filename = csvPath.getFileName().toString();
//
//            System.out.println("Returning CSV: " + filename);
//            return ResponseEntity.ok()
//                    .contentType(MediaType.parseMediaType(contentType))
//                    .header(HttpHeaders.CONTENT_DISPOSITION,
//                            "attachment; filename=\"" + filename + "\"")
//                    .body(resource);
//
//        } catch (Exception e) {
//            System.err.println("ERROR in CSV export: " + e.getMessage());
//            e.printStackTrace();
//            throw new FileStorageException("Could not generate CSV report: " + e.getMessage(), e);
//        }
//    }

    @GetMapping("/export/csv")
    public ResponseEntity<Resource> exportCsv(@RequestParam(required = false) String company) {
        try {
            System.out.println("=== GENERATING CSV REPORT ===");
            System.out.println("Company filter: " + company);

            // 1. Generuj CSV
            Path csvPath = reportGeneratorService.generateCsvReport(company);
            System.out.println("CSV path: " + csvPath.toAbsolutePath());
            System.out.println("CSV exists: " + Files.exists(csvPath));

            // 2. POPRAWKA: Sprawdź czy plik został zapisany w reports/
            if (!Files.exists(csvPath)) {
                // Spróbuj znaleźć plik w katalogu reports
                Path reportsDir = fileStorageService.getReportsStorageLocation();
                String fileName = csvPath.getFileName().toString();
                Path alternativePath = reportsDir.resolve(fileName);

                System.out.println("Trying alternative path: " + alternativePath.toAbsolutePath());

                if (Files.exists(alternativePath)) {
                    csvPath = alternativePath;
                    System.out.println("Found CSV at alternative location");
                } else {
                    throw new FileNotFoundException("CSV file not found at: " + csvPath.toAbsolutePath() + " or " + alternativePath.toAbsolutePath());
                }
            }

            // 3. Użyj bezpośredniej ścieżki
            Resource resource = new UrlResource(csvPath.toUri());
            System.out.println("Resource exists: " + resource.exists());

            if (!resource.exists()) {
                throw new FileNotFoundException("CSV file not found at: " + csvPath.toAbsolutePath());
            }

            // 4. Ustaw poprawne nagłówki
            String contentType = "text/csv";
            String filename = csvPath.getFileName().toString();

            System.out.println("Returning CSV: " + filename);
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + filename + "\"")
                    .body(resource);

        } catch (Exception e) {
            System.err.println("ERROR in CSV export: " + e.getMessage());
            e.printStackTrace();
            throw new FileStorageException("Could not generate CSV report: " + e.getMessage(), e);
        }
    }

//    @GetMapping("/reports/statistics/{companyName}")
//    public ResponseEntity<Resource> generateStatisticsReport(@PathVariable String companyName) {
//        try {
//            System.out.println("AAAAAA");
//            Path pdfPath = reportGeneratorService.generateStatisticsPdf(companyName);
//            Resource resource = fileStorageService.loadFileAsResource(
//                    pdfPath.getFileName().toString(), "reports");
//
//            return ResponseEntity.ok()
//                    .contentType(MediaType.APPLICATION_PDF)
//                    .header(HttpHeaders.CONTENT_DISPOSITION,
//                            "attachment; filename=\"" + pdfPath.getFileName().toString() + "\"")
//                    .body(resource);
//
//        } catch (Exception e) {
//            throw new FileStorageException("Could not generate statistics report", e);
//        }
//    }

    @GetMapping("/reports/statistics/{companyName}")
    public ResponseEntity<Resource> generateStatisticsReport(@PathVariable String companyName) {
        try {
            System.out.println("=== START GENERATING REPORT ===");
            System.out.println("Company: " + companyName);

            // 1. Generuj PDF
            Path pdfPath = reportGeneratorService.generateStatisticsPdf(companyName);
            System.out.println("PDF path: " + pdfPath.toAbsolutePath());
            System.out.println("PDF exists: " + Files.exists(pdfPath));

            // 2. POPRAWKA: Użyj bezpośrednio ścieżki zamiast loadFileAsResource
            Resource resource = new UrlResource(pdfPath.toUri());
            System.out.println("Resource created from: " + pdfPath.toUri());
            System.out.println("Resource exists: " + resource.exists());

            if (!resource.exists()) {
                throw new FileNotFoundException("PDF file not found at: " + pdfPath.toAbsolutePath());
            }

            // 3. Zwróć response
            System.out.println("Returning PDF response...");
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + pdfPath.getFileName().toString() + "\"")
                    .body(resource);

        } catch (Exception e) {
            System.err.println("ERROR in controller: " + e.getMessage());
            e.printStackTrace();
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
            System.out.println("=== UPLOAD PHOTO ===");
            System.out.println("Email: " + email);
            System.out.println("Original filename: " + file.getOriginalFilename());

            // Validate employee exists
            if (!employeeService.employeeExists(email)) {
                throw new EmployeeNotFoundException("Employee not found with email: " + email);
            }

            fileStorageService.validateFile(file);
            fileStorageService.validateImageFile(file);
            fileStorageService.validateFileSize(file, 2 * 1024 * 1024); // 2MB

            // Use email as filename to ensure one photo per employee
            String fileExtension = getFileExtension(file.getOriginalFilename());
            String customFileName = email.replace("@", "_").replace(".", "_") + fileExtension;

            System.out.println("Custom filename: " + customFileName);

            // ✅ UŻYJ NOWEJ METODY z custom nazwą
            String savedFileName = fileStorageService.storeFileWithCustomName(file, "photos", customFileName);

            System.out.println("Actually saved as: " + savedFileName);

            // Update employee record
            employeeService.findEmployeeByEmail(email).ifPresent(employee -> {
                employee.setPhotoFileName(savedFileName);
                System.out.println("Updated employee photo filename to: " + savedFileName);
            });

            return ResponseEntity.ok("Photo uploaded successfully: " + savedFileName);

        } catch (InvalidFileException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/photos/{email}")
    public ResponseEntity<Resource> getPhoto(@PathVariable String email) {
        try {
            System.out.println("=== GET PHOTO ===");
            System.out.println("Email: " + email);

            // Find employee to get photo filename
            String photoFileName = employeeService.findEmployeeByEmail(email)
                    .map(Employee::getPhotoFileName)
                    .orElseThrow(() -> {
                        System.out.println("Employee not found or has no photo");
                        return new FileNotFoundException("Employee not found or has no photo");
                    });

            System.out.println("Photo filename from DB: " + photoFileName);

            // ✅ Użyj bezpośredniej ścieżki
            Path photosDir = fileStorageService.getPhotosStorageLocation();
            Path photoPath = photosDir.resolve(photoFileName);
            System.out.println("Full photo path: " + photoPath.toAbsolutePath());
            System.out.println("Photo exists: " + Files.exists(photoPath));

            Resource resource = new UrlResource(photoPath.toUri());
            System.out.println("Resource exists: " + resource.exists());

            if (!resource.exists()) {
                System.err.println("Photo file not found on disk");
                return ResponseEntity.notFound().build();
            }

            // Determine content type
            String contentType = photoFileName.toLowerCase().endsWith(".png") ?
                    "image/png" : "image/jpeg";

            System.out.println("Returning photo with Content-Type: " + contentType);

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + photoFileName + "\"")
                    .body(resource);

        } catch (FileNotFoundException e) {
            System.err.println("Photo not found: " + e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            System.err.println("Error getting photo: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
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