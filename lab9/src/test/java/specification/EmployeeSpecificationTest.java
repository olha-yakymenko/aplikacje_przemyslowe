package com.techcorp.employee.repository;

import com.techcorp.employee.model.*;
import com.techcorp.employee.specification.EmployeeSpecification; // Dodaj ten import!
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

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class EmployeeRepositorySpecificationTest {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    private Department itDepartment;
    private Department hrDepartment;
    private Employee janKowalski;
    private Employee annaNowak;
    private Employee piotrWisniewski;
    private Employee mariaZielinska;
    private Employee tomaszLewandowski;

    @BeforeEach
    void setUp() {
        // Clean up
        employeeRepository.deleteAll();
        departmentRepository.deleteAll();

        // Create departments
        itDepartment = createDepartment("IT", "Warsaw", 100000.0);
        hrDepartment = createDepartment("HR", "Krakow", 50000.0);

        // Create employees
        janKowalski = createEmployee("Jan Kowalski", "jan.kowalski@techcorp.com",
                "TechCorp", Position.MANAGER, EmploymentStatus.ACTIVE, 15000.0, itDepartment);

        annaNowak = createEmployee("Anna Nowak", "anna.nowak@techcorp.com",
                "TechCorp", Position.PROGRAMMER, EmploymentStatus.ACTIVE, 8000.0, itDepartment);

        piotrWisniewski = createEmployee("Piotr Wiśniewski", "piotr.wisniewski@othercorp.com",
                "OtherCorp", Position.MANAGER, EmploymentStatus.ON_LEAVE, 12000.0, hrDepartment);

        mariaZielinska = createEmployee("Maria Zielińska", "maria.zielinska@techcorp.com",
                "TechCorp", Position.PROGRAMMER, EmploymentStatus.TERMINATED, 6000.0, null);

        tomaszLewandowski = createEmployee("Tomasz Lewandowski", "tomasz.lewandowski@othercorp.com",
                "OtherCorp", Position.INTERN, EmploymentStatus.ACTIVE, 3000.0, hrDepartment);

        // Save all
        employeeRepository.saveAll(Arrays.asList(
                janKowalski, annaNowak, piotrWisniewski, mariaZielinska, tomaszLewandowski
        ));
    }

    private Department createDepartment(String name, String location, Double budget) {
        Department department = new Department();
        department.setName(name);
        department.setLocation(location);
        department.setBudget(budget);
        return departmentRepository.save(department);
    }

    private Employee createEmployee(String name, String email, String company,
                                    Position position, EmploymentStatus status,
                                    Double salary, Department department) {
        Employee employee = new Employee();
        employee.setName(name);
        employee.setEmail(email);
        employee.setCompany(company);
        employee.setPosition(position);
        employee.setStatus(status);
        employee.setSalary(salary);
        employee.setDepartment(department);
        return employee;
    }

    // ===== TESTY DLA EmployeeSpecification =====

    @Test
    void testEmployeeSpecification_hasName() {
        // When
        Specification<Employee> spec = EmployeeSpecification.hasName("Jan");
        List<Employee> result = employeeRepository.findAll(spec);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Jan Kowalski");
    }

    @Test
    void testEmployeeSpecification_hasNameWithNull() {
        // When
        Specification<Employee> spec = EmployeeSpecification.hasName(null);
        List<Employee> result = employeeRepository.findAll(spec);

        // Then - zwraca wszystkich (specification zwraca null = brak filtrowania)
        assertThat(result).hasSize(5);
    }

    @Test
    void testEmployeeSpecification_hasNameWithEmptyString() {
        // When
        Specification<Employee> spec = EmployeeSpecification.hasName("");
        List<Employee> result = employeeRepository.findAll(spec);

        // Then
        assertThat(result).hasSize(5);
    }

    @Test
    void testEmployeeSpecification_hasNameCaseInsensitive() {
        // When
        Specification<Employee> spec = EmployeeSpecification.hasName("kowalski");
        List<Employee> result = employeeRepository.findAll(spec);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Jan Kowalski");
    }

    @Test
    void testEmployeeSpecification_hasNamePartialMatch() {
        // When
        Specification<Employee> spec = EmployeeSpecification.hasName("an");
        List<Employee> result = employeeRepository.findAll(spec);

        assertThat(result).hasSize(3);
        assertThat(result)
                .extracting(Employee::getName)
                .containsExactlyInAnyOrder("Jan Kowalski", "Anna Nowak", "Tomasz Lewandowski");
    }

    @Test
    void testEmployeeSpecification_fromCompany() {
        // When
        Specification<Employee> spec = EmployeeSpecification.fromCompany("TechCorp");
        List<Employee> result = employeeRepository.findAll(spec);

        // Then
        assertThat(result).hasSize(3);
        assertThat(result)
                .extracting(Employee::getCompany)
                .containsOnly("TechCorp");
    }

    @Test
    void testEmployeeSpecification_fromCompanyCaseInsensitive() {
        // When
        Specification<Employee> spec = EmployeeSpecification.fromCompany("techcorp");
        List<Employee> result = employeeRepository.findAll(spec);

        // Then
        assertThat(result).hasSize(3);
    }

    @Test
    void testEmployeeSpecification_fromCompanyWithNull() {
        // When
        Specification<Employee> spec = EmployeeSpecification.fromCompany(null);
        List<Employee> result = employeeRepository.findAll(spec);

        // Then
        assertThat(result).hasSize(5);
    }

    @Test
    void testEmployeeSpecification_withPosition() {
        // When
        Specification<Employee> spec = EmployeeSpecification.withPosition(Position.MANAGER);
        List<Employee> result = employeeRepository.findAll(spec);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(Employee::getName)
                .containsExactlyInAnyOrder("Jan Kowalski", "Piotr Wiśniewski");
    }

    @Test
    void testEmployeeSpecification_withPositionNull() {
        // When
        Specification<Employee> spec = EmployeeSpecification.withPosition(null);
        List<Employee> result = employeeRepository.findAll(spec);

        // Then
        assertThat(result).hasSize(5);
    }

    @Test
    void testEmployeeSpecification_withStatus() {
        // When
        Specification<Employee> spec = EmployeeSpecification.withStatus(EmploymentStatus.ACTIVE);
        List<Employee> result = employeeRepository.findAll(spec);

        // Then
        assertThat(result).hasSize(3);
        assertThat(result)
                .extracting(Employee::getName)
                .containsExactlyInAnyOrder(
                        "Jan Kowalski",
                        "Anna Nowak",
                        "Tomasz Lewandowski"
                );
    }

    @Test
    void testEmployeeSpecification_salaryGreaterThanOrEqual() {
        // When
        Specification<Employee> spec = EmployeeSpecification.salaryGreaterThanOrEqual(10000.0);
        List<Employee> result = employeeRepository.findAll(spec);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(Employee::getName)
                .containsExactlyInAnyOrder("Jan Kowalski", "Piotr Wiśniewski");
    }

    @Test
    void testEmployeeSpecification_salaryGreaterThanOrEqualWithNull() {
        // When
        Specification<Employee> spec = EmployeeSpecification.salaryGreaterThanOrEqual(null);
        List<Employee> result = employeeRepository.findAll(spec);

        // Then
        assertThat(result).hasSize(5);
    }

    @Test
    void testEmployeeSpecification_salaryLessThanOrEqual() {
        // When
        Specification<Employee> spec = EmployeeSpecification.salaryLessThanOrEqual(7000.0);
        List<Employee> result = employeeRepository.findAll(spec);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(Employee::getName)
                .containsExactlyInAnyOrder("Maria Zielińska", "Tomasz Lewandowski");
    }

    @Test
    void testEmployeeSpecification_salaryBetween() {
        // When
        Specification<Employee> spec = EmployeeSpecification.salaryBetween(5000.0, 10000.0);
        List<Employee> result = employeeRepository.findAll(spec);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(Employee::getName)
                .containsExactlyInAnyOrder("Anna Nowak", "Maria Zielińska");
    }

    @Test
    void testEmployeeSpecification_salaryBetweenMinOnly() {
        // When
        Specification<Employee> spec = EmployeeSpecification.salaryBetween(10000.0, null);
        List<Employee> result = employeeRepository.findAll(spec);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(Employee::getName)
                .containsExactlyInAnyOrder("Jan Kowalski", "Piotr Wiśniewski");
    }

    @Test
    void testEmployeeSpecification_salaryBetweenMaxOnly() {
        // When
        Specification<Employee> spec = EmployeeSpecification.salaryBetween(null, 7000.0);
        List<Employee> result = employeeRepository.findAll(spec);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(Employee::getName)
                .containsExactlyInAnyOrder("Maria Zielińska", "Tomasz Lewandowski");
    }

    @Test
    void testEmployeeSpecification_salaryBetweenBothNull() {
        // When
        Specification<Employee> spec = EmployeeSpecification.salaryBetween(null, null);
        List<Employee> result = employeeRepository.findAll(spec);

        // Then
        assertThat(result).hasSize(5);
    }

    @Test
    void testEmployeeSpecification_inDepartment() {
        // When
        Specification<Employee> spec = EmployeeSpecification.inDepartment("IT");
        List<Employee> result = employeeRepository.findAll(spec);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(Employee::getName)
                .containsExactlyInAnyOrder("Jan Kowalski", "Anna Nowak");
    }

    @Test
    void testEmployeeSpecification_inDepartmentCaseInsensitive() {
        // When
        Specification<Employee> spec = EmployeeSpecification.inDepartment("it");
        List<Employee> result = employeeRepository.findAll(spec);

        // Then
        assertThat(result).hasSize(2);
    }

    @Test
    void testEmployeeSpecification_inDepartmentNoDepartment() {
        // When
        Specification<Employee> spec = EmployeeSpecification.inDepartment("brak departamentu");
        List<Employee> result = employeeRepository.findAll(spec);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Maria Zielińska");
    }

    @Test
    void testEmployeeSpecification_inDepartmentNoDepartmentCaseInsensitive() {
        // When
        Specification<Employee> spec = EmployeeSpecification.inDepartment("BRAK DEPARTAMENTU");
        List<Employee> result = employeeRepository.findAll(spec);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Maria Zielińska");
    }

    @Test
    void testEmployeeSpecification_inDepartmentWithNull() {
        // When
        Specification<Employee> spec = EmployeeSpecification.inDepartment(null);
        List<Employee> result = employeeRepository.findAll(spec);

        // Then
        assertThat(result).hasSize(5);
    }

    @Test
    void testEmployeeSpecification_inDepartmentWithEmptyString() {
        // When
        Specification<Employee> spec = EmployeeSpecification.inDepartment("");
        List<Employee> result = employeeRepository.findAll(spec);

        // Then
        assertThat(result).hasSize(5);
    }

    @Test
    void testEmployeeSpecification_hasEmail() {
        // When
        Specification<Employee> spec = EmployeeSpecification.hasEmail("kowalski");
        List<Employee> result = employeeRepository.findAll(spec);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Jan Kowalski");
    }

    @Test
    void testEmployeeSpecification_hasEmailCaseInsensitive() {
        // When
        Specification<Employee> spec = EmployeeSpecification.hasEmail("KOWALSKI");
        List<Employee> result = employeeRepository.findAll(spec);

        // Then
        assertThat(result).hasSize(1);
    }

    @Test
    void testEmployeeSpecification_hasDepartment() {
        // When
        Specification<Employee> spec = EmployeeSpecification.hasDepartment();
        List<Employee> result = employeeRepository.findAll(spec);

        // Then
        assertThat(result).hasSize(4);
        assertThat(result)
                .extracting(Employee::getName)
                .containsExactlyInAnyOrder(
                        "Jan Kowalski",
                        "Anna Nowak",
                        "Piotr Wiśniewski",
                        "Tomasz Lewandowski"
                );
    }

    @Test
    void testEmployeeSpecification_hasNoDepartment() {
        // When
        Specification<Employee> spec = EmployeeSpecification.hasNoDepartment();
        List<Employee> result = employeeRepository.findAll(spec);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Maria Zielińska");
    }

    // ===== TESTY ZŁOŻONYCH SPECIFICATIONS Z EmployeeSpecification =====

    @Test
    void testEmployeeSpecification_combinedSpecifications() {
        // Given: TechCorp + Manager + Active
        Specification<Employee> spec = Specification
                .where(EmployeeSpecification.fromCompany("TechCorp"))
                .and(EmployeeSpecification.withPosition(Position.MANAGER))
                .and(EmployeeSpecification.withStatus(EmploymentStatus.ACTIVE));

        // When
        List<Employee> result = employeeRepository.findAll(spec);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Jan Kowalski");
    }

    @Test
    void testEmployeeSpecification_combinedWithOptionalConditions() {
        // Given: Dynamicznie budowane specification
        String company = "TechCorp";
        Position position = Position.PROGRAMMER;
        Double minSalary = 5000.0;

        Specification<Employee> spec = Specification.where(null);

        spec = spec.and(EmployeeSpecification.fromCompany(company));
        spec = spec.and(EmployeeSpecification.withPosition(position));
        spec = spec.and(EmployeeSpecification.salaryGreaterThanOrEqual(minSalary));

        // When
        List<Employee> result = employeeRepository.findAll(spec);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(Employee::getName)
                .containsExactlyInAnyOrder("Anna Nowak", "Maria Zielińska");
    }

    @Test
    void testEmployeeSpecification_predefinedComplexSpecification_highEarnersInIT() {
        // When
        Specification<Employee> spec = EmployeeSpecification.highEarnersInIT();
        List<Employee> result = employeeRepository.findAll(spec);

        // Then - szuka w firmie "IT" (nie istnieje w testowych danych)
        assertThat(result).isEmpty();
    }

    @Test
    void testEmployeeSpecification_predefinedComplexSpecification_activeManagers() {
        // When
        Specification<Employee> spec = EmployeeSpecification.activeManagers();
        List<Employee> result = employeeRepository.findAll(spec);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Jan Kowalski");
    }

    @Test
    void testEmployeeSpecification_predefinedComplexSpecification_lowPaidWithoutDepartment() {
        // When
        Specification<Employee> spec = EmployeeSpecification.lowPaidWithoutDepartment();
        List<Employee> result = employeeRepository.findAll(spec);

        // Then - Maria ma 6000 > 3000, więc nie pasuje
        assertThat(result).isEmpty();
    }

    @Test
    void testEmployeeSpecification_combinedWithPagination() {
        // Given
        Specification<Employee> spec = EmployeeSpecification.fromCompany("TechCorp");
        Pageable pageable = PageRequest.of(0, 2, Sort.by("name"));

        // When
        Page<Employee> result = employeeRepository.findAll(spec, pageable);

        // Then
        assertThat(result.getTotalElements()).isEqualTo(3);
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalPages()).isEqualTo(2);
        assertThat(result.getContent().get(0).getName()).isEqualTo("Anna Nowak");
        assertThat(result.getContent().get(1).getName()).isEqualTo("Jan Kowalski");
    }

    // ===== TESTY DLA PRZYPADKÓW BRZEGOWYCH =====

    @Test
    void testEmployeeSpecification_emptySearchCriteria() {
        // When: Wszystkie kryteria są null/empty
        Specification<Employee> spec = Specification
                .where(EmployeeSpecification.hasName(null))
                .and(EmployeeSpecification.fromCompany(null))
                .and(EmployeeSpecification.withPosition(null))
                .and(EmployeeSpecification.withStatus(null))
                .and(EmployeeSpecification.salaryBetween(null, null))
                .and(EmployeeSpecification.inDepartment(null));

        List<Employee> result = employeeRepository.findAll(spec);

        // Then: Zwraca wszystkich pracowników
        assertThat(result).hasSize(5);
    }

    @Test
    void testEmployeeSpecification_multipleConditionsAllMatch() {
        // When: Wszystkie warunki pasują do jednego pracownika
        Specification<Employee> spec = Specification
                .where(EmployeeSpecification.hasName("Jan"))
                .and(EmployeeSpecification.fromCompany("TechCorp"))
                .and(EmployeeSpecification.withPosition(Position.MANAGER))
                .and(EmployeeSpecification.withStatus(EmploymentStatus.ACTIVE))
                .and(EmployeeSpecification.salaryBetween(10000.0, 20000.0))
                .and(EmployeeSpecification.inDepartment("IT"));

        List<Employee> result = employeeRepository.findAll(spec);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Jan Kowalski");
    }

    @Test
    void testEmployeeSpecification_noResults() {
        // When: Warunki które nie pasują do żadnego pracownika
        Specification<Employee> spec = Specification
                .where(EmployeeSpecification.fromCompany("NieistniejącaFirma"))
                .and(EmployeeSpecification.withPosition(Position.PROGRAMMER));

        List<Employee> result = employeeRepository.findAll(spec);

        // Then
        assertThat(result).isEmpty();
    }

    // Zachowaj istniejące testy (opcjonalnie możesz je skrócić, ale tu pozostawiam dla kompletności)

    @Test
    void testFindAllWithSpecification_nameContains() {
        // Given
        Specification<Employee> spec = (root, query, cb) ->
                cb.like(cb.lower(root.get("name")), "%jan%");

        // When
        List<Employee> result = employeeRepository.findAll(spec);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Jan Kowalski");
    }

    @Test
    void testFindAllWithSpecification_companyEquals() {
        // Given
        Specification<Employee> spec = (root, query, cb) ->
                cb.equal(root.get("company"), "TechCorp");

        // When
        List<Employee> result = employeeRepository.findAll(spec);

        // Then
        assertThat(result).hasSize(3);
        assertThat(result)
                .extracting(Employee::getName)
                .containsExactlyInAnyOrder(
                        "Jan Kowalski",
                        "Anna Nowak",
                        "Maria Zielińska"
                );
    }

    @Test
    void testFindAllWithSpecificationAndPagination() {
        // Given
        Specification<Employee> spec = (root, query, cb) ->
                cb.equal(root.get("company"), "TechCorp");

        Pageable pageable = PageRequest.of(0, 2, Sort.by("name"));

        // When
        Page<Employee> result = employeeRepository.findAll(spec, pageable);

        // Then
        assertThat(result.getTotalElements()).isEqualTo(3);
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalPages()).isEqualTo(2);
        assertThat(result.getContent().get(0).getName()).isEqualTo("Anna Nowak");
        assertThat(result.getContent().get(1).getName()).isEqualTo("Jan Kowalski");
    }

}





//package com.techcorp.employee.repository;
//
//import com.techcorp.employee.model.*;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.domain.Sort;
//import org.springframework.data.jpa.domain.Specification;
//import org.springframework.test.context.ActiveProfiles;
//
//import java.util.Arrays;
//import java.util.List;
//import java.util.Optional;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//@DataJpaTest
//@ActiveProfiles("test")
//class EmployeeRepositorySpecificationTest {
//
//    @Autowired
//    private EmployeeRepository employeeRepository;
//
//    @Autowired
//    private DepartmentRepository departmentRepository;
//
//    private Department itDepartment;
//    private Department hrDepartment;
//    private Employee janKowalski;
//    private Employee annaNowak;
//    private Employee piotrWisniewski;
//    private Employee mariaZielinska;
//    private Employee tomaszLewandowski;
//
//    @BeforeEach
//    void setUp() {
//        // Clean up
//        employeeRepository.deleteAll();
//        departmentRepository.deleteAll();
//
//        // Create departments
//        itDepartment = createDepartment("IT", "Warsaw", 100000.0);
//        hrDepartment = createDepartment("HR", "Krakow", 50000.0);
//
//        // Create employees
//        janKowalski = createEmployee("Jan Kowalski", "jan.kowalski@techcorp.com",
//                "TechCorp", Position.MANAGER, EmploymentStatus.ACTIVE, 15000.0, itDepartment);
//
//        annaNowak = createEmployee("Anna Nowak", "anna.nowak@techcorp.com",
//                "TechCorp", Position.PROGRAMMER, EmploymentStatus.ACTIVE, 8000.0, itDepartment);
//
//        piotrWisniewski = createEmployee("Piotr Wiśniewski", "piotr.wisniewski@othercorp.com",
//                "OtherCorp", Position.MANAGER, EmploymentStatus.ON_LEAVE, 12000.0, hrDepartment);
//
//        mariaZielinska = createEmployee("Maria Zielińska", "maria.zielinska@techcorp.com",
//                "TechCorp", Position.PROGRAMMER, EmploymentStatus.TERMINATED, 6000.0, null);
//
//        tomaszLewandowski = createEmployee("Tomasz Lewandowski", "tomasz.lewandowski@othercorp.com",
//                "OtherCorp", Position.INTERN, EmploymentStatus.ACTIVE, 3000.0, hrDepartment);
//
//        // Save all
//        employeeRepository.saveAll(Arrays.asList(
//                janKowalski, annaNowak, piotrWisniewski, mariaZielinska, tomaszLewandowski
//        ));
//    }
//
//    private Department createDepartment(String name, String location, Double budget) {
//        Department department = new Department();
//        department.setName(name);
//        department.setLocation(location);
//        department.setBudget(budget);
//        return departmentRepository.save(department);
//    }
//
//    private Employee createEmployee(String name, String email, String company,
//                                    Position position, EmploymentStatus status,
//                                    Double salary, Department department) {
//        Employee employee = new Employee();
//        employee.setName(name);
//        employee.setEmail(email);
//        employee.setCompany(company);
//        employee.setPosition(position);
//        employee.setStatus(status);
//        employee.setSalary(salary);
//        employee.setDepartment(department);
//        return employee;
//    }
//
//    // ===== TESTY DLA SPECIFICATION =====
//
//    @Test
//    void testFindAllWithSpecification_nameContains() {
//        // Given
//        Specification<Employee> spec = (root, query, cb) ->
//                cb.like(cb.lower(root.get("name")), "%jan%");
//
//        // When
//        List<Employee> result = employeeRepository.findAll(spec);
//
//        // Then
//        assertThat(result).hasSize(1);
//        assertThat(result.get(0).getName()).isEqualTo("Jan Kowalski");
//    }
//
//    @Test
//    void testFindAllWithSpecification_companyEquals() {
//        // Given
//        Specification<Employee> spec = (root, query, cb) ->
//                cb.equal(root.get("company"), "TechCorp");
//
//        // When
//        List<Employee> result = employeeRepository.findAll(spec);
//
//        // Then
//        assertThat(result).hasSize(3);
//        assertThat(result)
//                .extracting(Employee::getName)
//                .containsExactlyInAnyOrder(
//                        "Jan Kowalski",
//                        "Anna Nowak",
//                        "Maria Zielińska"
//                );
//    }
//
//    @Test
//    void testFindAllWithSpecification_position() {
//        // Given
//        Specification<Employee> spec = (root, query, cb) ->
//                cb.equal(root.get("position"), Position.MANAGER);
//
//        // When
//        List<Employee> result = employeeRepository.findAll(spec);
//
//        // Then
//        assertThat(result).hasSize(2);
//        assertThat(result)
//                .extracting(Employee::getName)
//                .containsExactlyInAnyOrder("Jan Kowalski", "Piotr Wiśniewski");
//    }
//
//    @Test
//    void testFindAllWithSpecification_status() {
//        // Given
//        Specification<Employee> spec = (root, query, cb) ->
//                cb.equal(root.get("status"), EmploymentStatus.ACTIVE);
//
//        // When
//        List<Employee> result = employeeRepository.findAll(spec);
//
//        // Then
//        assertThat(result).hasSize(3);
//        assertThat(result)
//                .extracting(Employee::getName)
//                .containsExactlyInAnyOrder(
//                        "Jan Kowalski",
//                        "Anna Nowak",
//                        "Tomasz Lewandowski"
//                );
//    }
//
//    @Test
//    void testFindAllWithSpecification_salaryGreaterThan() {
//        // Given
//        Specification<Employee> spec = (root, query, cb) ->
//                cb.greaterThan(root.get("salary"), 10000.0);
//
//        // When
//        List<Employee> result = employeeRepository.findAll(spec);
//
//        // Then
//        assertThat(result).hasSize(2);
//        assertThat(result)
//                .extracting(Employee::getName)
//                .containsExactlyInAnyOrder("Jan Kowalski", "Piotr Wiśniewski");
//    }
//
//    @Test
//    void testFindAllWithSpecification_salaryBetween() {
//        // Given
//        Specification<Employee> spec = (root, query, cb) ->
//                cb.between(root.get("salary"), 5000.0, 10000.0);
//
//        // When
//        List<Employee> result = employeeRepository.findAll(spec);
//
//        // Then
//        assertThat(result).hasSize(2);
//        assertThat(result)
//                .extracting(Employee::getName)
//                .containsExactlyInAnyOrder("Anna Nowak", "Maria Zielińska");
//    }
//
//    @Test
//    void testFindAllWithSpecification_hasDepartment() {
//        // Given
//        Specification<Employee> spec = (root, query, cb) ->
//                cb.isNotNull(root.get("department"));
//
//        // When
//        List<Employee> result = employeeRepository.findAll(spec);
//
//        // Then
//        assertThat(result).hasSize(4);
//        assertThat(result)
//                .extracting(Employee::getName)
//                .containsExactlyInAnyOrder(
//                        "Jan Kowalski",
//                        "Anna Nowak",
//                        "Piotr Wiśniewski",
//                        "Tomasz Lewandowski"
//                );
//    }
//
//    @Test
//    void testFindAllWithSpecification_noDepartment() {
//        // Given
//        Specification<Employee> spec = (root, query, cb) ->
//                cb.isNull(root.get("department"));
//
//        // When
//        List<Employee> result = employeeRepository.findAll(spec);
//
//        // Then
//        assertThat(result).hasSize(1);
//        assertThat(result.get(0).getName()).isEqualTo("Maria Zielińska");
//    }
//
//    @Test
//    void testFindAllWithSpecification_departmentName() {
//        // Given
//        Specification<Employee> spec = (root, query, cb) ->
//                cb.equal(root.get("department").get("name"), "IT");
//
//        // When
//        List<Employee> result = employeeRepository.findAll(spec);
//
//        // Then
//        assertThat(result).hasSize(2);
//        assertThat(result)
//                .extracting(Employee::getName)
//                .containsExactlyInAnyOrder("Jan Kowalski", "Anna Nowak");
//    }
//
//    // ===== TESTY DLA ZŁOŻONYCH SPECIFICATION =====
//
//    @Test
//    void testFindAllWithComplexSpecification_companyAndPosition() {
//        // Given
//        Specification<Employee> spec = (root, query, cb) ->
//                cb.and(
//                        cb.equal(root.get("company"), "TechCorp"),
//                        cb.equal(root.get("position"), Position.PROGRAMMER)
//                );
//
//        // When
//        List<Employee> result = employeeRepository.findAll(spec);
//
//        // Then
//        assertThat(result).hasSize(2);
//        assertThat(result)
//                .extracting(Employee::getName)
//                .containsExactlyInAnyOrder("Anna Nowak", "Maria Zielińska");
//    }
//
//    @Test
//    void testFindAllWithComplexSpecification_companyAndStatusAndSalary() {
//        // Given
//        Specification<Employee> spec = (root, query, cb) ->
//                cb.and(
//                        cb.equal(root.get("company"), "TechCorp"),
//                        cb.equal(root.get("status"), EmploymentStatus.ACTIVE),
//                        cb.greaterThanOrEqualTo(root.get("salary"), 7000.0)
//                );
//
//        // When
//        List<Employee> result = employeeRepository.findAll(spec);
//
//        // Then
//        assertThat(result).hasSize(2);
//        assertThat(result)
//                .extracting(Employee::getName)
//                .containsExactlyInAnyOrder("Jan Kowalski", "Anna Nowak");
//    }
//
//    @Test
//    void testFindAllWithSpecificationBuilder_dynamicConditions() {
//        // Given - dynamiczne budowanie Specification
//        Specification<Employee> spec = Specification.where(null);
//
//        spec = spec.and((root, query, cb) -> cb.equal(root.get("company"), "OtherCorp"));
//        spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), EmploymentStatus.ACTIVE));
//
//        // When
//        List<Employee> result = employeeRepository.findAll(spec);
//
//        // Then
//        assertThat(result).hasSize(1);
//        assertThat(result.get(0).getName()).isEqualTo("Tomasz Lewandowski");
//    }
//
//    @Test
//    void testFindAllWithSpecificationBuilder_optionalConditions() {
//        // Given - tylko te warunki które są potrzebne
//        String company = "TechCorp";
//        Position position = null; // Ten warunek nie będzie dodany
//        Double minSalary = 5000.0;
//
//        Specification<Employee> spec = Specification.where(null);
//
//        if (company != null) {
//            spec = spec.and((root, query, cb) -> cb.equal(root.get("company"), company));
//        }
//
//        if (position != null) {
//            spec = spec.and((root, query, cb) -> cb.equal(root.get("position"), position));
//        }
//
//        if (minSalary != null) {
//            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("salary"), minSalary));
//        }
//
//        // When
//        List<Employee> result = employeeRepository.findAll(spec);
//
//        // Then
//        assertThat(result).hasSize(3); // Wszyscy z TechCorp zarabiający >= 5000
//        assertThat(result)
//                .extracting(Employee::getName)
//                .containsExactlyInAnyOrder(
//                        "Jan Kowalski",
//                        "Anna Nowak",
//                        "Maria Zielińska"
//                );
//    }
//
//    // ===== TESTY Z PAGINACJĄ =====
//
//    @Test
//    void testFindAllWithSpecificationAndPagination() {
//        // Given
//        Specification<Employee> spec = (root, query, cb) ->
//                cb.equal(root.get("company"), "TechCorp");
//
//        Pageable pageable = PageRequest.of(0, 2, Sort.by("name"));
//
//        // When
//        Page<Employee> result = employeeRepository.findAll(spec, pageable);
//
//        // Then
//        assertThat(result.getTotalElements()).isEqualTo(3);
//        assertThat(result.getContent()).hasSize(2);
//        assertThat(result.getTotalPages()).isEqualTo(2);
//
//        // Sprawdź sortowanie
//        assertThat(result.getContent().get(0).getName()).isEqualTo("Anna Nowak");
//        assertThat(result.getContent().get(1).getName()).isEqualTo("Jan Kowalski");
//    }
//
//    @Test
//    void testFindAllWithSpecificationAndPagination_pageTwo() {
//        // Given
//        Specification<Employee> spec = (root, query, cb) ->
//                cb.equal(root.get("company"), "TechCorp");
//
//        Pageable pageable = PageRequest.of(1, 2, Sort.by("name"));
//
//        // When
//        Page<Employee> result = employeeRepository.findAll(spec, pageable);
//
//        // Then
//        assertThat(result.getTotalElements()).isEqualTo(3);
//        assertThat(result.getContent()).hasSize(1);
//        assertThat(result.getTotalPages()).isEqualTo(2);
//        assertThat(result.getContent().get(0).getName()).isEqualTo("Maria Zielińska");
//    }
//
//    // ===== TESTY DLA MAPOWANIA ENCJI =====
//
//    @Test
//    void testEmployeeDepartmentRelationship() {
//        // Given
//        Optional<Employee> employeeOpt = employeeRepository.findByEmail("jan.kowalski@techcorp.com");
//
//        // When
//        assertThat(employeeOpt).isPresent();
//        Employee employee = employeeOpt.get();
//
//        // Then
//        assertThat(employee.getDepartment()).isNotNull();
//        assertThat(employee.getDepartment().getName()).isEqualTo("IT");
//        assertThat(employee.getDepartment().getLocation()).isEqualTo("Warsaw");
//    }
//
//    @Test
//    void testDepartmentEmployeeRelationship() {
//        // Given
//        Optional<Department> departmentOpt = departmentRepository.findByName("IT");
//
//        // When
//        assertThat(departmentOpt).isPresent();
//        Department department = departmentOpt.get();
//
//        // Then - poprzez zapytanie sprawdzamy pracowników w departamencie
//        List<Employee> employeesInDepartment = employeeRepository.findByDepartmentId(department.getId());
//
//        assertThat(employeesInDepartment).hasSize(2);
//        assertThat(employeesInDepartment)
//                .extracting(Employee::getName)
//                .containsExactlyInAnyOrder("Jan Kowalski", "Anna Nowak");
//    }
//
//    @Test
//    void testEmployeeWithoutDepartment() {
//        // Given
//        Optional<Employee> employeeOpt = employeeRepository.findByEmail("maria.zielinska@techcorp.com");
//
//        // When
//        assertThat(employeeOpt).isPresent();
//        Employee employee = employeeOpt.get();
//
//        // Then
//        assertThat(employee.getDepartment()).isNull();
//    }
//
//    @Test
//    void testEmployeeCascadingNotSet() {
//        // Test że usunięcie pracownika nie usuwa departamentu
//        // Given
//        long departmentCountBefore = departmentRepository.count();
//        employeeRepository.delete(janKowalski);
//
//        // When
//        long departmentCountAfter = departmentRepository.count();
//
//        // Then
//        assertThat(departmentCountAfter).isEqualTo(departmentCountBefore);
//        assertThat(departmentRepository.findById(itDepartment.getId())).isPresent();
//    }
//
//    @Test
//    void testEmployeeUpdate() {
//        // Given
//        Optional<Employee> employeeOpt = employeeRepository.findByEmail("anna.nowak@techcorp.com");
//        assertThat(employeeOpt).isPresent();
//        Employee employee = employeeOpt.get();
//
//        // When
//        employee.setSalary(9000.0);
//        employee.setPosition(Position.MANAGER);
//        employeeRepository.save(employee);
//
//        // Then
//        Optional<Employee> updatedOpt = employeeRepository.findByEmail("anna.nowak@techcorp.com");
//        assertThat(updatedOpt).isPresent();
//        Employee updated = updatedOpt.get();
//
//        assertThat(updated.getSalary()).isEqualTo(9000.0);
//        assertThat(updated.getPosition()).isEqualTo(Position.MANAGER);
//    }
//
//    // ===== TESTY DLA CUSTOM QUERY METHODS =====
//
//    @Test
//    void testFindByCompany() {
//        // When
//        List<Employee> result = employeeRepository.findByCompany("TechCorp");
//
//        // Then
//        assertThat(result).hasSize(3);
//        assertThat(result)
//                .extracting(Employee::getCompany)
//                .containsOnly("TechCorp");
//    }
//
//    @Test
//    void testFindByDepartmentId() {
//        // When
//        List<Employee> result = employeeRepository.findByDepartmentId(itDepartment.getId());
//
//        // Then
//        assertThat(result).hasSize(2);
//        assertThat(result)
//                .extracting(Employee::getName)
//                .containsExactlyInAnyOrder("Jan Kowalski", "Anna Nowak");
//    }
//
//    @Test
//    void testFindByDepartmentIsNull() {
//        // When
//        List<Employee> result = employeeRepository.findByDepartmentIsNull();
//
//        // Then
//        assertThat(result).hasSize(1);
//        assertThat(result.get(0).getName()).isEqualTo("Maria Zielińska");
//    }
//
//    @Test
//    void testExistsByEmail() {
//        // When & Then
//        assertThat(employeeRepository.existsByEmail("jan.kowalski@techcorp.com")).isTrue();
//        assertThat(employeeRepository.existsByEmail("nieistniejacy@email.com")).isFalse();
//    }
//
//    @Test
//    void testDeleteByEmail() {
//        // Given
//        assertThat(employeeRepository.existsByEmail("jan.kowalski@techcorp.com")).isTrue();
//
//        // When
//        employeeRepository.deleteByEmail("jan.kowalski@techcorp.com");
//
//        // Then
//        assertThat(employeeRepository.existsByEmail("jan.kowalski@techcorp.com")).isFalse();
//        assertThat(employeeRepository.count()).isEqualTo(4);
//    }
//
//    // ===== TESTY DLA SPECIFICATION Z JOIN =====
//
//    @Test
//    void testSpecificationWithJoin_departmentLocation() {
//        // Given - pracownicy w departamentach w Warszawie
//        Specification<Employee> spec = (root, query, cb) -> {
//            var departmentJoin = root.join("department");
//            return cb.equal(departmentJoin.get("location"), "Warsaw");
//        };
//
//        // When
//        List<Employee> result = employeeRepository.findAll(spec);
//
//        // Then
//        assertThat(result).hasSize(2);
//        assertThat(result)
//                .extracting(Employee::getName)
//                .containsExactlyInAnyOrder("Jan Kowalski", "Anna Nowak");
//    }
//
//    @Test
//    void testSpecificationWithMultipleJoins() {
//        // Given - pracownicy z departamentu IT o statusie ACTIVE
//        Specification<Employee> spec = (root, query, cb) -> {
//            var departmentJoin = root.join("department");
//            return cb.and(
//                    cb.equal(departmentJoin.get("name"), "IT"),
//                    cb.equal(root.get("status"), EmploymentStatus.ACTIVE)
//            );
//        };
//
//        // When
//        List<Employee> result = employeeRepository.findAll(spec);
//
//        // Then
//        assertThat(result).hasSize(2);
//        assertThat(result)
//                .extracting(Employee::getName)
//                .containsExactlyInAnyOrder("Jan Kowalski", "Anna Nowak");
//    }
//
//    // ===== TESTY DLA NULL WARUNKÓW =====
//
//    @Test
//    void testSpecificationWithNullValue() {
//        // Given - Specification zwracająca null (żaden warunek)
//        Specification<Employee> spec = (root, query, cb) -> null;
//
//        // When
//        List<Employee> result = employeeRepository.findAll(spec);
//
//        // Then - powinno zwrócić wszystkich
//        assertThat(result).hasSize(5);
//    }
//
//    @Test
//    void testDynamicSpecificationWithOptionalParameters() {
//        // Symulacja metody z EmployeeService
//        String name = "Jan";
//        String company = "TechCorp";
//        Position position = Position.MANAGER;
//        EmploymentStatus status = null; // Ten parametr nie będzie używany
//        Double minSalary = null;
//        Double maxSalary = null;
//        String departmentName = null;
//
//        Specification<Employee> spec = Specification.where(null);
//
//        if (name != null && !name.trim().isEmpty()) {
//            spec = spec.and((root, query, cb) ->
//                    cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%"));
//        }
//
//        if (company != null && !company.trim().isEmpty()) {
//            spec = spec.and((root, query, cb) -> cb.equal(root.get("company"), company));
//        }
//
//        if (position != null) {
//            spec = spec.and((root, query, cb) -> cb.equal(root.get("position"), position));
//        }
//
//        if (status != null) {
//            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
//        }
//
//        if (minSalary != null) {
//            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("salary"), minSalary));
//        }
//
//        if (maxSalary != null) {
//            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("salary"), maxSalary));
//        }
//
//        if (departmentName != null && !departmentName.trim().isEmpty()) {
//            spec = spec.and((root, query, cb) -> {
//                if ("brak departamentu".equalsIgnoreCase(departmentName)) {
//                    return cb.isNull(root.get("department"));
//                } else {
//                    return cb.equal(cb.lower(root.get("department").get("name")),
//                            departmentName.toLowerCase());
//                }
//            });
//        }
//
//        // When
//        List<Employee> result = employeeRepository.findAll(spec);
//
//        // Then
//        assertThat(result).hasSize(1);
//        assertThat(result.get(0).getName()).isEqualTo("Jan Kowalski");
//    }
//}