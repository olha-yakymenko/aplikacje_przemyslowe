package com.techcorp.employee.exception;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.techcorp.employee.dto.ErrorResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EmployeeNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEmployeeNotFound(EmployeeNotFoundException ex, WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
                ex.getMessage(),
                HttpStatus.NOT_FOUND.value(),
                request.getDescription(false)
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateEmail(DuplicateEmailException ex, WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
                ex.getMessage(),
                HttpStatus.CONFLICT.value(),
                request.getDescription(false)
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(InvalidDataException.class)
    public ResponseEntity<ErrorResponse> handleInvalidData(InvalidDataException ex, WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
                ex.getMessage(),
                HttpStatus.BAD_REQUEST.value(),
                request.getDescription(false)
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex, WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
                ex.getMessage(),
                HttpStatus.BAD_REQUEST.value(),
                request.getDescription(false)
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex, WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
                "An unexpected error occurred: " + ex.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                request.getDescription(false)
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }


    @ExceptionHandler(FileStorageException.class)
    public ResponseEntity<ErrorResponse> handleFileStorage(FileStorageException ex, WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
                ex.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                request.getDescription(false)
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(InvalidFileException.class)
    public ResponseEntity<ErrorResponse> handleInvalidFile(InvalidFileException ex, WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
                ex.getMessage(),
                HttpStatus.BAD_REQUEST.value(),
                request.getDescription(false)
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(FileNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleFileNotFound(FileNotFoundException ex, WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
                ex.getMessage(),
                HttpStatus.NOT_FOUND.value(),
                request.getDescription(false)
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxSizeException(MaxUploadSizeExceededException ex, WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
                "File size exceeds maximum allowed limit of 10MB",
                HttpStatus.PAYLOAD_TOO_LARGE.value(),
                request.getDescription(false)
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.PAYLOAD_TOO_LARGE);
    }


    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ErrorResponse> handleDataAccessException(DataAccessException ex, WebRequest request) {

        String userMessage = "Database operation failed. Please try again later.";

        ErrorResponse errorResponse = new ErrorResponse(
                userMessage,
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                request.getDescription(false)
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }



//
//    @Override
//    protected ResponseEntity<Object> handleMethodArgumentNotValid(
//            MethodArgumentNotValidException ex,
//            org.springframework.http.HttpHeaders headers,
//            org.springframework.http.HttpStatusCode status,
//            WebRequest request) {
//
//        Map<String, Object> responseBody = new HashMap<>();
//        Map<String, String> errors = new HashMap<>();
//
//        // Zbieranie błędów dla każdego pola
//        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
//            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
//        }
//
//        responseBody.put("timestamp", LocalDateTime.now());
//        responseBody.put("status", HttpStatus.BAD_REQUEST.value());
//        responseBody.put("error", "Validation Failed");
//        responseBody.put("message", "One or more validation errors occurred");
//        responseBody.put("errors", errors);
//
//        return new ResponseEntity<>(responseBody, headers, HttpStatus.BAD_REQUEST);
//    }
//
//

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {

        Map<String, String> errors = new LinkedHashMap<>();

        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );

        return ResponseEntity.badRequest().body(errors);
    }


    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Object> handleConstraintViolation(
            ConstraintViolationException ex, WebRequest request) {

        Map<String, Object> responseBody = new HashMap<>();

        Map<String, List<String>> errors = ex.getConstraintViolations()
                .stream()
                .collect(Collectors.groupingBy(
                        v -> extractFieldName(v.getPropertyPath().toString()),
                        LinkedHashMap::new,
                        Collectors.mapping(ConstraintViolation::getMessage, Collectors.toList())
                ));


        responseBody.put("timestamp", LocalDateTime.now());
        responseBody.put("status", HttpStatus.BAD_REQUEST.value());
        responseBody.put("error", "Validation Failed");
        responseBody.put("message", "Validation errors occurred");
        responseBody.put("errors", errors);

        return new ResponseEntity<>(responseBody, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidSalaryException.class)
    public ResponseEntity<ErrorResponse> handleInvalidSalary(
            InvalidSalaryException ex, WebRequest request) {

        ErrorResponse errorResponse = new ErrorResponse(
                ex.getMessage(), // "Salary cannot be negative"
                HttpStatus.BAD_REQUEST.value(),
                request.getDescription(false)
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }



//
//    @ExceptionHandler(ConstraintViolationException.class)
//    public ResponseEntity<Object> handleConstraintViolation(
//            ConstraintViolationException ex, WebRequest request) {
//
//        Map<String, Object> responseBody = new HashMap<>();
//        Map<String, String> errors = new LinkedHashMap<>();
//
//        for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
//            String fullPath = violation.getPropertyPath().toString();
//            String fieldName = extractFieldName(fullPath);
//            errors.put(fieldName, violation.getMessage());
//        }
//
//        responseBody.put("timestamp", LocalDateTime.now());
//        responseBody.put("status", HttpStatus.BAD_REQUEST.value());
//        responseBody.put("error", "Validation Failed");
//        responseBody.put("message", "Validation errors occurred");
//        responseBody.put("errors", errors);
//
//        return new ResponseEntity<>(responseBody, HttpStatus.BAD_REQUEST);
//    }

    private String extractFieldName(String propertyPath) {
        if (propertyPath == null || propertyPath.isEmpty()) {
            return "unknown";
        }

        // Usuń prefixy metod (np. "createEmployee.arg0." lub "addEmployee.employee.")
        String[] parts = propertyPath.split("\\.");

        // Weź ostatnią część (nazwę pola)
        return parts[parts.length - 1];
    }





//    @ExceptionHandler(HttpMessageNotReadableException.class)
//    public ResponseEntity<Object> handleHttpMessageNotReadable(
//            HttpMessageNotReadableException ex, WebRequest request) {
//
//        Map<String, Object> responseBody = new HashMap<>();
//        String errorMessage = "Invalid JSON format";
//
//        if (ex.getCause() instanceof InvalidFormatException) {
//            InvalidFormatException ife = (InvalidFormatException) ex.getCause();
//            if (ife.getTargetType() != null && ife.getTargetType().isEnum()) {
//                Object[] enumConstants = ife.getTargetType().getEnumConstants();
//                errorMessage = String.format("Invalid value '%s' for %s. Allowed values: %s",
//                        ife.getValue(),
//                        ife.getTargetType().getSimpleName(),
//                        Arrays.toString(enumConstants));
//            }
//        }
//
//        responseBody.put("timestamp", LocalDateTime.now());
//        responseBody.put("status", HttpStatus.BAD_REQUEST.value());
//        responseBody.put("error", "JSON Parse Error");
//        responseBody.put("message", errorMessage);
//        responseBody.put("errors", Collections.emptyMap());
//
//        return new ResponseEntity<>(responseBody, HttpStatus.BAD_REQUEST);
//    }
}