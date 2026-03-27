package com.example.productapiintegration.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "api")
public record ApiProperties(String baseUrl) {
}
