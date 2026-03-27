package com.example.productapiintegration.service;

import com.example.productapiintegration.config.ApiProperties;
import com.example.productapiintegration.dto.category.CategoryDto;
import com.example.productapiintegration.dto.item.ItemDto;
import com.example.productapiintegration.dto.save.ItemSaveRequestDto;
import com.example.productapiintegration.dto.save.ItemSaveResponseDto;
import com.example.productapiintegration.dto.image.ItemImageUploadResponseDto;
import com.example.productapiintegration.model.Category;
import com.example.productapiintegration.model.Product;
import com.example.productapiintegration.model.ProductImageResponse;
import com.example.productapiintegration.util.ApiEndpoints;
import com.example.productapiintegration.util.RemoteApiException;
import com.example.productapiintegration.util.RemoteJsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import java.util.List;
import java.util.Map;
import java.io.IOException;

@Service
public class ProductService {

    private final RestTemplate restTemplate;
    private final ApiProperties apiProperties;
    private final ObjectMapper objectMapper;

    public ProductService(RestTemplate restTemplate, ApiProperties apiProperties, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.apiProperties = apiProperties;
        this.objectMapper = objectMapper;
    }

    public List<Product> getProducts() {
        String url = apiProperties.baseUrl() + ApiEndpoints.GET_ITEMS;

        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            String body = response.getBody();
            if (body == null) return List.of();

            List<ItemDto> itemDtos = RemoteJsonParser.parseList(body, objectMapper, ItemDto.class);
            return itemDtos.stream().map(dto -> Product.builder()
                            .id(dto.getItemId())
                            .name(dto.getName())
                            .description(dto.getDescription())
                            .price(dto.getPrice())
                            .build())
                    .toList();
        } catch (HttpStatusCodeException ex) {
            throw new RemoteApiException("Failed to fetch products", ex.getRawStatusCode(), ex.getResponseBodyAsString());
        }
    }

    public List<Category> getCategories() {
        String url = apiProperties.baseUrl() + ApiEndpoints.GET_ITEM_CATEGORIES;
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            String body = response.getBody();
            if (body == null) return List.of();

            List<CategoryDto> categories = RemoteJsonParser.parseList(body, objectMapper, CategoryDto.class);
            return categories.stream()
                    .map(dto -> Category.builder()
                            .categoryId(dto.getCategoryId())
                            .name(dto.getName())
                            .build())
                    .toList();
        } catch (HttpStatusCodeException ex) {
            throw new RemoteApiException("Failed to fetch categories", ex.getRawStatusCode(), ex.getResponseBodyAsString());
        }
    }

    public ProductImageResponse getProductImage(Long itemId) {
        String url = apiProperties.baseUrl() + ApiEndpoints.GET_ITEM_IMAGE + "?itemId={itemId}";
        try {
            ResponseEntity<byte[]> response = restTemplate.getForEntity(url, byte[].class, Map.of("itemId", itemId));
            byte[] bytes = response.getBody();
            String contentType = response.getHeaders().getContentType() != null
                    ? response.getHeaders().getContentType().toString()
                    : MediaType.APPLICATION_OCTET_STREAM_VALUE;
            return new ProductImageResponse(bytes == null ? new byte[0] : bytes, contentType);
        } catch (HttpStatusCodeException ex) {
            throw new RemoteApiException("Failed to fetch product image", ex.getRawStatusCode(), ex.getResponseBodyAsString());
        }
    }

    public ItemSaveResponseDto saveProduct(ItemSaveRequestDto request) {
        String url = apiProperties.baseUrl() + ApiEndpoints.SAVE_ITEM;
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            String body = response.getBody();
            return RemoteJsonParser.parseObject(body, objectMapper, ItemSaveResponseDto.class);
        } catch (HttpStatusCodeException ex) {
            throw new RemoteApiException("Failed to save product", ex.getRawStatusCode(), ex.getResponseBodyAsString());
        }
    }

    public ItemImageUploadResponseDto uploadImage(Long itemId, MultipartFile file) {
        String url = apiProperties.baseUrl() + ApiEndpoints.UPLOAD_ITEM_IMAGE;

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("itemId", itemId);

            // Remote expects a multipart file part. If the remote uses a different field name than "file",
            // adjust the key below to match the API contract.
            body.add("file", asResource(file));

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);

            return RemoteJsonParser.parseObject(response.getBody(), objectMapper, ItemImageUploadResponseDto.class);
        } catch (HttpStatusCodeException ex) {
            throw new RemoteApiException("Failed to upload product image", ex.getRawStatusCode(), ex.getResponseBodyAsString());
        } catch (Exception e) {
            throw new IllegalStateException("Failed to upload product image", e);
        }
    }

    private Resource asResource(MultipartFile file) {
        try {
            byte[] bytes = file.getBytes();
            return new ByteArrayResource(bytes) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            };
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read multipart file", e);
        }
    }
}
