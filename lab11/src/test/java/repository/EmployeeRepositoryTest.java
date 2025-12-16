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

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@DataJpaTest
class EmployeeRepositoryTest {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    private Department itDepartment;
    private Department hrDepartment;

    @BeforeEach
    void setUp() {
        // Clear database
        employeeRepository.deleteAll();
        departmentRepository.deleteAll();

        // Create departments
        itDepartment = new Department("IT", "Warsaw", "IT Department", "it@company.com", 100000.0);
        hrDepartment = new Department("HR", "Krakow", "HR Department", "hr@company.com", 50000.0);

        departmentRepository.saveAll(List.of(itDepartment, hrDepartment));

        // Create employees
        Employee emp1 = new Employee("John Doe", "john@techcorp.com", "TechCorp",
                Position.PROGRAMMER, 5000.0, EmploymentStatus.ACTIVE, itDepartment);
        Employee emp2 = new Employee("Jane Smith", "jane@techcorp.com", "TechCorp",
                Position.MANAGER, 8000.0, EmploymentStatus.ACTIVE, hrDepartment);
        Employee emp3 = new Employee("Bob Johnson", "bob@techcorp.com", "OtherCorp",
                Position.PROGRAMMER, 4500.0, EmploymentStatus.TERMINATED, null);
        Employee emp4 = new Employee("Alice Brown", "alice@techcorp.com", "TechCorp",
                Position.PRESIDENT, 6000.0, EmploymentStatus.ACTIVE, null);

        employeeRepository.saveAll(List.of(emp1, emp2, emp3, emp4));

        // 🔄 WAŻNE: Odśwież departamenty aby załadować employees
        departmentRepository.flush();
        employeeRepository.flush();

        // Odśwież z bazy
        itDepartment = departmentRepository.findById(itDepartment.getId()).get();
        hrDepartment = departmentRepository.findById(hrDepartment.getId()).get();
    }

    // ===== TESTY PROJEKCJI (Wymaganie 2) =====


    // ===== TESTY SPECIFICATIONS (Wymaganie 3) =====

    @Test
    void testFindAll_withSpecifications() {
        // Given
        Specification<Employee> spec = (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("company"), "TechCorp");

        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Employee> result = employeeRepository.findAll(spec, pageable);

        // Then
        assertThat(result.getContent()).satisfies(employees -> {
            assertThat(employees).hasSize(3);
            assertThat(employees).extracting(Employee::getCompany).containsOnly("TechCorp");
        });
    }

    // ===== TESTY PAGINACJI (Wymaganie 4) =====

    @Test
    void testPagination() {
        // Given - create 15 employees with leading zeros for proper sorting
        for (int i = 1; i <= 15; i++) {
            String paddedNumber = String.format("%02d", i); // "01", "02", ..., "15"
            Employee emp = new Employee(
                    "Employee " + paddedNumber, // Użyj wiodących zer dla poprawnego sortowania
                    "emp" + paddedNumber + "@techcorp.com",
                    "TestCorp",
                    Position.PROGRAMMER,
                    3000.0 + i * 100,
                    EmploymentStatus.ACTIVE
            );
            employeeRepository.save(emp);
        }

        Pageable pageable = PageRequest.of(0, 5, Sort.by("name"));

        // When
        Page<Employee> page1 = employeeRepository.findAll(pageable);
        Page<Employee> page2 = employeeRepository.findAll(PageRequest.of(1, 5));
        Page<Employee> page3 = employeeRepository.findAll(PageRequest.of(2, 5));

        // Then
        assertThat(page2.getContent()).hasSize(5);
        assertThat(page3.getContent()).hasSize(5);
    }

    @Test
    void testFindByCompany_withPagination() {
        // Given
        Pageable pageable = PageRequest.of(0, 2, Sort.by("name"));

        // When
        Page<Employee> result = employeeRepository.findByCompany("TechCorp", pageable);



        // Sprawdź paginację na drugiej stronie
        Pageable pageable2 = PageRequest.of(1, 2, Sort.by("name"));
        Page<Employee> page2 = employeeRepository.findByCompany("TechCorp", pageable2);

        assertThat(page2.getContent()).satisfies(content -> {
            assertThat(content).hasSize(1);
            assertThat(content.get(0).getName()).isEqualTo("John Doe");
        });
    }

    // ===== TESTY MAPOWANIA ENCJI (Wymaganie 1) =====

    @Test
    void testEntityMapping_andRelationships() {
        // When
        Optional<Employee> employeeOpt = employeeRepository.findByEmail("john@techcorp.com");

        // Then
        assertThat(employeeOpt).isPresent().get().satisfies(employee ->
                assertAll(
                        () -> assertThat(employee.getName()).isEqualTo("John Doe"),
                        () -> assertThat(employee.getEmail()).isEqualTo("john@techcorp.com"),
                        () -> assertThat(employee.getCompany()).isEqualTo("TechCorp"),
                        () -> assertThat(employee.getPosition()).isEqualTo(Position.PROGRAMMER),
                        () -> assertThat(employee.getSalary()).isEqualTo(5000.0),
                        () -> assertThat(employee.getStatus()).isEqualTo(EmploymentStatus.ACTIVE),
                        () -> assertThat(employee.getDepartment()).isNotNull(),
                        () -> assertThat(employee.getDepartment().getName()).isEqualTo("IT")
                )
        );
    }

    @Test
    void testEmployeeWithoutDepartment() {
        // When
        Optional<Employee> employeeOpt = employeeRepository.findByEmail("alice@techcorp.com");

        // Then
        assertThat(employeeOpt).isPresent().get().satisfies(employee ->
                assertThat(employee.getDepartment()).isNull()
        );
    }

    // ===== DODATKOWE TESTY DLA ZAAWANSOWANYCH FUNKCJI =====

    @Test
    void testCountMethods() {
        assertAll(
                () -> assertThat(employeeRepository.countAllEmployees()).isEqualTo(4),
                () -> assertThat(employeeRepository.countEmployeesByCompany("TechCorp")).isEqualTo(3),
                () -> assertThat(employeeRepository.countEmployeesByStatus(EmploymentStatus.ACTIVE)).isEqualTo(3),
                () -> assertThat(employeeRepository.countEmployeesByPosition(Position.PROGRAMMER)).isEqualTo(2)
        );
    }


}
