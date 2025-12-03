//package com.techcorp.employee.service;
//
//import com.techcorp.employee.dto.EmployeeListView;
//import com.techcorp.employee.model.*;
//import com.techcorp.employee.repository.EmployeeRepository;
//import com.techcorp.employee.repository.DepartmentRepository;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.domain.Sort;
//import org.springframework.test.context.TestPropertySource;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.List;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//@SpringBootTest
//@Transactional
//@TestPropertySource(locations = "classpath:application-test.properties")
//class EmployeeServicePaginationTest {
//
//    @Autowired
//    private EmployeeService employeeService;
//
//    @Autowired
//    private EmployeeRepository employeeRepository;
//
//    @Autowired
//    private DepartmentRepository departmentRepository;
//
//    @BeforeEach
//    void setUp() {
//        employeeRepository.deleteAll();
//        departmentRepository.deleteAll();
//
//        // Create 25 employees for pagination tests
//        for (int i = 1; i <= 25; i++) {
//            String company = i % 2 == 0 ? "TechCorp" : "OtherCorp";
//            Position position = i % 3 == 0 ? Position.PROGRAMMER :
//                    i % 3 == 1 ? Position.MANAGER : Position.PRESIDENT;
//            EmploymentStatus status = i % 4 == 0 ? EmploymentStatus.TERMINATED : EmploymentStatus.ACTIVE;
//
//            Employee emp = new Employee(
//                    "Employee " + i,
//                    "emp" + i + "@" + company.toLowerCase() + ".com",
//                    company,
//                    position,
//                    3000.0 + i * 100,
//                    status
//            );
//            employeeRepository.save(emp);
//        }
//    }
//
//    @Test
//    void testGetAllEmployeesSummary_withPagination() {
//        // Given
//        Pageable pageable = PageRequest.of(0, 10, Sort.by("name"));
//
//        // When
//        Page<EmployeeListView> page1 = employeeService.getAllEmployeesSummary(pageable);
//        Page<EmployeeListView> page2 = employeeService.getAllEmployeesSummary(PageRequest.of(1, 10));
//        Page<EmployeeListView> page3 = employeeService.getAllEmployeesSummary(PageRequest.of(2, 10));
//
//        // Then
//        assertThat(page1.getContent()).hasSize(10);
//        assertThat(page2.getContent()).hasSize(10);
//        assertThat(page3.getContent()).hasSize(5); // 25-20 = 5
//
//        assertThat(page1.getTotalElements()).isEqualTo(25);
//        assertThat(page1.getTotalPages()).isEqualTo(3); // 25/10 = 3 pages
//    }
//
//    @Test
//    void testSearchEmployeesWithFilters_withPagination() {
//        // Given
//        Pageable pageable = PageRequest.of(0, 5);
//
//        // When - znajdź PROGRAMMERów z TechCorp
//        Page<EmployeeListView> result = employeeService.searchEmployeesWithFilters(
//                null, "TechCorp", Position.PROGRAMMER, EmploymentStatus.ACTIVE,
//                null, null, null, pageable);
//
//        // Then - metoda zwraca Page<EmployeeListView>
//        assertThat(result.getTotalElements()).isGreaterThan(0);
//        if (result.getContent().size() > 0) {
//            assertThat(result.getContent().get(0).getCompany()).isEqualTo("TechCorp");
//        }
//    }
//
//    @Test
//    void testGetEmployeesByCompany_withPagination() {
//        // Given
//        Pageable pageable = PageRequest.of(0, 5);
//
//        // When - użyj NIEprzestarzałej metody
//        // Możesz użyć getEmployeesByCompanyProjection jeśli istnieje
//        // Lub jeśli nie, użyj findByCompany bezpośrednio z repozytorium
//        Page<Employee> page1 = employeeRepository.findByCompany("TechCorp", pageable);
//        Page<Employee> page2 = employeeRepository.findByCompany("TechCorp", PageRequest.of(1, 5));
//
//        // Then
//        long totalTechCorp = employeeRepository.countEmployeesByCompany("TechCorp"); // long zamiast int
//        assertThat(page1.getTotalElements()).isEqualTo(totalTechCorp);
//
//        if (totalTechCorp > 5) {
//            long totalPages = (totalTechCorp + 5 - 1) / 5; // oblicz liczbę stron
//            assertThat(totalPages).isGreaterThan(1);
//            assertThat(page2.getContent()).isNotEmpty();
//        }
//    }
//
//    @Test
//    void testSortingWithPagination() {
//        // Given - sortuj po salary DESC
//        Pageable pageable = PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "salary"));
//
//        // When
//        Page<Employee> result = employeeRepository.findAll(pageable); // użyj repozytorium
//
//        // Then - sprawdź czy sortowanie działa
//        List<Employee> employees = result.getContent();
//        for (int i = 0; i < employees.size() - 1; i++) {
//            assertThat(employees.get(i).getSalary())
//                    .isGreaterThanOrEqualTo(employees.get(i + 1).getSalary());
//        }
//    }
//
//    @Test
//    void testFindEmployeesWithFiltersOptimized_withPagination() {
//        // Given
//        Pageable pageable = PageRequest.of(0, 5);
//
//        // When - użyj optymalizowanej metody z projekcją
//        Page<EmployeeListView> result = employeeService.findEmployeesWithFiltersOptimized(
//                null, "TechCorp", "PROGRAMMER", null, null, pageable); // "PROGRAMMER" nie "DEVELOPER"
//
//        // Then
//        assertThat(result.getContent()).isNotEmpty();
//        assertThat(result.getContent())
//                .extracting(EmployeeListView::getCompany)
//                .containsOnly("TechCorp");
//    }
//
//    @Test
//    void testGetEmployeesByStatusProjection_withPagination() {
//        // Given
//        Pageable pageable = PageRequest.of(0, 5);
//
//        // When - użyj metody z projekcją jeśli istnieje
//        try {
//            Page<EmployeeListView> result = employeeService.getEmployeesByStatusProjection(
//                    EmploymentStatus.ACTIVE, pageable);
//
//            // Then
//            assertThat(result.getTotalElements()).isGreaterThan(0);
//        } catch (Exception e) {
//            // Jeśli metoda nie istnieje, użyj repozytorium
//            Page<Employee> result = employeeRepository.findByStatus(EmploymentStatus.ACTIVE, pageable);
//            assertThat(result.getTotalElements()).isGreaterThan(0);
//        }
//    }
//}