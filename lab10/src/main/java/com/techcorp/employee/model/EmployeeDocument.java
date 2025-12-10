package com.techcorp.employee.model;

import java.time.LocalDateTime;
import java.util.Objects;

public class EmployeeDocument {
    private String id;
    private String employeeEmail;
    private String fileName;
    private String originalFileName;
    private DocumentType fileType;
    private LocalDateTime uploadDate;
    private String filePath;
    private long fileSize;

    public EmployeeDocument() {
        this.uploadDate = LocalDateTime.now();
    }

    public EmployeeDocument(String employeeEmail, String fileName, String originalFileName,
                            DocumentType fileType, String filePath, long fileSize) {
        this();
        this.employeeEmail = employeeEmail;
        this.fileName = fileName;
        this.originalFileName = originalFileName;
        this.fileType = fileType;
        this.filePath = filePath;
        this.fileSize = fileSize;
    }

    // Gettery i settery
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getEmployeeEmail() { return employeeEmail; }
    public void setEmployeeEmail(String employeeEmail) { this.employeeEmail = employeeEmail; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getOriginalFileName() { return originalFileName; }
    public void setOriginalFileName(String originalFileName) { this.originalFileName = originalFileName; }

    public DocumentType getFileType() { return fileType; }
    public void setFileType(DocumentType fileType) { this.fileType = fileType; }

    public LocalDateTime getUploadDate() { return uploadDate; }
    public void setUploadDate(LocalDateTime uploadDate) { this.uploadDate = uploadDate; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public long getFileSize() { return fileSize; }
    public void setFileSize(long fileSize) { this.fileSize = fileSize; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EmployeeDocument that = (EmployeeDocument) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("EmployeeDocument{id='%s', employeeEmail='%s', fileName='%s', type=%s}",
                id, employeeEmail, fileName, fileType);
    }
}