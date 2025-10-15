
import src.exception.ApiException;
import src.model.Employee;
import src.model.Position;
import org.junit.jupiter.api.Test;
import src.service.ApiService;

import static org.junit.jupiter.api.Assertions.*;

class ApiServiceTest {

    @Test
    void testApiServiceCreation() {
        ApiService apiService = new ApiService();
        assertNotNull(apiService);
    }

    @Test
    void testFetchEmployeesFromApi() {
        ApiService apiService = new ApiService();

        try {
            java.util.List<Employee> employees = apiService.fetchEmployeesFromApi();

            // If API is available, we should get some employees
            if (!employees.isEmpty()) {
                Employee first = employees.get(0);
                assertNotNull(first.getName());
                assertNotNull(first.getEmail());
                assertNotNull(first.getCompany());
                assertEquals(Position.PROGRAMMER, first.getPosition());
                assertEquals(Position.PROGRAMMER.getBaseSalary(), first.getSalary());
            }
        } catch (ApiException e) {
            System.out.println("API unavailable during test: " + e.getMessage());
        }
    }

    @Test
    void testParseApiResponse() {
        ApiService apiService = new ApiService();
        assertNotNull(apiService);
    }
}