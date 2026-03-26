package com.expense.management.service;

import com.expense.management.exception.FileStorageException;
import com.expense.management.util.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.UUID;

@Service
@Slf4j
public class FileStorageService {

    private final Path uploadDir;

    public FileStorageService(@Value("${app.file-storage.local.upload-dir:./uploads}") String uploadDir) {
        this.uploadDir = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.uploadDir);
            log.info("File storage directory: {}", this.uploadDir);
        } catch (IOException e) {
            throw new FileStorageException("Could not create upload directory", e);
        }
    }

    public String storeFile(MultipartFile file) {
        validateFile(file);

        String originalFilename = StringUtils.cleanPath(
                file.getOriginalFilename() != null ? file.getOriginalFilename() : "file");
        String extension = getExtension(originalFilename);
        String storedFilename = UUID.randomUUID() + "." + extension;

        try {
            Path targetPath = uploadDir.resolve(storedFilename);
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
            }
            log.info("Stored file: {} as {}", originalFilename, storedFilename);
            return storedFilename;
        } catch (IOException e) {
            throw new FileStorageException("Failed to store file: " + originalFilename, e);
        }
    }

    public byte[] loadFile(String storedFilename) {
        try {
            Path filePath = uploadDir.resolve(storedFilename).normalize();
            if (!filePath.startsWith(uploadDir)) {
                throw new FileStorageException("Invalid file path: path traversal detected");
            }
            return Files.readAllBytes(filePath);
        } catch (IOException e) {
            throw new FileStorageException("File not found: " + storedFilename, e);
        }
    }

    public void deleteFile(String storedFilename) {
        try {
            Path filePath = uploadDir.resolve(storedFilename).normalize();
            if (!filePath.startsWith(uploadDir)) {
                throw new FileStorageException("Invalid file path: path traversal detected");
            }
            Files.deleteIfExists(filePath);
            log.info("Deleted file: {}", storedFilename);
        } catch (IOException e) {
            log.warn("Could not delete file: {}", storedFilename);
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new FileStorageException("File is empty or missing");
        }
        if (file.getSize() > Constants.MAX_FILE_SIZE_BYTES) {
            throw new FileStorageException("File size exceeds the maximum allowed 10MB");
        }
        String contentType = file.getContentType();
        if (contentType == null || Arrays.stream(Constants.ALLOWED_FILE_TYPES)
                .noneMatch(allowed -> allowed.equalsIgnoreCase(contentType))) {
            throw new FileStorageException(
                "File type not allowed. Allowed types: JPG, PNG, PDF");
        }
        String filename = file.getOriginalFilename();
        if (filename == null || filename.isBlank()) {
            throw new FileStorageException("File name is missing");
        }
        String ext = getExtension(filename).toLowerCase();
        if (Arrays.stream(Constants.ALLOWED_EXTENSIONS).noneMatch(e -> e.equals(ext))) {
            throw new FileStorageException("File extension not allowed: " + ext);
        }
    }

    private String getExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex < 0) return "bin";
        return filename.substring(dotIndex + 1);
    }
}
