package com.example.auth.dto;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@Builder
public class RefreshTokenRequest {
        @NotBlank
        String refreshToken;
}
