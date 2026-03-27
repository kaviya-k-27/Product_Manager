package com.example.productapiintegration.dto.auth;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthRequestDto {

    // Some implementations use `username`, others require `userName`.
    // We send both to be compatible with the remote API validation rules.
    @JsonProperty("username")
    @JsonAlias({"userName"})
    private String username;

    @JsonProperty("userName")
    @JsonAlias({"username", "user_name"})
    private String userName;

    @JsonProperty("password")
    private String password;

    // Explicit getters so compilation is not dependent on Lombok capitalization nuances.
    public String getUsername() {
        return username;
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }
}

