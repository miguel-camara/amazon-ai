package com.amazon.ecommerce.controller;

import com.amazon.ecommerce.dto.request.ProductRequest;
import com.amazon.ecommerce.dto.response.ApiResponse;
import com.amazon.ecommerce.dto.response.ProductResponse;
import com.amazon.ecommerce.service.ProductService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getAllProducts() {
        List<ProductResponse> products = productService.getAllProducts();
        return ResponseEntity.ok(ApiResponse.success(products));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> getProductById(
        @PathVariable Long id
    ) {
        ProductResponse product = productService.getProductById(id);
        return ResponseEntity.ok(ApiResponse.success(product));
    }

    @GetMapping("/{productId}/images/{imageIndex}")
    public ResponseEntity<Resource> getProductImage(
        @PathVariable Long productId,
        @PathVariable int imageIndex
    ) {
        Resource image = productService.getProductImage(productId, imageIndex);
        return ResponseEntity.ok()
            .contentType(MediaType.IMAGE_JPEG)
            .body(image);
    }

    // @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    // @PreAuthorize("hasRole('ADMIN')")
    @PostMapping()
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(
        @Valid @RequestPart("product") ProductRequest request,
        @RequestPart(value = "images", required = false) MultipartFile[] images
    ) {
        System.out.println("---------------------");
        System.out.println("prueba");
        System.out.println("---------------------");

        ProductResponse product = productService.createProduct(request, images);
        return ResponseEntity.status(HttpStatus.CREATED).body(
            ApiResponse.success("Product created successfully", product)
        );
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(
        @PathVariable Long id,
        @Valid @RequestPart("product") ProductRequest request,
        @RequestPart(value = "images", required = false) MultipartFile[] images
    ) {
        ProductResponse product = productService.updateProduct(
            id,
            request,
            images
        );
        return ResponseEntity.ok(
            ApiResponse.success("Product updated successfully", product)
        );
    }

    @PostMapping(
        value = "/{id}/images",
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProductResponse>> addImages(
        @PathVariable Long id,
        @RequestPart("images") MultipartFile[] images
    ) {
        ProductResponse product = productService.addImages(id, images);
        return ResponseEntity.ok(
            ApiResponse.success("Images added successfully", product)
        );
    }

    @DeleteMapping("/{id}/images/{imageIndex}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProductResponse>> deleteImage(
        @PathVariable Long id,
        @PathVariable int imageIndex
    ) {
        ProductResponse product = productService.deleteImage(id, imageIndex);
        return ResponseEntity.ok(
            ApiResponse.success("Image deleted successfully", product)
        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(
        @PathVariable Long id
    ) {
        productService.deleteProduct(id);
        return ResponseEntity.ok(
            ApiResponse.success("Product deleted successfully", null)
        );
    }
}
