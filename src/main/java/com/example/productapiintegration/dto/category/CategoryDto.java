package com.example.productapiintegration.dto.category;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CategoryDto {

    @JsonAlias({"categoryId", "id"})
    private Long categoryId;

    @JsonAlias({"name", "categoryName"})
    private String name;
}

