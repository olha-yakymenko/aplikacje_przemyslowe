package com.techcorp.employee.exception;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ExceptionTest {

    @Test
    void testInvalidDataExceptionWithMessage() {
        // Given
        String message = "Invalid data provided";

        // When
        InvalidDataException exception = new InvalidDataException(message);

        // Then
        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void testInvalidDataExceptionWithMessageAndCause() {
        // Given
        String message = "Invalid data";
        Throwable cause = new IllegalArgumentException("Root cause");

        // When
        InvalidDataException exception = new InvalidDataException(message, cause);

        // Then
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void testDuplicateEmailExceptionWithMessage() {
        // Given
        String message = "Email already exists";

        // When
        DuplicateEmailException exception = new DuplicateEmailException(message);

        // Then
        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void testDuplicateEmailExceptionWithMessageAndCause() {
        // Given
        String message = "Duplicate email";
        Throwable cause = new RuntimeException("Database error");

        // When
        DuplicateEmailException exception = new DuplicateEmailException(message, cause);

        // Then
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void testEmployeeNotFoundExceptionWithMessage() {
        // Given
        String message = "Employee not found";

        // When
        EmployeeNotFoundException exception = new EmployeeNotFoundException(message);

        // Then
        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void testEmployeeNotFoundExceptionWithMessageAndCause() {
        // Given
        String message = "Employee not found";
        Throwable cause = new RuntimeException("Search error");

        // When
        EmployeeNotFoundException exception = new EmployeeNotFoundException(message, cause);

        // Then
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void testApiExceptionWithMessageAndCause() {
        // Given
        String message = "API error";
        Throwable cause = new RuntimeException("Network error");

        // When
        ApiException exception = new ApiException(message, cause);

        // Then
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }
}