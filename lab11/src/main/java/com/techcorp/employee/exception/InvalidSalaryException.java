// InvalidSalaryException.java
package com.techcorp.employee.exception;

public class InvalidSalaryException extends Exception {

    public InvalidSalaryException(String message) {
        super(message);
    }

    public InvalidSalaryException(String message, Throwable cause) {
        super(message, cause);
    }
}