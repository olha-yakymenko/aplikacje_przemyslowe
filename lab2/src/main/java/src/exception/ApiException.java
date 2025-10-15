package src.exception;

public class ApiException extends Exception {
    public ApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
