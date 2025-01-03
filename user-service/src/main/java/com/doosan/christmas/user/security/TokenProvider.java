package com.doosan.christmas.user.security;

import com.doosan.christmas.user.domain.Member;
import com.doosan.christmas.user.dto.TokenDto;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.security.Key;
import java.util.Date;

@Slf4j
@Component
public class TokenProvider {

    private final Key key;
    private final long accessTokenValidityInMilliseconds;
    private final long refreshTokenValidityInMilliseconds;
    private final UserDetailsService userDetailsService;

    public TokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-validity-in-seconds}") long accessTokenValidityInSeconds,
            @Value("${jwt.refresh-token-validity-in-seconds}") long refreshTokenValidityInSeconds,
            UserDetailsService userDetailsService) {
        Assert.hasText(secret, "JWT secret cannot be null or empty");
        Assert.isTrue(accessTokenValidityInSeconds > 0, "Access token validity must be greater than zero");
        Assert.isTrue(refreshTokenValidityInSeconds > 0, "Refresh token validity must be greater than zero");
        Assert.notNull(userDetailsService, "UserDetailsService cannot be null");
        
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
        this.accessTokenValidityInMilliseconds = accessTokenValidityInSeconds * 1000;
        this.refreshTokenValidityInMilliseconds = refreshTokenValidityInSeconds * 1000;
        this.userDetailsService = userDetailsService;
    }

    public TokenDto generateToken(Member member) {
        String accessToken = createToken(member, accessTokenValidityInMilliseconds);
        String refreshToken = createToken(member, refreshTokenValidityInMilliseconds);
        
        return new TokenDto(accessToken, refreshToken);
    }

    private String createToken(Member member, long validityInMilliseconds) {
        long now = (new Date()).getTime();
        Date validity = new Date(now + validityInMilliseconds);

        return Jwts.builder()
                .setSubject(member.getEmail())
                .claim("id", member.getId())
                .claim("role", member.getRole().name())
                .signWith(key, SignatureAlgorithm.HS512)
                .setExpiration(validity)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            log.error("JWT token validation failed: {}", e.getMessage());
            return false;
        }
    }

    public Claims validateAndGetClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public Authentication getAuthentication(String token) {
        Claims claims = validateAndGetClaims(token);
        
        UserDetails userDetails = userDetailsService.loadUserByUsername(claims.getSubject());
        
        return new UsernamePasswordAuthenticationToken(
                userDetails,
                "",
                userDetails.getAuthorities()
        );
    }

    public long getExpirationTime(String token) {
        Claims claims = validateAndGetClaims(token);
        return claims.getExpiration().getTime() - new Date().getTime();
    }
} 