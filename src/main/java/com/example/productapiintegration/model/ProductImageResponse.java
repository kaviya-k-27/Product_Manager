package com.example.productapiintegration.model;

public record ProductImageResponse(
        byte[] bytes,
        String contentType
) {
}

