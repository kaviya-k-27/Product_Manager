package com.example.productapiintegration.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

public final class RemoteJsonParser {

    private static final List<String> COMMON_LIST_WRAPPERS = List.of(
            "items", "result", "data", "Data", "Items",
            "categories", "Categories", "list", "List",
            "itemsList", "itemsDtoList"
    );

    private RemoteJsonParser() {
    }

    public static <T> List<T> parseList(String json, ObjectMapper objectMapper, Class<T> elementType) {
        try {
            JsonNode root = objectMapper.readTree(json);
            var collectionType = objectMapper.getTypeFactory().constructCollectionType(List.class, elementType);

            if (root == null || root.isNull()) {
                return List.of();
            }

            if (root.isArray()) {
                return objectMapper.convertValue(root, collectionType);
            }

            if (root.isObject()) {
                for (String key : COMMON_LIST_WRAPPERS) {
                    JsonNode node = root.get(key);
                    if (node != null && node.isArray()) {
                        return objectMapper.convertValue(node, collectionType);
                    }
                }
            }

            // Last resort: try converting the whole root as a list.
            return objectMapper.convertValue(root, collectionType);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse list response from remote API", e);
        }
    }

    public static <T> T parseObject(String json, ObjectMapper objectMapper, Class<T> type) {
        try {
            if (json == null || json.isBlank()) {
                return null;
            }
            return objectMapper.readValue(json, type);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse object response from remote API", e);
        }
    }

    public static JsonNode parseNode(String json, ObjectMapper objectMapper) {
        try {
            if (json == null || json.isBlank()) {
                return null;
            }
            return objectMapper.readTree(json);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse JSON from remote API", e);
        }
    }
}

