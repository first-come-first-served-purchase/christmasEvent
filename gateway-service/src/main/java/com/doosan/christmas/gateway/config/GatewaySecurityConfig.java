package com.doosan.christmas.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class GatewaySecurityConfig {

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeExchange(exchange -> exchange
                        // 인증 관련
                        .pathMatchers(HttpMethod.POST, "/api/v1/auth/login").permitAll()
                        .pathMatchers(HttpMethod.GET, "/api/v1/auth/user/**").permitAll()

                        .pathMatchers(HttpMethod.POST, "/api/v1/auth/register").permitAll()

                        // 상품 서비스 - 조회 허용
                        .pathMatchers(HttpMethod.GET, "/product-service/**").permitAll()
                        .pathMatchers(HttpMethod.GET, "/api/v1/products/**").permitAll()

                        // 주문 서비스 - 두 경로 모두 허용
                        .pathMatchers(HttpMethod.POST, "/order-service/**").permitAll()
                        .pathMatchers(HttpMethod.GET, "/order-service/**").permitAll()
                        .pathMatchers(HttpMethod.PUT, "/order-service/**").permitAll()
                        .pathMatchers(HttpMethod.DELETE, "/order-service/**").permitAll()
                        .pathMatchers(HttpMethod.POST, "/api/v1/orders/**").permitAll()
                        .pathMatchers(HttpMethod.GET, "/api/v1/orders/**").permitAll()
                        .pathMatchers(HttpMethod.PUT, "/api/v1/orders/**").permitAll()
                        .pathMatchers(HttpMethod.DELETE, "/api/v1/orders/**").permitAll()

                        // 모니터링
                        .pathMatchers("/actuator/**").permitAll()

                        .anyExchange().authenticated()
                );

        return http.build();
    }
}