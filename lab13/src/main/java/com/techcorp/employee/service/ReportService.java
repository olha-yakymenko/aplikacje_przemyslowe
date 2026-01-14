package com.techcorp.employee.service;

import com.techcorp.employee.model.AuditLog;
import com.techcorp.employee.repository.AuditLogReportRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ReportService {

    private final AuditLogReportRepository auditLogReportRepository;
    private final FileStorageService fileStorageService;

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String CSV_HEADER =
            "ID,Event Date,Event Type,Message,Affected Entity,Entity ID\n";

    public ReportService(
            AuditLogReportRepository auditLogReportRepository,
            FileStorageService fileStorageService) {
        this.auditLogReportRepository = auditLogReportRepository;
        this.fileStorageService = fileStorageService;
    }

    /**
     * Generuje raport CSV z logów audytowych z paginacją
     * @param pageable - stronicowanie i sortowanie
     * @param fileName - nazwa pliku (opcjonalna)
     * @return ścieżka do wygenerowanego pliku CSV
     */
    public Path generateAuditLogCsvReport(Pageable pageable, String fileName) throws IOException {
        if (fileName == null || fileName.trim().isEmpty()) {
            fileName = String.format("audit_logs_%s.csv",
                    java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")));
        }

        if (!fileName.endsWith(".csv")) {
            fileName += ".csv";
        }

        // Pobierz dane z użyciem paginacji
        Page<AuditLog> auditLogsPage = auditLogReportRepository.findAll(pageable);
        List<AuditLog> auditLogs = auditLogsPage.getContent();

        Path reportPath = fileStorageService.getReportsStorageLocation().resolve(fileName);

        try (BufferedWriter writer = Files.newBufferedWriter(reportPath,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {

            // Nagłówek CSV
            writer.write(CSV_HEADER);

            // Dane
            for (AuditLog log : auditLogs) {
                writer.write(convertToCsv(log));
                writer.newLine();
            }

            // Footer z metadanymi paginacji
            writer.write("\n# Report Metadata\n");
            writer.write(String.format("# Page: %d/%d\n",
                    pageable.getPageNumber() + 1, auditLogsPage.getTotalPages()));
            writer.write(String.format("# Page Size: %d\n", pageable.getPageSize()));
            writer.write(String.format("# Total Records: %d\n", auditLogsPage.getTotalElements()));
            writer.write(String.format("# Sort: %s\n",
                    pageable.getSort().toString().replace(": ", " ")));
            writer.write(String.format("# Generated: %s\n",
                    java.time.LocalDateTime.now().format(DATE_FORMATTER)));
        }

        return reportPath;
    }

    /**
     * Generuje pełny raport CSV (wszystkie rekordy) poprzez iterację po stronach
     */
    public Path generateFullAuditLogCsvReport() throws IOException {
        String fileName = String.format("audit_logs_full_%s.csv",
                java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")));

        Path reportPath = fileStorageService.getReportsStorageLocation().resolve(fileName);

        try (BufferedWriter writer = Files.newBufferedWriter(reportPath,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {

            writer.write(CSV_HEADER);

            int pageNumber = 0;
            int pageSize = 1000; // Optymalny rozmiar strony dla dużych zbiorów
            long totalProcessed = 0;

            Page<AuditLog> currentPage;

            do {
                Pageable pageable = org.springframework.data.domain.PageRequest.of(
                        pageNumber, pageSize,
                        org.springframework.data.domain.Sort.by("eventDate").descending()
                );

                currentPage = auditLogReportRepository.findAll(pageable);
                List<AuditLog> logs = currentPage.getContent();

                for (AuditLog log : logs) {
                    writer.write(convertToCsv(log));
                    writer.newLine();
                }

                totalProcessed += logs.size();
                pageNumber++;

                // Co 10 stron flush dla wydajności
                if (pageNumber % 10 == 0) {
                    writer.flush();
                }

            } while (currentPage.hasNext());

            writer.write("\n# Full Report Metadata\n");
            writer.write(String.format("# Total Records Exported: %d\n", totalProcessed));
            writer.write(String.format("# Generated: %s\n",
                    java.time.LocalDateTime.now().format(DATE_FORMATTER)));

        }

        return reportPath;
    }

    /**
     * Generuje raport CSV dla konkretnego typu zdarzenia
     */
    public Path generateAuditLogCsvByEventType(String eventType, Pageable pageable) throws IOException {
        String fileName = String.format("audit_logs_%s_%s.csv",
                eventType.toLowerCase().replace(" ", "_"),
                java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")));

        Path reportPath = fileStorageService.getReportsStorageLocation().resolve(fileName);

        try (BufferedWriter writer = Files.newBufferedWriter(reportPath,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {

            writer.write(CSV_HEADER);

            // Możesz dodać dedykowaną metodę w repozytorium: findByEventType(eventType, pageable)
            // Tutaj filtrujemy po pobraniu danych (dla prostoty)
            Page<AuditLog> allLogs = auditLogReportRepository.findAll(pageable);
            List<AuditLog> filteredLogs = allLogs.getContent().stream()
                    .filter(log -> eventType.equals(log.getEventType()))
                    .toList();

            for (AuditLog log : filteredLogs) {
                writer.write(convertToCsv(log));
                writer.newLine();
            }

            writer.write(String.format("\n# Filtered by event type: %s\n", eventType));
            writer.write(String.format("# Records: %d\n", filteredLogs.size()));
            writer.write(String.format("# Generated: %s\n",
                    java.time.LocalDateTime.now().format(DATE_FORMATTER)));

        }

        return reportPath;
    }

    /**
     * Pobiera stronicowane logi audytowe - czysta metoda do użycia w API
     */
    public Page<AuditLog> getPagedAuditLogs(Pageable pageable) {
        return auditLogReportRepository.findAll(pageable);
    }


    /**
     * Metoda pomocnicza - konwersja AuditLog do linii CSV
     */
    private String convertToCsv(AuditLog log) {
        return String.format("%d,%s,%s,\"%s\",%s,%s",
                log.getId(),
                log.getEventDate() != null ? log.getEventDate().format(DATE_FORMATTER) : "",
                escapeCsvField(log.getEventType()),
                escapeCsvField(log.getMessage()),
                escapeCsvField(log.getAffectedEntity()),
                log.getEntityId() != null ? log.getEntityId() : ""
        );
    }

    /**
     * Escapowanie pól CSV
     */
    private String escapeCsvField(String field) {
        if (field == null) {
            return "";
        }
        // Jeśli zawiera cudzysłowy, przecinki lub nowe linie, otaczamy cudzysłowami i duplikujemy cudzysłowy
        if (field.contains("\"") || field.contains(",") || field.contains("\n")) {
            return "\"" + field.replace("\"", "\"\"") + "\"";
        }
        return field;
    }
}