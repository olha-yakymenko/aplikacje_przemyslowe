package com.techcorp.employee.controller.api;

import com.techcorp.employee.model.AuditLog;
import com.techcorp.employee.service.ReportService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;

@RestController
@RequestMapping("/api/reports")
@PreAuthorize("hasRole('ADMIN')")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    /**
     * Pobierz stronicowane logi audytowe (JSON)
     */
    @GetMapping("/audit-logs")
    public Page<AuditLog> getAuditLogs(
            @PageableDefault(size = 50, sort = "eventDate", direction = Sort.Direction.DESC)
            Pageable pageable) {
        return reportService.getPagedAuditLogs(pageable);
    }

    /**
     * Generuj i pobierz raport CSV z paginacją
     */
    @GetMapping("/audit-logs/csv")
    public ResponseEntity<Resource> exportAuditLogsToCsv(
            @PageableDefault(size = 1000, sort = "eventDate", direction = Sort.Direction.DESC)
            Pageable pageable,
            @RequestParam(required = false) String filename) throws IOException {

        String reportFilename = filename != null ? filename :
                String.format("audit_logs_page_%d_%s.csv",
                        pageable.getPageNumber() + 1,
                        java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd")));

        Path csvPath = reportService.generateAuditLogCsvReport(pageable, reportFilename);

        File file = csvPath.toFile();
        InputStreamResource resource = new InputStreamResource(new FileInputStream(file));

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + file.getName() + "\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .contentLength(file.length())
                .body(resource);
    }

    /**
     * Generuj pełny raport CSV (wszystkie rekordy)
     */
    @GetMapping("/audit-logs/csv/full")
    public ResponseEntity<Resource> exportFullAuditLogsToCsv() throws IOException {
        Path csvPath = reportService.generateFullAuditLogCsvReport();

        File file = csvPath.toFile();
        InputStreamResource resource = new InputStreamResource(new FileInputStream(file));

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + file.getName() + "\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .contentLength(file.length())
                .body(resource);
    }
}