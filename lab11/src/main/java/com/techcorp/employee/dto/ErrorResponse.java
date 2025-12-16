//package com.techcorp.employee.dto;
//
//import java.time.LocalDateTime;
//
//public class ErrorResponse {
//    private String message;
//    private LocalDateTime timestamp;
//    private int status;
//    private String path;
//
//    // Konstruktory
//    public ErrorResponse() {
//        this.timestamp = LocalDateTime.now();
//    }
//
//    public ErrorResponse(String message, int status, String path) {
//        this();
//        this.message = message;
//        this.status = status;
//        this.path = path;
//    }
//
//    // Gettery i settery
//    public String getMessage() { return message; }
//    public void setMessage(String message) { this.message = message; }
//
//    public LocalDateTime getTimestamp() { return timestamp; }
//    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
//
//    public int getStatus() { return status; }
//    public void setStatus(int status) { this.status = status; }
//
//    public String getPath() { return path; }
//    public void setPath(String path) { this.path = path; }
//}




package com.techcorp.employee.dto;

import java.time.LocalDateTime;

public class ErrorResponse {
    private String message;
    private LocalDateTime timestamp;
    private int status;
    private String path;
    private Object details; // Może być String lub Map<String, String> dla błędów walidacji

    // Konstruktory
    public ErrorResponse() {
        this.timestamp = LocalDateTime.now();
    }

    public ErrorResponse(String message, int status, String path) {
        this();
        this.message = message;
        this.status = status;
        this.path = path;
    }

    public ErrorResponse(String message, int status, String path, Object details) {
        this(message, status, path);
        this.details = details;
    }

    // Gettery i settery
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }

    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }

    public Object getDetails() { return details; }
    public void setDetails(Object details) { this.details = details; }
}