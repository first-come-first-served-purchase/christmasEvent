//package com.doosan.christmas.common.jwt;
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.server.reactive.ServerHttpRequest;
//import org.springframework.security.core.context.ReactiveSecurityContextHolder;
//import org.springframework.web.server.ServerWebExchange;
//import org.springframework.web.server.WebFilter;
//import org.springframework.web.server.WebFilterChain;
//import reactor.core.publisher.Mono;
//import org.springframework.http.HttpHeaders;
//
//@RequiredArgsConstructor
//public class JwtAuthenticationWebFilter implements WebFilter {
//
//    private final JwtTokenProvider jwtTokenProvider;
//    private static final String BEARER_PREFIX = "Bearer ";
//
//    @Override
//    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
//        String token = resolveToken(exchange.getRequest());
//
//        if (token != null && jwtTokenProvider.validateToken(token)) {
//            return jwtTokenProvider.getAuthentication(token)
//                    .flatMap(authentication -> chain.filter(exchange)
//                            .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication)));
//        }
//
//        return chain.filter(exchange);
//    }
//
//    private String resolveToken(ServerHttpRequest request) {
//        String bearerToken = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
//        if (bearerToken != null && bearerToken.startsWith(BEARER_PREFIX)) {
//            return bearerToken.substring(7);
//        }
//        return null;
//    }
//}