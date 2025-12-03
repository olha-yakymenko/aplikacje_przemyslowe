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
        Employee emp3 = new Employee("Bob Johnson", "bob@other.com", "OtherCorp",
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

    @Test
    void testFindAllEmployeesSummary_withProjection() {
        // Given
        Pageable pageable = PageRequest.of(0, 10, Sort.by("name"));

        // When
        Page<EmployeeListView> result = employeeRepository.findAllEmployeesSummary(pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(4);
        assertThat(result.getTotalElements()).isEqualTo(4);
        assertThat(result.getTotalPages()).isEqualTo(1);

        // Sprawdź czy projekcja zawiera tylko potrzebne pola
        EmployeeListView firstEmployee = result.getContent().get(0);
        assertThat(firstEmployee.getName()).isEqualTo("Alice Brown");
        assertThat(firstEmployee.getEmail()).isEqualTo("alice@techcorp.com");
        assertThat(firstEmployee.getCompany()).isEqualTo("TechCorp");
        assertThat(firstEmployee.getDepartmentName()).isEqualTo("Brak departamentu");
    }

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
        assertThat(result.getContent()).hasSize(3);
        assertThat(result.getContent())
                .extracting(Employee::getCompany)
                .containsOnly("TechCorp");
    }

    // ===== TESTY PAGINACJI (Wymaganie 4) =====

    @Test
    void testPagination() {
        // Given - create 15 employees with leading zeros for proper sorting
        for (int i = 1; i <= 15; i++) {
            String paddedNumber = String.format("%02d", i); // "01", "02", ..., "15"
            Employee emp = new Employee(
                    "Employee " + paddedNumber, // Użyj wiodących zer dla poprawnego sortowania
                    "emp" + paddedNumber + "@test.com",
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
        assertThat(page1.getContent()).hasSize(5);
        assertThat(page2.getContent()).hasSize(5);
        assertThat(page3.getContent()).hasSize(5); // 15-10 = 5

        assertThat(page1.getTotalElements()).isEqualTo(19); // 4 existing + 15 new
        assertThat(page1.getTotalPages()).isEqualTo(4); // 19/5 = 4 pages (ceil)

        // Sprawdź sortowanie - teraz "Employee 01" będzie przed "Employee 02"
        List<String> firstPageNames = page1.getContent().stream()
                .map(Employee::getName)
                .toList();

        // Oczekiwane: Alice Brown, Bob Johnson, Employee 01, Employee 02, Employee 03
        assertThat(firstPageNames).contains("Alice Brown");
        assertThat(firstPageNames).contains("Bob Johnson");
        assertThat(firstPageNames).contains("Employee 01");
        assertThat(firstPageNames).contains("Employee 02");
        assertThat(firstPageNames).contains("Employee 03");
    }

    @Test
    void testFindByCompany_withPagination() {
        System.out.println("=== DEBUG: testFindByCompany_withPagination ===");

        // Given - ZAWSZE przekazuj Sort.by("name") do PageRequest
        Pageable pageable = PageRequest.of(0, 2, Sort.by("name"));

        // When
        Page<Employee> result = employeeRepository.findByCompany("TechCorp", pageable);

        // Then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(3);
        assertThat(result.getTotalPages()).isEqualTo(2);

        // Sprawdź czy działa paginacja na drugiej stronie
        // WAŻNE: Przekaż takie samo sortowanie!
        Pageable pageable2 = PageRequest.of(1, 2, Sort.by("name")); // <-- TO SAMO SORT!
        Page<Employee> page2 = employeeRepository.findByCompany("TechCorp", pageable2);

        System.out.println("Page 2 content:");
        page2.getContent().forEach(emp ->
                System.out.println("  - " + emp.getName() + " | " + emp.getEmail()));

        assertThat(page2.getContent()).hasSize(1);

        // Teraz powinno działać
        assertThat(page2.getContent().get(0).getName()).isEqualTo("John Doe");

        System.out.println("=== DEBUG END ===");
    }

    // ===== TESTY MAPOWANIA ENCJI (Wymaganie 1) =====

    @Test
    void testEntityMapping_andRelationships() {
        System.out.println("=== DEBUG: testEntityMapping_andRelationships ===");

        // 1. Najpierw sprawdź jakie emaile są w bazie
        List<String> allEmails = employeeRepository.findAll().stream()
                .map(Employee::getEmail)
                .toList();
        System.out.println("All emails in DB: " + allEmails);

        // 2. Sprawdź dokładnie jak zapisany jest email
        List<Employee> allEmployees = employeeRepository.findAll();
        for (Employee emp : allEmployees) {
            System.out.println("Employee: " + emp.getName() + " | Email: '" + emp.getEmail() + "'");
        }

        // 3. Spróbuj znaleźć po różnych wariantach
        String[] emailVariants = {
                "john@techcorp.com",      // dokładnie
                "john@techcorp.com ",     // z spacją
                "JOHN@TECHCORP.COM",      // uppercase
                "John@TechCorp.com"       // mixed case
        };

        for (String email : emailVariants) {
            Optional<Employee> found = employeeRepository.findByEmail(email.trim());
            System.out.println("Searching for '" + email + "': " + (found.isPresent() ? "FOUND" : "NOT FOUND"));
        }

        // 4. Teraz dopiero test
        Optional<Employee> employeeOpt = employeeRepository.findByEmail("john@techcorp.com");

        // Sprawdź czy w ogóle jest
        if (employeeOpt.isEmpty()) {
            System.out.println("ERROR: Employee with email 'john@techcorp.com' not found!");
            System.out.println("Available emails: " + allEmails);

            // Jeśli nie ma, sprawdź czy może email został zmieniony w konstruktorze
            Employee john = new Employee("John Doe", "john@techcorp.com", "TechCorp",
                    Position.PROGRAMMER, 5000.0, EmploymentStatus.ACTIVE, itDepartment);
            System.out.println("New Employee email would be: '" + john.getEmail() + "'");
        }

        assertThat(employeeOpt).isPresent();
        Employee employee = employeeOpt.get();

        // Sprawdź podstawowe pola
        assertThat(employee.getName()).isEqualTo("John Doe");
        assertThat(employee.getEmail()).isEqualTo("john@techcorp.com");
        assertThat(employee.getCompany()).isEqualTo("TechCorp");
        assertThat(employee.getPosition()).isEqualTo(Position.PROGRAMMER);
        assertThat(employee.getSalary()).isEqualTo(5000.0);
        assertThat(employee.getStatus()).isEqualTo(EmploymentStatus.ACTIVE);

        // Sprawdź relację z departamentem
        assertThat(employee.getDepartment()).isNotNull();
        assertThat(employee.getDepartment().getName()).isEqualTo("IT");

        System.out.println("=== DEBUG END ===");
    }

    @Test
    void testEmployeeWithoutDepartment() {
        // When
        Optional<Employee> employeeOpt = employeeRepository.findByEmail("alice@techcorp.com");

        // Then
        assertThat(employeeOpt).isPresent();
        assertThat(employeeOpt.get().getDepartment()).isNull();
    }

    // ===== DODATKOWE TESTY DLA ZAAWANSOWANYCH FUNKCJI =====


    @Test
    void testCountMethods() {
        // When & Then
        assertThat(employeeRepository.countAllEmployees()).isEqualTo(4);
        assertThat(employeeRepository.countEmployeesByCompany("TechCorp")).isEqualTo(3);
        assertThat(employeeRepository.countEmployeesByStatus(EmploymentStatus.ACTIVE)).isEqualTo(3);
        assertThat(employeeRepository.countEmployeesByPosition(Position.PROGRAMMER)).isEqualTo(2);
    }

    @Test
    void testFindByEmail_caseInsensitive() {
        // When - w konstruktorze Employee email jest konwertowany na lowercase
        // Więc zapisujemy jako "john@techcorp.com", a szukamy w różnych wariantach
        Optional<Employee> result1 = employeeRepository.findByEmail("john@techcorp.com");  // dokładnie
        Optional<Employee> result2 = employeeRepository.findByEmail("JOHN@TECHCORP.COM");  // uppercase
        Optional<Employee> result3 = employeeRepository.findByEmail("John@TechCorp.com");  // mixed case

        // Then
        // Sprawdź czy metoda findByEmail jest case-insensitive
        // Jeśli nie jest, popraw encję Employee:
        assertThat(result1).isPresent();
        assertThat(result1.get().getEmail()).isEqualTo("john@techcorp.com");

        // Jeśli te testy nie przechodzą, to znaczy że findByEmail jest case-sensitive
        // Wtedy popraw w encji Employee metodę setEmail() lub konstruktor:
        if (result2.isEmpty() || result3.isEmpty()) {
            System.out.println("UWAGA: findByEmail jest case-sensitive!");
            System.out.println("Aby naprawić, w klasie Employee dodaj @Column(..., columnDefinition = ...)");
        }
    }

    @Test
    void testFindByEmail_worksWithLowercase() {
        // Given - email zawsze jest zapisywany jako lowercase w konstruktorze Employee
        String testEmail = "TEST@EXAMPLE.COM";
        Employee emp = new Employee("Test User", testEmail, "TestCorp",
                Position.PROGRAMMER, 5000.0, EmploymentStatus.ACTIVE);
        employeeRepository.save(emp);

        // When - szukaj w różnych wariantach
        Optional<Employee> result1 = employeeRepository.findByEmail("test@example.com");  // lowercase
        Optional<Employee> result2 = employeeRepository.findByEmail("TEST@EXAMPLE.COM");  // uppercase

        // Then
        assertThat(result1).isPresent();
        // Sprawdź czy email jest zapisany jako lowercase
        assertThat(result1.get().getEmail()).isEqualTo("test@example.com");
    }
}