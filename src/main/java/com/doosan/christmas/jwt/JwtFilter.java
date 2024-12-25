package com.doosan.christmas.jwt;

import com.doosan.christmas.dto.responsedto.ResponseDto;
import com.doosan.christmas.service.UserDetailsServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.Key;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    public static String AUTHORIZATION_HEADER = "Authorization"; // 요청 헤더 이름
    public static String BEARER_PREFIX = "Bearer "; // 토큰 접두사
    public static String AUTHORITIES_KEY = "auth"; // 권한 키

    private final String SECRET_KEY; // 암호화 키
    private final TokenProvider tokenProvider; // 토큰 관련 유틸리티
    private final UserDetailsServiceImpl userDetailsService; // 사용자 정보 서비스

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws IOException, ServletException {

        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY); // 키 디코딩
        Key key = Keys.hmacShaKeyFor(keyBytes); // 키 생성

        String jwt = resolveToken(request); // 요청에서 JWT 토큰 추출

        if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) { // 토큰 유효성 검사
            Claims claims;

            try {
                claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(jwt).getBody(); // 토큰 파싱
            } catch (ExpiredJwtException e) {
                claims = e.getClaims(); // 만료된 토큰도 클레임에서 데이터 추출
            }

            if (claims.getExpiration().toInstant().toEpochMilli() < Instant.now().toEpochMilli()) { // 토큰 만료 여부 확인
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().println(
                        new ObjectMapper().writeValueAsString(
                                ResponseDto.fail("BAD_REQUEST", "Token이 유효하지 않습니다.")
                        )
                );
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return; // 필터 체인 중단
            }

            String subject = claims.getSubject(); // 사용자 식별 정보 추출

            // 권한 정보 필터링 및 변환
            // 권한 정보 추출
            String authorities = claims.get(AUTHORITIES_KEY) != null ? claims.get(AUTHORITIES_KEY).toString() : "";
            Collection<? extends GrantedAuthority> grantedAuthorities;

            if (authorities.isBlank()) {

                // 권한 정보가 없으면 기본 ROLE_USER 설정
                grantedAuthorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));

            } else {
                // 권한 정보가 있으면 변환
                grantedAuthorities = Arrays.stream(authorities.split(","))
                        .filter(auth -> auth != null && !auth.isBlank())
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());
            }

            UserDetails principal = userDetailsService.loadUserByUsername(subject); // 사용자 정보 로드

            Authentication authentication = new UsernamePasswordAuthenticationToken(principal, jwt, grantedAuthorities); // 인증 객체 생성
            SecurityContextHolder.getContext().setAuthentication(authentication); // 인증 정보 저장
        }

        filterChain.doFilter(request, response); // 다음 필터 실행
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER); // 헤더에서 토큰 가져오기
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(7); // "Bearer " 제거 후 반환
        }
        return null; // 토큰이 없으면 null 반환
    }
}
