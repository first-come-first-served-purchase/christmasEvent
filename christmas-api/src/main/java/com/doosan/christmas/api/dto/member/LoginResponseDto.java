package com.doosan.christmas.api.dto.member;

import com.doosan.christmas.common.dto.TokenDto;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginResponseDto {
    private String accessToken;
    private String refreshToken;
    private MemberResponseDto member;

    public static LoginResponseDto from(TokenDto tokenDto, MemberResponseDto member) {
        return LoginResponseDto.builder()
                .accessToken(tokenDto.getAccessToken())
                .refreshToken(tokenDto.getRefreshToken())
                .member(member)
                .build();
    }
} 