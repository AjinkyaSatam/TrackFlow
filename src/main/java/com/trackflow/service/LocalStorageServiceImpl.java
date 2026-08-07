package com.trackflow.service;

import com.trackflow.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Local Disk implementation of the {@link StorageService}.
 */
@Service
public class LocalStorageServiceImpl implements StorageService {

    private final Path rootLocation;
    private final List<String> allowedExtensions;
    private final long maxFileSize;

    public LocalStorageServiceImpl(
            @Value("${app.storage.local.upload-dir}") String uploadDir,
            @Value("${app.storage.allowed-extensions}") String extensions,
            @Value("${app.storage.max-file-size}") long maxFileSize
    ) {
        this.rootLocation = Paths.get(uploadDir);
        this.allowedExtensions = Arrays.asList(extensions.split(","));
        this.maxFileSize = maxFileSize;
        initDirectories();
    }

    private void initDirectories() {
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize storage directory", e);
        }
    }

    @Override
    public String store(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BadRequestException("Failed to store empty file.");
        }

        if (file.getSize() > maxFileSize) {
            throw new BadRequestException("File size exceeds limit of " + (maxFileSize / (1024 * 1024)) + "MB");
        }

        String originalFilename = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        String extension = getFileExtension(originalFilename);

        if (!allowedExtensions.contains(extension.toLowerCase())) {
            throw new BadRequestException("File type not allowed. Allowed types: " + allowedExtensions);
        }

        // Generate unique name to prevent collisions
        String uniqueFilename = UUID.randomUUID().toString() + "." + extension;

        try {
            Path destinationFile = this.rootLocation.resolve(Paths.get(uniqueFilename)).normalize().toAbsolutePath();
            
            // Security check to prevent directory traversal attacks
            if (!destinationFile.getParent().equals(this.rootLocation.toAbsolutePath())) {
                throw new BadRequestException("Cannot store file outside current directory.");
            }

            Files.copy(file.getInputStream(), destinationFile, StandardCopyOption.REPLACE_EXISTING);
            return uniqueFilename; // Return relative filename to save in DB
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file.", e);
        }
    }

    @Override
    public Resource load(String filePath) {
        try {
            Path file = rootLocation.resolve(filePath);
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("Could not read file: " + filePath);
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("Could not read file: " + filePath, e);
        }
    }

    @Override
    public void delete(String filePath) {
        try {
            Path file = rootLocation.resolve(filePath);
            Files.deleteIfExists(file);
        } catch (IOException e) {
            throw new RuntimeException("Could not delete file: " + filePath, e);
        }
    }

    private String getFileExtension(String filename) {
        int lastIndex = filename.lastIndexOf('.');
        if (lastIndex == -1) {
            return "";
        }
        return filename.substring(lastIndex + 1);
    }
}
