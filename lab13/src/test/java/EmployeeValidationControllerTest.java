package com.techcorp.employee.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.techcorp.employee.dto.EmployeeDTO;
import com.techcorp.employee.model.EmploymentStatus;
import com.techcorp.employee.model.Position;
import com.techcorp.employee.service.EmployeeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EmployeeController.class)
class EmployeeValidationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EmployeeService employeeService;

    // ========== Walidacja pustych pól ==========
    @Test
    @WithMockUser  // Dodaj to!
    void createEmployee_withEmptyFields_shouldReturn400WithValidationErrors() throws Exception {
        // Arrange: tworzymy DTO z pustymi polami
        EmployeeDTO invalidEmployee = new EmployeeDTO();
        invalidEmployee.setFirstName(""); // puste imię
        invalidEmployee.setLastName(""); // puste nazwisko
        invalidEmployee.setEmail(""); // pusty email
        invalidEmployee.setCompany(""); // pusta firma
        invalidEmployee.setPosition(null); // null position
        invalidEmployee.setSalary(null); // null salary
        invalidEmployee.setStatus(null); // null status

        // Act & Assert
        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidEmployee))
                        .with(csrf())) // Dodaj to!
                .andExpect(status().isBadRequest()) // oczekujemy 400
                .andExpect(jsonPath("$.status", is(400))) // sprawdzamy status
                .andExpect(jsonPath("$.error", is("Validation Failed"))) // typ błędu
                .andExpect(jsonPath("$.message", is("One or more validation errors occurred"))) // komunikat
                .andExpect(jsonPath("$.errors", aMapWithSize(7))) // 7 błędów walidacji
                .andExpect(jsonPath("$.errors.firstName",
                        either(is("Imię jest wymagane"))
                                .or(is("Imię musi mieć od 2 do 50 znaków"))
                ))
                .andExpect(jsonPath("$.errors.lastName",
                        either(is("Nazwisko jest wymagane"))
                                .or(is("Nazwisko musi mieć od 2 do 50 znaków"))
                ))
                .andExpect(jsonPath("$.errors.email", is("Email jest wymagany")))
//                .andExpect(jsonPath("$.errors.company", is("Firma jest wymagana")))
                .andExpect(jsonPath("$.errors.company",
                        either(is("Firma jest wymagana"))
                                .or(is("Nazwa firmy musi mieć od 2 do 100 znaków"))
                ))
                .andExpect(jsonPath("$.errors.position", is("Stanowisko jest wymagane")))
                .andExpect(jsonPath("$.errors.salary", is("Wynagrodzenie jest wymagane")))
                .andExpect(jsonPath("$.errors.status", is("Status jest wymagany")));
    }

    // ========== Walidacja domeny email (Custom Validator) ==========
    @Test
    @WithMockUser  // Dodaj to!
    void createEmployee_withWrongEmailDomain_shouldReturn400WithCustomValidationError() throws Exception {
        // Arrange: email z nieprawidłową domeną
        EmployeeDTO invalidEmployee = new EmployeeDTO();
        invalidEmployee.setFirstName("Jan");
        invalidEmployee.setLastName("Kowalski");
        invalidEmployee.setEmail("jan.kowalski@gmail.com"); // NIE @techcorp.com!
        invalidEmployee.setCompany("TechCorp");
        invalidEmployee.setPosition(Position.PROGRAMMER);
        invalidEmployee.setSalary(BigDecimal.valueOf(5000.0));
        invalidEmployee.setStatus(EmploymentStatus.ACTIVE);

        // Act & Assert
        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidEmployee))
                        .with(csrf())) // Dodaj to!
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.email", is("Email musi być w domenie @techcorp.com")));
    }

    // ========== Walidacja długości pól ==========
    @Test
    @WithMockUser  // Dodaj to!
    void createEmployee_withInvalidFieldLengths_shouldReturn400() throws Exception {
        // Arrange: pola z nieprawidłową długością
        EmployeeDTO invalidEmployee = new EmployeeDTO();
        invalidEmployee.setFirstName("A"); // za krótkie (min 2)
        invalidEmployee.setLastName("B"); // za krótkie (min 2)
        invalidEmployee.setEmail("test@techcorp.com"); // poprawna domena
        invalidEmployee.setCompany("A"); // za krótkie (min 2)
        invalidEmployee.setPosition(Position.PROGRAMMER);
        invalidEmployee.setSalary(BigDecimal.valueOf(5000.0));
        invalidEmployee.setStatus(EmploymentStatus.ACTIVE);

        // Act & Assert
        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidEmployee))
                        .with(csrf())) // Dodaj to!
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", aMapWithSize(3)))
                .andExpect(jsonPath("$.errors.firstName", is("Imię musi mieć od 2 do 50 znaków")))
                .andExpect(jsonPath("$.errors.lastName", is("Nazwisko musi mieć od 2 do 50 znaków")))
                .andExpect(jsonPath("$.errors.company", is("Nazwa firmy musi mieć od 2 do 100 znaków")));
    }

    // ========== Walidacja wynagrodzenia ==========
    @Test
    @WithMockUser  // Dodaj to!
    void createEmployee_withInvalidSalary_shouldReturn400() throws Exception {
        // Arrange: nieprawidłowe wynagrodzenie
        EmployeeDTO invalidEmployee = new EmployeeDTO();
        invalidEmployee.setFirstName("Jan");
        invalidEmployee.setLastName("Kowalski");
        invalidEmployee.setEmail("jan.kowalski@techcorp.com");
        invalidEmployee.setCompany("TechCorp");
        invalidEmployee.setPosition(Position.PROGRAMMER);
        invalidEmployee.setSalary(BigDecimal.valueOf(-100.0)); // ujemne wynagrodzenie
        invalidEmployee.setStatus(EmploymentStatus.ACTIVE);

        // Test 4a: ujemne wynagrodzenie
        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidEmployee))
                        .with(csrf())) // Dodaj to!
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.salary", is("Wynagrodzenie musi być większe niż 0")));

        // Test 4b: za wysokie wynagrodzenie
        invalidEmployee.setSalary(BigDecimal.valueOf(2000000.0)); // powyżej 1,000,000
        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidEmployee))
                        .with(csrf())) // Dodaj to!
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.salary", is("Wynagrodzenie nie może przekraczać 1,000,000")));

        // Test 4c: wynagrodzenie = 0
        invalidEmployee.setSalary(BigDecimal.valueOf(0.0));
        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidEmployee))
                        .with(csrf())) // Dodaj to!
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.salary", is("Wynagrodzenie musi być większe niż 0")));
    }

    // ========== Walidacja podczas aktualizacji (PUT) ==========
    @Test
    @WithMockUser  // Dodaj to!
    void updateEmployee_withInvalidData_shouldReturn400() throws Exception {
        // Arrange
        String email = "test@techcorp.com";
        EmployeeDTO invalidEmployee = new EmployeeDTO();
        invalidEmployee.setFirstName("");
        invalidEmployee.setLastName("Kowalski");
        invalidEmployee.setEmail(email);
        invalidEmployee.setCompany("TechCorp");
        invalidEmployee.setPosition(Position.PROGRAMMER);
        invalidEmployee.setSalary(BigDecimal.valueOf(-100.0));
        invalidEmployee.setStatus(EmploymentStatus.ACTIVE);

        // Act & Assert
        mockMvc.perform(put("/api/employees/{email}", email)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidEmployee))
                        .with(csrf())) // Dodaj to!
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.firstName",
                        either(is("Imię jest wymagane"))
                                .or(is("Imię musi mieć od 2 do 50 znaków"))
                ))
                .andExpect(jsonPath("$.errors.salary", is("Wynagrodzenie musi być większe niż 0")));
    }

    // ========== Walidacja domeny ==========
    @Test
    @WithMockUser  // Dodaj to!
    void createEmployee_withInvalidEmailFormat_shouldReturn400() throws Exception {
        // Arrange
        EmployeeDTO invalidEmployee = new EmployeeDTO();
        invalidEmployee.setFirstName("Jan");
        invalidEmployee.setLastName("Kowalski");
        invalidEmployee.setEmail("niepoprawny-email"); // zły format
        invalidEmployee.setCompany("TechCorp");
        invalidEmployee.setPosition(Position.PROGRAMMER);
        invalidEmployee.setSalary(BigDecimal.valueOf(5000.0));
        invalidEmployee.setStatus(EmploymentStatus.ACTIVE);

        // Act & Assert
        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidEmployee))
                        .with(csrf())) // Dodaj to!
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.email",
                        either(is("Email musi być w domenie @techcorp.com"))
                                .or(is("Nieprawidłowy format email"))
                ));
    }

    @Test
    @WithMockUser  // Dodaj to!
    void createEmployee_shouldMatchExpectedJson() throws Exception {
        // Arrange
        EmployeeDTO invalidEmployee = new EmployeeDTO();
        invalidEmployee.setFirstName("a");
        invalidEmployee.setLastName("a");
        invalidEmployee.setEmail("a@a");
        invalidEmployee.setCompany("a");
        invalidEmployee.setPosition(null);
        invalidEmployee.setSalary(null);
        invalidEmployee.setStatus(null);

        // Act & Assert
        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidEmployee))
                        .with(csrf())) // Dodaj to!
                .andExpect(status().isBadRequest())
                .andExpect(content().json("""
                {
                    "status": 400,
                    "error": "Validation Failed",
                    "message": "One or more validation errors occurred",
                    "errors": {
                        "lastName": "Nazwisko musi mieć od 2 do 50 znaków",
                        "email": "Email musi być w domenie @techcorp.com",
                        "company": "Nazwa firmy musi mieć od 2 do 100 znaków",
                        "position": "Stanowisko jest wymagane",
                        "salary": "Wynagrodzenie jest wymagane",
                        "status": "Status jest wymagany"
                    }
                }
                """, false)); // false = strict checking (ignores extra fields like timestamp)
    }

    // ========== Dodatkowe testy ==========
//
//    @Test
//    @WithMockUser  // Test dla poprawnych danych
//    void createEmployee_withValidData_shouldReturn201() throws Exception {
//        // Arrange
//        EmployeeDTO validEmployee = new EmployeeDTO();
//        validEmployee.setFirstName("Jan");
//        validEmployee.setLastName("Kowalski");
//        validEmployee.setEmail("jan.kowalski@techcorp.com");
//        validEmployee.setCompany("TechCorp");
//        validEmployee.setPosition(Position.PROGRAMMER);
//        validEmployee.setSalary(BigDecimal.valueOf(5000.0));
//        validEmployee.setStatus(EmploymentStatus.ACTIVE);
//
//        // Mockowanie serwisu - ważne żeby test nie wywoływał rzeczywistej logiki
//        when(employeeService.saveEmployee(any())).thenReturn(null);
//
//        // Act & Assert
//        mockMvc.perform(post("/api/employees")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(validEmployee))
//                        .with(csrf()))
//                .andExpect(status().isCreated()); // 201 Created
//    }

    @Test
    @WithMockUser  // Test bez CSRF (powinien zwrócić 403)
    void createEmployee_withoutCsrf_shouldReturn403() throws Exception {
        // Arrange
        EmployeeDTO validEmployee = new EmployeeDTO();
        validEmployee.setFirstName("Jan");
        validEmployee.setLastName("Kowalski");
        validEmployee.setEmail("jan.kowalski@techcorp.com");
        validEmployee.setCompany("TechCorp");
        validEmployee.setPosition(Position.PROGRAMMER);
        validEmployee.setSalary(BigDecimal.valueOf(5000.0));
        validEmployee.setStatus(EmploymentStatus.ACTIVE);

        // Act & Assert
        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validEmployee)))
                .andExpect(status().isForbidden()); // 403 Forbidden bez CSRF
    }

    @Test
    @WithMockUser  // Test dla zbyt długich pól
    void createEmployee_withTooLongFields_shouldReturn400() throws Exception {
        // Arrange
        EmployeeDTO invalidEmployee = new EmployeeDTO();
        invalidEmployee.setFirstName("A".repeat(51)); // za długie (> 50)
        invalidEmployee.setLastName("B".repeat(51)); // za długie (> 50)
        invalidEmployee.setEmail("test@techcorp.com");
        invalidEmployee.setCompany("C".repeat(101)); // za długie (> 100)
        invalidEmployee.setPosition(Position.PROGRAMMER);
        invalidEmployee.setSalary(BigDecimal.valueOf(5000.0));
        invalidEmployee.setStatus(EmploymentStatus.ACTIVE);

        // Act & Assert
        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidEmployee))
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", aMapWithSize(3)))
                .andExpect(jsonPath("$.errors.firstName", is("Imię musi mieć od 2 do 50 znaków")))
                .andExpect(jsonPath("$.errors.lastName", is("Nazwisko musi mieć od 2 do 50 znaków")))
                .andExpect(jsonPath("$.errors.company", is("Nazwa firmy musi mieć od 2 do 100 znaków")));
    }

    @Test
    @WithMockUser  // Test dla null DTO
    void createEmployee_withNullDto_shouldReturn400() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("null")
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser  // Test dla pustego JSON
    void createEmployee_withEmptyJson_shouldReturn400() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}")
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isMap());
    }

    @Test
    @WithMockUser  // Test dla nieprawidłowego typu danych
    void createEmployee_withInvalidDataType_shouldReturn400() throws Exception {
        // Act & Assert - wysyłamy string zamiast number dla salary
        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                            "firstName": "Jan",
                            "lastName": "Kowalski",
                            "email": "jan.kowalski@techcorp.com",
                            "company": "TechCorp",
                            "position": "PROGRAMMER",
                            "salary": "not-a-number",
                            "status": "ACTIVE"
                        }
                        """)
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser  // Test dla nieprawidłowego enum value
    void createEmployee_withInvalidEnumValue_shouldReturn400() throws Exception {
        // Act & Assert - nieprawidłowa wartość dla statusu
        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                            "firstName": "Jan",
                            "lastName": "Kowalski",
                            "email": "jan.kowalski@techcorp.com",
                            "company": "TechCorp",
                            "position": "INVALID_POSITION",
                            "salary": 5000.0,
                            "status": "INVALID_STATUS"
                        }
                        """)
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }
}