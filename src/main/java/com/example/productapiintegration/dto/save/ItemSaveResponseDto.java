package com.example.productapiintegration.dto.save;

import com.example.productapiintegration.dto.item.ItemDto;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ItemSaveResponseDto {
    private Boolean success;
    private String message;
    private ItemDto item;

    @JsonAlias({"itemId", "id", "Id", "savedId"})
    private Long itemId;

    @JsonAlias({"statusCode", "status", "code"})
    private Integer statusCode;

    public boolean isSuccessful() {
        if (success != null) return success;
        if (statusCode != null) return statusCode == 200 || statusCode == 1;
        return true;
    }
}