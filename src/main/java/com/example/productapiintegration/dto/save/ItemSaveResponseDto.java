package com.example.productapiintegration.dto.save;

import com.example.productapiintegration.dto.item.ItemDto;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ItemSaveResponseDto {
    private Boolean success;
    private String message;
    private ItemDto item;
}

