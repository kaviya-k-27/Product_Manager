package com.example.productapiintegration.dto.save;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonAlias;
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
public class ItemSaveRequestDto {

    @JsonAlias({"itemId", "id"})
    private Long itemId;

    private String name;

    private String description;

    private Double price;

    private Long categoryId;

    // Allows the remote API to accept extra fields without breaking request serialization.
    @Builder.Default
    @JsonAnyGetter
    private Map<String, Object> extra = new HashMap<>();

    @JsonAnySetter
    public void setExtra(String key, Object value) {
        extra.put(key, value);
    }
}

