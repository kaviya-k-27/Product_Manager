package com.example.productapiintegration.controller;

import com.example.productapiintegration.dto.image.ItemImageUploadResponseDto;
import com.example.productapiintegration.dto.save.ItemSaveRequestDto;
import com.example.productapiintegration.dto.save.ItemSaveResponseDto;
import com.example.productapiintegration.model.Category;
import com.example.productapiintegration.model.Product;
import com.example.productapiintegration.model.ProductImageResponse;
import com.example.productapiintegration.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@Tag(name = "Product API Proxy", description = "Endpoints that proxy the remote interview API")
@RequestMapping("/api-proxy")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @Operation(summary = "Get all products")
    @GetMapping("/items")
    public List<Product> getProducts() {
        return productService.getProducts();
    }

    @Operation(summary = "Get item categories")
    @GetMapping("/items/categories")
    public List<Category> getCategories() {
        return productService.getCategories();
    }

    @Operation(summary = "Get product image (raw bytes)")
    @GetMapping("/items/{itemId}/image")
    public ResponseEntity<byte[]> getProductImage(@PathVariable Long itemId) {
        ProductImageResponse image = productService.getProductImage(itemId);

        HttpHeaders headers = new HttpHeaders();
        if (image.contentType() != null && !image.contentType().isBlank()) {
            try {
                headers.setContentType(MediaType.parseMediaType(image.contentType()));
            } catch (Exception ignored) {
                // If contentType isn't parseable, fall back to default.
            }
        }

        return ResponseEntity.ok()
                .headers(headers)
                .body(image.bytes());
    }

    @Operation(summary = "Save a product (JSON)")
    @PostMapping(value = "/items/save", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ItemSaveResponseDto saveProduct(@RequestBody ItemSaveRequestDto request) {
        return productService.saveProduct(request);
    }

    @Operation(summary = "Upload product image (multipart)")
    @PostMapping(value = "/items/{itemId}/upload-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ItemImageUploadResponseDto uploadImage(
            @PathVariable Long itemId,
            @RequestPart("file") MultipartFile file
    ) {
        return productService.uploadImage(itemId, file);
    }
}
