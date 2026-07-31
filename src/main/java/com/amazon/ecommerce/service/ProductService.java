package com.amazon.ecommerce.service;

import com.amazon.ecommerce.dto.request.ProductRequest;
import com.amazon.ecommerce.dto.response.ProductResponse;
import com.amazon.ecommerce.entity.Product;
import com.amazon.ecommerce.exception.ResourceNotFoundException;
import com.amazon.ecommerce.repository.ProductRepository;
import java.util.List;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final FileStorageService fileStorageService;

    public ProductService(
        ProductRepository productRepository,
        FileStorageService fileStorageService
    ) {
        this.productRepository = productRepository;
        this.fileStorageService = fileStorageService;
    }

    public List<ProductResponse> getAllProducts() {
        return productRepository
            .findAll()
            .stream()
            .map(this::toResponse)
            .toList();
    }

    public ProductResponse getProductById(Long id) {
        Product product = findProduct(id);
        return toResponse(product);
    }

    public Resource getProductImage(Long productId, int imageIndex) {
        Product product = findProduct(productId);
        List<String> images = product.getImageUrls();
        if (imageIndex < 0 || imageIndex >= images.size()) {
            throw new ResourceNotFoundException(
                "Image not found at index " + imageIndex
            );
        }
        return fileStorageService.loadFile(images.get(imageIndex));
    }

    @Transactional
    public ProductResponse createProduct(
        ProductRequest request,
        MultipartFile[] images
    ) {
        Product product = Product.builder()
            .name(request.getName())
            .description(request.getDescription())
            .price(request.getPrice())
            .quantity(request.getQuantity())
            .build();

        System.out.println("---------------------");
        System.out.println("prueba 2");
        System.out.println("---------------------");

        if (images != null && images.length > 0) {
            List<String> storedNames = fileStorageService.storeFiles(images);
            product.setImageUrls(storedNames);
        }

        product = productRepository.save(product);
        return toResponse(product);
    }

    @Transactional
    public ProductResponse updateProduct(
        Long id,
        ProductRequest request,
        MultipartFile[] images
    ) {
        Product product = findProduct(id);

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setQuantity(request.getQuantity());

        if (images != null && images.length > 0) {
            fileStorageService.deleteFiles(product.getImageUrls());
            List<String> storedNames = fileStorageService.storeFiles(images);
            product.setImageUrls(storedNames);
        }

        product = productRepository.save(product);
        return toResponse(product);
    }

    @Transactional
    public ProductResponse addImages(Long id, MultipartFile[] images) {
        Product product = findProduct(id);
        if (images != null && images.length > 0) {
            List<String> storedNames = fileStorageService.storeFiles(images);
            product.getImageUrls().addAll(storedNames);
            product = productRepository.save(product);
        }
        return toResponse(product);
    }

    @Transactional
    public ProductResponse deleteImage(Long id, int imageIndex) {
        Product product = findProduct(id);
        List<String> images = product.getImageUrls();

        if (imageIndex < 0 || imageIndex >= images.size()) {
            throw new ResourceNotFoundException(
                "Image not found at index " + imageIndex
            );
        }

        String removed = images.remove(imageIndex);
        fileStorageService.deleteFile(removed);
        product = productRepository.save(product);
        return toResponse(product);
    }

    @Transactional
    public void deleteProduct(Long id) {
        Product product = findProduct(id);
        fileStorageService.deleteFiles(product.getImageUrls());
        productRepository.delete(product);
    }

    private Product findProduct(Long id) {
        return productRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product", id));
    }

    private ProductResponse toResponse(Product product) {
        return ProductResponse.builder()
            .id(product.getId())
            .name(product.getName())
            .description(product.getDescription())
            .price(product.getPrice())
            .quantity(product.getQuantity())
            .imageUrls(product.getImageUrls())
            .createdAt(product.getCreatedAt())
            .updatedAt(product.getUpdatedAt())
            .build();
    }
}
