
package com.flowtech.eventhub_spring_backend.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


@Service
public class EventImageStorageService {

    private final Path uploadPath;

    public EventImageStorageService(
            @Value("${eventhub.upload-dir}") String uploadDir) {

        this.uploadPath = Paths.get(uploadDir)
                .toAbsolutePath()
                .normalize();

        try {
            Files.createDirectories(uploadPath);
        } catch (IOException e) {
            throw new RuntimeException(
                    "Could not create upload directory", e);
        }
    }

    public String storeImage(MultipartFile image) throws IOException {

        if (image == null || image.isEmpty()) {
            throw new IllegalArgumentException("Please select an image");
        }

        String extension = org.springframework.util.StringUtils.getFilenameExtension(
                image.getOriginalFilename());

        if (extension == null ||
                !extension.matches("(?i)jpg|jpeg|png|webp")) {
            throw new IllegalArgumentException(
                    "Only JPG, JPEG, PNG, and WEBP images are allowed");
        }

        String filename = UUID.randomUUID() + "."
                + extension.toLowerCase();

        Path destination = uploadPath.resolve(filename).normalize();

        if (!destination.startsWith(uploadPath)) {
            throw new IllegalArgumentException("Invalid filename");
        }

        Files.copy(image.getInputStream(), destination);

        return "/uploads/" + filename;
    }
}