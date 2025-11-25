package com.techcorp.employee.dao;

import com.techcorp.employee.model.Employee;
import com.techcorp.employee.model.Position;
import com.techcorp.employee.model.EmploymentStatus;
import com.techcorp.employee.model.CompanyStatistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@Sql(scripts = "/schema.sql")
class JdbcEmployeeDAOTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private JdbcEmployeeDAO employeeDAO;

    @BeforeEach
    void setUp() {
        employeeDAO = new JdbcEmployeeDAO(jdbcTemplate);
    }

    @Test
    void shouldSaveAndFindEmployeeByEmail() {
        // Given
        Employee employee = new Employee();
        employee.setName("Jan Kowalski");
        employee.setEmail("jan.kowalski@techcorp.com");
        employee.setCompany("TechCorp");
        employee.setPosition(Position.PROGRAMMER);
        employee.setSalary(5000.0);
        employee.setStatus(EmploymentStatus.ACTIVE);

        // When
        Employee saved = employeeDAO.save(employee);
        Optional<Employee> found = employeeDAO.findByEmail("jan.kowalski@techcorp.com");

        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("jan.kowalski@techcorp.com");
        assertThat(found.get().getName()).isEqualTo("Jan Kowalski");
        assertThat(found.get().getFirstName()).isEqualTo("Jan");
        assertThat(found.get().getLastName()).isEqualTo("Kowalski");
    }

    @Test
    void shouldUpdateExistingEmployee() {
        // Given
        Employee employee = new Employee();
        employee.setName("Jan Kowalski");
        employee.setEmail("jan@techcorp.com");
        employee.setCompany("TechCorp");
        employee.setPosition(Position.PROGRAMMER);
        employee.setSalary(5000.0);
        employee.setStatus(EmploymentStatus.ACTIVE);
        Employee saved = employeeDAO.save(employee);

        // When
        saved.setSalary(6000.0);
        saved.setPosition(Position.MANAGER);
        Employee updated = employeeDAO.save(saved);

        // Then
        Optional<Employee> found = employeeDAO.findByEmail("jan@techcorp.com");
        assertThat(found).isPresent();
        assertThat(found.get().getSalary()).isEqualTo(6000.0);
        assertThat(found.get().getPosition()).isEqualTo(Position.MANAGER);
    }

    @Test
    void shouldDeleteEmployeeByEmail() {
        // Given
        Employee employee = new Employee();
        employee.setName("Jan Kowalski");
        employee.setEmail("jan@techcorp.com");
        employee.setCompany("TechCorp");
        employee.setPosition(Position.PROGRAMMER);
        employee.setSalary(5000.0);
        employee.setStatus(EmploymentStatus.ACTIVE);
        employeeDAO.save(employee);

        // When
        employeeDAO.deleteByEmail("jan@techcorp.com");

        // Then
        Optional<Employee> found = employeeDAO.findByEmail("jan@techcorp.com");
        assertThat(found).isEmpty();
    }

    @Test
    void shouldFindEmployeesByCompany() {
        // Given
        createTestEmployees();

        // When
        List<Employee> techcorpEmployees = employeeDAO.findByCompany("TechCorp");

        // Then
        assertThat(techcorpEmployees).hasSize(2);
        assertThat(techcorpEmployees).extracting(Employee::getCompany)
                .containsOnly("TechCorp");
    }

    @Test
    void shouldFindEmployeesWithoutDepartment() {
        // Given
        Employee employeeWithDept = new Employee();
        employeeWithDept.setName("With Dept");
        employeeWithDept.setEmail("with.dept@techcorp.com");
        employeeWithDept.setCompany("TechCorp");
        employeeWithDept.setPosition(Position.PROGRAMMER);
        employeeWithDept.setSalary(5000.0);
        employeeWithDept.setStatus(EmploymentStatus.ACTIVE);
        employeeWithDept.setDepartmentId(1L);
        employeeDAO.save(employeeWithDept);

        Employee employeeWithoutDept = new Employee();
        employeeWithoutDept.setName("Without Dept");
        employeeWithoutDept.setEmail("without.dept@techcorp.com");
        employeeWithoutDept.setCompany("TechCorp");
        employeeWithoutDept.setPosition(Position.PROGRAMMER);
        employeeWithoutDept.setSalary(4000.0);
        employeeWithoutDept.setStatus(EmploymentStatus.ACTIVE);
        employeeDAO.save(employeeWithoutDept);

        // When
        List<Employee> withoutDepartment = employeeDAO.findEmployeesWithoutDepartment();

        // Then
        assertThat(withoutDepartment).hasSize(1);
        assertThat(withoutDepartment.get(0).getEmail()).isEqualTo("without.dept@techcorp.com");
    }

    @Test
    void shouldReturnCompanyStatistics() {
        // Given
        createTestEmployees();

        // When
        List<CompanyStatistics> statistics = employeeDAO.getCompanyStatistics();

        // Then
        assertThat(statistics).hasSize(2);

        // Sprawdź statystyki TechCorp
        CompanyStatistics techcorpStats = statistics.stream()
                .filter(stat -> "TechCorp".equals(stat.getCompanyName()))
                .findFirst()
                .orElseThrow();

        assertThat(techcorpStats.getEmployeeCount()).isEqualTo(2);
        assertThat(techcorpStats.getAverageSalary()).isEqualTo(6000.0);
        assertThat(techcorpStats.getMaxSalary()).isEqualTo(7000.0);
        assertThat(techcorpStats.getHighestPaidEmployee()).contains("Anna Nowak");

        // Sprawdź statystyki OtherCorp
        CompanyStatistics othercorpStats = statistics.stream()
                .filter(stat -> "OtherCorp".equals(stat.getCompanyName()))
                .findFirst()
                .orElseThrow();

        assertThat(othercorpStats.getEmployeeCount()).isEqualTo(1);
        assertThat(othercorpStats.getAverageSalary()).isEqualTo(4000.0);
        assertThat(othercorpStats.getMaxSalary()).isEqualTo(4000.0);
    }

    @Test
    void shouldReturnEmptyStatisticsForNoEmployees() {
        // When - baza jest pusta (czyszczona przed każdym testem)
        List<CompanyStatistics> statistics = employeeDAO.getCompanyStatistics();

        // Then
        assertThat(statistics).isEmpty();
    }

    @Test
    void shouldCheckIfEmployeeExistsByEmail() {
        // Given
        Employee employee = new Employee();
        employee.setName("Test Employee");
        employee.setEmail("test@techcorp.com");
        employee.setCompany("TechCorp");
        employee.setPosition(Position.PROGRAMMER);
        employee.setSalary(5000.0);
        employee.setStatus(EmploymentStatus.ACTIVE);
        employeeDAO.save(employee);

        // When & Then
        assertThat(employeeDAO.existsByEmail("test@techcorp.com")).isTrue();
        assertThat(employeeDAO.existsByEmail("nonexistent@techcorp.com")).isFalse();
    }

    @Test
    void shouldTransformNameToFirstAndLastName() {
        // Given
        Employee employee = new Employee();
        employee.setName("Jan Maria Kowalski");
        employee.setEmail("jan@techcorp.com");
        employee.setCompany("TechCorp");
        employee.setPosition(Position.PROGRAMMER);
        employee.setSalary(5000.0);
        employee.setStatus(EmploymentStatus.ACTIVE);

        // When
        Employee saved = employeeDAO.save(employee);

        // Then - sprawdź bezpośrednio w bazie danych
        String firstName = jdbcTemplate.queryForObject(
                "SELECT first_name FROM employees WHERE email = ?",
                String.class,
                "jan@techcorp.com"
        );
        String lastName = jdbcTemplate.queryForObject(
                "SELECT last_name FROM employees WHERE email = ?",
                String.class,
                "jan@techcorp.com"
        );

        assertThat(firstName).isEqualTo("Jan");
        assertThat(lastName).isEqualTo("Maria Kowalski");
    }

    @Test
    void shouldHandleSingleName() {
        // Given
        Employee employee = new Employee();
        employee.setName("Jan");
        employee.setEmail("jan@techcorp.com");
        employee.setCompany("TechCorp");
        employee.setPosition(Position.PROGRAMMER);
        employee.setSalary(5000.0);
        employee.setStatus(EmploymentStatus.ACTIVE);

        // When
        employeeDAO.save(employee);

        // Then
        String firstName = jdbcTemplate.queryForObject(
                "SELECT first_name FROM employees WHERE email = ?",
                String.class,
                "jan@techcorp.com"
        );
        String lastName = jdbcTemplate.queryForObject(
                "SELECT last_name FROM employees WHERE email = ?",
                String.class,
                "jan@techcorp.com"
        );

        assertThat(firstName).isEqualTo("Jan");
        assertThat(lastName).isEqualTo("");
    }

    private void createTestEmployees() {
        Employee emp1 = new Employee();
        emp1.setName("Anna Nowak");
        emp1.setEmail("anna@techcorp.com");
        emp1.setCompany("TechCorp");
        emp1.setPosition(Position.MANAGER);
        emp1.setSalary(7000.0);
        emp1.setStatus(EmploymentStatus.ACTIVE);
        employeeDAO.save(emp1);

        Employee emp2 = new Employee();
        emp2.setName("Jan Kowalski");
        emp2.setEmail("jan@techcorp.com");
        emp2.setCompany("TechCorp");
        emp2.setPosition(Position.PROGRAMMER);
        emp2.setSalary(5000.0);
        emp2.setStatus(EmploymentStatus.ACTIVE);
        employeeDAO.save(emp2);

        Employee emp3 = new Employee();
        emp3.setName("Piotr Wiśniewski");
        emp3.setEmail("piotr@othercorp.com");
        emp3.setCompany("OtherCorp");
        emp3.setPosition(Position.PROGRAMMER);
        emp3.setSalary(4000.0);
        emp3.setStatus(EmploymentStatus.ACTIVE);
        employeeDAO.save(emp3);
    }
}