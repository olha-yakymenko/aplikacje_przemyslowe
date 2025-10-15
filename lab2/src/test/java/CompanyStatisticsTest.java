
import org.junit.jupiter.api.Test;
import src.model.CompanyStatistics;

import static org.junit.jupiter.api.Assertions.*;

class CompanyStatisticsTest {

    @Test
    void testCompanyStatisticsCreation() {
        CompanyStatistics stats = new CompanyStatistics(10, 5000.0, "John Doe");

        assertEquals(10, stats.getEmployeeCount());
        assertEquals(5000.0, stats.getAverageSalary(), 0.001);
        assertEquals("John Doe", stats.getHighestPaidEmployee());
    }

    @Test
    void testToString() {
        CompanyStatistics stats = new CompanyStatistics(5, 7500.50, "Jane Smith");

        String result = stats.toString();
        System.out.println(result);
        assertTrue(result.contains("Employees: 5"));
        assertTrue(result.contains("Avg Salary: 7500,50"));
        assertTrue(result.contains("Highest Paid: Jane Smith"));
    }
}