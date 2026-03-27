package com.example.productapiintegration.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.client.RestTemplate;

@Configuration
@EnableConfigurationProperties(ApiProperties.class)
public class AppConfig {

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder, AccessTokenStore tokenStore) {
        return builder
                .additionalInterceptors((request, body, execution) -> {
                    String token = tokenStore.getToken();
                    if (token != null && !token.isBlank()) {
                        request.getHeaders().set("Authorization", "Bearer " + token);
                    }
                    return execution.execute(request, body);
                })
                .build();
    }
}
