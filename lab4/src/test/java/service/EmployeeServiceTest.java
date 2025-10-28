package service;

import com.techcorp.employee.service.*;

import com.techcorp.employee.exception.InvalidDataException;
import com.techcorp.employee.model.Employee;
import com.techcorp.employee.model.Position;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class EmployeeServiceTest {

    private EmployeeService employeeService;

    @BeforeEach
    void setUp() {
        employeeService = new EmployeeService();
    }

    @Test
    void shouldAddEmployee() throws InvalidDataException {
        // Given
        Employee employee = new Employee("John Doe", "john.doe@company.com", "TechCorp", Position.PROGRAMMER, 8000);

        // When
        boolean result = employeeService.addEmployee(employee);

        // Then
        assertTrue(result);
        assertEquals(1, employeeService.getEmployeeCount());
    }

    @Test
    void shouldNotAddDuplicateEmployee() throws InvalidDataException {
        // Given
        Employee employee1 = new Employee("John Doe", "john.doe@company.com", "TechCorp", Position.PROGRAMMER, 8000);
        Employee employee2 = new Employee("Jane Smith", "john.doe@company.com", "TechCorp", Position.MANAGER, 12000);

        // When
        employeeService.addEmployee(employee1);

        // Then
        assertThrows(InvalidDataException.class, () -> employeeService.addEmployee(employee2));
        assertEquals(1, employeeService.getEmployeeCount());
    }

    @Test
    void shouldFindEmployeeByEmail() throws InvalidDataException {
        // Given
        Employee employee = new Employee("John Doe", "john.doe@company.com", "TechCorp", Position.PROGRAMMER, 8000);
        employeeService.addEmployee(employee);

        // When
        Optional<Employee> found = employeeService.findEmployeeByEmail("john.doe@company.com");

        // Then
        assertTrue(found.isPresent());
        assertEquals("John Doe", found.get().getName());
        assertEquals("John", found.get().getFirstName());
        assertEquals("Doe", found.get().getLastName());
    }

    @Test
    void shouldRemoveEmployee() throws InvalidDataException {
        // Given
        Employee employee = new Employee("John Doe", "john.doe@company.com", "TechCorp", Position.PROGRAMMER, 8000);
        employeeService.addEmployee(employee);

        // When
        boolean result = employeeService.removeEmployee("john.doe@company.com");

        // Then
        assertTrue(result);
        assertEquals(0, employeeService.getEmployeeCount());
    }

    @Test
    void shouldGetAllEmployees() throws InvalidDataException {
        // Given
        Employee employee1 = new Employee("John Doe", "john.doe@company.com", "TechCorp", Position.PROGRAMMER, 8000);
        Employee employee2 = new Employee("Jane Smith", "jane.smith@company.com", "TechCorp", Position.MANAGER, 12000);
        employeeService.addEmployee(employee1);
        employeeService.addEmployee(employee2);

        // When
        List<Employee> employees = employeeService.getAllEmployees();

        // Then
        assertEquals(2, employees.size());
    }

    @Test
    void shouldCheckIfEmployeeExists() throws InvalidDataException {
        // Given
        Employee employee = new Employee("John Doe", "john.doe@company.com", "TechCorp", Position.PROGRAMMER, 8000);
        employeeService.addEmployee(employee);

        // When & Then
        assertTrue(employeeService.employeeExists("john.doe@company.com"));
        assertFalse(employeeService.employeeExists("nonexistent@company.com"));
    }

    @Test
    void shouldGetEmployeesByCompany() throws InvalidDataException {
        // Given
        Employee employee1 = new Employee("John Doe", "john.doe@techcorp.com", "TechCorp", Position.PROGRAMMER, 8000);
        Employee employee2 = new Employee("Jane Smith", "jane.smith@innovate.com", "Innovate Inc", Position.MANAGER, 12000);
        Employee employee3 = new Employee("Bob Johnson", "bob.johnson@techcorp.com", "TechCorp", Position.PROGRAMMER, 8500);

        employeeService.addEmployee(employee1);
        employeeService.addEmployee(employee2);
        employeeService.addEmployee(employee3);

        // When
        List<Employee> techCorpEmployees = employeeService.getEmployeesByCompany("TechCorp");

        // Then
        assertEquals(2, techCorpEmployees.size());
    }

    @Test
    void shouldSortEmployeesByName() throws InvalidDataException {
        // Given
        Employee employee1 = new Employee("John Doe", "john.doe@company.com", "TechCorp", Position.PROGRAMMER, 8000);
        Employee employee2 = new Employee("Alice Adams", "alice.adams@company.com", "TechCorp", Position.MANAGER, 12000);
        Employee employee3 = new Employee("Bob Brown", "bob.brown@company.com", "TechCorp", Position.PROGRAMMER, 8500);

        employeeService.addEmployee(employee1);
        employeeService.addEmployee(employee2);
        employeeService.addEmployee(employee3);

        // When
        List<Employee> sorted = employeeService.sortEmployeesByName();

        // Then
        assertEquals("Adams", sorted.get(0).getLastName());
        assertEquals("Brown", sorted.get(1).getLastName());
        assertEquals("Doe", sorted.get(2).getLastName());
    }

    @Test
    void shouldGroupEmployeesByPosition() throws InvalidDataException {
        // Given
        Employee employee1 = new Employee("John Doe", "john.doe@company.com", "TechCorp", Position.PROGRAMMER, 8000);
        Employee employee2 = new Employee("Jane Smith", "jane.smith@company.com", "TechCorp", Position.MANAGER, 12000);
        Employee employee3 = new Employee("Bob Johnson", "bob.johnson@company.com", "TechCorp", Position.PROGRAMMER, 8500);

        employeeService.addEmployee(employee1);
        employeeService.addEmployee(employee2);
        employeeService.addEmployee(employee3);

        // When
        Map<Position, List<Employee>> grouped = employeeService.groupEmployeesByPosition();

        // Then
        assertEquals(2, grouped.get(Position.PROGRAMMER).size());
        assertEquals(1, grouped.get(Position.MANAGER).size());
    }

    @Test
    void shouldCountEmployeesByPosition() throws InvalidDataException {
        // Given
        Employee employee1 = new Employee("John Doe", "john.doe@company.com", "TechCorp", Position.PROGRAMMER, 8000);
        Employee employee2 = new Employee("Jane Smith", "jane.smith@company.com", "TechCorp", Position.MANAGER, 12000);
        Employee employee3 = new Employee("Bob Johnson", "bob.johnson@company.com", "TechCorp", Position.PROGRAMMER, 8500);

        employeeService.addEmployee(employee1);
        employeeService.addEmployee(employee2);
        employeeService.addEmployee(employee3);

        // When
        Map<Position, Long> counts = employeeService.countEmployeesByPosition();

        // Then
        assertEquals(2, counts.get(Position.PROGRAMMER));
        assertEquals(1, counts.get(Position.MANAGER));
    }

    @Test
    void shouldGroupEmployeesByCompany() throws InvalidDataException {
        // Given
        Employee employee1 = new Employee("John Doe", "john.doe@techcorp.com", "TechCorp", Position.PROGRAMMER, 8000);
        Employee employee2 = new Employee("Jane Smith", "jane.smith@innovate.com", "Innovate Inc", Position.MANAGER, 12000);
        Employee employee3 = new Employee("Bob Johnson", "bob.johnson@techcorp.com", "TechCorp", Position.PROGRAMMER, 8500);

        employeeService.addEmployee(employee1);
        employeeService.addEmployee(employee2);
        employeeService.addEmployee(employee3);

        // When
        Map<String, List<Employee>> grouped = employeeService.groupEmployeesByCompany();

        // Then
        assertEquals(2, grouped.get("TechCorp").size());
        assertEquals(1, grouped.get("Innovate Inc").size());
    }

    @Test
    void shouldCalculateAverageSalary() throws InvalidDataException {
        // Given
        Employee employee1 = new Employee("John Doe", "john.doe@company.com", "TechCorp", Position.PROGRAMMER, 8000);
        Employee employee2 = new Employee("Jane Smith", "jane.smith@company.com", "TechCorp", Position.MANAGER, 12000);

        employeeService.addEmployee(employee1);
        employeeService.addEmployee(employee2);

        // When
        double averageSalary = employeeService.calculateAverageSalary().orElse(0.0);

        // Then
        assertEquals(10000.0, averageSalary, 0.001);
    }

    @Test
    void shouldCalculateAverageSalaryByCompany() throws InvalidDataException {
        // Given
        Employee employee1 = new Employee("John Doe", "john.doe@techcorp.com", "TechCorp", Position.PROGRAMMER, 8000);
        Employee employee2 = new Employee("Jane Smith", "jane.smith@techcorp.com", "TechCorp", Position.MANAGER, 12000);
        Employee employee3 = new Employee("Bob Johnson", "bob.johnson@other.com", "Other Corp", Position.PROGRAMMER, 7000);

        employeeService.addEmployee(employee1);
        employeeService.addEmployee(employee2);
        employeeService.addEmployee(employee3);

        // When
        double averageSalary = employeeService.calculateAverageSalaryByCompany("TechCorp").orElse(0.0);

        // Then
        assertEquals(10000.0, averageSalary, 0.001);
    }

    @Test
    void shouldFindHighestPaidEmployee() throws InvalidDataException {
        // Given
        Employee employee1 = new Employee("John Doe", "john.doe@company.com", "TechCorp", Position.PROGRAMMER, 8000);
        Employee employee2 = new Employee("Jane Smith", "jane.smith@company.com", "TechCorp", Position.MANAGER, 12000);
        Employee employee3 = new Employee("Bob Johnson", "bob.johnson@company.com", "TechCorp", Position.PROGRAMMER, 8500);

        employeeService.addEmployee(employee1);
        employeeService.addEmployee(employee2);
        employeeService.addEmployee(employee3);

        // When
        Optional<Employee> highestPaid = employeeService.findHighestPaidEmployee();

        // Then
        assertTrue(highestPaid.isPresent());
        assertEquals("Jane Smith", highestPaid.get().getName());
        assertEquals(12000.0, highestPaid.get().getSalary(), 0.001);
    }

    @Test
    void shouldFindLowestPaidEmployee() throws InvalidDataException {
        // Given
        Employee employee1 = new Employee("John Doe", "john.doe@company.com", "TechCorp", Position.PROGRAMMER, 8000);
        Employee employee2 = new Employee("Jane Smith", "jane.smith@company.com", "TechCorp", Position.MANAGER, 12000);
        Employee employee3 = new Employee("Bob Johnson", "bob.johnson@company.com", "TechCorp", Position.PROGRAMMER, 7500);

        employeeService.addEmployee(employee1);
        employeeService.addEmployee(employee2);
        employeeService.addEmployee(employee3);

        // When
        Optional<Employee> lowestPaid = employeeService.findLowestPaidEmployee();

        // Then
        assertTrue(lowestPaid.isPresent());
        assertEquals("Bob Johnson", lowestPaid.get().getName());
        assertEquals(7500.0, lowestPaid.get().getSalary(), 0.001);
    }

    @Test
    void shouldCalculateTotalSalaryCost() throws InvalidDataException {
        // Given
        Employee employee1 = new Employee("John Doe", "john.doe@company.com", "TechCorp", Position.PROGRAMMER, 8000);
        Employee employee2 = new Employee("Jane Smith", "jane.smith@company.com", "TechCorp", Position.MANAGER, 12000);

        employeeService.addEmployee(employee1);
        employeeService.addEmployee(employee2);

        // When
        double totalCost = employeeService.calculateTotalSalaryCost();

        // Then
        assertEquals(20000.0, totalCost, 0.001);
    }

    @Test
    void shouldCalculateTotalSalaryCostByCompany() throws InvalidDataException {
        // Given
        Employee employee1 = new Employee("John Doe", "john.doe@techcorp.com", "TechCorp", Position.PROGRAMMER, 8000);
        Employee employee2 = new Employee("Jane Smith", "jane.smith@techcorp.com", "TechCorp", Position.MANAGER, 12000);
        Employee employee3 = new Employee("Bob Johnson", "bob.johnson@other.com", "Other Corp", Position.PROGRAMMER, 7000);

        employeeService.addEmployee(employee1);
        employeeService.addEmployee(employee2);
        employeeService.addEmployee(employee3);

        // When
        double totalCost = employeeService.calculateTotalSalaryCostByCompany("TechCorp");

        // Then
        assertEquals(20000.0, totalCost, 0.001);
    }

    @Test
    void shouldGetEmployeeCount() throws InvalidDataException {
        // Given
        Employee employee1 = new Employee("John Doe", "john.doe@company.com", "TechCorp", Position.PROGRAMMER, 8000);
        Employee employee2 = new Employee("Jane Smith", "jane.smith@company.com", "TechCorp", Position.MANAGER, 12000);

        // When
        employeeService.addEmployee(employee1);
        employeeService.addEmployee(employee2);

        // Then
        assertEquals(2, employeeService.getEmployeeCount());
    }

    @Test
    void shouldGetEmployeeCountByCompany() throws InvalidDataException {
        // Given
        Employee employee1 = new Employee("John Doe", "john.doe@techcorp.com", "TechCorp", Position.PROGRAMMER, 8000);
        Employee employee2 = new Employee("Jane Smith", "jane.smith@techcorp.com", "TechCorp", Position.MANAGER, 12000);
        Employee employee3 = new Employee("Bob Johnson", "bob.johnson@other.com", "Other Corp", Position.PROGRAMMER, 7000);

        employeeService.addEmployee(employee1);
        employeeService.addEmployee(employee2);
        employeeService.addEmployee(employee3);

        // When
        long count = employeeService.getEmployeeCountByCompany("TechCorp");

        // Then
        assertEquals(2, count);
    }

    @Test
    void shouldCheckIfEmpty() throws InvalidDataException {
        // Given & When & Then
        assertTrue(employeeService.isEmpty());

        // When adding an employee
        Employee employee = new Employee("John Doe", "john.doe@company.com", "TechCorp", Position.PROGRAMMER, 8000);
        assertDoesNotThrow(() -> employeeService.addEmployee(employee));

        // Then
        assertFalse(employeeService.isEmpty());
    }

    @Test
    void shouldValidateSalaryConsistency() throws InvalidDataException {
        // Given
        Employee employee1 = new Employee("John Doe", "john.doe@company.com", "TechCorp", Position.PROGRAMMER, 6000);
        Employee employee2 = new Employee("Jane Smith", "jane.smith@company.com", "TechCorp", Position.MANAGER, 12000);
        Employee employee3 = new Employee("Bob Johnson", "bob.johnson@company.com", "TechCorp", Position.INTERN, 2500);

        employeeService.addEmployee(employee1);
        employeeService.addEmployee(employee2);
        employeeService.addEmployee(employee3);

        // When
        List<Employee> underpaid = employeeService.validateSalaryConsistency(5000);

        // Then
        assertEquals(1, underpaid.size());
    }

    @Test
    void shouldGetCompanyStatistics() throws InvalidDataException {
        // Given
        Employee employee1 = new Employee("John Doe", "john.doe@techcorp.com", "TechCorp", Position.PROGRAMMER, 8000);
        Employee employee2 = new Employee("Jane Smith", "jane.smith@techcorp.com", "TechCorp", Position.MANAGER, 12000);

        employeeService.addEmployee(employee1);
        employeeService.addEmployee(employee2);

        // When
        var stats = employeeService.getCompanyStatistics("TechCorp");

        // Then
        assertEquals(2, stats.getEmployeeCount());
        assertEquals(10000.0, stats.getAverageSalary(), 0.001);
    }

    @Test
    void shouldGetAverageSalaryByPosition() throws InvalidDataException {
        // Given
        Employee employee1 = new Employee("John Doe", "john.doe@company.com", "TechCorp", Position.PROGRAMMER, 8000);
        Employee employee2 = new Employee("Jane Smith", "jane.smith@company.com", "TechCorp", Position.PROGRAMMER, 9000);
        Employee employee3 = new Employee("Bob Johnson", "bob.johnson@company.com", "TechCorp", Position.MANAGER, 12000);

        employeeService.addEmployee(employee1);
        employeeService.addEmployee(employee2);
        employeeService.addEmployee(employee3);

        // When
        Map<Position, Double> avgSalaries = employeeService.getAverageSalaryByPosition();

        // Then
        assertEquals(8500.0, avgSalaries.get(Position.PROGRAMMER), 0.001);
        assertEquals(12000.0, avgSalaries.get(Position.MANAGER), 0.001);
    }

    @Test
    void shouldAddAllEmployees() throws InvalidDataException {
        // Given
        Employee employee1 = new Employee("John Doe", "john.doe@company.com", "TechCorp", Position.PROGRAMMER, 8000);
        Employee employee2 = new Employee("Jane Smith", "jane.smith@company.com", "TechCorp", Position.MANAGER, 12000);
        List<Employee> employeesToAdd = List.of(employee1, employee2);

        // When
        employeeService.addAllEmployees(employeesToAdd);

        // Then
        assertEquals(2, employeeService.getEmployeeCount());
    }
}