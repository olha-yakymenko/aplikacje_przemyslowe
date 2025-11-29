package com.techcorp.employee.service;

import com.techcorp.employee.exception.FileStorageException;
import com.techcorp.employee.exception.InvalidFileException;
import com.techcorp.employee.exception.FileNotFoundException;
import com.techcorp.employee.exception.MaxUploadSizeExceededException;
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
import java.util.List;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path fileStorageLocation;
    private final Path reportsStorageLocation;
    private final Path documentsStorageLocation;
    private final Path photosStorageLocation;

    // Domyślne limity i dozwolone typy
    private static final long DEFAULT_MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final String[] ALLOWED_IMAGE_TYPES = {".jpg", ".jpeg", ".png"};
    private static final String[] ALLOWED_DOCUMENT_TYPES = {".pdf", ".doc", ".docx", ".txt"};
    private static final String[] ALLOWED_CSV_TYPES = {".csv"};
    private static final String[] ALLOWED_XML_TYPES = {".xml"};

    public FileStorageService(
            @Value("${app.upload.directory:uploads}") String uploadDir,
            @Value("${app.reports.directory:reports}") String reportsDir,
            @Value("${app.documents.directory:documents}") String documentsDir,
            @Value("${app.photos.directory:photos}") String photosDir) {

        // Inicjalizuj ścieżki - katalogi są tworzone przez FileStorageConfig
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        this.reportsStorageLocation = Paths.get(reportsDir).toAbsolutePath().normalize();
        this.documentsStorageLocation = Paths.get(documentsDir).toAbsolutePath().normalize();
        this.photosStorageLocation = Paths.get(photosDir).toAbsolutePath().normalize();

        validateStorageDirectories();
        logStoragePaths();
    }

    private void validateStorageDirectories() {
        Path[] requiredPaths = {
                fileStorageLocation,
                reportsStorageLocation,
                documentsStorageLocation,
                photosStorageLocation
        };

        for (Path path : requiredPaths) {
            if (!Files.exists(path)) {
                throw new FileStorageException("Storage directory doesn't exist: " + path);
            }
            if (!Files.isDirectory(path)) {
                throw new FileStorageException("Storage path is not a directory: " + path);
            }
            if (!Files.isWritable(path)) {
                throw new FileStorageException("Storage directory is not writable: " + path);
            }
        }
    }

    private void logStoragePaths() {
        System.out.println("FileStorageService initialized with directories:");
        System.out.println("Uploads: " + this.fileStorageLocation);
        System.out.println("Reports: " + this.reportsStorageLocation);
        System.out.println("Documents: " + this.documentsStorageLocation);
        System.out.println("Photos: " + this.photosStorageLocation);
    }

    // ========== PODSTAWOWE METODY ZAPISU ==========

    public String storeFile(MultipartFile file, String subdirectory) {
        return storeFile(file, subdirectory, false);
    }

    public String storeFile(MultipartFile file, String subdirectory, boolean useOriginalName) {
        validateFile(file);

        try {
            String fileName = generateFileName(file, useOriginalName);
            Path targetLocation = resolvePath(subdirectory).resolve(fileName);

            System.out.println("Storing file: " + fileName + " at " + targetLocation.toAbsolutePath());
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            return fileName;
        } catch (IOException ex) {
            throw new FileStorageException("Could not store file " + file.getOriginalFilename(), ex);
        }
    }

    public String storeFileWithCustomName(MultipartFile file, String subdirectory, String customFileName) {
        validateFile(file);

        try {
            String fileName = normalizeFileName(customFileName);
            Path targetLocation = resolvePath(subdirectory).resolve(fileName);

            System.out.println("Storing file with custom name: " + fileName);
            System.out.println("Target location: " + targetLocation.toAbsolutePath());

            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("File stored successfully: " + fileName);

            return fileName;
        } catch (IOException ex) {
            throw new FileStorageException("Could not store file " + customFileName, ex);
        }
    }

    // ========== SPECJALIZOWANE METODY DLA RÓŻNYCH TYPÓW PLIKÓW ==========

    public String storeDocument(MultipartFile file, String employeeEmail) {
        validateFileTypeAndSize(file, ALLOWED_DOCUMENT_TYPES, DEFAULT_MAX_FILE_SIZE);
        String subdirectory = "documents/" + normalizeFileName(employeeEmail);
        return storeFile(file, subdirectory, false);
    }

    public String storePhoto(MultipartFile file, String employeeEmail) {
        validateImageFile(file);
        String fileName = normalizeFileName(employeeEmail) + getFileExtension(file.getOriginalFilename());
        String subdirectory = "photos";
        return storeFileWithCustomName(file, subdirectory, fileName);
    }

    public String storeReport(MultipartFile file, String reportType) {
        validateFileTypeAndSize(file, ALLOWED_DOCUMENT_TYPES, DEFAULT_MAX_FILE_SIZE);
        String subdirectory = "reports/" + reportType;
        return storeFile(file, subdirectory, false);
    }

    // ========== METODY ODCZYTU ==========

    public Resource loadFileAsResource(String fileName, String subdirectory) {
        try {
            Path filePath = resolvePath(subdirectory).resolve(fileName).normalize();
            validatePathSecurity(filePath);

            System.out.println("Loading file: " + filePath.toAbsolutePath());
            System.out.println("File exists: " + Files.exists(filePath));

            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                System.out.println("Resource loaded successfully");
                return resource;
            } else {
                System.err.println("File not found or not readable: " + filePath.toAbsolutePath());
                throw new FileNotFoundException("File not found: " + fileName);
            }
        } catch (MalformedURLException ex) {
            throw new FileNotFoundException("File not found: " + fileName, ex);
        }
    }

    public Resource loadCsvFile(String fileName) {
        return loadFileAsResource(fileName, "csv-imports");
    }

    public Resource loadXmlFile(String fileName) {
        return loadFileAsResource(fileName, "xml-imports");
    }

    public Resource loadDocument(String fileName, String employeeEmail) {
        String subdirectory = "documents/" + normalizeFileName(employeeEmail);
        return loadFileAsResource(fileName, subdirectory);
    }

    public Resource loadPhoto(String employeeEmail) {
        String subdirectory = "photos";

        Path photosDir = resolvePath(subdirectory);
        if (!Files.exists(photosDir) || !Files.isDirectory(photosDir)) {
            throw new FileNotFoundException("Photos directory not found");
        }

        String baseFileName = normalizeFileName(employeeEmail);

        try {
            return Files.list(photosDir)
                    .filter(path -> {
                        String filename = path.getFileName().toString();
                        return filename.startsWith(baseFileName + ".") ||
                                filename.equals(baseFileName);
                    })
                    .findFirst()
                    .map(path -> {
                        try {
                            Resource resource = new UrlResource(path.toUri());
                            if (resource.exists() && resource.isReadable()) {
                                return resource;
                            }
                            throw new FileNotFoundException("Photo exists but is not readable for: " + employeeEmail);
                        } catch (MalformedURLException e) {
                            throw new FileStorageException("Could not access photo file", e);
                        }
                    })
                    .orElseThrow(() -> new FileNotFoundException("Photo not found for: " + employeeEmail));
        } catch (IOException ex) {
            throw new FileStorageException("Could not search for photo", ex);
        }
    }

    public Resource loadReport(String fileName, String reportType) {
        String subdirectory = "reports/" + reportType;
        return loadFileAsResource(fileName, subdirectory);
    }

    // ========== METODY USUWANIA ==========

    public boolean deleteFile(String fileName, String subdirectory) {
        try {
            Path filePath = resolvePath(subdirectory).resolve(fileName).normalize();
            validatePathSecurity(filePath);

            System.out.println("Deleting file: " + filePath.toAbsolutePath());
            boolean deleted = Files.deleteIfExists(filePath);
            System.out.println("File deleted: " + deleted);

            return deleted;
        } catch (IOException ex) {
            throw new FileStorageException("Could not delete file: " + fileName, ex);
        }
    }

    public boolean deleteDocument(String fileName, String employeeEmail) {
        String subdirectory = "documents/" + normalizeFileName(employeeEmail);
        return deleteFile(fileName, subdirectory);
    }

    public boolean deletePhoto(String employeeEmail) {
        String subdirectory = "photos";
        String baseFileName = normalizeFileName(employeeEmail);

        try {
            Path photosDir = resolvePath(subdirectory);
            if (Files.exists(photosDir) && Files.isDirectory(photosDir)) {
                return Files.list(photosDir)
                        .filter(path -> {
                            String filename = path.getFileName().toString();
                            return filename.startsWith(baseFileName + ".") ||
                                    filename.equals(baseFileName);
                        })
                        .findFirst()
                        .map(path -> {
                            try {
                                return Files.deleteIfExists(path);
                            } catch (IOException e) {
                                throw new FileStorageException("Could not delete photo", e);
                            }
                        })
                        .orElse(false);
            }
            return false;
        } catch (IOException ex) {
            throw new FileStorageException("Could not search for photo to delete", ex);
        }
    }

    // ========== WALIDACJE ==========

    public void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidFileException("File is empty or null");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.contains("..")) {
            throw new InvalidFileException("Invalid file name: " + originalFilename);
        }

        if (file.getSize() == 0) {
            throw new InvalidFileException("File is empty");
        }
    }

    public void validateFileType(MultipartFile file, String[] allowedExtensions) {
        validateFile(file);

        String fileName = file.getOriginalFilename();
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
        validateFile(file);

        if (file.getSize() > maxSizeBytes) {
            throw new MaxUploadSizeExceededException("File size exceeds maximum allowed size: " +
                    maxSizeBytes + " bytes (file: " + file.getSize() + " bytes)");
        }
    }

    /**
     * KOMPLETNA METODA WALIDACJI - spełnia wymaganie z zadania
     */
    public void validateFileTypeAndSize(MultipartFile file, String[] allowedExtensions, long maxSizeBytes) {
        validateFileType(file, allowedExtensions);
        validateFileSize(file, maxSizeBytes);
    }

    public void validateImageFile(MultipartFile file) {
        validateFileType(file, ALLOWED_IMAGE_TYPES);
        validateFileSize(file, 2 * 1024 * 1024); // 2MB dla zdjęć

        // Walidacja MIME type
        String contentType = file.getContentType();
        if (contentType == null ||
                (!contentType.equals("image/jpeg") &&
                        !contentType.equals("image/jpg") &&
                        !contentType.equals("image/png"))) {
            throw new InvalidFileException("File must be a JPEG or PNG image. Detected: " + contentType);
        }
    }

    // ========== METODY POMOCNICZE ==========

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

    private void validatePathSecurity(Path filePath) {
        Path normalizedPath = filePath.normalize();
        if (!normalizedPath.startsWith(fileStorageLocation)) {
            // Używamy InvalidFileException zamiast SecurityException dla lepszej obsługi
            throw new InvalidFileException("Path traversal attempt detected: " + filePath);
        }
    }

    private String generateFileName(MultipartFile file, boolean useOriginalName) {
        if (useOriginalName) {
            return normalizeFileName(file.getOriginalFilename());
        } else {
            String fileExtension = getFileExtension(file.getOriginalFilename());
            return UUID.randomUUID().toString() + fileExtension;
        }
    }

    private String normalizeFileName(String fileName) {
        if (fileName == null) return "unknown_file";

        return fileName.replaceAll("[^a-zA-Z0-9._-]", "_")
                .replace(" ", "_")
                .replace("..", "_");
    }

    private String getFileExtension(String fileName) {
        if (fileName == null) return "";
        int lastDotIndex = fileName.lastIndexOf(".");
        return lastDotIndex > 0 ? fileName.substring(lastDotIndex).toLowerCase() : "";
    }

    // ========== METODY UTILITY ==========

    public boolean fileExists(String fileName, String subdirectory) {
        Path filePath = resolvePath(subdirectory).resolve(fileName).normalize();
        validatePathSecurity(filePath);
        return Files.exists(filePath) && Files.isRegularFile(filePath);
    }

    public long getFileSize(String fileName, String subdirectory) {
        try {
            Path filePath = resolvePath(subdirectory).resolve(fileName).normalize();
            validatePathSecurity(filePath);
            return Files.size(filePath);
        } catch (IOException ex) {
            throw new FileNotFoundException("Could not get file size for: " + fileName, ex);
        }
    }

    public String storeDepartmentDocument(MultipartFile file, Long departmentId) {
        validateFileTypeAndSize(file, ALLOWED_DOCUMENT_TYPES, DEFAULT_MAX_FILE_SIZE);
        String subdirectory = "department-documents/" + departmentId;

        String uniqueFileName = generateUniqueDepartmentFileName(file.getOriginalFilename());
        return storeFileWithCustomName(file, subdirectory, uniqueFileName);
    }

    private String generateUniqueDepartmentFileName(String originalFileName) {
        if (originalFileName == null || originalFileName.isEmpty()) {
            return UUID.randomUUID().toString();
        }

        String fileExtension = getFileExtension(originalFileName);
        String baseName = originalFileName.substring(0, originalFileName.lastIndexOf('.'));

        String shortUuid = UUID.randomUUID().toString().substring(0, 6);

        return normalizeFileName(baseName)  + "_" + shortUuid + fileExtension;
    }

    public Resource loadDepartmentDocument(String fileName, Long departmentId) {
        String subdirectory = "department-documents/" + departmentId;
        return loadFileAsResource(fileName, subdirectory);
    }

    public boolean deleteDepartmentDocument(String fileName, Long departmentId) {
        String subdirectory = "department-documents/" + departmentId;
        return deleteFile(fileName, subdirectory);
    }

    public List<String> getDepartmentDocumentNames(Long departmentId) {
        String subdirectory = "department-documents/" + departmentId;
        Path departmentDir = resolvePath(subdirectory);

        try {
            if (!Files.exists(departmentDir)) {
                return List.of();
            }

            return Files.list(departmentDir)
                    .filter(Files::isRegularFile)
                    .map(path -> path.getFileName().toString())
                    .toList();
        } catch (IOException ex) {
            throw new FileStorageException("Could not list department documents", ex);
        }
    }

    // ========== GETTERY ==========

    public Path getFileStorageLocation() {
        return fileStorageLocation;
    }

    public Path getReportsStorageLocation() {
        return reportsStorageLocation;
    }

    public Path getDocumentsStorageLocation() {
        return documentsStorageLocation;
    }

    public Path getPhotosStorageLocation() {
        return photosStorageLocation;
    }
}