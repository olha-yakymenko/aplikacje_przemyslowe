package com.techcorp.employee.controller.api;

import com.techcorp.employee.model.AuditLog;
import com.techcorp.employee.repository.AuditLogRepository;
import com.techcorp.employee.service.ReportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.containsString;


@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ReportControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReportService reportService;

    @Autowired
    private AuditLogRepository auditLogRepository;

    private List<AuditLog> testLogs;

    @BeforeEach
    void setUp() {
        // Clean up before each test
        auditLogRepository.deleteAll();

        // Create test data
        testLogs = Arrays.asList(
                AuditLog.builder()
                        .eventType("SALARY_UPDATE_SUCCESS")
                        .message("Test message 1")
                        .affectedEntity("Employee")
                        .entityId(1L)
                        .eventDate(LocalDateTime.now())
                        .build(),
                AuditLog.builder()
                        .eventType("SECURITY")
                        .message("Test message 2")
                        .affectedEntity("User")
                        .entityId(2L)
                        .eventDate(LocalDateTime.now().minusHours(1))
                        .build()
        );

        auditLogRepository.saveAll(testLogs);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAuditLogs_IntegrationTest() throws Exception {
        // Given
        Page<AuditLog> page = new PageImpl<>(testLogs, PageRequest.of(0, 10), testLogs.size());
        when(reportService.getPagedAuditLogs(any(Pageable.class))).thenReturn(page);

        // When & Then
        mockMvc.perform(get("/api/reports/audit-logs")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.totalElements", is(2)))
                .andExpect(jsonPath("$.content[0].eventType", is("SALARY_UPDATE_SUCCESS")))
                .andExpect(jsonPath("$.content[1].eventType", is("SECURITY")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAuditLogs_ShouldReturnDefaultPageSize_WhenNoSizeParameter() throws Exception {
        // Given
        Page<AuditLog> page = new PageImpl<>(testLogs, PageRequest.of(0, 50), testLogs.size());
        when(reportService.getPagedAuditLogs(any(Pageable.class))).thenReturn(page);

        // When & Then
        mockMvc.perform(get("/api/reports/audit-logs")
                        .param("page", "0")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)));

        verify(reportService, times(1)).getPagedAuditLogs(
                argThat(pageable -> pageable.getPageSize() == 50)
        );
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAuditLogs_ShouldSortByEventDateDesc_WhenNoSortParameter() throws Exception {
        // Given
        Page<AuditLog> page = new PageImpl<>(testLogs, PageRequest.of(0, 50), testLogs.size());
        when(reportService.getPagedAuditLogs(any(Pageable.class))).thenReturn(page);

        // When & Then
        mockMvc.perform(get("/api/reports/audit-logs")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(reportService, times(1)).getPagedAuditLogs(
                argThat(pageable ->
                        pageable.getSort().getOrderFor("eventDate") != null &&
                                pageable.getSort().getOrderFor("eventDate").getDirection().isDescending()
                )
        );
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void exportAuditLogsToCsv_IntegrationTest() throws Exception {
        // Given
        Path tempFile = Files.createTempFile("test-report", ".csv");
        Files.write(tempFile, "id,event_type,message\n1,TEST,Test message".getBytes());

        when(reportService.generateAuditLogCsvReport(any(Pageable.class), anyString()))
                .thenReturn(tempFile);

        // When
        MvcResult result = mockMvc.perform(get("/api/reports/audit-logs/csv")
                        .param("filename", "test.csv")
                        .param("page", "0")
                        .param("size", "1000")
                        .param("sort", "eventDate,desc"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", containsString("test-")))
                .andExpect(content().contentTypeCompatibleWith("text/csv"))
                .andReturn();

        // Then
        MockHttpServletResponse response = result.getResponse();
        assertThat(response.getContentAsString()).contains("Test message");
        assertThat(response.getHeader("Content-Length")).isNotNull();

        // Clean up
        Files.deleteIfExists(tempFile);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void exportAuditLogsToCsv_ShouldUseDefaultFilename_WhenNotProvided() throws Exception {
        // Given
        Path tempFile = Files.createTempFile("default-report", ".csv");
        Files.write(tempFile, "test".getBytes());

        when(reportService.generateAuditLogCsvReport(any(Pageable.class), anyString()))
                .thenReturn(tempFile);

        // When & Then
        mockMvc.perform(get("/api/reports/audit-logs/csv"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", containsString("default-report")));

        // Clean up
        Files.deleteIfExists(tempFile);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void exportFullAuditLogsToCsv_IntegrationTest() throws Exception {
        // Given
        Path tempFile = Files.createTempFile("full-report", ".csv");
        Files.write(tempFile, "id,event_type,message\n1,FULL,Full report".getBytes());

        when(reportService.generateFullAuditLogCsvReport()).thenReturn(tempFile);

        // When
        MvcResult result = mockMvc.perform(get("/api/reports/audit-logs/csv/full"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/csv"))
                .andExpect(header().exists("Content-Disposition"))
                .andExpect(header().string("Content-Disposition", containsString(".csv")))
                .andReturn();

        // Then
        assertThat(result.getResponse().getContentAsString()).contains("Full report");

        // Clean up
        Files.deleteIfExists(tempFile);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void exportFullAuditLogsToCsv_ShouldHandleServiceException() throws Exception {
        // Given
        when(reportService.generateFullAuditLogCsvReport())
                .thenThrow(new RuntimeException("File generation failed"));

        // When & Then
        mockMvc.perform(get("/api/reports/audit-logs/csv/full"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser(roles = "USER")
    void accessDenied_WhenUserNotAdmin() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/reports/audit-logs"))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/reports/audit-logs/csv"))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/reports/audit-logs/csv/full"))
                .andExpect(status().isForbidden());
    }

    @Test
    void unauthenticatedAccess_ShouldReturnUnauthorized() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/reports/audit-logs"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAuditLogs_ShouldHandleCustomSorting() throws Exception {
        // Given
        Page<AuditLog> page = new PageImpl<>(testLogs, PageRequest.of(0, 10), testLogs.size());
        when(reportService.getPagedAuditLogs(any(Pageable.class))).thenReturn(page);

        // When & Then
        mockMvc.perform(get("/api/reports/audit-logs")
                        .param("sort", "message,asc")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(reportService, times(1)).getPagedAuditLogs(
                argThat(pageable ->
                        pageable.getSort().getOrderFor("message") != null &&
                                pageable.getSort().getOrderFor("message").getDirection().isAscending()
                )
        );
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void exportAuditLogsToCsv_ShouldHandleIOException() throws Exception {
        // Given
        when(reportService.generateAuditLogCsvReport(any(Pageable.class), anyString()))
                .thenThrow(new RuntimeException("IO Error"));

        // When & Then
        mockMvc.perform(get("/api/reports/audit-logs/csv"))
                .andExpect(status().isInternalServerError());
    }
}