package com.example.productapiintegration.service;

import com.example.productapiintegration.config.AccessTokenStore;
import com.example.productapiintegration.config.ApiProperties;
import com.example.productapiintegration.dto.auth.AuthRequestDto;
import com.example.productapiintegration.util.ApiEndpoints;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@Service
public class AuthService {
    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final RestTemplate restTemplate;
    private final ApiProperties apiProperties;
    private final AccessTokenStore tokenStore;
    private final ObjectMapper objectMapper;

    public AuthService(RestTemplate restTemplate, ApiProperties apiProperties, AccessTokenStore tokenStore, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.apiProperties = apiProperties;
        this.tokenStore = tokenStore;
        this.objectMapper = objectMapper;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void fetchTokenOnStartup() {
        try {
            String url = apiProperties.baseUrl() + ApiEndpoints.GET_ACCESS;
            HttpHeaders headers = new HttpHeaders();
            // The remote endpoint appears to validate fields only when sent as form data.
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            AuthRequestDto request = AuthRequestDto.builder()
                    .username("admin")
                    .userName("admin")
                    .password("admin")
                    .build();

            // Send as x-www-form-urlencoded to match remote model binding.
            MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
            form.add("username", request.getUsername());
            form.add("userName", request.getUserName());
            form.add("password", request.getPassword());

            HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(form, headers);
            log.info("Auth request form: {}", form);

            ResponseEntity<String> resp = restTemplate.exchange(
                    url,
                    org.springframework.http.HttpMethod.POST,
                    entity,
                    String.class
            );

            String body = resp.getBody();
            log.info("Auth response status: {} body: {}", resp.getStatusCode(), body);
            if (body == null || body.isBlank()) {
                throw new IllegalStateException("Authentication response body is empty");
            }

            JsonNode root = objectMapper.readTree(body);
            String token = extractToken(root);
            if (token == null || token.isBlank()) {
                throw new IllegalStateException("Access token missing in authentication response: " + body);
            }

            tokenStore.setToken(token);
            log.info("Authenticated successfully. Token stored.");
        } catch (Exception e) {
            // Fail fast because the downstream endpoints all require Authorization.
            log.error("Failed to authenticate on startup.", e);
            throw e instanceof RuntimeException ? (RuntimeException) e : new RuntimeException(e);
        }
    }

    private String extractToken(JsonNode node) {
        // Search for a token field anywhere in the response (top-level or nested).
        if (node == null || node.isNull()) {
            return null;
        }

        if (node.isObject()) {
            JsonNode direct = findTextField(node, "token", "accessToken", "jwt", "JWT");
            if (direct != null) {
                return direct.asText();
            }

            // Common wrappers
            JsonNode data = node.get("data");
            if (data == null) data = node.get("Data");
            JsonNode result = node.get("result");
            if (result == null) result = node.get("Result");

            String fromData = extractToken(data);
            if (fromData != null && !fromData.isBlank()) return fromData;

            String fromResult = extractToken(result);
            if (fromResult != null && !fromResult.isBlank()) return fromResult;

            // Recursive search as a last resort
            for (var it = node.fields(); it.hasNext(); ) {
                var entry = it.next();
                if (entry.getValue() != null && entry.getValue().isContainerNode()) {
                    String found = extractToken(entry.getValue());
                    if (found != null && !found.isBlank()) return found;
                } else if (entry.getKey() != null && entry.getKey().equalsIgnoreCase("token")) {
                    if (entry.getValue().isTextual()) {
                        String v = entry.getValue().asText();
                        if (v != null && !v.isBlank()) return v;
                    }
                }
            }
        }

        if (node.isArray()) {
            for (JsonNode child : node) {
                String found = extractToken(child);
                if (found != null && !found.isBlank()) return found;
            }
        }

        return null;
    }

    private JsonNode findTextField(JsonNode node, String... fieldNames) {
        for (String f : fieldNames) {
            JsonNode v = node.get(f);
            if (v != null && v.isTextual() && !v.asText().isBlank()) {
                return v;
            }
        }
        // Case-insensitive scan
        for (var it = node.fields(); it.hasNext(); ) {
            var entry = it.next();
            for (String f : fieldNames) {
                if (entry.getKey() != null && entry.getKey().equalsIgnoreCase(f) && entry.getValue().isTextual() && !entry.getValue().asText().isBlank()) {
                    return entry.getValue();
                }
            }
        }

        return null;
    }
}

