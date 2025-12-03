package com.techcorp.employee.service;

import com.techcorp.employee.dto.EmployeeListView;
import com.techcorp.employee.model.*;
import com.techcorp.employee.repository.EmployeeRepository;
import com.techcorp.employee.repository.DepartmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@TestPropertySource(locations = "classpath:application-test.properties")
class EmployeeServicePaginationTest {

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @BeforeEach
    void setUp() {
        employeeRepository.deleteAll();
        departmentRepository.deleteAll();

        // Create 25 employees for pagination tests
        for (int i = 1; i <= 25; i++) {
            String company = i % 2 == 0 ? "TechCorp" : "OtherCorp";
            Position position = i % 3 == 0 ? Position.PROGRAMMER :
                    i % 3 == 1 ? Position.MANAGER : Position.PRESIDENT;
            EmploymentStatus status = i % 4 == 0 ? EmploymentStatus.TERMINATED : EmploymentStatus.ACTIVE;

            Employee emp = new Employee(
                    "Employee " + i,
                    "emp" + i + "@" + company.toLowerCase() + ".com",
                    company,
                    position,
                    3000.0 + i * 100,
                    status
            );
            employeeRepository.save(emp);
        }
    }

    // ===== TESTOWANIE RZECZYWISTYCH METOD Z EmployeeService =====

    @Test
    void testGetAllEmployees_withPagination() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        // When - testujemy rzeczywistą metodę
        Page<Employee> page1 = employeeService.getAllEmployees(pageable);
        Page<Employee> page2 = employeeService.getAllEmployees(PageRequest.of(1, 10));
        Page<Employee> page3 = employeeService.getAllEmployees(PageRequest.of(2, 10));

        // Then
        assertThat(page1.getContent()).hasSize(10);
        assertThat(page2.getContent()).hasSize(10);
        assertThat(page3.getContent()).hasSize(5); // 25-20 = 5

        assertThat(page1.getTotalElements()).isEqualTo(25);
        assertThat(page1.getTotalPages()).isEqualTo(3); // 25/10 = 3 pages
    }

    @Test
    void testGetEmployeesByStatusProjection_withPagination() {
        // Given
        Pageable pageable = PageRequest.of(0, 5);

        // When - testujemy rzeczywistą metodę
        Page<EmployeeListView> result = employeeService.getEmployeesByStatusProjection(
                EmploymentStatus.ACTIVE, pageable);

        // Then
        assertThat(result.getTotalElements()).isGreaterThan(0);

    }

    @Test
    void testGetEmployeesByCompanyProjection_withPagination() {
        // Given
        Pageable pageable = PageRequest.of(0, 5);

        // When - testujemy rzeczywistą metodę
        Page<EmployeeListView> result = employeeService.getEmployeesByCompanyProjection("TechCorp", pageable);

        // Then
        assertThat(result.getTotalElements()).isGreaterThan(0);
        if (!result.getContent().isEmpty()) {
            assertThat(result.getContent().get(0).getCompany()).isEqualTo("TechCorp");
        }
    }

    @Test
    void testSearchEmployeesAdvanced_withPagination() {
        // Given
        Pageable pageable = PageRequest.of(0, 5);

        // When - testujemy rzeczywistą metodę
        Page<EmployeeListView> result = employeeService.searchEmployeesAdvanced(
                null, "TechCorp", Position.PROGRAMMER, EmploymentStatus.ACTIVE,
                null, null, null, pageable);

        // Then
        assertThat(result.getTotalElements()).isGreaterThan(0);
        if (!result.getContent().isEmpty()) {
            EmployeeListView first = result.getContent().get(0);
            assertThat(first.getCompany()).isEqualTo("TechCorp");
            assertThat(first.getPosition()).isEqualTo("PROGRAMMER");
        }
    }

    @Test
    void testGetEmployeesByStatus_deprecated_withPagination() {
        // Given
        Pageable pageable = PageRequest.of(0, 5);

        // When - testujemy przestarzałą metodę (ale nadal istniejącą)
        Page<Employee> result = employeeService.getEmployeesByStatus(EmploymentStatus.ACTIVE, pageable);

        // Then
        assertThat(result.getTotalElements()).isGreaterThan(0);
        if (!result.getContent().isEmpty()) {
            assertThat(result.getContent().get(0).getStatus()).isEqualTo(EmploymentStatus.ACTIVE);
        }
    }

    @Test
    void testGetEmployeesByCompany_deprecated_withPagination() {
        // Given
        Pageable pageable = PageRequest.of(0, 5);

        // When - testujemy przestarzałą metodę (ale nadal istniejącą)
        Page<Employee> result = employeeService.getEmployeesByCompany("TechCorp", pageable);

        // Then
        assertThat(result.getTotalElements()).isGreaterThan(0);
        if (!result.getContent().isEmpty()) {
            assertThat(result.getContent().get(0).getCompany()).isEqualTo("TechCorp");
        }
    }

    // ===== TESTOWANIE SORTOWANIA =====

    @Test
    void testSortingWithPagination() {
        // Given - sortuj po salary DESC
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "salary"));

        // When
        Page<Employee> result = employeeService.getAllEmployees(pageable);

        // Then - sprawdź czy sortowanie działa
        List<Employee> employees = result.getContent();
        for (int i = 0; i < employees.size() - 1; i++) {
            assertThat(employees.get(i).getSalary())
                    .isGreaterThanOrEqualTo(employees.get(i + 1).getSalary());
        }
    }

    @Test
    void testSortingByNameWithPagination() {
        // Given - sortuj po name ASC
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "name"));

        // When
        Page<Employee> result = employeeService.getAllEmployees(pageable);

        // Then - sprawdź czy sortowanie alfabetyczne działa
        List<Employee> employees = result.getContent();
        for (int i = 0; i < employees.size() - 1; i++) {
            String name1 = employees.get(i).getName();
            String name2 = employees.get(i + 1).getName();
            assertThat(name1.compareTo(name2)).isLessThanOrEqualTo(0);
        }
    }

    // ===== TESTOWANIE METOD LICZĄCYCH =====

    @Test
    void testGetEmployeeCountByCompany_withPagination() {
        // Given
        long techCorpCount = employeeService.getEmployeeCountByCompany("TechCorp");
        long otherCorpCount = employeeService.getEmployeeCountByCompany("OtherCorp");

        // When - pobierz strony
        Page<EmployeeListView> techCorpPage = employeeService.getEmployeesByCompanyProjection(
                "TechCorp", PageRequest.of(0, 100));
        Page<EmployeeListView> otherCorpPage = employeeService.getEmployeesByCompanyProjection(
                "OtherCorp", PageRequest.of(0, 100));

        // Then
        assertThat(techCorpPage.getTotalElements()).isEqualTo(techCorpCount);
        assertThat(otherCorpPage.getTotalElements()).isEqualTo(otherCorpCount);
        assertThat(techCorpCount + otherCorpCount).isEqualTo(25);
    }

    @Test
    void testGetEmployeeCountByStatus_withPagination() {
        // Given
        long activeCount = employeeService.getEmployeeCountByStatus(EmploymentStatus.ACTIVE);

        // When
        Page<EmployeeListView> activePage = employeeService.getEmployeesByStatusProjection(
                EmploymentStatus.ACTIVE, PageRequest.of(0, 100));

        // Then
        assertThat(activePage.getTotalElements()).isEqualTo(activeCount);
    }

    // ===== TESTOWANIE PRZYPADKÓW BRZEGOWYCH =====

    @Test
    void testEmptyPage() {
        // Given - strona poza zakresem
        Pageable pageable = PageRequest.of(100, 10); // strona 100, 10 na stronę

        // When
        Page<Employee> result = employeeService.getAllEmployees(pageable);

        // Then
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(25);
        assertThat(result.getTotalPages()).isEqualTo(3);
    }

    @Test
    void testSingleEmployeePerPage() {
        // Given - 1 pracownik na stronę
        Pageable pageable = PageRequest.of(0, 1);

        // When
        Page<Employee> result = employeeService.getAllEmployees(pageable);

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(25);
        assertThat(result.getTotalPages()).isEqualTo(25);
    }

    @Test
    void testNoSorting() {
        // Given - bez sortowania
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Employee> result = employeeService.getAllEmployees(pageable);

        // Then
        assertThat(result.getContent()).hasSize(10);
        assertThat(result.getSort().isSorted()).isFalse();
    }

    // ===== TESTOWANIE STATYSTYK =====

    @Test
    void testCompanyStatistics_withPaginationVerification() {
        // Given
        List<String> companies = employeeService.getAllUniqueCompanies();

        // When & Then - dla każdej firmy sprawdź czy liczba na stronie zgadza się ze statystykami
        for (String company : companies) {
            long count = employeeService.getEmployeeCountByCompany(company);
            Page<EmployeeListView> page = employeeService.getEmployeesByCompanyProjection(
                    company, PageRequest.of(0, 100));

            assertThat(page.getTotalElements()).isEqualTo(count);
            assertThat(page.getContent()).hasSize((int) count);
        }
    }

    // ===== TESTOWANIE METOD Z PROJEKCJĄ =====

    @Test
    void testProjectionFields() {
        // Given
        Pageable pageable = PageRequest.of(0, 5);

        // When
        Page<EmployeeListView> result = employeeService.getEmployeesByStatusProjection(
                EmploymentStatus.ACTIVE, pageable);

        // Then - sprawdź czy projekcja zwraca wszystkie wymagane pola
        if (!result.getContent().isEmpty()) {
            EmployeeListView view = result.getContent().get(0);

            assertThat(view.getName()).isNotNull();
            assertThat(view.getCompany()).isNotNull();
            assertThat(view.getPosition()).isNotNull();
            assertThat(view.getDepartmentName()).isNotNull();
        }
    }

    @Test
    void testProjectionConsistency() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        // When - pobierz te same dane przez dwie różne metody
        Page<Employee> employeesPage = employeeService.getEmployeesByStatus(EmploymentStatus.ACTIVE, pageable);
        Page<EmployeeListView> projectionPage = employeeService.getEmployeesByStatusProjection(
                EmploymentStatus.ACTIVE, pageable);

        // Then - sprawdź czy liczba wyników jest taka sama
        assertThat(employeesPage.getTotalElements()).isEqualTo(projectionPage.getTotalElements());
        assertThat(employeesPage.getTotalPages()).isEqualTo(projectionPage.getTotalPages());
    }
}