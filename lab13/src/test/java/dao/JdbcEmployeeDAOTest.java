////package com.techcorp.employee.dao;
////
////import com.techcorp.employee.exception.DataAccessException;
////import com.techcorp.employee.exception.DuplicateEmailException;
////import com.techcorp.employee.model.Employee;
////import com.techcorp.employee.model.Position;
////import com.techcorp.employee.model.EmploymentStatus;
////import com.techcorp.employee.model.CompanyStatistics;
////import org.junit.jupiter.api.BeforeEach;
////import org.junit.jupiter.api.Test;
////import org.springframework.beans.factory.annotation.Autowired;
////import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
////import org.springframework.jdbc.core.JdbcTemplate;
////import org.springframework.test.context.jdbc.Sql;
////
////import java.util.List;
////import java.util.Optional;
////
////import static org.assertj.core.api.Assertions.assertThat;
////import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
////
////@JdbcTest
////@Sql(scripts = "/schema.sql")
////class JdbcEmployeeDAOTest {
//
//package com.techcorp.employee.dao;
//
//import com.techcorp.employee.exception.DataAccessException;
//import com.techcorp.employee.exception.DuplicateEmailException;
//import com.techcorp.employee.model.Employee;
//import com.techcorp.employee.model.Position;
//import com.techcorp.employee.model.EmploymentStatus;
//import com.techcorp.employee.model.CompanyStatistics;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
//import org.springframework.jdbc.core.JdbcTemplate;
//import org.springframework.test.context.ActiveProfiles;
//import org.springframework.test.context.jdbc.Sql;
//
//import javax.sql.DataSource;
//import java.util.List;
//import java.util.Optional;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
//
////@JdbcTest
////@ActiveProfiles("test")
////@Sql(scripts = "/schema.sql")
////class JdbcEmployeeDAOTest {
////
////    @Autowired
////    private JdbcTemplate jdbcTemplate;
////
////    private JdbcEmployeeDAO employeeDAO;
////
////    @BeforeEach
////    void setUp() {
////        employeeDAO = new JdbcEmployeeDAO(jdbcTemplate);
////    }
//
//
//@JdbcTest
//@ActiveProfiles("test")
//@Sql(scripts = "/schema.sql")
//class JdbcEmployeeDAOTest {
//
//    @Autowired
//    private JdbcTemplate jdbcTemplate;
//
//    private JdbcEmployeeDAO employeeDAO;
//
//    @BeforeEach
//    void setUp() {
//        employeeDAO = new JdbcEmployeeDAO(jdbcTemplate);
//    }
//
//
//    @Test
//    void shouldSaveEmployeeToDatabase() {
//        Employee employee = createTestEmployee("jan.kowalski@techcorp.com", "Jan Kowalski");
//
//        employeeDAO.save(employee);
//        Optional<Employee> foundEmployee = employeeDAO.findByEmail("jan.kowalski@techcorp.com");
//
//        assertThat(foundEmployee).isPresent();
//    }
//
//    @Test
//    void shouldReturnCorrectDataAfterSave() {
//        Employee employee = createTestEmployee("jan.kowalski@techcorp.com", "Jan Kowalski");
//        employee.setDepartmentId(1L);
//        employee.setPhotoFileName("photo.jpg");
//        employeeDAO.save(employee);
//
//        Employee result = employeeDAO.findByEmail("jan.kowalski@techcorp.com").get();
//
//        assertThat(result.getEmail()).isEqualTo(employee.getEmail());
//        assertThat(result.getName()).isEqualTo(employee.getName());
//        assertThat(result.getDepartmentId()).isEqualTo(employee.getDepartmentId());
//        assertThat(result.getPhotoFileName()).isEqualTo(employee.getPhotoFileName());
//    }
//
//    @Test
//    void shouldReturnEmptyOptionalForNonExistingEmployee() {
//        Optional<Employee> result = employeeDAO.findByEmail("nonexistent@techcorp.com");
//
//        assertThat(result).isEmpty();
//    }
//
//    @Test
//    void shouldUpdateEmployeeSalary() {
//        Employee employee = createTestEmployee("jan@techcorp.com", "Jan Kowalski");
//        employeeDAO.save(employee);
//
//        // Pobierz employee z ID
//        Employee savedEmployee = employeeDAO.findByEmail("jan@techcorp.com").get();
//        savedEmployee.setSalary(6000.0);
//        employeeDAO.save(savedEmployee);
//
//        Employee updatedEmployee = employeeDAO.findByEmail("jan@techcorp.com").get();
//        assertThat(updatedEmployee.getSalary()).isEqualTo(6000.0);
//    }
//
//    @Test
//    void shouldDeleteEmployeeFromDatabase() {
//        Employee employee = createTestEmployee("jan@techcorp.com", "Jan Kowalski");
//        employeeDAO.save(employee);
//
//        employeeDAO.deleteByEmail("jan@techcorp.com");
//
//        Optional<Employee> result = employeeDAO.findByEmail("jan@techcorp.com");
//        assertThat(result).isEmpty();
//    }
//
//    @Test
//    void shouldTransformNameToFirstAndLastName() {
//        Employee employee = createTestEmployee("jan@techcorp.com", "Jan Maria Kowalski");
//        employeeDAO.save(employee);
//
//        String firstName = jdbcTemplate.queryForObject(
//                "SELECT first_name FROM employees WHERE email = ?", String.class, "jan@techcorp.com");
//        String lastName = jdbcTemplate.queryForObject(
//                "SELECT last_name FROM employees WHERE email = ?", String.class, "jan@techcorp.com");
//
//        assertThat(firstName).isEqualTo("Jan");
//        assertThat(lastName).isEqualTo("Maria Kowalski");
//    }
//
//    @Test
//    void shouldHandleSingleName() {
//        Employee employee = createTestEmployee("jan@techcorp.com", "Jan");
//        employeeDAO.save(employee);
//
//        String firstName = jdbcTemplate.queryForObject(
//                "SELECT first_name FROM employees WHERE email = ?", String.class, "jan@techcorp.com");
//        String lastName = jdbcTemplate.queryForObject(
//                "SELECT last_name FROM employees WHERE email = ?", String.class, "jan@techcorp.com");
//
//        assertThat(firstName).isEqualTo("Jan");
//        assertThat(lastName).isEqualTo("");
//    }
//
//    @Test
//    void shouldThrowDuplicateEmailExceptionWhenSavingDuplicateEmail() {
//        Employee employee1 = createTestEmployee("jan@techcorp.com", "Jan Kowalski");
//        employeeDAO.save(employee1);
//
//        Employee employee2 = createTestEmployee("jan@techcorp.com", "Anna Nowak");
//
//        assertThatThrownBy(() -> employeeDAO.save(employee2))
//                .isInstanceOf(DuplicateEmailException.class)
//                .hasMessageContaining("Email already exists: jan@techcorp.com");
//    }
//
//    @Test
//    void shouldHandleNullValuesInSave() {
//        Employee employee = createTestEmployee("test@techcorp.com", "Test Employee");
//        employee.setDepartmentId(null);
//        employee.setPhotoFileName(null);
//        employeeDAO.save(employee);
//
//        Employee saved = employeeDAO.findByEmail("test@techcorp.com").get();
//
//        assertThat(saved.getDepartmentId()).isNull();
//        assertThat(saved.getPhotoFileName()).isNull();
//    }
//
//    @Test
//    void shouldReturnEmptyOptionalWhenEmployeeNotFoundById() {
//        Optional<Employee> found = employeeDAO.findById(999L);
//
//        assertThat(found).isEmpty();
//    }
//
//    @Test
//    void shouldHandleEmptyDatabaseInFindAll() {
//        List<Employee> employees = employeeDAO.findAll();
//
//        assertThat(employees).isEmpty();
//    }
//
//    @Test
//    void shouldHandleEmptyDatabaseInFindByCompany() {
//        List<Employee> employees = employeeDAO.findByCompany("NonExistentCompany");
//
//        assertThat(employees).isEmpty();
//    }
//
//    @Test
//    void shouldReturnFalseWhenCheckingNonExistentEmail() {
//        boolean exists = employeeDAO.existsByEmail("nonexistent@techcorp.com");
//
//        assertThat(exists).isFalse();
//    }
//
//    @Test
//    void shouldHandlePositionEnumConversionError() {
//        jdbcTemplate.update("INSERT INTO employees (first_name, last_name, email, salary, position, company, status) VALUES (?, ?, ?, ?, ?, ?, ?)",
//                "John", "Doe", "john@techcorp.com", 5000.0, "INVALID_POSITION", "TechCorp", "ACTIVE");
//
//        List<Employee> employees = employeeDAO.findAll();
//
//        assertThat(employees.get(0).getPosition()).isEqualTo(Position.PROGRAMMER);
//    }
//
//    @Test
//    void shouldHandleStatusEnumConversionError() {
//        jdbcTemplate.update("INSERT INTO employees (first_name, last_name, email, salary, position, company, status) VALUES (?, ?, ?, ?, ?, ?, ?)",
//                "John", "Doe", "john@techcorp.com", 5000.0, "PROGRAMMER", "TechCorp", "INVALID_STATUS");
//
//        List<Employee> employees = employeeDAO.findAll();
//
//        assertThat(employees.get(0).getStatus()).isEqualTo(EmploymentStatus.ACTIVE);
//    }
//
//    @Test
//    void shouldUpdateEmployeeWithNullDepartmentAndPhoto() {
//        Employee employee = createTestEmployee("jan@techcorp.com", "Jan Kowalski");
//        employee.setDepartmentId(1L);
//        employee.setPhotoFileName("photo.jpg");
//        employeeDAO.save(employee);
//
//        // Pobierz z ID i zaktualizuj
//        Employee saved = employeeDAO.findByEmail("jan@techcorp.com").get();
//        saved.setDepartmentId(null);
//        saved.setPhotoFileName(null);
//        employeeDAO.save(saved);
//
//        Employee updated = employeeDAO.findByEmail("jan@techcorp.com").get();
//        assertThat(updated.getDepartmentId()).isNull();
//        assertThat(updated.getPhotoFileName()).isNull();
//    }
//
//    @Test
//    void shouldDeleteNonExistentEmployeeWithoutError() {
//        employeeDAO.deleteByEmail("nonexistent@techcorp.com");
//
//        Optional<Employee> result = employeeDAO.findByEmail("nonexistent@techcorp.com");
//        assertThat(result).isEmpty();
//    }
//
//    @Test
//    void shouldHandleCompanyStatisticsWithMultipleHighestPaidEmployees() {
//        jdbcTemplate.update("INSERT INTO employees (first_name, last_name, email, salary, position, company, status) VALUES (?, ?, ?, ?, ?, ?, ?)",
//                "Anna", "Nowak", "anna1@techcorp.com", 7000.0, "MANAGER", "TechCorp", "ACTIVE");
//        jdbcTemplate.update("INSERT INTO employees (first_name, last_name, email, salary, position, company, status) VALUES (?, ?, ?, ?, ?, ?, ?)",
//                "Jan", "Kowalski", "anna2@techcorp.com", 7000.0, "MANAGER", "TechCorp", "ACTIVE");
//
//        List<CompanyStatistics> statistics = employeeDAO.getCompanyStatistics();
//
//        CompanyStatistics stats = statistics.get(0);
//        assertThat(stats.getEmployeeCount()).isEqualTo(2);
//        assertThat(stats.getMaxSalary()).isEqualTo(7000.0);
//    }
//
//    @Test
//    void shouldHandleZeroSalaryInStatistics() {
//        jdbcTemplate.update("INSERT INTO employees (first_name, last_name, email, salary, position, company, status) VALUES (?, ?, ?, ?, ?, ?, ?)",
//                "Anna", "Nowak", "anna@techcorp.com", 0.0, "MANAGER", "TechCorp", "ACTIVE");
//
//        List<CompanyStatistics> statistics = employeeDAO.getCompanyStatistics();
//
//        CompanyStatistics stats = statistics.get(0);
//        assertThat(stats.getAverageSalary()).isEqualTo(0.0);
//        assertThat(stats.getMaxSalary()).isEqualTo(0.0);
//    }
//
//    @Test
//    void shouldHandleLargeSalaryValues() {
//        Employee employee = createTestEmployee("ceo@techcorp.com", "CEO");
//        employee.setPosition(Position.PRESIDENT);
//        employee.setSalary(1_000_000.0);
//        employeeDAO.save(employee);
//
//        Employee saved = employeeDAO.findByEmail("ceo@techcorp.com").get();
//        assertThat(saved.getSalary()).isEqualTo(1_000_000.0);
//    }
//
//    @Test
//    void shouldHandleSpecialCharactersInNames() {
//        Employee employee = createTestEmployee("jozef@techcorp.com", "Józef Żółć");
//        employeeDAO.save(employee);
//
//        Employee found = employeeDAO.findByEmail("jozef@techcorp.com").get();
//
//        assertThat(found.getName()).isEqualTo("Józef Żółć");
//        assertThat(found.getFirstName()).isEqualTo("Józef");
//        assertThat(found.getLastName()).isEqualTo("Żółć");
//    }
//
//    @Test
//    void shouldFindEmployeesByDepartmentId() {
//        Employee employee1 = createTestEmployee("emp1@techcorp.com", "Employee One");
//        employee1.setDepartmentId(1L);
//        employeeDAO.save(employee1);
//
//        Employee employee2 = createTestEmployee("emp2@techcorp.com", "Employee Two");
//        employee2.setDepartmentId(1L);
//        employeeDAO.save(employee2);
//
//        List<Employee> employees = employeeDAO.findByDepartmentId(1L);
//
//        assertThat(employees).hasSize(2);
//    }
//
//    @Test
//    void shouldReturnTrueWhenEmployeeExistsByEmail() {
//        Employee employee = createTestEmployee("test@techcorp.com", "Test Employee");
//        employeeDAO.save(employee);
//
//        boolean exists = employeeDAO.existsByEmail("test@techcorp.com");
//
//        assertThat(exists).isTrue();
//    }
//
//    @Test
//    void shouldFindEmployeeById() {
//        Employee employee = createTestEmployee("test@techcorp.com", "Test Employee");
//        employeeDAO.save(employee);
//
//        // Pobierz employee z bazy aby dostać ID
//        Employee saved = employeeDAO.findByEmail("test@techcorp.com").get();
//        Optional<Employee> found = employeeDAO.findById(saved.getId());
//
//        assertThat(found).isPresent();
//        assertThat(found.get().getEmail()).isEqualTo("test@techcorp.com");
//    }
//
//    @Test
//    void shouldReturnEmptyListForNonExistingDepartment() {
//        List<Employee> employees = employeeDAO.findByDepartmentId(999L);
//
//        assertThat(employees).isEmpty();
//    }
//
//    @Test
//    void shouldReturnEmptyListForEmployeesWithoutDepartmentWhenAllHaveDepartment() {
//        Employee employee = createTestEmployee("test@techcorp.com", "Test Employee");
//        employee.setDepartmentId(1L);
//        employeeDAO.save(employee);
//
//        List<Employee> employees = employeeDAO.findEmployeesWithoutDepartment();
//
//        assertThat(employees).isEmpty();
//    }
//
//    @Test
//    void shouldDeleteAllEmployees() {
//        createTestEmployees();
//
//        employeeDAO.deleteAll();
//
//        List<Employee> employees = employeeDAO.findAll();
//        assertThat(employees).isEmpty();
//    }
//
//    @Test
//    void shouldHandleEmptyDatabaseInDeleteAll() {
//        employeeDAO.deleteAll();
//
//        List<Employee> employees = employeeDAO.findAll();
//        assertThat(employees).isEmpty();
//    }
//
//    @Test
//    void shouldHandleCompanyStatisticsForSingleEmployee() {
//        jdbcTemplate.update("INSERT INTO employees (first_name, last_name, email, salary, position, company, status) VALUES (?, ?, ?, ?, ?, ?, ?)",
//                "Anna", "Nowak", "anna@techcorp.com", 5000.0, "MANAGER", "TechCorp", "ACTIVE");
//
//        List<CompanyStatistics> statistics = employeeDAO.getCompanyStatistics();
//
//        CompanyStatistics stats = statistics.get(0);
//        assertThat(stats.getEmployeeCount()).isEqualTo(1);
//        assertThat(stats.getAverageSalary()).isEqualTo(5000.0);
//        assertThat(stats.getMaxSalary()).isEqualTo(5000.0);
//    }
//
//    @Test
//    void shouldHandleMultipleCompaniesInStatistics() {
//        jdbcTemplate.update("INSERT INTO employees (first_name, last_name, email, salary, position, company, status) VALUES (?, ?, ?, ?, ?, ?, ?)",
//                "Anna", "Nowak", "anna@techcorp.com", 5000.0, "MANAGER", "TechCorp", "ACTIVE");
//        jdbcTemplate.update("INSERT INTO employees (first_name, last_name, email, salary, position, company, status) VALUES (?, ?, ?, ?, ?, ?, ?)",
//                "Jan", "Kowalski", "jan@othercorp.com", 6000.0, "PROGRAMMER", "OtherCorp", "ACTIVE");
//
//        List<CompanyStatistics> statistics = employeeDAO.getCompanyStatistics();
//
//        assertThat(statistics).hasSize(2);
//    }
//
//    @Test
//    void shouldHandleZeroEmployeeCountInStatistics() {
//        List<CompanyStatistics> statistics = employeeDAO.getCompanyStatistics();
//
//        assertThat(statistics).isEmpty();
//    }
//
//    @Test
//    void shouldHandleAllEmploymentStatuses() {
//        for (EmploymentStatus status : EmploymentStatus.values()) {
//            Employee employee = createTestEmployee(status.name().toLowerCase() + "@techcorp.com", "Test Employee");
//            employee.setStatus(status);
//            employeeDAO.save(employee);
//
//            Employee found = employeeDAO.findByEmail(status.name().toLowerCase() + "@techcorp.com").get();
//            assertThat(found.getStatus()).isEqualTo(status);
//        }
//    }
//
//    @Test
//    void shouldHandleAllPositions() {
//        for (Position position : Position.values()) {
//            Employee employee = createTestEmployee(position.name().toLowerCase() + "@techcorp.com", "Test Employee");
//            employee.setPosition(position);
//            employeeDAO.save(employee);
//
//            Employee found = employeeDAO.findByEmail(position.name().toLowerCase() + "@techcorp.com").get();
//            assertThat(found.getPosition()).isEqualTo(position);
//        }
//    }
//
//    @Test
//    void shouldHandleDecimalSalaries() {
//        Employee employee = createTestEmployee("test@techcorp.com", "Test Employee");
//        employee.setSalary(1234.56);
//        employeeDAO.save(employee);
//
//        Employee found = employeeDAO.findByEmail("test@techcorp.com").get();
//        assertThat(found.getSalary()).isEqualTo(1234.56);
//    }
//
//    @Test
//    void shouldHandlePhotoFileNameWithSpecialCharacters() {
//        Employee employee = createTestEmployee("test@techcorp.com", "Test Employee");
//        employee.setPhotoFileName("photo with spaces and (special) characters.jpg");
//        employeeDAO.save(employee);
//
//        Employee found = employeeDAO.findByEmail("test@techcorp.com").get();
//        assertThat(found.getPhotoFileName()).isEqualTo("photo with spaces and (special) characters.jpg");
//    }
//
//    @Test
//    void shouldSetIdAfterInsert() {
//        Employee employee = createTestEmployee("test@techcorp.com", "Test Employee");
//
//        employeeDAO.save(employee);
//
//        Employee saved = employeeDAO.findByEmail("test@techcorp.com").get();
//        assertThat(saved.getId()).isNotNull();
//        assertThat(saved.getId()).isPositive();
//    }
//
//    private Employee createTestEmployee(String email, String name) {
//        Employee employee = new Employee();
//        employee.setName(name);
//        employee.setEmail(email);
//        employee.setCompany("TechCorp");
//        employee.setPosition(Position.PROGRAMMER);
//        employee.setSalary(5000.0);
//        employee.setStatus(EmploymentStatus.ACTIVE);
//        return employee;
//    }
//
//    private void createTestEmployees() {
//        Employee emp1 = createTestEmployee("anna@techcorp.com", "Anna Nowak");
//        emp1.setPosition(Position.MANAGER);
//        emp1.setSalary(7000.0);
//        employeeDAO.save(emp1);
//
//        Employee emp2 = createTestEmployee("jan@techcorp.com", "Jan Kowalski");
//        employeeDAO.save(emp2);
//
//        Employee emp3 = createTestEmployee("piotr@othercorp.com", "Piotr Wiśniewski");
//        emp3.setCompany("OtherCorp");
//        emp3.setSalary(4000.0);
//        employeeDAO.save(emp3);
//    }
//}