package service;

import src.service.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import src.exception.InvalidDataException;
import src.model.CompanyStatistics;
import src.model.Employee;
import src.model.Position;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;

import static org.junit.jupiter.api.Assertions.*;

class EmployeeServiceTest {

    private EmployeeService employeeService;
    private Employee employee1;
    private Employee employee2;
    private Employee employee3;

    @BeforeEach
    void setUp() throws InvalidDataException {
        employeeService = new EmployeeService();
        initializeTestEmployees();
        addAllEmployeesToService();
    }

    private void initializeTestEmployees() throws InvalidDataException {
        employee1 = new Employee(
                "Jan Kowalski",
                "jan.kowalski@techcorp.com",
                "TechCorp",
                Position.MANAGER,
                15000
        );

        employee2 = new Employee(
                "Anna Nowak",
                "anna.nowak@techcorp.com",
                "TechCorp",
                Position.PROGRAMMER,
                9000
        );

        employee3 = new Employee(
                "Piotr Wiśniewski",
                "piotr.wisniewski@othercorp.com",
                "OtherCorp",
                Position.INTERN,
                3500
        );
    }

    private void addAllEmployeesToService() throws InvalidDataException {
        employeeService.addEmployee(employee1);
        employeeService.addEmployee(employee2);
        employeeService.addEmployee(employee3);
    }

    // ===== TESTOWANIE OPERACJI PODSTAWOWYCH =====

    @Test
    @DisplayName("Should return true when adding employee with unique email")
    void addEmployee_WithUniqueEmail_ShouldReturnTrue() throws InvalidDataException {
        EmployeeService emptyService = new EmployeeService();
        Employee newEmployee = new Employee("Test Employee", "test@test.com", "TestCorp", Position.PROGRAMMER, 5000);

        boolean result = emptyService.addEmployee(newEmployee);

        assertTrue(result);
    }

    @Test
    @DisplayName("Should increment employee count after adding employee")
    void addEmployee_WithUniqueEmail_ShouldIncrementCount() throws InvalidDataException {
        EmployeeService emptyService = new EmployeeService();
        Employee newEmployee = new Employee("Test Employee", "test@test.com", "TestCorp", Position.PROGRAMMER, 5000);

        emptyService.addEmployee(newEmployee);

        assertEquals(1, emptyService.getEmployeeCount());
    }

    @Test
    @DisplayName("Should mark employee as existing after addition")
    void addEmployee_WithUniqueEmail_ShouldMarkEmployeeAsExisting() throws InvalidDataException {
        EmployeeService emptyService = new EmployeeService();
        Employee newEmployee = new Employee("Test Employee", "test@test.com", "TestCorp", Position.PROGRAMMER, 5000);

        emptyService.addEmployee(newEmployee);

        assertTrue(emptyService.employeeExists(newEmployee.getEmail()));
    }

    @Test
    @DisplayName("Should throw exception when adding employee with duplicate email")
    void addEmployee_WithDuplicateEmail_ShouldThrowException() throws InvalidDataException {
        Employee duplicateEmployee = new Employee(
                "Different Name",
                employee1.getEmail(),
                "Different Corp",
                Position.PROGRAMMER,
                8000
        );

        InvalidDataException exception = assertThrows(
                InvalidDataException.class,
                () -> employeeService.addEmployee(duplicateEmployee)
        );

        assertTrue(exception.getMessage().contains("already exists"));
    }

    @Test
    @DisplayName("Should include email in duplicate exception message")
    void addEmployee_WithDuplicateEmail_ShouldIncludeEmailInMessage() throws InvalidDataException {
        Employee duplicateEmployee = new Employee(
                "Different Name",
                employee1.getEmail(),
                "Different Corp",
                Position.PROGRAMMER,
                8000
        );

        InvalidDataException exception = assertThrows(
                InvalidDataException.class,
                () -> employeeService.addEmployee(duplicateEmployee)
        );

        assertTrue(exception.getMessage().contains(employee1.getEmail()));
    }

    @Test
    @DisplayName("Should throw exception when adding null employee")
    void addEmployee_WithNullEmployee_ShouldThrowException() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> employeeService.addEmployee(null)
        );

        assertEquals("Employee cannot be null", exception.getMessage());
    }

    @Test
    @DisplayName("Should return true when removing existing employee")
    void removeEmployee_WithExistingEmail_ShouldReturnTrue() {
        boolean result = employeeService.removeEmployee(employee1.getEmail());

        assertTrue(result);
    }

    @Test
    @DisplayName("Should decrement count after removing employee")
    void removeEmployee_WithExistingEmail_ShouldDecrementCount() {
        employeeService.removeEmployee(employee1.getEmail());

        assertEquals(2, employeeService.getEmployeeCount());
    }

    @Test
    @DisplayName("Should mark employee as non-existent after removal")
    void removeEmployee_WithExistingEmail_ShouldRemoveEmployeeExistence() {
        employeeService.removeEmployee(employee1.getEmail());

        assertFalse(employeeService.employeeExists(employee1.getEmail()));
    }

    @Test
    @DisplayName("Should preserve other employees after removal")
    void removeEmployee_WithExistingEmail_ShouldPreserveOtherEmployees() {
        employeeService.removeEmployee(employee1.getEmail());

        assertTrue(employeeService.employeeExists(employee2.getEmail()));
    }

    @Test
    @DisplayName("Should return false when removing non-existing employee")
    void removeEmployee_WithNonExistingEmail_ShouldReturnFalse() {
        boolean result = employeeService.removeEmployee("nonexisting@email.com");

        assertFalse(result);
    }

    @Test
    @DisplayName("Should maintain count when removing non-existing employee")
    void removeEmployee_WithNonExistingEmail_ShouldMaintainCount() {
        employeeService.removeEmployee("nonexisting@email.com");

        assertEquals(3, employeeService.getEmployeeCount());
    }

    @Test
    @DisplayName("Should find employee by email ignoring case")
    void findEmployeeByEmail_WithExistingEmail_ShouldReturnEmployee() {
        Optional<Employee> found = employeeService.findEmployeeByEmail(employee1.getEmail().toUpperCase());

        assertTrue(found.isPresent());
    }

    @Test
    @DisplayName("Should return correct email when finding employee")
    void findEmployeeByEmail_WithExistingEmail_ShouldReturnCorrectEmail() {
        Optional<Employee> found = employeeService.findEmployeeByEmail(employee1.getEmail().toUpperCase());

        assertEquals(employee1.getEmail(), found.get().getEmail());
    }

    @Test
    @DisplayName("Should return correct first name when finding employee")
    void findEmployeeByEmail_WithExistingEmail_ShouldReturnCorrectFirstName() {
        Optional<Employee> found = employeeService.findEmployeeByEmail(employee1.getEmail().toUpperCase());

        assertEquals(employee1.getFirstName(), found.get().getFirstName());
    }

    @Test
    @DisplayName("Should return empty optional when employee not found")
    void findEmployeeByEmail_WithNonExistingEmail_ShouldReturnEmpty() {
        Optional<Employee> found = employeeService.findEmployeeByEmail("nonexisting@email.com");

        assertTrue(found.isEmpty());
    }

    @Test
    @DisplayName("Should return correct number of all employees")
    void getAllEmployees_ShouldReturnCorrectCount() {
        List<Employee> allEmployees = employeeService.getAllEmployees();

        assertEquals(3, allEmployees.size());
    }

    @Test
    @DisplayName("Should include all employees in returned list")
    void getAllEmployees_ShouldIncludeAllEmployees() {
        List<Employee> allEmployees = employeeService.getAllEmployees();

        assertAll(
                () -> assertTrue(allEmployees.contains(employee1)),
                () -> assertTrue(allEmployees.contains(employee2)),
                () -> assertTrue(allEmployees.contains(employee3))
        );
    }

    // ===== TESTOWANIE OPERACJI ANALITYCZNYCH =====

    @Test
    @DisplayName("Should return correct number of employees for company")
    void findEmployeesByCompany_WithExistingCompany_ShouldReturnCorrectCount() {
        List<Employee> techCorpEmployees = employeeService.findEmployeesByCompany("TechCorp");

        assertEquals(2, techCorpEmployees.size());
    }

    @Test
    @DisplayName("Should return only employees from specified company")
    void findEmployeesByCompany_WithExistingCompany_ShouldReturnOnlyCompanyEmployees() {
        List<Employee> techCorpEmployees = employeeService.findEmployeesByCompany("TechCorp");

        assertTrue(techCorpEmployees.stream().allMatch(emp -> emp.getCompany().equals("TechCorp")));
    }

    @Test
    @DisplayName("Should return empty list for non-existing company")
    void findEmployeesByCompany_WithNonExistingCompany_ShouldReturnEmptyList() {
        List<Employee> employees = employeeService.findEmployeesByCompany("NonExistingCorp");

        assertTrue(employees.isEmpty());
    }

    @ParameterizedTest
    @CsvSource({
            "TechCorp, 2",
            "OtherCorp, 1",
            "NonExistingCorp, 0"
    })
    @DisplayName("Should return correct employee count for various companies")
    void getEmployeeCountByCompany_WithVariousCompanies_ShouldReturnCorrectCount(String company, long expectedCount) {
        long count = employeeService.getEmployeeCountByCompany(company);

        assertEquals(expectedCount, count);
    }

    @Test
    @DisplayName("Should return sorted list of employees")
    void sortEmployeesByName_ShouldReturnSortedList() {
        List<Employee> sorted = employeeService.sortEmployeesByName();

        assertEquals(3, sorted.size());
    }

    @Test
    @DisplayName("Should sort employees by last name in correct order")
    void sortEmployeesByName_ShouldSortByLastName() {
        List<Employee> sorted = employeeService.sortEmployeesByName();

        assertAll(
                () -> assertEquals("Kowalski", sorted.get(0).getLastName()),
                () -> assertEquals("Nowak", sorted.get(1).getLastName()),
                () -> assertEquals("Wiśniewski", sorted.get(2).getLastName())
        );
    }

    @Test
    @DisplayName("Should group employees into correct number of position groups")
    void groupEmployeesByPosition_ShouldReturnCorrectNumberOfGroups() {
        Map<Position, List<Employee>> grouped = employeeService.groupEmployeesByPosition();

        assertEquals(3, grouped.size());
    }

    @Test
    @DisplayName("Should have correct employee count per position")
    void groupEmployeesByPosition_ShouldHaveCorrectCountsPerPosition() {
        Map<Position, List<Employee>> grouped = employeeService.groupEmployeesByPosition();

        assertAll(
                () -> assertEquals(1, grouped.get(Position.MANAGER).size()),
                () -> assertEquals(1, grouped.get(Position.PROGRAMMER).size()),
                () -> assertEquals(1, grouped.get(Position.INTERN).size())
        );
    }

    // ===== TESTOWANIE STATYSTYK FINANSOWYCH =====

    @Test
    @DisplayName("Should return non-empty optional when calculating average salary")
    void calculateAverageSalary_WithMultipleEmployees_ShouldReturnNonEmpty() {
        OptionalDouble average = employeeService.calculateAverageSalary();

        assertTrue(average.isPresent());
    }

    @Test
    @DisplayName("Should calculate correct average salary")
    void calculateAverageSalary_WithMultipleEmployees_ShouldReturnCorrectAverage() {
        OptionalDouble average = employeeService.calculateAverageSalary();

        assertEquals(9166.67, average.getAsDouble(), 0.01);
    }

    @Test
    @DisplayName("Should return empty optional when calculating average salary for empty service")
    void calculateAverageSalary_WithNoEmployees_ShouldReturnEmpty() {
        EmployeeService emptyService = new EmployeeService();

        OptionalDouble average = emptyService.calculateAverageSalary();

        assertTrue(average.isEmpty());
    }

    @Test
    @DisplayName("Should return non-empty optional when calculating average salary by company")
    void calculateAverageSalaryByCompany_ShouldReturnNonEmpty() {
        OptionalDouble result = employeeService.calculateAverageSalaryByCompany("TechCorp");

        assertTrue(result.isPresent());
    }

    @Test
    @DisplayName("Should calculate correct average salary by company")
    void calculateAverageSalaryByCompany_ShouldReturnCorrectValue() {
        OptionalDouble result = employeeService.calculateAverageSalaryByCompany("TechCorp");

        assertEquals(12000.0, result.getAsDouble(), 0.01);
    }

    @Test
    @DisplayName("Should return non-empty optional when finding highest paid employee")
    void findHighestPaidEmployee_ShouldReturnNonEmpty() {
        Optional<Employee> highestPaid = employeeService.findHighestPaidEmployee();

        assertTrue(highestPaid.isPresent());
    }

    @Test
    @DisplayName("Should return correct highest paid employee")
    void findHighestPaidEmployee_ShouldReturnCorrectEmployee() {
        Optional<Employee> highestPaid = employeeService.findHighestPaidEmployee();

        assertEquals(employee1.getEmail(), highestPaid.get().getEmail());
    }

    @Test
    @DisplayName("Should return correct salary for highest paid employee")
    void findHighestPaidEmployee_ShouldReturnCorrectSalary() {
        Optional<Employee> highestPaid = employeeService.findHighestPaidEmployee();

        assertEquals(15000, highestPaid.get().getSalary());
    }

    @Test
    @DisplayName("Should return empty optional when finding highest paid employee in empty service")
    void findHighestPaidEmployee_WithNoEmployees_ShouldReturnEmpty() {
        EmployeeService emptyService = new EmployeeService();

        Optional<Employee> highestPaid = emptyService.findHighestPaidEmployee();

        assertTrue(highestPaid.isEmpty());
    }

    @Test
    @DisplayName("Should return non-empty optional when finding lowest paid employee")
    void findLowestPaidEmployee_ShouldReturnNonEmpty() {
        Optional<Employee> lowestPaid = employeeService.findLowestPaidEmployee();

        assertTrue(lowestPaid.isPresent());
    }

    @Test
    @DisplayName("Should return correct lowest paid employee")
    void findLowestPaidEmployee_ShouldReturnCorrectEmployee() {
        Optional<Employee> lowestPaid = employeeService.findLowestPaidEmployee();

        assertEquals(employee3.getEmail(), lowestPaid.get().getEmail());
    }

    @Test
    @DisplayName("Should return correct salary for lowest paid employee")
    void findLowestPaidEmployee_ShouldReturnCorrectSalary() {
        Optional<Employee> lowestPaid = employeeService.findLowestPaidEmployee();

        assertEquals(3500, lowestPaid.get().getSalary());
    }

    @Test
    @DisplayName("Should calculate correct total salary cost")
    void calculateTotalSalaryCost_ShouldReturnCorrectSum() {
        double totalCost = employeeService.calculateTotalSalaryCost();

        assertEquals(27500, totalCost);
    }

    @Test
    @DisplayName("Should calculate correct total salary cost by company")
    void calculateTotalSalaryCostByCompany_ShouldReturnCorrectSum() {
        double totalCost = employeeService.calculateTotalSalaryCostByCompany("TechCorp");

        assertEquals(24000.0, totalCost);
    }

    // ===== TESTOWANIE WALIDACJI I SPÓJNOŚCI =====

    @Test
    @DisplayName("Should find inconsistent salaries when employees are underpaid")
    void validateSalaryConsistency_ShouldFindInconsistentSalaries() throws InvalidDataException {
        Employee underpaidEmployee = new Employee(
                "Underpaid Employee",
                "underpaid@test.com",
                "TestCorp",
                Position.MANAGER,
                10000
        );

        employeeService.addEmployee(underpaidEmployee);
        List<Employee> inconsistent = employeeService.validateSalaryConsistency();

        assertEquals(1, inconsistent.size());
    }

    @Test
    @DisplayName("Should identify correct underpaid employee")
    void validateSalaryConsistency_ShouldIdentifyCorrectEmployee() throws InvalidDataException {
        Employee underpaidEmployee = new Employee(
                "Underpaid Employee",
                "underpaid@test.com",
                "TestCorp",
                Position.MANAGER,
                10000
        );

        employeeService.addEmployee(underpaidEmployee);
        List<Employee> inconsistent = employeeService.validateSalaryConsistency();

        assertEquals(underpaidEmployee.getEmail(), inconsistent.get(0).getEmail());
    }

    @Test
    @DisplayName("Should return empty list when all salaries are consistent")
    void validateSalaryConsistency_WithConsistentSalaries_ShouldReturnEmptyList() {
        List<Employee> inconsistent = employeeService.validateSalaryConsistency();

        assertTrue(inconsistent.isEmpty());
    }

    // ===== TESTOWANIE STATYSTYK FIRM =====

    @Test
    @DisplayName("Should return correct number of company statistics")
    void getCompanyStatistics_ShouldReturnCorrectNumberOfStats() {
        Map<String, CompanyStatistics> stats = employeeService.getCompanyStatistics();

        assertEquals(2, stats.size());
    }

    @Test
    @DisplayName("Should return statistics for TechCorp")
    void getCompanyStatistics_ShouldReturnTechCorpStats() {
        Map<String, CompanyStatistics> stats = employeeService.getCompanyStatistics();

        assertNotNull(stats.get("TechCorp"));
    }

    @Test
    @DisplayName("Should return correct employee count for TechCorp")
    void getCompanyStatistics_ShouldReturnCorrectEmployeeCountForTechCorp() {
        Map<String, CompanyStatistics> stats = employeeService.getCompanyStatistics();

        assertEquals(2, stats.get("TechCorp").getEmployeeCount());
    }

    @Test
    @DisplayName("Should return correct average salary for TechCorp")
    void getCompanyStatistics_ShouldReturnCorrectAverageSalaryForTechCorp() {
        Map<String, CompanyStatistics> stats = employeeService.getCompanyStatistics();

        assertEquals(12000, stats.get("TechCorp").getAverageSalary(), 0.01);
    }

    @Test
    @DisplayName("Should return correct highest paid employee for TechCorp")
    void getCompanyStatistics_ShouldReturnCorrectHighestPaidForTechCorp() {
        Map<String, CompanyStatistics> stats = employeeService.getCompanyStatistics();

        assertEquals("Jan Kowalski", stats.get("TechCorp").getHighestPaidEmployee());
    }

    @Test
    @DisplayName("Should return statistics for OtherCorp")
    void getCompanyStatistics_ShouldReturnOtherCorpStats() {
        Map<String, CompanyStatistics> stats = employeeService.getCompanyStatistics();

        assertNotNull(stats.get("OtherCorp"));
    }

    @Test
    @DisplayName("Should return correct employee count for OtherCorp")
    void getCompanyStatistics_ShouldReturnCorrectEmployeeCountForOtherCorp() {
        Map<String, CompanyStatistics> stats = employeeService.getCompanyStatistics();

        assertEquals(1, stats.get("OtherCorp").getEmployeeCount());
    }

    // ===== TESTOWANIE STANU SERVICE =====

    @Test
    @DisplayName("Should return true when service is empty")
    void isEmpty_WithNoEmployees_ShouldReturnTrue() {
        EmployeeService emptyService = new EmployeeService();

        assertTrue(emptyService.isEmpty());
    }

    @Test
    @DisplayName("Should return false when service has employees")
    void isEmpty_WithEmployees_ShouldReturnFalse() {
        assertFalse(employeeService.isEmpty());
    }

    @Test
    @DisplayName("Should return correct employee count")
    void getEmployeeCount_ShouldReturnCorrectNumber() {
        assertEquals(3, employeeService.getEmployeeCount());
    }

    // ===== TESTOWANIE WALIDACJI DANYCH WEJŚCIOWYCH =====

    @ParameterizedTest
    @ValueSource(strings = {"", "  ", "\t", "\n"})
    @DisplayName("Should throw exception when validating empty email")
    void validateEmail_WithEmptyEmail_ShouldThrowException(String invalidEmail) {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> employeeService.findEmployeeByEmail(invalidEmail)
        );

        assertEquals("Email cannot be null or empty", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when validating null email")
    void validateEmail_WithNullEmail_ShouldThrowException() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> employeeService.findEmployeeByEmail(null)
        );

        assertEquals("Email cannot be null or empty", exception.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "  ", "\t", "\n"})
    @DisplayName("Should throw exception when validating empty company")
    void validateCompany_WithEmptyCompany_ShouldThrowException(String invalidCompany) {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> employeeService.findEmployeesByCompany(invalidCompany)
        );

        assertEquals("Company name cannot be null or empty", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when validating null company")
    void validateCompany_WithNullCompany_ShouldThrowException() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> employeeService.findEmployeesByCompany(null)
        );

        assertEquals("Company name cannot be null or empty", exception.getMessage());
    }

    // ===== TESTOWANIE PRZYPADKÓW BRZEGOWYCH =====

    @Test
    @DisplayName("Should handle case sensitivity in company names - lowercase")
    void handleCaseSensitiveCompanyNames_Lowercase() {
        List<Employee> lowerCase = employeeService.findEmployeesByCompany("techcorp");

        assertEquals(2, lowerCase.size());
    }

    @Test
    @DisplayName("Should handle case sensitivity in company names - uppercase")
    void handleCaseSensitiveCompanyNames_Uppercase() {
        List<Employee> upperCase = employeeService.findEmployeesByCompany("TECHCORP");

        assertEquals(2, upperCase.size());
    }

    @Test
    @DisplayName("Should handle case sensitivity in company names - mixed case")
    void handleCaseSensitiveCompanyNames_MixedCase() {
        List<Employee> mixedCase = employeeService.findEmployeesByCompany("TechCorp");

        assertEquals(2, mixedCase.size());
    }

    @Test
    @DisplayName("Should count employees by position - number of positions")
    void countEmployeesByPosition_ShouldReturnCorrectNumberOfPositions() {
        Map<Position, Long> positionCounts = employeeService.countEmployeesByPosition();

        assertEquals(3, positionCounts.size());
    }

    @Test
    @DisplayName("Should count employees by position - manager count")
    void countEmployeesByPosition_ShouldReturnCorrectManagerCount() {
        Map<Position, Long> positionCounts = employeeService.countEmployeesByPosition();

        assertEquals(1, positionCounts.get(Position.MANAGER));
    }

    @Test
    @DisplayName("Should group employees by company - number of companies")
    void groupEmployeesByCompany_ShouldReturnCorrectNumberOfCompanies() {
        Map<String, List<Employee>> groupedByCompany = employeeService.groupEmployeesByCompany();

        assertEquals(2, groupedByCompany.size());
    }

    @Test
    @DisplayName("Should group employees by company - TechCorp employee count")
    void groupEmployeesByCompany_ShouldReturnCorrectTechCorpCount() {
        Map<String, List<Employee>> groupedByCompany = employeeService.groupEmployeesByCompany();

        assertEquals(2, groupedByCompany.get("TechCorp").size());
    }
}