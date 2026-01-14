package com.techcorp.employee.service;

import com.techcorp.employee.model.AuditLog;
import com.techcorp.employee.repository.AuditLogReportRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private AuditLogReportRepository auditLogReportRepository;

    @Mock
    private FileStorageService fileStorageService;

    @InjectMocks
    private ReportService reportService;

    @TempDir
    Path tempDir;

    private List<AuditLog> logs;

    @BeforeEach
    void setUp() {
        logs = List.of(
                AuditLog.builder()
                        .id(1L)
                        .eventType("LOGIN")
                        .message("User logged in")
                        .affectedEntity("User")
                        .entityId(10L)
                        .eventDate(LocalDateTime.now())
                        .build(),
                AuditLog.builder()
                        .id(2L)
                        .eventType("UPDATE")
                        .message("Data updated")
                        .affectedEntity("Employee")
                        .entityId(20L)
                        .eventDate(LocalDateTime.now().minusHours(1))
                        .build()
        );
    }

    // -------------------------------------------------
    // getPagedAuditLogs
    // -------------------------------------------------

    @Test
    void getPagedAuditLogs_ShouldReturnPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<AuditLog> page = new PageImpl<>(logs, pageable, logs.size());

        when(auditLogReportRepository.findAll(pageable)).thenReturn(page);

        Page<AuditLog> result = reportService.getPagedAuditLogs(pageable);

        assertThat(result.getContent()).hasSize(2);
    }

    // -------------------------------------------------
    // generateAuditLogCsvReport
    // -------------------------------------------------

    @Test
    void generateAuditLogCsvReport_ShouldCreateCsvFile() throws Exception {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("eventDate").descending());
        Page<AuditLog> page = new PageImpl<>(logs, pageable, logs.size());

        when(fileStorageService.getReportsStorageLocation()).thenReturn(tempDir);
        when(auditLogReportRepository.findAll(any(Pageable.class))).thenReturn(page);

        Path result = reportService.generateAuditLogCsvReport(pageable, "test.csv");

        assertAll(
                () -> assertThat(result).exists(),
                () -> assertThat(Files.readString(result)).contains("LOGIN")
        );
    }

    @Test
    void generateAuditLogCsvReport_ShouldGenerateDefaultFilename_WhenNull() throws Exception {
        Pageable pageable = PageRequest.of(0, 5);
        Page<AuditLog> page = new PageImpl<>(logs, pageable, logs.size());

        when(fileStorageService.getReportsStorageLocation()).thenReturn(tempDir);
        when(auditLogReportRepository.findAll(any(Pageable.class))).thenReturn(page);

        Path result = reportService.generateAuditLogCsvReport(pageable, null);

        assertThat(result.getFileName().toString().startsWith("audit_logs_"));
    }

    // -------------------------------------------------
    // generateFullAuditLogCsvReport
    // -------------------------------------------------

    @Test
    void generateFullAuditLogCsvReport_ShouldExportAllPages() throws Exception {
        Page<AuditLog> page = new PageImpl<>(logs, PageRequest.of(0, 1000), logs.size());

        when(fileStorageService.getReportsStorageLocation()).thenReturn(tempDir);
        when(auditLogReportRepository.findAll(any(Pageable.class))).thenReturn(page);

        Path result = reportService.generateFullAuditLogCsvReport();

        assertAll(
                () -> assertThat(result).exists(),
                () -> assertThat(Files.readString(result)).contains("Full Report Metadata")
        );
    }

    // -------------------------------------------------
    // generateAuditLogCsvByEventType
    // -------------------------------------------------

    @Test
    void generateAuditLogCsvByEventType_ShouldFilterCorrectly() throws Exception {
        Pageable pageable = PageRequest.of(0, 10);
        Page<AuditLog> page = new PageImpl<>(logs, pageable, logs.size());

        when(fileStorageService.getReportsStorageLocation()).thenReturn(tempDir);
        when(auditLogReportRepository.findAll(pageable)).thenReturn(page);

        Path result = reportService.generateAuditLogCsvByEventType("LOGIN", pageable);

        assertThat(Files.readString(result).contains("LOGIN")
                && !Files.readString(result).contains("UPDATE"));
    }



    @Test
    void generateAuditLogCsvReport_ShouldCreateValidCsvFormat() throws Exception {
        Pageable pageable = PageRequest.of(0, 10);
        Page<AuditLog> page = new PageImpl<>(logs, pageable, logs.size());

        when(fileStorageService.getReportsStorageLocation()).thenReturn(tempDir);
        when(auditLogReportRepository.findAll(pageable)).thenReturn(page);

        Path reportPath = reportService.generateAuditLogCsvReport(pageable, "format_test.csv");
        String csvContent = Files.readString(reportPath);

        assertAll(
                () -> assertThat(csvContent).contains("ID,Event Date,Event Type,Message,Affected Entity,Entity ID"),
                () -> assertThat(csvContent).contains("LOGIN"),
                () -> assertThat(csvContent).contains("UPDATE"),
                () -> assertThat(csvContent).contains("User logged in"),
                () -> assertThat(csvContent).contains("Page: 1/1"),
                () -> assertThat(csvContent).contains("Total Records: 2")
        );

        // Sprawdź linie CSV
        String[] lines = csvContent.split("\n");
        assertThat(lines[0]).isEqualTo("ID,Event Date,Event Type,Message,Affected Entity,Entity ID");
        assertThat(lines.length).isGreaterThan(2); // Nagłówek + dane + metadata
    }


    @Test
    void simpleDebugTest() throws Exception {
        Pageable pageable = PageRequest.of(0, 10);
        Page<AuditLog> page = new PageImpl<>(logs, pageable, logs.size());

        Path testDir = tempDir.resolve("reports");
        Files.createDirectories(testDir);

        when(fileStorageService.getReportsStorageLocation()).thenReturn(testDir);
        when(auditLogReportRepository.findAll(pageable)).thenReturn(page);

        Path result = reportService.generateAuditLogCsvReport(pageable, "debug.csv");

        // Wypisz wszystko
        System.out.println("=== DEBUG INFO ===");
        System.out.println("Temp directory: " + tempDir.toAbsolutePath());
        System.out.println("Reports directory: " + testDir.toAbsolutePath());
        System.out.println("Generated file: " + result.toAbsolutePath());
        System.out.println("File exists: " + Files.exists(result));
        System.out.println("File size: " + Files.size(result) + " bytes");

        if (Files.exists(result)) {
            System.out.println("=== FILE CONTENT ===");
            System.out.println(Files.readString(result));
        }
    }
}
