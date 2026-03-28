package com.example.productapiintegration.dto.category;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CategoryDto {

    // Remote API uses many possible names — capture all variants
    @JsonAlias({"categoryId", "id", "Id", "CategoryId", "category_id",
                "ItemCategoryId", "itemCategoryId", "catId"})
    private Long categoryId;

    @JsonAlias({"name", "categoryName", "CategoryName", "category_name", "title"})
    private String name;

    // Catch-all: if remote uses an unknown field name for id/name, we grab it here
    @JsonAnySetter
    public void setExtra(String key, Object value) {
        String k = key.toLowerCase();
        if (categoryId == null && (k.contains("id") || k.contains("cat"))) {
            try { categoryId = Long.parseLong(String.valueOf(value)); } catch (Exception ignored) {}
        }
        if (name == null && (k.contains("name") || k.contains("title"))) {
            name = String.valueOf(value);
        }
    }
}