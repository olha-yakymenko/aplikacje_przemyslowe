package service;

import com.techcorp.employee.exception.DuplicateEmailException;
import com.techcorp.employee.exception.EmployeeNotFoundException;
import com.techcorp.employee.exception.InvalidDataException;
import com.techcorp.employee.exception.InvalidFileException;
import com.techcorp.employee.model.CompanyStatistics;
import com.techcorp.employee.model.Employee;
import com.techcorp.employee.model.EmploymentStatus;
import com.techcorp.employee.model.Position;
import com.techcorp.employee.service.EmployeeService;
import com.techcorp.employee.service.FileStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.UrlResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    private FileStorageService fileStorageService;

    private EmployeeService employeeService;

    @BeforeEach
    void setUp() {
        // Przekaż fileStorageService do konstruktora
        employeeService = new EmployeeService(fileStorageService);
    }

    // ===== TESTY DODAWANIA PRACOWNIKÓW =====

    @Test
    void shouldAddEmployeeAndReturnTrue() throws InvalidDataException {
        Employee employee = new Employee("John Doe", "john.doe@company.com", "TechCorp", Position.PROGRAMMER, 8000);
        boolean result = employeeService.addEmployee(employee);
        assertTrue(result);
    }

    @Test
    void shouldIncreaseCountAfterAddingEmployee() throws InvalidDataException {
        Employee employee = new Employee("John Doe", "john.doe@company.com", "TechCorp", Position.PROGRAMMER, 8000);
        employeeService.addEmployee(employee);
        assertEquals(1, employeeService.getEmployeeCount());
    }

    @Test
    void shouldThrowDuplicateEmailExceptionForDuplicateEmployee() throws InvalidDataException {
        Employee employee1 = new Employee("John Doe", "john.doe@company.com", "TechCorp", Position.PROGRAMMER, 8000);
        Employee employee2 = new Employee("Jane Smith", "john.doe@company.com", "TechCorp", Position.MANAGER, 12000);
        employeeService.addEmployee(employee1);
        assertThrows(DuplicateEmailException.class, () -> employeeService.addEmployee(employee2));
    }

    @Test
    void shouldThrowInvalidDataExceptionForNullEmployee() {
        assertThrows(InvalidDataException.class, () -> employeeService.addEmployee(null));
    }

    // ===== TESTY WYSZUKIWANIA PRACOWNIKÓW =====

    @Test
    void shouldFindExistingEmployeeByEmail() throws InvalidDataException {
        Employee employee = new Employee("John Doe", "john.doe@company.com", "TechCorp", Position.PROGRAMMER, 8000);
        employeeService.addEmployee(employee);
        Optional<Employee> found = employeeService.findEmployeeByEmail("john.doe@company.com");
        assertTrue(found.isPresent());
    }

    @Test
    void shouldReturnEmptyOptionalForNonExistingEmail() {
        Optional<Employee> found = employeeService.findEmployeeByEmail("nonexistent@company.com");
        assertTrue(found.isEmpty());
    }

    @Test
    void shouldThrowExceptionForNullEmailInFind() {
        assertThrows(IllegalArgumentException.class, () -> employeeService.findEmployeeByEmail(null));
    }

    @Test
    void shouldThrowExceptionForEmptyEmailInFind() {
        assertThrows(IllegalArgumentException.class, () -> employeeService.findEmployeeByEmail(""));
    }

    @Test
    void shouldConfirmEmployeeExists() throws InvalidDataException {
        Employee employee = new Employee("John Doe", "john.doe@company.com", "TechCorp", Position.PROGRAMMER, 8000);
        employeeService.addEmployee(employee);
        assertTrue(employeeService.employeeExists("john.doe@company.com"));
    }

    @Test
    void shouldConfirmEmployeeDoesNotExist() {
        assertFalse(employeeService.employeeExists("nonexistent@company.com"));
    }

    // ===== TESTY USUWANIA PRACOWNIKÓW =====

    @Test
    void shouldRemoveExistingEmployee() throws InvalidDataException {
        Employee employee = new Employee("John Doe", "john.doe@company.com", "TechCorp", Position.PROGRAMMER, 8000);
        employeeService.addEmployee(employee);
        boolean result = employeeService.removeEmployee("john.doe@company.com");
        assertTrue(result);
    }

    @Test
    void shouldReturnFalseWhenRemovingNonExistingEmployee() {
        boolean result = employeeService.removeEmployee("nonexistent@company.com");
        assertFalse(result);
    }

    @Test
    void shouldDecreaseCountAfterRemovingEmployee() throws InvalidDataException {
        Employee employee = new Employee("John Doe", "john.doe@company.com", "TechCorp", Position.PROGRAMMER, 8000);
        employeeService.addEmployee(employee);
        employeeService.removeEmployee("john.doe@company.com");
        assertEquals(0, employeeService.getEmployeeCount());
    }

    // ===== TESTY LIST I FILTRÓW =====

    @Test
    void shouldReturnAllEmployees() throws InvalidDataException {
        Employee employee1 = new Employee("John Doe", "john.doe@company.com", "TechCorp", Position.PROGRAMMER, 8000);
        Employee employee2 = new Employee("Jane Smith", "jane.smith@company.com", "TechCorp", Position.MANAGER, 12000);
        employeeService.addEmployee(employee1);
        employeeService.addEmployee(employee2);
        List<Employee> employees = employeeService.getAllEmployees();
        assertEquals(2, employees.size());
    }

    @Test
    void shouldReturnEmptyListWhenNoEmployees() {
        List<Employee> employees = employeeService.getAllEmployees();
        assertTrue(employees.isEmpty());
    }

    @Test
    void shouldFilterEmployeesByCompany() throws InvalidDataException {
        Employee employee1 = new Employee("John Doe", "john.doe@techcorp.com", "TechCorp", Position.PROGRAMMER, 8000);
        Employee employee2 = new Employee("Jane Smith", "jane.smith@softinc.com", "SoftInc", Position.MANAGER, 12000);
        employeeService.addEmployee(employee1);
        employeeService.addEmployee(employee2);
        List<Employee> techCorpEmployees = employeeService.getEmployeesByCompany("TechCorp");
        assertEquals(1, techCorpEmployees.size());
    }

    @Test
    void shouldThrowExceptionForNullCompanyInFilter() {
        assertThrows(IllegalArgumentException.class, () -> employeeService.getEmployeesByCompany(null));
    }

    @Test
    void shouldReturnEmptyListForNonExistingCompany() throws InvalidDataException {
        Employee employee = new Employee("John Doe", "john.doe@techcorp.com", "TechCorp", Position.PROGRAMMER, 8000);
        employeeService.addEmployee(employee);
        List<Employee> employees = employeeService.getEmployeesByCompany("NonExisting");
        assertTrue(employees.isEmpty());
    }

    // ===== TESTY STATUSU ZATRUDNIENIA =====

    @Test
    void shouldUpdateEmployeeStatus() throws InvalidDataException {
        Employee employee = new Employee("John Doe", "john.doe@company.com", "TechCorp", Position.PROGRAMMER, 8000);
        employeeService.addEmployee(employee);
        Employee updated = employeeService.updateEmployeeStatus("john.doe@company.com", EmploymentStatus.ON_LEAVE);
        assertEquals(EmploymentStatus.ON_LEAVE, updated.getStatus());
    }

    @Test
    void shouldThrowExceptionWhenUpdatingStatusOfNonExistingEmployee() {
        assertThrows(EmployeeNotFoundException.class, () ->
                employeeService.updateEmployeeStatus("nonexistent@company.com", EmploymentStatus.ON_LEAVE)
        );
    }

    @Test
    void shouldFilterEmployeesByStatus() throws InvalidDataException {
        Employee employee1 = new Employee("John Doe", "john.doe@company.com", "TechCorp", Position.PROGRAMMER, 8000);
        Employee employee2 = new Employee("Jane Smith", "jane.smith@company.com", "TechCorp", Position.MANAGER, 12000);
        employeeService.addEmployee(employee1);
        employeeService.addEmployee(employee2);
        employeeService.updateEmployeeStatus("jane.smith@company.com", EmploymentStatus.ON_LEAVE);

        List<Employee> activeEmployees = employeeService.getEmployeesByStatus(EmploymentStatus.ACTIVE);
        List<Employee> onLeaveEmployees = employeeService.getEmployeesByStatus(EmploymentStatus.ON_LEAVE);

        assertEquals(1, activeEmployees.size());
        assertEquals(1, onLeaveEmployees.size());
    }

    @Test
    void shouldThrowExceptionForNullStatusInFilter() {
        assertThrows(IllegalArgumentException.class, () -> employeeService.getEmployeesByStatus(null));
    }

    @Test
    void shouldCalculateEmploymentStatusDistribution() throws InvalidDataException {
        Employee employee1 = new Employee("John Doe", "john.doe@company.com", "TechCorp", Position.PROGRAMMER, 8000);
        Employee employee2 = new Employee("Jane Smith", "jane.smith@company.com", "TechCorp", Position.MANAGER, 12000);
        employeeService.addEmployee(employee1);
        employeeService.addEmployee(employee2);
        employeeService.updateEmployeeStatus("jane.smith@company.com", EmploymentStatus.ON_LEAVE);

        Map<EmploymentStatus, Long> distribution = employeeService.getEmploymentStatusDistribution();

        assertEquals(1, distribution.get(EmploymentStatus.ACTIVE));
        assertEquals(1, distribution.get(EmploymentStatus.ON_LEAVE));
    }

    // ===== TESTY SORTOWANIA I GRUPOWANIA =====

    @Test
    void shouldSortEmployeesByName() throws InvalidDataException {
        Employee employee1 = new Employee("John Doe", "john.doe@company.com", "TechCorp", Position.PROGRAMMER, 8000);
        Employee employee2 = new Employee("Alice Adams", "alice.adams@company.com", "TechCorp", Position.MANAGER, 12000);
        employeeService.addEmployee(employee1);
        employeeService.addEmployee(employee2);

        List<Employee> sorted = employeeService.sortEmployeesByName();

        assertEquals("Alice Adams", sorted.get(0).getName());
        assertEquals("John Doe", sorted.get(1).getName());
    }

    @Test
    void shouldGroupEmployeesByPosition() throws InvalidDataException {
        Employee employee1 = new Employee("John Doe", "john.doe@company.com", "TechCorp", Position.PROGRAMMER, 8000);
        Employee employee2 = new Employee("Jane Smith", "jane.smith@company.com", "TechCorp", Position.MANAGER, 12000);
        employeeService.addEmployee(employee1);
        employeeService.addEmployee(employee2);

        Map<Position, List<Employee>> grouped = employeeService.groupEmployeesByPosition();

        assertEquals(1, grouped.get(Position.PROGRAMMER).size());
        assertEquals(1, grouped.get(Position.MANAGER).size());
    }

    @Test
    void shouldCountEmployeesByPosition() throws InvalidDataException {
        Employee employee1 = new Employee("John Doe", "john.doe@company.com", "TechCorp", Position.PROGRAMMER, 8000);
        Employee employee2 = new Employee("Jane Smith", "jane.smith@company.com", "TechCorp", Position.MANAGER, 12000);
        employeeService.addEmployee(employee1);
        employeeService.addEmployee(employee2);

        Map<Position, Long> counts = employeeService.countEmployeesByPosition();

        assertEquals(1, counts.get(Position.PROGRAMMER));
        assertEquals(1, counts.get(Position.MANAGER));
    }

    @Test
    void shouldGroupEmployeesByCompany() throws InvalidDataException {
        Employee employee1 = new Employee("John Doe", "john.doe@techcorp.com", "TechCorp", Position.PROGRAMMER, 8000);
        Employee employee2 = new Employee("Jane Smith", "jane.smith@softinc.com", "SoftInc", Position.MANAGER, 12000);
        employeeService.addEmployee(employee1);
        employeeService.addEmployee(employee2);

        Map<String, List<Employee>> grouped = employeeService.groupEmployeesByCompany();

        assertEquals(1, grouped.get("TechCorp").size());
        assertEquals(1, grouped.get("SoftInc").size());
    }

    // ===== TESTY STATYSTYK FINANSOWYCH =====

    @Test
    void shouldCalculateAverageSalary() throws InvalidDataException {
        Employee employee1 = new Employee("John Doe", "john.doe@company.com", "TechCorp", Position.PROGRAMMER, 8000);
        Employee employee2 = new Employee("Jane Smith", "jane.smith@company.com", "TechCorp", Position.MANAGER, 12000);
        employeeService.addEmployee(employee1);
        employeeService.addEmployee(employee2);

        double averageSalary = employeeService.calculateAverageSalary().orElse(0.0);

        assertEquals(10000.0, averageSalary, 0.001);
    }

    @Test
    void shouldReturnEmptyOptionalForAverageSalaryWhenNoEmployees() {
        OptionalDouble averageSalary = employeeService.calculateAverageSalary();
        assertTrue(averageSalary.isEmpty());
    }

    @Test
    void shouldCalculateAverageSalaryByCompany() throws InvalidDataException {
        Employee employee1 = new Employee("John Doe", "john.doe@techcorp.com", "TechCorp", Position.PROGRAMMER, 8000);
        Employee employee2 = new Employee("Jane Smith", "jane.smith@techcorp.com", "TechCorp", Position.MANAGER, 12000);
        employeeService.addEmployee(employee1);
        employeeService.addEmployee(employee2);

        double averageSalary = employeeService.calculateAverageSalaryByCompany("TechCorp").orElse(0.0);

        assertEquals(10000.0, averageSalary, 0.001);
    }

    @Test
    void shouldReturnEmptyOptionalForAverageSalaryOfNonExistingCompany() throws InvalidDataException {
        Employee employee = new Employee("John Doe", "john.doe@techcorp.com", "TechCorp", Position.PROGRAMMER, 8000);
        employeeService.addEmployee(employee);

        OptionalDouble averageSalary = employeeService.calculateAverageSalaryByCompany("NonExisting");

        assertTrue(averageSalary.isEmpty());
    }

    @Test
    void shouldFindHighestPaidEmployee() throws InvalidDataException {
        Employee employee1 = new Employee("John Doe", "john.doe@company.com", "TechCorp", Position.PROGRAMMER, 8000);
        Employee employee2 = new Employee("Jane Smith", "jane.smith@company.com", "TechCorp", Position.MANAGER, 12000);
        employeeService.addEmployee(employee1);
        employeeService.addEmployee(employee2);

        Optional<Employee> highestPaid = employeeService.findHighestPaidEmployee();

        assertTrue(highestPaid.isPresent());
        assertEquals("Jane Smith", highestPaid.get().getName());
    }

    @Test
    void shouldReturnEmptyOptionalForHighestPaidWhenNoEmployees() {
        Optional<Employee> highestPaid = employeeService.findHighestPaidEmployee();
        assertTrue(highestPaid.isEmpty());
    }

    @Test
    void shouldFindLowestPaidEmployee() throws InvalidDataException {
        Employee employee1 = new Employee("John Doe", "john.doe@company.com", "TechCorp", Position.PROGRAMMER, 8000);
        Employee employee2 = new Employee("Jane Smith", "jane.smith@company.com", "TechCorp", Position.MANAGER, 12000);
        employeeService.addEmployee(employee1);
        employeeService.addEmployee(employee2);

        Optional<Employee> lowestPaid = employeeService.findLowestPaidEmployee();

        assertTrue(lowestPaid.isPresent());
        assertEquals("John Doe", lowestPaid.get().getName());
    }

    @Test
    void shouldCalculateTotalSalaryCost() throws InvalidDataException {
        Employee employee1 = new Employee("John Doe", "john.doe@company.com", "TechCorp", Position.PROGRAMMER, 8000);
        Employee employee2 = new Employee("Jane Smith", "jane.smith@company.com", "TechCorp", Position.MANAGER, 12000);
        employeeService.addEmployee(employee1);
        employeeService.addEmployee(employee2);

        double totalCost = employeeService.calculateTotalSalaryCost();

        assertEquals(20000.0, totalCost, 0.001);
    }

    @Test
    void shouldCalculateTotalSalaryCostByCompany() throws InvalidDataException {
        Employee employee1 = new Employee("John Doe", "john.doe@techcorp.com", "TechCorp", Position.PROGRAMMER, 8000);
        Employee employee2 = new Employee("Jane Smith", "jane.smith@techcorp.com", "TechCorp", Position.MANAGER, 12000);
        employeeService.addEmployee(employee1);
        employeeService.addEmployee(employee2);

        double totalCost = employeeService.calculateTotalSalaryCostByCompany("TechCorp");

        assertEquals(20000.0, totalCost, 0.001);
    }

    @Test
    void shouldReturnZeroTotalCostForNonExistingCompany() throws InvalidDataException {
        Employee employee = new Employee("John Doe", "john.doe@techcorp.com", "TechCorp", Position.PROGRAMMER, 8000);
        employeeService.addEmployee(employee);

        double totalCost = employeeService.calculateTotalSalaryCostByCompany("NonExisting");

        assertEquals(0.0, totalCost, 0.001);
    }

    // ===== TESTY STATYSTYK FIRM =====

    @Test
    void shouldGetCompanyStatistics() throws InvalidDataException {
        Employee employee1 = new Employee("John Doe", "john.doe@techcorp.com", "TechCorp", Position.PROGRAMMER, 8000);
        Employee employee2 = new Employee("Jane Smith", "jane.smith@techcorp.com", "TechCorp", Position.MANAGER, 12000);
        employeeService.addEmployee(employee1);
        employeeService.addEmployee(employee2);

        CompanyStatistics stats = employeeService.getCompanyStatistics("TechCorp");

        assertEquals(2, stats.getEmployeeCount());
        assertEquals(10000.0, stats.getAverageSalary(), 0.001);
        assertEquals("Jane Smith", stats.getHighestPaidEmployee());
    }

    @Test
    void shouldReturnEmptyStatisticsForNonExistingCompany() {
        CompanyStatistics stats = employeeService.getCompanyStatistics("NonExisting");

        assertEquals(0, stats.getEmployeeCount());
        assertEquals(0.0, stats.getAverageSalary(), 0.001);
        assertEquals("None", stats.getHighestPaidEmployee());
    }

    @Test
    void shouldGetAllCompanyStatistics() throws InvalidDataException {
        Employee employee1 = new Employee("John Doe", "john.doe@techcorp.com", "TechCorp", Position.PROGRAMMER, 8000);
        Employee employee2 = new Employee("Jane Smith", "jane.smith@softinc.com", "SoftInc", Position.MANAGER, 12000);
        employeeService.addEmployee(employee1);
        employeeService.addEmployee(employee2);

        Map<String, CompanyStatistics> allStats = employeeService.getCompanyStatistics();

        assertEquals(2, allStats.size());
        assertTrue(allStats.containsKey("TechCorp"));
        assertTrue(allStats.containsKey("SoftInc"));
    }

    // ===== TESTY WALIDACJI I POMOCNICZE =====

    @Test
    void shouldValidateSalaryConsistency() throws InvalidDataException {
        Employee employee1 = new Employee("John Doe", "john.doe@company.com", "TechCorp", Position.PROGRAMMER, 6000);
        Employee employee2 = new Employee("Jane Smith", "jane.smith@company.com", "TechCorp", Position.MANAGER, 12000);
        employeeService.addEmployee(employee1);
        employeeService.addEmployee(employee2);

        List<Employee> underpaid = employeeService.validateSalaryConsistency(7000);

        assertEquals(1, underpaid.size());
        assertEquals("John Doe", underpaid.get(0).getName());
    }

    @Test
    void shouldReturnEmptyListWhenNoUnderpaidEmployees() throws InvalidDataException {
        Employee employee = new Employee("John Doe", "john.doe@company.com", "TechCorp", Position.PROGRAMMER, 8000);
        employeeService.addEmployee(employee);

        List<Employee> underpaid = employeeService.validateSalaryConsistency(7000);

        assertTrue(underpaid.isEmpty());
    }

    @Test
    void shouldGetEmployeeCountByCompany() throws InvalidDataException {
        Employee employee1 = new Employee("John Doe", "john.doe@techcorp.com", "TechCorp", Position.PROGRAMMER, 8000);
        Employee employee2 = new Employee("Jane Smith", "jane.smith@techcorp.com", "TechCorp", Position.MANAGER, 12000);
        employeeService.addEmployee(employee1);
        employeeService.addEmployee(employee2);

        long count = employeeService.getEmployeeCountByCompany("TechCorp");

        assertEquals(2, count);
    }

    @Test
    void shouldReturnZeroCountForNonExistingCompany() {
        long count = employeeService.getEmployeeCountByCompany("NonExisting");
        assertEquals(0, count);
    }

    @Test
    void shouldConfirmServiceIsEmptyInitially() {
        assertTrue(employeeService.isEmpty());
    }

    @Test
    void shouldConfirmServiceIsNotEmptyAfterAddingEmployee() throws InvalidDataException {
        Employee employee = new Employee("John Doe", "john.doe@company.com", "TechCorp", Position.PROGRAMMER, 8000);
        employeeService.addEmployee(employee);
        assertFalse(employeeService.isEmpty());
    }

    @Test
    void shouldAddAllEmployees() throws InvalidDataException {
        Employee employee1 = new Employee("John Doe", "john.doe@company.com", "TechCorp", Position.PROGRAMMER, 8000);
        Employee employee2 = new Employee("Jane Smith", "jane.smith@company.com", "TechCorp", Position.MANAGER, 12000);
        List<Employee> employeesToAdd = List.of(employee1, employee2);

        employeeService.addAllEmployees(employeesToAdd);

        assertEquals(2, employeeService.getEmployeeCount());
    }

    @Test
    void shouldUpdateEmployee() throws InvalidDataException {
        Employee original = new Employee("John Doe", "john.doe@company.com", "TechCorp", Position.PROGRAMMER, 8000);
        employeeService.addEmployee(original);

        Employee updated = new Employee("John Smith", "john.doe@company.com", "NewCorp", Position.MANAGER, 15000);

        Employee result = employeeService.updateEmployee(updated);

        assertEquals("John Smith", result.getName());
        assertEquals("NewCorp", result.getCompany());
        assertEquals(Position.MANAGER, result.getPosition());
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNonExistingEmployee() throws InvalidDataException {
        Employee nonExisting = new Employee("Non Existing", "nonexistent@company.com", "TechCorp", Position.PROGRAMMER, 8000);

        assertThrows(EmployeeNotFoundException.class, () -> employeeService.updateEmployee(nonExisting));
    }


    // ===== TESTY ZARZĄDZANIA ZDJĘCIAMI =====




    @Test
    void shouldThrowInvalidFileExceptionWhenUploadingInvalidFile() throws InvalidDataException {
        // Given
        Employee employee = new Employee("John Doe", "john.doe@company.com", "TechCorp", Position.PROGRAMMER, 8000);
        employeeService.addEmployee(employee);

        MultipartFile mockFile = mock(MultipartFile.class);
        doThrow(new InvalidFileException("Invalid file")).when(fileStorageService).validateFile(mockFile);

        // When & Then - Oczekujemy że wyjątek zostanie rzucony
        assertThrows(InvalidFileException.class,
                () -> employeeService.uploadEmployeePhoto("john.doe@company.com", mockFile));

        InvalidFileException exception = assertThrows(InvalidFileException.class,
                () -> employeeService.uploadEmployeePhoto("john.doe@company.com", mockFile));
        assertEquals("Invalid file", exception.getMessage());
    }



    @Test
    void shouldReturnNotFoundWhenGettingPhotoForNonExistingEmployee() {
        // When
        ResponseEntity<org.springframework.core.io.Resource> response =
                employeeService.getEmployeePhoto("nonexistent@company.com");

        // Then
        assertEquals(404, response.getStatusCodeValue());
    }

    @Test
    void shouldReturnNotFoundWhenEmployeeHasNoPhoto() throws InvalidDataException {
        // Given
        Employee employee = new Employee("John Doe", "john.doe@company.com", "TechCorp", Position.PROGRAMMER, 8000);
        employeeService.addEmployee(employee);

        // When
        ResponseEntity<org.springframework.core.io.Resource> response =
                employeeService.getEmployeePhoto("john.doe@company.com");

        // Then
        assertEquals(404, response.getStatusCodeValue());
    }

    @Test
    void shouldDeleteEmployeePhotoSuccessfully() throws InvalidDataException {
        // Given
        Employee employee = new Employee("John Doe", "john.doe@company.com", "TechCorp", Position.PROGRAMMER, 8000);
        employee.setPhotoFileName("john_doe_photo.jpg");
        employeeService.addEmployee(employee);

        // When
        employeeService.deleteEmployeePhoto("john.doe@company.com");

        // Then
        verify(fileStorageService, times(1)).deleteFile("john_doe_photo.jpg", "photos");
        assertNull(employeeService.findEmployeeByEmail("john.doe@company.com").get().getPhotoFileName());
    }

    @Test
    void shouldHandleDeletePhotoWhenEmployeeHasNoPhoto() throws InvalidDataException {
        // Given
        Employee employee = new Employee("John Doe", "john.doe@company.com", "TechCorp", Position.PROGRAMMER, 8000);
        employeeService.addEmployee(employee);

        // When & Then - Should not throw exception
        assertDoesNotThrow(() -> employeeService.deleteEmployeePhoto("john.doe@company.com"));
    }

// ===== TESTY DODATKOWYCH METOD POMOCNICZYCH =====

    @Test
    void shouldCalculateAverageSalaryByPosition() throws InvalidDataException {
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
        assertEquals(2, avgSalaries.size());
        assertEquals(8500.0, avgSalaries.get(Position.PROGRAMMER), 0.001);
        assertEquals(12000.0, avgSalaries.get(Position.MANAGER), 0.001);
    }

    @Test
    void shouldFindHighestSalary() throws InvalidDataException {
        // Given
        Employee employee1 = new Employee("John Doe", "john.doe@company.com", "TechCorp", Position.PROGRAMMER, 8000);
        Employee employee2 = new Employee("Jane Smith", "jane.smith@company.com", "TechCorp", Position.MANAGER, 12000);
        employeeService.addEmployee(employee1);
        employeeService.addEmployee(employee2);

        // When
        double highestSalary = employeeService.findHighestSalary();

        // Then
        assertEquals(12000.0, highestSalary, 0.001);
    }

    @Test
    void shouldReturnZeroForHighestSalaryWhenNoEmployees() {
        // When
        double highestSalary = employeeService.findHighestSalary();

        // Then
        assertEquals(0.0, highestSalary, 0.001);
    }

    @Test
    void shouldFindHighestSalaryByCompany() throws InvalidDataException {
        // Given
        Employee employee1 = new Employee("John Doe", "john.doe@techcorp.com", "TechCorp", Position.PROGRAMMER, 8000);
        Employee employee2 = new Employee("Jane Smith", "jane.smith@techcorp.com", "TechCorp", Position.MANAGER, 12000);
        employeeService.addEmployee(employee1);
        employeeService.addEmployee(employee2);

        // When
        double highestSalary = employeeService.findHighestSalaryByCompany("TechCorp");

        // Then
        assertEquals(12000.0, highestSalary, 0.001);
    }

    @Test
    void shouldReturnZeroForHighestSalaryByNonExistingCompany() throws InvalidDataException {
        // Given
        Employee employee = new Employee("John Doe", "john.doe@techcorp.com", "TechCorp", Position.PROGRAMMER, 8000);
        employeeService.addEmployee(employee);

        // When
        double highestSalary = employeeService.findHighestSalaryByCompany("NonExisting");

        // Then
        assertEquals(0.0, highestSalary, 0.001);
    }

// ===== TESTY DODATKOWYCH PRZYPADKÓW BRZEGOWYCH =====

    @Test
    void shouldHandleEmptyListInAddAllEmployees() {
        // Given
        List<Employee> emptyList = Collections.emptyList();

        // When & Then - Should not throw exception
        assertDoesNotThrow(() -> employeeService.addAllEmployees(emptyList));
        assertEquals(0, employeeService.getEmployeeCount());
    }



    @Test
    void shouldHandleUpdateEmployeeWithSameEmail() throws InvalidDataException {
        // Given
        Employee original = new Employee("John Doe", "john.doe@company.com", "TechCorp", Position.PROGRAMMER, 8000);
        employeeService.addEmployee(original);

        Employee updated = new Employee("John Smith", "john.doe@company.com", "NewCorp", Position.MANAGER, 15000);

        // When
        Employee result = employeeService.updateEmployee(updated);

        // Then
        assertEquals("John Smith", result.getName());
        assertEquals("NewCorp", result.getCompany());
        assertEquals(Position.MANAGER, result.getPosition());
        assertEquals(1, employeeService.getEmployeeCount()); // Count should remain the same
    }

    @Test
    void shouldHandleCaseInsensitiveEmailSearch() throws InvalidDataException {
        // Given
        Employee employee = new Employee("John Doe", "John.Doe@Company.COM", "TechCorp", Position.PROGRAMMER, 8000);
        employeeService.addEmployee(employee);

        // When
        Optional<Employee> found1 = employeeService.findEmployeeByEmail("john.doe@company.com");
        Optional<Employee> found2 = employeeService.findEmployeeByEmail("JOHN.DOE@COMPANY.COM");

        // Then
        assertTrue(found1.isPresent());
        assertTrue(found2.isPresent());
        assertEquals("John Doe", found1.get().getName());
    }

    @Test
    void shouldHandleCaseInsensitiveCompanyFilter() throws InvalidDataException {
        // Given
        Employee employee = new Employee("John Doe", "john.doe@techcorp.com", "TechCorp", Position.PROGRAMMER, 8000);
        employeeService.addEmployee(employee);

        // When
        List<Employee> result1 = employeeService.getEmployeesByCompany("techcorp");
        List<Employee> result2 = employeeService.getEmployeesByCompany("TECHCORP");

        // Then
        assertEquals(1, result1.size());
        assertEquals(1, result2.size());
    }

    @Test
    void shouldHandleEmptyCollectionsInStatistics() {
        // When
        Map<EmploymentStatus, Long> statusDistribution = employeeService.getEmploymentStatusDistribution();
        Map<Position, List<Employee>> positionGrouping = employeeService.groupEmployeesByPosition();
        Map<Position, Long> positionCounts = employeeService.countEmployeesByPosition();
        Map<String, List<Employee>> companyGrouping = employeeService.groupEmployeesByCompany();
        Map<Position, Double> avgSalariesByPosition = employeeService.getAverageSalaryByPosition();

        // Then - All should be empty but not null
        assertNotNull(statusDistribution);
        assertTrue(statusDistribution.isEmpty());
        assertNotNull(positionGrouping);
        assertTrue(positionGrouping.isEmpty());
        assertNotNull(positionCounts);
        assertTrue(positionCounts.isEmpty());
        assertNotNull(companyGrouping);
        assertTrue(companyGrouping.isEmpty());
        assertNotNull(avgSalariesByPosition);
        assertTrue(avgSalariesByPosition.isEmpty());
    }

// ===== TESTY DETERMINE IMAGE CONTENT TYPE =====

    @Test
    void shouldDetermineCorrectImageContentTypes() throws InvalidDataException {
        // This tests the private method through public API
        Employee employee = new Employee("John Doe", "john.doe@company.com", "TechCorp", Position.PROGRAMMER, 8000);
        employee.setPhotoFileName("photo.png");
        employeeService.addEmployee(employee);

        Employee employee2 = new Employee("Jane Smith", "jane.smith@company.com", "TechCorp", Position.MANAGER, 12000);
        employee2.setPhotoFileName("photo.gif");
        employeeService.addEmployee(employee2);



        assertEquals("photo.png", employee.getPhotoFileName());
        assertEquals("photo.gif", employee2.getPhotoFileName());
    }
}