package com.techcorp.employee.repository;

import com.techcorp.employee.model.*;
import com.techcorp.employee.dto.EmployeeListView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test") // Dodaj tę adnotację!
class EmployeeRepositoryDataJpaTest {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    private Department itDepartment;
    private Department hrDepartment;

    @BeforeEach
    void setUp() {
        // Czyszczenie bazy przed każdym testem
        employeeRepository.deleteAll();
        departmentRepository.deleteAll();

        // Tworzenie departamentów
        itDepartment = new Department("IT", "Warsaw", "IT Department", "it.manager@company.com", 150000.0);
        hrDepartment = new Department("HR", "Krakow", "Human Resources", "hr.manager@company.com", 80000.0);

        departmentRepository.saveAll(List.of(itDepartment, hrDepartment));

        // Tworzenie pracowników
        Employee emp1 = new Employee("Jan Kowalski", "jan.kowalski@techcorp.com", "TechCorp",
                Position.PROGRAMMER, 8500.0, EmploymentStatus.ACTIVE, itDepartment);
        Employee emp2 = new Employee("Anna Nowak", "anna.nowak@techcorp.com", "TechCorp",
                Position.MANAGER, 12000.0, EmploymentStatus.ACTIVE, hrDepartment);
        Employee emp3 = new Employee("Piotr Wiśniewski", "piotr.wisniewski@othercorp.com", "OtherCorp",
                Position.PROGRAMMER, 7500.0, EmploymentStatus.TERMINATED, null);
        Employee emp4 = new Employee("Maria Lewandowska", "maria.lewandowska@techcorp.com", "TechCorp",
                Position.PRESIDENT, 25000.0, EmploymentStatus.ACTIVE, null);
        Employee emp5 = new Employee("Tomasz Wójcik", "tomasz.wojcik@techcorp.com", "TechCorp",
                Position.PROGRAMMER, 9000.0, EmploymentStatus.ON_LEAVE, itDepartment);

        employeeRepository.saveAll(List.of(emp1, emp2, emp3, emp4, emp5));

        // Flush dla pewności
        employeeRepository.flush();
        departmentRepository.flush();
    }

    // ========== TESTY MAPOWANIA ENCJI (Wymaganie 1) ==========

    @Test
    void testEmployeeEntityMapping() {
        // Given
        String email = "jan.kowalski@techcorp.com";

        // When
        Optional<Employee> employeeOpt = employeeRepository.findByEmail(email);

        // Then
        assertThat(employeeOpt).isPresent();
        Employee employee = employeeOpt.get();

        // Podstawowe pola
        assertThat(employee.getId()).isNotNull();
        assertThat(employee.getName()).isEqualTo("Jan Kowalski");
        assertThat(employee.getEmail()).isEqualTo("jan.kowalski@techcorp.com");
        assertThat(employee.getCompany()).isEqualTo("TechCorp");
        assertThat(employee.getPosition()).isEqualTo(Position.PROGRAMMER);
        assertThat(employee.getSalary()).isEqualTo(8500.0);
        assertThat(employee.getStatus()).isEqualTo(EmploymentStatus.ACTIVE);

        // Relacja ManyToOne
        assertThat(employee.getDepartment()).isNotNull();
        assertThat(employee.getDepartment().getName()).isEqualTo("IT");
    }

    @Test
    void testDepartmentEntityMapping() {
        // When
        Optional<Department> deptOpt = departmentRepository.findByName("IT");

        // Then
        assertThat(deptOpt).isPresent();
        Department department = deptOpt.get();

        assertThat(department.getId()).isNotNull();
        assertThat(department.getName()).isEqualTo("IT");
        assertThat(department.getLocation()).isEqualTo("Warsaw");
        assertThat(department.getBudget()).isEqualTo(150000.0);
        assertThat(department.getManagerEmail()).isEqualTo("it.manager@company.com");
    }

    @Test
    void testEntityRelationships_Bidirectional() {
        // Given
        Optional<Department> deptOpt = departmentRepository.findByName("IT");
        assertThat(deptOpt).isPresent();
        Department department = deptOpt.get();

        // Relacja OneToMany - pobierz z eager/lazy
        List<Employee> employeesInDept = employeeRepository.findByDepartmentId(department.getId());

        // Then
        assertThat(employeesInDept).hasSize(2);
        assertThat(employeesInDept)
                .extracting(Employee::getName)
                .contains("Jan Kowalski", "Tomasz Wójcik");
    }

    @Test
    void testEmployeeWithoutDepartment() {
        // When
        List<Employee> employeesWithoutDept = employeeRepository.findByDepartmentIsNull();

        // Then
        assertThat(employeesWithoutDept).hasSize(2);
        assertThat(employeesWithoutDept)
                .extracting(Employee::getName)
                .contains("Piotr Wiśniewski", "Maria Lewandowska");
    }

    // ========== TESTY PROJEKCJI (Wymaganie 2) ==========

    @Test
    void testEmployeeListViewProjection() {
        // Given
        Pageable pageable = PageRequest.of(0, 10, Sort.by("name"));

        // When
        Page<EmployeeListView> result = employeeRepository.findAllEmployeesSummary(pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(5);

        // Sprawdź czy projekcja zwraca tylko potrzebne pola
        List<EmployeeListView> content = result.getContent();
        assertThat(content).isNotEmpty();

        EmployeeListView firstEmployee = content.get(0);
        assertThat(firstEmployee.getName()).isNotNull();
        assertThat(firstEmployee.getEmail()).isNotNull();
        assertThat(firstEmployee.getCompany()).isNotNull();
        assertThat(firstEmployee.getPosition()).isNotNull();
        assertThat(firstEmployee.getDepartmentName()).isNotNull();

        // Metody domyślne z interfejsu
        assertThat(firstEmployee.getFirstName()).isNotNull();
        assertThat(firstEmployee.getLastName()).isNotNull();
    }



    // ========== TESTY SPECIFICATIONS (Wymaganie 3) ==========

    @Test
    void testSpecifications_withDynamicFilters() {
        // Given
        Specification<Employee> spec = (root, query, criteriaBuilder) -> {
            return criteriaBuilder.and(
                    criteriaBuilder.equal(root.get("company"), "TechCorp"),
                    criteriaBuilder.equal(root.get("status"), EmploymentStatus.ACTIVE),
                    criteriaBuilder.greaterThanOrEqualTo(root.get("salary"), 8000.0)
            );
        };

        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Employee> result = employeeRepository.findAll(spec, pageable);

        // Then
        assertThat(result.getTotalElements()).isEqualTo(3); // Jan, Anna, Maria
        assertThat(result.getContent())
                .extracting(Employee::getCompany)
                .containsOnly("TechCorp");
        assertThat(result.getContent())
                .extracting(Employee::getStatus)
                .containsOnly(EmploymentStatus.ACTIVE);
        assertThat(result.getContent())
                .allMatch(e -> e.getSalary() >= 8000.0);
    }

    @Test
    void testSpecifications_withPartialFilters() {
        // Given - tylko firma
        Specification<Employee> spec = (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("company"), "TechCorp");

        // When
        List<Employee> result = employeeRepository.findAll(spec);

        // Then
        assertThat(result).hasSize(4);
        assertThat(result)
                .extracting(Employee::getCompany)
                .containsOnly("TechCorp");
    }

    @Test
    void testSpecifications_withNameLike() {
        // Given - imię zawiera "kowal"
        Specification<Employee> spec = (root, query, criteriaBuilder) ->
                criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("name")),
                        "%kowal%"
                );

        // When
        List<Employee> result = employeeRepository.findAll(spec);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Jan Kowalski");
    }

    // ========== TESTY PAGINACJI (Wymaganie 4) ==========

    @Test
    void testPaginationBasic() {
        // Given
        int pageSize = 2;
        long totalEmployees = employeeRepository.countAllEmployees();
        int expectedPages = (int) Math.ceil((double) totalEmployees / pageSize);

        // When & Then - iteracja przez wszystkie strony
        for (int page = 0; page < expectedPages; page++) {
            Pageable pageable = PageRequest.of(page, pageSize, Sort.by("name"));
            Page<Employee> resultPage = employeeRepository.findAll(pageable);

            assertThat(resultPage.getNumber()).isEqualTo(page);
            assertThat(resultPage.getSize()).isEqualTo(pageSize);
            assertThat(resultPage.getTotalElements()).isEqualTo(totalEmployees);
            assertThat(resultPage.getTotalPages()).isEqualTo(expectedPages);

            // Ostatnia strona może mieć mniej elementów
            int expectedContentSize = (page == expectedPages - 1)
                    ? (int) (totalEmployees % pageSize)
                    : pageSize;
            if (expectedContentSize == 0 && totalEmployees > 0) {
                expectedContentSize = pageSize; // gdy ostatnia pełna strona
            }

            assertThat(resultPage.getContent()).hasSize(expectedContentSize);
        }
    }

    @Test
    void testPaginationWithSorting() {
        // Given
        Pageable pageableAsc = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "salary"));
        Pageable pageableDesc = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "salary"));

        // When
        Page<Employee> resultAsc = employeeRepository.findAll(pageableAsc);
        Page<Employee> resultDesc = employeeRepository.findAll(pageableDesc);

        // Then - sprawdź sortowanie
        List<Double> salariesAsc = resultAsc.getContent().stream()
                .map(Employee::getSalary)
                .toList();
        List<Double> salariesDesc = resultDesc.getContent().stream()
                .map(Employee::getSalary)
                .toList();

        // Sprawdź czy rosnąco
        for (int i = 0; i < salariesAsc.size() - 1; i++) {
            assertThat(salariesAsc.get(i)).isLessThanOrEqualTo(salariesAsc.get(i + 1));
        }

        // Sprawdź czy malejąco
        for (int i = 0; i < salariesDesc.size() - 1; i++) {
            assertThat(salariesDesc.get(i)).isGreaterThanOrEqualTo(salariesDesc.get(i + 1));
        }
    }

    @Test
    void testFindByCompanyWithPagination() {
        // Given
        Pageable pageable = PageRequest.of(0, 2, Sort.by("name"));

        // When
        Page<Employee> page1 = employeeRepository.findByCompany("TechCorp", pageable);
        Page<Employee> page2 = employeeRepository.findByCompany("TechCorp",
                PageRequest.of(1, 2, Sort.by("name")));

        // Then
        assertThat(page1.getTotalElements()).isEqualTo(4);
        assertThat(page1.getTotalPages()).isEqualTo(2); // 4/2 = 2 strony

        assertThat(page1.getContent()).hasSize(2);
        assertThat(page2.getContent()).hasSize(2);

        // Sprawdź że strony nie zawierają tych samych pracowników
        List<String> page1Names = page1.getContent().stream()
                .map(Employee::getName)
                .toList();
        List<String> page2Names = page2.getContent().stream()
                .map(Employee::getName)
                .toList();

        assertThat(page1Names).doesNotContainAnyElementsOf(page2Names);
    }

    // ========== TESTY ZAAWANSOWANYCH FUNKCJI ==========

    @Test
    void testCustomQueryMethods() {
        // Test metod z @Query
        Double avgSalary = employeeRepository.findAverageSalary();
        Double maxSalary = employeeRepository.findMaxSalary();
        Long totalCount = employeeRepository.countAllEmployees();

        assertThat(avgSalary).isNotNull().isGreaterThan(0);
        assertThat(maxSalary).isNotNull().isGreaterThan(0);
        assertThat(totalCount).isEqualTo(5);

        // Statystyki firmy
        Double techCorpAvg = employeeRepository.findAverageSalaryByCompany("TechCorp");
        Long techCorpCount = employeeRepository.countEmployeesByCompany("TechCorp");

        assertThat(techCorpAvg).isNotNull();
        assertThat(techCorpCount).isEqualTo(4);
    }

    @Test
    void testExistsAndDeleteByEmail() {
        // Given
        String email = "jan.kowalski@techcorp.com";

        // When - sprawdź czy istnieje
        boolean existsBefore = employeeRepository.existsByEmail(email);
        assertThat(existsBefore).isTrue();

        // When - usuń
        employeeRepository.deleteByEmail(email);
        employeeRepository.flush();

        // Then - sprawdź czy nie istnieje
        boolean existsAfter = employeeRepository.existsByEmail(email);
        Optional<Employee> foundAfter = employeeRepository.findByEmail(email);

        assertThat(existsAfter).isFalse();
        assertThat(foundAfter).isEmpty();
    }

    @Test
    void testFindByStatusWithPagination() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Employee> activeEmployees = employeeRepository.findByStatus(EmploymentStatus.ACTIVE, pageable);
        Page<Employee> inactiveEmployees = employeeRepository.findByStatus(EmploymentStatus.TERMINATED, pageable);

        // Then
        assertThat(activeEmployees.getTotalElements()).isEqualTo(3); // Jan, Anna, Maria
        assertThat(inactiveEmployees.getTotalElements()).isEqualTo(1); // Piotr
    }
}