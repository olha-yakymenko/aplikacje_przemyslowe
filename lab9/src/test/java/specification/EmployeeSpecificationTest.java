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
    private Employee emp1, emp2, emp3, emp4;

    @BeforeEach
    void setUp() {
        employeeRepository.deleteAll();
        departmentRepository.deleteAll();

        // Create department
        itDepartment = new Department("IT", "Warsaw", "IT Department", "it@company.com", 100000.0);
        departmentRepository.save(itDepartment);

        emp1 = new Employee(
                "John Doe",
                "john@techcorp.com",
                "TechCorp",
                Position.PROGRAMMER,
                5000.0,
                EmploymentStatus.ACTIVE
        );
        emp1.setDepartment(itDepartment);

        emp2 = new Employee(
                "Jane Smith",
                "jane@techcorp.com",
                "TechCorp",
                Position.MANAGER,
                8000.0,
                EmploymentStatus.ACTIVE
        );
        // emp2 without department

        emp3 = new Employee(
                "Bob Johnson",
                "bob@other.com",
                "OtherCorp",
                Position.PROGRAMMER,
                4500.0,
                EmploymentStatus.TERMINATED
        );
        // emp3 without department

        emp4 = new Employee(
                "Alice Brown",
                "alice@techcorp.com",
                "TechCorp",
                Position.PRESIDENT,
                12000.0,
                EmploymentStatus.ON_LEAVE
        );

        employeeRepository.saveAll(List.of(emp1, emp2, emp3, emp4));
    }

    @Test
    void testWithDynamicQuery_WithAllFilters_ShouldReturnSingleEmployee() {
        // Arrange
        Specification<Employee> spec = EmployeeSpecification.withDynamicQuery(
                "John",           // name
                "TechCorp",       // company
                Position.PROGRAMMER, // position
                EmploymentStatus.ACTIVE, // status
                4000.0,           // minSalary
                6000.0,           // maxSalary
                "IT"              // departmentName
        );

        // Act
        List<Employee> result = employeeRepository.findAll(spec);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getEmail()).isEqualTo("john@techcorp.com");
    }

    @Test
    void testWithDynamicQuery_WithCompanyAndStatus_ShouldReturnMultipleEmployees() {
        // Arrange
        Specification<Employee> spec = EmployeeSpecification.withDynamicQuery(
                null,             // name
                "TechCorp",       // company
                null,             // position
                EmploymentStatus.ACTIVE, // status
                null,             // minSalary
                null,             // maxSalary
                null              // departmentName
        );

        // Act
        List<Employee> result = employeeRepository.findAll(spec);

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result).extracting(Employee::getCompany).containsOnly("TechCorp");
    }

    @Test
    void testWithDynamicQuery_WithNameContains_ShouldReturnMatchingEmployees() {
        // Arrange
        Specification<Employee> spec = EmployeeSpecification.withDynamicQuery(
                "oh",             // name contains "oh"
                null,             // company
                null,             // position
                null,             // status
                null,             // minSalary
                null,             // maxSalary
                null              // departmentName
        );

        // Act
        List<Employee> result = employeeRepository.findAll(spec);

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result).extracting(Employee::getName).contains("John Doe", "Bob Johnson");
    }

    @Test
    void testWithDynamicQuery_WithCaseInsensitiveName_ShouldReturnMatchingEmployees() {
        // Arrange
        Specification<Employee> spec = EmployeeSpecification.withDynamicQuery(
                "john",           // name in lowercase
                null,             // company
                null,             // position
                null,             // status
                null,             // minSalary
                null,             // maxSalary
                null              // departmentName
        );

        // Act
        List<Employee> result = employeeRepository.findAll(spec);

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("John Doe");
    }

    @Test
    void testWithDynamicQuery_WithSalaryRange_ShouldReturnEmployeesInRange() {
        // Arrange
        Specification<Employee> spec = EmployeeSpecification.withDynamicQuery(
                null,             // name
                null,             // company
                null,             // position
                null,             // status
                5500.0,           // minSalary
                8500.0,           // maxSalary
                null              // departmentName
        );

        // Act
        List<Employee> result = employeeRepository.findAll(spec);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Jane Smith");
    }

    @Test
    void testWithDynamicQuery_WithDepartmentName_ShouldReturnDepartmentEmployees() {
        // Arrange
        Specification<Employee> spec = EmployeeSpecification.withDynamicQuery(
                null,             // name
                null,             // company
                null,             // position
                null,             // status
                null,             // minSalary
                null,             // maxSalary
                "IT"              // departmentName
        );

        // Act
        List<Employee> result = employeeRepository.findAll(spec);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("John Doe");
        assertThat(result.get(0).getDepartment().getName()).isEqualTo("IT");
    }

    @Test
    void testWithDynamicQuery_WithNoDepartment_ShouldReturnEmployeesWithoutDepartment() {
        // Arrange
        Specification<Employee> spec = EmployeeSpecification.withDynamicQuery(
                null,             // name
                null,             // company
                null,             // position
                null,             // status
                null,             // minSalary
                null,             // maxSalary
                "Brak departamentu" // departmentName
        );

        // Act
        List<Employee> result = employeeRepository.findAll(spec);

        // Assert
        assertThat(result).hasSize(3);
        assertThat(result).extracting(Employee::getDepartment).containsOnlyNulls();
    }

    @Test
    void testWithDynamicQuery_WithEmptyParameters_ShouldReturnAllEmployees() {
        // Arrange
        Specification<Employee> spec = EmployeeSpecification.withDynamicQuery(
                null, null, null, null, null, null, null
        );

        // Act
        List<Employee> result = employeeRepository.findAll(spec);

        // Assert
        assertThat(result).hasSize(4);
    }

    @Test
    void testWithDynamicQuery_WithPositionFilter_ShouldReturnEmployeesWithPosition() {
        // Arrange
        Specification<Employee> spec = EmployeeSpecification.withDynamicQuery(
                null,             // name
                null,             // company
                Position.PROGRAMMER, // position
                null,             // status
                null,             // minSalary
                null,             // maxSalary
                null              // departmentName
        );

        // Act
        List<Employee> result = employeeRepository.findAll(spec);

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result).extracting(Employee::getPosition).containsOnly(Position.PROGRAMMER);
    }

    @Test
    void testWithDynamicQuery_WithStatusFilter_ShouldReturnEmployeesWithStatus() {
        // Arrange
        Specification<Employee> spec = EmployeeSpecification.withDynamicQuery(
                null,             // name
                null,             // company
                null,             // position
                EmploymentStatus.ACTIVE, // status
                null,             // minSalary
                null,             // maxSalary
                null              // departmentName
        );

        // Act
        List<Employee> result = employeeRepository.findAll(spec);

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result).extracting(Employee::getStatus).containsOnly(EmploymentStatus.ACTIVE);
    }

    @Test
    void testWithDynamicQuery_WithMinSalaryOnly_ShouldReturnEmployeesAboveMinSalary() {
        // Arrange
        Specification<Employee> spec = EmployeeSpecification.withDynamicQuery(
                null,             // name
                null,             // company
                null,             // position
                null,             // status
                10000.0,          // minSalary
                null,             // maxSalary
                null              // departmentName
        );

        // Act
        List<Employee> result = employeeRepository.findAll(spec);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Alice Brown");
    }

    @Test
    void testWithDynamicQuery_WithMaxSalaryOnly_ShouldReturnEmployeesBelowMaxSalary() {
        // Arrange
        Specification<Employee> spec = EmployeeSpecification.withDynamicQuery(
                null,             // name
                null,             // company
                null,             // position
                null,             // status
                null,             // minSalary
                6000.0,           // maxSalary
                null              // departmentName
        );

        // Act
        List<Employee> result = employeeRepository.findAll(spec);

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result).extracting(Employee::getName).contains("John Doe", "Bob Johnson");
    }

    @Test
    void testWithDynamicQuery_WithEmptyNameString_ShouldIgnoreNameFilter() {
        // Arrange
        Specification<Employee> spec = EmployeeSpecification.withDynamicQuery(
                "",               // empty name
                "TechCorp",       // company
                null,             // position
                null,             // status
                null,             // minSalary
                null,             // maxSalary
                null              // departmentName
        );

        // Act
        List<Employee> result = employeeRepository.findAll(spec);

        // Assert
        assertThat(result).hasSize(3);
        assertThat(result).extracting(Employee::getCompany).containsOnly("TechCorp");
    }

    @Test
    void testWithDynamicQuery_WithEmptyCompanyString_ShouldIgnoreCompanyFilter() {
        // Arrange
        Specification<Employee> spec = EmployeeSpecification.withDynamicQuery(
                "John",           // name
                "",               // empty company
                null,             // position
                null,             // status
                null,             // minSalary
                null,             // maxSalary
                null              // departmentName
        );

        // Act
        List<Employee> result = employeeRepository.findAll(spec);

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("John Doe");
    }

    @Test
    void testWithDynamicQuery_WithPagination_ShouldReturnPagedResults() {
        // Arrange
        Specification<Employee> spec = EmployeeSpecification.withDynamicQuery(
                null,             // name
                "TechCorp",       // company
                null,             // position
                null,             // status
                null,             // minSalary
                null,             // maxSalary
                null              // departmentName
        );

        Pageable pageable = PageRequest.of(0, 2);

        // Act
        Page<Employee> result = employeeRepository.findAll(spec, pageable);

        // Assert
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(3);
        assertThat(result.getTotalPages()).isEqualTo(2);
    }

    @Test
    void testWithDynamicQuery_WithCaseInsensitiveCompany_ShouldReturnMatchingEmployees() {
        // Arrange
        Specification<Employee> spec = EmployeeSpecification.withDynamicQuery(
                null,             // name
                "techcorp",       // company in lowercase
                null,             // position
                null,             // status
                null,             // minSalary
                null,             // maxSalary
                null              // departmentName
        );

        // Act
        List<Employee> result = employeeRepository.findAll(spec);

        // Assert
        assertThat(result).hasSize(3);
        assertThat(result).extracting(Employee::getCompany).containsOnly("TechCorp");
    }

    @Test
    void testWithDynamicQuery_WithCaseInsensitiveDepartment_ShouldReturnMatchingEmployees() {
        // Arrange
        Specification<Employee> spec = EmployeeSpecification.withDynamicQuery(
                null,             // name
                null,             // company
                null,             // position
                null,             // status
                null,             // minSalary
                null,             // maxSalary
                "it"              // departmentName in lowercase
        );

        // Act
        List<Employee> result = employeeRepository.findAll(spec);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDepartment().getName()).isEqualTo("IT");
    }

    @Test
    void testWithDynamicQuery_WithCombinedFilters_ShouldReturnCorrectEmployee() {
        // Arrange
        Specification<Employee> spec = EmployeeSpecification.withDynamicQuery(
                "Smith",          // name
                "TechCorp",       // company
                Position.MANAGER, // position
                EmploymentStatus.ACTIVE, // status
                7000.0,           // minSalary
                9000.0,           // maxSalary
                null              // departmentName
        );

        // Act
        List<Employee> result = employeeRepository.findAll(spec);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getEmail()).isEqualTo("jane@techcorp.com");
    }
}