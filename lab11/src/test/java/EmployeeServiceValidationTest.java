package com.techcorp.employee.service;

import com.techcorp.employee.model.*;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
class EmployeeServiceValidationTest {

    @Autowired
    private EmployeeService employeeService;

    @Test
    void testCreateEmployee_WithInvalidDataFromCsvImport_ThrowsConstraintViolationException() {
        Employee invalidEmployeeFromCsv = new Employee(
                "J",
                "jan@gmail.com",
                "",
                null,
                -5000.0,
                null
        );

        // Act & Assert
        assertThatThrownBy(() -> {
            employeeService.createEmployee(invalidEmployeeFromCsv);
        })
                .isInstanceOf(ConstraintViolationException.class)
                .satisfies(exception -> {
                    ConstraintViolationException cvException = (ConstraintViolationException) exception;
                    String errorMessage = cvException.getMessage();

                    assertThat(errorMessage)
                            .contains("name")
                            .contains("email")
                            .contains("company")
                            .contains("salary");
                });
    }
}