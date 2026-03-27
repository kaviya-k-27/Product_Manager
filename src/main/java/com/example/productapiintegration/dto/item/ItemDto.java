package com.example.productapiintegration.dto.item;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ItemDto {

    @JsonAlias({"itemId", "id", "Id", "ItemId"})
    private Long itemId;

    @JsonAlias({"name", "itemName", "ItemName"})
    private String name;

    // Remote sometimes returns the "Category" as the descriptive text.
    @JsonAlias({"description", "itemDescription", "Category", "category", "SKU"})
    private String description;

    @JsonAlias({"price", "itemPrice", "SellingPrice"})
    private Double price;

    @JsonAlias({"categoryId", "itemCategoryId", "ItemCategoryId"})
    private Long categoryId;

    // Some APIs return an image URL/path alongside the item.
    @JsonAlias({"imageUrl", "imagePath", "ImagePath"})
    private String imageUrl;
}

