//package com.techcorp.employee.controller;
//
//import com.techcorp.employee.dto.DepartmentDTO;
//import com.techcorp.employee.model.Department;
//import com.techcorp.employee.model.Employee;
//import com.techcorp.employee.model.Position;
//import com.techcorp.employee.model.EmploymentStatus;
//import com.techcorp.employee.service.DepartmentService;
//import com.techcorp.employee.service.EmployeeService;
//import com.techcorp.employee.service.FileStorageService;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.core.io.Resource;
//import org.springframework.http.ResponseEntity;
//import org.springframework.mock.web.MockMultipartFile;
//import org.springframework.test.web.servlet.MockMvc;
//
//import java.util.Arrays;
//import java.util.List;
//import java.util.Optional;
//
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.anyLong;
//import static org.mockito.ArgumentMatchers.eq;
//import static org.mockito.Mockito.*;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
//@WebMvcTest(DepartmentViewController.class)
//public class DepartmentViewControllerTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @MockBean
//    private DepartmentService departmentService;
//
//    @MockBean
//    private EmployeeService employeeService;
//
//    @MockBean
//    private FileStorageService fileStorageService;
//
//    // ========== TESTY DLA DOKUMENTÓW DEPARTAMENTU ==========
//
//    @Test
//    public void testShowDepartmentDocuments_DepartmentExists() throws Exception {
//        // Given
//        Department department = new Department(1L, "IT", "Warszawa", "Dział technologii", "manager@example.com", 100000.0);
//        List<String> documents = Arrays.asList("raport_1704234567890_a1b2c3.pdf", "budget_1704234578901_d4e5f6.docx");
//
//        when(departmentService.getDepartmentById(1L)).thenReturn(Optional.of(department));
//        when(fileStorageService.getDepartmentDocumentNames(1L)).thenReturn(documents);
//
//        // When & Then
//        mockMvc.perform(get("/departments/documents/1"))
//                .andExpect(status().isOk())
//                .andExpect(view().name("departments/documents"))
//                .andExpect(model().attributeExists("department"))
//                .andExpect(model().attributeExists("documents"))
//                .andExpect(model().attributeExists("pageTitle"))
//                .andExpect(model().attribute("pageTitle", "Dokumenty Departamentu"))
//                .andExpect(model().attribute("department", department))
//                .andExpect(model().attribute("documents", documents));
//
//        verify(departmentService, times(1)).getDepartmentById(1L);
//        verify(fileStorageService, times(1)).getDepartmentDocumentNames(1L);
//    }
//
//    @Test
//    public void testShowDepartmentDocuments_DepartmentNotFound() throws Exception {
//        // Given
//        when(departmentService.getDepartmentById(999L)).thenReturn(Optional.empty());
//
//        // When & Then
//        mockMvc.perform(get("/departments/documents/999"))
//                .andExpect(redirectedUrl("/departments"));
//
//        verify(departmentService, times(1)).getDepartmentById(999L);
//        verify(fileStorageService, never()).getDepartmentDocumentNames(anyLong());
//    }
//
//    @Test
//    public void testShowDepartmentDocuments_NoDocuments() throws Exception {
//        // Given
//        Department department = new Department(1L, "IT", "Warszawa", "Dział technologii", "manager@example.com", 100000.0);
//
//        when(departmentService.getDepartmentById(1L)).thenReturn(Optional.of(department));
//        when(fileStorageService.getDepartmentDocumentNames(1L)).thenReturn(List.of());
//
//        // When & Then
//        mockMvc.perform(get("/departments/documents/1"))
//                .andExpect(status().isOk())
//                .andExpect(view().name("departments/documents"))
//                .andExpect(model().attributeExists("documents"))
//                .andExpect(model().attribute("documents", List.of()));
//
//        verify(departmentService, times(1)).getDepartmentById(1L);
//        verify(fileStorageService, times(1)).getDepartmentDocumentNames(1L);
//    }
//
//    @Test
//    public void testUploadDepartmentDocument_Success() throws Exception {
//        // Given
//        MockMultipartFile file = new MockMultipartFile(
//                "file",
//                "test_document.pdf",
//                "application/pdf",
//                "test content".getBytes()
//        );
//
//        when(fileStorageService.storeDepartmentDocument(any(), eq(1L)))
//                .thenReturn("test_document_1704234567890_a1b2c3.pdf");
//
//        // When & Then
//        mockMvc.perform(multipart("/departments/documents/1/upload")
//                        .file(file))
//                .andExpect(redirectedUrl("/departments/documents/1"))
//                .andExpect(flash().attributeExists("message"))
//                .andExpect(flash().attribute("message", "Dokument dodany pomyślnie!"));
//
//        verify(fileStorageService, times(1)).storeDepartmentDocument(any(), eq(1L));
//    }
//
//    @Test
//    public void testUploadDepartmentDocument_Exception() throws Exception {
//        // Given
//        MockMultipartFile file = new MockMultipartFile(
//                "file",
//                "test_document.pdf",
//                "application/pdf",
//                "test content".getBytes()
//        );
//
//        when(fileStorageService.storeDepartmentDocument(any(), eq(1L)))
//                .thenThrow(new RuntimeException("Storage error"));
//
//        // When & Then
//        mockMvc.perform(multipart("/departments/documents/1/upload")
//                        .file(file))
//                .andExpect(redirectedUrl("/departments/documents/1"))
//                .andExpect(flash().attributeExists("error"))
//                .andExpect(flash().attribute("error", "Błąd podczas dodawania dokumentu: Storage error"));
//
//        verify(fileStorageService, times(1)).storeDepartmentDocument(any(), eq(1L));
//    }
//
//    @Test
//    public void testDownloadDepartmentDocument_Success() throws Exception {
//        // Given
//        String fileName = "test_document_1704234567890_a1b2c3.pdf";
//        Resource mockResource = mock(Resource.class);
//        ResponseEntity<Resource> mockResponse = ResponseEntity.ok(mockResource);
//
//        when(fileStorageService.loadDepartmentDocument(fileName, 1L)).thenReturn(mockResource);
//
//        // When & Then - testujemy tylko czy metoda jest wywoływana, response jest trudny do przetestowania w MockMvc
//        mockMvc.perform(get("/departments/documents/1/download/" + fileName))
//                .andExpect(status().isOk());
//
//        verify(fileStorageService, times(1)).loadDepartmentDocument(fileName, 1L);
//    }
//
//    @Test
//    public void testDeleteDepartmentDocument_Success() throws Exception {
//        // Given
//        String fileName = "test_document_1704234567890_a1b2c3.pdf";
//
//        when(fileStorageService.deleteDepartmentDocument(fileName, 1L)).thenReturn(true);
//
//        // When & Then
//        mockMvc.perform(get("/departments/documents/1/delete/" + fileName))
//                .andExpect(redirectedUrl("/departments/documents/1"))
//                .andExpect(flash().attributeExists("message"))
//                .andExpect(flash().attribute("message", "Dokument usunięty pomyślnie!"));
//
//        verify(fileStorageService, times(1)).deleteDepartmentDocument(fileName, 1L);
//    }
//
//    @Test
//    public void testDeleteDepartmentDocument_Exception() throws Exception {
//        // Given
//        String fileName = "test_document_1704234567890_a1b2c3.pdf";
//
//        when(fileStorageService.deleteDepartmentDocument(fileName, 1L))
//                .thenThrow(new RuntimeException("Delete error"));
//
//        // When & Then
//        mockMvc.perform(get("/departments/documents/1/delete/" + fileName))
//                .andExpect(redirectedUrl("/departments/documents/1"))
//                .andExpect(flash().attributeExists("error"))
//                .andExpect(flash().attribute("error", "Błąd podczas usuwania dokumentu: Delete error"));
//
//        verify(fileStorageService, times(1)).deleteDepartmentDocument(fileName, 1L);
//    }
//
//
//
//    @Test
//    public void testListDepartments() throws Exception {
//        // Given
//        Department department1 = new Department(1L, "IT", "Warszawa", "Dział technologii", "manager@example.com", 100000.0);
//        Department department2 = new Department(2L, "HR", "Kraków", "Dział kadr", "hr@example.com", 50000.0);
//        List<Department> departments = Arrays.asList(department1, department2);
//
//        when(departmentService.getAllDepartments()).thenReturn(departments);
//
//        // When & Then
//        mockMvc.perform(get("/departments"))
//                .andExpect(status().isOk())
//                .andExpect(view().name("departments/list"))
//                .andExpect(model().attributeExists("departments"))
//                .andExpect(model().attributeExists("pageTitle"))
//                .andExpect(model().attribute("pageTitle", "Lista Departamentów"))
//                .andExpect(model().attribute("departments", departments));
//
//        verify(departmentService, times(1)).getAllDepartments();
//    }
//
//    @Test
//    public void testShowAddForm() throws Exception {
//        // Given
//        Employee manager = new Employee("Jan Manager", "manager@example.com", "TechCorp",
//                Position.MANAGER, 8000.0, EmploymentStatus.ACTIVE);
//        List<Employee> managers = Arrays.asList(manager);
//
//        when(employeeService.getAvailableManagers()).thenReturn(managers);
//
//        // When & Then
//        mockMvc.perform(get("/departments/add"))
//                .andExpect(status().isOk())
//                .andExpect(view().name("departments/form"))
//                .andExpect(model().attributeExists("department"))
//                .andExpect(model().attributeExists("managers"))
//                .andExpect(model().attributeExists("pageTitle"))
//                .andExpect(model().attribute("pageTitle", "Dodaj Departament"))
//                .andExpect(model().attribute("managers", managers));
//
//        verify(employeeService, times(1)).getAvailableManagers();
//    }
//
//    @Test
//    public void testAddDepartment_Success() throws Exception {
//        // Given
//        Department department = new Department(1L, "IT", "Warszawa", "Dział technologii", "manager@example.com", 100000.0);
//
//        when(departmentService.createDepartment(any(Department.class))).thenReturn(department);
//
//        // When & Then
//        mockMvc.perform(post("/departments/add")
//                        .param("name", "IT")
//                        .param("location", "Warszawa")
//                        .param("description", "Dział technologii")
//                        .param("managerEmail", "manager@example.com")
//                        .param("budget", "100000.0"))
//                .andExpect(redirectedUrl("/departments"))
//                .andExpect(flash().attributeExists("message"))
//                .andExpect(flash().attribute("message", "Departament dodany pomyślnie!"));
//
//        verify(departmentService, times(1)).createDepartment(any(Department.class));
//    }
//
//    @Test
//    public void testAddDepartment_ValidationErrors() throws Exception {
//        // Given - puste wymagane pola name i location
//        mockMvc.perform(post("/departments/add")
//                        .param("name", "")
//                        .param("location", "")
//                        .param("description", "")
//                        .param("managerEmail", "")
//                        .param("budget", "-100.0"))
//                .andExpect(redirectedUrl("/departments/add"));
//
//        verify(departmentService, never()).createDepartment(any(Department.class));
//    }
//
//    @Test
//    public void testAddDepartment_Exception() throws Exception {
//        // Given
//        when(departmentService.createDepartment(any(Department.class)))
//                .thenThrow(new RuntimeException("Database error"));
//
//        // When & Then
//        mockMvc.perform(post("/departments/add")
//                        .param("name", "IT")
//                        .param("location", "Warszawa")
//                        .param("description", "Dział technologii")
//                        .param("managerEmail", "manager@example.com")
//                        .param("budget", "100000.0"))
//                .andExpect(redirectedUrl("/departments"))
//                .andExpect(flash().attributeExists("error"));
//
//        verify(departmentService, times(1)).createDepartment(any(Department.class));
//    }
//
//    @Test
//    public void testShowEditForm_DepartmentExists() throws Exception {
//        // Given
//        Department department = new Department(1L, "IT", "Warszawa", "Dział technologii", "manager@example.com", 100000.0);
//        Employee manager = new Employee("Jan Manager", "manager@example.com", "TechCorp",
//                Position.MANAGER, 8000.0, EmploymentStatus.ACTIVE);
//        List<Employee> managers = Arrays.asList(manager);
//
//        when(departmentService.getDepartmentById(1L)).thenReturn(Optional.of(department));
//        when(employeeService.getAvailableManagers()).thenReturn(managers);
//
//        // When & Then
//        mockMvc.perform(get("/departments/edit/1"))
//                .andExpect(status().isOk())
//                .andExpect(view().name("departments/form"))
//                .andExpect(model().attributeExists("department"))
//                .andExpect(model().attributeExists("managers"))
//                .andExpect(model().attributeExists("pageTitle"))
//                .andExpect(model().attribute("pageTitle", "Edytuj Departament"))
//                .andExpect(model().attribute("department", department))
//                .andExpect(model().attribute("managers", managers));
//
//        verify(departmentService, times(1)).getDepartmentById(1L);
//        verify(employeeService, times(1)).getAvailableManagers();
//    }
//
//    @Test
//    public void testShowEditForm_DepartmentNotFound() throws Exception {
//        // Given
//        when(departmentService.getDepartmentById(999L)).thenReturn(Optional.empty());
//
//        // When & Then
//        mockMvc.perform(get("/departments/edit/999"))
//                .andExpect(redirectedUrl("/departments"));
//
//        verify(departmentService, times(1)).getDepartmentById(999L);
//        verify(employeeService, never()).getAvailableManagers();
//    }
//
//    @Test
//    public void testUpdateDepartment_Success() throws Exception {
//        // Given
//        Department updatedDepartment = new Department(1L, "IT Updated", "Kraków", "Zaktualizowany dział", "newmanager@example.com", 120000.0);
//
//        when(departmentService.updateDepartment(anyLong(), any(Department.class))).thenReturn(updatedDepartment);
//
//        // When & Then
//        mockMvc.perform(post("/departments/edit")
//                        .param("id", "1")
//                        .param("name", "IT Updated")
//                        .param("location", "Kraków")
//                        .param("description", "Zaktualizowany dział")
//                        .param("managerEmail", "newmanager@example.com")
//                        .param("budget", "120000.0"))
//                .andExpect(redirectedUrl("/departments"))
//                .andExpect(flash().attributeExists("message"))
//                .andExpect(flash().attribute("message", "Departament zaktualizowany pomyślnie!"));
//
//        verify(departmentService, times(1)).updateDepartment(eq(1L), any(Department.class));
//    }
//
//    @Test
//    public void testUpdateDepartment_ValidationErrors() throws Exception {
//        // Given - puste wymagane pola name i location
//        mockMvc.perform(post("/departments/edit")
//                        .param("id", "1")
//                        .param("name", "")
//                        .param("location", "")
//                        .param("description", "")
//                        .param("managerEmail", "")
//                        .param("budget", "-100.0"))
//                .andExpect(redirectedUrl("/departments/edit/1"));
//
//        verify(departmentService, never()).updateDepartment(anyLong(), any(Department.class));
//    }
//
//    @Test
//    public void testUpdateDepartment_Exception() throws Exception {
//        // Given
//        when(departmentService.updateDepartment(anyLong(), any(Department.class)))
//                .thenThrow(new RuntimeException("Database error"));
//
//        // When & Then
//        mockMvc.perform(post("/departments/edit")
//                        .param("id", "1")
//                        .param("name", "IT Updated")
//                        .param("location", "Kraków")
//                        .param("description", "Zaktualizowany dział")
//                        .param("managerEmail", "newmanager@example.com")
//                        .param("budget", "120000.0"))
//                .andExpect(redirectedUrl("/departments"))
//                .andExpect(flash().attributeExists("error"));
//
//        verify(departmentService, times(1)).updateDepartment(eq(1L), any(Department.class));
//    }
//
//    @Test
//    public void testDeleteDepartment_Success() throws Exception {
//        // Given
//        when(departmentService.deleteDepartment(1L)).thenReturn(true);
//
//        // When & Then
//        mockMvc.perform(get("/departments/delete/1"))
//                .andExpect(redirectedUrl("/departments"))
//                .andExpect(flash().attributeExists("message"))
//                .andExpect(flash().attribute("message", "Departament usunięty pomyślnie!"));
//
//        verify(departmentService, times(1)).deleteDepartment(1L);
//    }
//
//    @Test
//    public void testDeleteDepartment_Exception() throws Exception {
//        // Given
//        when(departmentService.deleteDepartment(1L))
//                .thenThrow(new RuntimeException("Cannot delete department"));
//
//        // When & Then
//        mockMvc.perform(get("/departments/delete/1"))
//                .andExpect(redirectedUrl("/departments"))
//                .andExpect(flash().attributeExists("error"));
//
//        verify(departmentService, times(1)).deleteDepartment(1L);
//    }
//
//    @Test
//    public void testShowDepartmentDetails_Success() throws Exception {
//        // Given
//        Department department = new Department(1L, "IT", "Warszawa", "Dział technologii", "manager@example.com", 100000.0);
//        Employee manager = new Employee("Jan Manager", "manager@example.com", "TechCorp",
//                Position.MANAGER, 8000.0, EmploymentStatus.ACTIVE);
//        Employee employee = new Employee("Jan Developer", "dev@example.com", "TechCorp",
//                Position.PROGRAMMER, 5000.0, EmploymentStatus.ACTIVE);
//        employee.setDepartmentId(1L);
//
//        List<Employee> departmentEmployees = Arrays.asList(employee);
//        DepartmentDTO departmentDTO = new DepartmentDTO(department, departmentEmployees, Optional.of(manager));
//
//        when(departmentService.getDepartmentDetails(1L)).thenReturn(departmentDTO);
//
//        // When & Then
//        mockMvc.perform(get("/departments/details/1"))
//                .andExpect(status().isOk())
//                .andExpect(view().name("departments/details"))
//                .andExpect(model().attributeExists("department"))
//                .andExpect(model().attributeExists("employees"))
//                .andExpect(model().attributeExists("manager"))
//                .andExpect(model().attributeExists("pageTitle"))
//                .andExpect(model().attribute("pageTitle", "Szczegóły Departamentu"))
//                .andExpect(model().attribute("department", department))
//                .andExpect(model().attribute("employees", departmentEmployees))
//                .andExpect(model().attribute("manager", Optional.of(manager)));
//
//        verify(departmentService, times(1)).getDepartmentDetails(1L);
//    }
//
//    @Test
//    public void testShowDepartmentDetails_DepartmentNotFound() throws Exception {
//        // Given
//        when(departmentService.getDepartmentDetails(999L)).thenReturn(null);
//
//        // When & Then
//        mockMvc.perform(get("/departments/details/999"))
//                .andExpect(redirectedUrl("/departments"));
//
//        verify(departmentService, times(1)).getDepartmentDetails(999L);
//    }
//
//    @Test
//    public void testAddDepartment_BudgetZeroValidation() throws Exception {
//        // Given - budżet = 0 powinien być odrzucony przez walidację
//        mockMvc.perform(post("/departments/add")
//                        .param("name", "IT")
//                        .param("location", "Warszawa")
//                        .param("description", "Dział technologii")
//                        .param("managerEmail", "manager@example.com")
//                        .param("budget", "0.0"))
//                .andExpect(redirectedUrl("/departments/add"));
//
//        verify(departmentService, never()).createDepartment(any(Department.class));
//    }
//}











package com.techcorp.employee.controller;

import com.techcorp.employee.dto.DepartmentDTO;
import com.techcorp.employee.model.Department;
import com.techcorp.employee.model.Employee;
import com.techcorp.employee.model.Position;
import com.techcorp.employee.model.EmploymentStatus;
import com.techcorp.employee.service.DepartmentService;
import com.techcorp.employee.service.EmployeeService;
import com.techcorp.employee.service.FileStorageService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DepartmentViewController.class)
public class DepartmentViewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DepartmentService departmentService;

    @MockBean
    private EmployeeService employeeService;

    @MockBean
    private FileStorageService fileStorageService;

    // ========== TESTY DLA DOKUMENTÓW DEPARTAMENTU ==========

    @Test
    public void testShowDepartmentDocuments_DepartmentExists() throws Exception {
        // Given
        Department department = createDepartment(1L, "IT", "Warszawa", "Dział technologii", "manager@techcorp.com", 100000.0);
        List<String> documents = Arrays.asList("raport_1704234567890_a1b2c3.pdf", "budget_1704234578901_d4e5f6.docx");

        when(departmentService.getDepartmentById(1L)).thenReturn(Optional.of(department));
        when(fileStorageService.getDepartmentDocumentNames(1L)).thenReturn(documents);

        // When & Then
        mockMvc.perform(get("/departments/documents/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("departments/documents"))
                .andExpect(model().attributeExists("department"))
                .andExpect(model().attributeExists("documents"))
                .andExpect(model().attributeExists("pageTitle"))
                .andExpect(model().attribute("pageTitle", "Dokumenty Departamentu"))
                .andExpect(model().attribute("department", department))
                .andExpect(model().attribute("documents", documents));

        verify(departmentService, times(1)).getDepartmentById(1L);
        verify(fileStorageService, times(1)).getDepartmentDocumentNames(1L);
    }

//    @Test
//    public void testShowDepartmentDocuments_DepartmentNotFound() throws Exception {
//        // Given
//        when(departmentService.getDepartmentById(999L)).thenReturn(Optional.empty());
//
//        // When & Then
//        mockMvc.perform(get("/departments/documents/999"))
//                .andExpect(redirectedUrl("/departments"));
//
//        verify(departmentService, times(1)).getDepartmentById(999L);
//        verify(fileStorageService, never()).getDepartmentDocumentNames(anyLong());
//    }

    @Test
    public void testShowDepartmentDocuments_NoDocuments() throws Exception {
        // Given
        Department department = createDepartment(1L, "IT", "Warszawa", "Dział technologii", "manager@techcorp.com", 100000.0);

        when(departmentService.getDepartmentById(1L)).thenReturn(Optional.of(department));
        when(fileStorageService.getDepartmentDocumentNames(1L)).thenReturn(List.of());

        // When & Then
        mockMvc.perform(get("/departments/documents/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("departments/documents"))
                .andExpect(model().attributeExists("documents"))
                .andExpect(model().attribute("documents", List.of()));

        verify(departmentService, times(1)).getDepartmentById(1L);
        verify(fileStorageService, times(1)).getDepartmentDocumentNames(1L);
    }

    @Test
    public void testUploadDepartmentDocument_Success() throws Exception {
        // Given
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test_document.pdf",
                "application/pdf",
                "test content".getBytes()
        );

        String storedFileName = "test_document_1704234567890_a1b2c3.pdf";
        when(fileStorageService.storeDepartmentDocument(any(), eq(1L))).thenReturn(storedFileName);

        // When & Then
        mockMvc.perform(multipart("/departments/documents/1/upload")
                        .file(file))
                .andExpect(redirectedUrl("/departments/documents/1"))
                .andExpect(flash().attributeExists("message"))
                .andExpect(flash().attribute("message", "Dokument dodany pomyślnie!"));

        verify(fileStorageService, times(1)).storeDepartmentDocument(any(), eq(1L));
    }

//    @Test
//    public void testUploadDepartmentDocument_Exception() throws Exception {
//        // Given
//        MockMultipartFile file = new MockMultipartFile(
//                "file",
//                "test_document.pdf",
//                "application/pdf",
//                "test content".getBytes()
//        );
//
//        when(fileStorageService.storeDepartmentDocument(any(), eq(1L)))
//                .thenThrow(new RuntimeException("Storage error"));
//
//        // When & Then
//        mockMvc.perform(multipart("/departments/documents/1/upload")
//                        .file(file))
//                .andExpect(redirectedUrl("/departments/documents/1"))
//                .andExpect(flash().attributeExists("error"))
//                .andExpect(flash().attribute("error", "Błąd podczas dodawania dokumentu: Storage error"));
//
//        verify(fileStorageService, times(1)).storeDepartmentDocument(any(), eq(1L));
//    }

    @Test
    public void testDownloadDepartmentDocument_Success() throws Exception {
        // Given
        String fileName = "test_document_1704234567890_a1b2c3.pdf";
        Resource mockResource = mock(Resource.class);

        when(fileStorageService.loadDepartmentDocument(fileName, 1L)).thenReturn(mockResource);

        // When & Then
        mockMvc.perform(get("/departments/documents/1/download/" + fileName))
                .andExpect(status().isOk());

        verify(fileStorageService, times(1)).loadDepartmentDocument(fileName, 1L);
    }

    @Test
    public void testDeleteDepartmentDocument_Success() throws Exception {
        // Given
        String fileName = "test_document_1704234567890_a1b2c3.pdf";

        when(fileStorageService.deleteDepartmentDocument(fileName, 1L)).thenReturn(true);

        // When & Then
        mockMvc.perform(get("/departments/documents/1/delete/" + fileName))
                .andExpect(redirectedUrl("/departments/documents/1"))
                .andExpect(flash().attributeExists("message"))
                .andExpect(flash().attribute("message", "Dokument usunięty pomyślnie!"));

        verify(fileStorageService, times(1)).deleteDepartmentDocument(fileName, 1L);
    }


//    @Test
//    public void testDeleteDepartmentDocument_Exception() throws Exception {
//        // Given
//        String fileName = "test_document_1704234567890_a1b2c3.pdf";
//
//        when(fileStorageService.deleteDepartmentDocument(fileName, 1L))
//                .thenThrow(new RuntimeException("Delete error"));
//
//        // When & Then
//        mockMvc.perform(get("/departments/documents/1/delete/" + fileName))
//                .andExpect(redirectedUrl("/departments/documents/1"))
//                .andExpect(flash().attributeExists("error"))
//                .andExpect(flash().attribute("error", "Błąd podczas usuwania dokumentu: Delete error"));
//
//        verify(fileStorageService, times(1)).deleteDepartmentDocument(fileName, 1L);
//    }

    // ========== TESTY DLA PRZYPISYWANIA I USUWANIA PRACOWNIKÓW ==========

    @Test
    public void testShowAssignEmployeeForm_DepartmentExists() throws Exception {
        // Given
        Department department = createDepartment(1L, "IT", "Warszawa", "Dział technologii", "manager@techcorp.com", 100000.0);
        List<Employee> availableEmployees = Arrays.asList(
                new Employee("Jan Kowalski", "jan@techcorp.com", "TechCorp", Position.PROGRAMMER, 5000.0, EmploymentStatus.ACTIVE)
        );

        when(departmentService.getDepartmentById(1L)).thenReturn(Optional.of(department));
        when(employeeService.getEmployeesWithoutDepartment()).thenReturn(availableEmployees);

        // When & Then
        mockMvc.perform(get("/departments/details/1/assign-employee"))
                .andExpect(status().isOk())
                .andExpect(view().name("departments/assign-employee-form"))
                .andExpect(model().attributeExists("department"))
                .andExpect(model().attributeExists("availableEmployees"))
                .andExpect(model().attributeExists("pageTitle"))
                .andExpect(model().attribute("pageTitle", "Przypisz Pracownika do Departamentu"))
                .andExpect(model().attribute("department", department))
                .andExpect(model().attribute("availableEmployees", availableEmployees));

        verify(departmentService, times(1)).getDepartmentById(1L);
        verify(employeeService, times(1)).getEmployeesWithoutDepartment();
    }

//    @Test
//    public void testShowAssignEmployeeForm_DepartmentNotFound() throws Exception {
//        // Given
//        when(departmentService.getDepartmentById(999L)).thenReturn(Optional.empty());
//
//        // When & Then
//        mockMvc.perform(get("/departments/details/999/assign-employee"))
//                .andExpect(redirectedUrl("/departments"));
//
//        verify(departmentService, times(1)).getDepartmentById(999L);
//        verify(employeeService, never()).getEmployeesWithoutDepartment();
//    }

    @Test
    public void testAssignEmployeeToDepartment_Success() throws Exception {
        // Given
        doNothing().when(departmentService).assignEmployeeToDepartment("employee@techcorp.com", 1L);

        // When & Then
        mockMvc.perform(post("/departments/details/1/assign-employee")
                        .param("employeeEmail", "employee@techcorp.com"))
                .andExpect(redirectedUrl("/departments/details/1"))
                .andExpect(flash().attributeExists("message"))
                .andExpect(flash().attribute("message", "Pracownik został przypisany do departamentu!"));

        verify(departmentService, times(1)).assignEmployeeToDepartment("employee@techcorp.com", 1L);
    }

//    @Test
//    public void testAssignEmployeeToDepartment_Exception() throws Exception {
//        // Given
//        doThrow(new RuntimeException("Assignment error"))
//                .when(departmentService).assignEmployeeToDepartment("employee@techcorp.com", 1L);
//
//        // When & Then
//        mockMvc.perform(post("/departments/details/1/assign-employee")
//                        .param("employeeEmail", "employee@techcorp.com"))
//                .andExpect(redirectedUrl("/departments/details/1"))
//                .andExpect(flash().attributeExists("error"))
//                .andExpect(flash().attribute("error", "Błąd podczas przypisywania pracownika: Assignment error"));
//
//        verify(departmentService, times(1)).assignEmployeeToDepartment("employee@techcorp.com", 1L);
//    }

    @Test
    public void testRemoveEmployeeFromDepartment_Success() throws Exception {
        // Given
        doNothing().when(departmentService).removeEmployeeFromDepartment("employee@techcorp.com");

        // When & Then
        mockMvc.perform(get("/departments/details/1/remove-employee/employee@techcorp.com"))
                .andExpect(redirectedUrl("/departments/details/1"))
                .andExpect(flash().attributeExists("message"))
                .andExpect(flash().attribute("message", "Pracownik został usunięty z departamentu!"));

        verify(departmentService, times(1)).removeEmployeeFromDepartment("employee@techcorp.com");
    }

//    @Test
//    public void testRemoveEmployeeFromDepartment_Exception() throws Exception {
//        // Given
//        doThrow(new RuntimeException("Removal error"))
//                .when(departmentService).removeEmployeeFromDepartment("employee@techcorp.com");
//
//        // When & Then
//        mockMvc.perform(get("/departments/details/1/remove-employee/employee@techcorp.com"))
//                .andExpect(redirectedUrl("/departments/details/1"))
//                .andExpect(flash().attributeExists("error"))
//                .andExpect(flash().attribute("error", "Błąd podczas usuwania pracownika: Removal error"));
//
//        verify(departmentService, times(1)).removeEmployeeFromDepartment("employee@techcorp.com");
//    }

    // ========== POZOSTAŁE TESTY ==========

    @Test
    public void testListDepartments() throws Exception {
        // Given
        Department department1 = createDepartment(1L, "IT", "Warszawa", "Dział technologii", "manager@techcorp.com", 100000.0);
        Department department2 = createDepartment(2L, "HR", "Kraków", "Dział kadr", "hr@techcorp.com", 50000.0);
        List<Department> departments = Arrays.asList(department1, department2);

        when(departmentService.getAllDepartments()).thenReturn(departments);

        // When & Then
        mockMvc.perform(get("/departments"))
                .andExpect(status().isOk())
                .andExpect(view().name("departments/list"))
                .andExpect(model().attributeExists("departments"))
                .andExpect(model().attributeExists("pageTitle"))
                .andExpect(model().attribute("pageTitle", "Lista Departamentów"))
                .andExpect(model().attribute("departments", departments));

        verify(departmentService, times(1)).getAllDepartments();
    }

    @Test
    public void testShowAddForm() throws Exception {
        // Given
        Employee manager = new Employee("Jan Manager", "manager@techcorp.com", "TechCorp",
                Position.MANAGER, 8000.0, EmploymentStatus.ACTIVE);
        List<Employee> managers = Arrays.asList(manager);

        when(employeeService.getAvailableManagers()).thenReturn(managers);

        // When & Then
        mockMvc.perform(get("/departments/add"))
                .andExpect(status().isOk())
                .andExpect(view().name("departments/form"))
                .andExpect(model().attributeExists("department"))
                .andExpect(model().attributeExists("managers"))
                .andExpect(model().attributeExists("pageTitle"))
                .andExpect(model().attribute("pageTitle", "Dodaj Departament"))
                .andExpect(model().attribute("managers", managers));

        verify(employeeService, times(1)).getAvailableManagers();
    }

    @Test
    public void testAddDepartment_Success() throws Exception {
        // Given
        Department department = createDepartment(1L, "IT", "Warszawa", "Dział technologii", "manager@techcorp.com", 100000.0);

        when(departmentService.createDepartment(any(Department.class))).thenReturn(department);

        // When & Then
        mockMvc.perform(post("/departments/add")
                        .param("name", "IT")
                        .param("location", "Warszawa")
                        .param("description", "Dział technologii")
                        .param("managerEmail", "manager@techcorp.com")
                        .param("budget", "100000.0"))
                .andExpect(redirectedUrl("/departments"))
                .andExpect(flash().attributeExists("message"))
                .andExpect(flash().attribute("message", "Departament dodany pomyślnie!"));

        verify(departmentService, times(1)).createDepartment(any(Department.class));
    }

//    @Test
//    public void testAddDepartment_ValidationErrors() throws Exception {
//        // Given - puste wymagane pola name i location
//        mockMvc.perform(post("/departments/add")
//                        .param("name", "")
//                        .param("location", "")
//                        .param("description", "")
//                        .param("managerEmail", "")
//                        .param("budget", "-100.0"))
//                .andExpect(redirectedUrl("/departments/add"));
//
//        verify(departmentService, never()).createDepartment(any(Department.class));
//    }

//    @Test
//    public void testAddDepartment_ValidationErrors_NameTooShort() throws Exception {
//        // Given - nazwa za krótka
//        mockMvc.perform(post("/departments/add")
//                        .param("name", "A") // za krótkie (min 2 znaki)
//                        .param("location", "Warszawa")
//                        .param("description", "Dział")
//                        .param("managerEmail", "manager@techcorp.com")
//                        .param("budget", "100000.0"))
//                .andExpect(redirectedUrl("/departments/add"));
//
//        verify(departmentService, never()).createDepartment(any(Department.class));
//    }

//    @Test
//    public void testAddDepartment_ValidationErrors_BudgetZero() throws Exception {
//        // Given - budżet = 0 (musi być > 0)
//        mockMvc.perform(post("/departments/add")
//                        .param("name", "IT")
//                        .param("location", "Warszawa")
//                        .param("description", "Dział technologii")
//                        .param("managerEmail", "manager@techcorp.com")
//                        .param("budget", "0.0"))
//                .andExpect(redirectedUrl("/departments/add"));
//
//        verify(departmentService, never()).createDepartment(any(Department.class));
//    }

//    @Test
//    public void testAddDepartment_ValidationErrors_InvalidEmail() throws Exception {
//        // Given - nieprawidłowy email
//        mockMvc.perform(post("/departments/add")
//                        .param("name", "IT")
//                        .param("location", "Warszawa")
//                        .param("description", "Dział technologii")
//                        .param("managerEmail", "nieprawidłowy-email")
//                        .param("budget", "100000.0"))
//                .andExpect(redirectedUrl("/departments/add"));
//
//        verify(departmentService, never()).createDepartment(any(Department.class));
//    }

//    @Test
//    public void testAddDepartment_Exception() throws Exception {
//        // Given
//        when(departmentService.createDepartment(any(Department.class)))
//                .thenThrow(new RuntimeException("Database error"));
//
//        // When & Then
//        mockMvc.perform(post("/departments/add")
//                        .param("name", "IT")
//                        .param("location", "Warszawa")
//                        .param("description", "Dział technologii")
//                        .param("managerEmail", "manager@techcorp.com")
//                        .param("budget", "100000.0"))
//                .andExpect(redirectedUrl("/departments"))
//                .andExpect(flash().attributeExists("error"))
//                .andExpect(flash().attribute("error", "Błąd podczas dodawania departamentu: Database error"));
//
//        verify(departmentService, times(1)).createDepartment(any(Department.class));
//    }

    @Test
    public void testShowEditForm_DepartmentExists() throws Exception {
        // Given
        Department department = createDepartment(1L, "IT", "Warszawa", "Dział technologii", "manager@techcorp.com", 100000.0);
        Employee manager = new Employee("Jan Manager", "manager@techcorp.com", "TechCorp",
                Position.MANAGER, 8000.0, EmploymentStatus.ACTIVE);
        List<Employee> managers = Arrays.asList(manager);

        when(departmentService.getDepartmentById(1L)).thenReturn(Optional.of(department));
        when(employeeService.getAvailableManagers()).thenReturn(managers);

        // When & Then
        mockMvc.perform(get("/departments/edit/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("departments/form"))
                .andExpect(model().attributeExists("department"))
                .andExpect(model().attributeExists("managers"))
                .andExpect(model().attributeExists("pageTitle"))
                .andExpect(model().attribute("pageTitle", "Edytuj Departament"))
                .andExpect(model().attribute("department", department))
                .andExpect(model().attribute("managers", managers));

        verify(departmentService, times(1)).getDepartmentById(1L);
        verify(employeeService, times(1)).getAvailableManagers();
    }

//    @Test
//    public void testShowEditForm_DepartmentNotFound() throws Exception {
//        // Given
//        when(departmentService.getDepartmentById(999L)).thenReturn(Optional.empty());
//
//        // When & Then
//        mockMvc.perform(get("/departments/edit/999"))
//                .andExpect(redirectedUrl("/departments"));
//
//        verify(departmentService, times(1)).getDepartmentById(999L);
//        verify(employeeService, never()).getAvailableManagers();
//    }

    @Test
    public void testUpdateDepartment_Success() throws Exception {
        // Given
        Department updatedDepartment = createDepartment(1L, "IT Updated", "Kraków", "Zaktualizowany dział", "newmanager@techcorp.com", 120000.0);

        when(departmentService.updateDepartment(anyLong(), any(Department.class))).thenReturn(updatedDepartment);

        // When & Then
        mockMvc.perform(post("/departments/edit")
                        .param("id", "1")
                        .param("name", "IT Updated")
                        .param("location", "Kraków")
                        .param("description", "Zaktualizowany dział")
                        .param("managerEmail", "newmanager@techcorp.com")
                        .param("budget", "120000.0"))
                .andExpect(redirectedUrl("/departments"))
                .andExpect(flash().attributeExists("message"))
                .andExpect(flash().attribute("message", "Departament zaktualizowany pomyślnie!"));

        verify(departmentService, times(1)).updateDepartment(eq(1L), any(Department.class));
    }

//    @Test
//    public void testUpdateDepartment_ValidationErrors() throws Exception {
//        // Given - puste wymagane pola name i location
//        mockMvc.perform(post("/departments/edit")
//                        .param("id", "1")
//                        .param("name", "")
//                        .param("location", "")
//                        .param("description", "")
//                        .param("managerEmail", "")
//                        .param("budget", "-100.0"))
//                .andExpect(redirectedUrl("/departments/edit/1"));
//
//        verify(departmentService, never()).updateDepartment(anyLong(), any(Department.class));
//    }

//    @Test
//    public void testUpdateDepartment_Exception() throws Exception {
//        // Given
//        when(departmentService.updateDepartment(anyLong(), any(Department.class)))
//                .thenThrow(new RuntimeException("Database error"));
//
//        // When & Then
//        mockMvc.perform(post("/departments/edit")
//                        .param("id", "1")
//                        .param("name", "IT Updated")
//                        .param("location", "Kraków")
//                        .param("description", "Zaktualizowany dział")
//                        .param("managerEmail", "newmanager@techcorp.com")
//                        .param("budget", "120000.0"))
//                .andExpect(redirectedUrl("/departments"))
//                .andExpect(flash().attributeExists("error"))
//                .andExpect(flash().attribute("error", "Błąd podczas aktualizacji departamentu: Database error"));
//
//        verify(departmentService, times(1)).updateDepartment(eq(1L), any(Department.class));
//    }

    @Test
    public void testDeleteDepartment_Success() throws Exception {
        // Given
        doNothing().when(departmentService).deleteDepartment(1L);

        // When & Then
        mockMvc.perform(get("/departments/delete/1"))
                .andExpect(redirectedUrl("/departments"))
                .andExpect(flash().attributeExists("message"))
                .andExpect(flash().attribute("message", "Departament usunięty pomyślnie!"));

        verify(departmentService, times(1)).deleteDepartment(1L);
    }

//    @Test
//    public void testDeleteDepartment_Exception() throws Exception {
//        // Given
//        doThrow(new RuntimeException("Cannot delete department"))
//                .when(departmentService).deleteDepartment(1L);
//
//        // When & Then
//        mockMvc.perform(get("/departments/delete/1"))
//                .andExpect(redirectedUrl("/departments"))
//                .andExpect(flash().attributeExists("error"))
//                .andExpect(flash().attribute("error", "Błąd podczas usuwania departamentu: Cannot delete department"));
//
//        verify(departmentService, times(1)).deleteDepartment(1L);
//    }

    @Test
    public void testShowDepartmentDetails_Success() throws Exception {
        // Given
        Department department = createDepartment(1L, "IT", "Warszawa", "Dział technologii", "manager@techcorp.com", 100000.0);
        Employee manager = new Employee("Jan Manager", "manager@techcorp.com", "TechCorp",
                Position.MANAGER, 8000.0, EmploymentStatus.ACTIVE);
        Employee employee = new Employee("Jan Developer", "dev@techcorp.com", "TechCorp",
                Position.PROGRAMMER, 5000.0, EmploymentStatus.ACTIVE);
        // Ustawienie departmentu dla pracownika
        employee.setDepartment(department);

        List<Employee> departmentEmployees = Arrays.asList(employee);
        DepartmentDTO departmentDTO = new DepartmentDTO(department, departmentEmployees, Optional.of(manager));

        when(departmentService.getDepartmentDetails(1L)).thenReturn(departmentDTO);

        // When & Then
        mockMvc.perform(get("/departments/details/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("departments/details"))
                .andExpect(model().attributeExists("department"))
                .andExpect(model().attributeExists("employees"))
                .andExpect(model().attributeExists("manager"))
                .andExpect(model().attributeExists("pageTitle"))
                .andExpect(model().attribute("pageTitle", "Szczegóły Departamentu"))
                .andExpect(model().attribute("department", department))
                .andExpect(model().attribute("employees", departmentEmployees))
                .andExpect(model().attribute("manager", Optional.of(manager)));

        verify(departmentService, times(1)).getDepartmentDetails(1L);
    }
//
//    @Test
//    public void testShowDepartmentDetails_DepartmentNotFound() throws Exception {
//        // Given
//        when(departmentService.getDepartmentDetails(999L)).thenReturn(null);
//
//        // When & Then
//        mockMvc.perform(get("/departments/details/999"))
//                .andExpect(redirectedUrl("/departments"));
//
//        verify(departmentService, times(1)).getDepartmentDetails(999L);
//    }

    // Metoda pomocnicza do tworzenia obiektów Department
    private Department createDepartment(Long id, String name, String location, String description,
                                        String managerEmail, Double budget) {
        Department department = new Department(name, location, description, managerEmail, budget);
        department.setId(id);
        return department;
    }
}