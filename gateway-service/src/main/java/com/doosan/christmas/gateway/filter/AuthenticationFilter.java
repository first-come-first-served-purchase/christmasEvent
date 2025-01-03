package com.doosan.christmas.gateway.filter;

import com.doosan.christmas.common.jwt.JwtTokenProvider;
import com.doosan.christmas.common.dto.ResponseDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthenticationFilter implements WebFilter {
    
    private final JwtTokenProvider jwtTokenProvider;
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
        
        // "Authorization" 헤더 검사
        String token = extractToken(request);
        
        if (token != null) {
            try {
                if (!jwtTokenProvider.validateToken(token)) {
                    return handleError(exchange, "토큰이 만료되었습니다.", HttpStatus.UNAUTHORIZED);
                }
                
                Claims claims = jwtTokenProvider.getAllClaimsFromToken(token);
                addAuthorizationHeaders(exchange, claims);
                
            } catch (ExpiredJwtException e) {
                log.error("JWT 토큰이 만료되었습니다: {}", e.getMessage());
                return handleError(exchange, "토큰이 만료되었습니다. 다시 로그인해주세요.", HttpStatus.UNAUTHORIZED);
            } catch (Exception e) {
                log.error("JWT 토큰 처리 중 오류 발생: {}", e.getMessage());
                return handleError(exchange, "유효하지 않은 토큰입니다.", HttpStatus.UNAUTHORIZED);
            }
        }
        
        return chain.filter(exchange);
    }
    
    private Mono<Void> handleError(ServerWebExchange exchange, String message, HttpStatus status) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        
        ResponseDto<Void> errorResponse = ResponseDto.fail("JWT_ERROR", message);
        
        byte[] bytes;
        try {
            bytes = new ObjectMapper().writeValueAsBytes(errorResponse);
        } catch (JsonProcessingException e) {
            return Mono.error(e);
        }
        
        DataBuffer buffer = response.bufferFactory().wrap(bytes);
        return response.writeWith(Mono.just(buffer));
    }
    
    private void addAuthorizationHeaders(ServerWebExchange exchange, Claims claims) {
        exchange.mutate()
            .request(exchange.getRequest().mutate()
                .header("X-USER-ID", String.valueOf(claims.get("id")))
                .header("X-USER-ROLE", String.valueOf(claims.get("role")))
                .build())
            .build();
    }
    
    private String extractToken(ServerHttpRequest request) {
        String bearerToken = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
} 