package com.techcorp.employee.specification;

import com.techcorp.employee.model.*;
import com.techcorp.employee.repository.EmployeeRepository;
import com.techcorp.employee.repository.DepartmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class EmployeeSpecificationTest {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    private Department itDepartment;

    @BeforeEach
    void setUp() {
        employeeRepository.deleteAll();
        departmentRepository.deleteAll();

        itDepartment = new Department("IT", "Warsaw", "IT Department", "it@company.com", 100000.0);
        departmentRepository.save(itDepartment);

        // Create test employees
        Employee emp1 = new Employee("John Doe", "john@techcorp.com", "TechCorp",
                Position.PROGRAMMER, 5000.0, EmploymentStatus.ACTIVE, itDepartment);
        Employee emp2 = new Employee("Jane Smith", "jane@techcorp.com", "TechCorp",
                Position.MANAGER, 8000.0, EmploymentStatus.ACTIVE, null);
        Employee emp3 = new Employee("Bob Johnson", "bob@other.com", "OtherCorp",
                Position.PROGRAMMER, 4500.0, EmploymentStatus.TERMINATED, null);
        Employee emp4 = new Employee("Alice Brown", "alice@techcorp.com", "TechCorp",
                Position.PRESIDENT, 6000.0, EmploymentStatus.ACTIVE, null);

        employeeRepository.saveAll(List.of(emp1, emp2, emp3, emp4));
    }

    @Test
    void testWithDynamicQuery_allParameters() {
        // Given
        Specification<Employee> spec = EmployeeSpecification.withDynamicQuery(
                "John",           // name
                "TechCorp",       // company
                Position.PROGRAMMER, // position
                EmploymentStatus.ACTIVE, // status
                4000.0,           // minSalary
                6000.0,           // maxSalary
                "IT"              // departmentName
        );

        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Employee> result = employeeRepository.findAll(spec, pageable);

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("John Doe");
    }

    @Test
    void testWithDynamicQuery_partialParameters() {
        // Given - tylko firma i status
        Specification<Employee> spec = EmployeeSpecification.withDynamicQuery(
                null,             // name
                "TechCorp",       // company
                null,             // position
                EmploymentStatus.ACTIVE, // status
                null,             // minSalary
                null,             // maxSalary
                null              // departmentName
        );

        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Employee> result = employeeRepository.findAll(spec, pageable);

        // Then
        assertThat(result.getContent()).hasSize(3);
        assertThat(result.getContent())
                .extracting(Employee::getCompany)
                .containsOnly("TechCorp");
        assertThat(result.getContent())
                .extracting(Employee::getStatus)
                .containsOnly(EmploymentStatus.ACTIVE);
    }

    @Test
    void testWithDynamicQuery_nameContains() {
        // Given - szukaj po fragmencie imienia
        Specification<Employee> spec = EmployeeSpecification.withDynamicQuery(
                "ohn",            // name contains "ohn"
                null,             // company
                null,             // position
                null,             // status
                null,             // minSalary
                null,             // maxSalary
                null              // departmentName
        );

        // When
        List<Employee> result = employeeRepository.findAll(spec);

        // Then - powinien znaleźć Johna i Boba Johnsona
        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(Employee::getName)
                .contains("John Doe", "Bob Johnson");
    }

    @Test
    void testWithDynamicQuery_salaryRange() {
        // Given - pensja między 5500 a 8500
        Specification<Employee> spec = EmployeeSpecification.withDynamicQuery(
                null,             // name
                null,             // company
                null,             // position
                null,             // status
                5500.0,           // minSalary
                8500.0,           // maxSalary
                null              // departmentName
        );

        // When
        List<Employee> result = employeeRepository.findAll(spec);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(Employee::getName)
                .contains("Jane Smith", "Alice Brown");
    }

    @Test
    void testWithDynamicQuery_departmentName() {
        // Given - pracownicy z departamentu IT
        Specification<Employee> spec = EmployeeSpecification.withDynamicQuery(
                null,             // name
                null,             // company
                null,             // position
                null,             // status
                null,             // minSalary
                null,             // maxSalary
                "IT"              // departmentName
        );

        // When
        List<Employee> result = employeeRepository.findAll(spec);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("John Doe");
        assertThat(result.get(0).getDepartment().getName()).isEqualTo("IT");
    }

    @Test
    void testWithDynamicQuery_caseInsensitiveSearch() {
        // Given - wyszukiwanie bez względu na wielkość liter
        Specification<Employee> spec = EmployeeSpecification.withDynamicQuery(
                "john",           // name - małe litery
                "techcorp",       // company - małe litery
                null,             // position
                null,             // status
                null,             // minSalary
                null,             // maxSalary
                null              // departmentName
        );

        // When
        List<Employee> result = employeeRepository.findAll(spec);

        // Then - powinien znaleźć mimo różnicy w wielkości liter
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("John Doe");
    }

    @Test
    void testWithDynamicQuery_emptyParameters() {
        // Given - wszystkie parametry null (powinno zwrócić wszystkich)
        Specification<Employee> spec = EmployeeSpecification.withDynamicQuery(
                null, null, null, null, null, null, null
        );

        // When
        List<Employee> result = employeeRepository.findAll(spec);

        // Then
        assertThat(result).hasSize(4);
    }
}