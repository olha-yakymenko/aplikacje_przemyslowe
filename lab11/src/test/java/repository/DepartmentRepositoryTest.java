package com.techcorp.employee.repository;

import com.techcorp.employee.model.Department;
import com.techcorp.employee.model.Employee;
import com.techcorp.employee.model.Position;
import com.techcorp.employee.model.EmploymentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

@DataJpaTest
@ActiveProfiles("test")
class DepartmentRepositoryTest {

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    private Department itDepartment;
    private Department hrDepartment;
    private Department financeDepartment;

    @BeforeEach
    void setUp() {
        employeeRepository.deleteAll();
        departmentRepository.deleteAll();

        itDepartment = new Department("IT", "Warsaw", "IT Department", "it@company.com", 100000.0);
        hrDepartment = new Department("HR", "Krakow", "HR Department", "hr@company.com", 50000.0);
        financeDepartment = new Department("Finance", "Warsaw", "Finance Department", "finance@company.com", 75000.0);

        departmentRepository.saveAll(List.of(itDepartment, hrDepartment, financeDepartment));

        Employee emp1 = new Employee("John Doe", "john@techcorp.com", "TechCorp",
                Position.PROGRAMMER, new BigDecimal(5000), EmploymentStatus.ACTIVE, itDepartment);
        Employee emp2 = new Employee("Jane Smith", "jane@techcorp.com", "TechCorp",
                Position.MANAGER, new BigDecimal(8000), EmploymentStatus.ACTIVE, hrDepartment);
        Employee emp3 = new Employee("Bob Johnson", "bob@techcorp.com", "TechCorp",
                Position.PROGRAMMER, new BigDecimal(4500), EmploymentStatus.ACTIVE, itDepartment);
        Employee emp4 = new Employee("Alice Brown", "alice@techcorp.com", "TechCorp",
                Position.PRESIDENT, new BigDecimal(6000), EmploymentStatus.ACTIVE, financeDepartment);
        Employee emp5 = new Employee("Charlie Wilson", "charlie@techcorp.com", "TechCorp",
                Position.PROGRAMMER, new BigDecimal(4000), EmploymentStatus.ON_LEAVE, hrDepartment);

        employeeRepository.saveAll(List.of(emp1, emp2, emp3, emp4, emp5));

        departmentRepository.flush();
        employeeRepository.flush();
    }

    // ===== TESTOWANIE METOD Z JpaRepository =====

    @Test
    void testSave_SavesDepartmentSuccessfully() {
        // Testowanie: departmentRepository.save()
        Department newDepartment = new Department("Marketing", "Wroclaw", "Marketing Department",
                "marketing@company.com", 30000.0);

        Department saved = departmentRepository.save(newDepartment);
        departmentRepository.flush();

        assertAll("departmentRepository.save() powinien poprawnie zapisać departament",
                () -> assertThat(saved.getId()).isNotNull(),
                () -> assertThat(saved.getName()).isEqualTo("Marketing"),
                () -> assertThat(saved.getLocation()).isEqualTo("Wroclaw"),
                () -> assertThat(saved.getManagerEmail()).isEqualTo("marketing@company.com"),
                () -> assertThat(saved.getBudget()).isEqualTo(30000.0)
        );
    }

    @Test
    void testFindById_FindsExistingDepartment() {
        // Testowanie: departmentRepository.findById()
        Optional<Department> found = departmentRepository.findById(itDepartment.getId());

        assertAll("departmentRepository.findById() powinien znaleźć istniejący departament",
                () -> assertThat(found).isPresent(),
                () -> assertThat(found.get().getName()).isEqualTo("IT"),
                () -> assertThat(found.get().getLocation()).isEqualTo("Warsaw")
        );
    }

    @Test
    void testFindAll_ReturnsAllDepartments() {
        // Testowanie: departmentRepository.findAll()
        List<Department> allDepartments = departmentRepository.findAll();

        assertAll("departmentRepository.findAll() powinien zwrócić wszystkie departamenty",
                () -> assertThat(allDepartments).hasSize(3),
                () -> assertThat(allDepartments)
                        .extracting(Department::getName)
                        .containsExactlyInAnyOrder("IT", "HR", "Finance")
        );
    }

    @Test
    void testFindById_ReturnsEmptyForNonExistentId() {
        // Testowanie: departmentRepository.findById() - przypadek nieistniejącego ID
        Optional<Department> found = departmentRepository.findById(999L);

        assertAll("departmentRepository.findById() powinien zwrócić pusty Optional dla nieistniejącego ID",
                () -> assertThat(found).isEmpty()
        );
    }

    // ===== TESTOWANIE METOD WŁASNYCH REPOZYTORIUM =====

    @Test
    void testFindByName_FindsDepartmentByName() {
        // Testowanie: departmentRepository.findByName()
        Optional<Department> found = departmentRepository.findByName("HR");

        assertAll("departmentRepository.findByName() powinien znaleźć departament po nazwie",
                () -> assertThat(found).isPresent(),
                () -> assertThat(found.get().getLocation()).isEqualTo("Krakow"),
                () -> assertThat(found.get().getDescription()).isEqualTo("HR Department")
        );
    }

    @Test
    void testExistsByName_ChecksDepartmentExistence() {
        // Testowanie: departmentRepository.existsByName()
        assertAll("departmentRepository.existsByName() powinien poprawnie sprawdzać istnienie departamentów",
                () -> assertThat(departmentRepository.existsByName("IT")).isTrue(),
                () -> assertThat(departmentRepository.existsByName("HR")).isTrue(),
                () -> assertThat(departmentRepository.existsByName("NonExistent")).isFalse()
        );
    }

    @Test
    void testFindByName_ReturnsEmptyForNonExistentName() {
        // Testowanie: departmentRepository.findByName() - przypadek nieistniejącej nazwy
        Optional<Department> found = departmentRepository.findByName("NonExistent");

        assertAll("departmentRepository.findByName() powinien zwrócić pusty Optional dla nieistniejącej nazwy",
                () -> assertThat(found).isEmpty()
        );
    }

    // ===== TESTOWANIE METOD @Query =====

    @Test
    void testFindByLocation_FindsDepartmentsByLocation() {
        // Testowanie: departmentRepository.findByLocation()
        List<Department> warsawDepartments = departmentRepository.findByLocation("Warsaw");

        assertAll("departmentRepository.findByLocation() powinien znaleźć departamenty w danej lokalizacji",
                () -> assertThat(warsawDepartments).hasSize(2),
                () -> assertThat(warsawDepartments)
                        .extracting(Department::getName)
                        .containsExactlyInAnyOrder("IT", "Finance")
        );
    }

    @Test
    void testFindAllDepartmentNames_ReturnsSortedNames() {
        // Testowanie: departmentRepository.findAllDepartmentNames()
        List<String> departmentNames = departmentRepository.findAllDepartmentNames();

        assertAll("departmentRepository.findAllDepartmentNames() powinien zwrócić posortowane nazwy departamentów",
                () -> assertThat(departmentNames).hasSize(3),
                () -> assertThat(departmentNames).containsExactly("Finance", "HR", "IT") // Posortowane alfabetycznie
        );
    }

    @Test
    void testFindEmployeesByDepartmentId_FindsEmployeesForDepartment() {
        // Testowanie: departmentRepository.findEmployeesByDepartmentId()
        List<Employee> employees = departmentRepository.findEmployeesByDepartmentId(itDepartment.getId());

        assertAll("departmentRepository.findEmployeesByDepartmentId() powinien znaleźć pracowników departamentu",
                () -> assertThat(employees).hasSize(2),
                () -> assertThat(employees)
                        .extracting(Employee::getName)
                        .containsExactlyInAnyOrder("John Doe", "Bob Johnson"),
                () -> assertThat(employees.get(0).getDepartment()).isNotNull(),
                () -> assertThat(employees.get(0).getDepartment().getName()).isEqualTo("IT")
        );
    }

    @Test
    void testFindEmployeesByDepartmentId_ReturnsEmptyForNonExistentDepartment() {
        // Testowanie: departmentRepository.findEmployeesByDepartmentId() - przypadek nieistniejącego departamentu
        List<Employee> employees = departmentRepository.findEmployeesByDepartmentId(999L);

        assertAll("departmentRepository.findEmployeesByDepartmentId() powinien zwrócić pustą listę dla nieistniejącego departamentu",
                () -> assertThat(employees).isEmpty()
        );
    }

    @Test
    void testFindEmployeeWithDepartmentByEmail_FindsEmployeeWithDepartment() {
        // Testowanie: departmentRepository.findEmployeeWithDepartmentByEmail()
        Optional<Employee> employeeOpt = departmentRepository.findEmployeeWithDepartmentByEmail("john@techcorp.com");

        assertAll("departmentRepository.findEmployeeWithDepartmentByEmail() powinien znaleźć pracownika z departamentem",
                () -> assertThat(employeeOpt).isPresent(),
                () -> {
                    Employee employee = employeeOpt.get();
                    assertAll("Szczegóły pracownika",
                            () -> assertThat(employee.getName()).isEqualTo("John Doe"),
                            () -> assertThat(employee.getDepartment()).isNotNull(),
                            () -> assertThat(employee.getDepartment().getName()).isEqualTo("IT")
                    );
                }
        );
    }

    @Test
    void testFindAllEmployeesWithDepartment_ReturnsAllEmployeesWithDepartments() {
        // Testowanie: departmentRepository.findAllEmployeesWithDepartment()
        List<Employee> employees = departmentRepository.findAllEmployeesWithDepartment();

        assertAll("departmentRepository.findAllEmployeesWithDepartment() powinien zwrócić wszystkich pracowników z departamentami",
                () -> assertThat(employees).hasSize(5),
                () -> assertThat(employees)
                        .allMatch(emp -> emp.getDepartment() != null) // Wszyscy pracownicy mają przypisany departament
        );
    }

    @Test
    void testFindDepartmentNameById_FindsDepartmentName() {
        // Testowanie: departmentRepository.findDepartmentNameById()
        Optional<String> departmentName = departmentRepository.findDepartmentNameById(hrDepartment.getId());

        assertAll("departmentRepository.findDepartmentNameById() powinien znaleźć nazwę departamentu po ID",
                () -> assertThat(departmentName).isPresent(),
                () -> assertThat(departmentName.get()).isEqualTo("HR")
        );
    }

    @Test
    void testFindAllEmployeeEmailsWithDepartmentNames_ReturnsEmailDeptMapping() {
        // Testowanie: departmentRepository.findAllEmployeeEmailsWithDepartmentNames()
        List<Object[]> results = departmentRepository.findAllEmployeeEmailsWithDepartmentNames();

        Map<String, String> emailToDeptMap = results.stream()
                .collect(Collectors.toMap(
                        arr -> (String) arr[0],
                        arr -> arr[1] != null ? (String) arr[1] : "No Department"
                ));

        assertAll("departmentRepository.findAllEmployeeEmailsWithDepartmentNames() powinien zwrócić mapowanie email→departament",
                () -> assertThat(results).hasSize(5),
                () -> assertThat(emailToDeptMap.get("john@techcorp.com")).isEqualTo("IT"),
                () -> assertThat(emailToDeptMap.get("jane@techcorp.com")).isEqualTo("HR"),
                () -> assertThat(emailToDeptMap.get("bob@techcorp.com")).isEqualTo("IT"),
                () -> assertThat(emailToDeptMap.get("alice@techcorp.com")).isEqualTo("Finance"),
                () -> assertThat(emailToDeptMap.get("charlie@techcorp.com")).isEqualTo("HR")
        );
    }


    // ===== TESTOWANIE OGRANICZEŃ BAZY DANYCH =====

    @Test
    void testUniqueDepartmentNameConstraint_ThrowsExceptionOnDuplicateName() {
        // Testowanie ograniczenia UNIQUE na nazwę departamentu
        Department duplicateDepartment = new Department("IT", "Gdansk", "Duplicate IT",
                "duplicate@company.com", 50000.0);

        assertAll("Powinno rzucić wyjątek przy próbie zapisu departamentu z duplikującą się nazwą",
                () -> assertThatThrownBy(() -> {
                    departmentRepository.save(duplicateDepartment);
                    departmentRepository.flush();
                }).isInstanceOf(DataIntegrityViolationException.class)
        );
    }

    // ===== TESTOWANIE OPERACJI UPDATE =====

    @Test
    void testUpdateDepartment_UpdatesSuccessfully() {
        // Testowanie aktualizacji departamentu
        Department department = departmentRepository.findByName("IT").orElseThrow();
        department.setBudget(120000.0);
        department.setManagerEmail("new.it.manager@company.com");

        Department updated = departmentRepository.save(department);
        departmentRepository.flush();

        assertAll("departmentRepository.save() powinien poprawnie zaktualizować departament",
                () -> assertThat(updated.getBudget()).isEqualTo(120000.0),
                () -> assertThat(updated.getManagerEmail()).isEqualTo("new.it.manager@company.com")
        );
    }

    // ===== TESTOWANIE LOGIKI BIZNESOWEJ =====

    @Test
    void testDepartmentBudgetCalculations_BudgetExceedsTotalSalaries() {
        // Testowanie logiki budżetu vs sumy pensji
        Department it = departmentRepository.findByName("IT").orElseThrow();
        List<Employee> itEmployees = employeeRepository.findByDepartmentId(it.getId());

        BigDecimal totalSalary = itEmployees.stream()
                .map(Employee::getSalary)
                .reduce(BigDecimal.ZERO, BigDecimal::add);


        assertAll("Budżet departamentu powinien przekraczać sumę pensji pracowników",
                () -> assertThat(totalSalary)
                        .isEqualByComparingTo(BigDecimal.valueOf(9500)) // 5000 + 4500
        );

    }

    @Test
    void testBudgetFiltering_AllDepartmentsHavePositiveBudget() {
        // Testowanie walidacji budżetu
        List<Department> allDepartments = departmentRepository.findAll();
        Department it = departmentRepository.findByName("IT").orElseThrow();

        assertAll("Wszystkie departamenty powinny mieć dodatni budżet",
                () -> assertThat(allDepartments)
                        .allMatch(dept -> dept.getBudget() > 0),
                () -> assertThat(it.getBudget()).isEqualTo(100000.0)
        );
    }

    // ===== TESTOWANIE METODY findAllEmployeesWithDepartmentDetails =====

    @Test
    void testFindAllEmployeesWithDepartmentDetails_ReturnsAllEmployeeDetails() {
        // Testowanie: departmentRepository.findAllEmployeesWithDepartmentDetails()
        List<Object[]> results = departmentRepository.findAllEmployeesWithDepartmentDetails();

        assertAll("departmentRepository.findAllEmployeesWithDepartmentDetails() powinien zwrócić szczegółowe dane wszystkich pracowników",
                () -> assertThat(results).hasSize(5),
                () -> {
                    Object[] firstEmployee = results.get(0);
                    assertAll("Struktura danych pracownika",
                            () -> assertThat(firstEmployee).hasSize(7), // 7 kolumn w zapytaniu
                            () -> assertThat(firstEmployee[0]).isNotNull(), // ID
                            () -> assertThat(firstEmployee[1]).isNotNull(), // Imię
                            () -> assertThat(firstEmployee[2]).isNotNull(), // Email
                            () -> assertThat(firstEmployee[3]).isNotNull(), // Stanowisko
                            () -> assertThat(firstEmployee[4]).isNotNull() // Pensja
                    );
                }
        );
    }
}