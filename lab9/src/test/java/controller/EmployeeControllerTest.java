package com.techcorp.employee.controller;

import com.techcorp.employee.dto.EmployeeDTO;
import com.techcorp.employee.dto.EmployeeListView;
import com.techcorp.employee.exception.DuplicateEmailException;
import com.techcorp.employee.exception.EmployeeNotFoundException;
import com.techcorp.employee.model.Employee;
import com.techcorp.employee.model.EmploymentStatus;
import com.techcorp.employee.model.Position;
import com.techcorp.employee.service.EmployeeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EmployeeController.class)
class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmployeeService employeeService;

    @Autowired
    private ObjectMapper objectMapper;

    private Employee testEmployee;
    private EmployeeDTO testEmployeeDTO;

    @BeforeEach
    void setUp() {
        testEmployee = new Employee(
                "Jan Kowalski",
                "jan@example.com",
                "TechCorp",
                Position.PROGRAMMER,
                8000.0,
                EmploymentStatus.ACTIVE
        );
        testEmployee.setId(1L); // Dodaj ID dla testów update

        testEmployeeDTO = new EmployeeDTO(
                "Jan",
                "Kowalski",
                "jan@example.com",
                "TechCorp",
                Position.PROGRAMMER,
                8000.0,
                EmploymentStatus.ACTIVE
        );
    }

    // ===== GET TESTS =====

    @Test
    void getEmployeeByEmail_ExistingEmployee_ShouldReturnEmployee() throws Exception {
        // Given
        when(employeeService.findEmployeeByEmail("jan@example.com"))
                .thenReturn(Optional.of(testEmployee));

        // When & Then
        mockMvc.perform(get("/api/employees/jan@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Jan"))
                .andExpect(jsonPath("$.lastName").value("Kowalski"))
                .andExpect(jsonPath("$.email").value("jan@example.com"));

        verify(employeeService, times(1)).findEmployeeByEmail("jan@example.com");
    }

    @Test
    void getEmployeeByEmail_NonExistingEmployee_ShouldReturn404() throws Exception {
        // Given
        when(employeeService.findEmployeeByEmail("nonexistent@example.com"))
                .thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/employees/nonexistent@example.com"))
                .andExpect(status().isNotFound());

        verify(employeeService, times(1)).findEmployeeByEmail("nonexistent@example.com");
    }

    @Test
    void getEmployeesByStatus_ShouldReturnPagedResults() throws Exception {
        // Given
        Pageable pageable = PageRequest.of(0, 20);

        // Tworzymy anonimową implementację interfejsu EmployeeListView
        EmployeeListView employeeView = new EmployeeListView() {
            @Override
            public String getName() { return "Jan Kowalski"; }
            @Override
            public String getEmail() { return "jan@example.com"; }
            @Override
            public String getCompany() { return "TechCorp"; }
            @Override
            public String getPosition() { return "PROGRAMMER"; }
            @Override
            public Double getSalary() { return 8000.0; }
            @Override
            public EmploymentStatus getStatus() { return EmploymentStatus.ACTIVE; }
            @Override
            public String getDepartmentName() { return "IT"; }
        };

        Page<EmployeeListView> employeesPage = new PageImpl<>(Arrays.asList(employeeView), pageable, 1);

        when(employeeService.getEmployeesByStatusProjection(eq(EmploymentStatus.ACTIVE), any(Pageable.class)))
                .thenReturn(employeesPage);

        // When & Then
        mockMvc.perform(get("/api/employees/status/ACTIVE")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Jan Kowalski"));

        verify(employeeService, times(1)).getEmployeesByStatusProjection(eq(EmploymentStatus.ACTIVE), any(Pageable.class));
    }

    @Test
    void getEmployeesByCompany_ShouldReturnPagedResults() throws Exception {
        // Given
        Pageable pageable = PageRequest.of(0, 20);

        EmployeeListView employeeView = new EmployeeListView() {
            @Override
            public String getName() { return "Jan Kowalski"; }
            @Override
            public String getEmail() { return "jan@example.com"; }
            @Override
            public String getCompany() { return "TechCorp"; }
            @Override
            public String getPosition() { return "PROGRAMMER"; }
            @Override
            public Double getSalary() { return 8000.0; }
            @Override
            public EmploymentStatus getStatus() { return EmploymentStatus.ACTIVE; }
            @Override
            public String getDepartmentName() { return "IT"; }
        };

        Page<EmployeeListView> employeesPage = new PageImpl<>(Arrays.asList(employeeView), pageable, 1);

        when(employeeService.getEmployeesByCompanyProjection(eq("TechCorp"), any(Pageable.class)))
                .thenReturn(employeesPage);

        // When & Then
        mockMvc.perform(get("/api/employees/company/TechCorp")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1));

        verify(employeeService, times(1)).getEmployeesByCompanyProjection(eq("TechCorp"), any(Pageable.class));
    }

    // ===== POST TESTS =====

    @Test
    void createEmployee_ValidEmployee_ShouldReturn201() throws Exception {
        // Given
        when(employeeService.saveEmployee(any(Employee.class))).thenReturn(testEmployee);

        // When & Then
        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testEmployeeDTO)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.firstName").value("Jan"));

        verify(employeeService, times(1)).saveEmployee(any(Employee.class));
    }

    @Test
    void createEmployee_DuplicateEmail_ShouldReturn409() throws Exception {
        // Given
        when(employeeService.saveEmployee(any(Employee.class)))
                .thenThrow(new DuplicateEmailException("Employee with email jan@example.com already exists"));

        // When & Then
        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testEmployeeDTO)))
                .andExpect(status().isConflict());

        verify(employeeService, times(1)).saveEmployee(any(Employee.class));
    }

    @Test
    void createEmployee_ServiceReturnsNull_ShouldReturn500() throws Exception {
        // Given
        when(employeeService.saveEmployee(any(Employee.class))).thenReturn(null);

        // When & Then
        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testEmployeeDTO)))
                .andExpect(status().isInternalServerError());

        verify(employeeService, times(1)).saveEmployee(any(Employee.class));
    }

    // ===== PUT TESTS =====

    @Test
    void updateEmployee_ExistingEmployee_ShouldReturn200() throws Exception {
        // Given
        when(employeeService.findEmployeeByEmail("jan@example.com"))
                .thenReturn(Optional.of(testEmployee));
        when(employeeService.saveEmployee(any(Employee.class)))
                .thenReturn(testEmployee);

        // When & Then
        mockMvc.perform(put("/api/employees/jan@example.com")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testEmployeeDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Jan"));

        verify(employeeService, times(1)).findEmployeeByEmail("jan@example.com");
        verify(employeeService, times(1)).saveEmployee(any(Employee.class));
    }

    @Test
    void updateEmployee_EmailMismatch_ShouldReturn400() throws Exception {
        // Given
        EmployeeDTO mismatchedDTO = new EmployeeDTO(
                "Jan", "Kowalski", "different@example.com",
                "TechCorp", Position.PROGRAMMER, 8000.0, EmploymentStatus.ACTIVE
        );

        // When & Then
        mockMvc.perform(put("/api/employees/jan@example.com")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mismatchedDTO)))
                .andExpect(status().isBadRequest());

        verify(employeeService, never()).findEmployeeByEmail(anyString());
        verify(employeeService, never()).saveEmployee(any(Employee.class));
    }

    @Test
    void updateEmployee_NonExistingEmployee_ShouldReturn404() throws Exception {
        // Given
        when(employeeService.findEmployeeByEmail("nonexistent@example.com"))
                .thenReturn(Optional.empty());

        EmployeeDTO dtoWithMatchingEmail = new EmployeeDTO(
                "Jan", "Kowalski", "nonexistent@example.com",
                "TechCorp", Position.PROGRAMMER, 8000.0, EmploymentStatus.ACTIVE
        );

        // When & Then
        mockMvc.perform(put("/api/employees/nonexistent@example.com")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dtoWithMatchingEmail)))
                .andExpect(status().isNotFound());

        verify(employeeService, times(1)).findEmployeeByEmail("nonexistent@example.com");
        verify(employeeService, never()).saveEmployee(any(Employee.class));
    }

    @Test
    void updateEmployee_ServiceReturnsNull_ShouldReturn500() throws Exception {
        // Given
        when(employeeService.findEmployeeByEmail("jan@example.com"))
                .thenReturn(Optional.of(testEmployee));
        when(employeeService.saveEmployee(any(Employee.class)))
                .thenReturn(null);

        // When & Then
        mockMvc.perform(put("/api/employees/jan@example.com")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testEmployeeDTO)))
                .andExpect(status().isInternalServerError());

        verify(employeeService, times(1)).findEmployeeByEmail("jan@example.com");
        verify(employeeService, times(1)).saveEmployee(any(Employee.class));
    }

    // ===== DELETE TESTS =====

    @Test
    void deleteEmployee_ExistingEmployee_ShouldReturn204() throws Exception {
        // Given
        doNothing().when(employeeService).deleteEmployeeByEmail("jan@example.com");

        // When & Then
        mockMvc.perform(delete("/api/employees/jan@example.com"))
                .andExpect(status().isNoContent());

        verify(employeeService, times(1)).deleteEmployeeByEmail("jan@example.com");
    }

    @Test
    void deleteEmployee_NonExistingEmployee_ShouldReturn404() throws Exception {
        // Given
        doThrow(new EmployeeNotFoundException("Employee not found"))
                .when(employeeService).deleteEmployeeByEmail("nonexistent@example.com");

        // When & Then
        mockMvc.perform(delete("/api/employees/nonexistent@example.com"))
                .andExpect(status().isNotFound());

        verify(employeeService, times(1)).deleteEmployeeByEmail("nonexistent@example.com");
    }

    // ===== PATCH TESTS =====

    @Test
    void updateEmployeeStatus_ValidStatus_ShouldReturn200() throws Exception {
        // Given
        EmployeeController.EmploymentStatusUpdateRequest statusRequest =
                new EmployeeController.EmploymentStatusUpdateRequest();
        statusRequest.setStatus(EmploymentStatus.ON_LEAVE);

        Employee updatedEmployee = new Employee(
                "Jan Kowalski",
                "jan@example.com",
                "TechCorp",
                Position.PROGRAMMER,
                8000.0,
                EmploymentStatus.ON_LEAVE
        );

        when(employeeService.updateEmployeeStatus(eq("jan@example.com"), eq(EmploymentStatus.ON_LEAVE)))
                .thenReturn(updatedEmployee);

        // When & Then
        mockMvc.perform(patch("/api/employees/jan@example.com/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ON_LEAVE"));

        verify(employeeService, times(1)).updateEmployeeStatus("jan@example.com", EmploymentStatus.ON_LEAVE);
    }

    @Test
    void updateEmployeeStatus_NonExistingEmployee_ShouldReturn404() throws Exception {
        // Given
        EmployeeController.EmploymentStatusUpdateRequest statusRequest =
                new EmployeeController.EmploymentStatusUpdateRequest();
        statusRequest.setStatus(EmploymentStatus.ON_LEAVE);

        when(employeeService.updateEmployeeStatus(eq("nonexistent@example.com"), any()))
                .thenThrow(new EmployeeNotFoundException("Employee not found"));

        // When & Then
        mockMvc.perform(patch("/api/employees/nonexistent@example.com/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusRequest)))
                .andExpect(status().isNotFound());

        verify(employeeService, times(1)).updateEmployeeStatus(eq("nonexistent@example.com"), any());
    }

    // ===== STATISTICS TESTS =====

    @Test
    void getCompanyStatistics_ShouldReturnStatistics() throws Exception {
        // Given
        when(employeeService.getAllCompanyStatisticsDTO()).thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/api/employees/statistics/company"))
                .andExpect(status().isOk());

        verify(employeeService, times(1)).getAllCompanyStatisticsDTO();
    }

    // ===== EDGE CASES TESTS =====

    @Test
    void updateEmployee_WithNullLastName_ShouldHandleCorrectly() throws Exception {
        // Given
        EmployeeDTO dtoWithNullLastName = new EmployeeDTO(
                "Jan", null, "jan@example.com",
                "TechCorp", Position.PROGRAMMER, 8000.0, EmploymentStatus.ACTIVE
        );

        when(employeeService.findEmployeeByEmail("jan@example.com"))
                .thenReturn(Optional.of(testEmployee));
        when(employeeService.saveEmployee(any(Employee.class)))
                .thenReturn(testEmployee);

        // When & Then
        mockMvc.perform(put("/api/employees/jan@example.com")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dtoWithNullLastName)))
                .andExpect(status().isOk());

        verify(employeeService, times(1)).findEmployeeByEmail("jan@example.com");
        verify(employeeService, times(1)).saveEmployee(any(Employee.class));
    }

}