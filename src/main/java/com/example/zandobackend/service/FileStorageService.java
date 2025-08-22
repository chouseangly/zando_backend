package com.example.zandobackend.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
    /**
     * Saves a file to the server.
     * @param file The file to be saved.
     * @return The filename of the saved file.
     */
    String saveFile(MultipartFile file);
}