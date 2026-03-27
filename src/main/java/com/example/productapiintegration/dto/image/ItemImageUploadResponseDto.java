package com.example.productapiintegration.dto.image;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ItemImageUploadResponseDto {
    private Boolean success;
    private String message;

    // Common variants.
    private String imageUrl;
    private String imagePath;
}

