package com.doosan.christmas.common.jwt;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter implements WebFilter {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String token = resolveToken(exchange.getRequest());

        // 토큰이 없는 경우에도 다음 필터로 진행
        if (token == null) {
            return chain.filter(exchange);
        }

        try {
            if (jwtTokenProvider.validateToken(token)) {
                Authentication authentication = (Authentication) jwtTokenProvider.getAuthentication(token);
                exchange.getAttributes().put("user", authentication.getPrincipal());
            }
        } catch (Exception e) {
            // 토큰 검증 실패시에도 다음 필터로 진행
            return chain.filter(exchange);
        }

        return chain.filter(exchange);
    }

    private String resolveToken(ServerHttpRequest request) {
        String bearerToken = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}