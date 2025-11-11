package com.techcorp.employee.service;

import com.techcorp.employee.exception.FileStorageException;
import com.techcorp.employee.exception.InvalidFileException;
import com.techcorp.employee.exception.FileNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path fileStorageLocation;
    private final Path reportsStorageLocation;
    private final Path documentsStorageLocation;
    private final Path photosStorageLocation;

    public FileStorageService(
            @Value("${app.upload.directory}") String uploadDir,
            @Value("${app.reports.directory}") String reportsDir,
            @Value("${app.documents.directory}") String documentsDir,
            @Value("${app.photos.directory}") String photosDir) {

        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        this.reportsStorageLocation = Paths.get(reportsDir).toAbsolutePath().normalize();
        this.documentsStorageLocation = Paths.get(documentsDir).toAbsolutePath().normalize();
        this.photosStorageLocation = Paths.get(photosDir).toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.fileStorageLocation);
            Files.createDirectories(this.reportsStorageLocation);
            Files.createDirectories(this.documentsStorageLocation);
            Files.createDirectories(this.photosStorageLocation);
        } catch (Exception ex) {
            throw new FileStorageException("Could not create the directories", ex);
        }
    }

    public String storeFile(MultipartFile file, String subdirectory) {
        return storeFile(file, subdirectory, false);
    }

    public String storeFile(MultipartFile file, String subdirectory, boolean useOriginalName) {
        validateFile(file);

        try {
            String fileName;
            if (useOriginalName) {
                fileName = normalizeFileName(file.getOriginalFilename());
            } else {
                String fileExtension = getFileExtension(file.getOriginalFilename());
                fileName = UUID.randomUUID().toString() + fileExtension;
            }

            Path targetLocation = resolvePath(subdirectory).resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            return fileName;
        } catch (IOException ex) {
            throw new FileStorageException("Could not store file " + file.getOriginalFilename(), ex);
        }
    }

    public Resource loadFileAsResource(String fileName, String subdirectory) {
        try {
            Path filePath = resolvePath(subdirectory).resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists()) {
                return resource;
            } else {
                throw new FileNotFoundException("File not found: " + fileName);
            }
        } catch (MalformedURLException ex) {
            throw new FileNotFoundException("File not found: " + fileName, ex);
        }
    }

    public boolean deleteFile(String fileName, String subdirectory) {
        try {
            Path filePath = resolvePath(subdirectory).resolve(fileName).normalize();
            return Files.deleteIfExists(filePath);
        } catch (IOException ex) {
            throw new FileStorageException("Could not delete file: " + fileName, ex);
        }
    }

    public void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidFileException("File is empty or null");
        }

        if (file.getOriginalFilename() == null || file.getOriginalFilename().contains("..")) {
            throw new InvalidFileException("Invalid file name: " + file.getOriginalFilename());
        }
    }

    public void validateFileType(MultipartFile file, String[] allowedExtensions) {
        String fileName = file.getOriginalFilename();
        if (fileName == null) {
            throw new InvalidFileException("File name is null");
        }

        String fileExtension = getFileExtension(fileName).toLowerCase();
        boolean isValid = false;

        for (String extension : allowedExtensions) {
            if (fileExtension.equals(extension.toLowerCase())) {
                isValid = true;
                break;
            }
        }

        if (!isValid) {
            throw new InvalidFileException("File type not allowed. Allowed types: " +
                    String.join(", ", allowedExtensions));
        }
    }

    public void validateFileSize(MultipartFile file, long maxSizeBytes) {
        if (file.getSize() > maxSizeBytes) {
            throw new InvalidFileException("File size exceeds maximum allowed size: " +
                    maxSizeBytes + " bytes");
        }
    }

    public void validateImageFile(MultipartFile file) {
        validateFileType(file, new String[]{".jpg", ".jpeg", ".png"});

        // Basic MIME type validation
        String contentType = file.getContentType();
        if (contentType == null ||
                (!contentType.equals("image/jpeg") && !contentType.equals("image/png"))) {
            throw new InvalidFileException("File must be a JPEG or PNG image");
        }
    }

    private Path resolvePath(String subdirectory) {
        if (subdirectory == null || subdirectory.trim().isEmpty()) {
            return fileStorageLocation;
        }

        Path path = fileStorageLocation.resolve(subdirectory).normalize();
        try {
            Files.createDirectories(path);
        } catch (IOException ex) {
            throw new FileStorageException("Could not create subdirectory: " + subdirectory, ex);
        }
        return path;
    }

    private String normalizeFileName(String fileName) {
        if (fileName == null) return null;
        return fileName.replace(" ", "_").replaceAll("[^a-zA-Z0-9._-]", "");
    }

    private String getFileExtension(String fileName) {
        if (fileName == null) return "";
        int lastDotIndex = fileName.lastIndexOf(".");
        return lastDotIndex > 0 ? fileName.substring(lastDotIndex) : "";
    }

    // Gettery dla ścieżek
    public Path getFileStorageLocation() { return fileStorageLocation; }
    public Path getReportsStorageLocation() { return reportsStorageLocation; }
    public Path getDocumentsStorageLocation() { return documentsStorageLocation; }
    public Path getPhotosStorageLocation() { return photosStorageLocation; }
}