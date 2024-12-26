package com.doosan.christmas.common.dto.requestDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TokenDto {
    private String grantType; // 인증 타입 (Bearer )
    private String accessToken; // 엑세스토큰
    private String refreshToken; // 리프레시토큰
    private Long accessTokenExpiresIn; // 액세스 토큰 만료 시간 (밀리초)


}