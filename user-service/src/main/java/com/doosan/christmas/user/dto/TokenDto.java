package com.doosan.christmas.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TokenDto {
    private String access_token;
    private String refresh_token;
    private long expires_in = 7200; // 2시간

    public TokenDto(String accessToken, String refreshToken) {
        this.access_token = accessToken;
        this.refresh_token = refreshToken;
    }
} 