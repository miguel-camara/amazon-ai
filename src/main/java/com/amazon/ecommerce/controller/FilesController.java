package com.amazon.ecommerce.controller;

import java.io.IOException;
import java.util.Map;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.amazon.ecommerce.service.FileStorageService;

@RestController
@RequestMapping("files")
public class FilesController {

  private final FileStorageService fileStorageService;

  public FilesController(FileStorageService fileStorageService) {
    this.fileStorageService = fileStorageService;
  }

  @GetMapping("/product/{imageName}")
  public ResponseEntity<Resource> findProductImage(
      @PathVariable String imageName) {

    Resource resource = fileStorageService.loadFile(imageName);

    return ResponseEntity.ok()
        .contentType(resolveMediaType(imageName))
        .body(resource);
  }

  @PostMapping("/product")
  public ResponseEntity<?> uploadProductImage(
      @RequestParam("file") MultipartFile file) throws IOException {
    if (file == null || file.isEmpty()) {
      throw new IOException(
          "Make sure that the file is an image");
    }

    var img = this.fileStorageService.storeFile(file);

    return ResponseEntity.ok().body(Map.of(
        "img", img));
  }

  private MediaType resolveMediaType(String fileName) {
    String extension = fileName
        .substring(fileName.lastIndexOf('.') + 1)
        .toLowerCase();
    return switch (extension) {
      case "jpg", "jpeg" -> MediaType.IMAGE_JPEG;
      case "png" -> MediaType.IMAGE_PNG;
      case "gif" -> MediaType.IMAGE_GIF;
      default -> MediaType.APPLICATION_OCTET_STREAM;
    };
  }
}
