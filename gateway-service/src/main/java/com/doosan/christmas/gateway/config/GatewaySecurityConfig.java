package com.doosan.christmas.gateway.config;

import com.doosan.christmas.common.jwt.JwtAuthenticationFilter;
import com.doosan.christmas.common.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.core.Authentication;
import reactor.core.publisher.Mono;
import org.springframework.http.HttpMethod;
import com.doosan.christmas.gateway.filter.AuthenticationFilter;

@Configuration
@EnableWebFluxSecurity
@RequiredArgsConstructor
public class GatewaySecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationFilter authenticationFilter;

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        return http
            .csrf(ServerHttpSecurity.CsrfSpec::disable)
            .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
            .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
            .authorizeExchange(exchanges -> exchanges
                .pathMatchers(HttpMethod.POST, "/api/v1/auth/login").permitAll()
                .pathMatchers(HttpMethod.POST, "/api/v1/auth/register").permitAll()
                .pathMatchers(HttpMethod.POST, "/api/v1/auth/refresh").permitAll()
                .pathMatchers(HttpMethod.GET, "/api/v1/products/**").permitAll()
                .pathMatchers(HttpMethod.POST, "/order-service/api/v1/auth/**").permitAll()
                .pathMatchers(HttpMethod.POST, "/order-service/**").permitAll()
                .pathMatchers(HttpMethod.POST, "/api/v1/users/**").permitAll()
                .pathMatchers(HttpMethod.GET, "/api/v1/users/**").permitAll()
                .pathMatchers(HttpMethod.POST, "/api/v1/orders/**").permitAll()
                .pathMatchers(HttpMethod.GET, "/api/v1/orders/**").permitAll()
                .pathMatchers(HttpMethod.POST, "/api/v1/ok/**").permitAll()
                .pathMatchers(HttpMethod.GET, "/api/v1/ok/**").permitAll()
                .pathMatchers("/actuator/**").permitAll()
                .anyExchange().authenticated()
            )
            .addFilterAt(authenticationFilter, SecurityWebFiltersOrder.AUTHENTICATION)
            .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
            .build();
    }

    @Bean
    public ReactiveAuthenticationManager reactiveAuthenticationManager() {
        return authentication -> Mono.just(authentication);
    }
} 