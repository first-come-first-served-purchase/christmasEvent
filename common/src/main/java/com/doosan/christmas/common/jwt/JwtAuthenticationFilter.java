//package com.doosan.christmas.common.jwt;
//
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.server.reactive.ServerHttpRequest;
//// import org.springframework.security.core.context.ReactiveSecurityContextHolder;
//import org.springframework.stereotype.Component;
//import org.springframework.web.server.ServerWebExchange;
//import org.springframework.web.server.WebFilter;
//import org.springframework.web.server.WebFilterChain;
//import reactor.core.publisher.Mono;
//
//@Component
//public class JwtAuthenticationFilter implements WebFilter {
//
//    private final JwtTokenProvider jwtTokenProvider;
//
//    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
//        this.jwtTokenProvider = jwtTokenProvider;
//    }
//
//    @Override
//    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
//        ServerHttpRequest request = exchange.getRequest();
//        String token = resolveToken(request);
//
//        if (token != null && jwtTokenProvider.validateToken(token)) {
//            return jwtTokenProvider.getAuthentication(token)
////                    .flatMap(authentication -> chain.filter(exchange)
////                            .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication)));
//        }
//
//        return chain.filter(exchange);
//    }
//
//    private String resolveToken(ServerHttpRequest request) {
//        String bearerToken = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
//        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
//            return bearerToken.substring(7);
//        }
//        return null;
//    }
//}
