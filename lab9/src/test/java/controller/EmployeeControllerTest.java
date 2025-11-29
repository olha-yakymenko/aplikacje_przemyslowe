package com.techcorp.employee.controller;

import com.techcorp.employee.dto.EmployeeDTO;
import com.techcorp.employee.exception.DuplicateEmailException;
import com.techcorp.employee.exception.EmployeeNotFoundException;
import com.techcorp.employee.exception.InvalidDataException;
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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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
    void setUp() throws Exception {
        testEmployee = new Employee(
                "Jan Kowalski",
                "jan@example.com",
                "TechCorp",
                Position.PROGRAMMER,
                8000.0,
                EmploymentStatus.ACTIVE
        );

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
    void getAllEmployees_ShouldReturnEmployees() throws Exception {
        // Given
        List<Employee> employees = Arrays.asList(testEmployee);
        when(employeeService.getAllEmployees()).thenReturn(employees);

        // When & Then
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].firstName").value("Jan"))
                .andExpect(jsonPath("$[0].email").value("jan@example.com"));

        verify(employeeService, times(1)).getAllEmployees();
    }

    @Test
    void getAllEmployees_EmptyDatabase_ShouldReturnEmptyList() throws Exception {
        // Given
        when(employeeService.getAllEmployees()).thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(employeeService, times(1)).getAllEmployees();
    }

    @Test
    void getEmployeeByEmail_ExistingEmployee_ShouldReturnEmployee() throws Exception {
        // Given
        when(employeeService.findEmployeeByEmail("jan@example.com"))
                .thenReturn(Optional.of(testEmployee));

        // When & Then
        mockMvc.perform(get("/api/employees/jan@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Jan"))
                .andExpect(jsonPath("$.email").value("jan@example.com"))
                .andExpect(jsonPath("$.company").value("TechCorp"))
                .andExpect(jsonPath("$.position").value("PROGRAMMER"));

        verify(employeeService, times(1)).findEmployeeByEmail("jan@example.com");
    }

    @Test
    void getEmployeeByEmail_NonExistingEmployee_ShouldReturn404() throws Exception {
        // Given
        when(employeeService.findEmployeeByEmail("nonexistent@example.com"))
                .thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/employees/nonexistent@example.com"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.message").value("Employee not found with email: nonexistent@example.com"));

        verify(employeeService, times(1)).findEmployeeByEmail("nonexistent@example.com");
    }

    @Test
    void getEmployeesByCompany_ShouldReturnFilteredEmployees() throws Exception {
        // Given
        List<Employee> employees = Arrays.asList(testEmployee);
        when(employeeService.getEmployeesByCompany("TechCorp")).thenReturn(employees);

        // When & Then
        mockMvc.perform(get("/api/employees")
                        .param("company", "TechCorp"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].company").value("TechCorp"));

        verify(employeeService, times(1)).getEmployeesByCompany("TechCorp");
    }

    @Test
    void getEmployeesByCompany_NonExistingCompany_ShouldReturnEmptyList() throws Exception {
        // Given
        when(employeeService.getEmployeesByCompany("NonExisting")).thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/api/employees")
                        .param("company", "NonExisting"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(employeeService, times(1)).getEmployeesByCompany("NonExisting");
    }

    @Test
    void getEmployeesByStatus_ShouldReturnFilteredEmployees() throws Exception {
        // Given
        List<Employee> activeEmployees = Arrays.asList(testEmployee);
        when(employeeService.getEmployeesByStatus(EmploymentStatus.ACTIVE)).thenReturn(activeEmployees);

        // When & Then
        mockMvc.perform(get("/api/employees/status/ACTIVE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].status").value("ACTIVE"));

        verify(employeeService, times(1)).getEmployeesByStatus(EmploymentStatus.ACTIVE);
    }

    // ===== POST TESTS =====

    @Test
    void createEmployee_ValidEmployee_ShouldReturn201() throws Exception {
        // Given
        when(employeeService.addEmployee(any(Employee.class))).thenReturn(true);

        // When & Then
        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testEmployeeDTO)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(header().string("Location", "http://localhost/api/employees/jan@example.com"))
                .andExpect(jsonPath("$.firstName").value("Jan"))
                .andExpect(jsonPath("$.email").value("jan@example.com"));

        verify(employeeService, times(1)).addEmployee(any(Employee.class));
    }

    @Test
    void createEmployee_DuplicateEmail_ShouldReturn409() throws Exception {
        // Given
        when(employeeService.addEmployee(any(Employee.class)))
                .thenThrow(new DuplicateEmailException("Employee with email jan@example.com already exists"));

        // When & Then
        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testEmployeeDTO)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.message").value("Employee with email jan@example.com already exists"));

        verify(employeeService, times(1)).addEmployee(any(Employee.class));
    }

    @Test
    void createEmployee_InvalidData_ShouldReturn400() throws Exception {
        // Given
        EmployeeDTO invalidEmployeeDTO = new EmployeeDTO(
                "", // empty first name
                "Kowalski",
                "invalid-email",
                "",
                Position.PROGRAMMER,
                -1000.0,
                EmploymentStatus.ACTIVE
        );

        // When & Then
        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidEmployeeDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());

        verify(employeeService, never()).addEmployee(any(Employee.class));
    }

    // ===== PUT TESTS =====

    @Test
    void updateEmployee_ExistingEmployee_ShouldReturn200() throws Exception {
        // Given
        when(employeeService.findEmployeeByEmail("jan@example.com"))
                .thenReturn(Optional.of(testEmployee));
        when(employeeService.updateEmployee(any(Employee.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When & Then
        mockMvc.perform(put("/api/employees/jan@example.com")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testEmployeeDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Jan"))
                .andExpect(jsonPath("$.email").value("jan@example.com"));

        verify(employeeService, times(1)).findEmployeeByEmail("jan@example.com");
        verify(employeeService, times(1)).updateEmployee(any(Employee.class));
    }

    @Test
    void updateEmployee_WithNullLastName_ShouldHandleCorrectly() throws Exception {
        // Given
        EmployeeDTO dtoWithNullLastName = new EmployeeDTO(
                "Jan", null, "jan@example.com",
                "TechCorp", Position.PROGRAMMER, 8000.0, EmploymentStatus.ACTIVE
        );

        when(employeeService.findEmployeeByEmail("jan@example.com"))
                .thenReturn(Optional.of(testEmployee));
        when(employeeService.updateEmployee(any(Employee.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When & Then - nie powinno rzucić wyjątku
        mockMvc.perform(put("/api/employees/jan@example.com")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dtoWithNullLastName)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Jan"));

        // ✅ DODAJ VERIFY - ponieważ mockujemy metody serwisu
        verify(employeeService, times(1)).findEmployeeByEmail("jan@example.com");
        verify(employeeService, times(1)).updateEmployee(any(Employee.class));
    }


    // ===== DELETE TESTS =====

    @Test
    void deleteEmployee_ExistingEmployee_ShouldReturn204() throws Exception {
        // Given
        when(employeeService.removeEmployee("jan@example.com")).thenReturn(true);

        // When & Then
        mockMvc.perform(delete("/api/employees/jan@example.com"))
                .andExpect(status().isNoContent());

        verify(employeeService, times(1)).removeEmployee("jan@example.com");
    }

    @Test
    void deleteEmployee_NonExistingEmployee_ShouldReturn404() throws Exception {
        // Given
        when(employeeService.removeEmployee("nonexistent@example.com")).thenReturn(false);

        // When & Then
        mockMvc.perform(delete("/api/employees/nonexistent@example.com"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Employee not found with email: nonexistent@example.com"));

        verify(employeeService, times(1)).removeEmployee("nonexistent@example.com");
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
                .andExpect(jsonPath("$.status").value("ON_LEAVE"))
                .andExpect(jsonPath("$.email").value("jan@example.com"));

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
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());

        verify(employeeService, times(1)).updateEmployeeStatus(eq("nonexistent@example.com"), any());
    }

    @Test
    void updateEmployeeStatus_InvalidStatus_ShouldReturn400() throws Exception {
        // Given
        String invalidStatusJson = "{\"status\": \"INVALID_STATUS\"}";

        // When & Then
        mockMvc.perform(patch("/api/employees/jan@example.com/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidStatusJson))
                .andExpect(status().isBadRequest());

        verify(employeeService, never()).updateEmployeeStatus(anyString(), any());
    }


    @Test
    void getEmployeeByEmail_CaseInsensitive_ShouldWork() throws Exception {
        // Given
        when(employeeService.findEmployeeByEmail("JAN@EXAMPLE.COM"))
                .thenReturn(Optional.of(testEmployee));

        // When & Then
        mockMvc.perform(get("/api/employees/JAN@EXAMPLE.COM"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("jan@example.com"));

        verify(employeeService, times(1)).findEmployeeByEmail("JAN@EXAMPLE.COM");
    }

//    @Test
//    void createEmployee_MissingRequiredFields_ShouldReturn400() throws Exception {
//        // Given
//        String incompleteJson = "{\"firstName\": \"Jan\"}"; // missing other required fields
//
//        // When & Then
//        mockMvc.perform(post("/api/employees")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(incompleteJson))
//                .andExpect(status().isBadRequest());
//
//        verify(employeeService, never()).addEmployee(any(Employee.class));
//    }

    @Test
    void getAllEmployees_WithMultipleEmployees_ShouldReturnAll() throws Exception {
        // Given
        Employee employee1 = new Employee("Jan Kowalski", "jan@example.com", "TechCorp", Position.PROGRAMMER, 8000.0, EmploymentStatus.ACTIVE);
        Employee employee2 = new Employee("Anna Nowak", "anna@example.com", "SoftInc", Position.MANAGER, 12000.0, EmploymentStatus.ACTIVE);
        List<Employee> employees = Arrays.asList(employee1, employee2);

        when(employeeService.getAllEmployees()).thenReturn(employees);

        // When & Then
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].firstName").value("Jan"))
                .andExpect(jsonPath("$[1].firstName").value("Anna"));

        verify(employeeService, times(1)).getAllEmployees();
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
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Email in path must match email in request body"));

        verify(employeeService, never()).findEmployeeByEmail(anyString());
        verify(employeeService, never()).updateEmployee(any(Employee.class));
    }


}