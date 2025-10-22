package service;

import src.service.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
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
    @DisplayName("Should add employee with unique email successfully")
    void addEmployee_WithUniqueEmail_ShouldSucceed() throws InvalidDataException {
        EmployeeService emptyService = new EmployeeService();
        Employee newEmployee = new Employee("Test Employee", "test@test.com", "TestCorp", Position.PROGRAMMER, 5000);

        boolean result = emptyService.addEmployee(newEmployee);

        assertAll(
                () -> assertTrue(result),
                () -> assertEquals(1, emptyService.getEmployeeCount()),
                () -> assertTrue(emptyService.employeeExists(newEmployee.getEmail()))
        );
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

        assertAll(
                () -> assertTrue(exception.getMessage().contains("already exists")),
                () -> assertTrue(exception.getMessage().contains(employee1.getEmail()))
        );
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

    @ParameterizedTest
    @CsvSource({
            "jan.kowalski@techcorp.com, true, 2, false",   // email, expectedResult, expectedCount, shouldExistAfter
            "nonexisting@email.com, false, 3, false"       // email, expectedResult, expectedCount, shouldExistAfter
    })
    @DisplayName("Should remove employee correctly based on email")
    void removeEmployee_WithVariousEmails_ShouldBehaveCorrectly(
            String email, boolean expectedResult, int expectedCount, boolean shouldExistAfter) {

        boolean result = employeeService.removeEmployee(email);

        assertAll(
                () -> assertEquals(expectedResult, result),
                () -> assertEquals(expectedCount, employeeService.getEmployeeCount()),
                () -> assertEquals(shouldExistAfter, employeeService.employeeExists(email))
        );
    }

    @ParameterizedTest
    @CsvSource({
            "jan.kowalski@techcorp.com, JAN.KOWALSKI@TECHCORP.COM, jan.kowalski@techcorp.com, Jan Kowalski",
            "anna.nowak@techcorp.com, ANNA.NOWAK@TECHCORP.COM, anna.nowak@techcorp.com, Anna Nowak",
            "TEST@example.com, test@EXAMPLE.COM, test@example.com, Test User"
    })
    @DisplayName("Should handle email case insensitivity consistently across operations")
    void emailOperations_WithCaseVariations_ShouldBeConsistent(
            String originalEmail, String searchEmail, String expectedStoredEmail, String name) throws InvalidDataException {

        EmployeeService service = new EmployeeService();
        Employee employee = new Employee(name, originalEmail, "Company", Position.PROGRAMMER, 5000);

        // Test dodawania i wyszukiwania
        service.addEmployee(employee);

        Optional<Employee> found = service.findEmployeeByEmail(searchEmail);

        assertAll(
                () -> assertTrue(found.isPresent(), "Should find employee with case-insensitive email"),
                () -> assertEquals(expectedStoredEmail, found.get().getEmail(), "Should store email in consistent case"),
                () -> assertEquals(name, found.get().getName()),
                () -> assertTrue(service.employeeExists(searchEmail), "Should exist when searched with different case"),
                () -> assertTrue(service.employeeExists(originalEmail), "Should exist when searched with original case")
        );
    }

    @Test
    @DisplayName("Should return empty optional when employee not found")
    void findEmployeeByEmail_WithNonExistingEmail_ShouldReturnEmpty() {
        Optional<Employee> found = employeeService.findEmployeeByEmail("nonexisting@email.com");

        assertTrue(found.isEmpty());
    }

    @Test
    @DisplayName("Should return all employees correctly")
    void getAllEmployees_ShouldReturnAllEmployees() {
        List<Employee> allEmployees = employeeService.getAllEmployees();

        assertAll(
                () -> assertEquals(3, allEmployees.size()),
                () -> assertTrue(allEmployees.contains(employee1)),
                () -> assertTrue(allEmployees.contains(employee2)),
                () -> assertTrue(allEmployees.contains(employee3))
        );
    }

    // ===== TESTOWANIE OPERACJI ANALITYCZNYCH =====

    @ParameterizedTest
    @CsvSource({
            "TechCorp, 2, true",
            "OtherCorp, 1, true",
            "NonExistingCorp, 0, false",
            "techcorp, 2, true",
            "TECHCORP, 2, true"
    })
    @DisplayName("Should find employees by company with case insensitivity")
    void findEmployeesByCompany_WithVariousCompanies_ShouldReturnCorrectResults(
            String company, int expectedCount, boolean shouldContainEmployees) {

        List<Employee> employees = employeeService.findEmployeesByCompany(company);

        assertAll(
                () -> assertEquals(expectedCount, employees.size()),
                () -> {
                    if (shouldContainEmployees) {
                        assertTrue(employees.stream().allMatch(emp -> emp.getCompany().equalsIgnoreCase(company)));
                    }
                }
        );
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
    @DisplayName("Should sort employees by last name correctly")
    void sortEmployeesByName_ShouldReturnSortedList() {
        List<Employee> sorted = employeeService.sortEmployeesByName();

        assertAll(
                () -> assertEquals(3, sorted.size()),
                () -> assertEquals("Kowalski", sorted.get(0).getLastName()),
                () -> assertEquals("Nowak", sorted.get(1).getLastName()),
                () -> assertEquals("Wiśniewski", sorted.get(2).getLastName())
        );
    }

    @Test
    @DisplayName("Should group employees by position correctly")
    void groupEmployeesByPosition_ShouldReturnCorrectGroups() {
        Map<Position, List<Employee>> grouped = employeeService.groupEmployeesByPosition();

        assertAll(
                () -> assertEquals(3, grouped.size()),
                () -> assertEquals(1, grouped.get(Position.MANAGER).size()),
                () -> assertEquals(1, grouped.get(Position.PROGRAMMER).size()),
                () -> assertEquals(1, grouped.get(Position.INTERN).size())
        );
    }

    // ===== TESTOWANIE STATYSTYK FINANSOWYCH =====

    @ParameterizedTest
    @CsvSource({
            "TechCorp, 12000.0",
            "OtherCorp, 3500.0"
    })
    @DisplayName("Should calculate correct average salary by company")
    void calculateAverageSalaryByCompany_ShouldReturnCorrectValue(String company, double expectedAverage) {
        OptionalDouble result = employeeService.calculateAverageSalaryByCompany(company);

        assertAll(
                () -> assertTrue(result.isPresent()),
                () -> assertEquals(expectedAverage, result.getAsDouble(), 0.01)
        );
    }

    @Test
    @DisplayName("Should handle salary calculations correctly")
    void salaryCalculations_ShouldReturnCorrectValues() {
        OptionalDouble average = employeeService.calculateAverageSalary();
        Optional<Employee> highestPaid = employeeService.findHighestPaidEmployee();
        Optional<Employee> lowestPaid = employeeService.findLowestPaidEmployee();
        double totalCost = employeeService.calculateTotalSalaryCost();

        assertAll(
                () -> assertTrue(average.isPresent()),
                () -> assertEquals(9166.67, average.getAsDouble(), 0.01),
                () -> assertTrue(highestPaid.isPresent()),
                () -> assertEquals(employee1.getEmail(), highestPaid.get().getEmail()),
                () -> assertEquals(15000, highestPaid.get().getSalary()),
                () -> assertTrue(lowestPaid.isPresent()),
                () -> assertEquals(employee3.getEmail(), lowestPaid.get().getEmail()),
                () -> assertEquals(3500, lowestPaid.get().getSalary()),
                () -> assertEquals(27500, totalCost)
        );
    }

    @ParameterizedTest
    @CsvSource({
            "0, 0.0",
            "1000, 1000.0",
            "1000000, 1000000.0",
            "0.5, 0.5",
            "999.99, 999.99",
            "50000, 50000.0"
    })
    @DisplayName("Should handle various non-negative salary values correctly")
    void addEmployee_WithVariousNonNegativeSalaryValues_ShouldHandleCorrectly(double salary, double expectedSalary) throws InvalidDataException {
        EmployeeService service = new EmployeeService();
        String email = "test" + salary + "@test.com";
        Employee employee = new Employee("Test Employee", email, "Company", Position.PROGRAMMER, salary);

        service.addEmployee(employee);
        Optional<Employee> found = service.findEmployeeByEmail(email);

        assertAll(
                () -> assertTrue(found.isPresent()),
                () -> assertEquals(expectedSalary, found.get().getSalary(), 0.001),
                () -> assertTrue(service.employeeExists(email))
        );
    }

    @ParameterizedTest
    @CsvSource({
            "-1000",
            "-999.99",
            "-0.01",
            "-50000",
            "-1",
            "-1000000"
    })
    @DisplayName("Should throw InvalidDataException when creating employee with negative salary")
    void createEmployee_WithNegativeSalary_ShouldThrowInvalidDataException(double negativeSalary) {
        String email = "test" + negativeSalary + "@test.com";

        InvalidDataException exception = assertThrows(
                InvalidDataException.class,
                () -> new Employee("Test Employee", email, "Company", Position.PROGRAMMER, negativeSalary)
        );

        assertTrue(exception.getMessage().toLowerCase().contains("salary"));
        assertTrue(exception.getMessage().toLowerCase().contains("negative"));
    }



    @Test
    @DisplayName("Should return empty results for empty service")
    void financialOperations_WithEmptyService_ShouldReturnEmpty() {
        EmployeeService emptyService = new EmployeeService();

        assertAll(
                () -> assertTrue(emptyService.calculateAverageSalary().isEmpty()),
                () -> assertTrue(emptyService.findHighestPaidEmployee().isEmpty()),
                () -> assertTrue(emptyService.findLowestPaidEmployee().isEmpty()),
                () -> assertEquals(0.0, emptyService.calculateTotalSalaryCost())
        );
    }

    // ===== TESTOWANIE WALIDACJI I SPÓJNOŚCI =====

    @Test
    @DisplayName("Should validate salary consistency correctly")
    void validateSalaryConsistency_ShouldIdentifyUnderpaidEmployees() throws InvalidDataException {
        Employee underpaidEmployee = new Employee(
                "Underpaid Employee",
                "underpaid@test.com",
                "TestCorp",
                Position.MANAGER,
                10000
        );

        employeeService.addEmployee(underpaidEmployee);
        List<Employee> inconsistent = employeeService.validateSalaryConsistency();

        assertAll(
                () -> assertEquals(1, inconsistent.size()),
                () -> assertEquals(underpaidEmployee.getEmail(), inconsistent.get(0).getEmail())
        );
    }

    @Test
    @DisplayName("Should return empty list when all salaries are consistent")
    void validateSalaryConsistency_WithConsistentSalaries_ShouldReturnEmptyList() {
        List<Employee> inconsistent = employeeService.validateSalaryConsistency();

        assertTrue(inconsistent.isEmpty());
    }



    // ===== TESTOWANIE STATYSTYK FIRM =====

    @Test
    @DisplayName("Should generate correct company statistics")
    void getCompanyStatistics_ShouldReturnCorrectStats() {
        Map<String, CompanyStatistics> stats = employeeService.getCompanyStatistics();

        assertAll(
                () -> assertEquals(2, stats.size()),
                () -> assertNotNull(stats.get("TechCorp")),
                () -> assertNotNull(stats.get("OtherCorp")),
                () -> assertEquals(2, stats.get("TechCorp").getEmployeeCount()),
                () -> assertEquals(12000, stats.get("TechCorp").getAverageSalary(), 0.01),
                () -> assertEquals("Jan Kowalski", stats.get("TechCorp").getHighestPaidEmployee()),
                () -> assertEquals(1, stats.get("OtherCorp").getEmployeeCount())
        );
    }

    // ===== TESTOWANIE STANU SERVICE =====

    @ParameterizedTest
    @CsvSource({
            "true, 0",
            "false, 3"
    })
    @DisplayName("Should correctly report empty state and employee count")
    void serviceState_ShouldBeCorrect(boolean expectedEmpty, int expectedCount) {
        EmployeeService service = expectedEmpty ? new EmployeeService() : employeeService;

        assertAll(
                () -> assertEquals(expectedEmpty, service.isEmpty()),
                () -> assertEquals(expectedCount, service.getEmployeeCount())
        );
    }

    // ===== TESTOWANIE WALIDACJI DANYCH WEJŚCIOWYCH =====

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  ", "\t", "\n"})
    @DisplayName("Should throw exception when validating empty or null email")
    void validateEmail_WithInvalidEmail_ShouldThrowException(String invalidEmail) {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> employeeService.findEmployeeByEmail(invalidEmail)
        );

        assertEquals("Email cannot be null or empty", exception.getMessage());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  ", "\t", "\n"})
    @DisplayName("Should throw exception when validating empty or null company")
    void validateCompany_WithInvalidCompany_ShouldThrowException(String invalidCompany) {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> employeeService.findEmployeesByCompany(invalidCompany)
        );

        assertEquals("Company name cannot be null or empty", exception.getMessage());
    }

    // ===== TESTOWANIE GRUPOWANIA I ZLICZANIA =====

    @Test
    @DisplayName("Should count employees by position correctly")
    void countEmployeesByPosition_ShouldReturnCorrectCounts() {
        Map<Position, Long> positionCounts = employeeService.countEmployeesByPosition();

        assertAll(
                () -> assertEquals(3, positionCounts.size()),
                () -> assertEquals(1, positionCounts.get(Position.MANAGER)),
                () -> assertEquals(1, positionCounts.get(Position.PROGRAMMER)),
                () -> assertEquals(1, positionCounts.get(Position.INTERN))
        );
    }

    @Test
    @DisplayName("Should group employees by company correctly")
    void groupEmployeesByCompany_ShouldReturnCorrectGroups() {
        Map<String, List<Employee>> groupedByCompany = employeeService.groupEmployeesByCompany();

        assertAll(
                () -> assertEquals(2, groupedByCompany.size()),
                () -> assertEquals(2, groupedByCompany.get("TechCorp").size()),
                () -> assertEquals(1, groupedByCompany.get("OtherCorp").size())
        );
    }

    @ParameterizedTest
    @CsvSource({
            "TechCorp, 24000.0",
            "OtherCorp, 3500.0",
            "NonExistingCorp, 0.0",
            "techcorp, 24000.0",
            "TECHCORP, 24000.0"
    })
    @DisplayName("Should calculate correct total salary cost by company")
    void calculateTotalSalaryCostByCompany_WithVariousCompanies_ShouldReturnCorrectTotal(
            String company, double expectedTotal) {

        double totalCost = employeeService.calculateTotalSalaryCostByCompany(company);

        assertEquals(expectedTotal, totalCost, 0.01);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  ", "\t", "\n"})
    @DisplayName("Should throw exception when calculating total cost with invalid company name")
    void calculateTotalSalaryCostByCompany_WithInvalidCompany_ShouldThrowException(String invalidCompany) {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> employeeService.calculateTotalSalaryCostByCompany(invalidCompany)
        );

        assertEquals("Company name cannot be null or empty", exception.getMessage());
    }

    @Test
    @DisplayName("Should handle case sensitivity in company names")
    void calculateTotalSalaryCostByCompany_ShouldBeCaseInsensitive() {
        double lowerCase = employeeService.calculateTotalSalaryCostByCompany("techcorp");
        double upperCase = employeeService.calculateTotalSalaryCostByCompany("TECHCORP");
        double mixedCase = employeeService.calculateTotalSalaryCostByCompany("TechCorp");

        assertAll(
                () -> assertEquals(24000.0, lowerCase, 0.01),
                () -> assertEquals(24000.0, upperCase, 0.01),
                () -> assertEquals(24000.0, mixedCase, 0.01)
        );
    }

    @Test
    @DisplayName("Should calculate correct total after adding new employee")
    void calculateTotalSalaryCostByCompany_AfterAddingEmployee_ShouldUpdateTotal() throws InvalidDataException {
        // Given
        double initialTotal = employeeService.calculateTotalSalaryCostByCompany("TechCorp");

        // When
        Employee newEmployee = new Employee(
                "New Employee",
                "new@techcorp.com",
                "TechCorp",
                Position.PROGRAMMER,
                5000
        );
        employeeService.addEmployee(newEmployee);

        // Then
        double updatedTotal = employeeService.calculateTotalSalaryCostByCompany("TechCorp");
        assertEquals(initialTotal + 5000, updatedTotal, 0.01);
    }

    @Test
    @DisplayName("Should calculate correct total after removing employee")
    void calculateTotalSalaryCostByCompany_AfterRemovingEmployee_ShouldUpdateTotal() {
        // Given
        double initialTotal = employeeService.calculateTotalSalaryCostByCompany("TechCorp");

        // When
        employeeService.removeEmployee(employee1.getEmail());

        // Then
        double updatedTotal = employeeService.calculateTotalSalaryCostByCompany("TechCorp");
        assertEquals(initialTotal - employee1.getSalary(), updatedTotal, 0.01);
    }

    @Test
    @DisplayName("Should return zero total for empty service")
    void calculateTotalSalaryCostByCompany_WithEmptyService_ShouldReturnZero() {
        EmployeeService emptyService = new EmployeeService();

        double totalCost = emptyService.calculateTotalSalaryCostByCompany("AnyCompany");

        assertEquals(0.0, totalCost);
    }

    @Test
    @DisplayName("Should return zero for non-existing company")
    void calculateTotalSalaryCostByCompany_WithNonExistingCompany_ShouldReturnZero() {
        double totalCost = employeeService.calculateTotalSalaryCostByCompany("NonExistingCompany123");

        assertEquals(0.0, totalCost);
    }
}