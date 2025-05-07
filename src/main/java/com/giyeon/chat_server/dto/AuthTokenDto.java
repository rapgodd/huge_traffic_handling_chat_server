package com.giyeon.chat_server.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthTokenDto {
    private String grantType;
    private String accessToken;
    private String refreshToken;
    private Long userId;
}