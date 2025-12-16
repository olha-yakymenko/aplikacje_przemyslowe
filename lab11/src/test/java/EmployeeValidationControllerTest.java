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
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
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
    void createEmployee_withEmptyFields_shouldReturn400WithValidationErrors() throws Exception {
        EmployeeDTO invalidEmployee = new EmployeeDTO();
        invalidEmployee.setFirstName("");
        invalidEmployee.setLastName("");
        invalidEmployee.setEmail("");
        invalidEmployee.setCompany("");
        invalidEmployee.setPosition(null);
        invalidEmployee.setSalary(null);
        invalidEmployee.setStatus(null);

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidEmployee)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.firstName", anyOf(
                        is("Imię musi mieć od 2 do 50 znaków"),
                        is("Imię jest wymagane")
                )))
                .andExpect(jsonPath("$.lastName", anyOf(
                        is("Nazwisko musi mieć od 2 do 50 znaków"),
                        is("Nazwisko jest wymagane")
                )))
                .andExpect(jsonPath("$.email", anyOf(
                        is("Email musi być w domenie @techcorp.com"),
                        is("Email jest wymagany")
                )))
                .andExpect(jsonPath("$.company", anyOf(
                        is("Nazwa firmy musi mieć od 2 do 100 znaków"),
                        is("Firma jest wymagana")
                )))
//                .andExpect(jsonPath("$.company", is("Firma jest wymagana")))
                .andExpect(jsonPath("$.position", is("Stanowisko jest wymagane")))
//                .andExpect(jsonPath("$.salary", is("musi być większe od 0")))
                .andExpect(jsonPath("$.salary", anyOf(
                        is("Wynagrodzenie musi być większe niż 0"),
                        is("Wynagrodzenie jest wymagane")
                )))
                .andExpect(jsonPath("$.status", is("Status jest wymagany")));
    }

    // ========== Walidacja domeny email ==========
    @Test
    void createEmployee_withWrongEmailDomain_shouldReturn400WithCustomValidationError() throws Exception {
        EmployeeDTO invalidEmployee = new EmployeeDTO();
        invalidEmployee.setFirstName("Jan");
        invalidEmployee.setLastName("Kowalski");
        invalidEmployee.setEmail("jan.kowalski@gmail.com");
        invalidEmployee.setCompany("TechCorp");
        invalidEmployee.setPosition(Position.PROGRAMMER);
        invalidEmployee.setSalary(5000.0);
        invalidEmployee.setStatus(EmploymentStatus.ACTIVE);

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidEmployee)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.email", is("Email musi być w domenie @techcorp.com")));
    }

    // ========== Walidacja długości pól ==========
    @Test
    void createEmployee_withInvalidFieldLengths_shouldReturn400() throws Exception {
        EmployeeDTO invalidEmployee = new EmployeeDTO();
        invalidEmployee.setFirstName("A");
        invalidEmployee.setLastName("B");
        invalidEmployee.setEmail("test@techcorp.com");
        invalidEmployee.setCompany("A");
        invalidEmployee.setPosition(Position.PROGRAMMER);
        invalidEmployee.setSalary(5000.0);
        invalidEmployee.setStatus(EmploymentStatus.ACTIVE);

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidEmployee)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.firstName", is("Imię musi mieć od 2 do 50 znaków")))
                .andExpect(jsonPath("$.lastName", is("Nazwisko musi mieć od 2 do 50 znaków")))
                .andExpect(jsonPath("$.company", is("Nazwa firmy musi mieć od 2 do 100 znaków")));
    }

    // ========== Walidacja wynagrodzenia ==========
    @Test
    void createEmployee_withInvalidSalary_shouldReturn400() throws Exception {
        EmployeeDTO invalidEmployee = new EmployeeDTO();
        invalidEmployee.setFirstName("Jan");
        invalidEmployee.setLastName("Kowalski");
        invalidEmployee.setEmail("jan.kowalski@techcorp.com");
        invalidEmployee.setCompany("TechCorp");
        invalidEmployee.setPosition(Position.PROGRAMMER);
        invalidEmployee.setSalary(-100.0);
        invalidEmployee.setStatus(EmploymentStatus.ACTIVE);

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidEmployee)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.salary", is("Wynagrodzenie musi być większe niż 0")));

        invalidEmployee.setSalary(2_000_000.0);
        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidEmployee)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.salary", is("Wynagrodzenie nie może przekraczać 1,000,000")));

        invalidEmployee.setSalary(0.0);
        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidEmployee)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.salary", is("Wynagrodzenie musi być większe niż 0")));
    }

    // ========== Walidacja przy aktualizacji ==========
    @Test
    void updateEmployee_withInvalidData_shouldReturn400() throws Exception {
        String email = "test@techcorp.com";

        EmployeeDTO invalidEmployee = new EmployeeDTO();
        invalidEmployee.setFirstName("");
        invalidEmployee.setLastName("Kowalski");
        invalidEmployee.setEmail(email);
        invalidEmployee.setCompany("TechCorp");
        invalidEmployee.setPosition(Position.PROGRAMMER);
        invalidEmployee.setSalary(-100.0);
        invalidEmployee.setStatus(EmploymentStatus.ACTIVE);

        mockMvc.perform(put("/api/employees/{email}", email)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidEmployee)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.firstName", anyOf(
                        is("Imię musi mieć od 2 do 50 znaków"),
                        is("Imię jest wymagane")
                )))
                .andExpect(jsonPath("$.salary", is("Wynagrodzenie musi być większe niż 0")));
    }

    // ========== Zły format email ==========
    @Test
    void createEmployee_withInvalidEmailFormat_shouldReturn400() throws Exception {
        EmployeeDTO invalidEmployee = new EmployeeDTO();
        invalidEmployee.setFirstName("Jan");
        invalidEmployee.setLastName("Kowalski");
        invalidEmployee.setEmail("bad-email-format");
        invalidEmployee.setCompany("TechCorp");
        invalidEmployee.setPosition(Position.PROGRAMMER);
        invalidEmployee.setSalary(5000.0);
        invalidEmployee.setStatus(EmploymentStatus.ACTIVE);

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidEmployee)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.email", anyOf(
                        is("Email musi być w domenie @techcorp.com"),
                        is("Nieprawidłowy format email")
                )));
    }

    ///jdbc

    // ========== Pełne dopasowanie JSON ==========
    @Test
    void createEmployee_shouldMatchExpectedJson() throws Exception {
        EmployeeDTO invalidEmployee = new EmployeeDTO();
        invalidEmployee.setFirstName("a");
        invalidEmployee.setLastName("a");
        invalidEmployee.setEmail("a@a");
        invalidEmployee.setCompany("a");
        invalidEmployee.setPosition(null);
        invalidEmployee.setSalary(null);
        invalidEmployee.setStatus(null);

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidEmployee)))
                .andExpect(status().isBadRequest())
                .andExpect(content().json("""
                {
                        "firstName": "Imię musi mieć od 2 do 50 znaków",
                        "lastName": "Nazwisko musi mieć od 2 do 50 znaków",
                        "email": "Email musi być w domenie @techcorp.com",
                        "company": "Nazwa firmy musi mieć od 2 do 100 znaków",
                        "position": "Stanowisko jest wymagane",
                        "salary": "Wynagrodzenie jest wymagane",
                        "status": "Status jest wymagany"
                }
                """, false));
    }
}















//package com.techcorp.employee.controller;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.techcorp.employee.dto.EmployeeDTO;
//import com.techcorp.employee.model.EmploymentStatus;
//import com.techcorp.employee.model.Position;
//import com.techcorp.employee.service.EmployeeService;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.http.MediaType;
//import org.springframework.test.web.servlet.MockMvc;
//
//import java.util.Map;
//
//import static org.hamcrest.Matchers.*;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.when;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
//@WebMvcTest(EmployeeController.class)
//class EmployeeValidationControllerTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @Autowired
//    private ObjectMapper objectMapper;
//
//    @MockBean
//    private EmployeeService employeeService;
//
//    // ========== Walidacja pustych pól ==========
//    @Test
//    void createEmployee_withEmptyFields_shouldReturn400WithValidationErrors() throws Exception {
//        // Arrange: tworzymy DTO z pustymi polami
//        EmployeeDTO invalidEmployee = new EmployeeDTO();
//        invalidEmployee.setFirstName(""); // puste imię
//        invalidEmployee.setLastName(""); // puste nazwisko
//        invalidEmployee.setEmail(""); // pusty email
//        invalidEmployee.setCompany(""); // pusta firma
//        invalidEmployee.setPosition(null); // null position
//        invalidEmployee.setSalary(null); // null salary
//        invalidEmployee.setStatus(null); // null status
//
//        // Act & Assert
//        mockMvc.perform(post("/api/employees")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(invalidEmployee)))
//                .andExpect(status().isBadRequest()) // oczekujemy 400
//                .andExpect(jsonPath("$.status", is(400))) // sprawdzamy status
//                .andExpect(jsonPath("$.error", is("Validation Failed"))) // typ błędu
//                .andExpect(jsonPath("$.message", is("One or more validation errors occurred"))) // komunikat
//                .andExpect(jsonPath("$.errors", aMapWithSize(7))) // 7 błędów walidacji
//                .andExpect(jsonPath("$.errors.firstName",
//                        either(is("Imię jest wymagane"))
//                                .or(is("Imię musi mieć od 2 do 50 znaków"))
//                ))
//                .andExpect(jsonPath("$.errors.lastName",
//                        either(is("Nazwisko jest wymagane"))
//                                .or(is("Nazwisko musi mieć od 2 do 50 znaków"))
//                ))
//                .andExpect(jsonPath("$.errors.email", is("Email jest wymagany")))
//                .andExpect(jsonPath("$.errors.company", is("Firma jest wymagana")))
//                .andExpect(jsonPath("$.errors.position", is("Stanowisko jest wymagane")))
//                .andExpect(jsonPath("$.errors.salary", is("Wynagrodzenie jest wymagane")))
//                .andExpect(jsonPath("$.errors.status", is("Status jest wymagany")));
//    }
//
//    // ========== Walidacja domeny email (Custom Validator) ==========
//    @Test
//    void createEmployee_withWrongEmailDomain_shouldReturn400WithCustomValidationError() throws Exception {
//        // Arrange: email z nieprawidłową domeną
//        EmployeeDTO invalidEmployee = new EmployeeDTO();
//        invalidEmployee.setFirstName("Jan");
//        invalidEmployee.setLastName("Kowalski");
//        invalidEmployee.setEmail("jan.kowalski@gmail.com"); // NIE @techcorp.com!
//        invalidEmployee.setCompany("TechCorp");
//        invalidEmployee.setPosition(Position.PROGRAMMER);
//        invalidEmployee.setSalary(5000.0);
//        invalidEmployee.setStatus(EmploymentStatus.ACTIVE);
//
//        // Act & Assert
//        mockMvc.perform(post("/api/employees")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(invalidEmployee)))
//                .andExpect(status().isBadRequest())
//                .andExpect(jsonPath("$.errors.email", is("Email musi być w domenie @techcorp.com")));
//    }
//
//    // ========== Walidacja długości pól ==========
//    @Test
//    void createEmployee_withInvalidFieldLengths_shouldReturn400() throws Exception {
//        // Arrange: pola z nieprawidłową długością
//        EmployeeDTO invalidEmployee = new EmployeeDTO();
//        invalidEmployee.setFirstName("A"); // za krótkie (min 2)
//        invalidEmployee.setLastName("B"); // za krótkie (min 2)
//        invalidEmployee.setEmail("test@techcorp.com"); // poprawna domena
//        invalidEmployee.setCompany("A"); // za krótkie (min 2)
//        invalidEmployee.setPosition(Position.PROGRAMMER);
//        invalidEmployee.setSalary(5000.0);
//        invalidEmployee.setStatus(EmploymentStatus.ACTIVE);
//
//        // Act & Assert
//        mockMvc.perform(post("/api/employees")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(invalidEmployee)))
//                .andExpect(status().isBadRequest())
//                .andExpect(jsonPath("$.errors", aMapWithSize(3)))
//                .andExpect(jsonPath("$.errors.firstName", is("Imię musi mieć od 2 do 50 znaków")))
//                .andExpect(jsonPath("$.errors.lastName", is("Nazwisko musi mieć od 2 do 50 znaków")))
//                .andExpect(jsonPath("$.errors.company", is("Nazwa firmy musi mieć od 2 do 100 znaków")));
//    }
//
//    // ========== Walidacja wynagrodzenia ==========
//    @Test
//    void createEmployee_withInvalidSalary_shouldReturn400() throws Exception {
//        // Arrange: nieprawidłowe wynagrodzenie
//        EmployeeDTO invalidEmployee = new EmployeeDTO();
//        invalidEmployee.setFirstName("Jan");
//        invalidEmployee.setLastName("Kowalski");
//        invalidEmployee.setEmail("jan.kowalski@techcorp.com");
//        invalidEmployee.setCompany("TechCorp");
//        invalidEmployee.setPosition(Position.PROGRAMMER);
//        invalidEmployee.setSalary(-100.0); // ujemne wynagrodzenie
//        invalidEmployee.setStatus(EmploymentStatus.ACTIVE);
//
//        // Test 4a: ujemne wynagrodzenie
//        mockMvc.perform(post("/api/employees")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(invalidEmployee)))
//                .andExpect(status().isBadRequest())
//                .andExpect(jsonPath("$.errors.salary", is("Wynagrodzenie musi być większe niż 0")));
//
//        // Test 4b: za wysokie wynagrodzenie
//        invalidEmployee.setSalary(2000000.0); // powyżej 1,000,000
//        mockMvc.perform(post("/api/employees")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(invalidEmployee)))
//                .andExpect(status().isBadRequest())
//                .andExpect(jsonPath("$.errors.salary", is("Wynagrodzenie nie może przekraczać 1,000,000")));
//
//        // Test 4c: wynagrodzenie = 0
//        invalidEmployee.setSalary(0.0);
//        mockMvc.perform(post("/api/employees")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(invalidEmployee)))
//                .andExpect(status().isBadRequest())
//                .andExpect(jsonPath("$.errors.salary", is("Wynagrodzenie musi być większe niż 0")));
//    }
//
//
//    // ========== Walidacja podczas aktualizacji (PUT) ==========
//    @Test
//    void updateEmployee_withInvalidData_shouldReturn400() throws Exception {
//        // Arrange
//        String email = "test@techcorp.com";
//        EmployeeDTO invalidEmployee = new EmployeeDTO();
//        invalidEmployee.setFirstName("");
//        invalidEmployee.setLastName("Kowalski");
//        invalidEmployee.setEmail(email);
//        invalidEmployee.setCompany("TechCorp");
//        invalidEmployee.setPosition(Position.PROGRAMMER);
//        invalidEmployee.setSalary(-100.0);
//        invalidEmployee.setStatus(EmploymentStatus.ACTIVE);
//
//        // Act & Assert
//        mockMvc.perform(put("/api/employees/{email}", email)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(invalidEmployee)))
//                .andExpect(status().isBadRequest())
//                .andExpect(jsonPath("$.errors.firstName",
//                        either(is("Imię jest wymagane"))
//                                .or(is("Imię musi mieć od 2 do 50 znaków"))
//                ))
//                .andExpect(jsonPath("$.errors.salary", is("Wynagrodzenie musi być większe niż 0")));
//    }
//
//    // ========== Walidacja domeny ==========
//    @Test
//    void createEmployee_withInvalidEmailFormat_shouldReturn400() throws Exception {
//        // Arrange
//        EmployeeDTO invalidEmployee = new EmployeeDTO();
//        invalidEmployee.setFirstName("Jan");
//        invalidEmployee.setLastName("Kowalski");
//        invalidEmployee.setEmail("niepoprawny-email"); // zły format
//        invalidEmployee.setCompany("TechCorp");
//        invalidEmployee.setPosition(Position.PROGRAMMER);
//        invalidEmployee.setSalary(5000.0);
//        invalidEmployee.setStatus(EmploymentStatus.ACTIVE);
//
//        // Act & Assert
//        mockMvc.perform(post("/api/employees")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(invalidEmployee)))
//                .andExpect(status().isBadRequest())
//                .andExpect(jsonPath("$.errors.email",
//                        either(is("Email musi być w domenie @techcorp.com"))
//                                .or(is("Nieprawidłowy format email"))
//                ));
//    }
//
//
//    @Test
//    void createEmployee_shouldMatchExpectedJson() throws Exception {
//        // Arrange
//        EmployeeDTO invalidEmployee = new EmployeeDTO();
//        invalidEmployee.setFirstName("a");
//        invalidEmployee.setLastName("a");
//        invalidEmployee.setEmail("a@a");
//        invalidEmployee.setCompany("a");
//        invalidEmployee.setPosition(null);
//        invalidEmployee.setSalary(null);
//        invalidEmployee.setStatus(null);
//
//        // Act & Assert
//        mockMvc.perform(post("/api/employees")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(invalidEmployee)))
//                .andExpect(status().isBadRequest())
//                .andExpect(content().json("""
//                {
//                    "status": 400,
//                    "error": "Validation Failed",
//                    "message": "One or more validation errors occurred",
//                    "errors": {
//                        "lastName": "Nazwisko musi mieć od 2 do 50 znaków",
//                        "email": "Email musi być w domenie @techcorp.com",
//                        "company": "Nazwa firmy musi mieć od 2 do 100 znaków",
//                        "position": "Stanowisko jest wymagane",
//                        "salary": "Wynagrodzenie jest wymagane",
//                        "status": "Status jest wymagany"
//                    }
//                }
//                """, false)); // false = strict checking (ignores extra fields like timestamp)
//    }
//}
//
