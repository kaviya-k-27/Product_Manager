package com.example.productapiintegration.dto.auth;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AuthResponseDto {

    // The API may return token under different field names; handle common variants.
    @JsonAlias({"token", "accessToken", "jwt", "JWT"})
    private String token;
}

