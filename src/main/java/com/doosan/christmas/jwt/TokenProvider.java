package com.doosan.christmas.jwt;

import com.doosan.christmas.domain.Member;
import com.doosan.christmas.domain.RefreshToken;
import com.doosan.christmas.domain.UserDetailsImpl;
import com.doosan.christmas.dto.requestdto.TokenDto;
import com.doosan.christmas.dto.responsedto.ResponseDto;
import com.doosan.christmas.repository.RefreshTokenRepository;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.security.Key;
import java.util.Date;
import java.util.Optional;

@Slf4j
@Component
public class TokenProvider {

    private static final String AUTHORITIES_KEY = "auth"; // 권한 키
    private static final String BEARER_PREFIX = "Bearer "; // 토큰 접두사
    private static final long ACCESS_TOKEN_EXPIRE_TIME = 1000 * 60 * 30; // 엑세스 토큰 만료시간 (30분)
    private static final long REFRESH_TOKEN_EXPIRE_TIME = 1000 * 60 * 60 * 24 * 7; // 리프레시 토큰 만료시간 (7일)

    private final Key key; // JWT 서명 키
    private final RefreshTokenRepository refreshTokenRepository; // 리프레시 토큰 저장소

    // TokenProvider 생성자 - secretKey를 사용해 서명 키 생성
    public TokenProvider(@Value("${jwt.secret}") String secretKey, RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    // JWT 토큰 생성
    public TokenDto generateTokenDto(Member member) {
        long now = (new Date().getTime()); // 현재 시간

        // 엑세스 토큰 생성
        Date accessTokenExpiresIn = new Date(now + ACCESS_TOKEN_EXPIRE_TIME);
        String accessToken = Jwts.builder()
                .setSubject(member.getNickname())
                .claim(AUTHORITIES_KEY, "ROLE_USER") // 권한 추가
                .setExpiration(accessTokenExpiresIn)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        // 리프레시 토큰 생성
        String refreshToken = Jwts.builder()
                .setExpiration(new Date(now + REFRESH_TOKEN_EXPIRE_TIME))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        // 리프레시 토큰 객체 저장
        RefreshToken refreshTokenObject = RefreshToken.builder()
                .id(member.getId())
                .member(member)
                .value(refreshToken)
                .build();
        refreshTokenRepository.save(refreshTokenObject);

        // 토큰 정보 반환
        return TokenDto.builder()
                .grantType(BEARER_PREFIX)
                .accessToken(accessToken)
                .accessTokenExpiresIn(accessTokenExpiresIn.getTime())
                .refreshToken(refreshToken)
                .build();
    }

    // 현재 인증된 사용자 정보 가져오기
    public Member getMemberFromAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || AnonymousAuthenticationToken.class.isAssignableFrom(authentication.getClass())) {
            return null; // 인증 정보가 없으면 null 반환
        }
        return ((UserDetailsImpl) authentication.getPrincipal()).getMember();
    }

    // JWT 토큰 유효성 검사
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true; // 토큰 유효
        } catch (SecurityException | MalformedJwtException e) {
            log.info("Invalid JWT signature, 유효하지 않는 JWT 서명 입니다.");
        } catch (ExpiredJwtException e) {
            log.info("Expired JWT token, 만료된 JWT token 입니다.");
        } catch (UnsupportedJwtException e) {
            log.info("Unsupported JWT token, 지원되지 않는 JWT 토큰 입니다.");
        } catch (IllegalArgumentException e) {
            log.info("JWT claims is empty, 잘못된 JWT 토큰 입니다.");
        }
        return false; // 토큰 유효하지 않음
    }

    // 특정 회원의 리프레시 토큰 존재 여부 확인
    @Transactional(readOnly = true)
    public RefreshToken isPresentRefreshToken(Member member) {
        Optional<RefreshToken> optionalRefreshToken = refreshTokenRepository.findByMember(member);
        return optionalRefreshToken.orElse(null); // 존재하지 않으면 null 반환
    }

    // 리프레시 토큰 삭제
    @Transactional
    public ResponseDto<?> deleteRefreshToken(Member member) {
        RefreshToken refreshToken = isPresentRefreshToken(member);
        if (null == refreshToken) { // 토큰이 없으면 에러 반환
            return ResponseDto.fail("TOKEN_NOT_FOUND", "존재하지 않는 Token 입니다.");
        }

        refreshTokenRepository.delete(refreshToken); // 리프레시 토큰 삭제
        return ResponseDto.success("success"); // 성공 메시지 반환
    }
}