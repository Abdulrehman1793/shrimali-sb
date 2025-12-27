package com.shrimali.modules.auth.dto;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TokenResponse {
    @Builder(toBuilder = true)
    public TokenResponse(String token) {
        this.token = token;
    }

    private String token;
}
