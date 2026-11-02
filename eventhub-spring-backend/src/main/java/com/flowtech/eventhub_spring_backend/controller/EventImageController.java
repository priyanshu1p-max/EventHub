
package com.flowtech.eventhub_spring_backend.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/uploads")
public class EventImageController {

    private final Path uploadPath;

    public EventImageController(
            @Value("${eventhub.upload-dir}") String uploadDir) {

        this.uploadPath = Paths.get(uploadDir)
                .toAbsolutePath()
                .normalize();
    }

    @GetMapping("/{filename:.+}")
    public ResponseEntity<Resource> getImage(
            @PathVariable String filename) throws IOException {

        // Allow only supported image filenames.
        if (!filename.matches(
                "[a-fA-F0-9-]+\\.(jpg|jpeg|png|webp)")) {
            return ResponseEntity.badRequest().build();
        }

        Path file = uploadPath.resolve(filename).normalize();

        // Prevent path traversal and reject missing files.
        if (!file.startsWith(uploadPath)
                || !Files.isRegularFile(file)) {
            return ResponseEntity.notFound().build();
        }

        Resource resource = new UrlResource(file.toUri());

        if (!resource.exists() || !resource.isReadable()) {
            return ResponseEntity.notFound().build();
        }

        MediaType mediaType = MediaTypeFactory
                .getMediaType(resource)
                .orElse(MediaType.APPLICATION_OCTET_STREAM);

        return ResponseEntity.ok()
                .contentType(mediaType)
                .body(resource);
    }
}
