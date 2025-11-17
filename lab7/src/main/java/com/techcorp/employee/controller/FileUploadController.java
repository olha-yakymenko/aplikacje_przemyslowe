package com.techcorp.employee.controller;

import com.techcorp.employee.service.*;
import com.techcorp.employee.model.*;
import com.techcorp.employee.exception.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

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

//    @GetMapping("/export/csv")
//    public ResponseEntity<org.springframework.core.io.Resource> exportCsv(@RequestParam(required = false) String company) {
//        return reportGeneratorService.exportCsvReport(company);
//    }
//
//    @GetMapping("/reports/statistics/{companyName}")
//    public ResponseEntity<org.springframework.core.io.Resource> generateStatisticsReport(@PathVariable String companyName) {
//        return reportGeneratorService.exportStatisticsReport(companyName);
//    }


    @GetMapping("/export/csv")
    public ResponseEntity<org.springframework.core.io.Resource> exportCsv(@RequestParam(required = false) String company) {
        Map<String, Object> reportData = reportGeneratorService.getCsvReportData(company);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType((String) reportData.get("contentType")))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        reportData.get("contentDisposition") + "; filename=\"" + reportData.get("fileName") + "\"")
                .body((org.springframework.core.io.Resource) reportData.get("resource"));
    }

    @GetMapping("/reports/statistics/{companyName}")
    public ResponseEntity<org.springframework.core.io.Resource> generateStatisticsReport(@PathVariable String companyName) {
        Map<String, Object> reportData = reportGeneratorService.getStatisticsReportData(companyName);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType((String) reportData.get("contentType")))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        reportData.get("contentDisposition") + "; filename=\"" + reportData.get("fileName") + "\"")
                .body((org.springframework.core.io.Resource) reportData.get("resource"));
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