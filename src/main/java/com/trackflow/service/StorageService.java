package com.trackflow.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

/**
 * Service interface for handling file storage operations.
 *
 * <p>Uses the Strategy Pattern to decouple storage providers (Local disk, AWS S3, MinIO)
 * from the rest of the application.</p>
 */
public interface StorageService {

    /**
     * Stores a file and returns its relative stored path.
     */
    String store(MultipartFile file);

    /**
     * Loads a file as a Spring Resource for downloading.
     */
    Resource load(String filePath);

    /**
     * Deletes a file from storage.
     */
    void delete(String filePath);
}
