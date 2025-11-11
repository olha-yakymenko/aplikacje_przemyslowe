package com.techcorp.employee.service;

import com.techcorp.employee.model.EmployeeDocument;
import com.techcorp.employee.model.DocumentType;
import com.techcorp.employee.exception.EmployeeNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class DocumentStorageService {

    private final Map<String, EmployeeDocument> documents = new ConcurrentHashMap<>();
    private final FileStorageService fileStorageService;
    private final EmployeeService employeeService;

    @Autowired
    public DocumentStorageService(FileStorageService fileStorageService, EmployeeService employeeService) {
        this.fileStorageService = fileStorageService;
        this.employeeService = employeeService;
    }

    public EmployeeDocument storeDocument(String employeeEmail, MultipartFile file, DocumentType documentType) {
        // Walidacja czy pracownik istnieje
        if (!employeeService.employeeExists(employeeEmail)) {
            throw new EmployeeNotFoundException("Employee not found with email: " + employeeEmail);
        }

        // Walidacja pliku
        fileStorageService.validateFile(file);

        // Zapisz plik
        String fileName = fileStorageService.storeFile(file, "documents/" + employeeEmail);

        // Utwórz metadane dokumentu
        EmployeeDocument document = new EmployeeDocument();
        document.setId(UUID.randomUUID().toString());
        document.setEmployeeEmail(employeeEmail);
        document.setFileName(fileName);
        document.setOriginalFileName(file.getOriginalFilename());
        document.setFileType(documentType);
        document.setFilePath("documents/" + employeeEmail + "/" + fileName);
        document.setFileSize(file.getSize());

        // Zapisz metadane
        documents.put(document.getId(), document);

        return document;
    }

    public List<EmployeeDocument> getEmployeeDocuments(String employeeEmail) {
        return documents.values().stream()
                .filter(doc -> doc.getEmployeeEmail().equalsIgnoreCase(employeeEmail))
                .sorted((d1, d2) -> d2.getUploadDate().compareTo(d1.getUploadDate()))
                .collect(Collectors.toList());
    }

    public EmployeeDocument getDocument(String documentId) {
        EmployeeDocument document = documents.get(documentId);
        if (document == null) {
            throw new com.techcorp.employee.exception.FileNotFoundException("Document not found with ID: " + documentId);
        }
        return document;
    }

    public void deleteDocument(String documentId) {
        EmployeeDocument document = documents.remove(documentId);
        if (document != null) {
            fileStorageService.deleteFile(document.getFileName(), "documents/" + document.getEmployeeEmail());
        }
    }

    public boolean documentExists(String documentId) {
        return documents.containsKey(documentId);
    }
}