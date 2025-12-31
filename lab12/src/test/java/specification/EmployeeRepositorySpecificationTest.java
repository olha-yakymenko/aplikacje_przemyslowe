package com.techcorp.employee.repository;

import com.techcorp.employee.model.*;
import com.techcorp.employee.specification.EmployeeSpecification;
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

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

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
        employeeRepository.deleteAll();
        departmentRepository.deleteAll();

        itDepartment = createDepartment("IT", "Warsaw", 100000.0);
        hrDepartment = createDepartment("HR", "Krakow", 50000.0);

        janKowalski = createEmployee("Jan Kowalski", "jan.kowalski@techcorp.com",
                "TechCorp", Position.MANAGER, EmploymentStatus.ACTIVE, 15000.0, itDepartment);

        annaNowak = createEmployee("Anna Nowak", "anna.nowak@techcorp.com",
                "TechCorp", Position.PROGRAMMER, EmploymentStatus.ACTIVE, 8000.0, itDepartment);

        piotrWisniewski = createEmployee("Piotr Wiśniewski", "piotr.wisniewski@techcorp.com",
                "OtherCorp", Position.MANAGER, EmploymentStatus.ON_LEAVE, 12000.0, hrDepartment);

        mariaZielinska = createEmployee("Maria Zielińska", "maria.zielinska@techcorp.com",
                "TechCorp", Position.PROGRAMMER, EmploymentStatus.TERMINATED, 6000.0, null);

        tomaszLewandowski = createEmployee("Tomasz Lewandowski", "tomasz.lewandowski@techcorp.com",
                "OtherCorp", Position.INTERN, EmploymentStatus.ACTIVE, 3000.0, hrDepartment);

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
        employee.setSalary(BigDecimal.valueOf(salary));
        employee.setDepartment(department);
        return employee;
    }

    @Test
    void testEmployeeSpecification_hasName() {
        Specification<Employee> spec = EmployeeSpecification.hasName("Jan");
        List<Employee> result = employeeRepository.findAll(spec);
        assertThat(result).hasSize(1).extracting(Employee::getName).containsExactly("Jan Kowalski");
    }

    @Test
    void testEmployeeSpecification_hasNameWithNull() {
        Specification<Employee> spec = EmployeeSpecification.hasName(null);
        List<Employee> result = employeeRepository.findAll(spec);
        assertThat(result).hasSize(5);
    }

    @Test
    void testEmployeeSpecification_hasNameWithEmptyString() {
        Specification<Employee> spec = EmployeeSpecification.hasName("");
        List<Employee> result = employeeRepository.findAll(spec);
        assertThat(result).hasSize(5);
    }

    @Test
    void testEmployeeSpecification_hasNameCaseInsensitive() {
        Specification<Employee> spec = EmployeeSpecification.hasName("kowalski");
        List<Employee> result = employeeRepository.findAll(spec);
        assertThat(result).hasSize(1).extracting(Employee::getName).containsExactly("Jan Kowalski");
    }

    @Test
    void testEmployeeSpecification_hasNamePartialMatch() {
        Specification<Employee> spec = EmployeeSpecification.hasName("an");
        List<Employee> result = employeeRepository.findAll(spec);
        assertAll(
                () -> assertThat(result).hasSize(3),
                () -> assertThat(result).extracting(Employee::getName)
                        .containsExactlyInAnyOrder("Jan Kowalski", "Anna Nowak", "Tomasz Lewandowski")
        );
    }

    @Test
    void testEmployeeSpecification_fromCompany() {
        Specification<Employee> spec = EmployeeSpecification.fromCompany("TechCorp");
        List<Employee> result = employeeRepository.findAll(spec);
        assertAll(
                () -> assertThat(result).hasSize(3),
                () -> assertThat(result).extracting(Employee::getCompany).containsOnly("TechCorp")
        );
    }

    @Test
    void testEmployeeSpecification_fromCompanyCaseInsensitive() {
        Specification<Employee> spec = EmployeeSpecification.fromCompany("techcorp");
        List<Employee> result = employeeRepository.findAll(spec);
        assertThat(result).hasSize(3);
    }

    @Test
    void testEmployeeSpecification_fromCompanyWithNull() {
        Specification<Employee> spec = EmployeeSpecification.fromCompany(null);
        List<Employee> result = employeeRepository.findAll(spec);
        assertThat(result).hasSize(5);
    }

    @Test
    void testEmployeeSpecification_withPosition() {
        Specification<Employee> spec = EmployeeSpecification.withPosition(Position.MANAGER);
        List<Employee> result = employeeRepository.findAll(spec);
        assertAll(
                () -> assertThat(result).hasSize(2),
                () -> assertThat(result).extracting(Employee::getName)
                        .containsExactlyInAnyOrder("Jan Kowalski", "Piotr Wiśniewski")
        );
    }

    @Test
    void testEmployeeSpecification_withPositionNull() {
        Specification<Employee> spec = EmployeeSpecification.withPosition(null);
        List<Employee> result = employeeRepository.findAll(spec);
        assertThat(result).hasSize(5);
    }

    @Test
    void testEmployeeSpecification_withStatus() {
        Specification<Employee> spec = EmployeeSpecification.withStatus(EmploymentStatus.ACTIVE);
        List<Employee> result = employeeRepository.findAll(spec);
        assertAll(
                () -> assertThat(result).hasSize(3),
                () -> assertThat(result).extracting(Employee::getName)
                        .containsExactlyInAnyOrder("Jan Kowalski", "Anna Nowak", "Tomasz Lewandowski")
        );
    }

    @Test
    void testEmployeeSpecification_salaryGreaterThanOrEqual() {
        Specification<Employee> spec = EmployeeSpecification.salaryGreaterThanOrEqual(10000.0);
        List<Employee> result = employeeRepository.findAll(spec);
        assertAll(
                () -> assertThat(result).hasSize(2),
                () -> assertThat(result).extracting(Employee::getName)
                        .containsExactlyInAnyOrder("Jan Kowalski", "Piotr Wiśniewski")
        );
    }

    @Test
    void testEmployeeSpecification_salaryGreaterThanOrEqualWithNull() {
        Specification<Employee> spec = EmployeeSpecification.salaryGreaterThanOrEqual(null);
        List<Employee> result = employeeRepository.findAll(spec);
        assertThat(result).hasSize(5);
    }

    @Test
    void testEmployeeSpecification_salaryLessThanOrEqual() {
        Specification<Employee> spec = EmployeeSpecification.salaryLessThanOrEqual(7000.0);
        List<Employee> result = employeeRepository.findAll(spec);
        assertAll(
                () -> assertThat(result).hasSize(2),
                () -> assertThat(result).extracting(Employee::getName)
                        .containsExactlyInAnyOrder("Maria Zielińska", "Tomasz Lewandowski")
        );
    }

    @Test
    void testEmployeeSpecification_salaryBetween() {
        Specification<Employee> spec = EmployeeSpecification.salaryBetween(5000.0, 10000.0);
        List<Employee> result = employeeRepository.findAll(spec);
        assertAll(
                () -> assertThat(result).hasSize(2),
                () -> assertThat(result).extracting(Employee::getName)
                        .containsExactlyInAnyOrder("Anna Nowak", "Maria Zielińska")
        );
    }

    @Test
    void testEmployeeSpecification_salaryBetweenMinOnly() {
        Specification<Employee> spec = EmployeeSpecification.salaryBetween(10000.0, null);
        List<Employee> result = employeeRepository.findAll(spec);
        assertAll(
                () -> assertThat(result).hasSize(2),
                () -> assertThat(result).extracting(Employee::getName)
                        .containsExactlyInAnyOrder("Jan Kowalski", "Piotr Wiśniewski")
        );
    }

    @Test
    void testEmployeeSpecification_salaryBetweenMaxOnly() {
        Specification<Employee> spec = EmployeeSpecification.salaryBetween(null, 7000.0);
        List<Employee> result = employeeRepository.findAll(spec);
        assertAll(
                () -> assertThat(result).hasSize(2),
                () -> assertThat(result).extracting(Employee::getName)
                        .containsExactlyInAnyOrder("Maria Zielińska", "Tomasz Lewandowski")
        );
    }

    @Test
    void testEmployeeSpecification_salaryBetweenBothNull() {
        Specification<Employee> spec = EmployeeSpecification.salaryBetween(null, null);
        List<Employee> result = employeeRepository.findAll(spec);
        assertThat(result).hasSize(5);
    }

    @Test
    void testEmployeeSpecification_inDepartment() {
        Specification<Employee> spec = EmployeeSpecification.inDepartment("IT");
        List<Employee> result = employeeRepository.findAll(spec);
        assertAll(
                () -> assertThat(result).hasSize(2),
                () -> assertThat(result).extracting(Employee::getName)
                        .containsExactlyInAnyOrder("Jan Kowalski", "Anna Nowak")
        );
    }

    @Test
    void testEmployeeSpecification_inDepartmentCaseInsensitive() {
        Specification<Employee> spec = EmployeeSpecification.inDepartment("it");
        List<Employee> result = employeeRepository.findAll(spec);
        assertThat(result).hasSize(2);
    }

    @Test
    void testEmployeeSpecification_inDepartmentNoDepartment() {
        Specification<Employee> spec = EmployeeSpecification.inDepartment("brak departamentu");
        List<Employee> result = employeeRepository.findAll(spec);
        assertThat(result).hasSize(1).extracting(Employee::getName).containsExactly("Maria Zielińska");
    }

    @Test
    void testEmployeeSpecification_inDepartmentNoDepartmentCaseInsensitive() {
        Specification<Employee> spec = EmployeeSpecification.inDepartment("BRAK DEPARTAMENTU");
        List<Employee> result = employeeRepository.findAll(spec);
        assertThat(result).hasSize(1).extracting(Employee::getName).containsExactly("Maria Zielińska");
    }

    @Test
    void testEmployeeSpecification_inDepartmentWithNull() {
        Specification<Employee> spec = EmployeeSpecification.inDepartment(null);
        List<Employee> result = employeeRepository.findAll(spec);
        assertThat(result).hasSize(5);
    }

    @Test
    void testEmployeeSpecification_inDepartmentWithEmptyString() {
        Specification<Employee> spec = EmployeeSpecification.inDepartment("");
        List<Employee> result = employeeRepository.findAll(spec);
        assertThat(result).hasSize(5);
    }

    @Test
    void testEmployeeSpecification_hasEmail() {
        Specification<Employee> spec = EmployeeSpecification.hasEmail("kowalski");
        List<Employee> result = employeeRepository.findAll(spec);
        assertThat(result).hasSize(1).extracting(Employee::getName).containsExactly("Jan Kowalski");
    }

    @Test
    void testEmployeeSpecification_hasEmailCaseInsensitive() {
        Specification<Employee> spec = EmployeeSpecification.hasEmail("KOWALSKI");
        List<Employee> result = employeeRepository.findAll(spec);
        assertThat(result).hasSize(1);
    }

    @Test
    void testEmployeeSpecification_hasDepartment() {
        Specification<Employee> spec = EmployeeSpecification.hasDepartment();
        List<Employee> result = employeeRepository.findAll(spec);
        assertAll(
                () -> assertThat(result).hasSize(4),
                () -> assertThat(result).extracting(Employee::getName)
                        .containsExactlyInAnyOrder("Jan Kowalski", "Anna Nowak", "Piotr Wiśniewski", "Tomasz Lewandowski")
        );
    }

    @Test
    void testEmployeeSpecification_hasNoDepartment() {
        Specification<Employee> spec = EmployeeSpecification.hasNoDepartment();
        List<Employee> result = employeeRepository.findAll(spec);
        assertThat(result).hasSize(1).extracting(Employee::getName).containsExactly("Maria Zielińska");
    }

    @Test
    void testEmployeeSpecification_combinedSpecifications() {
        Specification<Employee> spec = Specification
                .where(EmployeeSpecification.fromCompany("TechCorp"))
                .and(EmployeeSpecification.withPosition(Position.MANAGER))
                .and(EmployeeSpecification.withStatus(EmploymentStatus.ACTIVE));
        List<Employee> result = employeeRepository.findAll(spec);
        assertThat(result).hasSize(1).extracting(Employee::getName).containsExactly("Jan Kowalski");
    }

    @Test
    void testEmployeeSpecification_combinedWithOptionalConditions() {
        String company = "TechCorp";
        Position position = Position.PROGRAMMER;
        Double minSalary = 5000.0;
        Specification<Employee> spec = Specification.where(null);
        spec = spec.and(EmployeeSpecification.fromCompany(company));
        spec = spec.and(EmployeeSpecification.withPosition(position));
        spec = spec.and(EmployeeSpecification.salaryGreaterThanOrEqual(minSalary));
        List<Employee> result = employeeRepository.findAll(spec);
        assertAll(
                () -> assertThat(result).hasSize(2),
                () -> assertThat(result).extracting(Employee::getName)
                        .containsExactlyInAnyOrder("Anna Nowak", "Maria Zielińska")
        );
    }

    @Test
    void testEmployeeSpecification_predefinedComplexSpecification_highEarnersInIT() {
        Specification<Employee> spec = EmployeeSpecification.highEarnersInIT();
        List<Employee> result = employeeRepository.findAll(spec);
        assertThat(result).isEmpty();
    }

    @Test
    void testEmployeeSpecification_predefinedComplexSpecification_activeManagers() {
        Specification<Employee> spec = EmployeeSpecification.activeManagers();
        List<Employee> result = employeeRepository.findAll(spec);
        assertThat(result).hasSize(1).extracting(Employee::getName).containsExactly("Jan Kowalski");
    }

    @Test
    void testEmployeeSpecification_predefinedComplexSpecification_lowPaidWithoutDepartment() {
        Specification<Employee> spec = EmployeeSpecification.lowPaidWithoutDepartment();
        List<Employee> result = employeeRepository.findAll(spec);
        assertThat(result).isEmpty();
    }

    @Test
    void testEmployeeSpecification_combinedWithPagination() {
        Specification<Employee> spec = EmployeeSpecification.fromCompany("TechCorp");
        Pageable pageable = PageRequest.of(0, 2, Sort.by("name"));
        Page<Employee> result = employeeRepository.findAll(spec, pageable);
        assertAll(
                () -> assertThat(result.getTotalElements()).isEqualTo(3),
                () -> assertThat(result.getContent()).hasSize(2),
                () -> assertThat(result.getTotalPages()).isEqualTo(2),
                () -> assertThat(result.getContent().get(0).getName()).isEqualTo("Anna Nowak"),
                () -> assertThat(result.getContent().get(1).getName()).isEqualTo("Jan Kowalski")
        );
    }

    @Test
    void testEmployeeSpecification_emptySearchCriteria() {
        Specification<Employee> spec = Specification
                .where(EmployeeSpecification.hasName(null))
                .and(EmployeeSpecification.fromCompany(null))
                .and(EmployeeSpecification.withPosition(null))
                .and(EmployeeSpecification.withStatus(null))
                .and(EmployeeSpecification.salaryBetween(null, null))
                .and(EmployeeSpecification.inDepartment(null));
        List<Employee> result = employeeRepository.findAll(spec);
        assertThat(result).hasSize(5);
    }

    @Test
    void testEmployeeSpecification_multipleConditionsAllMatch() {
        Specification<Employee> spec = Specification
                .where(EmployeeSpecification.hasName("Jan"))
                .and(EmployeeSpecification.fromCompany("TechCorp"))
                .and(EmployeeSpecification.withPosition(Position.MANAGER))
                .and(EmployeeSpecification.withStatus(EmploymentStatus.ACTIVE))
                .and(EmployeeSpecification.salaryBetween(10000.0, 20000.0))
                .and(EmployeeSpecification.inDepartment("IT"));
        List<Employee> result = employeeRepository.findAll(spec);
        assertThat(result).hasSize(1).extracting(Employee::getName).containsExactly("Jan Kowalski");
    }

    @Test
    void testEmployeeSpecification_noResults() {
        Specification<Employee> spec = Specification
                .where(EmployeeSpecification.fromCompany("NieistniejącaFirma"))
                .and(EmployeeSpecification.withPosition(Position.PROGRAMMER));
        List<Employee> result = employeeRepository.findAll(spec);
        assertThat(result).isEmpty();
    }

    @Test
    void testFindAllWithSpecification_nameContains() {
        Specification<Employee> spec = (root, query, cb) ->
                cb.like(cb.lower(root.get("name")), "%jan%");
        List<Employee> result = employeeRepository.findAll(spec);
        assertThat(result).hasSize(1).extracting(Employee::getName).containsExactly("Jan Kowalski");
    }

    @Test
    void testFindAllWithSpecification_companyEquals() {
        Specification<Employee> spec = (root, query, cb) ->
                cb.equal(root.get("company"), "TechCorp");
        List<Employee> result = employeeRepository.findAll(spec);
        assertAll(
                () -> assertThat(result).hasSize(3),
                () -> assertThat(result).extracting(Employee::getName)
                        .containsExactlyInAnyOrder("Jan Kowalski", "Anna Nowak", "Maria Zielińska")
        );
    }

    @Test
    void testFindAllWithSpecificationAndPagination() {
        Specification<Employee> spec = (root, query, cb) ->
                cb.equal(root.get("company"), "TechCorp");
        Pageable pageable = PageRequest.of(0, 2, Sort.by("name"));
        Page<Employee> result = employeeRepository.findAll(spec, pageable);
        assertAll(
                () -> assertThat(result.getTotalElements()).isEqualTo(3),
                () -> assertThat(result.getContent()).hasSize(2),
                () -> assertThat(result.getTotalPages()).isEqualTo(2),
                () -> assertThat(result.getContent().get(0).getName()).isEqualTo("Anna Nowak"),
                () -> assertThat(result.getContent().get(1).getName()).isEqualTo("Jan Kowalski")
        );
    }
}