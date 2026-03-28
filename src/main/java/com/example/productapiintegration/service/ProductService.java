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

            // Parse raw JSON and extract id+name flexibly — remote API field names are inconsistent
            com.fasterxml.jackson.databind.JsonNode root = objectMapper.readTree(body);
            com.fasterxml.jackson.databind.JsonNode arr = root.isArray() ? root
                    : (root.get("data") != null ? root.get("data")
                    : root.get("result") != null ? root.get("result")
                    : root.get("categories") != null ? root.get("categories")
                    : root);

            java.util.List<Category> result = new java.util.ArrayList<>();
            for (com.fasterxml.jackson.databind.JsonNode node : arr) {
                Long id = extractLong(node, "categoryId","id","Id","CategoryId","itemCategoryId","catId");
                String name = extractString(node, "name","categoryName","CategoryName","title",
                        "itemCategory","ItemCategory","category","Category","label","catName");
                result.add(Category.builder().categoryId(id).name(name).build());
            }
            return result;
        } catch (Exception ex) {
            throw new RemoteApiException("Failed to fetch categories", 500, ex.getMessage());
        }
    }

    private Long extractLong(com.fasterxml.jackson.databind.JsonNode node, String... keys) {
        for (String k : keys) {
            com.fasterxml.jackson.databind.JsonNode v = node.get(k);
            if (v != null && !v.isNull()) {
                try { return v.asLong(); } catch (Exception ignored) {}
            }
        }
        // case-insensitive fallback
        node.fields().forEachRemaining(e -> {});
        for (var it = node.fields(); it.hasNext();) {
            var e = it.next();
            for (String k : keys) {
                if (e.getKey().equalsIgnoreCase(k) && !e.getValue().isNull()) {
                    try { return e.getValue().asLong(); } catch (Exception ignored) {}
                }
            }
        }
        return null;
    }

    private String extractString(com.fasterxml.jackson.databind.JsonNode node, String... keys) {
        for (String k : keys) {
            com.fasterxml.jackson.databind.JsonNode v = node.get(k);
            if (v != null && !v.isNull() && v.isTextual()) return v.asText();
        }
        // case-insensitive fallback
        for (var it = node.fields(); it.hasNext();) {
            var e = it.next();
            for (String k : keys) {
                if (e.getKey().equalsIgnoreCase(k) && e.getValue().isTextual()) {
                    return e.getValue().asText();
                }
            }
        }
        return null;
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
            // Remote API may use different field name conventions (itemName, itemPrice, etc.)
            // Send all common variants via the extra map so the remote API can bind whichever it expects.
            request.setExtra("itemName",        request.getName());
            request.setExtra("itemDescription", request.getDescription());
            request.setExtra("itemPrice",        request.getPrice());
            request.setExtra("itemCategoryId",   request.getCategoryId());
            request.setExtra("categoryId",       request.getCategoryId());

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