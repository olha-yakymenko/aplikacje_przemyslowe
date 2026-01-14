package com.techcorp.employee.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class FileStorageConfig implements CommandLineRunner {

    @Value("${app.upload.directory}")
    private String uploadDir;

    @Value("${app.reports.directory}")
    private String reportsDir;

    @Value("${app.documents.directory}")
    private String documentsDir;

    @Value("${app.photos.directory}")
    private String photosDir;

    @Override
    public void run(String... args) throws Exception {
        createDirectories();
    }

    private void createDirectories() throws IOException {
        Path uploadPath = Paths.get(uploadDir);
        Path reportsPath = Paths.get(reportsDir);
        Path documentsPath = Paths.get(documentsDir);
        Path photosPath = Paths.get(photosDir);

        Files.createDirectories(uploadPath);
        Files.createDirectories(reportsPath);
        Files.createDirectories(documentsPath);
        Files.createDirectories(photosPath);

        System.out.println("Created directories for file storage");
    }
}