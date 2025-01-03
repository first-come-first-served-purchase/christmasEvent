package com.doosan.christmas.common.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Collections;

@Slf4j
@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secretKey;

    public Claims getAllClaimsFromToken(String token) {
        try {
            return Jwts.parserBuilder()
                .setSigningKey(secretKey.getBytes())
                .build()
                .parseClaimsJws(token)
                .getBody();
        } catch (Exception e) {
            log.error("토큰에서 클레임을 추출하는데 실패했습니다: {}", e.getMessage());
            return null;
        }
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(secretKey.getBytes())
                .build()
                .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            log.error("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }

    public Mono<Authentication> getAuthentication(String token) {
        Claims claims = getAllClaimsFromToken(token);
        if (claims == null) {
            return Mono.empty();
        }

        Long userId = claims.get("id", Long.class);
        String role = claims.get("role", String.class);

        return Mono.just(new UsernamePasswordAuthenticationToken(
            userId,
            null,
            Collections.singletonList(new SimpleGrantedAuthority(role))
        ));
    }
} 