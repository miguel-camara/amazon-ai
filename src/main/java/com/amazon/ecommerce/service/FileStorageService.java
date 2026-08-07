package com.amazon.ecommerce.service;

import com.amazon.ecommerce.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

// import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class FileStorageService {

  private final Path uploadDir;

  private static final Set<String> ALLOWED_TYPES = Set.of("image/jpeg", "image/png", "image/gif", "image/webp");
  private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

  public FileStorageService(@Value("${app.upload.dir:uploads/images}") String uploadDir) {
    this.uploadDir = Paths.get(uploadDir).toAbsolutePath().normalize();
    try {
      Files.createDirectories(this.uploadDir);
    } catch (IOException e) {
      throw new RuntimeException("Could not create upload directory: " + this.uploadDir, e);
    }
  }

  public String storeFile(MultipartFile file) {
    validateFile(file);

    String originalName = file.getOriginalFilename();
    String extension = "";
    if (originalName != null && originalName.contains(".")) {
      extension = originalName.substring(originalName.lastIndexOf("."));
    }

    String storedName = UUID.randomUUID().toString() + extension;

    try {
      Path targetPath = this.uploadDir.resolve(storedName);
      Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
      return storedName;
    } catch (IOException e) {
      throw new RuntimeException("Could not store file " + storedName, e);
    }
  }

  public Resource loadFile(String fileName) {
    try {
      Path filePath = this.uploadDir.resolve(fileName).normalize();
      Resource resource = new UrlResource(filePath.toUri());
      if (resource.exists() && resource.isReadable()) {
        return resource;
      } else {
        throw new BadRequestException("File not found: " + fileName);
      }
    } catch (MalformedURLException e) {
      throw new BadRequestException("File not found: " + fileName);
    }
  }

  public void deleteFile(String fileName) {
    try {
      Path filePath = this.uploadDir.resolve(fileName).normalize();
      Files.deleteIfExists(filePath);
    } catch (IOException e) {
      throw new RuntimeException("Could not delete file: " + fileName, e);
    }
  }

  public void deleteFiles(List<String> fileNames) {
    for (String fileName : fileNames) {
      deleteFile(fileName);
    }
  }

  private void validateFile(MultipartFile file) {
    if (file.isEmpty()) {
      throw new BadRequestException("File is empty");
    }
    if (file.getSize() > MAX_FILE_SIZE) {
      throw new BadRequestException("File size exceeds maximum allowed (5MB)");
    }
    String contentType = file.getContentType();
    if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
      throw new BadRequestException("Invalid file type. Allowed: JPEG, PNG, GIF, WebP");
    }
  }
}
