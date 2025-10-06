package com.example.bulksmsAPI.Services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${file.upload-dir:uploads/validation-images}")
    private String uploadDir;

    public String storeFile(MultipartFile file, Long userId) throws IOException {
        // Crear el directorio si no existe
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Validar el archivo
        if (file.isEmpty()) {
            throw new IOException("Failed to store empty file");
        }

        // Validar tipo de archivo (solo imágenes)
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IOException("Only image files are allowed");
        }

        // Generar nombre único para el archivo
        String originalFilename = file.getOriginalFilename();
        String fileExtension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String newFilename = "user_" + userId + "_" + UUID.randomUUID().toString() + fileExtension;

        // Guardar el archivo
        Path destinationFile = uploadPath.resolve(newFilename);
        Files.copy(file.getInputStream(), destinationFile, StandardCopyOption.REPLACE_EXISTING);

        return newFilename;
    }

    public void deleteFile(String filename) throws IOException {
        if (filename != null && !filename.isEmpty()) {
            Path filePath = Paths.get(uploadDir).resolve(filename);
            Files.deleteIfExists(filePath);
        }
    }

    public Path getFilePath(String filename) {
        return Paths.get(uploadDir).resolve(filename);
    }

    public byte[] loadFileAsBytes(String filename) throws IOException {
        Path filePath = getFilePath(filename);
        return Files.readAllBytes(filePath);
    }
}

