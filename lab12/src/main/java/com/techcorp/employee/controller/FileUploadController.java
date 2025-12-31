
package com.techcorp.employee.controller;

import com.techcorp.employee.service.*;
import com.techcorp.employee.model.*;
import com.techcorp.employee.exception.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/files")
@Validated  // Dodaj adnotację @Validated
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

    @PostMapping("/import/csv")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ImportSummary> importCsv(
            @RequestParam("file") @NotNull(message = "File cannot be null") MultipartFile file) {

        ImportSummary summary = importService.importCsvFile(file);
        return createImportResponse(summary);
    }

    @PostMapping("/import/xml")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ImportSummary> importXml(
            @RequestParam("file") @NotNull(message = "File cannot be null") MultipartFile file) {

        ImportSummary summary = importService.importXmlFile(file);
        return createImportResponse(summary);
    }

    @GetMapping("/export/csv")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<org.springframework.core.io.Resource> exportCsv(
            @RequestParam(required = false) String company) {

        Map<String, Object> reportData = reportGeneratorService.getCsvReportData(company);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType((String) reportData.get("contentType")))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        reportData.get("contentDisposition") + "; filename=\"" + reportData.get("fileName") + "\"")
                .body((org.springframework.core.io.Resource) reportData.get("resource"));
    }

    @GetMapping("/reports/statistics/{companyName}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<org.springframework.core.io.Resource> generateStatisticsReport(
            @PathVariable @NotBlank(message = "Company name cannot be blank") String companyName) {

        Map<String, Object> reportData = reportGeneratorService.getStatisticsReportData(companyName);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType((String) reportData.get("contentType")))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        reportData.get("contentDisposition") + "; filename=\"" + reportData.get("fileName") + "\"")
                .body((org.springframework.core.io.Resource) reportData.get("resource"));
    }

    @PostMapping("/documents/{email}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EmployeeDocument> uploadDocument(
            @PathVariable @NotBlank(message = "Email cannot be blank") String email,
            @RequestParam("file") @NotNull(message = "File cannot be null") MultipartFile file,
            @RequestParam("type") @NotNull(message = "Document type cannot be null") DocumentType documentType) {

        EmployeeDocument document = documentService.storeDocument(email, file, documentType);
        return ResponseEntity.status(HttpStatus.CREATED).body(document);
    }

    @GetMapping("/documents/{email}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<EmployeeDocument>> getEmployeeDocuments(
            @PathVariable @NotBlank(message = "Email cannot be blank") String email) {

        List<EmployeeDocument> documents = documentService.getEmployeeDocuments(email);
        return ResponseEntity.ok(documents);
    }

    @GetMapping("/documents/{email}/{documentId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<org.springframework.core.io.Resource> downloadDocument(
            @PathVariable @NotBlank(message = "Email cannot be blank") String email,
            @PathVariable @NotBlank(message = "Document ID cannot be blank") String documentId) {

        return documentService.downloadDocument(email, documentId);
    }

    @DeleteMapping("/documents/{email}/{documentId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteDocument(
            @PathVariable @NotBlank(message = "Email cannot be blank") String email,
            @PathVariable @NotBlank(message = "Document ID cannot be blank") String documentId) {

        documentService.deleteDocument(email, documentId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/photos/{email}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> uploadPhoto(
            @PathVariable @NotBlank(message = "Email cannot be blank") String email,
            @RequestParam("file") @NotNull(message = "File cannot be null") MultipartFile file) {

        return employeeService.uploadEmployeePhoto(email, file);
    }

    @GetMapping("/photos/{email}")
    public ResponseEntity<org.springframework.core.io.Resource> getPhoto(
            @PathVariable @NotBlank(message = "Email cannot be blank") String email) {

        return employeeService.getEmployeePhoto(email);
    }

    @DeleteMapping("/photos/{email}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deletePhoto(
            @PathVariable @NotBlank(message = "Email cannot be blank") String email) {

        employeeService.deleteEmployeePhoto(email);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/department-documents/{departmentId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> uploadDepartmentDocument(
            @PathVariable @Min(value = 1, message = "Department ID must be greater than 0") Long departmentId,
            @RequestParam("file") @NotNull(message = "File cannot be null") MultipartFile file) {

        try {
            String fileName = fileStorageService.storeDepartmentDocument(file, departmentId);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body("Document uploaded successfully: " + fileName);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error uploading document: " + e.getMessage());
        }
    }

    @GetMapping("/department-documents/{departmentId}")
    public ResponseEntity<List<String>> getDepartmentDocuments(
            @PathVariable @Min(value = 1, message = "Department ID must be greater than 0") Long departmentId) {

        try {
            List<String> documentNames = fileStorageService.getDepartmentDocumentNames(departmentId);
            return ResponseEntity.ok(documentNames);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(List.of());
        }
    }

    @GetMapping("/department-documents/{departmentId}/{fileName}")
    public ResponseEntity<Resource> downloadDepartmentDocument(
            @PathVariable @Min(value = 1, message = "Department ID must be greater than 0") Long departmentId,
            @PathVariable @NotBlank(message = "File name cannot be blank") String fileName) {

        try {
            Resource resource = fileStorageService.loadDepartmentDocument(fileName, departmentId);

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + fileName + "\"")
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/department-documents/{departmentId}/{fileName}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteDepartmentDocument(
            @PathVariable @Min(value = 1, message = "Department ID must be greater than 0") Long departmentId,
            @PathVariable @NotBlank(message = "File name cannot be blank") String fileName) {

        try {
            boolean deleted = fileStorageService.deleteDepartmentDocument(fileName, departmentId);
            if (deleted) {
                return ResponseEntity.noContent().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    private ResponseEntity<ImportSummary> createImportResponse(ImportSummary summary) {
        if (summary.hasErrors()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(summary);
        }
        return ResponseEntity.ok(summary);
    }
}