import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import src.exception.InvalidDataException;
import src.model.Employee;
import src.model.Position;
import src.service.EmployeeService;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;

import static org.junit.jupiter.api.Assertions.*;

class EmployeeManagementSystemTest {

    private EmployeeService system;
    private Employee emp1, emp2, emp3, emp4, emp5;

    @BeforeEach
    void setUp() throws InvalidDataException {
        system = new EmployeeService();
        emp1 = new Employee("Jan Kowalski", "jan.kowalski@techcorp.com", "TechCorp", Position.MANAGER, 15000);
        emp2 = new Employee("Anna Nowak", "anna.nowak@techcorp.com", "TechCorp", Position.PROGRAMMER, 9000);
        emp3 = new Employee("Piotr Wiśniewski", "piotr.wisniewski@other.com", "OtherCorp", Position.INTERN);
        emp4 = new Employee("Maria Zielińska", "maria.zielinska@techcorp.com", "TechCorp", Position.VICE_PRESIDENT, 20000);
        emp5 = new Employee("Krzysztof Lewandowski", "krzysztof.lewandowski@techcorp.com", "TechCorp", Position.PROGRAMMER, 8500);

        system.addEmployee(emp1);
        system.addEmployee(emp2);
        system.addEmployee(emp3);
        system.addEmployee(emp4);
        system.addEmployee(emp5);
    }

    @Test
    void testRemoveExistingEmployeeByEmail() {
        boolean removed = system.removeEmployee("piotr.wisniewski@other.com");
        assertTrue(removed);
    }

    @Test
    void testEmployeeNoLongerExistsAfterRemoval() {
        system.removeEmployee("piotr.wisniewski@other.com");
        assertFalse(system.employeeExists("piotr.wisniewski@other.com"));
    }

    @Test
    void testEmployeeCountDecreasesAfterRemoval() {
        system.removeEmployee("piotr.wisniewski@other.com");
        assertEquals(4, system.getEmployeeCount());
    }

    @Test
    void testRemoveNonExistentEmployeeReturnsFalse() {
        boolean removed = system.removeEmployee("nieistniejacy@email.com");
        assertFalse(removed);
    }

    @Test
    void testEmployeeCountUnchangedAfterRemovingNonExistent() {
        system.removeEmployee("nieistniejacy@email.com");
        assertEquals(5, system.getEmployeeCount());
    }

    @Test
    void testRemoveEmployeeWithNullEmailThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> system.removeEmployee(null));
    }

    @Test
    void testRemoveEmployeeWithEmptyEmailThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> system.removeEmployee(""));
    }

    @Test
    void testRemoveEmployeeWithBlankEmailThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> system.removeEmployee("   "));
    }

    @Test
    void testFindExistingEmployeeByEmail() {
        Optional<Employee> found = system.findEmployeeByEmail("anna.nowak@techcorp.com");
        assertTrue(found.isPresent());
    }

    @Test
    void testFoundEmployeeHasCorrectName() {
        Optional<Employee> found = system.findEmployeeByEmail("anna.nowak@techcorp.com");
        assertEquals("Anna Nowak", found.get().getName());
    }

    @Test
    void testFoundEmployeeIsCorrectInstance() {
        Optional<Employee> found = system.findEmployeeByEmail("anna.nowak@techcorp.com");
        assertEquals(emp2, found.get());
    }

    @Test
    void testFindEmployeeByEmailCaseInsensitive() {
        Optional<Employee> found = system.findEmployeeByEmail("ANNA.NOWAK@TECHCORP.COM");
        assertTrue(found.isPresent());
    }

    @Test
    void testCaseInsensitiveFoundEmployeeIsCorrect() {
        Optional<Employee> found = system.findEmployeeByEmail("ANNA.NOWAK@TECHCORP.COM");
        assertEquals(emp2, found.get());
    }

    @Test
    void testFindNonExistentEmployeeReturnsEmpty() {
        Optional<Employee> found = system.findEmployeeByEmail("nieistniejacy@email.com");
        assertFalse(found.isPresent());
    }

    @Test
    void testFindEmployeeWithNullEmailThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> system.findEmployeeByEmail(null));
    }

    @Test
    void testFindEmployeeWithEmptyEmailThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> system.findEmployeeByEmail(""));
    }

    @Test
    void testFindEmployeeWithBlankEmailThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> system.findEmployeeByEmail("   "));
    }

    @Test
    void testEmployeeExistsForValidEmail() {
        assertTrue(system.employeeExists("jan.kowalski@techcorp.com"));
    }

    @Test
    void testEmployeeExistsCaseInsensitive() {
        assertTrue(system.employeeExists("JAN.KOWALSKI@TECHCORP.COM"));
    }

    @Test
    void testEmployeeDoesNotExistForInvalidEmail() {
        assertFalse(system.employeeExists("nieistniejacy@email.com"));
    }

    @Test
    void testEmployeeExistsWithNullThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> system.employeeExists(null));
    }

    @Test
    void testEmployeeExistsWithEmptyThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> system.employeeExists(""));
    }

    @Test
    void testEmployeeExistsWithBlankThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> system.employeeExists("   "));
    }

    @Test
    void testInitialEmployeeCount() {
        assertEquals(5, system.getEmployeeCount());
    }

    @Test
    void testEmployeeCountAfterRemoval() {
        system.removeEmployee("piotr.wisniewski@other.com");
        assertEquals(4, system.getEmployeeCount());
    }

    @Test
    void testEmployeeCountAfterAddingNewEmployee() throws InvalidDataException {
        system.addEmployee(new Employee("Nowy Pracownik", "nowy@test.com", "TestCorp", Position.PROGRAMMER));
        assertEquals(6, system.getEmployeeCount());
    }

    @Test
    void testGetEmployeeCountForTechCorp() {
        assertEquals(4, system.getEmployeeCountByCompany("TechCorp"));
    }

    @Test
    void testGetEmployeeCountForOtherCorp() {
        assertEquals(1, system.getEmployeeCountByCompany("OtherCorp"));
    }

    @Test
    void testGetEmployeeCountForNonExistentCompany() {
        assertEquals(0, system.getEmployeeCountByCompany("NieistniejacaFirma"));
    }

    @Test
    void testGetEmployeeCountByCompanyCaseInsensitiveLowercase() {
        assertEquals(4, system.getEmployeeCountByCompany("techcorp"));
    }

    @Test
    void testGetEmployeeCountByCompanyCaseInsensitiveUppercase() {
        assertEquals(4, system.getEmployeeCountByCompany("TECHCORP"));
    }

    @Test
    void testGetEmployeeCountByCompanyWithNullThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> system.getEmployeeCountByCompany(null));
    }

    @Test
    void testGetEmployeeCountByCompanyWithEmptyThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> system.getEmployeeCountByCompany(""));
    }

    @Test
    void testGetEmployeeCountByCompanyWithBlankThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> system.getEmployeeCountByCompany("   "));
    }

    @Test
    void testNonEmptySystemIsNotEmpty() {
        assertFalse(system.isEmpty());
    }

    @Test
    void testNewEmptySystemIsEmpty() {
        EmployeeService emptySystem = new EmployeeService();
        assertTrue(emptySystem.isEmpty());
    }

    @Test
    void testSystemNotEmptyAfterAddingEmployee() throws InvalidDataException {
        EmployeeService emptySystem = new EmployeeService();
        emptySystem.addEmployee(emp1);
        assertFalse(emptySystem.isEmpty());
    }

    @Test
    void testCalculateAverageSalaryForTechCorp() {
        OptionalDouble techCorpAvg = system.calculateAverageSalaryByCompany("TechCorp");
        assertTrue(techCorpAvg.isPresent());
    }

    @Test
    void testTechCorpAverageSalaryValue() {
        OptionalDouble techCorpAvg = system.calculateAverageSalaryByCompany("TechCorp");
        double expectedTechCorpAvg = (15000 + 9000 + 20000 + 8500) / 4.0;
        assertEquals(expectedTechCorpAvg, techCorpAvg.getAsDouble(), 0.001);
    }

    @Test
    void testCalculateAverageSalaryForOtherCorp() {
        OptionalDouble otherCorpAvg = system.calculateAverageSalaryByCompany("OtherCorp");
        assertTrue(otherCorpAvg.isPresent());
    }

    @Test
    void testOtherCorpAverageSalaryValue() {
        OptionalDouble otherCorpAvg = system.calculateAverageSalaryByCompany("OtherCorp");
        assertEquals(3000, otherCorpAvg.getAsDouble(), 0.001);
    }

    @Test
    void testCalculateAverageSalaryForUnknownCompany() {
        OptionalDouble unknownCorpAvg = system.calculateAverageSalaryByCompany("UnknownCorp");
        assertFalse(unknownCorpAvg.isPresent());
    }

    @Test
    void testCalculateAverageSalaryWithNullThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> system.calculateAverageSalaryByCompany(null));
    }

    @Test
    void testCalculateAverageSalaryWithEmptyThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> system.calculateAverageSalaryByCompany(""));
    }

    @Test
    void testCalculateAverageSalaryWithBlankThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> system.calculateAverageSalaryByCompany("   "));
    }

    @Test
    void testFindLowestPaidEmployeeExists() {
        Optional<Employee> lowestPaid = system.findLowestPaidEmployee();
        assertTrue(lowestPaid.isPresent());
    }

    @Test
    void testLowestPaidEmployeeIsCorrect() {
        Optional<Employee> lowestPaid = system.findLowestPaidEmployee();
        assertEquals(emp3, lowestPaid.get());
    }

    @Test
    void testLowestPaidEmployeeSalary() {
        Optional<Employee> lowestPaid = system.findLowestPaidEmployee();
        assertEquals(3000, lowestPaid.get().getSalary(), 0.001);
    }

    @Test
    void testFindLowestPaidEmployeeInEmptySystem() {
        EmployeeService emptySystem = new EmployeeService();
        Optional<Employee> lowestPaid = emptySystem.findLowestPaidEmployee();
        assertFalse(lowestPaid.isPresent());
    }

    @Test
    void testCalculateTotalSalaryCost() {
        double expectedTotal = 15000 + 9000 + 3000 + 20000 + 8500;
        double actualTotal = system.calculateTotalSalaryCost();
        assertEquals(expectedTotal, actualTotal, 0.001);
    }

    @Test
    void testCalculateTotalSalaryCostForEmptySystem() {
        EmployeeService emptySystem = new EmployeeService();
        double total = emptySystem.calculateTotalSalaryCost();
        assertEquals(0.0, total, 0.001);
    }

    @Test
    void testCalculateTotalSalaryCostForTechCorp() {
        double expectedTechCorpTotal = 15000 + 9000 + 20000 + 8500;
        double techCorpTotal = system.calculateTotalSalaryCostByCompany("TechCorp");
        assertEquals(expectedTechCorpTotal, techCorpTotal, 0.001);
    }

    @Test
    void testCalculateTotalSalaryCostForOtherCorp() {
        double expectedOtherCorpTotal = 3000;
        double otherCorpTotal = system.calculateTotalSalaryCostByCompany("OtherCorp");
        assertEquals(expectedOtherCorpTotal, otherCorpTotal, 0.001);
    }

    @Test
    void testCalculateTotalSalaryCostForUnknownCompany() {
        double unknownCorpTotal = system.calculateTotalSalaryCostByCompany("UnknownCorp");
        assertEquals(0.0, unknownCorpTotal, 0.001);
    }

    @Test
    void testCalculateTotalSalaryCostByCompanyWithNullThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> system.calculateTotalSalaryCostByCompany(null));
    }

    @Test
    void testCalculateTotalSalaryCostByCompanyWithEmptyThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> system.calculateTotalSalaryCostByCompany(""));
    }

    @Test
    void testCalculateTotalSalaryCostByCompanyWithBlankThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> system.calculateTotalSalaryCostByCompany("   "));
    }

    @Test
    void testGroupEmployeesByCompanyHasTwoCompanies() {
        Map<String, List<Employee>> groupedByCompany = system.groupEmployeesByCompany();
        assertEquals(2, groupedByCompany.size());
    }

    @Test
    void testGroupEmployeesByCompanyContainsTechCorp() {
        Map<String, List<Employee>> groupedByCompany = system.groupEmployeesByCompany();
        assertTrue(groupedByCompany.containsKey("TechCorp"));
    }

    @Test
    void testGroupEmployeesByCompanyContainsOtherCorp() {
        Map<String, List<Employee>> groupedByCompany = system.groupEmployeesByCompany();
        assertTrue(groupedByCompany.containsKey("OtherCorp"));
    }

    @Test
    void testTechCorpHasFourEmployees() {
        Map<String, List<Employee>> groupedByCompany = system.groupEmployeesByCompany();
        assertEquals(4, groupedByCompany.get("TechCorp").size());
    }

    @Test
    void testOtherCorpHasOneEmployee() {
        Map<String, List<Employee>> groupedByCompany = system.groupEmployeesByCompany();
        assertEquals(1, groupedByCompany.get("OtherCorp").size());
    }

    @Test
    void testTechCorpContainsEmp1() {
        Map<String, List<Employee>> groupedByCompany = system.groupEmployeesByCompany();
        assertTrue(groupedByCompany.get("TechCorp").contains(emp1));
    }

    @Test
    void testTechCorpContainsEmp2() {
        Map<String, List<Employee>> groupedByCompany = system.groupEmployeesByCompany();
        assertTrue(groupedByCompany.get("TechCorp").contains(emp2));
    }

    @Test
    void testTechCorpContainsEmp4() {
        Map<String, List<Employee>> groupedByCompany = system.groupEmployeesByCompany();
        assertTrue(groupedByCompany.get("TechCorp").contains(emp4));
    }

    @Test
    void testTechCorpContainsEmp5() {
        Map<String, List<Employee>> groupedByCompany = system.groupEmployeesByCompany();
        assertTrue(groupedByCompany.get("TechCorp").contains(emp5));
    }

    @Test
    void testOtherCorpContainsEmp3() {
        Map<String, List<Employee>> groupedByCompany = system.groupEmployeesByCompany();
        assertTrue(groupedByCompany.get("OtherCorp").contains(emp3));
    }

    @Test
    void testSortEmployeesByNameFirstElement() throws InvalidDataException {
        Employee emp6 = new Employee("Adam Kowalski", "adam.kowalski@test.com", "TestCorp", Position.PROGRAMMER);
        Employee emp7 = new Employee("Zofia Kowalski", "zofia.kowalski@test.com", "TestCorp", Position.PROGRAMMER);

        system.addEmployee(emp6);
        system.addEmployee(emp7);

        List<Employee> sorted = system.sortEmployeesByName();
        assertEquals("Adam", sorted.get(0).getFirstName());
    }

    @Test
    void testSortEmployeesByNameSecondElement() throws InvalidDataException {
        Employee emp6 = new Employee("Adam Kowalski", "adam.kowalski@test.com", "TestCorp", Position.PROGRAMMER);
        Employee emp7 = new Employee("Zofia Kowalski", "zofia.kowalski@test.com", "TestCorp", Position.PROGRAMMER);

        system.addEmployee(emp6);
        system.addEmployee(emp7);

        List<Employee> sorted = system.sortEmployeesByName();
        assertEquals("Jan", sorted.get(1).getFirstName());
    }

    @Test
    void testSortEmployeesByNameThirdElement() throws InvalidDataException {
        Employee emp6 = new Employee("Adam Kowalski", "adam.kowalski@test.com", "TestCorp", Position.PROGRAMMER);
        Employee emp7 = new Employee("Zofia Kowalski", "zofia.kowalski@test.com", "TestCorp", Position.PROGRAMMER);

        system.addEmployee(emp6);
        system.addEmployee(emp7);

        List<Employee> sorted = system.sortEmployeesByName();
        assertEquals("Zofia", sorted.get(2).getFirstName());
    }

    @Test
    void testCountProgrammers() {
        Map<Position, Long> counts = system.countEmployeesByPosition();
        assertEquals(2, counts.get(Position.PROGRAMMER));
    }

    @Test
    void testCountManagers() {
        Map<Position, Long> counts = system.countEmployeesByPosition();
        assertEquals(1, counts.get(Position.MANAGER));
    }

    @Test
    void testCountInterns() {
        Map<Position, Long> counts = system.countEmployeesByPosition();
        assertEquals(1, counts.get(Position.INTERN));
    }

    @Test
    void testCountVicePresidents() {
        Map<Position, Long> counts = system.countEmployeesByPosition();
        assertEquals(1, counts.get(Position.VICE_PRESIDENT));
    }

    @Test
    void testNoPresidentInCounts() {
        Map<Position, Long> counts = system.countEmployeesByPosition();
        assertNull(counts.get(Position.PRESIDENT));
    }

    @Test
    void testEmptySystemIsEmpty() {
        EmployeeService emptySystem = new EmployeeService();
        assertTrue(emptySystem.isEmpty());
    }

    @Test
    void testEmptySystemHasZeroEmployeeCount() {
        EmployeeService emptySystem = new EmployeeService();
        assertEquals(0, emptySystem.getEmployeeCount());
    }

    @Test
    void testEmptySystemHasEmptyEmployeeList() {
        EmployeeService emptySystem = new EmployeeService();
        assertEquals(0, emptySystem.getAllEmployees().size());
    }

    @Test
    void testEmptySystemHasNoEmployeesForCompany() {
        EmployeeService emptySystem = new EmployeeService();
        assertEquals(0, emptySystem.findEmployeesByCompany("TechCorp").size());
    }

    @Test
    void testEmptySystemHasEmptySortedList() {
        EmployeeService emptySystem = new EmployeeService();
        assertEquals(0, emptySystem.sortEmployeesByName().size());
    }

    @Test
    void testEmptySystemHasEmptyGroupByPosition() {
        EmployeeService emptySystem = new EmployeeService();
        assertTrue(emptySystem.groupEmployeesByPosition().isEmpty());
    }

    @Test
    void testEmptySystemHasEmptyCountByPosition() {
        EmployeeService emptySystem = new EmployeeService();
        assertTrue(emptySystem.countEmployeesByPosition().isEmpty());
    }

    @Test
    void testEmptySystemHasNoAverageSalary() {
        EmployeeService emptySystem = new EmployeeService();
        assertFalse(emptySystem.calculateAverageSalary().isPresent());
    }

    @Test
    void testEmptySystemHasNoHighestPaidEmployee() {
        EmployeeService emptySystem = new EmployeeService();
        assertFalse(emptySystem.findHighestPaidEmployee().isPresent());
    }

    @Test
    void testEmptySystemHasNoLowestPaidEmployee() {
        EmployeeService emptySystem = new EmployeeService();
        assertFalse(emptySystem.findLowestPaidEmployee().isPresent());
    }

    @Test
    void testEmptySystemHasZeroTotalSalaryCost() {
        EmployeeService emptySystem = new EmployeeService();
        assertEquals(0.0, emptySystem.calculateTotalSalaryCost(), 0.001);
    }

    @Test
    void testEmptySystemHasEmptyGroupByCompany() {
        EmployeeService emptySystem = new EmployeeService();
        assertTrue(emptySystem.groupEmployeesByCompany().isEmpty());
    }

    // Zamiast IllegalDataException powinno być InvalidDataException
    @Test
    void testDuplicateEmailAcrossCompaniesThrowsException() throws InvalidDataException {
        Employee duplicateEmail = new Employee("Inny Jan", "jan.kowalski@techcorp.com", "DifferentCorp", Position.PROGRAMMER);
        assertThrows(InvalidDataException.class, () -> system.addEmployee(duplicateEmail));
    }

    @Test
    void testDuplicateEmailExceptionMessage() throws InvalidDataException {
        Employee duplicateEmail = new Employee("Inny Jan", "jan.kowalski@techcorp.com", "DifferentCorp", Position.PROGRAMMER);
        Exception exception = assertThrows(InvalidDataException.class, () -> system.addEmployee(duplicateEmail));
        assertTrue(exception.getMessage().contains("already exists"));
    }

    @Test
    void testEmployeeCountAfterMultipleOperations() throws InvalidDataException {
        system.removeEmployee("piotr.wisniewski@other.com");
        Employee newEmp = new Employee("Nowy Pracownik", "nowy@test.com", "TestCorp", Position.MANAGER, 12000);
        system.addEmployee(newEmp);
        assertEquals(5, system.getEmployeeCount());
    }

    @Test
    void testEmployeeExistsAfterMultipleOperations() throws InvalidDataException {
        system.removeEmployee("piotr.wisniewski@other.com");
        Employee newEmp = new Employee("Nowy Pracownik", "nowy@test.com", "TestCorp", Position.MANAGER, 12000);
        system.addEmployee(newEmp);
        assertTrue(system.employeeExists("nowy@test.com"));
    }

    @Test
    void testEmployeeNoLongerExistsAfterMultipleOperations() throws InvalidDataException {
        system.removeEmployee("piotr.wisniewski@other.com");
        Employee newEmp = new Employee("Nowy Pracownik", "nowy@test.com", "TestCorp", Position.MANAGER, 12000);
        system.addEmployee(newEmp);
        assertFalse(system.employeeExists("piotr.wisniewski@other.com"));
    }

    @Test
    void testEmployeeListSizeAfterMultipleOperations() throws InvalidDataException {
        system.removeEmployee("piotr.wisniewski@other.com");
        Employee newEmp = new Employee("Nowy Pracownik", "nowy@test.com", "TestCorp", Position.MANAGER, 12000);
        system.addEmployee(newEmp);
        assertEquals(5, system.getAllEmployees().size());
    }

    @Test
    void testCompanyCountAfterMultipleOperations() throws InvalidDataException {
        system.removeEmployee("piotr.wisniewski@other.com");
        Employee newEmp = new Employee("Nowy Pracownik", "nowy@test.com", "TestCorp", Position.MANAGER, 12000);
        system.addEmployee(newEmp);
        assertEquals(1, system.getEmployeeCountByCompany("TestCorp"));
    }

}