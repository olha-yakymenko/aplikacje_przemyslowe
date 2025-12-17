package com.techcorp.employee.dto;

import com.techcorp.employee.model.EmploymentStatus;
import com.techcorp.employee.model.Position;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class DtoTest {

    @Test
    void testEmployeeDTO() {
        // Given & When
        EmployeeDTO dto = new EmployeeDTO();
        dto.setFirstName("Jan");
        dto.setLastName("Kowalski");
        dto.setEmail("jan@example.com");
        dto.setCompany("TechCorp");
        dto.setPosition(Position.PROGRAMMER);
        dto.setSalary( new BigDecimal(8000));
        dto.setStatus(EmploymentStatus.ACTIVE);

        // Then
        assertEquals("Jan", dto.getFirstName());
        assertEquals("Kowalski", dto.getLastName());
        assertEquals("jan@example.com", dto.getEmail());
        assertEquals("TechCorp", dto.getCompany());
        assertEquals(Position.PROGRAMMER, dto.getPosition());
        assertEquals(0, BigDecimal.valueOf(8000).compareTo(dto.getSalary()));
        assertEquals(EmploymentStatus.ACTIVE, dto.getStatus());
    }

    @Test
    void testEmployeeDTOConstructor() {
        // Given & When
        EmployeeDTO dto = new EmployeeDTO(
                "Anna", "Nowak", "anna@example.com",
                "SoftInc", Position.MANAGER,  new BigDecimal(12000), EmploymentStatus.ON_LEAVE
        );

        // Then
        assertEquals("Anna", dto.getFirstName());
        assertEquals("Nowak", dto.getLastName());
        assertEquals("anna@example.com", dto.getEmail());
        assertEquals("SoftInc", dto.getCompany());
        assertEquals(Position.MANAGER, dto.getPosition());
        assertEquals(0, BigDecimal.valueOf(12000).compareTo(dto.getSalary()));
        assertEquals(EmploymentStatus.ON_LEAVE, dto.getStatus());
    }

    @Test
    void testCompanyStatisticsDTO() {
        // Given & When
        CompanyStatisticsDTO dto = new CompanyStatisticsDTO();
        dto.setCompanyName("TechCorp");
        dto.setEmployeeCount(10);
        dto.setAverageSalary(7500.0);
        dto.setHighestSalary(15000.0);
        dto.setTopEarnerName("Jan Kowalski");

        // Then
        assertEquals("TechCorp", dto.getCompanyName());
        assertEquals(10, dto.getEmployeeCount());
        assertEquals(7500.0, dto.getAverageSalary());
        assertEquals(15000.0, dto.getHighestSalary());
        assertEquals("Jan Kowalski", dto.getTopEarnerName());
    }

    @Test
    void testCompanyStatisticsDTOConstructor() {
        // Given & When
        CompanyStatisticsDTO dto = new CompanyStatisticsDTO(
                "InnovateInc", 5, 8000.0, 12000.0, "Anna Nowak"
        );

        // Then
        assertEquals("InnovateInc", dto.getCompanyName());
        assertEquals(5, dto.getEmployeeCount());
        assertEquals(8000.0, dto.getAverageSalary());
        assertEquals(12000.0, dto.getHighestSalary());
        assertEquals("Anna Nowak", dto.getTopEarnerName());
    }

    @Test
    void testErrorResponse() {
        // Given & When
        ErrorResponse error = new ErrorResponse();
        error.setMessage("Not found");
        error.setStatus(404);
        error.setPath("/api/employees/123");

        // Then
        assertEquals("Not found", error.getMessage());
        assertEquals(404, error.getStatus());
        assertEquals("/api/employees/123", error.getPath());
        assertNotNull(error.getTimestamp());
    }

    @Test
    void testErrorResponseConstructor() {
        // Given & When
        ErrorResponse error = new ErrorResponse("Bad request", 400, "/api/test");

        // Then
        assertEquals("Bad request", error.getMessage());
        assertEquals(400, error.getStatus());
        assertEquals("/api/test", error.getPath());
        assertNotNull(error.getTimestamp());
    }
}